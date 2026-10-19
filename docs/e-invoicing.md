# E-invoicing with XRechnung and ZUGFeRD

Since 1 January 2025, businesses in Germany must be able to **receive**
electronic invoices (E-Rechnung) in B2B transactions. Issuing e-invoices
becomes mandatory step by step until 2028 (Wachstumschancengesetz). Kontelio
produces EN 16931 compliant e-invoices, and the SDK lets your application use
them without implementing the standards yourself.

## Supported formats

| `eInvoiceFormat` | Format | Typical use |
| --- | --- | --- |
| `pdf` | Plain PDF | B2C, customers outside Germany |
| `zugferd` | ZUGFeRD 2.x / Factur-X, profile EN 16931 (PDF/A-3 with embedded CII XML) | B2B — human-readable and machine-readable in one file |
| `xrechnung_cii` | XRechnung, UN/CEFACT CII syntax | Public sector (B2G), B2B |
| `xrechnung_ubl` | XRechnung, OASIS UBL syntax | Public sector (B2G), Peppol |

Set a default per customer (`customer.eInvoiceFormat`) or per invoice.

## XRechnung for public authorities (B2G)

Public authorities require a **Leitweg-ID** as buyer reference (BT-10):

```ts
const invoice = await kontelio.invoices.create({
  customer: {
    type: "public_authority",
    name: "Stadtverwaltung Musterstadt",
    leitwegId: "991-12345-67",
    billingAddress: { line1: "Rathausplatz 1", postalCode: "12345", city: "Musterstadt", country: "DE" },
  },
  buyerReference: "991-12345-67",
  eInvoiceFormat: "xrechnung_cii",
  lines: [{ name: "Wartungsvertrag Q4/2026", unitPrice: "1500.00" }],
  issue: true,
});

const xml = await kontelio.invoices.downloadEInvoice(invoice.id);
```

## Validation

Kontelio validates every e-invoice against the EN 16931 and XRechnung business
rules before issuing it. Violations are returned as a `ValidationError` with
`details` pointing to the offending field, for example a missing buyer
reference or an invalid unit code.

Learn more: <https://kontelio.de/e-rechnung/>
