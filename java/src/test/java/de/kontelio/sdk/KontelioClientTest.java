package de.kontelio.sdk;

import static org.junit.jupiter.api.Assertions.*;

import de.kontelio.sdk.exception.*;
import de.kontelio.sdk.params.InvoiceCustomer;
import de.kontelio.sdk.params.InvoiceLineParams;
import de.kontelio.sdk.params.InvoiceParams;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class KontelioClientTest {
  private MockServer server;

  @BeforeEach
  void setUp() throws Exception {
    server = new MockServer();
  }

  @AfterEach
  void tearDown() {
    server.close();
  }

  private static InvoiceParams minimalInvoice() {
    return new InvoiceParams().customer(InvoiceCustomer.byId("cus_1")).addLine(InvoiceLineParams.of("x", "1"));
  }

  @Test
  void sendsAuthVersionAndUserAgentHeaders() {
    server.enqueue(200, "{\"id\":\"cus_1\"}");
    server.client().customers().retrieve("cus_1");

    var req = server.request(0);
    assertEquals("GET", req.method());
    assertEquals("/v1/customers/cus_1", req.uri());
    assertEquals("Bearer kt_test_123", req.header("Authorization"));
    assertEquals(Version.API_VERSION, req.header("Kontelio-Version"));
    assertEquals("kontelio-sdk-java/" + Version.VERSION, req.header("User-Agent"));
    assertNull(req.header("Idempotency-Key"));
  }

  @Test
  void addsIdempotencyKeyToPostAndHonoursCustomKey() {
    server.enqueue(201, "{}").enqueue(201, "{}");
    var client = server.client();
    client.invoices().create(minimalInvoice());
    client.invoices().create(minimalInvoice(), RequestOptions.idempotent("order-42"));

    assertTrue(server.request(0).header("Idempotency-Key").matches("kt-sdk-[0-9a-f-]{36}"));
    assertEquals("order-42", server.request(1).header("Idempotency-Key"));
  }

  @Test
  void versionIsFilteredFromPom() {
    assertTrue(Version.VERSION.matches("\\d+\\.\\d+\\.\\d+.*"), Version.VERSION);
  }

  @Test
  void encodesPathParameters() {
    server.enqueue(200, "{}");
    server.client().invoices().retrieve("a/b?c");
    assertEquals("/v1/invoices/a%2Fb%3Fc", server.request(0).uri());
  }

  @Test
  void stripsTrailingSlashFromBaseUrl() {
    var client = KontelioClient.builder().apiKey("k").baseUrl("https://example.test/v1///").build();
    assertEquals("https://example.test/v1", client.baseUrl());
  }

  static Stream<Arguments> errorMapping() {
    return Stream.of(
        Arguments.of(400, BadRequestException.class),
        Arguments.of(401, AuthenticationException.class),
        Arguments.of(403, PermissionDeniedException.class),
        Arguments.of(404, NotFoundException.class),
        Arguments.of(409, ConflictException.class),
        Arguments.of(422, ValidationException.class),
        Arguments.of(429, RateLimitException.class),
        Arguments.of(503, InternalServerException.class),
        Arguments.of(418, ApiException.class));
  }

  @ParameterizedTest
  @MethodSource("errorMapping")
  void mapsStatusCodesToExceptions(int status, Class<? extends ApiException> type) {
    server.enqueue(status, "{\"error\":{\"type\":\"t\",\"code\":\"c\",\"message\":\"boom\"}}", Map.of("X-Request-Id", "req_9"));
    var ex = assertThrows(ApiException.class, () -> server.client().invoices().retrieve("x"));
    assertEquals(type, ex.getClass());
    assertEquals(status, ex.status());
    assertEquals("boom", ex.getMessage());
    assertEquals("c", ex.code());
    assertEquals("req_9", ex.requestId());
  }

  @Test
  void exposesFieldErrors() {
    server.enqueue(422, """
        {"error":{"type":"validation_error","code":"invalid_request","message":"Invalid invoice",
          "details":[{"field":"lines[0].unitPrice","code":"required","message":"is required"}]}}
        """);
    var ex = assertThrows(ValidationException.class, () -> server.client().invoices().create(minimalInvoice()));
    assertEquals(1, ex.details().size());
    assertEquals("lines[0].unitPrice", ex.details().get(0).field());
  }

  @Test
  void handlesNonJsonErrorBodies() {
    server.enqueue(502, "<html>Bad gateway</html>", Map.of("Content-Type", "text/html"));
    var ex = assertThrows(InternalServerException.class, () -> server.client().invoices().retrieve("x"));
    assertEquals("Kontelio API request failed with status 502", ex.getMessage());
    assertEquals("<html>Bad gateway</html>", ex.body());
  }

  @Test
  void exposesRetryAfter() {
    server.enqueue(429, "{}", Map.of("Retry-After", "7"));
    var ex = assertThrows(RateLimitException.class, () -> server.client().invoices().retrieve("x"));
    assertEquals(Duration.ofSeconds(7), ex.retryAfter().orElseThrow());
  }

  @Test
  void retries5xxWithSameIdempotencyKey() {
    server.enqueue(502, "{}", Map.of("Retry-After", "0"))
        .enqueue(503, "{}", Map.of("Retry-After", "0"))
        .enqueue(201, "{\"id\":\"inv_1\"}");
    var invoice = server.client(2).invoices().create(minimalInvoice());

    assertEquals("inv_1", invoice.id());
    assertEquals(3, server.requests.size());
    assertEquals(1, server.requests.stream().map(r -> r.header("Idempotency-Key")).distinct().count());
  }

  @Test
  void doesNotRetry4xx() {
    server.enqueue(422, "{}");
    assertThrows(ValidationException.class, () -> server.client(3).invoices().retrieve("x"));
    assertEquals(1, server.requests.size());
  }

  @Test
  void respectsShouldRetryHeader() {
    server.enqueue(409, "{}", Map.of("X-Should-Retry", "true", "Retry-After", "0")).enqueue(200, "{\"id\":\"inv_1\"}");
    assertEquals("inv_1", server.client(1).invoices().retrieve("x").id());
  }

  @Test
  void wrapsConnectionErrors() {
    var client = KontelioClient.builder().apiKey("k").baseUrl("http://127.0.0.1:1/v1").maxRetries(0).build();
    assertThrows(ApiConnectionException.class, () -> client.invoices().retrieve("x"));
  }

  @Test
  void backoffIsBounded() {
    for (int attempt = 0; attempt < 40; attempt++) {
      long delay = de.kontelio.sdk.internal.HttpTransportAccess.retryDelay(attempt);
      assertTrue(delay > 0 && delay <= 8_000, "delay " + delay);
    }
  }
}
