import { KontelioClient, type ClientOptions } from "../src/index.js";

export interface RecordedCall {
  url: string;
  method: string;
  headers: Record<string, string>;
  body: unknown;
}

type Responder = (call: RecordedCall) => Response | Promise<Response>;

export function json(body: unknown, status = 200, headers: Record<string, string> = {}): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "content-type": "application/json", ...headers },
  });
}

/** Creates a client whose `fetch` answers from a queue of responders. */
export function mockClient(responders: Responder[], options: ClientOptions = {}) {
  const calls: RecordedCall[] = [];
  const queue = [...responders];
  const fetch = async (url: string, init: RequestInit) => {
    const call: RecordedCall = {
      url,
      method: init.method ?? "GET",
      headers: Object.fromEntries(Object.entries((init.headers ?? {}) as Record<string, string>)),
      body: typeof init.body === "string" ? JSON.parse(init.body) : undefined,
    };
    calls.push(call);
    const next = queue.shift();
    if (!next) throw new Error(`Unexpected request: ${call.method} ${url}`);
    return next(call);
  };
  const client = new KontelioClient({
    apiKey: "kt_test_123",
    baseUrl: "https://api.test/v1",
    maxRetries: 0,
    ...options,
    fetch,
  });
  return { client, calls };
}

export const invoiceFixture = {
  id: "inv_01",
  object: "invoice",
  number: "RE-2026-0001",
  status: "issued",
  currency: "EUR",
  totals: { netAmount: "1000.00", vatAmount: "190.00", grossAmount: "1190.00", amountPaid: "0.00", amountDue: "1190.00", vatBreakdown: [] },
};
