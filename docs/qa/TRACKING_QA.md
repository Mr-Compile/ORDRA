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
- [ ] Ambiguous match behavior: weak/fuzzy signals never silently update an unrelated order
- [ ] Status normalization: varied platform wording maps to canonical ShipmentStatus
- [ ] Unrecognized status text maps to UNKNOWN, not a wrong status
- [ ] Notification parser extracts tracking number/courier/status for supported sources
- [ ] Unsupported notification source packages ignored and never stored
- [ ] OCR status extraction from marketplace/tracking screenshots
- [ ] Shared-content imports route through the same normalizer + matching pipeline

## Confidence gates & updates
- [ ] High-confidence automatic update applies ONLY when user-enabled
- [ ] Medium-confidence detection requires Confirm/Ignore
- [ ] Low-confidence detection is rejected (suggestion only, no mutation)
- [ ] Invalid status transition rejected or escalated for confirmation
- [ ] Manual status update always works and records source = MANUAL
- [ ] Applied update creates TrackingEvent + optional local notification (no duplicates)

## UX & labeling
- [ ] Status surfaces show detection source + freshness; no "live"/"real-time" claims
- [ ] Status Suggestion review UI shows matched order, proposed status, confidence
- [ ] Track Externally opens official courier page; result is never scraped
- [ ] Settings toggles: marketplace detection, auto high-confidence updates,
      status-change notifications, per-marketplace controls

## Offline & degradation
- [ ] All tracking features work offline (OCR, manual, timelines, notifications queued locally)
- [ ] Groq unavailable: OCR/status extraction unaffected (Groq is optional enhancement only)
- [ ] Notification access revoked: app unaffected, manual path intact
- [ ] Notification permission disabled at install: no broken states

## Privacy & backup
- [ ] No raw notification bodies or full OCR text persisted (parsed fields + minimal rawReference only)
- [ ] Backup/restore includes shipments + tracking events with source/confidence/timestamps and relationship integrity
- [ ] Multiple parcels for one order round-trip through backup
- [ ] Local status-change notifications dedupe per TrackingEvent
- [ ] No unrelated notification content stored
