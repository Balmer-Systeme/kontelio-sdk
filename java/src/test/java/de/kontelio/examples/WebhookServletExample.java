package de.kontelio.examples;

import de.kontelio.sdk.KontelioClient;
import de.kontelio.sdk.WebhookEvent;
import de.kontelio.sdk.Webhooks;
import de.kontelio.sdk.exception.WebhookVerificationException;

/**
 * Framework-agnostic webhook handling. In Spring Boot, take the body as {@code @RequestBody
 * String} (raw, not a parsed object) and the header via {@code @RequestHeader}.
 *
 * <pre>{@code
 * @PostMapping("/webhooks/kontelio")
 * ResponseEntity<Void> kontelio(@RequestBody String body, @RequestHeader("Kontelio-Signature") String sig) {
 *   return ResponseEntity.status(handler.handle(body, sig)).build();
 * }
 * }</pre>
 */
public class WebhookServletExample {
  private final KontelioClient kontelio = KontelioClient.fromEnv();
  private final String secret = System.getenv("KONTELIO_WEBHOOK_SECRET");

  /** Returns the HTTP status to answer with. */
  public int handle(String rawBody, String signatureHeader) {
    WebhookEvent event;
    try {
      event = kontelio.webhooks().constructEvent(rawBody, signatureHeader, secret);
    } catch (WebhookVerificationException e) {
      return 400;
    }

    switch (event.type()) {
      case INVOICE_PAID -> System.out.println("Invoice " + event.invoice().number() + " was paid");
      case INVOICE_OVERDUE -> System.out.println("Invoice " + event.invoice().number() + " is overdue");
      default -> { }
    }
    return 204;
  }

  static String headerName() {
    return Webhooks.SIGNATURE_HEADER;
  }
}
