# Invoices

## Lifecycle

```text
draft ──issue()──▶ issued ──send()──▶ (sent) ──payments──▶ partially_paid ──▶ paid
  │                  │                                         │
delete()          cancel() ──▶ cancelled (Stornorechnung)    overdue (after due date)
```

- **Drafts** can be updated and deleted freely.
- **Issuing** assigns the next sequential invoice number and renders the PDF and
  e-invoice. From then on the invoice is immutable, as required by the German
  GoBD. Corrections are made by cancelling (Stornorechnung) and re-issuing.

## Create an invoice

```ts
const invoice = await kontelio.invoices.create({
  customer: { id: "cus_123" },             // or { externalReference: "CRM-4711" } or inline data
  externalReference: "ORDER-2026-001234",   // your unique reference, prevents duplicates
  serviceDate: "2026-10-15",                // Leistungsdatum (or servicePeriod)
  paymentTermsDays: 14,
  lines: [
    { name: "Software Development", quantity: 12, unit: "HUR", unitPrice: "95.00", vatRate: 19 },
    { name: "Fachbuch", quantity: 1, unitPrice: "39.90", vatRate: 7 },
  ],
  issue: true,                              // finalize immediately
});

invoice.number;               // "RE-2026-0042"
invoice.totals.grossAmount;   // "1396.49"
invoice.totals.vatBreakdown;  // one entry per VAT rate
invoice.hostedUrl;            // shareable link for your customer
```

### Inline customers

Passing customer data instead of an ID creates the customer on the fly. If you
include `externalReference`, Kontelio reuses an existing customer with the
same reference instead of creating a duplicate.

### Net vs. gross prices

`priceMode: "net"` (default) treats `unitPrice` as net. With `priceMode: "gross"`
unit prices include VAT — typical for B2C shops.

### Units

Use [UN/ECE Recommendation 20](https://unece.org/trade/uncefact/cl-recommendations)
codes — they are required for e-invoices: `C62` piece (default), `HUR` hour,
`DAY` day, `MON` month, `KGM` kilogram, `MTR` metre, `LS` lump sum.

## Other operations

```ts
await kontelio.invoices.retrieve("inv_123");
await kontelio.invoices.update("inv_123", { closingText: "Vielen Dank!" }); // drafts only
await kontelio.invoices.delete("inv_123");                                  // drafts only
await kontelio.invoices.issue("inv_123");
await kontelio.invoices.send("inv_123", { to: ["ap@example.de"], message: "Anbei Ihre Rechnung." });
await kontelio.invoices.cancel("inv_123", { reason: "Falscher Rechnungsempfänger" });
```

## Payments

```ts
await kontelio.invoices.recordPayment("inv_123", {
  amount: "500.00",
  date: "2026-10-20",
  method: "bank_transfer",
  reference: "SEPA-REF-998877",
});

const payments = await kontelio.invoices.listPayments("inv_123");
```

The invoice status moves to `partially_paid` or `paid` automatically.

## Documents

```ts
import { writeFile } from "node:fs/promises";

const pdf = await kontelio.invoices.downloadPdf("inv_123");          // Uint8Array
await writeFile("invoice.pdf", pdf);

const xml = await kontelio.invoices.downloadEInvoice("inv_123", { format: "xrechnung_ubl" });
```

## Listing

See [pagination](pagination.md).

```ts
const page = await kontelio.invoices.list({
  status: ["issued", "overdue"],
  issueDateFrom: "2026-01-01",
  issueDateTo: "2026-12-31",
});
```
