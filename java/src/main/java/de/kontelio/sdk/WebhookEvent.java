package de.kontelio.sdk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import de.kontelio.sdk.exception.KontelioException;
import de.kontelio.sdk.internal.Json;
import de.kontelio.sdk.model.Customer;
import de.kontelio.sdk.model.Invoice;
import de.kontelio.sdk.model.WebhookEventType;
import java.time.OffsetDateTime;

/**
 * A webhook event.
 *
 * @param id event ID — use it to deduplicate deliveries
 * @param object always {@code event}
 * @param type event type
 * @param createdAt creation time
 * @param livemode {@code false} for events triggered with a test key
 * @param data payload; use {@link #invoice()} or {@link #customer()} to read it typed
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WebhookEvent(String id, String object, WebhookEventType type, OffsetDateTime createdAt, boolean livemode, Data data) {

  /** Event payload. */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Data(JsonNode object) {}

  /** The invoice of an {@code invoice.*} event. */
  public Invoice invoice() {
    return dataAs(Invoice.class);
  }

  /** The customer of a {@code customer.*} event. */
  public Customer customer() {
    return dataAs(Customer.class);
  }

  /** The event payload as any type. */
  public <T> T dataAs(Class<T> type) {
    if (data == null || data.object() == null) return null;
    try {
      return Json.MAPPER.treeToValue(data.object(), type);
    } catch (JsonProcessingException e) {
      throw new KontelioException("Could not read webhook payload as " + type.getSimpleName(), e);
    }
  }
}
