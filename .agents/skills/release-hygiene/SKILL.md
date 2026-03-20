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
4. Verify the closest real Gradle release/build task, not just a lightweight compile.
5. Update GitHub issues or project state if the task materially changes backlog or release status.

## Verification
- Run the nearest relevant Gradle task.
- Re-check the affected workflow definitions after editing.
- Summarize what was verified locally and what still depends on remote CI or deploy credentials.
