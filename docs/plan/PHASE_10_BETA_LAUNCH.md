# Phase 10 — Beta, Launch & Maintenance

## Implement
- Internal testing → closed beta → staged production rollout.
- Test real devices and multiple Android versions.
- Collect bug/UX feedback without changing scope mid-release.
- Store listing assets, screenshots, descriptions and support/privacy links.
- Monitor crashes/ANRs and rollback/block problematic releases when needed.
- Establish patch/minor/major version policy and changelog.

## Post-launch backlog (OUT OF CURRENT SCOPE / FUTURE CONSIDERATION)
Only after real usage and explicit owner approval: multi-device/cloud sync, official courier `TrackingProvider` integrations where legally/technically supported (extension point per TRACKING_CONTRACT — never scraped or unofficial APIs), improved parsers, widgets, and other enhancements justified by users. Before any public distribution, the Groq API-key architecture must be reassessed — secrets embedded in a distributed APK are not secure.
