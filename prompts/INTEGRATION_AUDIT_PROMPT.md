# COPY/PASTE — ORDRA FULL INTEGRATION AUDIT

Audit the implemented Ordra app end-to-end against all canonical project documentation and the Integration & Data Contract Pack under `docs/contracts/` (roadmap: `docs/plan/MEGAPLAN_INTEGRATION.md`).

Verify:
- screen -> ViewModel -> use case -> repository -> data source boundaries
- canonical model/enum/field naming
- Room relationships, transactions, cascades, migrations
- money represented as integer minor units
- no Room entities leaking into UI/domain
- no direct DAO/service calls from Compose
- correct Flow/state propagation
- navigation/back-stack correctness
- local reminders and WorkManager integration
- OCR -> parser -> OrderDraft -> review -> save
- optional Groq behavior and offline fallback
- no committed/hardcoded real Groq key
- notification assistance is optional and confirmation-safe
- tracking contract: shipments as first-class model, tracking_events.shipment_id, source+confidence on every detected status, confidence gates (auto-apply/confirm/suggest-only), StatusNormalizer single layer, no courier API/third-party tracker/scraping, no "live"/"real-time" labeling
- local analytics rules
- returns/refunds/warranty/receipts relationships
- backup/restore round trip and versioning
- validation/error mapping
- offline behavior
- privacy/logging rules
- TODO/FIXME/placeholders/dead/duplicate code
- documentation drift

Run the available build/tests and report factual results only.

Output:
1. Executive status: PASS / PASS WITH ISSUES / FAIL
2. Critical blockers
3. High/medium/low findings
4. Contract violations with file references
5. Tests/build executed and results
6. Repair plan in dependency order
7. Release-readiness recommendation

Do not implement unrelated new features during the audit.
