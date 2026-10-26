# Screen Acceptance Checklist

For every screen verify: route reachable; correct title/actions; state survives expected recreation; back behavior; empty state; error state; dark/light; font scaling; touch targets; no clipped text; keyboard/IME behavior; loading does not block forever; destructive actions confirmed/undoable; user-facing wording is nontechnical; screenshot tests/previews where feasible.

Critical flows:
1. First launch -> onboarding -> Home -> Add Order -> Save -> Detail -> Home.
2. Orders -> Search/Filter -> Detail -> Edit -> Save -> return.
3. Home -> mark delivered -> history.
4. Scan screenshot -> OCR -> optional Groq -> review -> correct -> save -> detail.
5. Offline scan -> OCR/manual review -> save.
6. Calendar -> day -> order detail.
7. Order -> reminder -> local notification configuration.
8. Settings -> theme -> immediate visual update.
9. Backup -> create -> restore validation.
10. Notification suggestion -> confirm/ignore without silent mutation.
