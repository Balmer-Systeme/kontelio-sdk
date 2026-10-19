package de.kontelio.sdk.model;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

/** Type of a webhook event. */
public enum WebhookEventType {
  INVOICE_CREATED("invoice.created"),
  INVOICE_UPDATED("invoice.updated"),
  INVOICE_ISSUED("invoice.issued"),
  INVOICE_SENT("invoice.sent"),
  INVOICE_PARTIALLY_PAID("invoice.partially_paid"),
  INVOICE_PAID("invoice.paid"),
  INVOICE_OVERDUE("invoice.overdue"),
  INVOICE_CANCELLED("invoice.cancelled"),
  CUSTOMER_CREATED("customer.created"),
  CUSTOMER_UPDATED("customer.updated"),
  CUSTOMER_DELETED("customer.deleted"),
  /** A value introduced by a newer API version than this SDK knows. */
  @JsonEnumDefaultValue
  UNKNOWN("unknown");

  private final String value;

  WebhookEventType(String value) {
    this.value = value;
  }

  /** The value used on the wire. */
  @JsonValue
  public String value() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }
}
