package de.kontelio.sdk;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import de.kontelio.sdk.internal.Json;
import de.kontelio.sdk.model.Address;
import de.kontelio.sdk.model.EInvoiceFormat;
import de.kontelio.sdk.model.InvoiceStatus;
import de.kontelio.sdk.model.PaymentMethod;
import de.kontelio.sdk.model.ServicePeriod;
import de.kontelio.sdk.model.TaxTreatment;
import de.kontelio.sdk.params.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InvoiceServiceTest {
  private MockServer server;
  private KontelioClient client;

  @BeforeEach
  void setUp() throws Exception {
    server = new MockServer();
    client = server.client();
  }

  @AfterEach
  void tearDown() {
    server.close();
  }

  private JsonNode sentBody(int index) throws Exception {
    return Json.MAPPER.readTree(server.request(index).body());
  }

  @Test
  void createsInvoiceWithInlineCustomerAndParsesResponse() throws Exception {
    server.enqueue(201, Fixtures.INVOICE);
    var invoice = client.invoices().create(new InvoiceParams()
        .customer(InvoiceCustomer.inline(new CustomerParams()
            .name("Example GmbH")
            .vatId("DE123456789")
            .billingAddress(Address.of("Hauptstr. 1", "10115", "Berlin", "DE"))))
        .externalReference("ORDER-2026-001234")
        .servicePeriod(ServicePeriod.of(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)))
        .addLine(InvoiceLineParams.of("Dev", "100.00").quantity(10).unit("HUR").vatRate(19))
        .eInvoiceFormat(EInvoiceFormat.ZUGFERD)
        .issue(true));

    assertEquals("RE-2026-0001", invoice.number());
    assertEquals(InvoiceStatus.ISSUED, invoice.status());
    assertEquals(EInvoiceFormat.ZUGFERD, invoice.eInvoiceFormat());
    assertEquals(new BigDecimal("1190.00"), invoice.totals().grossAmount());
    assertEquals(LocalDate.of(2026, 10, 19), invoice.issueDate());
    assertEquals("Example GmbH", invoice.customer().name());

    var body = sentBody(0);
    assertEquals("POST", server.request(0).method());
    assertEquals("/v1/invoices", server.request(0).uri());
    assertEquals("Example GmbH", body.at("/customer/name").asText());
    assertEquals("Berlin", body.at("/customer/billingAddress/city").asText());
    assertEquals("ORDER-2026-001234", body.get("externalReference").asText());
    assertEquals("2026-10-01", body.at("/servicePeriod/start").asText());
    assertEquals("zugferd", body.get("eInvoiceFormat").asText());
    assertEquals("HUR", body.at("/lines/0/unit").asText());
    assertEquals(0, new BigDecimal("100.00").compareTo(body.at("/lines/0/unitPrice").decimalValue()));
    assertTrue(server.request(0).body().contains("\"unitPrice\":100.00"), "decimals keep their scale");
    assertTrue(body.get("issue").asBoolean());
    assertFalse(body.has("currency"), "unset fields must not be sent");
    assertFalse(body.at("/customer").has("email"));
  }

  @Test
  void serializesCustomerReferences() throws Exception {
    server.enqueue(201, "{}").enqueue(201, "{}");
    client.invoices().create(new InvoiceParams().customer(InvoiceCustomer.byId("cus_1")).addLine(InvoiceLineParams.of("x", "1")));
    client.invoices().create(new InvoiceParams().customer(InvoiceCustomer.byExternalReference("CRM-4711")).addLine(InvoiceLineParams.of("x", "1")));
    assertEquals("{\"id\":\"cus_1\"}", sentBody(0).get("customer").toString());
    assertEquals("{\"externalReference\":\"CRM-4711\"}", sentBody(1).get("customer").toString());
  }

  @Test
  void unknownEnumValuesDoNotBreakDeserialization() {
    server.enqueue(200, "{\"id\":\"inv_1\",\"status\":\"something_new\",\"taxTreatment\":\"future\"}");
    var invoice = client.invoices().retrieve("inv_1");
    assertEquals(InvoiceStatus.UNKNOWN, invoice.status());
    assertEquals(TaxTreatment.UNKNOWN, invoice.taxTreatment());
  }

  @Test
  void lifecycleEndpoints() {
    server.enqueue(200, "{}").enqueue(200, "{}").enqueue(200, "{}").enqueue(200, "{}").enqueue(200, "{\"id\":\"inv_1\",\"object\":\"invoice\",\"deleted\":true}");
    client.invoices().update("inv_1", new InvoiceParams().introText("Hallo"));
    client.invoices().issue("inv_1");
    client.invoices().send("inv_1", new InvoiceSendParams().to("ap@example.de").message("Anbei"));
    client.invoices().cancel("inv_1", new InvoiceCancelParams().reason("Falscher Empfänger"));
    var deleted = client.invoices().delete("inv_1");

    assertEquals(List.of("PATCH", "POST", "POST", "POST", "DELETE"), server.requests.stream().map(MockServer.Recorded::method).toList());
    assertEquals(
        List.of("/v1/invoices/inv_1", "/v1/invoices/inv_1/issue", "/v1/invoices/inv_1/send", "/v1/invoices/inv_1/cancel", "/v1/invoices/inv_1"),
        server.requests.stream().map(MockServer.Recorded::uri).toList());
    assertTrue(deleted.deleted());
  }

  @Test
  void recordsAndListsPayments() throws Exception {
    String payment = "{\"id\":\"pay_1\",\"object\":\"payment\",\"invoiceId\":\"inv_1\",\"amount\":\"500.00\",\"method\":\"bank_transfer\",\"date\":\"2026-10-20\"}";
    server.enqueue(201, payment).enqueue(200, "{\"object\":\"list\",\"data\":[" + payment + "],\"hasMore\":false,\"nextCursor\":null}");

    var recorded = client.invoices().recordPayment("inv_1", PaymentParams.of("500.00").method(PaymentMethod.BANK_TRANSFER).date(LocalDate.of(2026, 10, 20)));
    var payments = client.invoices().listPayments("inv_1");

    assertEquals(new BigDecimal("500.00"), recorded.amount());
    assertEquals("bank_transfer", sentBody(0).get("method").asText());
    assertEquals("2026-10-20", sentBody(0).get("date").asText());
    assertEquals(1, payments.size());
  }

  @Test
  void downloadsPdfAndXml() {
    byte[] pdf = {0x25, 0x50, 0x44, 0x46};
    String xml = "<?xml version=\"1.0\"?><rsm:CrossIndustryInvoice/>";
    server.enqueue(200, pdf, Map.of("Content-Type", "application/pdf")).enqueue(200, xml, Map.of("Content-Type", "application/xml"));

    assertArrayEquals(pdf, client.invoices().downloadPdf("inv_1"));
    assertEquals(xml, client.invoices().downloadEInvoice("inv_1", EInvoiceFormat.XRECHNUNG_CII, null));
    assertEquals("/v1/invoices/inv_1/e-invoice?format=xrechnung_cii", server.request(1).uri());
    assertThrows(IllegalArgumentException.class, () -> client.invoices().downloadEInvoice("inv_1", EInvoiceFormat.PDF, null));
  }

  @Test
  void autoPaginatesWithFilters() {
    server.enqueue(200, "{\"object\":\"list\",\"data\":[{\"id\":\"a\"},{\"id\":\"b\"}],\"hasMore\":true,\"nextCursor\":\"cur_2\"}")
        .enqueue(200, "{\"object\":\"list\",\"data\":[{\"id\":\"c\"}],\"hasMore\":false,\"nextCursor\":null}");

    var page = client.invoices().list(new InvoiceListParams()
        .status(InvoiceStatus.ISSUED, InvoiceStatus.OVERDUE)
        .issueDateFrom(LocalDate.of(2026, 1, 1))
        .limit(2));
    var ids = page.autoPagingStream().map(i -> i.id()).toList();

    assertEquals(List.of("a", "b", "c"), ids);
    assertEquals("/v1/invoices?status=issued&status=overdue&issueDateFrom=2026-01-01&limit=2", server.request(0).uri());
    assertEquals("/v1/invoices?status=issued&status=overdue&issueDateFrom=2026-01-01&limit=2&cursor=cur_2", server.request(1).uri());
  }

  @Test
  void lastPageHasNoNextPage() {
    server.enqueue(200, "{\"object\":\"list\",\"data\":[],\"hasMore\":false,\"nextCursor\":null}");
    var page = client.customers().list(new CustomerListParams().search("GmbH Köln"));
    assertFalse(page.hasNextPage());
    assertTrue(page.nextPage().isEmpty());
    assertEquals("/v1/customers?search=GmbH%20K%C3%B6ln", server.request(0).uri());
  }
}
