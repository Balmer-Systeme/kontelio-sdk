package de.kontelio.sdk;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/** Minimal in-process HTTP server answering from a queue of canned responses. */
final class MockServer implements AutoCloseable {
  record Recorded(String method, String uri, Map<String, List<String>> headers, String body) {
    String header(String name) {
      return headers.entrySet().stream()
          .filter(e -> e.getKey().equalsIgnoreCase(name))
          .map(e -> e.getValue().get(0))
          .findFirst()
          .orElse(null);
    }
  }

  record Canned(int status, byte[] body, Map<String, String> headers) {}

  private final HttpServer server;
  private final Deque<Canned> queue = new ArrayDeque<>();
  final List<Recorded> requests = new CopyOnWriteArrayList<>();

  MockServer() throws IOException {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/", exchange -> {
      String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
      requests.add(new Recorded(exchange.getRequestMethod(), exchange.getRequestURI().toString(), Map.copyOf(exchange.getRequestHeaders()), body));
      Canned canned;
      synchronized (queue) {
        canned = queue.poll();
      }
      if (canned == null) canned = new Canned(500, "{\"error\":{\"message\":\"no canned response\"}}".getBytes(StandardCharsets.UTF_8), Map.of());
      canned.headers().forEach((k, v) -> exchange.getResponseHeaders().add(k, v));
      if (!canned.headers().containsKey("Content-Type")) exchange.getResponseHeaders().add("Content-Type", "application/json");
      exchange.sendResponseHeaders(canned.status(), canned.body().length == 0 ? -1 : canned.body().length);
      try (OutputStream out = exchange.getResponseBody()) {
        out.write(canned.body());
      }
    });
    server.start();
  }

  MockServer enqueue(int status, String body) {
    return enqueue(status, body, Map.of());
  }

  MockServer enqueue(int status, String body, Map<String, String> headers) {
    return enqueue(status, body.getBytes(StandardCharsets.UTF_8), headers);
  }

  MockServer enqueue(int status, byte[] body, Map<String, String> headers) {
    synchronized (queue) {
      queue.add(new Canned(status, body, headers));
    }
    return this;
  }

  String url() {
    return "http://127.0.0.1:" + server.getAddress().getPort() + "/v1";
  }

  KontelioClient client() {
    return client(0);
  }

  KontelioClient client(int maxRetries) {
    return KontelioClient.builder().apiKey("kt_test_123").baseUrl(url()).maxRetries(maxRetries).build();
  }

  Recorded request(int index) {
    return new ArrayList<>(requests).get(index);
  }

  @Override
  public void close() {
    server.stop(0);
  }
}
