package de.kontelio.sdk.model;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

/** How a payment was received. */
public enum PaymentMethod {
  BANK_TRANSFER("bank_transfer"),
  DIRECT_DEBIT("direct_debit"),
  CARD("card"),
  PAYPAL("paypal"),
  CASH("cash"),
  OTHER("other"),
  /** A value introduced by a newer API version than this SDK knows. */
  @JsonEnumDefaultValue
  UNKNOWN("unknown");

  private final String value;

  PaymentMethod(String value) {
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
