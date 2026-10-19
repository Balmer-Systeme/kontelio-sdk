package de.kontelio.sdk.params;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Map;
import java.util.Objects;

/**
 * Customer reference of an invoice: an existing customer by ID, by your external reference, or
 * inline customer data (created or matched automatically).
 */
public final class InvoiceCustomer {
  private final Object value;

  private InvoiceCustomer(Object value) {
    this.value = value;
  }

  /** An existing customer by its Kontelio ID. */
  public static InvoiceCustomer byId(String id) {
    return new InvoiceCustomer(Map.of("id", Objects.requireNonNull(id, "id")));
  }

  /** An existing customer by your own reference. */
  public static InvoiceCustomer byExternalReference(String externalReference) {
    return new InvoiceCustomer(Map.of("externalReference", Objects.requireNonNull(externalReference, "externalReference")));
  }

  /** Inline customer data. With an external reference, an existing customer is reused. */
  public static InvoiceCustomer inline(CustomerParams customer) {
    return new InvoiceCustomer(Objects.requireNonNull(customer, "customer"));
  }

  @JsonValue
  Object value() {
    return value;
  }
}
