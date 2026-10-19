package de.kontelio.examples;

import de.kontelio.sdk.KontelioClient;
import de.kontelio.sdk.model.Invoice;
import de.kontelio.sdk.model.InvoiceStatus;
import de.kontelio.sdk.params.InvoiceListParams;
import java.math.BigDecimal;

/** Walk through all overdue invoices across pages. */
public class ListOverdueExample {
  public static void main(String[] args) {
    KontelioClient kontelio = KontelioClient.fromEnv();

    BigDecimal total = BigDecimal.ZERO;
    for (Invoice invoice : kontelio.invoices().list(new InvoiceListParams().status(InvoiceStatus.OVERDUE).limit(100)).autoPagingIterable()) {
      total = total.add(invoice.totals().amountDue());
      System.out.printf("%s  %s  %s %s  due %s%n",
          invoice.number(), invoice.customer().name(), invoice.totals().amountDue(), invoice.currency(), invoice.dueDate());
    }
    System.out.println("Total outstanding: " + total + " EUR");
  }
}
