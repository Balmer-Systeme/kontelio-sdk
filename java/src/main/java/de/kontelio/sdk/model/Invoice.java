package de.kontelio.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * An invoice.
 *
 * <p>{@code number} is {@code null} while the invoice is a draft. Once issued, the invoice is
 * immutable (GoBD); corrections are made through {@code cancel}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Invoice(
    String id,
    String object,
    String number,
    InvoiceStatus status,
    CustomerSummary customer,
    LocalDate issueDate,
    LocalDate serviceDate,
    ServicePeriod servicePeriod,
    LocalDate dueDate,
    String currency,
    DocumentLanguage language,
    PriceMode priceMode,
    TaxTreatment taxTreatment,
    EInvoiceFormat eInvoiceFormat,
    List<InvoiceLine> lines,
    InvoiceTotals totals,
    String buyerReference,
    String orderReference,
    String contractReference,
    String projectReference,
    String introText,
    String closingText,
    String paymentTermsText,
    String externalReference,
    String pdfUrl,
    String hostedUrl,
    OffsetDateTime issuedAt,
    OffsetDateTime sentAt,
    OffsetDateTime paidAt,
    OffsetDateTime cancelledAt,
    Map<String, String> metadata,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {}
