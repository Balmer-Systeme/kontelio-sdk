package de.kontelio.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

/** Net and VAT amounts per VAT rate. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VatBreakdownEntry(BigDecimal vatRate, TaxTreatment taxTreatment, BigDecimal netAmount, BigDecimal vatAmount) {}
