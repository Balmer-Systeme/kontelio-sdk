# Kontelio SDK

Official SDK for integrating external systems with **Kontelio**, a multilingual bookkeeping and invoicing platform for self-employed professionals, freelancers and small businesses in Germany.

With the Kontelio SDK, developers can integrate their own applications, ERP systems, SaaS platforms, e-commerce solutions or internal tools with Kontelio to create and manage invoices programmatically.

👉 Learn more about Kontelio:  
https://kontelio.de/

---

## What is Kontelio?

[Kontelio](https://kontelio.de/) is a bookkeeping and invoicing software built for freelancers, self-employed professionals and small businesses in Germany.

Kontelio helps businesses manage:

- invoices
- e-invoices
- expenses
- receipts and supporting documents
- EÜR
- VAT / Umsatzsteuer
- trade tax / Gewerbesteuer
- ELSTER-related workflows
- multilingual bookkeeping

Kontelio is designed to make German bookkeeping easier to understand, especially for international and multilingual entrepreneurs working in Germany.

Learn more:

https://kontelio.de/

---

## About the Kontelio SDK

The Kontelio SDK provides a developer-friendly integration layer for creating invoices and connecting external systems with Kontelio.

Typical integrations include:

- ERP systems
- CRM platforms
- SaaS applications
- e-commerce platforms
- booking systems
- subscription billing platforms
- internal business applications
- custom back-office systems

The goal is to make it simple for third-party software to create invoices in Kontelio without implementing the entire accounting workflow from scratch.

---

## Main use cases

### Create invoices from another system

Generate Kontelio invoices from your own application.

Example scenarios:

- an ERP creates an invoice after an order is completed
- a CRM generates an invoice after a contract is signed
- a SaaS platform creates recurring invoices for subscriptions
- an e-commerce system sends order data to Kontelio
- a booking platform generates an invoice after a service is delivered

---

### Connect your existing business software

Use Kontelio as the bookkeeping and invoicing layer behind another application.

```text
Your Application
      |
      v
Kontelio SDK
      |
      v
Kontelio API
      |
      v
Invoices / Bookkeeping
```

---

## Features

Depending on the API version, the SDK can provide support for functionality such as:

- invoice creation
- invoice retrieval
- customer data
- invoice positions
- VAT handling
- invoice status
- invoice metadata
- external reference IDs
- integration with existing business workflows

Additional functionality may be added over time.

---

## Installation

Installation instructions depend on the SDK language and package distribution.

```bash
# Example only
npm install @kontelio/sdk
```

or:

```bash
# Example only
maven install kontelio-sdk
```

Check the releases and package documentation for the currently supported installation method.

---

## Quick start

```javascript
import { KontelioClient } from "@kontelio/sdk";

const kontelio = new KontelioClient({
  apiKey: process.env.KONTELIO_API_KEY
});

const invoice = await kontelio.invoices.create({
  customer: {
    name: "Example Customer GmbH",
    country: "DE"
  },
  currency: "EUR",
  items: [
    {
      description: "Software Development",
      quantity: 1,
      unitPrice: 1000
    }
  ]
});

console.log(invoice);
```

> Note: The example above is illustrative. Please refer to the current SDK/API documentation for the exact interface.

---

## Invoice integration example

```text
Customer places an order
        |
        v
External system processes the order
        |
        v
Kontelio SDK creates the invoice
        |
        v
Invoice is stored in Kontelio
        |
        v
Bookkeeping workflow continues in Kontelio
```

---

## Authentication

Kontelio integrations should use the authentication mechanism defined by the current Kontelio API.

Do not store API keys or credentials directly in source code.

Use environment variables or a secure secrets manager.

```bash
KONTELIO_API_KEY=your-api-key
```

---

## Error handling

Applications integrating with Kontelio should handle common API errors such as:

- authentication errors
- validation errors
- missing required fields
- invalid invoice data
- unavailable services
- rate limits

```javascript
try {
  const invoice = await kontelio.invoices.create(data);
} catch (error) {
  console.error("Failed to create Kontelio invoice", error);
}
```

---

## Idempotency

When integrating invoice creation with external systems, use a stable external reference whenever supported.

```text
externalReference = "ORDER-2026-001234"
```

This helps prevent duplicate invoices when a request is retried.

---

## Recommended integration pattern

For production systems, we recommend:

1. create a unique external reference;
2. send invoice data to Kontelio;
3. store the returned Kontelio invoice ID;
4. handle retries safely;
5. log integration errors;
6. keep accounting and business data traceable.

---

## German invoicing software integration

Kontelio is designed for businesses operating in Germany and supports workflows related to German bookkeeping and invoicing.

If you are building software for:

- freelancers in Germany
- self-employed professionals
- small businesses
- SaaS billing
- ERP systems
- invoicing software
- accounting integrations

the Kontelio SDK can be used to connect your application with Kontelio.

More information:

https://kontelio.de/

---

## E-Invoicing

Kontelio also supports modern electronic invoicing workflows, including formats relevant in Germany such as:

- ZUGFeRD
- XRechnung

For more information about e-invoicing with Kontelio:

https://kontelio.de/e-rechnung/

---

## Bookkeeping for self-employed professionals

Kontelio is especially focused on making bookkeeping easier for freelancers and self-employed professionals in Germany.

Learn more:

https://kontelio.de/buchhaltungssoftware-selbststaendige/

---

## Multilingual bookkeeping

One of Kontelio's core goals is to make German bookkeeping easier to understand for international entrepreneurs.

Kontelio supports multiple languages while keeping important German accounting terminology available where needed.

Learn more:

https://kontelio.de/mehrsprachig/

---

## Documentation

Full API and integration documentation will be available at:

https://kontelio.de/

SDK-specific documentation should also be kept in this repository.

Suggested structure:

```text
docs/
├── authentication.md
├── invoices.md
├── customers.md
├── errors.md
├── examples.md
└── migration.md
```

---

## Examples

Example integrations may be added under:

```text
examples/
```

Possible examples:

- create an invoice
- create an invoice with multiple items
- create an invoice for an EU customer
- retrieve an invoice
- handle API errors
- use external references
- integrate with a SaaS subscription workflow

---

## Versioning

This SDK follows Semantic Versioning where possible.

```text
MAJOR.MINOR.PATCH
```

Breaking API changes should result in a new major version.

---

## Contributing

Contributions, bug reports and feature suggestions are welcome.

Before opening a pull request, please:

1. open an issue describing the proposed change;
2. follow the existing code style;
3. include tests where applicable;
4. update documentation when necessary.

---

## Security

Please do not report security vulnerabilities through public GitHub issues.

For security-related questions, use the contact information provided on:

https://kontelio.de/

Never publish:

- API keys
- access tokens
- customer data
- invoice data
- credentials

in public repositories.

---

## About Kontelio

Kontelio is a multilingual bookkeeping and invoicing platform for self-employed professionals, freelancers and small businesses in Germany.

It helps businesses manage invoices, e-invoices, expenses, receipts and tax-related bookkeeping workflows in one place.

Website:

https://kontelio.de/

E-Invoicing:

https://kontelio.de/e-rechnung/

Bookkeeping software for self-employed professionals:

https://kontelio.de/buchhaltungssoftware-selbststaendige/

Features:

https://kontelio.de/funktionen/

---

## Keywords

Kontelio SDK, invoicing SDK, invoice API Germany, German invoicing software, bookkeeping API, accounting API Germany, invoice integration, ERP invoice integration, SaaS invoicing, freelancer bookkeeping Germany, E-Rechnung, XRechnung, ZUGFeRD, ELSTER, EÜR, Buchhaltungssoftware, Rechnungssoftware.
