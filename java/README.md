# Kontelio SDK for Java

Official Java SDK for **[Kontelio](https://kontelio.de/)** — create invoices, **E-Rechnungen (XRechnung, ZUGFeRD)** and customers from Spring Boot, Jakarta EE, Quarkus, Micronaut or any Java 17+ application.

- Java 17+, built on `java.net.http.HttpClient`; Jackson is the only dependency
- Thread-safe client, automatic retries with idempotency keys
- Immutable `record` models, `BigDecimal` amounts, `java.time` dates
- Auto-pagination via `Iterable` / `Stream`
- Webhook signature verification

> **API access:** the Kontelio API is enabled per account. [Contact us](https://kontelio.de/) to get API keys for your integration.

## Installation

Maven:

```xml
<dependency>
  <groupId>de.kontelio</groupId>
  <artifactId>kontelio-sdk</artifactId>
  <version>0.1.0</version>
</dependency>
```

Gradle:

```kotlin
implementation("de.kontelio:kontelio-sdk:0.1.0")
```

## Quick start

```java
import de.kontelio.sdk.KontelioClient;
import de.kontelio.sdk.model.*;
import de.kontelio.sdk.params.*;

KontelioClient kontelio = KontelioClient.fromEnv(); // reads KONTELIO_API_KEY

Invoice invoice = kontelio.invoices().create(new InvoiceParams()
    .customer(InvoiceCustomer.inline(new CustomerParams()
        .name("Example Customer GmbH")
        .vatId("DE123456789")
        .billingAddress(Address.of("Friedrichstraße 1", "10117", "Berlin", "DE"))))
    .externalReference("ORDER-2026-001234")
    .addLine(InvoiceLineParams.of("Software Development", "100.00").quantity(10).unit("HUR").vatRate(19))
    .eInvoiceFormat(EInvoiceFormat.ZUGFERD)
    .issue(true));

System.out.println(invoice.number() + " " + invoice.totals().grossAmount()); // RE-2026-0042 1190.00
```

## Configuration

```java
KontelioClient kontelio = KontelioClient.builder()
    .apiKey(System.getenv("KONTELIO_API_KEY"))
    .baseUrl("https://api.kontelio.de/v1")   // or KONTELIO_BASE_URL
    .timeout(Duration.ofSeconds(30))         // default 60 s
    .maxRetries(3)                           // default 2
    .httpClient(customHttpClient)            // proxy, SSL, executor
    .build();
```

Create one client and reuse it (e.g. as a Spring `@Bean`). Per-request overrides:

```java
kontelio.invoices().retrieve("inv_123",
    RequestOptions.builder().timeout(Duration.ofSeconds(5)).maxRetries(0).header("X-Trace-Id", traceId).build());
```

## Usage

```java
// Lifecycle
kontelio.invoices().issue(id);
kontelio.invoices().send(id, new InvoiceSendParams().to("ap@example.de").message("Anbei Ihre Rechnung."));
kontelio.invoices().cancel(id, new InvoiceCancelParams().reason("Falscher Rechnungsempfänger"));

// Payments
kontelio.invoices().recordPayment(id, PaymentParams.of("1190.00").method(PaymentMethod.BANK_TRANSFER));

// Documents
byte[] pdf = kontelio.invoices().downloadPdf(id);
String xml = kontelio.invoices().downloadEInvoice(id, EInvoiceFormat.XRECHNUNG_UBL, null);

// Pagination
kontelio.invoices().list(new InvoiceListParams().status(InvoiceStatus.OVERDUE))
    .autoPagingStream()
    .forEach(inv -> System.out.println(inv.number()));

// Customers
Customer customer = kontelio.customers().create(new CustomerParams().name("Example GmbH").externalReference("CRM-4711"));
```

## Error handling

```java
try {
  kontelio.invoices().create(params);
} catch (ValidationException e) {
  e.details().forEach(d -> log.warn("{}: {}", d.field(), d.message()));
} catch (ConflictException e) {
  // e.g. duplicate externalReference
} catch (ApiException e) {
  log.error("Kontelio error {} {} (request {})", e.status(), e.code(), e.requestId());
}
```

All exceptions are unchecked and extend `KontelioException`. Connection errors, timeouts, `408`, `429` and `5xx` are retried automatically with exponential backoff; every `POST` carries an `Idempotency-Key` that is reused across retries.

## Webhooks (Spring Boot)

```java
@PostMapping("/webhooks/kontelio")
ResponseEntity<Void> webhook(@RequestBody String body, @RequestHeader(Webhooks.SIGNATURE_HEADER) String signature) {
  WebhookEvent event = kontelio.webhooks().constructEvent(body, signature, webhookSecret);
  if (event.type() == WebhookEventType.INVOICE_PAID) {
    orders.markPaid(event.invoice().externalReference());
  }
  return ResponseEntity.noContent().build();
}
```

## Examples

See [`src/test/java/de/kontelio/examples`](src/test/java/de/kontelio/examples): ZUGFeRD invoice, XRechnung for public authorities, EU reverse charge, idempotent order sync, overdue list, webhook handling.

More documentation (concepts are shared with the TypeScript SDK): [../docs](../docs) · API reference: [../openapi/openapi.yaml](../openapi/openapi.yaml)

## Development

```bash
cd java
mvn verify
```

## License

[Apache License 2.0](../LICENSE)
