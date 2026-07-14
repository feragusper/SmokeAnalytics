# Play Store listing assets

## Texts
- `fastlane/metadata/android/en-US/` — English listing (title, short/full description, changelog).
  Uploaded automatically by `fastlane deploy_playstore` on every production deploy.
- `pending-es-419/es-419/` — Spanish (Latin America) listing, ready to ship.
  **Gate:** enable "Spanish (Latin America) – es-419" as a store-listing language in Play Console first,
  otherwise `supply` fails the deploy on that locale. Once enabled, move it into
  `fastlane/metadata/android/es-419/` and deploy.

## Images (to produce/upload in Play Console or via supply `images/`)
- `feature_graphic.svg` — 1024×500 feature graphic (rasterize to PNG before upload).
- Phone screenshots: capture the localized UI (Home, Analytics, History, Goals, You),
  1080×2400+ PNG, add short caption overlays. See screenshot plan in the release notes.
