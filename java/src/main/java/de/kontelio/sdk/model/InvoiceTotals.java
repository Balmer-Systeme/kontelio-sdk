package de.kontelio.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.List;

/** Totals of an {@link Invoice}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record InvoiceTotals(
    BigDecimal netAmount,
    BigDecimal vatAmount,
    BigDecimal grossAmount,
    BigDecimal amountPaid,
    BigDecimal amountDue,
    List<VatBreakdownEntry> vatBreakdown) {}
