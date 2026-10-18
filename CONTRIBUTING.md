# Contributing to Southern Indiana Lore

Thanks for helping. The most valuable contribution is **fixing the history**: the lore pack has only been spot-checked against public sources, never against archives, and every correction with a good source makes the app more trustworthy. Bug reports and small
improvements are welcome too.

## Ground rules

- **Offline first.** The app declares no `INTERNET` permission and makes no network calls. Please do not add one
  without opening an issue first; a test (`PublicClaimsGuardTest`) fails if the permission set changes.
- **Keep the claims honest.** README.md and SECURITY.md make specific promises about permissions, privacy and
  verification. If a change makes one of them untrue, fix the code or change the docs in the same pull request.
- **No secrets.** Never commit keystores (`*.jks`, `*.keystore`), passwords, API keys, or `local.properties`.
  The app needs none of them to build.
- **Be careful with locations.** Do not add precise coordinates for sensitive sites (for example archaeological
  sites) or directions to private property.

## Building and testing

You need JDK 17 or newer and the Android SDK (compile SDK 36). Android Studio's bundled JDK works.

```bash
./gradlew testDebugUnitTest   # unit and Robolectric tests
./gradlew lintDebug           # Android lint (report in app/build/reports/)
./gradlew assembleDebug       # debug APK in app/build/outputs/apk/debug/
```

On Windows use `gradlew.bat`. If `JAVA_HOME` is not set, point it at Android Studio's `jbr` folder.

## Correcting or adding lore

The pack lives in `app/src/main/assets/lorepacks/southern_indiana/` (see `docs/design/PACK_AUTHORITY_AND_INTEGRITY.md`).

1. Find a source for **each specific claim** (dates, numbers, names). Prefer Indiana historical marker pages,
   National Register records, period newspapers and archives over summaries. Wikipedia is fine as a starting
   point, but say so.
2. Edit `sites.json`. Keep the file's formatting (two-space indent, LF line endings).
3. Update the entry's labels honestly:
   - `confidenceLevel` and `verificationGrade` must match what you actually checked. The tests enforce that an
     entry only claims a strong level (Official Source, Plausible and so on) if its grade starts with
     "Checked" and its source notes say so.
   - Use **Official Source** only when you confirmed an official designation. Use **Plausible** when the main
     facts check out but some details are unconfirmed. Leave an entry **Unverified** if you did not check it.
   - Where sources disagree, say so in the entry and give the range instead of picking one figure.
4. Record what you did in `docs/VERIFICATION_LOG.md` (the correction, the basis, and the source link).
5. Regenerate the manifest hashes and bump the pack version:

   ```bash
   python scripts/update_pack_manifest.py --bump-version 1.10.1
   ```

6. Run `./gradlew testDebugUnitTest`. The pack is rejected at startup if the hashes do not match.

If you only have a correction and not the time to edit the pack, open a **Data correction** issue with the source.

## Code changes

- Match the style of the surrounding code (Kotlin official style, Jetpack Compose, Room).
- Add or update tests for behavior you change.
- Keep pull requests focused; one topic per PR is easier to review.

## Licensing of contributions

By submitting a contribution you agree that your code is released under the MIT license (see `LICENSE`) and that
changes to the lore pack are released under CC BY-SA 4.0 (see `THIRD_PARTY_NOTICES.md`).

## Reporting security problems

Please do not open a public issue. See `SECURITY.md` for how to report privately.
