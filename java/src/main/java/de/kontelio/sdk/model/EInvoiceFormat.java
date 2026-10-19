package de.kontelio.sdk.model;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

/** Output format of the invoice document: plain PDF, ZUGFeRD (EN 16931) or XRechnung (CII/UBL). */
public enum EInvoiceFormat {
  PDF("pdf"),
  ZUGFERD("zugferd"),
  XRECHNUNG_CII("xrechnung_cii"),
  XRECHNUNG_UBL("xrechnung_ubl"),
  /** A value introduced by a newer API version than this SDK knows. */
  @JsonEnumDefaultValue
  UNKNOWN("unknown");

  private final String value;

  EInvoiceFormat(String value) {
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
