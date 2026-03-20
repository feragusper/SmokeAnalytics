---
name: kmp-feature-pass
description: Use for Kotlin Multiplatform feature work that spans shared domain logic, process holders, and mobile/web presentation layers.
---

# KMP Feature Pass

## Goals
- Keep shared logic truly shared when product behavior spans mobile and web.
- Minimize divergence between platform implementations unless product requirements demand it.

## Workflow
1. Read the feature's shared domain model and use cases.
2. Read the matching process holder or store logic.
3. Read both mobile and web presentation layers if both exist.
4. Extend shared contracts deliberately before patching per-platform UI behavior.
5. Verify the closest affected platform build plus the nearest feature-level test task.

## Guardrails
- Preserve public entrypoints where possible.
- Prefer additive shared models over duplicated platform-only derivations.
- Keep date-boundary, cadence, and summary logic centralized.
