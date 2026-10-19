package de.kontelio.sdk.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import de.kontelio.sdk.RequestOptions;
import de.kontelio.sdk.Version;
import de.kontelio.sdk.exception.ApiConnectionException;
import de.kontelio.sdk.exception.ApiException;
import de.kontelio.sdk.exception.ApiTimeoutException;
import de.kontelio.sdk.exception.AuthenticationException;
import de.kontelio.sdk.exception.BadRequestException;
import de.kontelio.sdk.exception.ConflictException;
import de.kontelio.sdk.exception.FieldError;
import de.kontelio.sdk.exception.InternalServerException;
import de.kontelio.sdk.exception.KontelioException;
import de.kontelio.sdk.exception.NotFoundException;
import de.kontelio.sdk.exception.PermissionDeniedException;
import de.kontelio.sdk.exception.RateLimitException;
import de.kontelio.sdk.exception.ValidationException;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** HTTP transport with retries and idempotency keys. Internal API, may change without notice. */
public final class HttpTransport {
  private static final long MAX_RETRY_DELAY_MS = 8_000;
  private static final long MAX_RETRY_AFTER_MS = 60_000;

  private final HttpClient httpClient;
  private final String baseUrl;
  private final String apiKey;
  private final String apiVersion;
  private final Duration timeout;
  private final int maxRetries;
  private final Map<String, String> defaultHeaders;

  public HttpTransport(
      HttpClient httpClient, String baseUrl, String apiKey, String apiVersion, Duration timeout, int maxRetries, Map<String, String> defaultHeaders) {
    this.httpClient = httpClient;
    this.baseUrl = baseUrl;
    this.apiKey = apiKey;
    this.apiVersion = apiVersion;
    this.timeout = timeout;
    this.maxRetries = maxRetries;
    this.defaultHeaders = Map.copyOf(defaultHeaders);
  }

  public String baseUrl() {
    return baseUrl;
  }

  public <T> T json(String method, String path, Map<String, List<String>> query, Object body, JavaType type, RequestOptions options) {
    String text = send(method, path, query, body, "application/json", HttpResponse.BodyHandlers.ofString(), options).body();
    if (text == null || text.isEmpty()) return null;
    try {
      return Json.MAPPER.readValue(text, type);
    } catch (JsonProcessingException e) {
      throw new KontelioException("Invalid JSON in API response: " + truncate(text), e);
    }
  }

  public String text(String method, String path, Map<String, List<String>> query, RequestOptions options) {
    return send(method, path, query, null, "*/*", HttpResponse.BodyHandlers.ofString(), options).body();
  }

  public byte[] bytes(String method, String path, Map<String, List<String>> query, RequestOptions options) {
    return send(method, path, query, null, "*/*", HttpResponse.BodyHandlers.ofByteArray(), options).body();
  }

  private <B> HttpResponse<B> send(
      String method, String path, Map<String, List<String>> query, Object body, String accept, HttpResponse.BodyHandler<B> handler, RequestOptions options) {
    RequestOptions opts = options == null ? RequestOptions.DEFAULT : options;
    int retries = opts.maxRetries() != null ? opts.maxRetries() : maxRetries;
    Duration requestTimeout = opts.timeout() != null ? opts.timeout() : timeout;

    HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path + buildQuery(query)))
        .timeout(requestTimeout)
        .header("Accept", accept)
        .header("Authorization", "Bearer " + apiKey)
        .header("Kontelio-Version", apiVersion)
        .header("User-Agent", "kontelio-sdk-java/" + Version.VERSION)
        .header("X-Kontelio-Client", "kontelio-sdk-java/" + Version.VERSION);
    defaultHeaders.forEach(builder::setHeader);
    opts.headers().forEach(builder::setHeader);
    if ("POST".equals(method)) {
      builder.setHeader("Idempotency-Key", opts.idempotencyKey() != null ? opts.idempotencyKey() : "kt-sdk-" + UUID.randomUUID());
    }
    if (body != null) {
      builder.setHeader("Content-Type", "application/json");
      try {
        builder.method(method, HttpRequest.BodyPublishers.ofString(Json.MAPPER.writeValueAsString(body)));
      } catch (JsonProcessingException e) {
        throw new KontelioException("Could not serialize request body", e);
      }
    } else {
      builder.method(method, HttpRequest.BodyPublishers.noBody());
    }
    HttpRequest request = builder.build();

    for (int attempt = 0; ; attempt++) {
      HttpResponse<B> response;
      try {
        response = httpClient.send(request, handler);
      } catch (HttpTimeoutException e) {
        if (attempt < retries) { sleep(retryDelay(attempt, null)); continue; }
        throw new ApiTimeoutException("Request timed out after " + requestTimeout.toMillis() + " ms", e);
      } catch (IOException e) {
        if (attempt < retries) { sleep(retryDelay(attempt, null)); continue; }
        throw new ApiConnectionException("Connection error: " + e.getMessage(), e);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new KontelioException("Request was interrupted", e);
      }

      int status = response.statusCode();
      if (status >= 200 && status < 300) return response;
      if (attempt < retries && shouldRetry(response)) {
        sleep(retryDelay(attempt, response.headers()));
        continue;
      }
      throw toException(status, bodyAsString(response.body()), response.headers());
    }
  }

  private static boolean shouldRetry(HttpResponse<?> response) {
    String hint = response.headers().firstValue("x-should-retry").orElse(null);
    if ("true".equals(hint)) return true;
    if ("false".equals(hint)) return false;
    int status = response.statusCode();
    return status == 408 || status == 429 || status >= 500;
  }

  static long retryDelay(int attempt, HttpHeaders headers) {
    if (headers != null) {
      String retryAfter = headers.firstValue("retry-after").orElse(null);
      if (retryAfter != null) {
        try {
          long ms = (long) (Double.parseDouble(retryAfter.trim()) * 1000);
          if (ms >= 0) return Math.min(ms, MAX_RETRY_AFTER_MS);
        } catch (NumberFormatException ignored) {
          // HTTP-date or garbage: fall back to exponential backoff
        }
      }
    }
    long base = Math.min(500L << Math.min(attempt, 10), MAX_RETRY_DELAY_MS);
    return (long) (base * (0.75 + ThreadLocalRandom.current().nextDouble() * 0.25));
  }

  private static void sleep(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new KontelioException("Interrupted while waiting to retry", e);
    }
  }

  static String buildQuery(Map<String, List<String>> query) {
    if (query == null || query.isEmpty()) return "";
    StringJoiner joiner = new StringJoiner("&", "?", "");
    query.forEach((key, values) -> {
      for (String value : values) {
        joiner.add(encode(key) + "=" + encode(value));
      }
    });
    return joiner.length() > 1 ? joiner.toString() : "";
  }

  public static String encodePath(String segment) {
    if (segment == null || segment.isEmpty()) throw new IllegalArgumentException("An ID is required");
    return encode(segment);
  }

  private static String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
  }

  private static String bodyAsString(Object body) {
    if (body == null) return "";
    if (body instanceof byte[] bytes) return new String(bytes, StandardCharsets.UTF_8);
    return body.toString();
  }

  private static String truncate(String text) {
    return text.length() > 200 ? text.substring(0, 200) : text;
  }

  static ApiException toException(int status, String body, HttpHeaders headers) {
    String message = null, code = null, type = null, param = null;
    List<FieldError> details = new ArrayList<>();
    try {
      JsonNode error = body.isEmpty() ? null : Json.MAPPER.readTree(body).get("error");
      if (error != null && error.isObject()) {
        message = textOrNull(error, "message");
        code = textOrNull(error, "code");
        type = textOrNull(error, "type");
        param = textOrNull(error, "param");
        JsonNode list = error.get("details");
        if (list != null && list.isArray()) {
          for (JsonNode item : list) details.add(Json.MAPPER.treeToValue(item, FieldError.class));
        }
      }
    } catch (JsonProcessingException ignored) {
      // non-JSON error body
    }
    return switch (status) {
      case 400 -> new BadRequestException(status, message, code, type, param, details, headers, body);
      case 401 -> new AuthenticationException(status, message, code, type, param, details, headers, body);
      case 403 -> new PermissionDeniedException(status, message, code, type, param, details, headers, body);
      case 404 -> new NotFoundException(status, message, code, type, param, details, headers, body);
      case 409 -> new ConflictException(status, message, code, type, param, details, headers, body);
      case 422 -> new ValidationException(status, message, code, type, param, details, headers, body);
      case 429 -> new RateLimitException(status, message, code, type, param, details, headers, body);
      default -> status >= 500
          ? new InternalServerException(status, message, code, type, param, details, headers, body)
          : new ApiException(status, message, code, type, param, details, headers, body);
    };
  }

  private static String textOrNull(JsonNode node, String field) {
    JsonNode value = node.get(field);
    return value == null || value.isNull() ? null : value.asText();
  }
}
