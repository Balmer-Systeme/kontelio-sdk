package de.kontelio.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;

/** Period in which the service was performed (Leistungszeitraum). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ServicePeriod(LocalDate start, LocalDate end) {

  public static ServicePeriod of(LocalDate start, LocalDate end) {
    return new ServicePeriod(start, end);
  }
}
