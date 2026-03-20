# Repo Rules

## What This File Is
This file documents repository guardrails for agents and humans. It is not a Codex execution-policy `.rules` file.

For executable approval-policy examples, see:
- `codex/rules/repo-default.rules.example`
- `codex/rules/release-ops.rules.example`

## Product Rules
- Mobile is the product baseline for visual language.
- Web can adapt for layout density and navigation, but should not drift in tone, palette, or interaction feel without intent.
- New UX work should preserve consistency across `home`, `history`, `stats`, `settings`, `auth`, and any new shared shell patterns.

## Engineering Rules
- Prefer targeted edits over broad refactors unless the refactor pays down real shared complexity.
- Keep shared interfaces stable when possible and extend them deliberately when cross-platform behavior truly changed.
- When a concern spans mobile and web, centralize the model or derivation logic first, then adapt each UI surface.

## Release And CI Rules
- Android and web release metadata must remain distinguishable operationally.
- Changes to workflows, versioning, tags, or release behavior should leave the repo in a state where local Gradle tasks and GitHub workflows tell the same story.
- If a task changes backlog or project state, update GitHub to match the code before finishing.

## Verification Rules
- Call out explicitly when browser tests are skipped or unavailable.
- Prefer the closest real build or workflow path when validating release-related changes.
- Search for stale copy, stale tokens, stale assets, or stale comments before finishing iterative UI work.
