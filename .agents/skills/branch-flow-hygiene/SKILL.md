---
name: branch-flow-hygiene
description: Use for SmokeAnalytics git branch flow work, especially when deciding base branches, updating develop, or preparing release merges.
---

# Branch Flow Hygiene

## Branch Model
- The repo flow is `master <- develop <- feature branch`.
- Feature and tranche branches must start from `develop`.
- Feature and tranche PRs must target `develop`.
- `develop` merges into `master` only for releases.
- Tags and deploys belong to the released state on `master`.

## Workflow
1. Check the current local and remote branch positions before starting work.
2. If a feature branch was merged to `master` by mistake, fast-forward `develop` to include that work before creating the next branch.
3. Create the next working branch from `develop`, not `master`.
4. Keep release operations separate from feature delivery operations.
5. Before merging, verify the PR base branch matches the intended stage of the flow.
6. Before merging any PR, inspect the PR head branch. If the head is `develop` or `master`, do not use branch auto-delete and do not pass `--delete-branch` to `gh pr merge`.

## Guardrails
- Do not open feature PRs against `master` unless the user explicitly asks for a release branch exception.
- Do not tag from `develop` or from a feature branch.
- If branch state is ambiguous, inspect it first instead of assuming the correct base.
- Delete only feature branches after merge. Treat `develop` and `master` as protected long-lived branches both in repo policy and in GitHub settings.
