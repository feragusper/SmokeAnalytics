# android-home-widget Specification

## ADDED Requirements

### Requirement: Home-aligned widget hierarchy
The Android Home widget SHALL present a glanceable Home-aligned status hierarchy that prioritizes goal or gap focus before secondary consumption metrics.

#### Scenario: Widget has current snapshot data
- **WHEN** the widget renders a stored Home snapshot
- **THEN** the most prominent content communicates the user's current goal or elapsed-gap focus
- **AND** today's count and weekly average appear as supporting metrics, not as the only hero content.

#### Scenario: Widget opens the app
- **WHEN** the user taps the main widget surface
- **THEN** the app opens through the existing `MainActivity` entry point.

### Requirement: Material-style widget layout
The Android Home widget SHALL use a Material-style card layout with clear hierarchy, app-aligned color, compact spacing, and small visual markers or icons where supported by Glance.

#### Scenario: User scans the widget
- **WHEN** the widget is displayed on the Android launcher
- **THEN** the content is grouped into visually distinct card-like regions
- **AND** the primary readout, supporting metrics, and quick action are distinguishable without explanatory copy.

#### Scenario: Launcher provides a compact widget size
- **WHEN** the launcher renders the widget in a constrained size
- **THEN** the primary goal or gap readout and quick-add affordance remain usable
- **AND** secondary metrics may shorten or reduce detail without overlapping text.

### Requirement: Quick add remains available
The Android Home widget SHALL preserve the existing quick-add action while updating its visual treatment to match the new widget hierarchy.

#### Scenario: User taps quick add
- **WHEN** the user taps the quick-add affordance in the widget
- **THEN** the app starts `MainActivity` with the existing widget quick-add action
- **AND** the action remains visually distinct from the main open-app surface.

### Requirement: Widget snapshot refresh remains consistent
The Android Home widget SHALL continue to refresh from Home and History updates using the shared widget snapshot contract.

#### Scenario: Home refreshes the snapshot
- **WHEN** Home writes a refreshed widget snapshot
- **THEN** the widget updates using the same goal/gap and supporting metric values derived from shared Home domain logic.

#### Scenario: History changes smoke data
- **WHEN** History adds, edits, or removes smoke data and refreshes the widget snapshot
- **THEN** the widget reflects the updated snapshot without requiring a separate widget-only data source.
