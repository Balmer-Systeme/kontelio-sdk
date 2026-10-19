/**
 * Invoice a business customer in another EU member state (reverse charge,
 * § 13b UStG). The customer's VAT ID is mandatory and printed on the invoice.
 */
import { KontelioClient } from "@kontelio/sdk";

const kontelio = new KontelioClient();

const invoice = await kontelio.invoices.create({
  customer: {
    name: "Exemple SARL",
    vatId: "FR12345678901",
    billingAddress: { line1: "10 Rue de Rivoli", postalCode: "75001", city: "Paris", country: "FR" },
    language: "en",
  },
  taxTreatment: "reverse_charge",
  servicePeriod: { start: "2026-09-01", end: "2026-09-30" },
  lines: [{ name: "IT consulting September 2026", quantity: 40, unit: "HUR", unitPrice: 120 }],
  issue: true,
});

console.log(invoice.number, invoice.totals.vatAmount); // VAT amount is "0.00"
