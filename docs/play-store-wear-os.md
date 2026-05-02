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

## Store Listing Requirements

The Wear OS app includes a launchable watch app and a Tile. Google Play Wear OS quality requirement `WO-G2` requires the Play listing description to:

- list the main app features
- mention `tile` when a Tile is included
- mention `complication` only if a complication is included
- avoid the deprecated term `Android Wear`
- be localized in every language offered by the app listing

SmokeAnalytics does not currently include a Wear OS complication. Do not mention complications unless one is added.

Use listing copy that explicitly mentions the Tile. Example English copy:

```text
Smoke Analytics helps you track smoking activity and monitor your daily pace from your phone and Wear OS watch. The watch app lets you check today's count, see your next pace target, and quickly record a smoke. The included Wear OS tile shows your current status at a glance and provides a quick Track action from the tile surface.
```

Example Spanish copy:

```text
Smoke Analytics te ayuda a registrar tu consumo y seguir tu ritmo diario desde el movil y desde tu reloj Wear OS. La app del reloj permite consultar el conteo de hoy, ver el proximo objetivo de ritmo y registrar un consumo rapidamente. El tile de Wear OS incluido muestra tu estado de un vistazo y ofrece una accion rapida para registrar desde el tile.
```

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
