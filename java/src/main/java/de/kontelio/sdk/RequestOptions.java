package de.kontelio.sdk;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Per-request options.
 *
 * <pre>{@code
 * RequestOptions.builder().idempotencyKey("order-42").timeout(Duration.ofSeconds(5)).build()
 * }</pre>
 */
public final class RequestOptions {
  /** No overrides. */
  public static final RequestOptions DEFAULT = builder().build();

  private final String idempotencyKey;
  private final Duration timeout;
  private final Integer maxRetries;
  private final Map<String, String> headers;

  private RequestOptions(Builder builder) {
    this.idempotencyKey = builder.idempotencyKey;
    this.timeout = builder.timeout;
    this.maxRetries = builder.maxRetries;
    this.headers = Map.copyOf(builder.headers);
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Shortcut for {@code builder().idempotencyKey(key).build()}. */
  public static RequestOptions idempotent(String idempotencyKey) {
    return builder().idempotencyKey(idempotencyKey).build();
  }

  public String idempotencyKey() { return idempotencyKey; }
  public Duration timeout() { return timeout; }
  public Integer maxRetries() { return maxRetries; }
  public Map<String, String> headers() { return headers; }

  public static final class Builder {
    private String idempotencyKey;
    private Duration timeout;
    private Integer maxRetries;
    private final Map<String, String> headers = new LinkedHashMap<>();

    private Builder() {}

    /**
     * Idempotency key for POST requests. Generated automatically when omitted, so automatic
     * retries never create duplicates.
     */
    public Builder idempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; return this; }
    public Builder timeout(Duration timeout) { this.timeout = timeout; return this; }
    public Builder maxRetries(int maxRetries) { this.maxRetries = maxRetries; return this; }
    public Builder header(String name, String value) { headers.put(name, value); return this; }

    public RequestOptions build() {
      return new RequestOptions(this);
    }
  }
}
