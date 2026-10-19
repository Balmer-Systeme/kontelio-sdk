package de.kontelio.examples;

import de.kontelio.sdk.KontelioClient;
import de.kontelio.sdk.model.Address;
import de.kontelio.sdk.model.DocumentLanguage;
import de.kontelio.sdk.model.Invoice;
import de.kontelio.sdk.model.ServicePeriod;
import de.kontelio.sdk.model.TaxTreatment;
import de.kontelio.sdk.params.CustomerParams;
import de.kontelio.sdk.params.InvoiceCustomer;
import de.kontelio.sdk.params.InvoiceLineParams;
import de.kontelio.sdk.params.InvoiceParams;
import java.time.LocalDate;

/** Invoice an EU business customer with reverse charge (§ 13b UStG). */
public class ReverseChargeExample {
  public static void main(String[] args) {
    KontelioClient kontelio = KontelioClient.fromEnv();

    Invoice invoice = kontelio.invoices().create(new InvoiceParams()
        .customer(InvoiceCustomer.inline(new CustomerParams()
            .name("Exemple SARL")
            .vatId("FR12345678901")
            .billingAddress(Address.of("10 Rue de Rivoli", "75001", "Paris", "FR"))
            .language(DocumentLanguage.EN)))
        .taxTreatment(TaxTreatment.REVERSE_CHARGE)
        .servicePeriod(ServicePeriod.of(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)))
        .addLine(InvoiceLineParams.of("IT consulting September 2026", "120").quantity(40).unit("HUR"))
        .issue(true));

    System.out.println(invoice.number() + " VAT: " + invoice.totals().vatAmount()); // 0.00
  }
}
