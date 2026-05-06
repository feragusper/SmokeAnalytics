# Skill Progression Map: 2026-05

This note summarizes which engineering skills are the best next investment based on recent SmokeAnalytics PRs and follow-up fixes merged between April 21, 2026 and April 30, 2026.

## 1. Temporal UI-state modeling for widgets, tiles, and process holders

Why this is next:
- `#274` (`d6ead52`) made the home widget show a live next-smoke countdown.
- `#275` (`82d1ece`) followed immediately to clarify countdown and compact-card behavior.
- `Fix Wear tile data refresh` (`e4d9000`, merged April 30, 2026) then corrected the same product surface again on Wear.

Concrete evidence:
- Mobile widget rendering changed in `apps/mobile/src/main/java/com/feragusper/smokeanalytics/widget/HomeStatusWidget.kt` and `apps/mobile/src/main/java/com/feragusper/smokeanalytics/widget/WidgetSnapshotStore.kt`.
- Shared home derivation and process logic changed in `features/home/domain/src/commonMain/kotlin/com/feragusper/smokeanalytics/features/home/domain/HomeInsights.kt` and `features/home/presentation/mobile/src/main/java/com/feragusper/smokeanalytics/features/home/presentation/process/HomeProcessHolder.kt`.
- Test coverage existed, but the follow-up cadence suggests missing scenarios in `features/home/domain/src/commonTest/kotlin/com/feragusper/smokeanalytics/features/home/domain/HomeInsightsTest.kt`.

What to practice:
- Write scenario matrices for time-based UI states before editing the UI: future countdown, zero crossing, stale snapshot restore, compact-card truncation, and background refresh on Wear.
- Push more of the derivation into shared state tests before touching widget or tile rendering code.

## 2. Android release automation verification across Gradle and GitHub Actions

Why this is next:
- `Align auth client ids across workflows` (`11c0914`) changed deployment and integration workflow inputs.
- `Bump AGP toolchain to 9.2.0 stable` (`a19d658`) changed foundational Android build tooling.
- `Publish Wear OS artifact to Play` (`e0e48be`, merged April 30, 2026) extended release automation again, continuing the recent pattern of delivery-path fixes and additions.

Concrete evidence:
- Workflow paths changed in `.github/workflows/deployment_artifact.yml` and `.github/workflows/integration.yml`.
- Build tooling changed in `gradle/libs.versions.toml` and `gradle/wrapper/gradle-wrapper.properties`.
- The recent merge history around `develop` is dominated by operational changes, not just product code, which is a sign that release-path confidence is still being built.

What to practice:
- Build a repeatable pre-merge checklist for toolchain bumps, workflow secret/input changes, and new Play publishing steps.
- For each release-path change, validate the nearest real workflow path and artifact, not only a local compile.

## 3. Production-safe diagnostics and serialization debugging in auth/settings/tracking flows

Why this is next:
- `Show mobile Google sign-in failures` (`93c111e`) added more visible auth error handling.
- `Show verbose mobile track diagnostics` (`c3e787f`) added more detailed home tracking diagnostics.
- `Diagnose Play Store Firestore writes` (`70983ef`) then had to correct serialization and add targeted diagnostics for settings persistence.

Concrete evidence:
- Diagnostics and state handling changed in `features/settings/presentation/mobile/src/main/java/com/feragusper/smokeanalytics/features/settings/presentation/diagnostics/FirestoreDiagnosticsRunner.kt`, `features/settings/presentation/mobile/src/main/java/com/feragusper/smokeanalytics/features/settings/presentation/process/SettingsProcessHolder.kt`, and `features/home/presentation/mobile/src/main/java/com/feragusper/smokeanalytics/features/home/presentation/process/HomeProcessHolder.kt`.
- Repository boundary fixes landed in `libraries/preferences/data/mobile/src/main/java/com/feragusper/smokeanalytics/libraries/preferences/data/UserPreferencesRepositoryImpl.kt` and `libraries/smokes/data/mobile/src/main/java/com/feragusper/smokeanalytics/libraries/smokes/data/SmokeRepositoryImpl.kt`.
- Test additions in the matching repository/process-holder tests show the right direction, but the production diagnostics pattern still repeats across surfaces.

What to practice:
- Add narrow contract tests at serialization boundaries before shipping diagnostics to UI.
- Prefer removable, operator-focused diagnostics that identify failing inputs and persistence shape without permanently expanding user-facing debug copy.

## Suggested near-term routine

For the next few mobile or release PRs:
- Start with a scenario list before implementation.
- Add at least one shared/domain or repository-boundary test for the risky branch.
- Verify the release or background-update path explicitly if the change touches workflows, widgets, tiles, or persistence.
