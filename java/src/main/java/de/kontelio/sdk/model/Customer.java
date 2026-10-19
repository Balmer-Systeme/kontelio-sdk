package de.kontelio.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * A customer (Debitor) invoices are addressed to.
 *
 * @param id Kontelio ID
 * @param object always {@code customer}
 * @param type kind of customer
 * @param number customer number assigned by Kontelio, e.g. {@code K-10023}
 * @param name name
 * @param email email address
 * @param phone phone number
 * @param vatId EU VAT identification number (USt-IdNr.)
 * @param taxNumber German tax number (Steuernummer)
 * @param leitwegId Leitweg-ID for XRechnung invoices to public authorities
 * @param billingAddress billing address
 * @param language preferred document language
 * @param paymentTermsDays default payment terms in days
 * @param eInvoiceFormat default e-invoice format
 * @param externalReference your own identifier, unique per account
 * @param metadata free-form metadata
 * @param createdAt creation time
 * @param updatedAt last modification time
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Customer(
    String id,
    String object,
    CustomerType type,
    String number,
    String name,
    String email,
    String phone,
    String vatId,
    String taxNumber,
    String leitwegId,
    Address billingAddress,
    DocumentLanguage language,
    Integer paymentTermsDays,
    EInvoiceFormat eInvoiceFormat,
    String externalReference,
    Map<String, String> metadata,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {}
