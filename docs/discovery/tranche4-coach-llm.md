# Coach / LLM Discovery

## Decision
- Keep the coach constrained, not open-ended chat.
- Use Gemini Developer API as the first production candidate.
- On mobile, move from `gemini-2.0-flash` to `gemini-2.5-flash-lite`.
- On web, do not ship direct model calls from the browser. A backend relay is required first.

## Why this provider
- The repo already uses Google's Generative AI client on Android, so the migration cost is low.
- Gemini 2.5 Flash-Lite is a better fit than the older 2.0 Flash for short coaching summaries and guided follow-ups.
- Google documents a free tier for Gemini Developer API pricing and rate limits, which keeps the first rollout viable without billing on day one.

## Product shape
- First interaction should be a generated coaching summary, not a blank chat.
- The summary should be based on bounded context:
  - today / week / month counts
  - current elapsed time since last smoke
  - recent cadence
  - trend versus the recent baseline
- Follow-up should be constrained to a few coach actions such as:
  - `Why today looks heavy`
  - `How to delay the next smoke`
  - `What improved this week`
- Avoid open free-form chat until cost, abuse, and prompt quality are under control.

## Prompt shape
- System intent:
  - calm
  - concrete
  - non-judgmental
  - short
- Input payload:
  - display name
  - recent smokes
  - counts by day/week/month
  - average gap
  - time since last smoke
  - current trend direction
- Output contract:
  - one summary paragraph
  - up to three bullets
  - one next action

## Cost and rate envelope
- Keep generated responses short and context bounded.
- Do not add grounding, tool use, or long chat history in the first iteration.
- This keeps the mobile coach inside the documented free-tier shape much more safely than open chat.

## Security / platform constraints
- Mobile can continue using the existing direct SDK integration for now.
- Web must not expose a long-lived model key in the shipped bundle.
- To ship the coach on web for real, add a backend relay first, ideally a Firebase callable or authenticated HTTP endpoint.

## Fallback UX
- If the live model fails, fall back to a deterministic coaching summary derived from local insights.
- Web should remain an explicit placeholder until the secure relay exists.

## Next implementation slice
1. Replace the current blank/coming-soon coach entry with a summary-first experience on mobile.
2. Add a server relay for web.
3. Reuse the same prompt contract on both platforms.

## Official references
- Gemini pricing: https://ai.google.dev/gemini-api/docs/pricing
- Gemini models: https://ai.google.dev/models/gemini
- Gemini quotas: https://ai.google.dev/gemini-api/docs/quota
