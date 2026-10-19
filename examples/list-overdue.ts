/**
 * Walk through all overdue invoices across pages.
 */
import { KontelioClient } from "@kontelio/sdk";

const kontelio = new KontelioClient();

let total = 0;
for await (const invoice of await kontelio.invoices.list({ status: "overdue", limit: 100 })) {
  total += Number(invoice.totals.amountDue);
  console.log(`${invoice.number}  ${invoice.customer.name}  ${invoice.totals.amountDue} ${invoice.currency}  due ${invoice.dueDate}`);
}
console.log(`Total outstanding: ${total.toFixed(2)} EUR`);
