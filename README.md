# Smoke Analytics

[![Integration](https://github.com/feragusper/SmokeAnalytics/actions/workflows/integration.yml/badge.svg?branch=master)](https://github.com/feragusper/SmokeAnalytics/actions/workflows/integration.yml)
[![Android Release](https://github.com/feragusper/SmokeAnalytics/actions/workflows/deployment_playstore.yml/badge.svg?branch=master)](https://github.com/feragusper/SmokeAnalytics/actions/workflows/deployment_playstore.yml)
[![Web Release](https://github.com/feragusper/SmokeAnalytics/actions/workflows/deploy-web-hosting.yml/badge.svg?branch=master)](https://github.com/feragusper/SmokeAnalytics/actions/workflows/deploy-web-hosting.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=feragusper_SmokeAnalytics&metric=alert_status)](https://sonarcloud.io/dashboard?id=feragusper_SmokeAnalytics)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=feragusper_SmokeAnalytics&metric=coverage)](https://sonarcloud.io/component_measures/metric/coverage/list?id=feragusper_SmokeAnalytics)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=feragusper_SmokeAnalytics&metric=bugs)](https://sonarcloud.io/component_measures/metric/reliability_rating/list?id=feragusper_SmokeAnalytics)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=feragusper_SmokeAnalytics&metric=code_smells)](https://sonarcloud.io/component_measures/metric/code_smells/list?id=feragusper_SmokeAnalytics)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=feragusper_SmokeAnalytics&metric=security_rating)](https://sonarcloud.io/component_measures/metric/security_rating/list?id=feragusper_SmokeAnalytics)

Smoke Analytics is a Kotlin Multiplatform smoking journal focused on one thing: making patterns visible without turning the product into noise.

The current `0.12.0` product is organized around five core destinations:

- `The Pulse`: today, the last cigarette, pace, goals, and short status cues
- `Analytics & Map`: smoking frequency and geographic clustering from one destination
- `The Archive`: list and calendar views for the detailed smoking log
- `The Guide`: contextual coaching with live-model or fallback guidance
- `You`: account, preferences, goals, and product actions in one place

## Platforms

- `apps/mobile`: Android app, the visual source of truth for the product
- `apps/web`: Compose for Web version aligned to the same product model
- `apps/wear`: Wear OS surface for quick status and lightweight interaction

## Current Product Surface

### The Pulse
- time since last cigarette and last-smoked clock time
- today count, current pace, weekly and monthly averages
- active goal state and progress
- quick access into the main smoking flow

### Analytics & Map
- smoking frequency trends
- period switching
- map clusters for repeated smoking areas
- location-disabled, no-data, loading, and error states

### The Archive
- day-based log review
- list and calendar modes
- add, edit, and delete entries
- date navigation and archive scanning

### The Guide
- initial insight based on recent smoking context
- intent-based prompts for cravings, stress, and progress
- live-model replies when available
- graceful fallback guidance when live replies are unavailable

### You
- authentication state
- goals
- personal preferences such as custom day start, price, and pack size
- product actions such as share, support, and bug reporting

## Repository Structure

The repository is organized as a Kotlin Multiplatform product, not as a single Android app with extras bolted on later.

```text
apps/
  mobile/     Android application shell
  web/        Compose Web application shell
  wear/       Wear OS app / tile surface

features/
  authentication/
  chatbot/
  goals/
  history/
  home/
  settings/
  stats/

libraries/
  architecture/
  authentication/
  design/
  logging/
  preferences/
  smokes/
  wear/

functions/
  Firebase Functions used by the web product

docs/
  releases/
  discovery/
```

Each feature or library follows the same general split when needed:

- `domain`: shared business logic and contracts
- `data`: implementation details and platform integration
- `presentation`: UI, view state, stores, process holders, and navigation

## Tech Baseline

- Kotlin `2.2.20`
- Jetpack Compose / Compose Multiplatform
- Android Gradle Plugin `8.13.2`
- Java 17 toolchain
- Hilt
- Firebase
- Kotlin Coroutines / Flow
- Compose for Web
- Firebase Functions for the secure web coach relay

## Local Setup

### Requirements

- Java 17
- Android Studio with Android SDK installed
- Node.js available for the Firebase Functions and web deploy tooling

### Android / shared app configuration

Local Android and product-specific keys live in `local.properties`. Typical entries include:

- `google.auth.server.client.id` for production
- `google.auth.server.client.id.staging` for staging
- `google.maps.android.api.key.staging`
- `google.maps.android.api.key.production`
- `google.ai.client.generativeai.api.key.staging`
- `google.ai.client.generativeai.api.key.production`

The mobile Firebase configs used by the current repo live at:

- `apps/mobile/src/staging/google-services.json`
- `apps/mobile/src/production/google-services.json`

### Web coach relay

The web coach no longer uses a browser-side Gemini key. It goes through Firebase Functions.

For GitHub-hosted deploys, the required repository secrets are:

- `WEB_COACH_GEMINI_API_KEY_STAGING`
- `WEB_COACH_GEMINI_API_KEY_PROD`

Those are used by the release workflow to populate the Firebase Functions secret `COACH_GEMINI_API_KEY`.

You do not need to add those web relay secrets to `local.properties` unless you are intentionally building your own manual deploy path outside the repository workflows.

## Build And Run

### Mobile

```bash
./gradlew :apps:mobile:assembleStagingDebug
```

### Web

```bash
./gradlew :apps:web:jsBrowserDevelopmentWebpack
```

### Wear

```bash
./gradlew :apps:wear:assembleDebug
```

If you want to install the Wear build on a connected Wear OS device or emulator:

```bash
adb install -r apps/wear/build/outputs/apk/debug/apps-wear-debug.apk
```

## Verification Defaults

These are the repository-default validation paths used for most product work:

- Web work: `./gradlew :apps:web:jsBrowserDevelopmentWebpack`
- Mobile work: the closest feature compile/test path plus an app-level mobile build when the shell changes materially
- Release or CI work: validate the nearest real workflow/build path, not only a lightweight compile

## Branch And Release Flow

The repository uses:

```text
master <- develop <- feature branch
```

Rules:

- start new work from `develop`
- open feature PRs into `develop`
- merge `develop` into `master` only for a release
- deploy Android and web from `master`
- bump `develop` to the next version only after the release is merged and deployed

Current product version:

- `product.version=0.12.0`

## CI And Automation

GitHub Actions drive the main repository automation:

- `integration.yml`: validation on PRs
- `deployment_playstore.yml`: Android release path
- `deploy-web-hosting.yml`: production web deploy path

Release notes are expected in GitHub Releases, not as a growing set of historical markdown files in the repo.

## Contributor Notes

- mobile is the visual source of truth unless a task explicitly needs platform divergence
- prefer shared domain logic and shared presentation models over platform-specific duplication
- keep issue state and project board state aligned
- do not widen product scope inside maintenance tickets

Companion docs:

- [AGENTS.md](AGENTS.md)
- [RULES.md](RULES.md)
- [SKILLS.md](SKILLS.md)
- [docs/agent-workflow.md](docs/agent-workflow.md)

## Screenshots

The old screenshots previously referenced by this README were removed because they no longer represented the current product. The next README asset pass should add refreshed captures from the current `0.12` app surfaces instead of reusing pre-revamp images.

## Support

- Issues: [github.com/feragusper/SmokeAnalytics/issues](https://github.com/feragusper/SmokeAnalytics/issues)
- Discussions and code: [github.com/feragusper/SmokeAnalytics](https://github.com/feragusper/SmokeAnalytics)
- Contact: `feragusper@gmail.com`

## License

```text
Copyright 2025 feragusper

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
