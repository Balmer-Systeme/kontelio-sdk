import { describe, expect, it } from "vitest";
import { KontelioClient, WebhookVerificationError } from "../src/index.js";

const client = new KontelioClient({ apiKey: "k", fetch: async () => new Response() });
const secret = "whsec_test_secret";
const payload = JSON.stringify({ id: "evt_1", object: "event", type: "invoice.paid", data: { object: { id: "inv_1" } } });

describe("webhooks", () => {
  it("verifies a valid signature and parses the event", async () => {
    const header = await client.webhooks.sign(payload, secret);
    const event = await client.webhooks.constructEvent(payload, header, secret);
    expect(event.type).toBe("invoice.paid");
    expect(event.data.object.id).toBe("inv_1");
  });

  it("accepts the raw body as bytes", async () => {
    const header = await client.webhooks.sign(payload, secret);
    const event = await client.webhooks.constructEvent(new TextEncoder().encode(payload), header, secret);
    expect(event.id).toBe("evt_1");
  });

  it("rejects a tampered payload", async () => {
    const header = await client.webhooks.sign(payload, secret);
    await expect(client.webhooks.constructEvent(payload.replace("paid", "sent"), header, secret)).rejects.toBeInstanceOf(
      WebhookVerificationError,
    );
  });

  it("rejects a wrong secret", async () => {
    const header = await client.webhooks.sign(payload, secret);
    await expect(client.webhooks.constructEvent(payload, header, "whsec_other")).rejects.toThrow(/does not match/);
  });

  it("rejects old events", async () => {
    const header = await client.webhooks.sign(payload, secret, 1_000);
    await expect(client.webhooks.constructEvent(payload, header, secret)).rejects.toThrow(/tolerance/);
  });

  it("rejects missing and malformed headers", async () => {
    await expect(client.webhooks.constructEvent(payload, undefined, secret)).rejects.toThrow(/Missing/);
    await expect(client.webhooks.constructEvent(payload, "garbage", secret)).rejects.toThrow(/Malformed/);
  });

  it("accepts any matching v1 signature (secret rotation)", async () => {
    const t = Math.floor(Date.now() / 1000);
    const valid = (await client.webhooks.sign(payload, secret, t)).split("v1=")[1];
    const header = `t=${t},v1=${"0".repeat(64)},v1=${valid}`;
    await expect(client.webhooks.verify(payload, header, secret)).resolves.toBeUndefined();
  });
});
