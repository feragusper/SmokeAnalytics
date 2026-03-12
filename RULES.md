# Repo Rules

## Design Consistency
- Mobile design language is the baseline.
- Web may simplify layouts for desktop, but should not drift in palette, component tone, or interaction feel.
- New UI copy should be concise and functional.

## Technical Guardrails
- Prefer existing KMP modules and shared design primitives over duplicating logic.
- Keep routing contracts stable unless the task explicitly requires navigation changes.
- Avoid mutation-heavy refactors when a targeted edit solves the problem.

## Verification
- Web UI changes: run `./gradlew :apps:web:jsBrowserDevelopmentWebpack`.
- If browser tests are available, run them; if skipped, call that out explicitly.
- Search for leftover temporary styling tokens or old palette values before finishing.
