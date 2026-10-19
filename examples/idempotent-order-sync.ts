/**
 * Sync paid shop orders to Kontelio without ever creating duplicates.
 *
 * The order number is used both as `externalReference` (business-level
 * uniqueness) and as the idempotency key (transport-level retry safety).
 */
import { ConflictError, KontelioClient, type Invoice } from "@kontelio/sdk";

interface ShopOrder {
  number: string;
  customerId: string;
  email: string;
  name: string;
  items: { title: string; qty: number; priceGross: number }[];
}

const kontelio = new KontelioClient({ maxRetries: 5 });

export async function invoiceOrder(order: ShopOrder): Promise<Invoice> {
  try {
    return await kontelio.invoices.create(
      {
        customer: { name: order.name, email: order.email, type: "individual", externalReference: order.customerId },
        externalReference: order.number,
        priceMode: "gross",
        lines: order.items.map((item) => ({ name: item.title, quantity: item.qty, unitPrice: item.priceGross })),
        issue: true,
      },
      { idempotencyKey: `order-${order.number}` },
    );
  } catch (error) {
    if (error instanceof ConflictError) {
      // Already invoiced earlier: return the existing invoice instead.
      const page = await kontelio.invoices.list({ externalReference: order.number, limit: 1 });
      if (page.data[0]) return page.data[0];
    }
    throw error;
  }
}
