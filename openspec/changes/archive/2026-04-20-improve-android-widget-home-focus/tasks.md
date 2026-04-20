# Tasks

## 1. Widget Data Contract

- [x] 1.1 Review `WidgetSnapshot`, `toWidgetSnapshot`, and `WidgetSnapshotStore` against the new goal/gap-first widget requirements.
- [x] 1.2 Extend the shared widget snapshot contract only if the existing fields cannot express the Home-aligned goal/gap status.
- [x] 1.3 Add or update shared domain tests for any changed widget snapshot derivation or formatting.

## 2. Glance Widget Layout

- [x] 2.1 Redesign `HomeStatusWidget` with a Material-style card hierarchy that makes the goal/gap readout the primary surface.
- [x] 2.2 Add compact supporting metric cards for today's count, weekly average, and target gap without making them the hero content.
- [x] 2.3 Add Glance-safe iconography or icon-like markers for primary status, metrics, and quick add.
- [x] 2.4 Preserve main widget tap behavior and the existing `ACTION_WIDGET_QUICK_ADD` quick-add action.

## 3. Responsive And State Polish

- [x] 3.1 Ensure compact widget sizes keep the primary readout and quick-add affordance visible.
- [x] 3.2 Keep copy short enough for launcher widgets and avoid overlapping or clipped metric text.
- [x] 3.3 Preserve backward-compatible preference reads for any new snapshot fields.

## 4. Verification

- [x] 4.1 Run the nearest affected domain/widget tests.
- [x] 4.2 Run `./gradlew :apps:mobile:assembleStagingDebug`.
- [x] 4.3 Manually inspect the widget layout or preview path if available for compact and wider widget sizes.
