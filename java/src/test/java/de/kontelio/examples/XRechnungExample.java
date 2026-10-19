package de.kontelio.examples;

import de.kontelio.sdk.KontelioClient;
import de.kontelio.sdk.model.Address;
import de.kontelio.sdk.model.CustomerType;
import de.kontelio.sdk.model.EInvoiceFormat;
import de.kontelio.sdk.model.Invoice;
import de.kontelio.sdk.params.CustomerParams;
import de.kontelio.sdk.params.InvoiceCustomer;
import de.kontelio.sdk.params.InvoiceLineParams;
import de.kontelio.sdk.params.InvoiceParams;
import java.nio.file.Files;
import java.nio.file.Path;

/** XRechnung (CII) for a German public authority with Leitweg-ID; saves the XML. */
public class XRechnungExample {
  public static void main(String[] args) throws Exception {
    KontelioClient kontelio = KontelioClient.fromEnv();

    Invoice invoice = kontelio.invoices().create(new InvoiceParams()
        .customer(InvoiceCustomer.inline(new CustomerParams()
            .type(CustomerType.PUBLIC_AUTHORITY)
            .name("Stadtverwaltung Musterstadt")
            .leitwegId("991-12345-67")
            .billingAddress(Address.of("Rathausplatz 1", "12345", "Musterstadt", "DE"))))
        .buyerReference("991-12345-67")
        .orderReference("BEST-2026-889")
        .eInvoiceFormat(EInvoiceFormat.XRECHNUNG_CII)
        .addLine(InvoiceLineParams.of("Wartungsvertrag Q4/2026", "1500.00").vatRate(19))
        .issue(true));

    String xml = kontelio.invoices().downloadEInvoice(invoice.id());
    Files.writeString(Path.of(invoice.number() + ".xml"), xml);
    System.out.println("Saved " + invoice.number() + ".xml");
  }
}
