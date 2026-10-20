# Changelog

All notable changes are recorded here. The format follows [Keep a Changelog](https://keepachangelog.com/), and the
app follows [Semantic Versioning](https://semver.org/). The lore pack has its own version, in
`app/src/main/assets/lorepacks/southern_indiana/pack_manifest.json`.

## [Unreleased]

### Added

- Web atlas (`site/`): a free, offline-capable website version of the lore pack for people who cannot install the
  Android app, with the same trust badges and wording, a schematic SVG map with no tile servers, a foreground-only
  Near me mode, and no trackers. Not published yet; see `docs/WEB_ATLAS.md`.
- `scripts/build_site_data.py` (regenerates the web atlas data from the pack) and `scripts/serve_site.py` (local preview).
- `Web atlas CI` workflow: runs the web atlas tests and the data-drift check on pull requests that touch the atlas, the pack, or
  the badge definitions.

## [1.0.0] - 2026-10-18

First release. App version 1.0.0, lore pack 1.11.0.

### Added

- Offline-first Android app: a location-aware field companion for 52 southern Indiana history and folklore sites
  across 17 counties, with proximity alerts, narration, a field journal, an oral-history tape deck, and dossier
  and GPX export.
- Built-in route simulation (Tour tab, Desk & Tabletop Mode) so the app can be demonstrated without driving.
- Confidence badges on every site, from Official Source to Unverified and Folk Legend.
- `docs/VERIFICATION_LOG.md`: what has been fact-checked, every correction and its basis, and what is still
  unchecked.
- Guard tests that keep the public claims true: no network, camera or wake-lock permission, no cloud backup, valid
  pack hashes, and honest confidence labels.
- `scripts/update_pack_manifest.py` to regenerate the lore pack hashes after editing it.
- MIT license for the code and artwork, CC BY-SA 4.0 for the lore pack, and third-party notices.
- Contributing guide, issue and pull request templates, Dependabot, and Android lint in CI.

### Changed

- The app no longer declares `INTERNET`, `CAMERA`, or `WAKE_LOCK`, and Android backup and device transfer are off.
- The map draws entirely on the device; the earlier prototype's tile download is gone. The base map is a minimal
  schematic.
- The "Gemini Archivist" is now the Offline Archivist, described honestly as a keyword lookup over hand-written text.
- The photo attach buttons are labeled "Gallery" (there is no camera capture).
- The detail screen says "Documented archival history" only for entries that earned it; everything else reads
  "History (not fully verified)".
- The emergency fallback data always loads as Unverified, and a database entity no longer defaults to a verified
  label.

### Fixed

- Lore pack corrections from a fact-check of 46 of 52 entries, including Saint Meinrad's archabbey year, New Harmony's
  sale price, Medora Covered Bridge's cost, Beck's Mill's National Register year, the Constitution Elm's height,
  Tulip Trestle's rankings, the Hindostan Falls story, the Duncan Tunnel's length and history, the Freeman Field court-martial outcome, the Jasper depot (a 2005 replica, not a restored 1904 building), and three dollar
  amounts that had lost their leading characters. Entries whose claims were not supported are labeled Unverified; most checked entries are Plausible. See the
  verification log.
- The pack's line endings are pinned so its SHA-256 hashes hold on every checkout, including Windows clones.

### Removed

- 23 unused dependency entries (Firebase, Retrofit, Moshi, CameraX, MapLibre and others) and the unused Google
  Services plugin settings from the Gradle version catalog.

### Known issues

See "Known limitations" in the README. In short: device testing is still in progress, 6 of 52 entries are unchecked and most of the rest are only Plausible, the map is minimal, and there is no backup of your notes and recordings.
