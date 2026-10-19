package de.kontelio.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

/** A line item of an {@link Invoice}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record InvoiceLine(
    String id,
    int position,
    String name,
    String description,
    String sku,
    BigDecimal quantity,
    String unit,
    BigDecimal unitPrice,
    BigDecimal vatRate,
    TaxTreatment taxTreatment,
    BigDecimal discountPercent,
    BigDecimal discountAmount,
    BigDecimal netAmount,
    BigDecimal vatAmount,
    BigDecimal grossAmount,
    ServicePeriod servicePeriod,
    String externalReference) {}
