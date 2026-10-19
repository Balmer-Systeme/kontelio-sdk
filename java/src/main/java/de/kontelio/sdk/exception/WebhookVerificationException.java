package de.kontelio.sdk.exception;

/** A webhook payload could not be verified. */
public class WebhookVerificationException extends KontelioException {
  public WebhookVerificationException(String message) {
    super(message);
  }

  public WebhookVerificationException(String message, Throwable cause) {
    super(message, cause);
  }
}
