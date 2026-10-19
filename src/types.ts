/**
 * Public data types of the Kontelio API.
 *
 * Monetary amounts are returned as decimal strings (e.g. `"1190.00"`) to avoid
 * floating point rounding issues. Request parameters accept either a number or
 * a decimal string.
 */

/** A decimal value. Accepts `1000`, `1000.5` or `"1000.50"`. */
export type Decimal = number | string;

/** ISO 8601 calendar date, e.g. `"2026-10-19"`. */
export type IsoDate = string;

/** ISO 8601 timestamp, e.g. `"2026-10-19T13:54:00Z"`. */
export type IsoDateTime = string;

/** ISO 3166-1 alpha-2 country code, e.g. `"DE"`, `"AT"`, `"FR"`. */
export type CountryCode = string;

/** ISO 4217 currency code, e.g. `"EUR"`. */
export type CurrencyCode = string;

/** Free-form key/value metadata stored with an object (max. 50 keys). */
export type Metadata = Record<string, string>;

export interface Address {
  line1: string;
  line2?: string | null;
  postalCode: string;
  city: string;
  /** ISO 3166-1 alpha-2 country code. */
  country: CountryCode;
  state?: string | null;
}

// ---------------------------------------------------------------------------
// Pagination
// ---------------------------------------------------------------------------

export interface ListParams {
  /** Number of items per page (1–100, default 25). */
  limit?: number;
  /** Cursor returned as `nextCursor` by the previous page. */
  cursor?: string;
}

export interface ListResponse<T> {
  object: "list";
  data: T[];
  hasMore: boolean;
  nextCursor: string | null;
}

// ---------------------------------------------------------------------------
// Customers
// ---------------------------------------------------------------------------

export type CustomerType = "company" | "individual" | "public_authority";

export interface Customer {
  id: string;
  object: "customer";
  type: CustomerType;
  /** Customer number assigned by Kontelio, e.g. `"K-10023"`. */
  number: string;
  name: string;
  email: string | null;
  phone: string | null;
  /** EU VAT identification number (USt-IdNr.), e.g. `"DE123456789"`. */
  vatId: string | null;
  /** German tax number (Steuernummer). */
  taxNumber: string | null;
  /** Leitweg-ID, required for XRechnung invoices to German public authorities. */
  leitwegId: string | null;
  billingAddress: Address | null;
  /** Preferred document language for invoices sent to this customer. */
  language: DocumentLanguage;
  /** Default payment terms in days. */
  paymentTermsDays: number | null;
  /** Default e-invoice format for this customer. */
  eInvoiceFormat: EInvoiceFormat;
  /** Your own identifier for this customer (e.g. ERP/CRM ID). Unique per account. */
  externalReference: string | null;
  metadata: Metadata;
  createdAt: IsoDateTime;
  updatedAt: IsoDateTime;
}

export interface CustomerCreateParams {
  type?: CustomerType;
  name: string;
  email?: string;
  phone?: string;
  vatId?: string;
  taxNumber?: string;
  leitwegId?: string;
  billingAddress?: Address;
  language?: DocumentLanguage;
  paymentTermsDays?: number;
  eInvoiceFormat?: EInvoiceFormat;
  externalReference?: string;
  metadata?: Metadata;
}

export type CustomerUpdateParams = Partial<CustomerCreateParams>;

export interface CustomerListParams extends ListParams {
  /** Full-text search over name, number, email and VAT ID. */
  search?: string;
  externalReference?: string;
  email?: string;
}

// ---------------------------------------------------------------------------
// Invoices
// ---------------------------------------------------------------------------

export type InvoiceStatus =
  | "draft"
  | "issued"
  | "partially_paid"
  | "paid"
  | "overdue"
  | "cancelled"
  | "credited";

/**
 * Output format of the invoice document.
 *
 * - `pdf` – plain PDF invoice
 * - `zugferd` – hybrid PDF/A-3 with embedded EN 16931 XML (ZUGFeRD / Factur-X)
 * - `xrechnung_cii` – XRechnung, UN/CEFACT CII syntax
 * - `xrechnung_ubl` – XRechnung, OASIS UBL syntax
 */
export type EInvoiceFormat = "pdf" | "zugferd" | "xrechnung_cii" | "xrechnung_ubl";

/** Document language. `de_en` produces bilingual (German/English) invoices. */
export type DocumentLanguage = "de" | "en" | "de_en";

/** Whether line unit prices are net (excl. VAT) or gross (incl. VAT). */
export type PriceMode = "net" | "gross";

/**
 * VAT treatment of an invoice or invoice line under German VAT law (UStG).
 *
 * - `standard` – taxable supply with German VAT (19 % / 7 %)
 * - `small_business` – Kleinunternehmerregelung, § 19 UStG (no VAT charged)
 * - `reverse_charge` – recipient owes the VAT, § 13b UStG
 * - `intra_community_supply` – tax-free intra-community supply of goods, § 4 Nr. 1b UStG
 * - `export` – tax-free export outside the EU, § 4 Nr. 1a UStG
 * - `exempt` – other tax-exempt supplies (e.g. § 4 Nr. 14 UStG)
 * - `not_taxable_in_germany` – place of supply outside Germany (non-taxable)
 */
export type TaxTreatment =
  | "standard"
  | "small_business"
  | "reverse_charge"
  | "intra_community_supply"
  | "export"
  | "exempt"
  | "not_taxable_in_germany";

/** UN/ECE Recommendation 20 unit code, e.g. `C62` (piece), `HUR` (hour), `DAY`, `MON`. */
export type UnitCode = string;

export interface ServicePeriod {
  start: IsoDate;
  end: IsoDate;
}

export interface InvoiceLineParams {
  /** Line title shown on the invoice. */
  name: string;
  description?: string;
  /** Optional article/service number. */
  sku?: string;
  /** Defaults to `1`. */
  quantity?: Decimal;
  /** UN/ECE Rec. 20 unit code. Defaults to `C62` (piece). */
  unit?: UnitCode;
  /** Unit price, net or gross depending on the invoice `priceMode`. */
  unitPrice: Decimal;
  /** VAT rate in percent, e.g. `19`, `7` or `0`. Defaults to the account's standard rate. */
  vatRate?: Decimal;
  /** Overrides the invoice-level `taxTreatment` for this line. */
  taxTreatment?: TaxTreatment;
  discountPercent?: Decimal;
  discountAmount?: Decimal;
  servicePeriod?: ServicePeriod;
  /** Reference to the line in your own system (e.g. order line ID). */
  externalReference?: string;
}

export interface InvoiceLine {
  id: string;
  position: number;
  name: string;
  description: string | null;
  sku: string | null;
  quantity: string;
  unit: UnitCode;
  unitPrice: string;
  vatRate: string;
  taxTreatment: TaxTreatment;
  discountPercent: string | null;
  discountAmount: string | null;
  netAmount: string;
  vatAmount: string;
  grossAmount: string;
  servicePeriod: ServicePeriod | null;
  externalReference: string | null;
}

export interface VatBreakdownEntry {
  vatRate: string;
  taxTreatment: TaxTreatment;
  netAmount: string;
  vatAmount: string;
}

export interface InvoiceTotals {
  netAmount: string;
  vatAmount: string;
  grossAmount: string;
  amountPaid: string;
  amountDue: string;
  vatBreakdown: VatBreakdownEntry[];
}

/**
 * Customer reference for an invoice. Either pass the ID of an existing
 * customer, look one up by your `externalReference`, or pass inline customer
 * data — Kontelio will create (or match) the customer automatically.
 */
export type InvoiceCustomerParams =
  | { id: string }
  | { externalReference: string }
  | CustomerCreateParams;

export interface InvoiceCreateParams {
  customer: InvoiceCustomerParams;
  lines: InvoiceLineParams[];
  /**
   * Your own unique reference (e.g. order number). Kontelio rejects a second
   * invoice with the same reference, which makes retries safe.
   */
  externalReference?: string;
  /** Defaults to today. */
  issueDate?: IsoDate;
  /** Date of supply (Leistungsdatum). Use either `serviceDate` or `servicePeriod`. */
  serviceDate?: IsoDate;
  servicePeriod?: ServicePeriod;
  /** Use either `dueDate` or `paymentTermsDays`. */
  dueDate?: IsoDate;
  paymentTermsDays?: number;
  /** Defaults to `EUR`. */
  currency?: CurrencyCode;
  /** Defaults to the customer's language. */
  language?: DocumentLanguage;
  /** Defaults to `net`. */
  priceMode?: PriceMode;
  /** Defaults to `standard`. */
  taxTreatment?: TaxTreatment;
  /** Defaults to the customer's preferred format. */
  eInvoiceFormat?: EInvoiceFormat;
  documentDiscountPercent?: Decimal;
  documentDiscountAmount?: Decimal;
  /** Buyer reference (BT-10). Required for XRechnung; usually the Leitweg-ID. */
  buyerReference?: string;
  orderReference?: string;
  contractReference?: string;
  projectReference?: string;
  introText?: string;
  closingText?: string;
  paymentTermsText?: string;
  /** Internal notes, never printed on the invoice. */
  internalNotes?: string;
  /**
   * When `true`, the invoice is finalized immediately: it receives its
   * sequential invoice number and becomes immutable (GoBD). Defaults to `false`
   * (the invoice is created as a draft).
   */
  issue?: boolean;
  metadata?: Metadata;
}

export type InvoiceUpdateParams = Partial<Omit<InvoiceCreateParams, "issue">>;

export interface InvoiceListParams extends ListParams {
  status?: InvoiceStatus | InvoiceStatus[];
  customerId?: string;
  externalReference?: string;
  /** Issue date lower bound (inclusive). */
  issueDateFrom?: IsoDate;
  /** Issue date upper bound (inclusive). */
  issueDateTo?: IsoDate;
  search?: string;
}

export interface Invoice {
  id: string;
  object: "invoice";
  /** Sequential invoice number. `null` while the invoice is a draft. */
  number: string | null;
  status: InvoiceStatus;
  customer: Pick<Customer, "id" | "number" | "name" | "vatId" | "billingAddress">;
  issueDate: IsoDate | null;
  serviceDate: IsoDate | null;
  servicePeriod: ServicePeriod | null;
  dueDate: IsoDate | null;
  currency: CurrencyCode;
  language: DocumentLanguage;
  priceMode: PriceMode;
  taxTreatment: TaxTreatment;
  eInvoiceFormat: EInvoiceFormat;
  lines: InvoiceLine[];
  totals: InvoiceTotals;
  buyerReference: string | null;
  orderReference: string | null;
  contractReference: string | null;
  projectReference: string | null;
  introText: string | null;
  closingText: string | null;
  paymentTermsText: string | null;
  externalReference: string | null;
  /** Temporary signed URL of the rendered PDF, available once issued. */
  pdfUrl: string | null;
  /** Public link where your customer can view and download the invoice. */
  hostedUrl: string | null;
  issuedAt: IsoDateTime | null;
  sentAt: IsoDateTime | null;
  paidAt: IsoDateTime | null;
  cancelledAt: IsoDateTime | null;
  metadata: Metadata;
  createdAt: IsoDateTime;
  updatedAt: IsoDateTime;
}

export interface InvoiceSendParams {
  /** Recipient addresses. Defaults to the customer's email. */
  to?: string[];
  cc?: string[];
  bcc?: string[];
  subject?: string;
  message?: string;
  /** Attach the structured e-invoice XML in addition to the PDF. Defaults to `true`. */
  attachEInvoice?: boolean;
}

export interface InvoiceCancelParams {
  /** Reason printed on the cancellation invoice (Stornorechnung). */
  reason?: string;
  /** Date of the cancellation document. Defaults to today. */
  cancellationDate?: IsoDate;
}

export type PaymentMethod =
  | "bank_transfer"
  | "direct_debit"
  | "card"
  | "paypal"
  | "cash"
  | "other";

export interface PaymentCreateParams {
  amount: Decimal;
  /** Defaults to today. */
  date?: IsoDate;
  method?: PaymentMethod;
  /** E.g. the bank transaction ID or payment provider reference. */
  reference?: string;
}

export interface Payment {
  id: string;
  object: "payment";
  invoiceId: string;
  amount: string;
  currency: CurrencyCode;
  date: IsoDate;
  method: PaymentMethod;
  reference: string | null;
  createdAt: IsoDateTime;
}

export interface EInvoiceDownloadParams {
  /** Defaults to the invoice's `eInvoiceFormat`. `pdf` is not allowed here. */
  format?: Exclude<EInvoiceFormat, "pdf">;
}

export interface DeletedObject {
  id: string;
  object: string;
  deleted: true;
}

// ---------------------------------------------------------------------------
// Webhooks
// ---------------------------------------------------------------------------

export type WebhookEventType =
  | "invoice.created"
  | "invoice.updated"
  | "invoice.issued"
  | "invoice.sent"
  | "invoice.partially_paid"
  | "invoice.paid"
  | "invoice.overdue"
  | "invoice.cancelled"
  | "customer.created"
  | "customer.updated"
  | "customer.deleted";

interface WebhookEventBase<TType extends WebhookEventType, TObject> {
  id: string;
  object: "event";
  type: TType;
  createdAt: IsoDateTime;
  /** `false` for events triggered with a test API key. */
  livemode: boolean;
  data: { object: TObject };
}

export type InvoiceWebhookEvent = WebhookEventBase<Extract<WebhookEventType, `invoice.${string}`>, Invoice>;
export type CustomerWebhookEvent = WebhookEventBase<Extract<WebhookEventType, `customer.${string}`>, Customer>;
export type WebhookEvent = InvoiceWebhookEvent | CustomerWebhookEvent;
