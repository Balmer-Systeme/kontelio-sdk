package de.kontelio.sdk.params;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.kontelio.sdk.model.DocumentLanguage;
import de.kontelio.sdk.model.EInvoiceFormat;
import de.kontelio.sdk.model.PriceMode;
import de.kontelio.sdk.model.ServicePeriod;
import de.kontelio.sdk.model.TaxTreatment;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data to create an invoice, or to update a draft (only the given fields are changed).
 *
 * <pre>{@code
 * new InvoiceParams()
 *     .customer(InvoiceCustomer.byExternalReference("CRM-4711"))
 *     .externalReference("ORDER-2026-001234")
 *     .addLine(InvoiceLineParams.of("Software Development", "95.00").quantity(12).unit("HUR"))
 *     .eInvoiceFormat(EInvoiceFormat.ZUGFERD)
 *     .issue(true);
 * }</pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public final class InvoiceParams {
  private InvoiceCustomer customer;
  private List<InvoiceLineParams> lines;
  private String externalReference;
  private LocalDate issueDate;
  private LocalDate serviceDate;
  private ServicePeriod servicePeriod;
  private LocalDate dueDate;
  private Integer paymentTermsDays;
  private String currency;
  private DocumentLanguage language;
  private PriceMode priceMode;
  private TaxTreatment taxTreatment;
  private EInvoiceFormat eInvoiceFormat;
  private BigDecimal documentDiscountPercent;
  private BigDecimal documentDiscountAmount;
  private String buyerReference;
  private String orderReference;
  private String contractReference;
  private String projectReference;
  private String introText;
  private String closingText;
  private String paymentTermsText;
  private String internalNotes;
  private Boolean issue;
  private Map<String, String> metadata;

  public InvoiceParams customer(InvoiceCustomer customer) { this.customer = customer; return this; }

  public InvoiceParams addLine(InvoiceLineParams line) {
    if (lines == null) lines = new ArrayList<>();
    lines.add(line);
    return this;
  }

  /** Replaces all lines. */
  public InvoiceParams lines(List<InvoiceLineParams> lines) { this.lines = new ArrayList<>(lines); return this; }
  /** Your unique reference (e.g. order number). A second invoice with the same reference is rejected. */
  public InvoiceParams externalReference(String externalReference) { this.externalReference = externalReference; return this; }
  /** Defaults to today. */
  public InvoiceParams issueDate(LocalDate issueDate) { this.issueDate = issueDate; return this; }
  /** Date of supply (Leistungsdatum). Use either this or {@link #servicePeriod}. */
  public InvoiceParams serviceDate(LocalDate serviceDate) { this.serviceDate = serviceDate; return this; }
  public InvoiceParams servicePeriod(ServicePeriod servicePeriod) { this.servicePeriod = servicePeriod; return this; }
  /** Use either this or {@link #paymentTermsDays}. */
  public InvoiceParams dueDate(LocalDate dueDate) { this.dueDate = dueDate; return this; }
  public InvoiceParams paymentTermsDays(int paymentTermsDays) { this.paymentTermsDays = paymentTermsDays; return this; }
  /** ISO 4217 code. Defaults to EUR. */
  public InvoiceParams currency(String currency) { this.currency = currency; return this; }
  public InvoiceParams language(DocumentLanguage language) { this.language = language; return this; }
  /** Defaults to {@link PriceMode#NET}. */
  public InvoiceParams priceMode(PriceMode priceMode) { this.priceMode = priceMode; return this; }
  /** Defaults to {@link TaxTreatment#STANDARD}. */
  public InvoiceParams taxTreatment(TaxTreatment taxTreatment) { this.taxTreatment = taxTreatment; return this; }
  public InvoiceParams eInvoiceFormat(EInvoiceFormat eInvoiceFormat) { this.eInvoiceFormat = eInvoiceFormat; return this; }
  public InvoiceParams documentDiscountPercent(BigDecimal value) { this.documentDiscountPercent = value; return this; }
  public InvoiceParams documentDiscountAmount(BigDecimal value) { this.documentDiscountAmount = value; return this; }
  /** Buyer reference (BT-10). Required for XRechnung; usually the Leitweg-ID. */
  public InvoiceParams buyerReference(String buyerReference) { this.buyerReference = buyerReference; return this; }
  public InvoiceParams orderReference(String orderReference) { this.orderReference = orderReference; return this; }
  public InvoiceParams contractReference(String contractReference) { this.contractReference = contractReference; return this; }
  public InvoiceParams projectReference(String projectReference) { this.projectReference = projectReference; return this; }
  public InvoiceParams introText(String introText) { this.introText = introText; return this; }
  public InvoiceParams closingText(String closingText) { this.closingText = closingText; return this; }
  public InvoiceParams paymentTermsText(String paymentTermsText) { this.paymentTermsText = paymentTermsText; return this; }
  /** Internal notes, never printed on the invoice. */
  public InvoiceParams internalNotes(String internalNotes) { this.internalNotes = internalNotes; return this; }

  /**
   * When {@code true}, the invoice is finalized immediately: it receives its sequential number
   * and becomes immutable (GoBD). Ignored on update.
   */
  public InvoiceParams issue(boolean issue) { this.issue = issue; return this; }

  public InvoiceParams metadata(String key, String value) {
    if (metadata == null) metadata = new LinkedHashMap<>();
    metadata.put(key, value);
    return this;
  }
}
