# Design

## Context

Home now uses a goal-first layout: a greeting, a goal hero card, a primary Track action, a last-cigarette/gap section, and compact supporting metrics. The Android widget is implemented with Glance in `HomeStatusWidget.kt` and currently renders a flat pulse summary using the stored `WidgetSnapshot`.

Glance has a smaller component set than Compose Material 3, so the widget cannot directly reuse `Surface`, Material icons, typography tokens, or every Home composable. The implementation still needs to feel materially aligned through hierarchy, color, rounded card surfaces, spacing, and compact icon-like markers that Glance can render reliably.

## Goals / Non-Goals

**Goals:**

- Make the Android widget mirror Home's current priority: goal/gap focus first, daily tracking context second, quick add always easy to reach.
- Use a card-based Glance layout with clear visual hierarchy and Material-style colors from the existing app palette.
- Preserve existing widget entry points: full widget opens the app, quick add starts `MainActivity.ACTION_WIDGET_QUICK_ADD`.
- Keep snapshot refresh owned by the existing Home and History refresh paths.
- Add tests around any shared snapshot derivation or formatting logic that changes.

**Non-Goals:**

- Rebuild Home's full screen inside the widget.
- Add new Android dependencies, image assets, or a separate widget design system.
- Change mobile navigation, routing contracts, or quick-add behavior.
- Add interactive widget configuration beyond the existing quick-add action.

## Decisions

1. **Keep Glance as the rendering layer and approximate Material with supported primitives.**

   The widget should stay in `HomeStatusWidget.kt` using Glance `Column`, `Row`, `Text`, backgrounds, padding, and clickable actions. Material 3 composables are not available in app widgets, so the implementation will use rounded or layered Glance backgrounds where supported, stronger spacing, and compact text/icon markers instead of trying to share Home composables directly.

   Alternative considered: introduce shared Compose UI components for Home and widget. That would not work cleanly because Glance and Compose Material render different component APIs.

2. **Derive the widget hierarchy from the current snapshot first, extending the snapshot only where product meaning requires it.**

   Existing data covers today count, elapsed time, target gap, and weekly average. The first implementation should use those fields to present a goal/gap card, progress toward target gap, and supporting metric cards. If the design needs explicit status labels or goal-aware progress, extend `WidgetSnapshot` in shared architecture/domain and update `toWidgetSnapshot` rather than hardcoding platform-only logic.

   Alternative considered: read Home presentation state directly. That would couple the app widget to a screen process holder and make History-triggered refreshes harder to keep consistent.

3. **Use icon-like text markers unless existing drawable resources already fit.**

   Glance icon support is more constrained than Compose Material icons, and adding new dependencies is not justified for widget polish. Use stable built-in drawable resources if they already exist; otherwise use compact labels or glyph-safe markers that render consistently in widgets.

   Alternative considered: add a Material icon dependency or a new asset set. That adds maintenance for a small surface and risks inconsistent rendering across launchers.

4. **Design for multiple widget sizes with graceful truncation.**

   The widget should have a compact primary readout and supporting cards that can wrap, hide secondary copy, or reduce detail on smaller sizes. Text must stay short and values should use existing duration/decimal formatting helpers or small local helpers.

## Risks / Trade-offs

- **Glance visual parity is limited** -> Use Home's hierarchy, palette, spacing, and wording rather than trying to reproduce every Material 3 component exactly.
- **Snapshot contract may need migration** -> Add defaults or backward-compatible reads in `WidgetSnapshotStore` so existing widget preferences do not break.
- **Launcher widget sizes vary heavily** -> Keep the primary goal/gap readout visible at small sizes and treat secondary metrics as optional supporting content.
- **Icon markers can look inconsistent across devices** -> Prefer existing drawable resources when practical; otherwise keep markers simple and nonessential.
