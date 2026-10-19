package de.kontelio.sdk;

import de.kontelio.sdk.exception.KontelioException;
import de.kontelio.sdk.internal.HttpTransport;
import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Entry point of the Kontelio SDK. Thread-safe; create one instance and reuse it.
 *
 * <pre>{@code
 * KontelioClient kontelio = KontelioClient.fromEnv(); // reads KONTELIO_API_KEY
 *
 * Invoice invoice = kontelio.invoices().create(new InvoiceParams()
 *     .customer(InvoiceCustomer.byId("cus_123"))
 *     .addLine(InvoiceLineParams.of("Software Development", "95.00").quantity(12).unit("HUR"))
 *     .issue(true));
 * }</pre>
 */
public final class KontelioClient {
  /** Default API base URL. */
  public static final String DEFAULT_BASE_URL = "https://api.kontelio.de/v1";

  private final HttpTransport transport;
  private final InvoiceService invoices;
  private final CustomerService customers;
  private final Webhooks webhooks;

  private KontelioClient(Builder builder) {
    String apiKey = builder.apiKey != null ? builder.apiKey : env("KONTELIO_API_KEY");
    if (apiKey == null) {
      throw new KontelioException(
          "Missing API key. Call apiKey(...) on the builder or set the KONTELIO_API_KEY environment variable.");
    }
    String baseUrl = builder.baseUrl != null ? builder.baseUrl : env("KONTELIO_BASE_URL");
    if (baseUrl == null) baseUrl = DEFAULT_BASE_URL;
    baseUrl = baseUrl.replaceAll("/+$", "");

    HttpClient httpClient = builder.httpClient != null
        ? builder.httpClient
        : HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).followRedirects(HttpClient.Redirect.NORMAL).build();

    this.transport = new HttpTransport(
        httpClient, baseUrl, apiKey, builder.apiVersion, builder.timeout, builder.maxRetries, builder.defaultHeaders);
    this.invoices = new InvoiceService(transport);
    this.customers = new CustomerService(transport);
    this.webhooks = new Webhooks(builder.clock);
  }

  /** Creates a client configured from {@code KONTELIO_API_KEY} (and optionally {@code KONTELIO_BASE_URL}). */
  public static KontelioClient fromEnv() {
    return builder().build();
  }

  public static Builder builder() {
    return new Builder();
  }

  public InvoiceService invoices() { return invoices; }
  public CustomerService customers() { return customers; }
  public Webhooks webhooks() { return webhooks; }
  public String baseUrl() { return transport.baseUrl(); }

  private static String env(String name) {
    String value = System.getenv(name);
    return value == null || value.isBlank() ? null : value.trim();
  }

  /** Builder for {@link KontelioClient}. */
  public static final class Builder {
    private String apiKey;
    private String baseUrl;
    private Duration timeout = Duration.ofSeconds(60);
    private int maxRetries = 2;
    private String apiVersion = Version.API_VERSION;
    private HttpClient httpClient;
    private Clock clock = Clock.systemUTC();
    private final Map<String, String> defaultHeaders = new LinkedHashMap<>();

    private Builder() {}

    /** API key. Defaults to the {@code KONTELIO_API_KEY} environment variable. */
    public Builder apiKey(String apiKey) { this.apiKey = apiKey; return this; }
    /** Defaults to {@code KONTELIO_BASE_URL} or {@value #DEFAULT_BASE_URL}. */
    public Builder baseUrl(String baseUrl) { this.baseUrl = baseUrl; return this; }
    /** Per-request timeout. Defaults to 60 seconds. */
    public Builder timeout(Duration timeout) { this.timeout = timeout; return this; }
    /** Retries for connection errors, 408, 429 and 5xx responses. Defaults to 2. */
    public Builder maxRetries(int maxRetries) { this.maxRetries = maxRetries; return this; }
    /** Pin the API version. Defaults to {@link Version#API_VERSION}. */
    public Builder apiVersion(String apiVersion) { this.apiVersion = apiVersion; return this; }
    /** Custom {@link HttpClient} (proxies, SSL context, executor). */
    public Builder httpClient(HttpClient httpClient) { this.httpClient = httpClient; return this; }
    /** Header sent with every request. */
    public Builder defaultHeader(String name, String value) { this.defaultHeaders.put(name, value); return this; }
    /** Clock used for webhook timestamp checks. Useful for tests. */
    public Builder clock(Clock clock) { this.clock = clock; return this; }

    public KontelioClient build() {
      return new KontelioClient(this);
    }
  }
}
