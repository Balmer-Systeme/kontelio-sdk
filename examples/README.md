# Kontelio SDK examples

| Example | What it shows |
| --- | --- |
| [create-invoice.ts](create-invoice.ts) | Create and issue a ZUGFeRD invoice for a German B2B customer |
| [eu-reverse-charge.ts](eu-reverse-charge.ts) | Invoice an EU business customer with reverse charge (§ 13b UStG) |
| [xrechnung-public-sector.ts](xrechnung-public-sector.ts) | XRechnung (CII) for a public authority with Leitweg-ID, download the XML |
| [idempotent-order-sync.ts](idempotent-order-sync.ts) | Sync e-commerce orders safely using `externalReference` and idempotency keys |
| [list-overdue.ts](list-overdue.ts) | Auto-pagination over all overdue invoices |
| [webhook-express.ts](webhook-express.ts) | Verify webhook signatures in Express |

Run an example:

```bash
npm install
npm run build
KONTELIO_API_KEY=kt_test_... npx tsx examples/create-invoice.ts
```
