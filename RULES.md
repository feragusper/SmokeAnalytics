# Repo Rules

## Design Consistency
- Mobile design language is the baseline.
- Web may simplify layouts for desktop, but should not drift in palette, component tone, or interaction feel.
- New UI copy should be concise and functional.
- Typography changes are product changes. Treat font loading and rendered appearance as part of the design system, not as a cosmetic afterthought.

## Technical Guardrails
- Prefer existing KMP modules and shared design primitives over duplicating logic.
- Keep routing contracts stable unless the task explicitly requires navigation changes.
- Avoid mutation-heavy refactors when a targeted edit solves the problem.
- When multiple platforms need related versioning or release metadata, centralize the shared base and derive platform-specific outputs from it.
- Release workflows should use platform-specific tags and metadata so Android and Web do not collide operationally.

## Verification
- Web UI changes: run `./gradlew :apps:web:jsBrowserDevelopmentWebpack`.
- If browser tests are available, run them; if skipped, call that out explicitly.
- Search for leftover temporary styling tokens or old palette values before finishing.
- If the task touches deployment or release flows, validate the closest real release task locally when feasible, not just a development build.
