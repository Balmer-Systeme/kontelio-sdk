package de.kontelio.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/** A payment recorded for an invoice. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Payment(
    String id,
    String object,
    String invoiceId,
    BigDecimal amount,
    String currency,
    LocalDate date,
    PaymentMethod method,
    String reference,
    OffsetDateTime createdAt) {}
