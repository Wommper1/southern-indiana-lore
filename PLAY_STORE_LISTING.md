# Google Play Store Release Kit: Southern Indiana Lore

> **Status: deferred.** The app currently ships through GitHub Releases. This kit is for a future Google Play release. Before using it, see "Before the first Play upload" in [docs/GOOGLE_PLAY_PUBLISHING_GUIDE.md](docs/GOOGLE_PLAY_PUBLISHING_GUIDE.md): a privacy policy URL, the background-location declaration, store graphics, and a signed bundle are still missing.

This document contains draft metadata, store copy, graphical asset specifications, and Google Play Console declarations for **Southern Indiana Lore**.

---

## 1. Store Listing Metadata

### App Title (Max 30 characters)
```text
Southern Indiana Lore
```
*(Character count: 21 / 30)*

### Short Description (Max 80 characters)
```text
Offline folklore, legends & historic maps across Southern Indiana
```
*(Character count: 65 / 80)*

### Full Description (Max 4,000 characters)
```text
Step off the highway and into the forgotten legends of Floyd County, the Knobstone Escarpment, and the Southern Indiana hill country.

Southern Indiana Lore is an offline-first geospatial field companion for history fans, folklore hunters, and road-trippers exploring the historic Ohio River Valley. Built with reverence for local memory, archival research, and architectural history, this app transforms your device into an antiquarian researcher's field deck.

🧭 HISTORIC EXPLORATION & GEOFENCING
• 52 sourced sites, from landmarks and rail tunnels to legends and limestone ruins, across 17 southern Indiana counties, centered on Floyd, Clark, Harrison, Lawrence, and Crawford.
• Intelligent multi-tier geofencing alerts you as you approach historical POIs while driving or hiking.
• 1800s vintage cartographic overlays and topographic survey styles.

🎙️ ORAL HISTORY FIELD TAPE DECK
• Record on-device audio interviews with local informants, elders, and eyewitnesses using a vintage analog reel-to-reel interface.
• Real-time decibel VU meter and live waveform visualizer.
• Tag voice memos directly to historical landmarks, complete with GPS coordinates and field notes.

📖 EXPEDITION DOSSIER & GPX EXPORT
• Generate structured Markdown (.md) field research logbooks detailing every encountered site.
• Export standard GPX 1.1 waypoints for Garmin GPS units, OsmAnd, QGIS, and Google Earth.

🔍 EPISTEMIC CONFIDENCE & ARCHIVAL RIGOR
• Every entry is classified by evidence tier: Official Primary Records, Verified Local Histories, Oral Traditions, and Unverified Folklore.
• Examine visible aboveground remains, what lies underground, and historical timeline slices from 1800 to present day.

🔒 PRIVACY & ZERO TELEMETRY COMMITMENT
• 100% Offline-First: All maps, audio narrations, and database records are stored locally on your device.
• Zero tracking, zero third-party telemetry, zero account sign-up required.
• Audio memos and GPS locations never leave your device.

Developed by Alex Shade Productions.
```

---

## 2. Graphical Asset Specifications

| Asset | Dimensions | Format | Notes |
| :--- | :--- | :--- | :--- |
| **App Icon** | 512 x 512 px | 32-bit PNG (with alpha) | No rounded corners in source file (Google Play applies mask). |
| **Feature Graphic** | 1024 x 500 px | 24-bit JPEG or PNG | Landscape orientation, parchment/limestone aesthetic, no text in edges. |
| **Phone Screenshots** | 1080 x 2400 px (9:16) | PNG / JPEG | Minimum 4 screenshots (Explorer Map, Site Detail, Field Journal, Tape Deck). |
| **Tablet Screenshots** | 1600 x 2560 px (16:10) | PNG / JPEG | Optional for 7-inch and 10-inch form factors. |

---

## 3. Store Categorization & Tags

- **Primary Category**: Travel & Local
- **Secondary Category**: Books & Reference
- **Tags**: History, Travel, Tourism, Hiking, Local Legends, Audio Guides, Navigation

---

## 4. Content Rating & Policy Questionnaire

- **Target Audience**: 13+ (Teen)
- **Violence**: Mild fantasy / supernatural references (ghost stories, train wrecks, 19th-century frontier skirmishes). No graphic gore.
- **Fear / Horror**: Spooky historical folklore and ghost legend narratives.
- **Location Tracking**: Background and foreground location used solely for active on-device geofence alerts when driving or walking. No server transmission.
- **Microphone**: Used exclusively on-demand when the user presses the Record button inside the Oral History Field Deck. Stored strictly in local app sandbox.

---

## 5. Google Play Data Safety Form Answers

| Data Type | Collected? | Shared? | Purpose | Ephemeral? |
| :--- | :--- | :--- | :--- | :--- |
| **Approximate & Precise Location** | Yes (On-device only) | **No** | App functionality (Geofence proximity radar & distance calculation) | Yes (processed in memory, not transmitted) |
| **Audio Recordings** | Yes (User-initiated only) | **No** | App functionality (Oral history voice memo creation) | No (stored in private app files directory until user deletes) |
| **Personal Identifiers** | **No** | **No** | N/A | N/A |
| **Analytics / Diagnostics** | **No** | **No** | N/A | N/A |

Notes for the form: the app declares no `INTERNET` permission and makes no network calls; backups are disabled; the camera is not used. Field-note voice dictation and spoken narration use the phone's system speech services, which are outside the app's control.

---

## 6. Build & Packaging Commands

### Generate Play Store AAB Bundle
```bash
gradle :app:bundleRelease
```
The output bundle will be located at:
`app/build/outputs/bundle/release/app-release.aab`

### Test Sideloadable APK
```bash
gradle :app:assembleDebug
```
Output:
`app/build/outputs/apk/debug/app-debug.apk`
