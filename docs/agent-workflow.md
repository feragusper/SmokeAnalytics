# Agent Workflow

## Automatic Vs Companion Context
- `AGENTS.md` is the primary repository instruction file Codex reads automatically.
- `RULES.md`, `SKILLS.md`, and the files under `.codex/` and `.agents/` are companion assets that clarify structure and reusable workflows.
- Keep critical repository expectations in `AGENTS.md`. Do not hide mandatory guidance only in companion files.

## Repository Layout For Codex
- `AGENTS.md`
  - durable repository expectations
- `apps/web/AGENTS.md`, `apps/mobile/AGENTS.md`, `.github/workflows/AGENTS.md`
  - subtree-specific overrides for specialized work
- `.agents/skills/`
  - repository-scoped skills discoverable by Codex
- `.codex/skills/`
  - OpenSpec workflow skills for Codex

## When To Reach For Each Tooling Layer
- Use `AGENTS.md` for stable repo expectations that should apply to almost every task.
- Use nested `AGENTS.md` files when a subtree has specialized constraints such as Compose Web UX, Android app wiring, or GitHub workflow behavior.
- Use repo skills for repeated, focused workflows such as web polish, KMP feature passes, and release hygiene.

## Repository-Specific Defaults
- Check the matching mobile surface before pushing web design changes.
- Prefer shared derivation logic for date ranges, summaries, cadence, and other product-wide calculations.
- Keep release workflows and GitHub project state synchronized with implemented work.
