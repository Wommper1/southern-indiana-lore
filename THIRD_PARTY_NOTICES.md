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

## Fonts (SIL Open Font License 1.1)

- **Cinzel**: Copyright 2020 The Cinzel Project Authors (https://github.com/NDISCOVER/Cinzel)
- **Plus Jakarta Sans**: Copyright 2020 The Plus Jakarta Sans Project Authors (https://github.com/tokotype/PlusJakartaSans)

Both fonts are licensed under the SIL Open Font License, Version 1.1. The license text is at https://openfontlicense.org/open-font-license-official-text/ and is also referenced inside each font file (https://scripts.sil.org/OFL).

## Dependencies

Libraries (AndroidX, Jetpack Compose, Room, Coil, Kotlin and others) are downloaded by Gradle at build time under their own licenses and are not part of this repository. Google Play services location is used under Google's terms.

## Lore pack content

The pack's historical facts come from public records and the cited sources listed in `sources.json`; those sources keep their own copyrights. CC BY-SA 4.0 applies to the pack's original text and compilation. Reuse of the pack requires attribution to Alex Shade Productions and sharing adaptations under the same license.
