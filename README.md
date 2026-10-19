# Kontelio SDK – Invoice & E-Invoicing API for Germany

[![CI](https://github.com/Balmer-Systeme/kontelio-sdk/actions/workflows/ci.yml/badge.svg)](https://github.com/Balmer-Systeme/kontelio-sdk/actions/workflows/ci.yml)
[![npm](https://img.shields.io/npm/v/@kontelio/sdk.svg)](https://www.npmjs.com/package/@kontelio/sdk)
[![Maven Central](https://img.shields.io/maven-central/v/de.kontelio/kontelio-sdk.svg)](https://central.sonatype.com/artifact/de.kontelio/kontelio-sdk)
[![License: Apache-2.0](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](LICENSE)
![TypeScript](https://img.shields.io/badge/TypeScript-ready-3178c6.svg)
![Java 17+](https://img.shields.io/badge/Java-17%2B-orange.svg)

| Language | Package | Docs |
| --- | --- | --- |
| TypeScript / JavaScript (Node.js, Bun, Deno, edge) | [`@kontelio/sdk`](https://www.npmjs.com/package/@kontelio/sdk) | this page |
| Java 17+ (Spring Boot, Jakarta EE, Quarkus, ...) | [`de.kontelio:kontelio-sdk`](https://central.sonatype.com/artifact/de.kontelio/kontelio-sdk) | [java/README.md](java/README.md) |

Official **TypeScript / JavaScript** and **Java** SDKs for **[Kontelio](https://kontelio.de/)**, the multilingual bookkeeping and invoicing platform for self-employed professionals, freelancers and small businesses in Germany.

Use it to create invoices, **E-Rechnungen (XRechnung, ZUGFeRD)** and customers in Kontelio from your own ERP, CRM, SaaS, e-commerce shop, booking platform or back-office tool — with German VAT handling, GoBD-compliant numbering and bookkeeping included.

```ts
import { KontelioClient } from "@kontelio/sdk";

const kontelio = new KontelioClient({ apiKey: process.env.KONTELIO_API_KEY });

const invoice = await kontelio.invoices.create({
  customer: { name: "Example Customer GmbH", vatId: "DE123456789", billingAddress: { line1: "Friedrichstraße 1", postalCode: "10117", city: "Berlin", country: "DE" } },
  externalReference: "ORDER-2026-001234",
  lines: [{ name: "Software Development", quantity: 10, unit: "HUR", unitPrice: "100.00", vatRate: 19 }],
  eInvoiceFormat: "zugferd",
  issue: true,
});

console.log(invoice.number, invoice.totals.grossAmount); // "RE-2026-0042" "1190.00"
```

> **API access:** the Kontelio API is enabled per account. [Contact us](https://kontelio.de/) to get API keys for your integration.

---

## Highlights

- **Invoices end to end** – create, issue, send by email, cancel (Stornorechnung), record payments, download PDF.
- **German e-invoicing** – ZUGFeRD / Factur-X (EN 16931), XRechnung in CII and UBL syntax, Leitweg-ID for public authorities (B2G).
- **German VAT built in** – 19 % / 7 %, Kleinunternehmer (§ 19 UStG), reverse charge (§ 13b UStG), intra-community supplies, exports.
- **Safe retries** – automatic idempotency keys, exponential backoff, `externalReference` deduplication.
- **Fully typed** – TypeScript declarations for every request and response.
- **Webhooks** – signature verification for `invoice.paid`, `invoice.overdue` and more.
- **Zero dependencies** – runs on Node.js ≥ 20, Bun, Deno, Cloudflare Workers and other edge runtimes. ESM and CommonJS.
- **Java SDK** – Java 17+, `java.net.http`, immutable records, `BigDecimal` amounts — see [java/](java/README.md).
- **OpenAPI 3.1 spec** – [`openapi/openapi.yaml`](openapi/openapi.yaml) to generate clients in any language.

---

## Installation

```bash
npm install @kontelio/sdk
```

```bash
pnpm add @kontelio/sdk    # pnpm
yarn add @kontelio/sdk    # yarn
bun add @kontelio/sdk     # bun
```

### Java

```xml
<dependency>
  <groupId>de.kontelio</groupId>
  <artifactId>kontelio-sdk</artifactId>
  <version>0.1.0</version>
</dependency>
```

```java
KontelioClient kontelio = KontelioClient.fromEnv();

Invoice invoice = kontelio.invoices().create(new InvoiceParams()
    .customer(InvoiceCustomer.byExternalReference("CRM-4711"))
    .externalReference("ORDER-2026-001234")
    .addLine(InvoiceLineParams.of("Software Development", "100.00").quantity(10).unit("HUR").vatRate(19))
    .eInvoiceFormat(EInvoiceFormat.ZUGFERD)
    .issue(true));
```

Full Java documentation: [java/README.md](java/README.md).

## Authentication

Create an API key in Kontelio under **Settings → Integrations → API keys** and provide it via environment variable:

```bash
KONTELIO_API_KEY=kt_live_...
```

```ts
const kontelio = new KontelioClient(); // reads KONTELIO_API_KEY
```

Test keys (`kt_test_...`) work against a sandbox: no emails are sent and nothing reaches your bookkeeping. Never commit API keys to source control. → [Authentication & options](docs/authentication.md)

---

## Usage

### Create an invoice for an existing customer

```ts
const invoice = await kontelio.invoices.create({
  customer: { externalReference: "CRM-4711" },
  servicePeriod: { start: "2026-10-01", end: "2026-10-31" },
  paymentTermsDays: 14,
  lines: [
    { name: "SaaS subscription – Pro plan", unit: "MON", unitPrice: "49.00" },
    { name: "Additional users", quantity: 5, unitPrice: "9.00" },
  ],
});

await kontelio.invoices.issue(invoice.id);
await kontelio.invoices.send(invoice.id, { message: "Thank you for your business!" });
```

### XRechnung for a public authority

```ts
const invoice = await kontelio.invoices.create({
  customer: { type: "public_authority", name: "Stadtverwaltung Musterstadt", leitwegId: "991-12345-67" },
  buyerReference: "991-12345-67",
  eInvoiceFormat: "xrechnung_cii",
  lines: [{ name: "Wartungsvertrag Q4/2026", unitPrice: "1500.00" }],
  issue: true,
});

const xml = await kontelio.invoices.downloadEInvoice(invoice.id);
```

### EU B2B customer with reverse charge

```ts
await kontelio.invoices.create({
  customer: { id: "cus_fr_42" },
  taxTreatment: "reverse_charge",
  lines: [{ name: "IT consulting", quantity: 40, unit: "HUR", unitPrice: 120 }],
  issue: true,
});
```

### Record a payment and download the PDF

```ts
await kontelio.invoices.recordPayment(invoice.id, { amount: "1190.00", method: "bank_transfer" });

const pdf = await kontelio.invoices.downloadPdf(invoice.id); // Uint8Array
```

### Iterate over all overdue invoices

```ts
for await (const inv of await kontelio.invoices.list({ status: "overdue" })) {
  console.log(inv.number, inv.customer.name, inv.totals.amountDue);
}
```

### Handle errors

```ts
import { ValidationError, ConflictError } from "@kontelio/sdk";

try {
  await kontelio.invoices.create(data);
} catch (error) {
  if (error instanceof ValidationError) {
    error.details.forEach((d) => console.error(`${d.field}: ${d.message}`));
  } else if (error instanceof ConflictError) {
    // e.g. an invoice with this externalReference already exists
  } else {
    throw error;
  }
}
```

### Verify webhooks

```ts
const event = await kontelio.webhooks.constructEvent(rawBody, req.headers["kontelio-signature"], process.env.KONTELIO_WEBHOOK_SECRET!);

if (event.type === "invoice.paid") {
  await fulfilOrder(event.data.object.externalReference);
}
```

More in [`examples/`](examples/).

---

## API overview

| Resource | Methods |
| --- | --- |
| `kontelio.invoices` | `create` · `retrieve` · `update` · `list` · `delete` · `issue` · `send` · `cancel` · `recordPayment` · `listPayments` · `downloadPdf` · `downloadEInvoice` |
| `kontelio.customers` | `create` · `retrieve` · `update` · `list` · `delete` |
| `kontelio.webhooks` | `constructEvent` · `verify` · `sign` |

Full reference: [OpenAPI 3.1 specification](openapi/openapi.yaml).

## Documentation

- [Getting started & authentication](docs/authentication.md)
- [Invoices](docs/invoices.md)
- [E-invoicing: XRechnung & ZUGFeRD](docs/e-invoicing.md)
- [German VAT treatments](docs/vat.md)
- [Customers](docs/customers.md)
- [Pagination](docs/pagination.md)
- [Errors, retries & idempotency](docs/errors.md)
- [Webhooks](docs/webhooks.md)

---

## Typical integrations

- an **ERP** creates an invoice after an order is completed
- a **CRM** generates an invoice after a contract is signed
- a **SaaS platform** bills subscriptions every month
- an **e-commerce shop** invoices paid orders automatically
- a **booking platform** invoices after a service is delivered
- an **agency tool** turns tracked hours into invoices

```text
Your application ──▶ Kontelio SDK ──▶ Kontelio API ──▶ Invoices, E-Rechnungen, bookkeeping (EÜR, UStVA)
```

## Recommended integration pattern

1. Use a stable `externalReference` (e.g. your order number) for every invoice.
2. Send the invoice data to Kontelio.
3. Store the returned Kontelio invoice `id` and `number`.
4. Let the SDK retry transient errors; pass your own `idempotencyKey` for retries across processes.
5. Subscribe to webhooks (`invoice.paid`) instead of polling.
6. Log `error.requestId` for support requests.

---

## What is Kontelio?

[Kontelio](https://kontelio.de/) is bookkeeping and invoicing software built for freelancers, self-employed professionals and small businesses in Germany. It helps businesses manage:

- invoices and [e-invoices (XRechnung, ZUGFeRD)](https://kontelio.de/e-rechnung/)
- expenses, receipts and supporting documents
- EÜR (Einnahmen-Überschuss-Rechnung)
- VAT / Umsatzsteuer and UStVA
- trade tax / Gewerbesteuer
- ELSTER-related workflows
- [multilingual bookkeeping](https://kontelio.de/mehrsprachig/)

Kontelio makes German bookkeeping easier to understand, especially for international and multilingual entrepreneurs working in Germany.

- Website: <https://kontelio.de/>
- E-Rechnung: <https://kontelio.de/e-rechnung/>
- Buchhaltungssoftware für Selbstständige: <https://kontelio.de/buchhaltungssoftware-selbststaendige/>
- Features: <https://kontelio.de/funktionen/>

---

## Development

```bash
npm install
npm run check          # TypeScript: typecheck, tests, build
cd java && mvn verify  # Java: compile, tests, javadoc
```

See [CONTRIBUTING.md](CONTRIBUTING.md). Releases follow [Semantic Versioning](https://semver.org/) — see the [changelog](CHANGELOG.md).

## Security

Please report vulnerabilities privately — see [SECURITY.md](SECURITY.md). Never publish API keys, access tokens, customer or invoice data in public repositories or issues.

## License

[Apache License 2.0](LICENSE) © Balmer Systeme

---

**Keywords:** Kontelio SDK, invoicing SDK, invoice API Germany, Rechnungs-API, E-Rechnung API, XRechnung API, ZUGFeRD API, EN 16931, German invoicing software, bookkeeping API, accounting API Germany, Buchhaltungs-API, invoice integration, ERP invoice integration, SaaS invoicing, e-commerce invoicing, freelancer bookkeeping Germany, Umsatzsteuer, Reverse Charge, Kleinunternehmer, ELSTER, EÜR, Buchhaltungssoftware, Rechnungssoftware, TypeScript, Node.js, Java, Spring Boot, Maven.
