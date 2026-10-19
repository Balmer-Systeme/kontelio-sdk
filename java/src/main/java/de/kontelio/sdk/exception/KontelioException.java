package de.kontelio.sdk.exception;

/** Base class of every exception thrown by the SDK. */
public class KontelioException extends RuntimeException {
  public KontelioException(String message) {
    super(message);
  }

  public KontelioException(String message, Throwable cause) {
    super(message, cause);
  }
}
