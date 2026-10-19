package de.kontelio.sdk.model;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

/** Document language. {@code DE_EN} produces bilingual (German/English) invoices. */
public enum DocumentLanguage {
  DE("de"),
  EN("en"),
  DE_EN("de_en"),
  /** A value introduced by a newer API version than this SDK knows. */
  @JsonEnumDefaultValue
  UNKNOWN("unknown");

  private final String value;

  DocumentLanguage(String value) {
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
