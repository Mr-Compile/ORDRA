# Smart Parcel Status QA Checklist

Tracking-specific gate per `docs/contracts/TRACKING_CONTRACT.md`. Mark each item
PASS / FAIL / BLOCKED with executed evidence. No PASS without running the check.

## Data & relationships
- [ ] Shipment creation (courier, tracking number, status, source, confidence, timestamps)
- [ ] Multiple shipments per order supported (Order 1→N Shipment)
- [ ] Tracking events reference `shipmentId` and belong to Shipment 1→N TrackingEvent
- [ ] Tracking event creation writes correct status/source/confidence/occurredAt/detectedAt
- [ ] Timeline ordering deterministic (occurredAt ordering, tie-broken deterministically)
- [ ] Duplicate event prevention (same detected transition cannot duplicate)
- [ ] Room migration preserves orders, shipments, events (no destructive reset)

## Detection & normalization
- [ ] Tracking-number matching: exact match yields strong confidence
- [ ] Ambiguous match behavior: weak/fuzzy signals never silently update an unrelated order; multi-candidate matches route to parcel-pick review (e.g. three parcels at COD ₱499 + "Delivery today ₱499" → review, never arbitrary)
- [ ] Status normalization: varied platform wording maps to canonical ShipmentStatus
- [ ] Unrecognized status text maps to UNKNOWN, not a wrong status
- [ ] Notification parser extracts tracking number/courier/status for supported sources
- [ ] Source registry covers marketplace + courier + messaging apps; unsupported notification source packages ignored and never stored
- [ ] Rider/message detection: Filipino + English patterns parsed ("Rider po ito", "May parcel po kayo today", "COD po ₱849", "Galing Shopee po") as candidate signals, still requiring match confidence
- [ ] Real-world fixtures parse correctly (J&T "TN: 635007688159 … COD 0.00", SPX Filipino "Pakihanda ang iyong bayad na 5.00 PHP" → SPX + ₱5.00 + out-for-delivery)
- [ ] Sender identity is not a match signal: unknown rider numbers still match on parcel signals
- [ ] Signal extraction covers: tracking no., order ID, courier, marketplace, COD/order amount, product hint, status phrase, delivery date, rider indicator, location
- [ ] OCR status extraction from marketplace/tracking screenshots
- [ ] Shared-content imports route through the same normalizer + matching pipeline

## Confidence gates & updates
- [ ] High-confidence automatic update applies ONLY when user-enabled
- [ ] Medium-confidence detection requires Confirm/Ignore
- [ ] Low-confidence detection is rejected (suggestion only, no mutation)
- [ ] Weak signals alone (price-only / marketplace-only / courier-only) never auto-update
- [ ] Invalid status transition rejected or escalated for confirmation
- [ ] Status regression protection: stale IN_TRANSIT detection cannot move DELIVERED backward
- [ ] Many-parcel matching: 10+ active shipments narrow by tracking → courier → marketplace → order ID → COD → hint → timing; never "most recent order"
- [ ] Manual status update always works and records source = MANUAL
- [ ] Applied update creates TrackingEvent + optional local notification (no duplicates)
- [ ] Event dedupe: reposted/updated notifications do not duplicate events (composite key, not text equality)

## UX & labeling
- [ ] Status surfaces show detection source + freshness; no "live"/"real-time" claims
- [ ] Source labels user-friendly ("Detected from courier notification", "Detected from delivery message"); no technical terms (NotificationListenerService, Groq, regex)
- [ ] Status Suggestion review UI shows matched order, proposed status, confidence
- [ ] Ambiguous-match review: "Possible Parcel Update" with candidate parcels + Select Parcel / Ignore
- [ ] Rider/message detection: dedicated privacy explainer shown before its toggle can enable
- [ ] Detection history shows outcomes (Detected/Matched/Needs Review/Ignored/Duplicate/Rejected/Failed) without raw content
- [ ] Track Externally opens official courier page; result is never scraped
- [ ] Settings toggles: marketplace notifications, courier notifications, rider/SMS detection (separate), automatic high-confidence updates, ask-before-ambiguous, AI-assisted detection (separate), status-change notifications, per-source controls

## Offline & degradation
- [ ] All tracking features work offline (OCR, manual, timelines, deterministic notification parsing + matching + Room writes)
- [ ] Groq unavailable: deterministic notification detection unaffected; AI leg skipped, not blocking
- [ ] Notification access revoked: app unaffected, manual path intact
- [ ] Notification permission disabled at install: no broken states
- [ ] Messaging notification hidden/text unavailable: skipped safely, no error surface

## Privacy & backup
- [ ] No raw notification bodies or full OCR text persisted (parsed fields + minimal provenance/rawReference only)
- [ ] SMS inbox never read: messaging signals arrive only via notifications
- [ ] AI fallback payload minimized: only the candidate notification text needed for extraction — no history, contacts, addresses, or other notifications
- [ ] Non-delivery notifications dropped before parsing; never stored
- [ ] Production logs free of notification/message contents
- [ ] Backup/restore includes shipments + tracking events with source/confidence/timestamps and relationship integrity
- [ ] Multiple parcels for one order round-trip through backup
- [ ] Local status-change notifications dedupe per TrackingEvent
- [ ] No unrelated notification content stored
