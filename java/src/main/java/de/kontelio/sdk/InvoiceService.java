package de.kontelio.sdk;

import static de.kontelio.sdk.internal.HttpTransport.encodePath;

import com.fasterxml.jackson.databind.JavaType;
import de.kontelio.sdk.internal.HttpTransport;
import de.kontelio.sdk.internal.Json;
import de.kontelio.sdk.model.DeletedObject;
import de.kontelio.sdk.model.EInvoiceFormat;
import de.kontelio.sdk.model.Invoice;
import de.kontelio.sdk.model.Payment;
import de.kontelio.sdk.params.InvoiceCancelParams;
import de.kontelio.sdk.params.InvoiceListParams;
import de.kontelio.sdk.params.InvoiceParams;
import de.kontelio.sdk.params.InvoiceSendParams;
import de.kontelio.sdk.params.PaymentParams;
import java.util.List;
import java.util.Map;

/** Invoices: create, issue, send, cancel, payments and document downloads. */
public final class InvoiceService {
  private static final JavaType INVOICE = Json.MAPPER.constructType(Invoice.class);
  private static final JavaType PAYMENT = Json.MAPPER.constructType(Payment.class);
  private static final JavaType DELETED = Json.MAPPER.constructType(DeletedObject.class);
  private static final JavaType INVOICE_LIST = Json.MAPPER.getTypeFactory().constructParametricType(Page.Envelope.class, Invoice.class);
  private static final JavaType PAYMENT_LIST = Json.MAPPER.getTypeFactory().constructParametricType(Page.Envelope.class, Payment.class);

  private final HttpTransport http;

  InvoiceService(HttpTransport http) {
    this.http = http;
  }

  /** Creates an invoice — a draft, or an issued invoice when {@code issue(true)} is set. */
  public Invoice create(InvoiceParams params) {
    return create(params, null);
  }

  public Invoice create(InvoiceParams params, RequestOptions options) {
    return http.json("POST", "/invoices", null, params, INVOICE, options);
  }

  public Invoice retrieve(String id) {
    return retrieve(id, null);
  }

  public Invoice retrieve(String id, RequestOptions options) {
    return http.json("GET", "/invoices/" + encodePath(id), null, null, INVOICE, options);
  }

  /** Updates a draft. Issued invoices are immutable and raise a {@code ConflictException}. */
  public Invoice update(String id, InvoiceParams params) {
    return update(id, params, null);
  }

  public Invoice update(String id, InvoiceParams params, RequestOptions options) {
    return http.json("PATCH", "/invoices/" + encodePath(id), null, params, INVOICE, options);
  }

  public Page<Invoice> list() {
    return list(new InvoiceListParams(), null);
  }

  public Page<Invoice> list(InvoiceListParams params) {
    return list(params, null);
  }

  public Page<Invoice> list(InvoiceListParams params, RequestOptions options) {
    return fetchPage(params.toQuery(), params, options);
  }

  private Page<Invoice> fetchPage(Map<String, List<String>> query, InvoiceListParams params, RequestOptions options) {
    Page.Envelope<Invoice> envelope = http.json("GET", "/invoices", query, null, INVOICE_LIST, options);
    return new Page<>(envelope, cursor -> fetchPage(params.toQuery(cursor), params, options));
  }

  /** Deletes a draft. Issued invoices must be cancelled instead. */
  public DeletedObject delete(String id) {
    return delete(id, null);
  }

  public DeletedObject delete(String id, RequestOptions options) {
    return http.json("DELETE", "/invoices/" + encodePath(id), null, null, DELETED, options);
  }

  /** Finalizes a draft: assigns the sequential number and renders the documents. */
  public Invoice issue(String id) {
    return issue(id, null);
  }

  public Invoice issue(String id, RequestOptions options) {
    return http.json("POST", "/invoices/" + encodePath(id) + "/issue", null, null, INVOICE, options);
  }

  /** Sends an issued invoice to the customer by email. */
  public Invoice send(String id) {
    return send(id, new InvoiceSendParams(), null);
  }

  public Invoice send(String id, InvoiceSendParams params) {
    return send(id, params, null);
  }

  public Invoice send(String id, InvoiceSendParams params, RequestOptions options) {
    return http.json("POST", "/invoices/" + encodePath(id) + "/send", null, params, INVOICE, options);
  }

  /** Cancels an issued invoice by creating a cancellation invoice (Stornorechnung). */
  public Invoice cancel(String id) {
    return cancel(id, new InvoiceCancelParams(), null);
  }

  public Invoice cancel(String id, InvoiceCancelParams params) {
    return cancel(id, params, null);
  }

  public Invoice cancel(String id, InvoiceCancelParams params, RequestOptions options) {
    return http.json("POST", "/invoices/" + encodePath(id) + "/cancel", null, params, INVOICE, options);
  }

  /** Records a (partial) payment received for an invoice. */
  public Payment recordPayment(String id, PaymentParams params) {
    return recordPayment(id, params, null);
  }

  public Payment recordPayment(String id, PaymentParams params, RequestOptions options) {
    return http.json("POST", "/invoices/" + encodePath(id) + "/payments", null, params, PAYMENT, options);
  }

  public List<Payment> listPayments(String id) {
    return listPayments(id, null);
  }

  public List<Payment> listPayments(String id, RequestOptions options) {
    Page.Envelope<Payment> envelope = http.json("GET", "/invoices/" + encodePath(id) + "/payments", null, null, PAYMENT_LIST, options);
    return envelope.data() == null ? List.of() : List.copyOf(envelope.data());
  }

  /** Downloads the PDF (for ZUGFeRD: the PDF/A-3 with embedded XML). */
  public byte[] downloadPdf(String id) {
    return downloadPdf(id, null);
  }

  public byte[] downloadPdf(String id, RequestOptions options) {
    return http.bytes("GET", "/invoices/" + encodePath(id) + "/pdf", null, options);
  }

  /** Downloads the e-invoice XML in the invoice's default format. */
  public String downloadEInvoice(String id) {
    return downloadEInvoice(id, null, null);
  }

  /**
   * Downloads the e-invoice XML (XRechnung CII/UBL or ZUGFeRD XML).
   *
   * @param format the XML format, or {@code null} for the invoice's default
   */
  public String downloadEInvoice(String id, EInvoiceFormat format, RequestOptions options) {
    if (format == EInvoiceFormat.PDF) throw new IllegalArgumentException("Use downloadPdf for PDF documents");
    Map<String, List<String>> query = format == null ? null : Map.of("format", List.of(format.value()));
    return http.text("GET", "/invoices/" + encodePath(id) + "/e-invoice", query, options);
  }
}
