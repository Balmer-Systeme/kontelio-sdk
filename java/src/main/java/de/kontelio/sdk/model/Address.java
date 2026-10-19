package de.kontelio.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Postal address.
 *
 * @param line1 street and house number
 * @param line2 additional address line
 * @param postalCode postal code
 * @param city city
 * @param country ISO 3166-1 alpha-2 country code, e.g. {@code DE}
 * @param state state or region
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Address(String line1, String line2, String postalCode, String city, String country, String state) {

  /** Creates an address without second line and state. */
  public static Address of(String line1, String postalCode, String city, String country) {
    return new Address(line1, null, postalCode, city, country, null);
  }
}
