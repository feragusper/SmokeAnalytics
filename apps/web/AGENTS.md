# Web App Instructions

## Scope
- This subtree owns the Compose for Web app shell, route wiring, static assets, and deploy-facing web app behavior.
- Prefer shared UI primitives from `libraries/design/web` before patching one screen locally.

## Working Rules
- Check the matching mobile flow before changing web UX unless the task is explicitly web-only.
- Keep web typography, spacing, and motion aligned with the shipped product language.
- When adding routes or shell behavior, verify sidebar/navigation consistency across all main screens.

## Verification
- Run `./gradlew :apps:web:jsBrowserDevelopmentWebpack` for any UI or asset change in this subtree.
- If the change affects deploy output or environment-specific assets, also verify the nearest `prepareFirebaseHosting` path.
