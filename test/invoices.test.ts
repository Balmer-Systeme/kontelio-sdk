import { describe, expect, it } from "vitest";
import { invoiceFixture, json, mockClient } from "./helpers.js";

describe("invoices", () => {
  it("creates an invoice with inline customer data", async () => {
    const { client, calls } = mockClient([() => json(invoiceFixture, 201)]);
    const invoice = await client.invoices.create({
      customer: { name: "Example GmbH", vatId: "DE123456789", billingAddress: { line1: "Hauptstr. 1", postalCode: "10115", city: "Berlin", country: "DE" } },
      externalReference: "ORDER-2026-001234",
      lines: [{ name: "Software Development", quantity: 10, unit: "HUR", unitPrice: "100.00", vatRate: 19 }],
      eInvoiceFormat: "zugferd",
      issue: true,
    });
    expect(invoice.number).toBe("RE-2026-0001");
    expect(calls[0]!.method).toBe("POST");
    expect(calls[0]!.url).toBe("https://api.test/v1/invoices");
    expect(calls[0]!.body).toMatchObject({ externalReference: "ORDER-2026-001234", issue: true });
  });

  it.each([
    ["issue", "/invoices/inv_1/issue"],
    ["send", "/invoices/inv_1/send"],
    ["cancel", "/invoices/inv_1/cancel"],
  ] as const)("%s posts to %s", async (method, path) => {
    const { client, calls } = mockClient([() => json(invoiceFixture)]);
    await client.invoices[method]("inv_1");
    expect(calls[0]!.method).toBe("POST");
    expect(calls[0]!.url).toBe(`https://api.test/v1${path}`);
  });

  it("updates drafts with PATCH and deletes with DELETE", async () => {
    const { client, calls } = mockClient([() => json(invoiceFixture), () => json({ id: "inv_1", object: "invoice", deleted: true })]);
    await client.invoices.update("inv_1", { introText: "Hello" });
    const deleted = await client.invoices.delete("inv_1");
    expect(calls.map((c) => c.method)).toEqual(["PATCH", "DELETE"]);
    expect(deleted.deleted).toBe(true);
  });

  it("records and lists payments", async () => {
    const payment = { id: "pay_1", object: "payment", invoiceId: "inv_1", amount: "500.00" };
    const { client, calls } = mockClient([
      () => json(payment, 201),
      () => json({ object: "list", data: [payment], hasMore: false, nextCursor: null }),
    ]);
    await client.invoices.recordPayment("inv_1", { amount: 500, method: "bank_transfer" });
    const payments = await client.invoices.listPayments("inv_1");
    expect(calls[0]!.body).toEqual({ amount: 500, method: "bank_transfer" });
    expect(payments).toHaveLength(1);
  });

  it("downloads the PDF as bytes", async () => {
    const bytes = new Uint8Array([0x25, 0x50, 0x44, 0x46]);
    const { client, calls } = mockClient([() => new Response(bytes, { headers: { "content-type": "application/pdf" } })]);
    const pdf = await client.invoices.downloadPdf("inv_1");
    expect(pdf).toBeInstanceOf(Uint8Array);
    expect(Array.from(pdf)).toEqual([0x25, 0x50, 0x44, 0x46]);
    expect(calls[0]!.headers.Accept).toBe("*/*");
  });

  it("downloads the e-invoice XML as text", async () => {
    const xml = '<?xml version="1.0"?><rsm:CrossIndustryInvoice/>';
    const { client, calls } = mockClient([() => new Response(xml, { headers: { "content-type": "application/xml" } })]);
    const result = await client.invoices.downloadEInvoice("inv_1", { format: "xrechnung_cii" });
    expect(result).toBe(xml);
    expect(calls[0]!.url).toBe("https://api.test/v1/invoices/inv_1/e-invoice?format=xrechnung_cii");
  });
});

describe("pagination", () => {
  it("serializes filters and iterates over all pages", async () => {
    const { client, calls } = mockClient([
      () => json({ object: "list", data: [{ id: "a" }, { id: "b" }], hasMore: true, nextCursor: "cur_2" }),
      () => json({ object: "list", data: [{ id: "c" }], hasMore: false, nextCursor: null }),
    ]);
    const page = await client.invoices.list({ status: ["issued", "overdue"], limit: 2 });
    const ids: string[] = [];
    for await (const invoice of page) ids.push(invoice.id);

    expect(ids).toEqual(["a", "b", "c"]);
    expect(calls[0]!.url).toBe("https://api.test/v1/invoices?status=issued&status=overdue&limit=2");
    expect(calls[1]!.url).toBe("https://api.test/v1/invoices?status=issued&status=overdue&limit=2&cursor=cur_2");
  });

  it("returns null from nextPage on the last page", async () => {
    const { client } = mockClient([() => json({ object: "list", data: [], hasMore: false, nextCursor: null })]);
    const page = await client.customers.list({ search: "GmbH" });
    expect(page.hasNextPage()).toBe(false);
    expect(await page.nextPage()).toBeNull();
  });
});
