package de.kontelio.sdk;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Version information of the SDK. */
public final class Version {
  /** SDK version, e.g. {@code 0.1.0}. */
  public static final String VERSION = load();

  /** API version this SDK was built against (sent as {@code Kontelio-Version} header). */
  public static final String API_VERSION = "2026-10-01";

  private Version() {}

  private static String load() {
    try (InputStream in = Version.class.getResourceAsStream("kontelio-sdk.properties")) {
      if (in == null) return "unknown";
      Properties props = new Properties();
      props.load(in);
      return props.getProperty("version", "unknown");
    } catch (IOException e) {
      return "unknown";
    }
  }
}
