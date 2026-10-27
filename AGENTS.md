# AGENTS.md — Instructions for Coding AI

You are working on Ordra, an Android-only local-first parcel/order manager.

## Mandatory reading order
1. `docs/BIBLE.md`
2. `docs/RULES.md`
3. `docs/TECH_STACK.md`
4. Relevant file in `docs/plan/`
5. Relevant checklist in `docs/qa/`

## Before changing code
- Inspect the current repository and report what already exists.
- Compare implementation against the current phase acceptance criteria.
- Preserve correct existing work.
- Identify migrations or compatibility risks before changing persisted data.
- Produce a short implementation plan.

## During implementation
- Work only on the requested phase/milestone.
- Keep code compiling as often as practical.
- Add/update tests with behavior changes.
- Never add unrelated features because they seem useful.
- Do not start future phases early.

## Commits and sync
- Commit after every completed feature or milestone — one feature, one commit.
- Write clear commit messages describing the change.
- Push each commit to `origin main` so the remote always reflects finished work.
- Never commit secrets, credentials, or git-ignored files.
- Do not bundle unrelated changes into one commit.

## Definition of done
A milestone is DONE only when:
- Required behavior exists.
- Project builds successfully.
- Relevant tests pass.
- No known critical regression remains.
- QA checklist has evidence/status.
- Documentation is updated if architecture/data behavior changed.

## Forbidden shortcuts
- Fake/mock completion presented as production behavior.
- Hardcoded secrets (including any Groq credential outside local git-ignored config).
- Destructive DB reset to avoid writing migrations.
- Raw exception messages in UI.
- Silent AI-generated order changes without validation.
- Any backend, cloud database, cloud sync, account/auth system, or mandatory network dependency without explicit owner approval.
- Starting the next phase automatically after a gate passes — report and wait.
