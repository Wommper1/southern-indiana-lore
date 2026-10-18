# Google Play Store Publishing Guide

> **Status: deferred.** The app currently ships through GitHub Releases (a debug APK attached to each `v*` tag). Use this guide when you are ready for Google Play.

## Before the first Play upload

- [ ] **Application ID is permanent.** `com.aistudio.southernindianalore.qvmtzy` (see `app/build.gradle.kts`) cannot be changed after the first upload. Decide now whether to keep that random suffix.
- [ ] **Upload key and signed bundle.** Create an upload keystore, back it up outside the repo, set `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`, run `bundleRelease`, and test the release build on a real device. Release minification is currently off.
- [ ] **Privacy policy URL.** Required; none exists yet. The app collects nothing, but the policy still has to be hosted and linked.
- [ ] **Background location declaration.** `ACCESS_BACKGROUND_LOCATION` needs a Play Console declaration (and usually a demo video) and is the likeliest source of review delay. Shipping v1 with foreground-service tours only would avoid it.
- [ ] **Store graphics.** 512 px icon PNG, 1024x500 feature graphic, and phone screenshots (none exist yet).
- [ ] **Developer account requirements.** Check Google's current rules for new personal accounts, which have required a closed test with a minimum number of testers for a minimum period before production access.
- [ ] **Content rating questionnaire** and the Data Safety form (draft answers below and in `PLAY_STORE_LISTING.md`).

## Application Overview
* **App Name**: `Southern Indiana Lore` (21 characters — compliant with Google Play's 30-character limit)
* **Package / Application ID**: `com.aistudio.southernindianalore.qvmtzy`
* **Target Audience**: Historians, road trippers, educators, folklore enthusiasts, and Southern Indiana travelers.
* **Category**: Travel & Local / Education

---

## 1. Store Listing Metadata

### Short Description (65 / 80 characters; same text as `PLAY_STORE_LISTING.md`)
> Offline folklore, legends & historic maps across Southern Indiana

### Full Description
Explore the forgotten history, ghost towns, underground railroad sanctuaries, riverboat relics, and subterranean karst caverns of Southern Indiana.

**Southern Indiana Lore** turns your phone into an autonomous GPS field expedition guide and driving tour companion. Drive along the scenic Ohio River bluffs, the historic Buffalo Trace, and pioneer hill corridors while the app alerts you in real time to the hidden history beneath your tires.

#### Key Features:
* **Cockpit Car Driving HUD**:
  * Hands-free GPS proximity engine with automatic audio narration.
  * Real-time distance and ETA countdowns for upcoming subterranean crossings, abandoned rail cuts, and historic bridge spans.
  * One-tap turn-by-turn navigation links directly to Google Maps.

* **52 Sourced Sites**:
  * Curated across 17 Southern Indiana counties, centered on Floyd, Clark, Harrison, Lawrence, and Crawford.
  * Each site includes historical documentation, local folklore traditions where they exist, a confidence rating, and source citations.

* **Interactive Historical Cartography**:
  * On-device stylized map canvas (not surveyed street data) with Knobstone Escarpment elevation contours (600ft, 800ft, 950ft).
  * 1937 Great Ohio River Flood watermark overlays and Buffalo Trace historic trail corridors.
  * Sensor-fused real-time compass radar.

* **100% Offline-First Architecture**:
  * All 52 site dossiers, geofences, vector maps, and speech synthesis run completely offline without cellular reception in remote hollows and river bluffs.
  * Personal field research journal with voice dictation (via the system speech recognizer) and one-tap archival export.

* **Immersive Acoustic Synthesizer**:
  * Optional ambient binaural audio generator with automatic voice-ducking during historical audio narration.

---

## 2. Google Play Data Safety Declaration

| Data Type | Collected | Shared | Purpose |
| :--- | :--- | :--- | :--- |
| **Location (Precise & Approximate)** | Yes (Device Only) | **No** | App Functionality & Navigation. Used exclusively to calculate distances, compass bearings, and geofence alerts during driving tours. Never transmitted off device. |
| **Audio (Microphone)** | Yes (Device Only) | **No** | Optional voice memos recorded in the Oral History Tape Deck, stored in the app's private files until the user deletes them. Field-note dictation uses the system speech recognizer, not the app's microphone permission. |
| **Personal Info** | **No** | **No** | No user account, login, email, or personal identity required. |
| **Financial / Health Info** | **No** | **No** | None collected. |
| **Ads & Analytics** | **No** | **No** | Zero ads, zero commercial telemetry SDKs. |

* **Data Encryption in Transit**: N/A (the app declares no `INTERNET` permission; all user data and field notes remain on-device in the local SQLite Room database, and Android backup is disabled).
* **Data Deletion Mechanism**: Users can clear all journal notes, favorites, and visited logs directly in the Archive & Settings screen.

---

## 3. Permissions Justification (For Play Console Declaration)

* `ACCESS_FINE_LOCATION` & `ACCESS_COARSE_LOCATION`: Required to position the user on the historical vector map and trigger proximity alerts.
* `ACCESS_BACKGROUND_LOCATION`: Required for the driving tour feature so alerts sound while the phone screen is turned off on the dashboard.
* `FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_LOCATION`: Required to keep the driving tour audio engine active while navigating.
* `POST_NOTIFICATIONS`: Displays an ongoing notification during an active driving tour with immediate controls to pause/stop tracking.
* `RECORD_AUDIO`: Optional voice memo recording in the Oral History Tape Deck, only when the user presses Record.
* `VIBRATE`: Haptic feedback for proximity alerts and controls.
* Not declared: `INTERNET`, `CAMERA`, `WAKE_LOCK`.

---

## 4. Release Artifacts

* **Production Bundle (AAB)**:
  `app/build/outputs/bundle/release/app-release.aab`
* **Universal Release APK**:
  `app/build/outputs/apk/release/app-release.apk`
* **Target SDK**: Android 14+ (API 34/36)
* **Minimum SDK**: Android 7.0 (API 24)

---

## 5. Testing & Verification Checklist
- [x] All 52 pack files verified against the pack manifest SHA-256 digests.
- [x] Room SQLite database migration and local persistence verified.
- [x] Subterranean and Bridge crossing countdown engine verified.
- [x] Unit test suite passing 100% on local JVM (`HistoricalResearchIntegrationTest`, `ProximityEngineHardeningTest`).
- [x] Adaptive launcher icon generated and configured in `mipmap-anydpi-v26`.
