# Southern Indiana Lore

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-blue.svg)](https://developer.android.com/jetpack/compose)
[![Database](https://img.shields.io/badge/Database-Room_SQLite-orange.svg)](https://developer.android.com/training/data-storage/room)
[![License](https://img.shields.io/badge/License-MIT%20code%20%2F%20CC%20BY--SA%204.0%20content-blue.svg)](#license)

A location-aware, offline-first native Android application by **Alex Shade Productions**. 

Southern Indiana Lore brings folklore, haunted legends, Underground Railroad routes, Monon rail tunnels, early Indiana statehood milestones, and forgotten Ohio River riverboat disasters to life. The bundled pack covers 52 sites across 17 southern Indiana counties, centered on Floyd, Clark, Harrison, Lawrence, and Crawford. About half have been fact-checked so far; the rest are labeled Unverified in the app (see [docs/VERIFICATION_LOG.md](docs/VERIFICATION_LOG.md)). As you drive or walk, the background GPS engine detects proximity to historical points of interest and triggers narration and archival accounts.

---

## Key Features

- **Spatial Indexing & Multi-Tier Proximity Geofencing**:
  - Uses bounding-box spatial indexing in Room SQLite to evaluate candidate POIs with low CPU and battery overhead.
  - Multi-tier alert stages: *Horizon Approaching* (500m), *Immediate Lore Trigger* (150m), and *Inside Site Geofence*.
- **Background Tour Service (`LoreTourForegroundService`)**:
  - Continuous location monitoring in the background while driving or walking with device in pocket.
  - Real-time notification updates with site distance, bearing, and immediate narration trigger actions.
- **Dynamic Multi-Persona Narration Engine**:
  - **Chief Historical Archivist**: Scholarly, verified historical analysis.
  - **Folklore Sage**: Fireside storyteller of Appalachian and Ohio Valley legends.
  - **Adventure Chronicler**: Resonant frontier documentary dispatch style.
  - **Karst Alchemist & Lore Wizard**: Arcane, geological perspective of Indiana limestone caves.
  - **Retro Radio (1948)**: Nostalgic, vintage Mid-Century AM broadcast style.
- **Offline-First SQLite Architecture**:
  - Bundled lore pack of Southern Indiana historical sites with coordinates, per-site source citations, and a confidence badge on every entry that says how thoroughly it has been verified (Official Source, Plausible, Unverified, Folk Legend, and so on).
  - User Field Journal: add personal field notes, coordinates, and custom sites offline.
- **On-Device Map**:
  - Pan/zoom map drawn entirely on the device, with your position, site markers, and historical overlays on top. The base layer is deliberately minimal: a dark schematic with the Ohio River, Silver Creek, a Knobstone ridgeline, and one historic road/rail corridor. It has no street data, place names, or hill shading, so use the "Navigate" buttons for directions.
- **Oral History Tape Deck**:
  - Record voice memos on the device and tag them to sites. Recording starts only when you press Record.
- **Field Expedition Dossier & GPX 1.1 Exporter**:
  - Export collected discoveries, field notes, and encounters into formatted Markdown (`.md`) dossiers.
  - Generate standard GPX 1.1 XML waypoints for Garmin GPS units, OsmAnd, QGIS, and Google Earth.
- **Offline Archivist**:
  - Ask about a featured site or request a driving-tour route. This is a keyword lookup over hand-written entries, not an AI model: well-known sites get a curated answer, and everything else gets a general overview.
- **Automated GitHub Actions CI/CD**:
  - Continuous integration running automated test suites on JDK 17.
  - Version tags (`v*`) build a debug APK and attach it to a GitHub Release for sideloading.

---

## Security & Privacy Architecture

| Security Vector | Implementation Detail |
| :--- | :--- |
| **Zero External Secrets** | No API keys, tokens, or credentials are used or compiled into the app. Signing keys are never committed (`*.jks` / `*.keystore` are git-ignored). |
| **No Network Access** | The app declares no `INTERNET` permission. Map tiles, narration audio, and sound are generated on-device. "Navigate" buttons and source links hand off to Google Maps or your browser. |
| **Permission Least-Privilege** | Location (fine, coarse, background), notifications, foreground service (location), vibrate, and microphone. The microphone is used only for the Oral History Tape Deck; the camera is not used. |
| **Android Component Isolation** | Foreground Service and Broadcast Receiver are non-exported (`android:exported="false"`). |
| **Data Privacy** | No ad networks, telemetry, or analytics SDKs. Android backup and device-transfer are disabled, so journal entries, custom sites, and voice memos stay on the device. |

One caveat: field-note voice dictation (`RecognizerIntent`) and spoken narration (Android text-to-speech) use your phone's system speech services. Depending on your device and settings, those services may use the network. The app itself makes no network calls.

---

## Project Structure

```
southern-indiana-lore/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── data/
│   │   │   │   │   ├── archivist/        # Offline keyword-lookup Archivist
│   │   │   │   │   ├── local/            # Room Database, DAOs, Entities, Seed data
│   │   │   │   │   ├── model/            # LoreSite, HistoricalArchive domain models
│   │   │   │   │   ├── pack/             # Lore pack validation & integrity (SHA-256)
│   │   │   │   │   └── repository/       # LoreRepository data layer
│   │   │   │   ├── service/              # LoreTourForegroundService, SpatialProximityCoordinator
│   │   │   │   ├── ui/                   # Jetpack Compose Screens, Dialogs, Theme
│   │   │   │   └── util/                 # Audio engine, sound synthesis, TTS, notifications
│   │   │   ├── assets/lorepacks/         # Bundled lore pack, geofences, schema, licenses
│   │   │   ├── res/                      # Android resources (drawables, fonts, values)
│   │   │   └── AndroidManifest.xml       # Least-privilege manifest declaration
│   └── build.gradle.kts                  # App build & dependency specifications
├── docs/                                 # Design notes, map architecture, publishing guide
├── .github/workflows/android-ci.yml      # CI: tests + debug APK
├── .gitignore                            # Ignore rules (keystores, build output, local config)
├── README.md                             # Repository documentation
└── settings.gradle.kts                   # Project configuration
```

---

## Getting Started

### Prerequisites

- **Android Studio**: a current stable release (the project uses Android Gradle Plugin 9.1.1)
- **JDK**: Java 17+
- **Android SDK**: Compile SDK 36, Minimum SDK 24 (Android 7.0+)

### Building Locally

1. **Clone the repository**:
   ```bash
   git clone https://github.com/Wommper1/southern-indiana-lore.git
   cd southern-indiana-lore
   ```

2. **Zero Configuration Required**:
   No API keys, cloud setup, or network access are needed.

3. **Build the Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run Unit & Robolectric Tests**:
   ```bash
   ./gradlew testDebugUnitTest
   ```

### Installing a Build

Debug APKs for tagged versions are attached to the repository's GitHub Releases. To sideload one, allow installs from your browser or file manager in Android settings, then open the APK. Debug builds use the application ID suffix `.debug`, so they install alongside any other copy of the app.

---

## Authors & Attribution

Created by **Alex Shade Productions**.  
Historical documentation referenced from Library of Congress HABS records, the Indiana Historical Bureau, and local Southern Indiana oral history archives.

---

## License

- **Source code and the app's artwork** (hero image, app icon, intro video): MIT, see [LICENSE](LICENSE).
- **Lore pack** (`app/src/main/assets/lorepacks/`): CC BY-SA 4.0, as declared in [`licenses.json`](app/src/main/assets/lorepacks/southern_indiana/licenses.json).
- **Bundled fonts** (Cinzel, Plus Jakarta Sans) are under the SIL Open Font License. See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
