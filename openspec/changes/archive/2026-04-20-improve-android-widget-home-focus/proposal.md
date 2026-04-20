# Improve Android widget Home focus

## Why

The Android widget still presents a flat pulse summary while Home now leads with goal progress, elapsed gap focus, and supporting metrics in Material cards. Updating the widget closes that product gap and makes the glanceable Android surface feel like the same goal-first experience users see when they open Home.

## What Changes

- Redesign the Android Home status widget with a Material-style card hierarchy, clear goal/gap emphasis, and compact supporting metric cards.
- Bring the widget copy and visual priority in line with the current Home focus: goal progress first, elapsed time since last cigarette second, daily count and weekly average as supporting context.
- Add lightweight iconography or icon-like visual markers that work within Glance widget constraints without adding new frontend dependencies.
- Keep existing widget actions stable: tapping the widget opens the app and the quick add action still triggers the existing widget quick-add flow.
- Preserve current snapshot refresh behavior while extending the snapshot contract only if Home-aligned widget content requires additional data.

## Capabilities

### New Capabilities

- `android-home-widget`: Covers the Android Home status widget's glanceable content, Material-style layout, Home-aligned goal/gap focus, and quick actions.

### Modified Capabilities

- None.

## Impact

- Affected Android widget files under `apps/mobile/src/main/java/com/feragusper/smokeanalytics/widget/`.
- Potential shared snapshot changes in `libraries/architecture/domain` and `features/home/domain` if the widget needs explicit goal progress/status values instead of deriving from existing snapshot fields.
- Existing refresh call sites in Home and History process holders remain the source of widget updates.
- Verification should include the nearest domain/widget tests plus `./gradlew :apps:mobile:assembleStagingDebug` because this touches Android widget surface code.
