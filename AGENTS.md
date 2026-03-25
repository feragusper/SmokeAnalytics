# SmokeAnalytics Agent Guide

## Repository Purpose
- Kotlin Multiplatform product with `apps/mobile`, `apps/web`, and shared feature/library modules.
- Mobile is the visual source of truth unless the task explicitly calls for platform-specific divergence.
- Web uses Compose for Web. Do not assume React, Vite, or a Node-first frontend stack.

## Core Working Agreements
- Inspect the affected flow on both mobile and web before editing when the feature exists on both platforms.
- Prefer shared domain logic, shared presentation models, and shared design primitives over platform-specific duplication.
- Treat release/versioning, GitHub backlog state, and CI behavior as product concerns. Keep code and operational state in sync.
- Keep copy minimal. Prefer structure, hierarchy, and reusable components over explanatory text.

## UI And Design Defaults
- Reuse tokens from `libraries/design/mobile` and `libraries/design/web` before introducing new values.
- Web should inherit mobile color, surface, spacing, motion, and brand decisions unless the user asks otherwise.
- Quiet loading, empty, error, and refresh states are preferred over flashy treatments.
- Typography changes are real product changes. If web font choice matters, verify the font is actually loaded.

## Verification Defaults
- Web work: run `./gradlew :apps:web:jsBrowserDevelopmentWebpack`.
- Mobile work: run the closest feature compile/test task and at least one app-level build when the UI shell changes materially.
- Release or CI work: validate the nearest real task or workflow path, not only a lightweight development compile.

## Branch Flow
- The repository branch model is `master <- develop <- feature branch`.
- Start new work from `develop`, not from `master`.
- Open feature/tranche PRs into `develop`.
- Merge `develop` into `master` only when preparing a release.
- Release flow is: open PR `develop -> master`, wait for green checks, merge, deploy Android and web from `master`, then bump `version.properties` on `develop` to the next version before resuming feature work.
- Tags and deploys happen from the release state on `master`, not from feature branches.
- If work is merged to `master` by mistake, bring `develop` forward before starting the next branch.
- Never use PR auto-delete or `gh pr merge --delete-branch` when the PR head branch is `develop` or `master`.
- Delete only short-lived feature branches after merge. Long-lived branches must remain in the remote.

## Repo-Specific Agent Assets
- Subtree-specific overrides live in `apps/web/AGENTS.md`, `apps/mobile/AGENTS.md`, and `.github/workflows/AGENTS.md`.
- Repository skills live in `.agents/skills/`.
- Project-scoped custom agents live in `.codex/agents/`.
- Opt-in examples for project-scoped Codex config and MCP setup live in `.codex/`.
- Human-readable companion docs live in `docs/agent-workflow.md`, `RULES.md`, and `SKILLS.md`.

## Guardrails
- Do not add new frontend dependencies for polish work without a clear product reason.
- Keep routing contracts stable unless navigation changes are the task.
- If you change shared design primitives, verify at least one downstream screen per major feature area.
- Prefer a single product-wide source of truth for repeated metadata such as versioning and platform release outputs.
