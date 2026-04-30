# Play Store Wear OS Publishing

SmokeAnalytics publishes Wear OS as a separate Play artifact under the same app listing as mobile.

## One-Time Play Console Setup

Before the GitHub release workflow can publish Wear OS, configure the form factor in Play Console:

1. Open the SmokeAnalytics app in Play Console.
2. Go to `Test and release > Setup > Advanced settings`.
3. Open the `Form factors` tab.
4. Add `Wear OS`.
5. Upload at least one Wear OS screenshot for the store listing.
6. Upload a Wear OS app bundle or APK to Open testing once when prompted.
7. Return to `Advanced settings > Form factors > Wear OS > Manage`.
8. Opt in to Wear OS distribution and accept the terms.

Google reviews the Wear OS experience before it becomes available on Play.

## Automated Release Behavior

The `deployment_playstore.yml` workflow builds:

- `:apps:mobile:bundleProductionRelease`
- `:apps:wear:bundleProductionRelease`

Fastlane uploads to Open testing:

- mobile AAB to `beta`
- Wear OS AAB to `wear:beta`

Google Play Console labels this track as Open testing. The Google Play Publishing API and Fastlane still use `beta` for the same track, and Wear OS uses the form factor prefix: `wear:beta`.

## Versioning

Wear OS uses the same package name and version name as mobile, but an independent version code:

```text
wearVersionCode = 1_000_000_000 + gitCommitCount
```

Google Play requires version codes to be unique across form factors in the same app listing.

## Local Testing Note

Wear Data Layer communication requires the phone and watch apps to use the same package name and signing certificate. A Play-installed mobile app cannot reliably communicate with a locally installed Wear app if they are signed with different keys.
