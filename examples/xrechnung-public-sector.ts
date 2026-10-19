/**
 * Create an XRechnung for a German public authority (B2G) and download the XML.
 */
import { writeFile } from "node:fs/promises";
import { KontelioClient } from "@kontelio/sdk";

const kontelio = new KontelioClient();

const invoice = await kontelio.invoices.create({
  customer: {
    type: "public_authority",
    name: "Stadtverwaltung Musterstadt",
    leitwegId: "991-12345-67",
    billingAddress: { line1: "Rathausplatz 1", postalCode: "12345", city: "Musterstadt", country: "DE" },
  },
  buyerReference: "991-12345-67",
  orderReference: "BEST-2026-889",
  eInvoiceFormat: "xrechnung_cii",
  lines: [{ name: "Wartungsvertrag Q4/2026", unitPrice: "1500.00", vatRate: 19 }],
  issue: true,
});

const xml = await kontelio.invoices.downloadEInvoice(invoice.id);
await writeFile(`${invoice.number}.xml`, xml, "utf8");
console.log(`Saved ${invoice.number}.xml`);
