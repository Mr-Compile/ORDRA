# Codebase Audit Prompt

Before each new milestone, audit the repository against BIBLE.md, RULES.md and the active phase.

Report:
1. What is implemented and verified.
2. What is partially implemented.
3. What is missing.
4. What violates architecture/rules.
5. Dead/duplicate code or dependencies.
6. Database migration risks.
7. Navigation/back-stack problems.
8. Security/privacy issues or exposed secrets.
9. Tests that exist and important missing tests.
10. Exact recommended repair order.

Do not modify code during the audit unless explicitly instructed. Do not hallucinate completion. Cite file paths/classes for every important finding.
