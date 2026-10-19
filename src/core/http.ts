import {
  APIConnectionError,
  APIError,
  APITimeoutError,
  APIUserAbortError,
  KontelioError,
} from "./errors.js";
import { VERSION } from "../version.js";

export const DEFAULT_BASE_URL = "https://api.kontelio.de/v1";
export const DEFAULT_TIMEOUT_MS = 60_000;
export const DEFAULT_MAX_RETRIES = 2;
export const API_VERSION = "2026-10-01";

const MAX_RETRY_DELAY_MS = 8_000;
const MAX_RETRY_AFTER_MS = 60_000;

export type FetchLike = (input: string, init: RequestInit) => Promise<Response>;

export interface ClientOptions {
  /** API key. Defaults to the `KONTELIO_API_KEY` environment variable. */
  apiKey?: string;
  /** Defaults to `KONTELIO_BASE_URL` or `https://api.kontelio.de/v1`. */
  baseUrl?: string;
  /** Per-request timeout in milliseconds. Defaults to 60 000. */
  timeoutMs?: number;
  /** Retries for connection errors, 408, 429 and 5xx responses. Defaults to 2. */
  maxRetries?: number;
  /** Custom `fetch` implementation (e.g. for proxies or testing). */
  fetch?: FetchLike;
  /** Headers sent with every request. */
  defaultHeaders?: Record<string, string>;
  /** Pin the API version. Defaults to the version this SDK was built against. */
  apiVersion?: string;
}

export interface RequestOptions {
  /**
   * Idempotency key for POST requests. Generated automatically when omitted,
   * so that automatic retries never create duplicates.
   */
  idempotencyKey?: string;
  timeoutMs?: number;
  maxRetries?: number;
  signal?: AbortSignal;
  headers?: Record<string, string>;
}

export type QueryValue = string | number | boolean | null | undefined | ReadonlyArray<string | number | boolean>;

export interface RequestConfig {
  method: "GET" | "POST" | "PATCH" | "PUT" | "DELETE";
  path: string;
  query?: Record<string, QueryValue> | object;
  body?: unknown;
  responseType?: "json" | "text" | "binary";
  options?: RequestOptions;
}

function readEnv(name: string): string | undefined {
  const proc = (globalThis as { process?: { env?: Record<string, string | undefined> } }).process;
  const value = proc?.env?.[name];
  return value && value.trim() !== "" ? value.trim() : undefined;
}

function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

function generateIdempotencyKey(): string {
  return `kt-sdk-${globalThis.crypto.randomUUID()}`;
}

export function buildQuery(query: object | undefined): string {
  if (!query) return "";
  const params = new URLSearchParams();
  for (const [key, value] of Object.entries(query)) {
    if (value === undefined || value === null) continue;
    if (Array.isArray(value)) {
      for (const item of value) params.append(key, String(item));
    } else {
      params.append(key, String(value));
    }
  }
  const qs = params.toString();
  return qs ? `?${qs}` : "";
}

export class HttpClient {
  readonly baseUrl: string;
  readonly timeoutMs: number;
  readonly maxRetries: number;
  readonly apiVersion: string;
  private readonly apiKey: string;
  private readonly fetchImpl: FetchLike;
  private readonly defaultHeaders: Record<string, string>;

  constructor(options: ClientOptions = {}) {
    const apiKey = options.apiKey ?? readEnv("KONTELIO_API_KEY");
    if (!apiKey) {
      throw new KontelioError(
        "Missing API key. Pass `apiKey` to the KontelioClient or set the KONTELIO_API_KEY environment variable.",
      );
    }
    const fetchImpl = options.fetch ?? (globalThis.fetch as FetchLike | undefined);
    if (!fetchImpl) {
      throw new KontelioError("No `fetch` implementation found. Use Node.js >= 20 or pass `fetch` in the options.");
    }

    this.apiKey = apiKey;
    this.baseUrl = (options.baseUrl ?? readEnv("KONTELIO_BASE_URL") ?? DEFAULT_BASE_URL).replace(/\/+$/, "");
    this.timeoutMs = options.timeoutMs ?? DEFAULT_TIMEOUT_MS;
    this.maxRetries = options.maxRetries ?? DEFAULT_MAX_RETRIES;
    this.apiVersion = options.apiVersion ?? API_VERSION;
    this.fetchImpl = fetchImpl;
    this.defaultHeaders = options.defaultHeaders ?? {};
  }

  async request<T>(config: RequestConfig): Promise<T> {
    const options = config.options ?? {};
    const maxRetries = options.maxRetries ?? this.maxRetries;
    const timeoutMs = options.timeoutMs ?? this.timeoutMs;
    const url = `${this.baseUrl}${config.path}${buildQuery(config.query)}`;

    const headers: Record<string, string> = {
      Accept: config.responseType === "binary" ? "*/*" : "application/json",
      Authorization: `Bearer ${this.apiKey}`,
      "Kontelio-Version": this.apiVersion,
      "User-Agent": `kontelio-sdk-js/${VERSION}`,
      "X-Kontelio-Client": `kontelio-sdk-js/${VERSION}`,
      ...this.defaultHeaders,
      ...options.headers,
    };
    if (config.body !== undefined) headers["Content-Type"] = "application/json";
    if (config.method === "POST") {
      headers["Idempotency-Key"] = options.idempotencyKey ?? generateIdempotencyKey();
    }
    const body = config.body === undefined ? undefined : JSON.stringify(config.body);

    for (let attempt = 0; ; attempt++) {
      let response: Response;
      try {
        response = await this.fetchWithTimeout(url, { method: config.method, headers, body }, timeoutMs, options.signal);
      } catch (error) {
        if (error instanceof APIUserAbortError) throw error;
        const wrapped =
          error instanceof APIConnectionError
            ? error
            : new APIConnectionError(`Connection error: ${(error as Error)?.message ?? String(error)}`, { cause: error });
        if (attempt < maxRetries) {
          await sleep(this.retryDelay(attempt));
          continue;
        }
        throw wrapped;
      }

      if (response.ok) {
        return (await this.parseResponse(response, config.responseType)) as T;
      }

      const errorBody = await this.safeParseJson(response);
      if (attempt < maxRetries && this.shouldRetry(response)) {
        await sleep(this.retryDelay(attempt, response.headers));
        continue;
      }
      throw APIError.from(response.status, errorBody, response.headers);
    }
  }

  private async fetchWithTimeout(
    url: string,
    init: RequestInit,
    timeoutMs: number,
    userSignal: AbortSignal | undefined,
  ): Promise<Response> {
    if (userSignal?.aborted) throw new APIUserAbortError();

    const controller = new AbortController();
    let timedOut = false;
    const timer = setTimeout(() => {
      timedOut = true;
      controller.abort();
    }, timeoutMs);
    const onUserAbort = () => controller.abort();
    userSignal?.addEventListener("abort", onUserAbort, { once: true });

    try {
      return await this.fetchImpl(url, { ...init, signal: controller.signal });
    } catch (error) {
      if (timedOut) throw new APITimeoutError(timeoutMs);
      if (userSignal?.aborted) throw new APIUserAbortError();
      throw error;
    } finally {
      clearTimeout(timer);
      userSignal?.removeEventListener("abort", onUserAbort);
    }
  }

  private shouldRetry(response: Response): boolean {
    const hint = response.headers.get("x-should-retry");
    if (hint === "true") return true;
    if (hint === "false") return false;
    return response.status === 408 || response.status === 429 || response.status >= 500;
  }

  private retryDelay(attempt: number, headers?: Headers): number {
    const retryAfter = headers?.get("retry-after");
    if (retryAfter) {
      const seconds = Number(retryAfter);
      const ms = Number.isFinite(seconds) ? seconds * 1000 : Date.parse(retryAfter) - Date.now();
      if (Number.isFinite(ms) && ms >= 0) return Math.min(ms, MAX_RETRY_AFTER_MS);
    }
    const base = Math.min(500 * 2 ** attempt, MAX_RETRY_DELAY_MS);
    return base * (0.75 + Math.random() * 0.25);
  }

  private async parseResponse(response: Response, responseType: RequestConfig["responseType"]): Promise<unknown> {
    if (responseType === "binary") return new Uint8Array(await response.arrayBuffer());
    if (responseType === "text") return response.text();
    if (response.status === 204) return undefined;
    const text = await response.text();
    if (!text) return undefined;
    try {
      return JSON.parse(text);
    } catch (error) {
      throw new KontelioError(`Invalid JSON in API response: ${text.slice(0, 200)}`, { cause: error });
    }
  }

  private async safeParseJson(response: Response): Promise<unknown> {
    const text = await response.text().catch(() => "");
    if (!text) return undefined;
    try {
      return JSON.parse(text);
    } catch {
      return text;
    }
  }
}
