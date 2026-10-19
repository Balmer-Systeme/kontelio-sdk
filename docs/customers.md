# Customers

```ts
const customer = await kontelio.customers.create({
  type: "company",              // "company" | "individual" | "public_authority"
  name: "Example Customer GmbH",
  email: "buchhaltung@example.de",
  vatId: "DE123456789",
  billingAddress: { line1: "Friedrichstraße 1", postalCode: "10117", city: "Berlin", country: "DE" },
  language: "de",               // "de" | "en" | "de_en" (bilingual invoices)
  paymentTermsDays: 14,
  eInvoiceFormat: "zugferd",
  externalReference: "CRM-4711", // your ID, unique per account
  metadata: { segment: "enterprise" },
});

await kontelio.customers.retrieve(customer.id);
await kontelio.customers.update(customer.id, { email: "ap@example.de" });
await kontelio.customers.delete(customer.id);

const page = await kontelio.customers.list({ search: "GmbH" });
const [match] = (await kontelio.customers.list({ externalReference: "CRM-4711" })).data;
```

Customers with issued invoices cannot be removed because of statutory retention
periods (10 years, § 147 AO); deleting them archives them instead.
