/** A single field-level problem reported by the API on a validation error. */
export interface FieldError {
  /** Dotted path of the offending field, e.g. `lines[0].unitPrice`. */
  field: string;
  code: string;
  message: string;
}

/** Shape of the error body returned by the Kontelio API. */
export interface ApiErrorBody {
  error?: {
    type?: string;
    code?: string;
    message?: string;
    param?: string;
    details?: FieldError[];
  };
}

/** Base class for every error thrown by the SDK. */
export class KontelioError extends Error {
  constructor(message: string, options?: { cause?: unknown }) {
    super(message, options);
    this.name = new.target.name;
  }
}

/** The API answered with a non-2xx status code. */
export class APIError extends KontelioError {
  readonly status: number;
  readonly code: string | undefined;
  readonly type: string | undefined;
  readonly param: string | undefined;
  readonly details: FieldError[];
  readonly requestId: string | undefined;
  readonly headers: Headers;
  readonly body: unknown;

  constructor(status: number, body: unknown, headers: Headers, message?: string) {
    const err = (body as ApiErrorBody | undefined)?.error;
    super(message ?? err?.message ?? `Kontelio API request failed with status ${status}`);
    this.status = status;
    this.code = err?.code;
    this.type = err?.type;
    this.param = err?.param;
    this.details = err?.details ?? [];
    this.requestId = headers.get("x-request-id") ?? undefined;
    this.headers = headers;
    this.body = body;
  }

  /** @internal */
  static from(status: number, body: unknown, headers: Headers): APIError {
    switch (status) {
      case 400:
        return new BadRequestError(status, body, headers);
      case 401:
        return new AuthenticationError(status, body, headers);
      case 403:
        return new PermissionDeniedError(status, body, headers);
      case 404:
        return new NotFoundError(status, body, headers);
      case 409:
        return new ConflictError(status, body, headers);
      case 422:
        return new ValidationError(status, body, headers);
      case 429:
        return new RateLimitError(status, body, headers);
      default:
        return status >= 500
          ? new InternalServerError(status, body, headers)
          : new APIError(status, body, headers);
    }
  }
}

/** 400 – malformed request. */
export class BadRequestError extends APIError {}

/** 401 – missing or invalid API key. */
export class AuthenticationError extends APIError {}

/** 403 – the API key is valid but lacks the required scope. */
export class PermissionDeniedError extends APIError {}

/** 404 – the requested object does not exist. */
export class NotFoundError extends APIError {}

/**
 * 409 – the request conflicts with the current state, e.g. an invoice with the
 * same `externalReference` already exists, or an issued invoice was modified.
 */
export class ConflictError extends APIError {}

/** 422 – the request body failed validation. See {@link APIError.details}. */
export class ValidationError extends APIError {}

/** 429 – too many requests. */
export class RateLimitError extends APIError {
  /** Seconds to wait before retrying, if the API provided a hint. */
  get retryAfter(): number | undefined {
    const value = this.headers.get("retry-after");
    if (value == null) return undefined;
    const seconds = Number(value);
    return Number.isFinite(seconds) ? seconds : undefined;
  }
}

/** 5xx – something went wrong on Kontelio's side. */
export class InternalServerError extends APIError {}

/** The request could not reach the API (DNS, TLS, connection reset, ...). */
export class APIConnectionError extends KontelioError {}

/** The request did not complete within the configured timeout. */
export class APITimeoutError extends APIConnectionError {
  constructor(timeoutMs: number) {
    super(`Request timed out after ${timeoutMs} ms`);
  }
}

/** The request was aborted through the caller's `AbortSignal`. */
export class APIUserAbortError extends KontelioError {
  constructor() {
    super("Request was aborted");
  }
}

/** A webhook payload could not be verified. */
export class WebhookVerificationError extends KontelioError {}
