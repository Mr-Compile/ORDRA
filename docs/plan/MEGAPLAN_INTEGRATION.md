# Ordra Integration & Data Contract Megaplan

Exhaustive execution plan for the **ORDRA Integration & Data Contract Pack v1**. One source of
truth for wiring Compose UI → ViewModel → Use Case → Repository → Room/services without drift.
Update the status table after each phase gate; a phase is DONE only when the contract checks and
`docs/qa/INTEGRATION_TEST_MATRIX.md` rows for its scope pass with real executed evidence.

## How to use this megaplan

1. Read the contracts in canonical order (`docs/contracts/README.md`):
   `docs/contracts/CONTRACT_BIBLE.md` → `docs/contracts/DOMAIN_MODELS.md` →
   `docs/contracts/DATABASE_CONTRACT.md` → `docs/contracts/TRACKING_CONTRACT.md` →
   `docs/contracts/REPOSITORY_USECASE_CONTRACT.md` →
   `docs/contracts/UI_STATE_EVENT_CONTRACT.md` → `docs/contracts/SMART_IMPORT_CONTRACT.md` →
   `docs/contracts/REMINDERS_NOTIFICATION_CONTRACT.md` → `docs/contracts/ANALYTICS_CONTRACT.md` →
   `docs/contracts/BACKUP_RESTORE_CONTRACT.md` → `docs/contracts/ERROR_VALIDATION_CONTRACT.md` →
   `docs/qa/INTEGRATION_TEST_MATRIX.md` → `docs/qa/TRACKING_QA.md`
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
- No courier/third-party tracking APIs, no scraping, no reverse-engineered endpoints —
  tracking is local Smart Parcel Status (`docs/contracts/TRACKING_CONTRACT.md`).
- A tracking number is an identifier/matching signal only; every detected shipment
  status carries `TrackingStatusSource` + `DetectionConfidence`; confidence decides
  auto-apply vs. confirm vs. suggest-only; manual status always exists.
- One canonical `docs/plan/` + `docs/qa/` structure — this file is the integration roadmap.

## Status key

DONE / IN PROGRESS / PENDING / DEFERRED / BLOCKED

## Phase status board

| I-phase | Scope | Master map | UI map | Status | Notes |
|---|---|---|---|---|---|
| I0 | Contract alignment audit | all | all | DONE | `docs/qa/INTEGRATION_AUDIT_EVIDENCE.md` — zero contract violations |
| I1 | Core order vertical slice | 1A–1D | UI-2/3/4 | DONE | Orders list/Add-Edit/Detail end-to-end (`a3c469a`,`d728a30`) |
| I2 | Dashboard/COD/analytics | 1C/1D/6 | UI-4/8 | DONE | Aggregate queries → Home + Analytics (`a85a64b`) |
| I3 | Reminders + calendar data | 2 | UI-5 | DONE | WorkManager + channels + ETA sync (`32284ff`) |
| I4 | OCR Smart Import | 3 | UI-6 | DONE | ML Kit → parser → draft → review → save (`eddcf8b`) |
| I5 | Groq enhancement | 4 | UI-6 | DONE | DTO-validated, redaction, offline fallback (`141b15e`) |
| I6 | Smart Parcel Status assistance | 5 | UI-7 | DONE | Share + listener + parser/matcher/confidence gates + review (`2b0adf8`) |
| I7 | Advanced records | 7 | UI-8 | DONE | Receipts/returns/refunds/warranties via Room v6 (`2883923`) |
| I8 | Backup/restore/export | 8 | UI-9 | DONE | Versioned manifest + transactional restore + CSV (`2069037`) |
| I9 | Full integration audit | 9 | UI-10 | DONE | PASS — `docs/qa/INTEGRATION_AUDIT_EVIDENCE.md` |

## Canonical domain naming (per `docs/contracts/DOMAIN_MODELS.md`)

Use these names verbatim — no aliases (`totalPrice`, `grandTotal`, `amountTotal` are forbidden
renames of `totalAmountMinor`).

- **Order**: id, platform (`Platform`), customPlatformName?, sellerName?, status (`OrderStatus`),
  paymentMethod (`PaymentMethod`), paymentStatus (`PaymentStatus`), isCod, currencyCode,
  itemSubtotalMinor, shippingFeeMinor, discountMinor, totalAmountMinor, orderedDate?,
  estimatedDeliveryStart?, estimatedDeliveryEnd?, deliveredAt?,
  notes?, productImageUri?, archivedAt?, createdAt, updatedAt.
  Invariants: totals non-negative; `isCod` consistent with payment semantics.
  Courier/tracking live on `Shipment`, not on Order.
- **OrderItem**: id, orderId, name, variant?, quantity, unitPriceMinor?, lineTotalMinor?,
  imageUri?, notes?
- **Shipment**: id, orderId, courier? (`Courier`), customCourierName?, trackingNumber?,
  shipmentStatus (`ShipmentStatus`), statusSource (`TrackingStatusSource`),
  statusConfidence (`DetectionConfidence`), lastStatusAt?, lastDetectedAt?,
  createdAt, updatedAt. Order 1→N Shipment.
- **Platform**: SHOPEE, TIKTOK_SHOP, LAZADA, TEMU, AMAZON, FACEBOOK, INSTAGRAM, OTHER
  (`OTHER` requires `customPlatformName`).
- **Courier**: SPX_EXPRESS, JNT_EXPRESS, NINJA_VAN, FLASH_EXPRESS, LBC, JRS_EXPRESS,
  GOGO_XPRESS, ENTREGO, TWO_GO, DHL, OTHER, UNKNOWN (`OTHER` requires `customCourierName`).
- **OrderStatus**: DRAFT, ORDERED, PROCESSING, PACKED, SHIPPED, DELIVERED,
  CANCELLED, RETURNED, REFUNDED. Parcel-movement states are NOT here.
- **ShipmentStatus**: UNKNOWN, PREPARING, PICKED_UP, IN_TRANSIT, ARRIVED_AT_HUB,
  OUT_FOR_DELIVERY, DELIVERED, DELIVERY_FAILED, RETURNING, RETURNED.
- **TrackingStatusSource**: MANUAL, MARKETPLACE_NOTIFICATION,
  COURIER_APP_NOTIFICATION, MESSAGE_NOTIFICATION, SCREENSHOT_OCR, SMART_IMPORT,
  SHARED_CONTENT, FUTURE_OFFICIAL_PROVIDER (extension point only).
- **DetectionConfidence**: LOW, MEDIUM, HIGH (internal numeric 0.0–1.0 allowed).
- **PaymentMethod**: COD, EWALLET, CARD, BANK_TRANSFER, CASH, OTHER, UNKNOWN.
- **PaymentStatus**: UNPAID, PARTIALLY_PAID, PAID, REFUNDED, UNKNOWN.
- **Category**: id, name, createdAt, updatedAt.
- **TrackingEvent**: id, shipmentId, shipmentStatus, title, description?, occurredAt,
  detectedAt, source (`TrackingStatusSource`), confidence (`DetectionConfidence`),
  rawStatus?, sourceDetail?, rawReference?. References `shipmentId`, never only
  `orderId`.
- **TrackingSuggestion**: id, detectionSource, candidateShipmentId?, candidateOrderId?,
  proposedShipmentStatus?, proposedTrackingNumber?, confidence, explanation,
  matchedSignals, requiresConfirmation. Transient — resolves to a TrackingEvent or nothing.
  (Supersedes the former order-level NotificationSuggestion.)
- **DetectionRecord** (Phase 5A): id, detectedAt, source, sourceDetail?, outcome
  (DETECTED|MATCHED|NEEDS_REVIEW|IGNORED|DUPLICATE|REJECTED|FAILED),
  candidateShipmentId?, proposedShipmentStatus?, confidence?, reason?. Structured
  history only — no raw notification bodies.
- **ExtractedSignals** (transient parser output): trackingNumber?,
  marketplaceOrderId?, courier?, marketplace?, codAmountMinor?, orderAmountMinor?,
  productHint?, rawStatusPhrase?, deliveryDateHint?, riderIndicator, locationHint?.
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
- `domain/model/` — Order, OrderItem, Shipment, TrackingEvent, enums (OrderStatus,
  ShipmentStatus, Courier, TrackingStatusSource, DetectionConfidence), OrderDraft
  (names per canonical table).
- `data/db/` — `OrdraDatabase`, entities (orders, order_items, shipments,
  tracking_events, categories, order_categories — add later tables only when their
  phase arrives), DAOs (`observeOrder`, `observeOrders(filter/sort/query)`,
  `observeActiveOrders`, `observeArchivedOrders`, `observeUpcomingDeliveries`,
  `observeOutstandingCod`, `observeShipments(orderId)`, `observeTrackingTimeline`,
  `findShipmentByTrackingNumber`, transactional order-with-relations load),
  `data/mappers/` Entity↔Domain.
- `data/repository/OrderRepositoryImpl` implementing domain `OrderRepository`
  (observe/create/update/archive/restore/deletePermanently/updateStatus/items/aggregates);
  `ShipmentRepositoryImpl` for observe/create/update/updateShipmentStatus/
  addTrackingEvent/timeline/findShipmentByTrackingNumber.
- `domain/usecase/` — CreateOrderUseCase, UpdateOrderUseCase, ArchiveOrderUseCase,
  GetOrderDetailsUseCase, ObserveOrdersUseCase, UpdateOrderStatusUseCase.
- `feature/orders/`, `feature/orderdetail/`, `feature/addedit/` — ViewModels exposing immutable
  UiState + UiEvent per `docs/contracts/UI_STATE_EVENT_CONTRACT.md`; one-off effects
  (navigation/snackbar) non-replaying; pass stable IDs between destinations.
- Transactions for order+items writes; cascade rules defined for permanent delete (no orphans);
  archive preferred for UX.

Acceptance: create/edit/archive/restore/delete round-trip through Room; shipments +
tracking events persist with order relations (Order 1→N Shipment 1→N TrackingEvent);
manual shipment-status update writes a `source = MANUAL` event; search/filter/sort
deterministic (incl. tracking number); validation errors mapped to user-safe messages;
test-matrix rows 1–5 + Shipment/Tracking rows executed.

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
  notification channels (delivery reminders; parcel-status updates + optional marketplace
  assistance later).
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

## I6 — Smart Parcel Status assistance

**Objective:** share-to-Ordra + optional NotificationListenerService → normalized,
confidence-gated shipment status updates per `docs/contracts/TRACKING_CONTRACT.md`
(Phase 5A–5C; 5D external providers stay an extension point).

Targets:
- Share-intent receivers routing into Smart Import flow / shipment detection.
- `data/notification/` — source registry (package → parser adapter): supported
  marketplace apps, courier apps, and messaging-app notifications carrying
  rider/delivery texts (Rider Message Detection; notifications only — never SMS
  inbox). Per-source parsers extract tracking number, order reference, courier,
  marketplace, COD/order amount, status text, product hints, rider indicators.
- Delivery-relevance + rider keyword dictionaries incl. mixed English/Filipino
  wording; unrelated notifications dropped before parsing and never stored.
- `StatusNormalizer` — raw platform/courier/rider wording → canonical
  `ShipmentStatus` (single dedicated layer; no scattered string matching).
- `ShipmentMatcher` + confidence engine producing `TrackingSuggestion` with
  confidence + matched signals; exact tracking-number match = strong confidence;
  signal tiers per contract; never silently fuzzy-match unrelated shipments;
  ambiguous multi-match → parcel-pick review, never arbitrary pick.
- Confidence gates: HIGH auto-applies only if user-enabled; MEDIUM shows
  Confirm/Ignore; LOW suggests only. Confirmation flow → use case →
  `ShipmentRepository` → `TrackingEvent` with source + confidence + provenance
  (`rawStatus`, `sourceDetail`) recorded.
- Optional ORDRA AI fallback for ambiguous delivery text — separately toggleable,
  minimized payload, deterministic-first; never every notification.
- Event deduplication (composite key) + status-transition/regression validation.
- Detection history (`DetectionRecord`: outcomes only, no raw bodies); Smart
  Detection settings incl. separate rider/SMS and AI toggles; permission and
  message-privacy explainers.
- Local status-change notification (dedupe per event); unrelated notification
  content never stored.
- "Track Externally" opens the official courier page only — never scraped.

Acceptance: assistance fully optional (revoke access → app unaffected); weak matches
require confirmation; ambiguous matches never auto-pick; invalid/stale transitions
rejected or confirmed (no regressing DELIVERED); test-matrix Notification-assist +
Tracking rows executed.

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
  version} + full relational payload — orders, items, shipments, tracking events (with source,
  confidence, timestamps) preserving Order → Shipment → TrackingEvent integrity; ZIP incl.
  attachments if implemented; CSV export separate (human-readable, not full-fidelity).
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
IDs/back, dark/light rendering. Tracking scope additionally: shipment creation, multiple
shipments per order, tracking-number matching (exact/ambiguous), status normalization,
notification parser, OCR status extraction, manual update, event/timeline ordering, duplicate
event prevention, confidence gates (auto-apply/confirm/reject), invalid transition, offline,
Track Externally, backup/restore of shipments, migration, local status-change notification —
checklist in `docs/qa/TRACKING_QA.md`. Notification-detection scope additionally:
source registry filtering (marketplace/courier/messaging), rider/Filipino-English
phrase parsing, ambiguous multi-match review, status regression protection,
dedupe on reposted notifications, AI-fallback payload minimization, detection
history privacy. No PASS claimed without executed evidence.

## Known conflicts & open decisions

- **Resolved (Smart Parcel Status update):** `Order.courier`/`Order.trackingNumber` and
  `OrderStatus.OUT_FOR_DELIVERY` from the pre-tracking schema are superseded — courier/tracking
  live on `Shipment`, parcel states on `ShipmentStatus`, `tracking_events.shipment_id`
  references the shipment. Existing DB v1 data migrates: each order's courier/tracking fields
  move into a default shipment row via an explicit versioned Migration (no destructive reset).
  Early code carrying the old fields stays valid until the shipments migration phase.
- `docs/specs/DATA_MODEL.md` vs `docs/contracts/DOMAIN_MODELS.md`: contract names win
  (`totalAmountMinor`, canonical enums). Align specs doc wording during I0 if it diverges.
- `docs/specs/DATA_MODEL.md` allows N↔N Category "or simplify to one" — pick per
  `docs/contracts/DATABASE_CONTRACT.md` (order_categories join table) and keep the mapping consistent.
- Money: `docs/specs/DATA_MODEL.md` mentions "minor units or decimal-safe" — contract fixes
  integer minor units + ISO code; treat contract as canonical.
- Record owner decisions here when conflicts are resolved; do not silently rewrite contracts.
