/**
 * Receive Kontelio webhooks in an Express app.
 *
 * IMPORTANT: the signature is computed over the raw body, so use
 * `express.raw()` for this route instead of `express.json()`.
 */
// @ts-nocheck – express is not a dependency of the SDK
import express from "express";
import { KontelioClient, SIGNATURE_HEADER, WebhookVerificationError } from "@kontelio/sdk";

const kontelio = new KontelioClient();
const app = express();

app.post("/webhooks/kontelio", express.raw({ type: "application/json" }), async (req, res) => {
  try {
    const event = await kontelio.webhooks.constructEvent(
      req.body,
      req.header(SIGNATURE_HEADER),
      process.env.KONTELIO_WEBHOOK_SECRET!,
    );

    switch (event.type) {
      case "invoice.paid":
        console.log(`Invoice ${event.data.object.number} was paid`);
        break;
      case "invoice.overdue":
        console.log(`Invoice ${event.data.object.number} is overdue`);
        break;
    }
    res.sendStatus(204);
  } catch (error) {
    if (error instanceof WebhookVerificationError) return res.status(400).send(error.message);
    throw error;
  }
});

app.listen(3000);
