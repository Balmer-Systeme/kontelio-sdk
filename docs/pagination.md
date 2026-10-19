# Pagination

List endpoints are cursor-based. `list()` returns a `Page` with `data`,
`hasMore` and `nextCursor`.

Iterate a page with `for await` to walk through **all** results — further pages
are fetched lazily:

```ts
for await (const invoice of await kontelio.invoices.list({ status: "overdue", limit: 100 })) {
  console.log(invoice.number);
}
```

Manual paging:

```ts
let page = await kontelio.customers.list({ limit: 50 });
while (page) {
  handle(page.data);
  page = await page.nextPage(); // null on the last page
}
```

Or page by page:

```ts
for await (const page of (await kontelio.invoices.list()).iterPages()) {
  await saveBatch(page.data);
}
```

`page.toArray()` collects everything into memory — only use it for small lists.
