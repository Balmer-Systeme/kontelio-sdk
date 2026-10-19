# Webhooks

Kontelio notifies your application about changes via HTTPS `POST` requests.
Configure endpoints in Kontelio under **Settings → Integrations → Webhooks**.

## Events

| Event | When |
| --- | --- |
| `invoice.created` / `invoice.updated` | Draft created or changed |
| `invoice.issued` | Invoice finalized and numbered |
| `invoice.sent` | Invoice emailed to the customer |
| `invoice.partially_paid` / `invoice.paid` | Payment recorded or matched from the bank account |
| `invoice.overdue` | Due date passed without full payment |
| `invoice.cancelled` | Cancellation invoice created |
| `customer.created` / `customer.updated` / `customer.deleted` | Customer changes |

## Verifying signatures

Each request carries a `Kontelio-Signature` header
(`t=<timestamp>,v1=<signature>`). Always verify it against the **raw** body:

```ts
const event = await kontelio.webhooks.constructEvent(
  rawBody,                                   // string or Uint8Array, unparsed
  request.headers.get("kontelio-signature"),
  process.env.KONTELIO_WEBHOOK_SECRET!,
);

if (event.type === "invoice.paid") {
  await markOrderPaid(event.data.object.externalReference);
}
```

Events older than 5 minutes are rejected to prevent replay attacks
(`toleranceSeconds` option). During secret rotation the header can contain
several `v1` signatures; any match is accepted.

### Framework notes

- **Express**: use `express.raw({ type: "application/json" })` on the webhook route.
- **Next.js route handlers**: `await request.text()`.
- **Cloudflare Workers / Hono**: `await c.req.text()`.

Respond with a `2xx` status quickly; Kontelio retries failed deliveries with
exponential backoff for up to 3 days. Events may arrive more than once — use
`event.id` to deduplicate.

## Testing

`kontelio.webhooks.sign(payload, secret)` produces a valid header for unit tests.
