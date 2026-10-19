package de.kontelio.sdk.exception;

import java.net.http.HttpHeaders;
import java.util.List;
import java.util.Optional;

/** The API answered with a non-2xx status code. */
public class ApiException extends KontelioException {
  private final int status;
  private final String code;
  private final String type;
  private final String param;
  private final List<FieldError> details;
  private final String requestId;
  private final HttpHeaders headers;
  private final String body;

  public ApiException(
      int status, String message, String code, String type, String param, List<FieldError> details, HttpHeaders headers, String body) {
    super(message != null ? message : "Kontelio API request failed with status " + status);
    this.status = status;
    this.code = code;
    this.type = type;
    this.param = param;
    this.details = details == null ? List.of() : List.copyOf(details);
    this.headers = headers;
    this.requestId = headers == null ? null : headers.firstValue("x-request-id").orElse(null);
    this.body = body;
  }

  /** HTTP status code. */
  public int status() { return status; }
  /** Machine-readable error code, e.g. {@code duplicate_external_reference}. */
  public String code() { return code; }
  /** Error type, e.g. {@code validation_error}. */
  public String type() { return type; }
  /** The request parameter the error relates to, if any. */
  public String param() { return param; }
  /** Field-level errors (validation errors). */
  public List<FieldError> details() { return details; }
  /** Request ID — include it in support requests. */
  public String requestId() { return requestId; }
  public HttpHeaders headers() { return headers; }
  /** Raw response body. */
  public String body() { return body; }

  protected Optional<String> header(String name) {
    return headers == null ? Optional.empty() : headers.firstValue(name);
  }
}
