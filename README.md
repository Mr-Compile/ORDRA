# Ordra — Complete Android Project Plan Pack

**Ordra** is an Android-only, local-only, offline-first universal order and parcel manager. Brand line: **All your orders. One place.**

Architecture position: no backend, no accounts, no cloud database, no cloud sync. Room is the source of truth. The only online dependency is the optional Groq API for Smart Import, called directly from the app with a local git-ignored key, and it always degrades to offline OCR + manual review.

This pack is the start-to-finish planning source for the project. Begin with `docs/START_HERE_PROMPT.md`, then follow `docs/plan/MASTER_PLAN.md` and `docs/MEGAPLAN.md`. The coding agent must read `docs/BIBLE.md`, `docs/RULES.md`, `AGENTS.md`, `docs/TECH_STACK.md`, and `docs/BRANDING.md` before implementation.

## Structure
- Root: `README.md` + `AGENTS.md` only; all other docs live under `docs/`
- `docs/`: canonical product/engineering docs (`docs/README.md` is the index)
- `docs/plan/`: one plan folder containing every development phase and the pack megaplans
- `docs/specs/`: architecture/data/privacy/UX/asset specifications
- `docs/qa/`: phase gates, test matrices and audits
- `docs/uiux/`: UI/UX Complete Pack reference docs
- `docs/contracts/`: Integration & Data Contract Pack
- `prompts/`: reusable coding-agent prompts
- `branding/` and `assets/`: Ordra visual foundations

## Non-negotiable workflow
Plan → Implement → Test → Audit → Repair → Verify → Commit → Next phase.
