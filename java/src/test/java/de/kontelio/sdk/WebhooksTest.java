package de.kontelio.sdk;

import static org.junit.jupiter.api.Assertions.*;

import de.kontelio.sdk.exception.WebhookVerificationException;
import de.kontelio.sdk.model.WebhookEventType;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class WebhooksTest {
  private static final Instant NOW = Instant.parse("2026-10-19T12:00:00Z");
  private static final String SECRET = "whsec_test_secret";
  private static final String PAYLOAD =
      "{\"id\":\"evt_1\",\"object\":\"event\",\"type\":\"invoice.paid\",\"livemode\":false,\"data\":{\"object\":{\"id\":\"inv_1\",\"number\":\"RE-1\"}}}";

  private final Webhooks webhooks =
      KontelioClient.builder().apiKey("k").clock(Clock.fixed(NOW, ZoneOffset.UTC)).build().webhooks();

  @Test
  void verifiesAndParsesEvent() {
    var event = webhooks.constructEvent(PAYLOAD, webhooks.sign(PAYLOAD, SECRET), SECRET);
    assertEquals(WebhookEventType.INVOICE_PAID, event.type());
    assertEquals("RE-1", event.invoice().number());
  }

  @Test
  void acceptsBytes() {
    var event = webhooks.constructEvent(PAYLOAD.getBytes(StandardCharsets.UTF_8), webhooks.sign(PAYLOAD, SECRET), SECRET);
    assertEquals("evt_1", event.id());
  }

  @Test
  void rejectsTamperedPayload() {
    String header = webhooks.sign(PAYLOAD, SECRET);
    assertThrows(WebhookVerificationException.class, () -> webhooks.constructEvent(PAYLOAD.replace("paid", "sent"), header, SECRET));
  }

  @Test
  void rejectsWrongSecret() {
    String header = webhooks.sign(PAYLOAD, SECRET);
    var ex = assertThrows(WebhookVerificationException.class, () -> webhooks.constructEvent(PAYLOAD, header, "whsec_other"));
    assertTrue(ex.getMessage().contains("does not match"));
  }

  @Test
  void rejectsOldEventsUnlessToleranceDisabled() {
    String header = webhooks.sign(PAYLOAD, SECRET, NOW.getEpochSecond() - 600);
    assertThrows(WebhookVerificationException.class, () -> webhooks.constructEvent(PAYLOAD, header, SECRET));
    assertEquals("evt_1", webhooks.constructEvent(PAYLOAD, header, SECRET, Duration.ZERO).id());
  }

  @Test
  void rejectsMissingAndMalformedHeaders() {
    assertThrows(WebhookVerificationException.class, () -> webhooks.constructEvent(PAYLOAD, null, SECRET));
    assertThrows(WebhookVerificationException.class, () -> webhooks.constructEvent(PAYLOAD, "garbage", SECRET));
    assertThrows(WebhookVerificationException.class, () -> webhooks.constructEvent(PAYLOAD, "t=abc,v1=00", SECRET));
  }

  @Test
  void acceptsAnyMatchingSignatureDuringRotation() {
    long t = NOW.getEpochSecond();
    String valid = webhooks.sign(PAYLOAD, SECRET, t).split("v1=")[1];
    String header = "t=" + t + ",v1=" + "0".repeat(64) + ",v1=" + valid;
    assertDoesNotThrow(() -> webhooks.verify(PAYLOAD, header, SECRET, Webhooks.DEFAULT_TOLERANCE));
  }

  @Test
  void signatureIsCompatibleWithJsSdk() {
    // Reference vector produced by the TypeScript SDK: kontelio.webhooks.sign('{"id":"evt_1"}', secret, 1700000000)
    assertEquals(
        "t=1700000000,v1=248a374f50f943a28b0f6ab50faf9a7e7e29b710fa26df9fb1618b9bf8ea9c9a",
        webhooks.sign("{\"id\":\"evt_1\"}", SECRET, 1700000000L));
  }
}
