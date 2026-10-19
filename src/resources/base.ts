import type { HttpClient, RequestOptions } from "../core/http.js";
import { Page } from "../core/pagination.js";
import type { ListParams, ListResponse } from "../types.js";

export abstract class Resource {
  constructor(protected readonly http: HttpClient) {}

  protected async paginate<T, P extends ListParams>(path: string, params: P, options?: RequestOptions): Promise<Page<T, P>> {
    const fetchPage = (p: P) =>
      this.http.request<ListResponse<T>>({ method: "GET", path, query: p, options });
    return new Page(await fetchPage(params), params, fetchPage);
  }
}

export function encodeId(id: string): string {
  if (!id) throw new TypeError("An ID is required");
  return encodeURIComponent(id);
}
