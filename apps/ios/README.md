# SmokeAnalytics — iOS app

Native SwiftUI app on top of the shared Kotlin Multiplatform domain + GitLive-Firebase data layer
(`:libraries:shared` → `Shared.xcframework`).

## One-time setup

```bash
# 1. Xcode project generator
brew install xcodegen

# 2. iOS simulator runtime (Xcode has no iOS platform installed yet, ~7GB)
xcodebuild -downloadPlatform iOS
```

Then add your **Firebase configs** (gitignored — they hold secrets):

- `apps/ios/Firebase/Staging/GoogleService-Info.plist` — bundle `com.feragusper.smokeanalytics.staging` (Debug)
- `apps/ios/Firebase/Production/GoogleService-Info.plist` — bundle `com.feragusper.smokeanalytics` (Release)

Debug builds use staging, Release builds use prod (bundle id, Firebase project and the
Google Sign-In URL scheme are all selected per build config in `project.yml`).

## Build & run

```bash
# 1. Build the shared framework (Kotlin → XCFramework)
./gradlew :libraries:shared:assembleSharedDebugXCFramework

# 2. Generate the Xcode project from project.yml
cd apps/ios && xcodegen generate

# 3. Open and run on a simulator
open SmokeAnalytics.xcodeproj
```

Rebuild step 1 whenever the shared Kotlin code changes; rerun step 2 only when `project.yml` changes.

## Architecture

- **Shared (Kotlin):** `:libraries:shared` exports the domain use cases and bundles the GitLive
  repositories (same `commonMain` implementations the web app uses). `KoinKt.doInitKoin()` starts DI.
- **Swift:** `FirebaseApp.configure()` then `doInitKoin()` at launch (`SmokeAnalyticsApp.swift`).
  Use cases are reached through `IosUseCases()`; screens are SwiftUI (Phase 2).

## Status

- **Phase 0 (done):** iOS targets on domain + data, `Shared.xcframework`, Koin init, proof-of-life view.
- **Phase 1:** run on simulator (needs runtime + `GoogleService-Info.plist`), verify Firebase sign-in.
- **Phase 2:** SwiftUI feature screens (home, history, stats, goals, settings, auth).
- **Phase 3:** App Store (Apple Developer Program, signing, screenshots).
