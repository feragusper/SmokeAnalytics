# Alexa Discovery

## Decision
- Alexa voice logging is technically feasible, but it should stay discovery-only for now.
- It depends on backend infrastructure the app does not currently have.

## What is feasible
- A custom Alexa skill can receive a user intent such as “log a cigarette”.
- Amazon documents account linking so the skill can act on behalf of the signed-in Smoke Analytics user.
- This could support multiple locales over time, including English and Spanish variants.

## Required architecture
- an Alexa custom skill
- account linking between Amazon and Smoke Analytics
- a backend endpoint that receives the intent and records the smoke for the linked user
- idempotency and audit handling so repeated voice requests do not double-log

## Why this is blocked today
- The current app has no backend API surface for account-linked voice actions.
- Alexa cannot write into the user's smoke history without a service endpoint and token validation.
- Locale support is possible, but each locale needs its own invocation model, sample utterances, and QA pass.

## Recommended MVP
- Start with one intent:
  - `LogSmokeIntent`
- Start with one locale:
  - `en-US`
- Add `es-ES` only after the first locale works and the wording is tuned.

## Recommended next step
1. Design a minimal authenticated endpoint for smoke logging.
2. Define the Alexa account-linking flow.
3. Prototype one locale before committing to multilingual rollout.

## Official references
- Alexa account linking: https://developer.amazon.com/en-US/docs/alexa/account-linking/use-access-tokens.html
