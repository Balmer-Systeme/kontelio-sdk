package de.kontelio.sdk.exception;

/** The request could not reach the API (DNS, TLS, connection reset, ...). */
public class ApiConnectionException extends KontelioException {
  public ApiConnectionException(String message, Throwable cause) {
    super(message, cause);
  }
}
