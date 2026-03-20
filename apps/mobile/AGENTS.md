# Mobile App Instructions

## Scope
- This subtree owns Android app wiring, navigation shell, platform services, widgets, maps, and mobile-specific presentation.
- Shared feature logic should still live outside this subtree when the behavior belongs to both platforms.

## Working Rules
- Prefer existing Material 3 patterns and shared design tokens over one-off mobile-only styling.
- When a change touches app shell behavior, check Home, History, Stats, Settings, and any mobile-only surface affected by the change.
- Treat Google/Firebase keys, manifests, and platform permissions as release-sensitive changes.

## Verification
- Run the nearest feature compile/test task for local changes here.
- When shell, manifest, map, widget, or release-sensitive wiring changes, also run `./gradlew :apps:mobile:assembleStagingDebug`.
