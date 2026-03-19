# Fitbit Discovery

## Decision
- Fitbit integration is feasible, but not ready for implementation in this tranche.
- Treat it as a backend-backed, mobile-first integration, not a quick local feature.

## What is feasible
- Fitbit documents OAuth 2.0 Authorization Code with PKCE for user consent.
- That is compatible with a mobile app flow and an eventual web onboarding flow.
- Fitbit also exposes health and activity endpoints that could help correlate smoking patterns with:
  - heart rate
  - sleep
  - activity

## Important constraint
- Fitbit documents that third-party developers who want access to other users' intraday data for client/server applications must request that access separately.
- That makes intraday correlation the risky part of the MVP.

## Recommended MVP
- Import read-only Fitbit signals that do not require an over-ambitious first scope plan.
- Start with:
  - profile
  - activity summary
  - resting heart rate or heart time series where permitted
  - sleep summary
- Use these only for correlation and contextual insights, not for automated coaching claims.

## Required architecture
- OAuth redirect handling with PKCE
- secure token storage
- refresh-token lifecycle
- explicit data deletion / disconnect flow
- user-facing explanation of why Fitbit is connected and what data is used

## Why this is not ready yet
- The app has no dedicated backend/token relay for third-party health integrations.
- The product still needs a clear privacy story before importing health data.
- Intraday access is the most interesting correlation source, but also the most constrained.

## Recommended next step
1. Build a proof-of-concept auth flow outside the main app UX.
2. Request the minimum scopes needed for a read-only MVP.
3. Decide whether summary-level Fitbit data is already useful enough before chasing intraday access.

## Official references
- Fitbit authorization guide: https://dev.fitbit.com/build/reference/web-api/developer-guide/authorization/
- Fitbit intraday heart-rate data notes: https://dev.fitbit.com/build/reference/web-api/intraday/get-heartrate-intraday-by-date-range/
