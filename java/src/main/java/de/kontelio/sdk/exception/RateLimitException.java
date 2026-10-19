package de.kontelio.sdk.exception;

import java.net.http.HttpHeaders;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

/** 429 – too many requests. */
public class RateLimitException extends ApiException {
  public RateLimitException(int status, String message, String code, String type, String param, List<FieldError> details, HttpHeaders headers, String body) {
    super(status, message, code, type, param, details, headers, body);
  }

  /** How long to wait before retrying, if the API provided a hint. */
  public Optional<Duration> retryAfter() {
    return header("retry-after").flatMap(value -> {
      try {
        return Optional.of(Duration.ofSeconds(Long.parseLong(value.trim())));
      } catch (NumberFormatException e) {
        return Optional.empty();
      }
    });
  }
}
