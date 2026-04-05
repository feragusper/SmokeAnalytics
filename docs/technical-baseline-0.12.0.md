# Technical Baseline `0.12.0`

## Scope
- close the remaining modernization pass in `#196` without widening product scope
- keep `mobile`, `web`, and shared modules building cleanly on the same baseline
- remove stale build flags and obvious deprecation paths that were already surfacing in local and CI builds

## Reviewed Baseline
- Kotlin: `2.2.20`
- Compose Multiplatform plugin: `1.9.3`
- Android Gradle Plugin: `8.13.2`
- Firebase Android BoM: `34.7.0`
- Hilt Android: `2.57.2`
- AndroidX Navigation Compose: `2.9.6`
- Material 3: `1.4.0`

These versions were reviewed against the current repository state and retained as the stable release baseline for `0.12.0`. The repo was already close to current on the main catalog entries, so this pass focused on modernization work that reduced maintenance noise without destabilizing the release train.

## Modernization Applied
- removed the stale Gradle property `android.lint.useK2Uast=false`
  - this flag was producing an experimental warning on every Android build and is no longer part of the intended baseline
- migrated Compose Hilt view-model lookups from `androidx.hilt.navigation.compose.hiltViewModel` to `androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel`
  - the version catalog now points at `androidx.hilt:hilt-lifecycle-viewmodel-compose`
  - this removes the deprecation path already reported during mobile builds
- replaced `GlobalScope.promise` usage in the active web surfaces with `rememberCoroutineScope().launch`
  - covered `The Guide`, `You`, and the remaining share actions in the revamp shell
  - this keeps async UI actions scoped to composition instead of using app-wide global coroutines

## Validation
- `./gradlew :apps:web:jsBrowserDevelopmentWebpack`
- `./gradlew :apps:mobile:assembleStagingDebug`

## Deferred On Purpose
- no broad version-catalog bump was forced for `0.12.0`
  - the main stack entries are already near-current and stable in this repo
  - a larger catalog churn this late in the release train would increase regression risk more than product value
- Compose Web still emits some non-blocking warnings outside the files modernized in this pass
  - those are acceptable for `0.12.0`
  - any broader cleanup should be handled as a follow-up only if it starts affecting CI signal or release velocity

## Outcome
`0.12.0` ships on a cleaner build baseline: fewer stale flags, no active mobile deprecation on `hiltViewModel`, and less fragile web async UI plumbing, while preserving product behavior.
