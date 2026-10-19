package de.kontelio.sdk.model;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

/** Lifecycle status of an invoice. */
public enum InvoiceStatus {
  DRAFT("draft"),
  ISSUED("issued"),
  PARTIALLY_PAID("partially_paid"),
  PAID("paid"),
  OVERDUE("overdue"),
  CANCELLED("cancelled"),
  CREDITED("credited"),
  /** A value introduced by a newer API version than this SDK knows. */
  @JsonEnumDefaultValue
  UNKNOWN("unknown");

  private final String value;

  InvoiceStatus(String value) {
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
