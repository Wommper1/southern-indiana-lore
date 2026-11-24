# Licensing Scope and Third-Party Notices

The repository's [LICENSE](LICENSE) (MIT) covers the **source code, build files, and the app's own artwork**. Some things in the repository are deliberately under different terms:

| What | Where | License |
| :--- | :--- | :--- |
| Source code, build scripts, docs, and the app's own artwork (hero image, app icon, intro video) | everything not listed below | MIT, see [LICENSE](LICENSE) |
| Lore pack (site stories, geofences, sources) | `app/src/main/assets/lorepacks/` | CC BY-SA 4.0, as declared in [`licenses.json`](app/src/main/assets/lorepacks/southern_indiana/licenses.json) |
| Fonts: Cinzel, Plus Jakarta Sans | `app/src/main/res/font/` | SIL Open Font License 1.1 (see below) |
| Web atlas code and icons (`site/`, `scripts/build_site_data.py`, `tests/site/`) | `site/` | MIT, see [LICENSE](LICENSE); the icons are cropped from the app icon artwork |
| Web atlas data (a generated copy of the lore pack) | `site/data/atlas.json` | CC BY-SA 4.0, like the pack it is built from |
| Web atlas fonts (copies of the two fonts above) | `site/fonts/` | SIL Open Font License 1.1, license text in `site/fonts/OFL.txt` |
| Web atlas tours | `site/data/tours.json` | CC BY-SA 4.0, like the lore pack |
| Web atlas map libraries (MapLibre GL JS 6.17.0, PMTiles 4.5.0, Protomaps basemaps 5.7.2) | `site/vendor/maplibre-gl/`, `site/vendor/pmtiles/`, `site/vendor/protomaps-basemaps/` | BSD-3-Clause; MapLibre's license text is in `site/vendor/maplibre-gl/LICENSE.txt` |
| fflate 0.8.2, qrcode-generator 2.0.4 | `site/vendor/fflate/`, `site/vendor/qrcode-generator/` | MIT; fflate's license is in `site/vendor/fflate/LICENSE` |
| Basemap label fonts (Noto Sans glyphs) and map icons | `site/vendor/basemap-assets/` | Fonts: SIL Open Font License 1.1 (`fonts/OFL.txt`). Icons: derived from MIT-licensed tangrams/icons |
| Street map data (built at deploy time, not committed) | `site/map/` on the published site | © OpenStreetMap contributors, Open Database License (ODbL) 1.0; packaged by Protomaps |

## Fonts (SIL Open Font License 1.1)

- **Cinzel**: Copyright 2020 The Cinzel Project Authors (https://github.com/NDISCOVER/Cinzel)
- **Plus Jakarta Sans**: Copyright 2020 The Plus Jakarta Sans Project Authors (https://github.com/tokotype/PlusJakartaSans)

Both fonts are licensed under the SIL Open Font License, Version 1.1. The license text is at https://openfontlicense.org/open-font-license-official-text/ and is also referenced inside each font file (https://scripts.sil.org/OFL).

## Web atlas libraries and map data

The web atlas copies its third-party libraries into `site/vendor/` with `scripts/vendor_web_libs.sh`, which pins each
version and checks npm's published integrity hash; `site/vendor/VENDORED.md` lists every file's SHA-256 and a test
checks them. PMTiles and Protomaps basemaps declare BSD-3-Clause in their package metadata (their npm packages ship no
separate license file); their authors are listed as Brandon Liu (PMTiles) and The Protomaps Authors (basemaps). MapLibre
GL JS: Copyright (c) 2023, MapLibre contributors, BSD-3-Clause, plus the Mapbox and other notices in its `LICENSE.txt`.
qrcode-generator: Copyright (c) 2009 Kazuhiko Arase, MIT.

The street map is cut from Protomaps' daily build of OpenStreetMap by `scripts/build_basemap.sh` during deployment.
OpenStreetMap data is © OpenStreetMap contributors and available under the Open Database License
(https://www.openstreetmap.org/copyright); the map shows this attribution in its corner and on the About page.

## Dependencies

Libraries (AndroidX, Jetpack Compose, Room, Coil, Kotlin and others) are downloaded by Gradle at build time under their own licenses and are not part of this repository. Google Play services location is used under Google's terms.

## Lore pack content

The pack's historical facts come from public records and the cited sources listed in `sources.json`; those sources keep their own copyrights. CC BY-SA 4.0 applies to the pack's original text and compilation. Reuse of the pack requires attribution to Alex Shade Productions and sharing adaptations under the same license.
