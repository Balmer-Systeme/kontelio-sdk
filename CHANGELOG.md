# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the project uses
[Semantic Versioning](https://semver.org/).

## [0.1.0] - 2026-10-19

### Added

- `KontelioClient` with API-key authentication, configurable timeouts, automatic
  retries with exponential backoff and automatic idempotency keys.
- Invoices: create, retrieve, update, list, delete, issue, send, cancel,
  record/list payments, download PDF and e-invoice XML.
- Customers: create, retrieve, update, list, delete.
- Cursor pagination with `for await` auto-iteration.
- Typed error hierarchy (`ValidationError`, `ConflictError`, `RateLimitError`, ...).
- Webhook signature verification (`kontelio.webhooks.constructEvent`).
- E-invoice formats: ZUGFeRD (EN 16931), XRechnung CII and UBL.
- OpenAPI 3.1 specification in `openapi/openapi.yaml`.
- ESM and CommonJS builds with TypeScript declarations; zero runtime dependencies.
- Java SDK `de.kontelio:kontelio-sdk` (Java 17+) with the same feature set:
  invoices, customers, pagination, typed exceptions, retries, webhooks.

[0.1.0]: https://github.com/Balmer-Systeme/kontelio-sdk/releases/tag/js-v0.1.0
