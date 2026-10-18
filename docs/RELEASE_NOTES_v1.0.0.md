# Southern Indiana Lore v1.0.0

First release of an offline-first Android field companion for southern Indiana history and folklore: 52 sites across
17 counties, with proximity alerts, narration, a field journal, an oral-history tape deck, and dossier and GPX
export. No account, no ads, no tracking, and no internet permission.

> **Early release.** The automated tests pass, but device testing is still in progress, and about half of the
> history entries have not been fact-checked yet (they are badged **Unverified** in the app). Please read
> "Known limitations" below.

## Download

| File | Size | SHA-256 |
| :--- | ---: | :--- |
| `SouthernIndianaLore-1.0.0-release.apk` | 14.6 MB | `48f8ef711f43bcc9873eb4a1518363dfcd9875e50b6b563d27a282068ebe7494` |

Requires Android 7.0 or newer. The APK is signed by **Alex Shade Productions**; the signer certificate's SHA-256 is
`42aa4c27c07c3afbf4e98e048d37f5e3febe061d6a34532d1fb99cecab761a41`.

### Check your download

```powershell
# Windows PowerShell
Get-FileHash .\SouthernIndianaLore-1.0.0-release.apk -Algorithm SHA256
```

```bash
# Linux or macOS
sha256sum SouthernIndianaLore-1.0.0-release.apk
```

The result must match the table above. To check the signer as well, use `apksigner` from the Android SDK
build-tools:

```bash
apksigner verify --print-certs SouthernIndianaLore-1.0.0-release.apk
```

The "certificate SHA-256 digest" must match the signer fingerprint above. If you build the app from source yourself,
your APK will have a different hash and your own signature.

## Install

1. Download the APK to your phone.
2. Open it. When Android asks, allow installs from your browser or file manager. The app is not on Google Play, so
   Android will warn that it is from an unknown source.
3. Open the app and allow location and notifications. "While using the app" is enough for a first look.
4. To try it without driving, open the **Tour** tab, find **Desk & Tabletop Mode**, and tap **Simulate Route**.

## Highlights

- **Offline by design.** The app declares no `INTERNET` permission. Map drawing, narration, and sound all happen on
  the phone.
- **52 sites, honestly labeled.** Every site shows a confidence badge, and the history screen says when something is
  not fully verified. [docs/VERIFICATION_LOG.md](VERIFICATION_LOG.md) lists every correction made in the first
  fact-check and what is still unchecked.
- **Background tours.** A foreground service watches your position and alerts you as you approach a site.
- **Field tools.** Journal, custom sites, voice memos, Markdown dossier export, and GPX waypoints for Garmin, OsmAnd,
  QGIS, and Google Earth.
- **Nothing leaves your phone.** No analytics or ad SDKs, and Android cloud backup is turned off.

## Known limitations

- **Device testing is in progress.** Expect rough edges on real phones. The layout targets large modern phones.
- **About half the entries are unchecked** and badged Unverified. Checked entries were compared with public sources
  only (not archival records), and can still contain mistakes. Corrections with a source are very welcome.
- **The map is minimal:** a dark schematic with a river, a creek, a ridgeline, and one road/rail corridor. It is not
  a navigation map; use the "Navigate" buttons.
- **No backup.** Journal entries, custom sites, and voice memos are lost if you uninstall the app or switch phones.
  Export a dossier first.
- **The Archivist is not an AI.** It answers from hand-written text about a few well-known sites.
- **Voice dictation and spoken narration** use your phone's speech services, which may use the network depending on
  your settings. The app itself makes no network calls.
- **Battery savers** can stop background tours. Exclude the app from battery optimization if alerts stop with the
  screen off.
- **Be careful out there.** Many sites are on private land or active rail lines. Use public viewpoints and obey
  posted signs and the law.

See the README for the full list.

## About this build

- Built on 2026-10-18 from commit `9c3eefd` (later commits change only documentation and templates).
- Android Gradle Plugin 9.1.1, Gradle 9.5.0, JDK 25 (Android Studio runtime), compile SDK 36, minimum SDK 24.
- Lore pack version 1.10.0. App version 1.0.0 (version code 1).
- 64 automated tests passed (unit and Robolectric). Android lint reported 84 warnings and no errors.
- Not minified. Signed with APK signature scheme v2.

## Licenses

Code and the app's artwork: MIT. Lore pack: CC BY-SA 4.0. Bundled fonts: SIL Open Font License. See
[THIRD_PARTY_NOTICES.md](../THIRD_PARTY_NOTICES.md) and [LICENSE](../LICENSE).

The full list of changes is in [CHANGELOG.md](../CHANGELOG.md).
