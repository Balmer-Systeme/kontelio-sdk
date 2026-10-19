import { HttpClient, type ClientOptions } from "./core/http.js";
import { Customers } from "./resources/customers.js";
import { Invoices } from "./resources/invoices.js";
import { Webhooks } from "./resources/webhooks.js";

/**
 * Entry point of the Kontelio SDK.
 *
 * ```ts
 * import { KontelioClient } from "@kontelio/sdk";
 *
 * const kontelio = new KontelioClient({ apiKey: process.env.KONTELIO_API_KEY });
 * const invoice = await kontelio.invoices.create({ ... });
 * ```
 */
export class KontelioClient {
  readonly invoices: Invoices;
  readonly customers: Customers;
  readonly webhooks: Webhooks;

  /** @internal */
  readonly http: HttpClient;

  constructor(options: ClientOptions = {}) {
    this.http = new HttpClient(options);
    this.invoices = new Invoices(this.http);
    this.customers = new Customers(this.http);
    this.webhooks = new Webhooks();
  }

  get baseUrl(): string {
    return this.http.baseUrl;
  }
}
