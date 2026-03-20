---
name: web-ux-pass
description: Use when polishing Compose Web UX, visual consistency, loading states, typography, or shell behavior. Do not use for backend or domain-only work.
---

# Web UX Pass

## Goals
- Keep web aligned with the shipped mobile product language.
- Prefer updates to shared web primitives before screen-specific overrides.
- Preserve quiet, legible states for loading, empty, error, and refresh behavior.

## Workflow
1. Inspect the matching mobile surface and the shared mobile theme tokens first.
2. Inspect the shared web design layer under `libraries/design/web`.
3. Apply changes in shared primitives or styles before patching individual screens.
4. Verify the final rendered behavior with the web build.
5. If typography or branding changed, confirm the assets are actually loaded and not silently falling back.

## Verification
- Run `./gradlew :apps:web:jsBrowserDevelopmentWebpack`.
- If the change touched shared web primitives, check at least one affected screen per major web feature area.
