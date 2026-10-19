/**
 * Create and issue an invoice for a German B2B customer.
 *
 *   KONTELIO_API_KEY=kt_test_... npx tsx examples/create-invoice.ts
 */
import { KontelioClient, ValidationError } from "@kontelio/sdk";

const kontelio = new KontelioClient();

try {
  const invoice = await kontelio.invoices.create({
    customer: {
      type: "company",
      name: "Example Customer GmbH",
      email: "buchhaltung@example.de",
      vatId: "DE123456789",
      billingAddress: { line1: "Friedrichstraße 1", postalCode: "10117", city: "Berlin", country: "DE" },
      externalReference: "CRM-4711",
    },
    externalReference: "ORDER-2026-001234",
    serviceDate: "2026-10-15",
    paymentTermsDays: 14,
    lines: [
      { name: "Software Development", quantity: 12, unit: "HUR", unitPrice: "95.00", vatRate: 19 },
      { name: "Hosting October 2026", quantity: 1, unit: "MON", unitPrice: "49.00", vatRate: 19 },
    ],
    eInvoiceFormat: "zugferd",
    issue: true,
  });

  console.log(`Invoice ${invoice.number} created: ${invoice.totals.grossAmount} ${invoice.currency}`);
  console.log(`Customer link: ${invoice.hostedUrl}`);
} catch (error) {
  if (error instanceof ValidationError) {
    for (const detail of error.details) console.error(`${detail.field}: ${detail.message}`);
  }
  throw error;
}
