# Web Atlas

A free, offline-capable website version of the lore pack, so people who cannot install the Android app (iPhone users
in particular) can still read the sites. It lives in [`site/`](../site/) and needs no build tools: plain HTML, CSS and
JavaScript that a browser opens directly.

**Status: built and tested locally; not published.** Nothing here is deployed. GitHub Pages is not enabled, and a
ready-to-use workflow waits in [`docs/web-atlas/pages.yml`](web-atlas/pages.yml) for the day the repository is
public (see "Publishing" below).

## What it does

- **Browse and search** the 52 sites; filter by county and by trust badge; sort by name or county.
- **A page per site** with the trust badge, the history, the folklore, open questions, citations, and visiting notes.
- **A schematic map** drawn in SVG from the pack's coordinates. No map tiles, no roads or rivers (so nothing
  unsurveyed is presented as geography), pan and zoom, tap a pin for the site.
- **Near me:** while the page is open and the screen is on, it sorts the sites by distance from you and can read the
  nearest one, or the one you arrive at, aloud. It says plainly that background drive-by alerts with the screen off
  are an Android-app feature and are not possible in an iPhone web page. A demo location lets you try it from a desk.
- **Installable and offline:** a web manifest and a service worker save every file, so it works with no connection
  after the first visit and can be added to a home screen.
- **Attribution and privacy** on every page's footer and in full on the About page.

## Try it on your computer

```bash
python scripts/serve_site.py
```

Then open <http://localhost:8080/>. (`localhost` counts as a secure page, so location and the offline copy work there.
On a phone they need `https`, which GitHub Pages provides once published.) The script only serves the `site/` folder.

## Keeping it in step with the app

The website never edits the lore pack. It reads one generated file, `site/data/atlas.json`, which a script builds
from the pack in `app/src/main/assets/lorepacks/southern_indiana/` and from the badge labels in the app's
`ConfidenceLevel.kt`. After you change the pack, or any file in `site/`, run both of these and commit the result:

```bash
python scripts/update_pack_manifest.py     # only after editing the pack (this is the app's existing step)
python scripts/build_site_data.py          # rewrites site/data/atlas.json and site/precache.js
```

- `python scripts/build_site_data.py --check` verifies without writing and exits 1 if something is stale.
- The script refuses to run if the pack files no longer match `pack_manifest.json`.
- It stops with an error if `ConfidenceLevel.kt` and the pack schema disagree about the list of levels.
- Unknown or missing confidence levels become `UNVERIFIED`, the same fail-closed rule the app uses.
- `site/precache.js` lists every file the offline copy saves plus a fingerprint of all of them. Phones use that
  fingerprint to notice a new version, which is why it must be rebuilt after editing any site file, not just the data.
  The Pages workflow template rebuilds it on every deploy, so a forgotten rebuild cannot reach visitors.

Everything under `site/` uses LF line endings (see `.gitattributes`) so the fingerprint is identical on Windows and Linux.

## How the trust labels carry over

The atlas follows [`TRUTH_LAYER.md`](design/TRUTH_LAYER.md) and the Android detail screen:

- The badge text and descriptions come straight from `ConfidenceLevel.kt` (Official Source, Plausible, Folk Legend,
  Unverified, and so on). A test checks that they still match.
- The heading is **"Documented archival history"** only for Official Source, Archivally Verified and Well Supported.
  Everything else, including Plausible, gets **"History (not fully verified)"**, exactly as in the app.
- History and folklore sit in separate panels. Folklore carries the app's advisory ("Not established as archival
  fact"). The pack's blended `fullStory` is kept, collapsed, under "Full narrative (history and folklore mixed)".
- Blank-history fallbacks match the app ("No independent archival record or public deed established...").
- Read-aloud always says the trust level first and ends with "this history is not fully verified" unless the
  entry is documented.
- Colour is never the only signal: each badge also has a shape (circle, rounded square, diamond, ring), and the same
  shapes are used on the map.

Small, deliberate differences from the app (all in the cautious direction):

- A short italic reminder inside the history panel ("Read this as supported, not proven." for Plausible; "Not
  confirmed by independent sources..." for weaker labels).
- The `unresolvedQuestions` text is shown as its own **Open questions** panel, because it holds the honest "what is
  not settled" notes (for example on Greenville and Duncan Tunnel).
- The location note says the pin is approximate and "Confirm before you go", instead of the app's blanket "Exact
  coordinate" chip, and every site page repeats the app's private-land and rail-line safety warning.

## Privacy by construction

- No analytics, no cookies, no accounts, no browser storage; the position is held in memory only.
- The page's Content-Security-Policy (`site/index.html`) only allows files from its own site: no external fonts,
  scripts, images, tiles or network calls. Fonts are vendored in `site/fonts/`.
- External links (map apps, sources) are plain links that open only when a person taps them.
- Location is never put in a URL, saved, or sent by the page. The honest caveats are stated on the Near me and About
  pages: the phone or browser may use network services to work out a position, and some browsers offer online speech
  voices; those happen outside the page. A host such as GitHub Pages keeps its own server logs.
- Tests in `tests/site/atlas.test.mjs` fail if a source file gains an external URL, a tracking-style API, browser
  storage, or console logging.

## Tests

```bash
node --test tests/site/atlas.test.mjs      # needs Node 22 or newer and Python 3
```

They check that the atlas matches the pack field-for-field and that `build_site_data.py --check` passes (drift),
the badge/heading/fallback rules, fail-closed behaviour, distance and map math, search and filters, the privacy
guards above, and that every site file is in the offline list.

GitHub runs the same checks automatically through [`.github/workflows/site-ci.yml`](../.github/workflows/site-ci.yml)
("Web atlas CI") on pushes to `main` and on pull requests that touch `site/`, `tests/site/`, the lore pack,
`ConfidenceLevel.kt` or the two scripts; Android-only changes skip it. It verifies the pack hashes, runs
`build_site_data.py --check` (so a forgotten data or offline-list rebuild fails the PR), and runs the Node tests. It
only checks; it never deploys.

## Publishing

The atlas is published with GitHub Pages at <https://wommper1.github.io/southern-indiana-lore/>. Pages is set to
Source: **GitHub Actions** (Settings, Pages). The workflow `.github/workflows/pages.yml` runs when `site/`, the tests,
the lore pack, `ConfidenceLevel.kt` or `scripts/build_site_data.py` change on `main` (and has a manual "Run workflow"
button). It verifies the pack, rebuilds the atlas data, runs the tests, and publishes only `site/`. Everything in
`site/` uses relative paths, so the sub-folder address works. After a deploy, open it once on a phone while online,
then try airplane mode.

If the repository is ever renamed, update the links in `site/js/lib/config.js`.

## What was and was not tested

Tested here (see the pull request for the run log):

- Layout, badges, filters, detail pages, map, Near me and About in a Chromium browser pane emulating a 375x812 phone,
  including a mocked `navigator.geolocation` and a recorded (not audible) speech call.
- Service worker install, the offline copy, reloading with the server stopped, and the cache swap after a rebuild, in
  headless Chrome, along with the real geolocation API driven by an emulated position.
- The Node unit and guard tests above.

**Not tested:** real iPhone/iOS Safari (home-screen install, iOS speech and location behaviour, safe-area spacing),
a real Android phone, real GPS, audible speech output, or an actual deployment to GitHub Pages. Treat iOS behaviour as
unverified until someone tries it on a device.

## Known limitations

- Pins and the map are schematic. Overlapping pins in the Floyd/Clark cluster need zooming; the card under the map
  lists pins that overlap the one you tapped.
- Near me needs the page open and the screen on. iPhones may keep the sound low until the volume is raised, and may
  ask for a tap before speaking; the page unlocks speech from the Start and checkbox taps.
- Web GPS is coarser than the app's; automatic read-aloud ignores fixes wider than 150 m.
- The lore pack's own limits apply: history is only partly verified (see [VERIFICATION_LOG.md](VERIFICATION_LOG.md)).

## Where things are

| Path | What it is |
| :--- | :--- |
| `site/index.html`, `site/css/`, `site/js/` | The atlas itself (plain files, no build step) |
| `site/data/atlas.json` | Generated from the pack by `scripts/build_site_data.py`; CC BY-SA 4.0 like the pack |
| `site/precache.js`, `site/sw.js`, `site/manifest.webmanifest` | Offline copy and install support |
| `site/fonts/` | Cinzel and Plus Jakarta Sans with their OFL license text |
| `site/icons/` | Home-screen icons, cropped from the app's own icon artwork (MIT) |
| `scripts/build_site_data.py`, `scripts/serve_site.py` | Data build and local preview |
| `tests/site/atlas.test.mjs` | Tests |
| `.github/workflows/site-ci.yml` | Runs the tests and the data-drift check on pull requests (never deploys) |
| `docs/web-atlas/pages.yml` | Pages workflow template (inactive) |
