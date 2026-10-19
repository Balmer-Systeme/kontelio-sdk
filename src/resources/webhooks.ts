import { WebhookVerificationError } from "../core/errors.js";
import type { WebhookEvent } from "../types.js";

/** Name of the HTTP header carrying the webhook signature. */
export const SIGNATURE_HEADER = "kontelio-signature";

const DEFAULT_TOLERANCE_SECONDS = 300;
const encoder = new TextEncoder();

export interface VerifyOptions {
  /** Maximum age of the event in seconds. Defaults to 300. Set to 0 to disable. */
  toleranceSeconds?: number;
  /** Current time, for testing. */
  now?: Date;
}

function toHex(buffer: ArrayBuffer): string {
  return Array.from(new Uint8Array(buffer), (b) => b.toString(16).padStart(2, "0")).join("");
}

function timingSafeEqual(a: string, b: string): boolean {
  if (a.length !== b.length) return false;
  let diff = 0;
  for (let i = 0; i < a.length; i++) diff |= a.charCodeAt(i) ^ b.charCodeAt(i);
  return diff === 0;
}

function parseHeader(header: string): { timestamp: number; signatures: string[] } {
  let timestamp = NaN;
  const signatures: string[] = [];
  for (const part of header.split(",")) {
    const [key, value] = part.trim().split("=", 2);
    if (!key || !value) continue;
    if (key === "t") timestamp = Number(value);
    else if (key === "v1") signatures.push(value);
  }
  return { timestamp, signatures };
}

/**
 * Verifies and parses webhook events sent by Kontelio.
 *
 * Every webhook request carries a `Kontelio-Signature` header of the form
 * `t=<unix-seconds>,v1=<hex-hmac-sha256>`, where the HMAC is computed over
 * `"<t>.<raw request body>"` with your endpoint's signing secret.
 */
export class Webhooks {
  /** Computes the signature header value for a payload. Useful for tests. */
  async sign(payload: string, secret: string, timestamp = Math.floor(Date.now() / 1000)): Promise<string> {
    const signature = await this.computeSignature(`${timestamp}.${payload}`, secret);
    return `t=${timestamp},v1=${signature}`;
  }

  /**
   * Verifies the signature and returns the parsed event.
   *
   * @param payload The **raw** request body, exactly as received.
   * @param signatureHeader Value of the `Kontelio-Signature` header.
   * @param secret The endpoint's signing secret (`whsec_...`).
   * @throws {WebhookVerificationError} if the signature is missing, invalid or too old.
   */
  async constructEvent(
    payload: string | Uint8Array,
    signatureHeader: string | null | undefined,
    secret: string,
    options: VerifyOptions = {},
  ): Promise<WebhookEvent> {
    const body = typeof payload === "string" ? payload : new TextDecoder().decode(payload);
    await this.verify(body, signatureHeader, secret, options);
    try {
      return JSON.parse(body) as WebhookEvent;
    } catch (error) {
      throw new WebhookVerificationError("Webhook payload is not valid JSON", { cause: error });
    }
  }

  /** Verifies the signature without parsing the payload. */
  async verify(
    payload: string,
    signatureHeader: string | null | undefined,
    secret: string,
    options: VerifyOptions = {},
  ): Promise<void> {
    if (!secret) throw new WebhookVerificationError("Webhook secret is required");
    if (!signatureHeader) throw new WebhookVerificationError(`Missing ${SIGNATURE_HEADER} header`);

    const { timestamp, signatures } = parseHeader(signatureHeader);
    if (!Number.isFinite(timestamp) || signatures.length === 0) {
      throw new WebhookVerificationError("Malformed signature header");
    }

    const tolerance = options.toleranceSeconds ?? DEFAULT_TOLERANCE_SECONDS;
    const now = Math.floor((options.now ?? new Date()).getTime() / 1000);
    if (tolerance > 0 && Math.abs(now - timestamp) > tolerance) {
      throw new WebhookVerificationError("Webhook timestamp is outside the tolerance window");
    }

    const expected = await this.computeSignature(`${timestamp}.${payload}`, secret);
    if (!signatures.some((sig) => timingSafeEqual(sig, expected))) {
      throw new WebhookVerificationError("Webhook signature does not match");
    }
  }

  private async computeSignature(data: string, secret: string): Promise<string> {
    const key = await globalThis.crypto.subtle.importKey(
      "raw",
      encoder.encode(secret),
      { name: "HMAC", hash: "SHA-256" },
      false,
      ["sign"],
    );
    return toHex(await globalThis.crypto.subtle.sign("HMAC", key, encoder.encode(data)));
  }
}
