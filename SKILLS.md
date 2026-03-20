# Repo Skills

## What This File Is
This file is an index of repository-scoped skills and related Codex assets.

Codex-compatible repo skills live in:
- `.agents/skills/web-ux-pass/`
- `.agents/skills/kmp-feature-pass/`
- `.agents/skills/release-hygiene/`

Project-scoped custom subagents live in:
- `.codex/agents/repo-explorer.toml`
- `.codex/agents/release-guardian.toml`

Project-scoped configuration examples live in:
- `.codex/config.example.toml`

## Intended Skill Usage
- `web-ux-pass`
  - Use for web UI polish, loading states, shell changes, and typography/brand consistency checks.
- `kmp-feature-pass`
  - Use for shared feature work that spans domain, process holders, and multiple platform presentation layers.
- `release-hygiene`
  - Use for versioning, GitHub Actions, release tags, deploy workflows, and backlog synchronization.

## Guidance
- Prefer narrow skills with one job over broad catch-all instructions.
- Keep repo skills instruction-first unless a deterministic script is truly needed.
- If a workflow becomes repeatedly useful across tasks, promote it into a repo skill instead of repeating it in ad hoc prompts.
