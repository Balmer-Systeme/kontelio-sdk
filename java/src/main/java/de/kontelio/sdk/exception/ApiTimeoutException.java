package de.kontelio.sdk.exception;

/** The request did not complete within the configured timeout. */
public class ApiTimeoutException extends ApiConnectionException {
  public ApiTimeoutException(String message, Throwable cause) {
    super(message, cause);
  }
}
