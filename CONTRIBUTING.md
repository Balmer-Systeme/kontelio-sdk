# Contributing to the Kontelio SDK

Thanks for helping! Bug reports, feature ideas and pull requests are welcome.

## Before you start

Open an issue first for anything larger than a small fix, so we can agree on
the approach — especially for changes to the public API surface.

## Development

TypeScript SDK (repository root):

```bash
npm install
npm run check        # typecheck + tests + build
npm run test:watch
npm run lint:openapi # validate openapi/openapi.yaml
```

Java SDK (`java/`):

```bash
cd java && mvn verify
```

Project layout:

```text
src/
├── client.ts          KontelioClient
├── core/              HTTP transport, errors, pagination
├── resources/         invoices, customers, webhooks
└── types.ts           public API types
test/                  vitest tests (fetch is mocked, no network)
java/                  Java SDK (Maven, Java 17+)
openapi/openapi.yaml   API contract — keep in sync with src/types.ts
docs/                  user documentation
examples/              runnable examples
```

## Guidelines

- TypeScript: no runtime dependencies. The SDK must run on Node.js, Bun, Deno and
  edge runtimes — only use `fetch`, Web Crypto and other web-standard APIs in `src/`.
- Java: Java 17 baseline; Jackson is the only runtime dependency.
- Keep both SDKs and `openapi/openapi.yaml` in sync when the API changes.
- Every behaviour change needs a test.
- Update `openapi/openapi.yaml`, `docs/` and `CHANGELOG.md` when the API changes.
- Never include real API keys, customer or invoice data in code, tests or issues.

## Releasing (maintainers)

Releases are published manually by the maintainers. Bump the version
(TypeScript: `package.json` and `src/version.ts`; Java: `java/pom.xml`), update
`CHANGELOG.md`, merge to `main` and tag the release (`js-vX.Y.Z` / `java-vX.Y.Z`).
