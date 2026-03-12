# SmokeAnalytics Agent Guide

## Product Shape
- Kotlin Multiplatform repo with `apps/mobile` and `apps/web`.
- Web UI is Compose for Web, not React/Vite.
- Visual source of truth is mobile. Web should inherit mobile color, surface, spacing, and motion decisions unless the user asks for a platform-specific divergence.

## Default Workflow
1. Inspect the affected feature on both web and mobile before editing.
2. Reuse tokens and patterns from `libraries/design/mobile` or `libraries/design/web` instead of inventing one-off styles.
3. Keep web changes consistent across `home`, `history`, `stats`, `settings`, and `auth`.
4. Verify with Gradle, at minimum `./gradlew :apps:web:jsBrowserDevelopmentWebpack` for web work and the closest feature/module test task when relevant.

## UX Rules
- Prefer minimal copy. Titles and actions should do most of the work.
- Avoid decorative gradients, loud branding, or “glassmorphism” unless explicitly requested.
- Preserve improvements to loading, empty, error, and refresh states, but keep them visually quiet.
- Favicon and brand marks must be legible at small sizes and aligned with the in-app look.

## Editing Rules
- Use `apply_patch` for file edits.
- Do not add new frontend dependencies for web polish without a clear need.
- If you change web L&F, inspect mobile theme tokens first.
- If a change touches shared design primitives, check at least one screen per major web feature after the build.
