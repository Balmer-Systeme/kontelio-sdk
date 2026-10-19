# Getting started & authentication

## Requirements

- Node.js 20 or newer, Bun, Deno, or any runtime with `fetch` and Web Crypto
  (Cloudflare Workers, Vercel Edge, ...).
- A Kontelio account with API access. API keys are created in Kontelio under
  **Settings → Integrations → API keys**.

## Install

```bash
npm install @kontelio/sdk
# or
pnpm add @kontelio/sdk
yarn add @kontelio/sdk
bun add @kontelio/sdk
```

## API keys

| Prefix | Environment | Effect |
| --- | --- | --- |
| `kt_test_` | Test | Invoices are created in a sandbox, no emails are sent, nothing reaches your bookkeeping. |
| `kt_live_` | Live | Real invoices with sequential numbers (GoBD). |

Keep keys out of source code. The client reads `KONTELIO_API_KEY` automatically:

```ts
import { KontelioClient } from "@kontelio/sdk";

const kontelio = new KontelioClient(); // uses process.env.KONTELIO_API_KEY
```

## Client options

```ts
const kontelio = new KontelioClient({
  apiKey: "kt_live_...",
  baseUrl: "https://api.kontelio.de/v1", // or KONTELIO_BASE_URL
  timeoutMs: 30_000,                      // default 60 000
  maxRetries: 3,                          // default 2
  apiVersion: "2026-10-01",               // pin the API version
  defaultHeaders: { "X-Tenant": "shop-1" },
  fetch: customFetch,                     // proxies, testing, older runtimes
});
```

Every method also accepts per-request options as its last argument:

```ts
await kontelio.invoices.retrieve("inv_123", {
  timeoutMs: 5_000,
  maxRetries: 0,
  signal: abortController.signal,
  headers: { "X-Trace-Id": traceId },
});
```

## CommonJS

```js
const { KontelioClient } = require("@kontelio/sdk");
```
