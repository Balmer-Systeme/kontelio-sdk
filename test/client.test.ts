import { afterEach, describe, expect, it, vi } from "vitest";
import {
  APIConnectionError,
  APITimeoutError,
  APIUserAbortError,
  AuthenticationError,
  ConflictError,
  InternalServerError,
  KontelioClient,
  KontelioError,
  NotFoundError,
  RateLimitError,
  ValidationError,
  VERSION,
} from "../src/index.js";
import { json, mockClient } from "./helpers.js";

describe("KontelioClient configuration", () => {
  afterEach(() => vi.unstubAllEnvs());

  it("throws without an API key", () => {
    vi.stubEnv("KONTELIO_API_KEY", "");
    expect(() => new KontelioClient()).toThrow(KontelioError);
  });

  it("reads the API key and base URL from the environment", () => {
    vi.stubEnv("KONTELIO_API_KEY", "kt_env");
    vi.stubEnv("KONTELIO_BASE_URL", "https://sandbox.example/v1/");
    const client = new KontelioClient({ fetch: async () => json({}) });
    expect(client.baseUrl).toBe("https://sandbox.example/v1");
  });
});

describe("requests", () => {
  it("sends auth, version and user-agent headers", async () => {
    const { client, calls } = mockClient([() => json({ id: "cus_1" })]);
    await client.customers.retrieve("cus_1");
    expect(calls[0]!.url).toBe("https://api.test/v1/customers/cus_1");
    expect(calls[0]!.headers.Authorization).toBe("Bearer kt_test_123");
    expect(calls[0]!.headers["Kontelio-Version"]).toMatch(/^\d{4}-\d{2}-\d{2}$/);
    expect(calls[0]!.headers["User-Agent"]).toBe(`kontelio-sdk-js/${VERSION}`);
  });

  it("adds an idempotency key to POST requests and honours a custom one", async () => {
    const { client, calls } = mockClient([() => json({}), () => json({})]);
    await client.customers.create({ name: "A" });
    await client.customers.create({ name: "B" }, { idempotencyKey: "order-42" });
    expect(calls[0]!.headers["Idempotency-Key"]).toMatch(/^kt-sdk-[0-9a-f-]{36}$/);
    expect(calls[1]!.headers["Idempotency-Key"]).toBe("order-42");
  });

  it("does not send an idempotency key on GET", async () => {
    const { client, calls } = mockClient([() => json({})]);
    await client.invoices.retrieve("inv_1");
    expect(calls[0]!.headers["Idempotency-Key"]).toBeUndefined();
  });

  it("encodes path parameters", async () => {
    const { client, calls } = mockClient([() => json({})]);
    await client.invoices.retrieve("a/b?c");
    expect(calls[0]!.url).toBe("https://api.test/v1/invoices/a%2Fb%3Fc");
  });
});

describe("errors", () => {
  const cases = [
    [401, AuthenticationError],
    [404, NotFoundError],
    [409, ConflictError],
    [422, ValidationError],
    [429, RateLimitError],
    [503, InternalServerError],
  ] as const;

  it.each(cases)("maps HTTP %i to %s", async (status, ErrorClass) => {
    const { client } = mockClient([
      () => json({ error: { type: "x", code: "c", message: "boom" } }, status, { "x-request-id": "req_9" }),
    ]);
    const error = await client.invoices.retrieve("inv_1").catch((e) => e);
    expect(error).toBeInstanceOf(ErrorClass);
    expect(error.status).toBe(status);
    expect(error.message).toBe("boom");
    expect(error.requestId).toBe("req_9");
  });

  it("exposes field errors on validation failures", async () => {
    const { client } = mockClient([
      () =>
        json(
          {
            error: {
              type: "validation_error",
              code: "invalid_request",
              message: "Invalid invoice",
              details: [{ field: "lines[0].unitPrice", code: "required", message: "is required" }],
            },
          },
          422,
        ),
    ]);
    const error = (await client.invoices
      .create({ customer: { id: "cus_1" }, lines: [] as never })
      .catch((e) => e)) as ValidationError;
    expect(error.details).toEqual([{ field: "lines[0].unitPrice", code: "required", message: "is required" }]);
  });

  it("exposes retry-after on rate limit errors", async () => {
    const { client } = mockClient([() => json({}, 429, { "retry-after": "7" })]);
    const error = (await client.invoices.retrieve("x").catch((e) => e)) as RateLimitError;
    expect(error.retryAfter).toBe(7);
  });
});

describe("retries", () => {
  it("retries 5xx responses with the same idempotency key", async () => {
    const { client, calls } = mockClient(
      [() => json({}, 502), () => json({}, 503, { "retry-after": "0" }), () => json({ id: "inv_1" })],
      { maxRetries: 2 },
    );
    vi.spyOn(Math, "random").mockReturnValue(0);
    const result = await client.invoices.create({ customer: { id: "c" }, lines: [{ name: "x", unitPrice: 1 }] });
    expect(result).toEqual({ id: "inv_1" });
    expect(calls).toHaveLength(3);
    expect(new Set(calls.map((c) => c.headers["Idempotency-Key"])).size).toBe(1);
    vi.restoreAllMocks();
  });

  it("does not retry 4xx responses", async () => {
    const { client, calls } = mockClient([() => json({}, 422)], { maxRetries: 3 });
    await expect(client.invoices.retrieve("x")).rejects.toBeInstanceOf(ValidationError);
    expect(calls).toHaveLength(1);
  });

  it("wraps network failures in APIConnectionError", async () => {
    const { client } = mockClient([
      () => {
        throw new TypeError("fetch failed");
      },
    ]);
    await expect(client.invoices.retrieve("x")).rejects.toBeInstanceOf(APIConnectionError);
  });
});

describe("timeouts and aborts", () => {
  const hanging = (url: string, init: RequestInit) =>
    new Promise<Response>((_, reject) => {
      init.signal?.addEventListener("abort", () => reject(new DOMException("aborted", "AbortError")));
    });

  it("throws APITimeoutError when the request takes too long", async () => {
    const client = new KontelioClient({ apiKey: "k", fetch: hanging, timeoutMs: 10, maxRetries: 0 });
    await expect(client.invoices.retrieve("x")).rejects.toBeInstanceOf(APITimeoutError);
  });

  it("throws APIUserAbortError when the caller aborts", async () => {
    const client = new KontelioClient({ apiKey: "k", fetch: hanging, maxRetries: 3 });
    const controller = new AbortController();
    const promise = client.invoices.retrieve("x", { signal: controller.signal });
    controller.abort();
    await expect(promise).rejects.toBeInstanceOf(APIUserAbortError);
  });
});
