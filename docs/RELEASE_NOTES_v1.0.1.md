# Southern Indiana Lore v1.0.1

> **DRAFT. Do not publish yet.** The sizes and hashes below are placeholders until the signed APK is built. See
> [RELEASING.md](RELEASING.md) for how to build, sign and check it, then fill in the table and delete this box.

A content update to v1.0.0. The app's features are the same; the lore pack is better checked.

## Download

| File | Size | SHA-256 |
| :--- | ---: | :--- |
| `SouthernIndianaLore-1.0.1-release.apk` | (fill in) | (fill in) |

Requires Android 7.0 or newer. The APK is signed by **Alex Shade Productions** with the same key as v1.0.0, so it installs
over v1.0.0. The signer certificate's SHA-256 must be
`42aa4c27c07c3afbf4e98e048d37f5e3febe061d6a34532d1fb99cecab761a41`, the same as v1.0.0. To check your download, see "Check
your download" in the [v1.0.0 notes](RELEASE_NOTES_v1.0.0.md); use the hash in the table above.

## What changed

- **Lore pack 1.12.1: all 52 entries have now been checked against public sources.** The last six were checked in this round:
  - *Salem (Morgan's Raid and the Quakers)* is now **Plausible**. The state marker confirms the July 10, 1863 occupation, the
    looting and a $1,000 ransom from each mill owner; the raiders numbered about 1,800, not 2,400. The Underground Railroad
    claims stay but are marked as resting on later recollections.
  - *Falling Run Phantom Trestle* and *Twelve Mile Island Steamboat Wrecks* are now **Unverified**: the places are real, but no
    source supports the trestle derailment or the wrecks.
  - *Floyd County Asylum*, *Floyds Knobs* and *Silver Hills Hermit* were corrected and keep their labels (Probable, Folk Legend,
    Folk Legend). No source was found for the hermit.
- Three missing apostrophes in the story text were fixed, and entries no source supports no longer show an exact date as their era.
- The Morgan's Raid trail description now matches the Battle of Corydon entry.
- Full details: [VERIFICATION_LOG.md](VERIFICATION_LOG.md).

## Updating from v1.0.0

Install the new APK over the old one. The app replaces only its own lore-pack sites when the pack changes and is built to
leave your journal entries and custom sites alone. (That behavior comes from reading the code; it has not been tested on a
phone.) Export a dossier first if you want a backup; the app does not back up your data.

## Known limitations

Unchanged from v1.0.0: the history is only partly confirmed (most entries are Plausible, and four are Unverified), the map is
a minimal schematic, there is no backup of your notes and recordings, the Archivist is a keyword lookup and not an AI, and
battery savers can silence background tours. Tried by the owner on one real phone (Samsung Galaxy S25 Ultra); other devices
are untested.

## About this build

- Version 1.0.1 (version code 2), lore pack 1.12.1, application ID unchanged.
- Fill in after building: commit, Android Gradle Plugin, Gradle and JDK versions, number of tests passed, size and SHA-256.
