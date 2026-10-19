import type { RequestOptions } from "../core/http.js";
import type { Page } from "../core/pagination.js";
import type {
  DeletedObject,
  EInvoiceDownloadParams,
  Invoice,
  InvoiceCancelParams,
  InvoiceCreateParams,
  InvoiceListParams,
  InvoiceSendParams,
  InvoiceUpdateParams,
  ListResponse,
  Payment,
  PaymentCreateParams,
} from "../types.js";
import { Resource, encodeId } from "./base.js";

export class Invoices extends Resource {
  /**
   * Creates an invoice. By default the invoice is created as a draft; pass
   * `issue: true` to finalize it immediately.
   */
  create(params: InvoiceCreateParams, options?: RequestOptions): Promise<Invoice> {
    return this.http.request({ method: "POST", path: "/invoices", body: params, options });
  }

  /** Retrieves an invoice by its Kontelio ID. */
  retrieve(id: string, options?: RequestOptions): Promise<Invoice> {
    return this.http.request({ method: "GET", path: `/invoices/${encodeId(id)}`, options });
  }

  /** Updates a draft invoice. Issued invoices are immutable and return a 409. */
  update(id: string, params: InvoiceUpdateParams, options?: RequestOptions): Promise<Invoice> {
    return this.http.request({ method: "PATCH", path: `/invoices/${encodeId(id)}`, body: params, options });
  }

  /** Lists invoices. The returned page can be iterated with `for await`. */
  list(params: InvoiceListParams = {}, options?: RequestOptions): Promise<Page<Invoice, InvoiceListParams>> {
    return this.paginate<Invoice, InvoiceListParams>("/invoices", params, options);
  }

  /** Deletes a draft invoice. Issued invoices must be cancelled instead. */
  delete(id: string, options?: RequestOptions): Promise<DeletedObject> {
    return this.http.request({ method: "DELETE", path: `/invoices/${encodeId(id)}`, options });
  }

  /** Finalizes a draft: assigns the sequential number and renders the documents. */
  issue(id: string, options?: RequestOptions): Promise<Invoice> {
    return this.http.request({ method: "POST", path: `/invoices/${encodeId(id)}/issue`, options });
  }

  /** Sends an issued invoice to the customer by email. */
  send(id: string, params: InvoiceSendParams = {}, options?: RequestOptions): Promise<Invoice> {
    return this.http.request({ method: "POST", path: `/invoices/${encodeId(id)}/send`, body: params, options });
  }

  /**
   * Cancels an issued invoice by creating a cancellation invoice
   * (Stornorechnung), as required by German bookkeeping rules (GoBD).
   */
  cancel(id: string, params: InvoiceCancelParams = {}, options?: RequestOptions): Promise<Invoice> {
    return this.http.request({ method: "POST", path: `/invoices/${encodeId(id)}/cancel`, body: params, options });
  }

  /** Records a (partial) payment received for an invoice. */
  recordPayment(id: string, params: PaymentCreateParams, options?: RequestOptions): Promise<Payment> {
    return this.http.request({ method: "POST", path: `/invoices/${encodeId(id)}/payments`, body: params, options });
  }

  /** Lists the payments recorded for an invoice. */
  async listPayments(id: string, options?: RequestOptions): Promise<Payment[]> {
    const response = await this.http.request<ListResponse<Payment>>({
      method: "GET",
      path: `/invoices/${encodeId(id)}/payments`,
      options,
    });
    return response.data;
  }

  /** Downloads the invoice PDF (for `zugferd`, the PDF/A-3 with embedded XML). */
  downloadPdf(id: string, options?: RequestOptions): Promise<Uint8Array> {
    return this.http.request({
      method: "GET",
      path: `/invoices/${encodeId(id)}/pdf`,
      responseType: "binary",
      options,
    });
  }

  /** Downloads the structured e-invoice XML (XRechnung CII/UBL or ZUGFeRD XML). */
  downloadEInvoice(id: string, params: EInvoiceDownloadParams = {}, options?: RequestOptions): Promise<string> {
    return this.http.request({
      method: "GET",
      path: `/invoices/${encodeId(id)}/e-invoice`,
      query: params,
      responseType: "text",
      options,
    });
  }
}
