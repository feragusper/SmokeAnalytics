# Workflow Instructions

## Scope
- This subtree owns GitHub Actions behavior for CI, release, deploy, and artifact workflows.

## Working Rules
- Preserve the distinction between validation workflows, release workflows, and deploy workflows.
- Avoid duplicate triggers across `pull_request` and `push` unless the duplication is intentional and documented.
- Keep Android and web release metadata operationally distinct.

## Verification
- After editing a workflow, reread the full file and trace when it runs, what it builds, and what remote state it changes.
- Validate the nearest matching local Gradle task so workflow behavior and local expectations stay aligned.
