package de.kontelio.examples;

import de.kontelio.sdk.KontelioClient;
import de.kontelio.sdk.exception.FieldError;
import de.kontelio.sdk.exception.ValidationException;
import de.kontelio.sdk.model.Address;
import de.kontelio.sdk.model.CustomerType;
import de.kontelio.sdk.model.EInvoiceFormat;
import de.kontelio.sdk.model.Invoice;
import de.kontelio.sdk.params.CustomerParams;
import de.kontelio.sdk.params.InvoiceCustomer;
import de.kontelio.sdk.params.InvoiceLineParams;
import de.kontelio.sdk.params.InvoiceParams;
import java.time.LocalDate;

/** Create and issue a ZUGFeRD invoice for a German B2B customer. */
public class CreateInvoiceExample {
  public static void main(String[] args) {
    KontelioClient kontelio = KontelioClient.fromEnv();

    try {
      Invoice invoice = kontelio.invoices().create(new InvoiceParams()
          .customer(InvoiceCustomer.inline(new CustomerParams()
              .type(CustomerType.COMPANY)
              .name("Example Customer GmbH")
              .email("buchhaltung@example.de")
              .vatId("DE123456789")
              .billingAddress(Address.of("Friedrichstraße 1", "10117", "Berlin", "DE"))
              .externalReference("CRM-4711")))
          .externalReference("ORDER-2026-001234")
          .serviceDate(LocalDate.of(2026, 10, 15))
          .paymentTermsDays(14)
          .addLine(InvoiceLineParams.of("Software Development", "95.00").quantity(12).unit("HUR").vatRate(19))
          .addLine(InvoiceLineParams.of("Hosting October 2026", "49.00").unit("MON").vatRate(19))
          .eInvoiceFormat(EInvoiceFormat.ZUGFERD)
          .issue(true));

      System.out.printf("Invoice %s created: %s %s%n", invoice.number(), invoice.totals().grossAmount(), invoice.currency());
      System.out.println("Customer link: " + invoice.hostedUrl());
    } catch (ValidationException e) {
      for (FieldError error : e.details()) System.err.println(error.field() + ": " + error.message());
      throw e;
    }
  }
}
