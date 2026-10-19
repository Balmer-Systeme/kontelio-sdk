package de.kontelio.sdk.model;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

/** VAT treatment under German VAT law (UStG): standard, small business (§ 19), reverse charge (§ 13b), intra-community supply (§ 4 Nr. 1b), export (§ 4 Nr. 1a), exempt, or not taxable in Germany. */
public enum TaxTreatment {
  STANDARD("standard"),
  SMALL_BUSINESS("small_business"),
  REVERSE_CHARGE("reverse_charge"),
  INTRA_COMMUNITY_SUPPLY("intra_community_supply"),
  EXPORT("export"),
  EXEMPT("exempt"),
  NOT_TAXABLE_IN_GERMANY("not_taxable_in_germany"),
  /** A value introduced by a newer API version than this SDK knows. */
  @JsonEnumDefaultValue
  UNKNOWN("unknown");

  private final String value;

  TaxTreatment(String value) {
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
