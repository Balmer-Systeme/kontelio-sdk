package de.kontelio.sdk.internal;

/** Test access to package-private helpers. */
public final class HttpTransportAccess {
  private HttpTransportAccess() {}

  public static long retryDelay(int attempt) {
    return HttpTransport.retryDelay(attempt, null);
  }
}
