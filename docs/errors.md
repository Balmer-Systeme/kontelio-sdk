# Error handling, retries & idempotency

## Error classes

All errors extend `KontelioError`. HTTP errors extend `APIError` and expose
`status`, `code`, `type`, `message`, `details`, `requestId` and `headers`.

| Class | Status | Typical cause |
| --- | --- | --- |
| `BadRequestError` | 400 | Malformed request |
| `AuthenticationError` | 401 | Missing or invalid API key |
| `PermissionDeniedError` | 403 | Key lacks the required scope |
| `NotFoundError` | 404 | Unknown ID |
| `ConflictError` | 409 | Duplicate `externalReference`, editing an issued invoice |
| `ValidationError` | 422 | Invalid data — see `details` |
| `RateLimitError` | 429 | Too many requests — see `retryAfter` |
| `InternalServerError` | 5xx | Problem on Kontelio's side |
| `APIConnectionError` | – | Network failure |
| `APITimeoutError` | – | Timeout exceeded |
| `APIUserAbortError` | – | Your `AbortSignal` fired |
| `WebhookVerificationError` | – | Invalid webhook signature |

```ts
import { ConflictError, RateLimitError, ValidationError, APIError } from "@kontelio/sdk";

try {
  await kontelio.invoices.create(params);
} catch (error) {
  if (error instanceof ValidationError) {
    for (const d of error.details) console.error(`${d.field}: ${d.message}`);
  } else if (error instanceof ConflictError && error.code === "duplicate_external_reference") {
    // already invoiced
  } else if (error instanceof APIError) {
    console.error(error.status, error.code, error.requestId); // include requestId in support requests
  }
  throw error;
}
```

## Automatic retries

Connection errors, timeouts, `408`, `429` and `5xx` responses are retried up to
`maxRetries` times (default 2) with exponential backoff and jitter. The
`Retry-After` header is respected.

## Idempotency

Every `POST` request carries an `Idempotency-Key` header. The SDK generates one
per call and reuses it across its own retries, so a retry after a network
glitch never creates a second invoice.

For retries that span processes (queues, cron jobs), pass a key derived from
your data — and set `externalReference` as a business-level guarantee:

```ts
await kontelio.invoices.create(
  { customer: { id }, externalReference: order.number, lines },
  { idempotencyKey: `order-${order.number}` },
);
```
