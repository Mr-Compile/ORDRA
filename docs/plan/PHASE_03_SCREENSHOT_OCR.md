# Phase 3 — Screenshot Smart Import (Local)

## Goal
Turn screenshots into editable OrderDrafts without making cloud AI mandatory.

## Pipeline
Image Picker/Share → privacy-aware preprocessing → ML Kit Text Recognition → deterministic parser → OrderDraft → confidence per field → Review → existing save pipeline.

## Implement
- Android Photo Picker for screenshots.
- On-device OCR with ML Kit.
- Platform hints for Shopee, TikTok Shop, Lazada and generic screenshots.
- Extract candidate product names, quantities, amounts, seller, status, tracking and ETA when visible.
- Field-level confidence and source text evidence internally.
- Duplicate hints using tracking/order identifiers and fuzzy order metadata.
- Multi-screenshot queue can be added only after single screenshot flow is stable.

## Rules
Never silently save OCR output. Never assume OCR text is correct. Screenshot import must degrade to manual editing.

## Exit criteria
[ ] User can correct every detected field.
[ ] Failed OCR does not lose the screenshot/draft unexpectedly.
[ ] No screenshot is uploaded unless an online feature explicitly requires it and the user is informed.
