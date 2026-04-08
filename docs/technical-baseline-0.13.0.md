# Technical Baseline `0.13.0`

## Scope
- refresh the dependency baseline for the `0.13` polish cycle without reopening product scope
- move the AndroidX Compose and Navigation stack to the latest stable versions verified in official release notes
- document the upgrades that remain intentionally deferred because they need a broader toolchain move or carry unnecessary risk right now

## Reviewed Baseline
- Kotlin: `2.2.20`
- Compose Multiplatform plugin: `1.9.3`
- Android Gradle Plugin: `8.13.2`
- Gradle wrapper: `8.14.3`
- Firebase Android BoM: `34.7.0`
- Hilt Android: `2.57.2`
- AndroidX Compose BOM: `2026.02.01`
- AndroidX Compose runtime: `1.10.6`
- AndroidX Navigation Compose: `2.9.7`
- Material 3: `1.4.0`

## Refresh Applied
- updated AndroidX Compose runtime artifacts from `1.10.0` to `1.10.6`
  - verified against the AndroidX release index, which lists Compose runtime and UI `1.10.6` as the current stable line as of March 25, 2026
- updated the Compose BOM from `2025.12.01` to `2026.02.01`
  - verified against the Jetpack Compose BOM guidance page, which shows `2026.02.01` as the current stable BOM example
- updated AndroidX Navigation from `2.9.6` to `2.9.7`
  - verified against the AndroidX Navigation release notes, which list `2.9.7` as the latest stable release

## Validation
- `./gradlew :apps:web:jsBrowserDevelopmentWebpack`
- `./gradlew :apps:mobile:assembleStagingDebug`

## Deferred On Purpose
- Kotlin remains on `2.2.20`
  - JetBrains lists `2.2.21` as the current stable Kotlin Multiplatform release, but their compatibility guide still documents official Android Gradle Plugin support only through `8.11.1`
  - this repo is already on AGP `8.13.2`, so bumping Kotlin inside this tranche would move further into an unsupported matrix instead of reducing risk
- Android Gradle Plugin and Gradle wrapper remain on `8.13.2` and `8.14.3`
  - they are already near-current for this repo, and changing them together with Kotlin would turn this into a wider toolchain migration instead of a safe catalog refresh
- Firebase Android BoM remains on `34.7.0`
  - Firebase’s official Android release notes currently list `34.7.0` as the latest BoM version
- Wear Tiles / ProtoLayout stack remains unchanged
  - official docs now point toward the `1.6.x` Tiles line, but the associated ProtoLayout guidance is in transition and would benefit from a dedicated Wear verification pass instead of piggybacking on this repo-wide baseline refresh
- Functions runtime dependencies remain unchanged
  - the relay deploy flow was only just stabilized, so npm package churn was intentionally left out of this tranche

## Outcome
`0.13` now builds on a fresher AndroidX UI baseline without widening the supported-matrix risk. Compose and Navigation move to the latest stable releases verified in official docs, while the larger Kotlin, AGP, Wear, and Functions decisions stay explicitly deferred instead of being changed opportunistically.
