package de.kontelio.examples;

import de.kontelio.sdk.KontelioClient;
import de.kontelio.sdk.RequestOptions;
import de.kontelio.sdk.exception.ConflictException;
import de.kontelio.sdk.model.CustomerType;
import de.kontelio.sdk.model.Invoice;
import de.kontelio.sdk.model.PriceMode;
import de.kontelio.sdk.params.CustomerParams;
import de.kontelio.sdk.params.InvoiceCustomer;
import de.kontelio.sdk.params.InvoiceLineParams;
import de.kontelio.sdk.params.InvoiceListParams;
import de.kontelio.sdk.params.InvoiceParams;
import java.math.BigDecimal;
import java.util.List;

/**
 * Sync paid shop orders to Kontelio without duplicates: the order number is used as {@code
 * externalReference} (business-level) and as idempotency key (transport-level).
 */
public class OrderSyncExample {
  record Item(String title, int qty, BigDecimal priceGross) {}

  record Order(String number, String customerId, String name, String email, List<Item> items) {}

  private final KontelioClient kontelio = KontelioClient.builder().maxRetries(5).build();

  public Invoice invoiceOrder(Order order) {
    InvoiceParams params = new InvoiceParams()
        .customer(InvoiceCustomer.inline(new CustomerParams()
            .type(CustomerType.INDIVIDUAL)
            .name(order.name())
            .email(order.email())
            .externalReference(order.customerId())))
        .externalReference(order.number())
        .priceMode(PriceMode.GROSS)
        .issue(true);
    order.items().forEach(item -> params.addLine(new InvoiceLineParams(item.title(), item.priceGross()).quantity(item.qty())));

    try {
      return kontelio.invoices().create(params, RequestOptions.idempotent("order-" + order.number()));
    } catch (ConflictException e) {
      // Already invoiced earlier: return the existing invoice.
      return kontelio.invoices().list(new InvoiceListParams().externalReference(order.number()).limit(1))
          .data().stream().findFirst().orElseThrow(() -> e);
    }
  }
}
