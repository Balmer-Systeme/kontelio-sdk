import type { RequestOptions } from "../core/http.js";
import type { Page } from "../core/pagination.js";
import type {
  Customer,
  CustomerCreateParams,
  CustomerListParams,
  CustomerUpdateParams,
  DeletedObject,
} from "../types.js";
import { Resource, encodeId } from "./base.js";

export class Customers extends Resource {
  /** Creates a customer. */
  create(params: CustomerCreateParams, options?: RequestOptions): Promise<Customer> {
    return this.http.request({ method: "POST", path: "/customers", body: params, options });
  }

  /** Retrieves a customer by its Kontelio ID. */
  retrieve(id: string, options?: RequestOptions): Promise<Customer> {
    return this.http.request({ method: "GET", path: `/customers/${encodeId(id)}`, options });
  }

  /** Updates a customer. Only the given fields are changed. */
  update(id: string, params: CustomerUpdateParams, options?: RequestOptions): Promise<Customer> {
    return this.http.request({ method: "PATCH", path: `/customers/${encodeId(id)}`, body: params, options });
  }

  /** Lists customers. The returned page can be iterated with `for await`. */
  list(params: CustomerListParams = {}, options?: RequestOptions): Promise<Page<Customer, CustomerListParams>> {
    return this.paginate<Customer, CustomerListParams>("/customers", params, options);
  }

  /**
   * Deletes a customer. Customers referenced by issued invoices cannot be
   * deleted (retention obligations); they are archived instead.
   */
  delete(id: string, options?: RequestOptions): Promise<DeletedObject> {
    return this.http.request({ method: "DELETE", path: `/customers/${encodeId(id)}`, options });
  }
}
