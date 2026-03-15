# Agent Workflow

## When changing web UI
1. Read the matching mobile screen and mobile theme tokens.
2. Update shared web primitives first.
3. Reduce explanatory copy unless the screen truly needs it for clarity.
4. Keep icons and brand marks simple enough to read in the browser tab and sidebar.

## When changing shared design
1. Check every web route that imports the shared primitive.
2. Verify light and dark tokens still map cleanly.
3. Re-run the web build after asset changes and after style changes.

## Useful commands
- `./gradlew :apps:web:jsBrowserDevelopmentWebpack`
- `./gradlew :apps:web:jsBrowserTest`
- `rg -n "old-token|old-copy|old-asset" apps/web features libraries`
