# Smart Import QA

Test clear/blurred/cropped screenshots, multiple currencies/amounts, discounts, multi-item orders, missing ETA, missing tracking, tracking-page screenshots (courier/tracking/status/events extraction), unrelated images, duplicated screenshots and unsupported platforms.

Verify OCR/AI never auto-saves, confidence is not presented as certainty, malformed AI JSON is rejected, deterministic totals are recalculated, offline behavior works, detected shipment statuses normalize to ShipmentStatus with source + confidence, shipment detections on existing orders produce reviewable TrackingSuggestions (never silent updates), and sensitive screenshot content is not logged.
