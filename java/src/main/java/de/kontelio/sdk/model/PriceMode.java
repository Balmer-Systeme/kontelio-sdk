package de.kontelio.sdk.model;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

/** Whether unit prices are net (excl. VAT) or gross (incl. VAT). */
public enum PriceMode {
  NET("net"),
  GROSS("gross"),
  /** A value introduced by a newer API version than this SDK knows. */
  @JsonEnumDefaultValue
  UNKNOWN("unknown");

  private final String value;

  PriceMode(String value) {
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
