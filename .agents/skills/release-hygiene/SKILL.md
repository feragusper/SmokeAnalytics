---
name: release-hygiene
description: Use for versioning, GitHub Actions, tags, releases, deploy workflows, or backlog state changes tied to shipped work.
---

# Release Hygiene

## Goals
- Keep code, Gradle metadata, GitHub workflows, and project state aligned.
- Prevent Android and web release outputs from drifting or colliding operationally.

## Workflow
1. Identify the single source of truth for product version and derived platform versions.
2. Inspect the affected workflow files before editing release behavior.
3. Keep tags, release names, and CI metadata platform-specific when Android and web ship on different cadences.
4. Use the branch flow `master <- develop <- feature branch`. Release PRs must go from `develop` into `master`.
5. Do not merge the release PR until its checks are green.
6. Before merging the release PR, create or update `docs/releases/<product.version>.md`. This file is the release-notes source of truth for GitHub releases and Play Store notes.
7. After the release PR lands on `master`, trigger the Android Play Store and web production deploy workflows from `master`.
8. Confirm the resulting tags, GitHub releases, and Play Store release notes match the version and notes derived from the repo state.
9. If a recent release is missing GitHub release notes, backfill it when the summary can be reconstructed confidently.
10. After the release is out, bump `version.properties` on `develop` to the next product version before continuing feature work.
11. Update GitHub issues or project state if the task materially changes backlog or release status.

## Verification
- Run the nearest relevant Gradle task.
- Re-check the affected workflow definitions after editing.
- When preparing a release, validate the derived Android and web release metadata from Gradle before opening the release PR.
- When preparing release notes, verify that `docs/releases/<product.version>.md` exists and is appropriate for both GitHub Releases and Play Store distribution notes.
- After bumping post-release version metadata, confirm the single source of truth was updated in the repo.
- Summarize what was verified locally and what still depends on remote CI or deploy credentials.
