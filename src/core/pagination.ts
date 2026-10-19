import type { ListParams, ListResponse } from "../types.js";

export type PageFetcher<T, P extends ListParams> = (params: P) => Promise<ListResponse<T>>;

/**
 * One page of a list result.
 *
 * Iterating a page with `for await` walks through *all* following pages
 * automatically:
 *
 * ```ts
 * for await (const invoice of await kontelio.invoices.list({ status: "overdue" })) {
 *   console.log(invoice.number);
 * }
 * ```
 */
export class Page<T, P extends ListParams = ListParams> implements AsyncIterable<T> {
  readonly data: T[];
  readonly hasMore: boolean;
  readonly nextCursor: string | null;

  constructor(
    response: ListResponse<T>,
    private readonly params: P,
    private readonly fetchPage: PageFetcher<T, P>,
  ) {
    this.data = response.data;
    this.hasMore = response.hasMore;
    this.nextCursor = response.nextCursor;
  }

  hasNextPage(): boolean {
    return this.hasMore && this.nextCursor != null;
  }

  /** Fetches the next page, or returns `null` if this is the last one. */
  async nextPage(): Promise<Page<T, P> | null> {
    if (!this.hasNextPage()) return null;
    const params: P = { ...this.params, cursor: this.nextCursor as string };
    return new Page(await this.fetchPage(params), params, this.fetchPage);
  }

  /** Iterates over the pages, starting with this one. */
  async *iterPages(): AsyncGenerator<Page<T, P>> {
    let page: Page<T, P> | null = this;
    while (page) {
      yield page;
      page = await page.nextPage();
    }
  }

  async *[Symbol.asyncIterator](): AsyncIterator<T> {
    for await (const page of this.iterPages()) {
      yield* page.data;
    }
  }

  /** Collects all items of all pages into an array. Use with care on large lists. */
  async toArray(): Promise<T[]> {
    const items: T[] = [];
    for await (const item of this) items.push(item);
    return items;
  }
}
