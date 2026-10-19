# Security policy

## Reporting a vulnerability

Please **do not** open public GitHub issues for security problems.

Report vulnerabilities privately via
[GitHub Security Advisories](https://github.com/Balmer-Systeme/kontelio-sdk/security/advisories/new)
or through the contact details on <https://kontelio.de/>.

We acknowledge reports within 3 business days and keep you informed until the
issue is resolved.

## Supported versions

| Version | Supported |
| --- | --- |
| 0.x (latest minor) | ✅ |

## Handling credentials

- Never commit API keys (`kt_live_...`, `kt_test_...`) or webhook secrets.
- Load them from environment variables or a secrets manager.
- Revoke a leaked key immediately in Kontelio under Settings → Integrations.
