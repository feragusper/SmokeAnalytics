# Repo Skills

## Web UX Pass
- Inspect mobile theme tokens first.
- Apply changes in `libraries/design/web` before patching individual screens.
- Keep skeletons, transitions, and state handling reusable.
- If typography is part of the UX complaint, verify the rendered font is actually available in the document and not silently falling back.

## KMP Feature Pass
- Read the feature's web presentation module, matching mobile presentation module, and any shared domain/process holder.
- Preserve public entrypoints when possible.

## Release Hygiene
- Keep a single product version source and derive platform versions from it.
- Expose CI-friendly Gradle tasks for any release metadata that workflows need.
- Distinguish platform tags and release names clearly when Android and Web ship on separate cadences.

## Final Check
- Build the affected app target.
- Search for stale copy, stale colors, or stale assets introduced by the previous iteration.
- Summarize what was verified and what remains manual.
