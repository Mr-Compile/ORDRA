# Ordra — Complete Android Project Plan Pack

**Ordra** is an Android-only, local-only, offline-first universal order and parcel manager. Brand line: **All your orders. One place.**

Architecture position: no backend, no accounts, no cloud database, no cloud sync. Room is the source of truth. The only online dependency is the optional Groq API for Smart Import, called directly from the app with a local git-ignored key, and it always degrades to offline OCR + manual review.

This pack is the start-to-finish planning source for the project. Begin with `START_HERE_PROMPT.md`, then follow `docs/plan/MASTER_PLAN.md`. The coding agent must read `BIBLE.md`, `RULES.md`, `AGENTS.md`, `TECH_STACK.md`, and `BRANDING.md` before implementation.

## Structure
- Root: canonical product/engineering rules
- `docs/plan/`: one plan folder containing every development phase
- `docs/specs/`: architecture/data/privacy/UX specifications
- `docs/qa/`: phase gates and audits
- `prompts/`: reusable coding-agent prompts
- `branding/` and `assets/`: Ordra visual foundations

## Non-negotiable workflow
Plan → Implement → Test → Audit → Repair → Verify → Commit → Next phase.
