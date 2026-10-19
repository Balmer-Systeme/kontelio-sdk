package de.kontelio.sdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.kontelio.sdk.exception.WebhookVerificationException;
import de.kontelio.sdk.internal.Json;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Verifies and parses webhook events.
 *
 * <p>Each webhook request carries a {@code Kontelio-Signature} header of the form {@code
 * t=<unix-seconds>,v1=<hex-hmac-sha256>}, where the HMAC is computed over {@code "<t>.<raw
 * body>"} with the endpoint's signing secret.
 */
public final class Webhooks {
  /** Name of the signature header. */
  public static final String SIGNATURE_HEADER = "Kontelio-Signature";

  /** Default maximum event age. */
  public static final Duration DEFAULT_TOLERANCE = Duration.ofMinutes(5);

  private final Clock clock;

  Webhooks(Clock clock) {
    this.clock = clock;
  }

  /**
   * Verifies the signature and returns the parsed event.
   *
   * @param payload the raw request body, exactly as received
   * @param signatureHeader value of the {@code Kontelio-Signature} header
   * @param secret the endpoint's signing secret
   * @throws WebhookVerificationException if the signature is missing, invalid or too old
   */
  public WebhookEvent constructEvent(String payload, String signatureHeader, String secret) {
    return constructEvent(payload, signatureHeader, secret, DEFAULT_TOLERANCE);
  }

  /** Like {@link #constructEvent(String, String, String)} with a custom tolerance ({@link Duration#ZERO} disables it). */
  public WebhookEvent constructEvent(String payload, String signatureHeader, String secret, Duration tolerance) {
    verify(payload, signatureHeader, secret, tolerance);
    try {
      return Json.MAPPER.readValue(payload, WebhookEvent.class);
    } catch (JsonProcessingException e) {
      throw new WebhookVerificationException("Webhook payload is not valid JSON", e);
    }
  }

  /** Raw-bytes variant of {@link #constructEvent(String, String, String)}. */
  public WebhookEvent constructEvent(byte[] payload, String signatureHeader, String secret) {
    return constructEvent(new String(payload, StandardCharsets.UTF_8), signatureHeader, secret);
  }

  /** Verifies the signature without parsing the payload. */
  public void verify(String payload, String signatureHeader, String secret, Duration tolerance) {
    if (secret == null || secret.isEmpty()) throw new WebhookVerificationException("Webhook secret is required");
    if (signatureHeader == null || signatureHeader.isBlank()) {
      throw new WebhookVerificationException("Missing " + SIGNATURE_HEADER + " header");
    }

    Long timestamp = null;
    List<String> signatures = new ArrayList<>();
    for (String part : signatureHeader.split(",")) {
      String[] kv = part.trim().split("=", 2);
      if (kv.length != 2) continue;
      if (kv[0].equals("t")) {
        try {
          timestamp = Long.parseLong(kv[1]);
        } catch (NumberFormatException ignored) {
          // reported as malformed below
        }
      } else if (kv[0].equals("v1")) {
        signatures.add(kv[1]);
      }
    }
    if (timestamp == null || signatures.isEmpty()) throw new WebhookVerificationException("Malformed signature header");

    long now = clock.instant().getEpochSecond();
    if (!tolerance.isZero() && Math.abs(now - timestamp) > tolerance.getSeconds()) {
      throw new WebhookVerificationException("Webhook timestamp is outside the tolerance window");
    }

    byte[] expected = hmac(timestamp + "." + payload, secret).getBytes(StandardCharsets.US_ASCII);
    for (String signature : signatures) {
      if (MessageDigest.isEqual(expected, signature.getBytes(StandardCharsets.US_ASCII))) return;
    }
    throw new WebhookVerificationException("Webhook signature does not match");
  }

  /** Computes a signature header for a payload. Useful for tests. */
  public String sign(String payload, String secret) {
    return sign(payload, secret, clock.instant().getEpochSecond());
  }

  public String sign(String payload, String secret, long timestamp) {
    return "t=" + timestamp + ",v1=" + hmac(timestamp + "." + payload, secret);
  }

  private static String hmac(String data, String secret) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("HmacSHA256 not available", e);
    }
  }
}
