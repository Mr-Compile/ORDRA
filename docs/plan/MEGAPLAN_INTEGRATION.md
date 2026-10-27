# Ordra Integration & Data Contract Megaplan

Exhaustive execution plan for the **ORDRA Integration & Data Contract Pack v1**. One source of
truth for wiring Compose UI → ViewModel → Use Case → Repository → Room/services without drift.
Update the status table after each phase gate; a phase is DONE only when the contract checks and
`docs/qa/INTEGRATION_TEST_MATRIX.md` rows for its scope pass with real executed evidence.

## How to use this megaplan

1. Read the contracts in canonical order (`docs/contracts/README.md`):
   `docs/contracts/CONTRACT_BIBLE.md` → `docs/contracts/DOMAIN_MODELS.md` →
   `docs/contracts/DATABASE_CONTRACT.md` → `docs/contracts/REPOSITORY_USECASE_CONTRACT.md` →
   `docs/contracts/UI_STATE_EVENT_CONTRACT.md` → `docs/contracts/SMART_IMPORT_CONTRACT.md` →
   `docs/contracts/REMINDERS_NOTIFICATION_CONTRACT.md` → `docs/contracts/ANALYTICS_CONTRACT.md` →
   `docs/contracts/BACKUP_RESTORE_CONTRACT.md` → `docs/contracts/ERROR_VALIDATION_CONTRACT.md` →
   `docs/qa/INTEGRATION_TEST_MATRIX.md`
   → `docs/plan/INTEGRATION_PHASES.md`.
2. Product/engineering rules still govern: `docs/BIBLE.md`, `docs/RULES.md`,
   `docs/TECH_STACK.md`, `docs/specs/ARCHITECTURE.md`, `docs/specs/DATA_MODEL.md`.
3. Kick off with `prompts/START_INTEGRATION_PROMPT.md` (audit-first, stop after I0);
   close out with `prompts/INTEGRATION_AUDIT_PROMPT.md` at I9.
4. Execute ONE integration phase at a time: PLAN → IMPLEMENT → TEST → AUDIT → REPAIR →
   VERIFY → COMMIT → STOP. Never implement a later phase because its contract exists.

## Hard rules (apply to every phase)

- Android only; Kotlin + Jetpack Compose; Room/SQLite is the persisted source of truth.
- Core features work fully offline; a network failure never blocks reading/managing orders.
- Dependency direction `UI → Domain → Data`; domain never imports Compose/Room/Retrofit types.
- UI never calls DAOs, Retrofit, WorkManager, or NotificationListenerService directly.
- Money = integer minor units (`Long`) + ISO currency code. Never Float/Double.
- IDs stable; dates consistent — local date-only concepts stay date concepts, conversion in mappers.
- All persisted mutations go through repositories/use cases; multi-table writes are transactional.
- AI/OCR/notification outputs are drafts/suggestions — never silent authoritative mutations.
- No committed/hardcoded Groq key; Groq optional and only for Smart Import.
- One canonical `docs/plan/` + `docs/qa/` structure — this file is the integration roadmap.

## Status key

DONE / IN PROGRESS / PENDING / DEFERRED / BLOCKED

## Phase status board

| I-phase | Scope | Master map | UI map | Status | Notes |
|---|---|---|---|---|---|
| I0 | Contract alignment audit | all | all | PENDING | Run `prompts/START_INTEGRATION_PROMPT.md`; report, don't expand |
| I1 | Core order vertical slice | 1A–1D | UI-2/3/4 | PENDING | Orders list/Add-Edit/Detail end-to-end |
| I2 | Dashboard/COD/analytics | 1C/1D/6 | UI-4/8 | PENDING | Aggregate queries → Home/Insights |
| I3 | Reminders + calendar data | 2 | UI-5 | PENDING | WorkManager + channels |
| I4 | OCR Smart Import | 3 | UI-6 | PENDING | ML Kit → parser → draft → review → save |
| I5 | Groq enhancement | 4 | UI-6 | PENDING | DTO-validated, offline fallback |
| I6 | Marketplace assistance | 5 | UI-7 | PENDING | Share intents + NotificationListener |
| I7 | Advanced records | 7 | UI-8 | PENDING | Receipts/returns/refunds/warranties |
| I8 | Backup/restore/export | 8 | UI-9 | PENDING | Versioned manifest, transactional restore |
| I9 | Full integration audit | 9 | UI-10 | PENDING | `prompts/INTEGRATION_AUDIT_PROMPT.md` |

## Canonical domain naming (per `docs/contracts/DOMAIN_MODELS.md`)

Use these names verbatim — no aliases (`totalPrice`, `grandTotal`, `amountTotal` are forbidden
renames of `totalAmountMinor`).

- **Order**: id, platform (`Platform`), customPlatformName?, sellerName?, status (`OrderStatus`),
  paymentMethod (`PaymentMethod`), paymentStatus (`PaymentStatus`), isCod, currencyCode,
  itemSubtotalMinor, shippingFeeMinor, discountMinor, totalAmountMinor, orderedDate?,
  estimatedDeliveryStart?, estimatedDeliveryEnd?, deliveredAt?, courier?, trackingNumber?,
  notes?, productImageUri?, archivedAt?, createdAt, updatedAt.
  Invariants: totals non-negative; `isCod` consistent with payment semantics.
- **OrderItem**: id, orderId, name, variant?, quantity, unitPriceMinor?, lineTotalMinor?,
  imageUri?, notes?
- **Platform**: SHOPEE, TIKTOK_SHOP, LAZADA, TEMU, AMAZON, FACEBOOK, INSTAGRAM, OTHER
  (`OTHER` requires `customPlatformName`).
- **OrderStatus**: DRAFT, ORDERED, PROCESSING, PACKED, SHIPPED, OUT_FOR_DELIVERY, DELIVERED,
  CANCELLED, RETURNED, REFUNDED.
- **PaymentMethod**: COD, EWALLET, CARD, BANK_TRANSFER, CASH, OTHER, UNKNOWN.
- **PaymentStatus**: UNPAID, PARTIALLY_PAID, PAID, REFUNDED, UNKNOWN.
- **Category**: id, name, createdAt, updatedAt.
- **TrackingEvent**: id, orderId, status?, title, description?, occurredAt, source.
- **Reminder**: id, orderId?, type, scheduledAt, enabled, notificationId?, createdAt.
- **Receipt**: id, orderId, localUri, displayName?, mimeType?, createdAt.
- **Warranty**: id, orderId, title?, startDate?, endDate?, notes?, reminderEnabled.
- **ReturnRecord**: id, orderId, status, requestedAt?, deadline?, reason?, notes?.
- **RefundRecord**: id, orderId, amountMinor, currencyCode, status, requestedAt?, completedAt?, notes?.
- **OrderDraft**: transient normalized input for manual entry + Smart Import; never authoritative
  until validated and saved.
- **ExtractionField\<T\>**: value?, confidence 0.0–1.0, source OCR|LOCAL_PARSER|GROQ|USER,
  evidence?, needsReview.
- **SmartImportResult**: detectedPlatform?, draft, warnings, overallConfidence, usedGroq.
- **NotificationSuggestion**: sourcePackage, candidateOrderId?, proposedStatus?,
  proposedTrackingNumber?, confidence, explanation, requiresConfirmation.

## Canonical error taxonomy (per `docs/contracts/ERROR_VALIDATION_CONTRACT.md`)

ValidationError, NotFound, DatabaseFailure, FileAccessFailure, PermissionRequired, OcrFailure,
NetworkUnavailable, GroqFailure, RateLimited, InvalidAiResponse, BackupInvalid, UnknownFailure.

- Map implementation exceptions → these categories before the ViewModel layer.
- Validation rules centralized: name required, platform required, total valid, quantity ≥ 1,
  delivery end ≥ start, deliveredAt consistent with status, currency valid, refund amount within
  policy, warranty end ≥ start.
- User copy actionable and human; logs may hold technical context but never secrets or excess PII.

## I0 — Contract alignment (audit only)

**Objective:** reconcile existing code/docs with this pack before any feature expansion.

Actions:
- Inventory `app/src/main/java/com/ordra/` (current: `app/MainActivity.kt`, `OrdraApp.kt`,
  `OrdraApplication.kt`, `di/DataStoreModule.kt`, `core/ui/theme/*`, `feature/home/HomeScreen.kt`,
  empty `data/` + `domain/` packages).
- Produce conflict report: model names, Room schema, repositories, use cases, ViewModels/UiState,
  navigation, reminders, OCR/Groq, analytics, backup, errors.
- Repair only what I0 requires; preserve valid code; do NOT pre-build later features.
- Report: files inspected, conflicts found, files changed, build result, test result, blockers,
  `I0 READY: YES/NO`, then STOP.

Gate: report delivered; `docs/qa/CODEBASE_AUDIT.md` structure followed; build/tests actually run.

## I1 — Core vertical slice (orders)

**Objective:** Orders list, Add/Edit, Detail wired end-to-end per contracts.

Targets (create under `app/src/main/java/com/ordra/`):
- `domain/model/` — Order, OrderItem, enums, OrderDraft (names per canonical table).
- `data/db/` — `OrdraDatabase`, entities (orders, order_items, categories, order_categories,
  tracking_events — add later tables only when their phase arrives), DAOs (`observeOrder`,
  `observeOrders(filter/sort/query)`, `observeActiveOrders`, `observeArchivedOrders`,
  `observeUpcomingDeliveries`, `observeOutstandingCod`, transactional order-with-relations load),
  `data/mappers/` Entity↔Domain.
- `data/repository/OrderRepositoryImpl` implementing domain `OrderRepository`
  (observe/create/update/archive/restore/deletePermanently/updateStatus/items/aggregates).
- `domain/usecase/` — CreateOrderUseCase, UpdateOrderUseCase, ArchiveOrderUseCase,
  GetOrderDetailsUseCase, ObserveOrdersUseCase, UpdateOrderStatusUseCase.
- `feature/orders/`, `feature/orderdetail/`, `feature/addedit/` — ViewModels exposing immutable
  UiState + UiEvent per `docs/contracts/UI_STATE_EVENT_CONTRACT.md`; one-off effects
  (navigation/snackbar) non-replaying; pass stable IDs between destinations.
- Transactions for order+items writes; cascade rules defined for permanent delete (no orphans);
  archive preferred for UX.

Acceptance: create/edit/archive/restore/delete round-trip through Room; search/filter/sort
deterministic; validation errors mapped to user-safe messages; test-matrix rows 1–5 executed.

## I2 — Dashboard, COD & analytics

**Objective:** Room aggregate queries powering Home metrics and Insights.

Targets:
- DAO aggregate families: `observeUpcomingDeliveries`, `observeOutstandingCod`,
  `observeSpendingRange` + platform/category breakdowns.
- `domain/usecase/CalculateCodSummaryUseCase`, `GetHomeSummaryUseCase`, analytics use cases.
- `feature/home/HomeViewModel` replacing any placeholder with real Flow data; `feature/insights/`.
- Analytics rules: minor units; V1 filters/groups by currency instead of mixing; documented
  policy for cancelled/refunded inclusion; deterministic date boundaries; UI gets defined
  domain analytics models.

Acceptance: Home shows real active/arriving/COD values; Insights metrics match contract metric
list; test-matrix Analytics + COD rows executed.

## I3 — Reminders & calendar data

**Objective:** persisted reminders driving WorkManager + local notifications; calendar fed by
delivery dates.

Targets:
- `data/db` reminders table + DAO; `domain` Reminder model; `ReminderRepository`.
- `domain/usecase/ScheduleDeliveryReminderUseCase` (+ cancel/reschedule on status/ETA change).
- `data/work/` — reminder Worker; boot/time-change rescheduling per platform behavior; stable
  notification channels (delivery reminders; optional marketplace assistance later).
- Settings/defaults storage (DataStore) for reminder preferences.

Acceptance: schedule → worker → notification intent/data verified; reminders reproducible from
persisted data; test-matrix Reminder row executed.

## I4 — OCR Smart Import (local)

**Objective:** image → ML Kit → parser → OrderDraft → review → save, fully offline-capable.

Targets:
- `data/ocr/` — photo-picker image access/validation, ML Kit text recognition, text normalizer,
  platform detector, deterministic parser producing `OrderDraft` + `ExtractionField`s.
- `domain/usecase/AnalyzeScreenshotUseCase`, `ConfirmSmartImportUseCase`.
- `SmartImportRepository`/service boundary; duplicate candidate detection
  (platform/order/tracking/date/amount signals — never silently merge/discard).
- Failure modes handled: no text, unreadable image, unsupported layout, partial extraction,
  duplicate candidate.

Acceptance: scan → draft → review → save works offline; user-edited values never overwritten by
later AI/parser output; test-matrix OCR + Smart review rows executed.

## I5 — Groq enhancement (optional online)

**Objective:** optional Groq interpretation layered on the local pipeline.

Targets:
- `data/groq/` — Retrofit/OkHttp + kotlinx-serialization DTOs; structured-JSON-only requests;
  per-field validation; unknown enums must not crash; map DTO → domain draft.
- Key from git-ignored `secrets.properties` only; never logged; minimized/redacted text sent
  (prefer normalized extracted text over raw screenshot content).
- Field-merge precedence: user-confirmed > deterministic high-confidence > Groq > weak OCR.
- Offline/timeout/4xx/5xx/rate-limit/malformed-JSON → graceful fallback to local review.

Acceptance: Groq path adds value when available and disappears gracefully when not; no secret in
source/tests/logs; test-matrix Groq rows executed.

## I6 — Marketplace assistance

**Objective:** share-to-Ordra + optional NotificationListenerService → normalized suggestions.

Targets:
- Share-intent receivers routing into Smart Import flow.
- `data/notification/` — source-package filter, per-source adapters/parsers, matcher producing
  `NotificationSuggestion` with confidence + explanation.
- Confirmation flow → use case → repository; `TrackingEvent.source` recorded; no silent overwrite
  of trusted fields on weak matches; unrelated notification content never stored.

Acceptance: assistance fully optional; weak matches require confirmation; test-matrix
Notification-assist row executed.

## I7 — Advanced records

**Objective:** receipts, returns/refunds, warranties integrated with orders.

Targets:
- Tables + DAOs: receipts, warranties, returns, refunds (+ order links); repositories
  (ReceiptRepository, WarrantyRepository, ReturnRefundRepository); use cases
  (CreateReturnUseCase, etc.).
- Attachment storage policy: local URIs, privacy note, no auto-upload; cascade on permanent
  delete defined.
- Warranty/return deadlines → reminder integration where enabled.

Acceptance: relationships and amount validation per contract; test-matrix Return/refund +
Warranty rows executed.

## I8 — Backup, restore & export

**Objective:** versioned local backup + integrity-tested restore + CSV/JSON export.

Targets:
- `data/backup/` + `BackupRepository`: manifest {schemaVersion, appVersion, exportedAt, payload
  version} + full relational payload; ZIP incl. attachments if implemented; CSV export separate
  (human-readable, not full-fidelity).
- Restore pipeline: select → validate type → parse manifest → version check → preview summary →
  explicit conflict strategy (replace-all / merge-by-stable-ID / import-as-new) → transactional
  restore → integrity verification → result.
- `domain/usecase/RestoreBackupUseCase`. Never partially restore without recovery; never export
  Groq credentials or transient secrets.

Acceptance: round-trip verified incl. attachments; rollback proven on failure injection;
test-matrix Backup/restore row executed.

## I9 — Full integration audit

**Objective:** end-to-end verification before release QA.

Actions per `prompts/INTEGRATION_AUDIT_PROMPT.md`: boundaries, naming, relationships/
transactions/cascades/migrations, money units, entity leakage, direct-DAO calls, Flow/state,
nav/back-stack, reminders+WorkManager, OCR→draft→save, Groq fallback, secret hygiene,
notification safety, analytics rules, advanced records, backup round trip, validation/error
mapping, offline behavior, privacy/logging, TODO/dead/duplicate code, doc drift.

Output: executive status, blockers, findings by severity with file refs, executed tests/builds,
dependency-ordered repair plan, release-readiness recommendation. Repair before Phase 9/10 gate.

## Cross-track mapping

| I-phase | Master phase | docs/plan file(s) | UI megaplan |
|---|---|---|---|
| I0 | all | this file | UI-x (alignment) |
| I1 | 1A–1D | PHASE_01A/01B/01C/01D | UI-2/3/4 |
| I2 | 1C/1D/6 | PHASE_01C/01D/06 | UI-4/8 |
| I3 | 2 | PHASE_02_REMINDERS_CALENDAR | UI-5 |
| I4 | 3 | PHASE_03_SCREENSHOT_OCR | UI-6 |
| I5 | 4 | PHASE_04_GROQ_SMART_EXTRACTION | UI-6 |
| I6 | 5 | PHASE_05_PLATFORM_ASSISTANCE | UI-7 |
| I7 | 7 | PHASE_07_POST_PURCHASE | UI-8 |
| I8 | 8 | PHASE_08_BACKUP_EXPORT | UI-9 |
| I9 | 9 | PHASE_09_HARDENING_RELEASE | UI-10 |

## Test coverage obligations (per `docs/qa/INTEGRATION_TEST_MATRIX.md`)

Every phase must execute its matrix rows at the cheapest useful layer + critical end-to-end:
create/edit/archive/delete, search/filter/sort, COD math, reminder scheduling, OCR→draft,
Groq DTO/malformed/offline, review-wins-over-AI, notification confirm, analytics rules,
returns/warranty, backup round-trip + rollback, schema migration preserving data, navigation
IDs/back, dark/light rendering. No PASS claimed without executed evidence.

## Known conflicts & open decisions

- `docs/specs/DATA_MODEL.md` vs `docs/contracts/DOMAIN_MODELS.md`: contract names win
  (`totalAmountMinor`, canonical enums). Align specs doc wording during I0 if it diverges.
- `docs/specs/DATA_MODEL.md` allows N↔N Category "or simplify to one" — pick per
  `docs/contracts/DATABASE_CONTRACT.md` (order_categories join table) and keep the mapping consistent.
- Money: `docs/specs/DATA_MODEL.md` mentions "minor units or decimal-safe" — contract fixes
  integer minor units + ISO code; treat contract as canonical.
- Record owner decisions here when conflicts are resolved; do not silently rewrite contracts.
