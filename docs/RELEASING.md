# Releasing a signed build

How to build, sign and check a release APK of Southern Indiana Lore. Written for the project owner, who holds the signing
key. The key and its password are never committed, never pasted into chat, and never stored in this repository.

## Why the signing key matters

Android only installs an update over an existing app if the new APK is signed with the **same key** as the installed one.
If the key is lost, nobody can ever update over existing copies of the app; people would have to uninstall first and lose
their notes and recordings. So keep the key file somewhere safe, keep a second copy somewhere that is not the same computer,
and keep the password in a password manager.

The signer certificate of every release must be:

```
SHA-256: 42aa4c27c07c3afbf4e98e048d37f5e3febe061d6a34532d1fb99cecab761a41
```

## Before you start

- Bump `appVersionName` and `versionCode` in `app/build.gradle.kts`, and the places that show the version on screen or in
  exports (search the code for the old version string, for example `v1.0.0`). `versionCode` must go up with every release.
- Work from a clean checkout of `main` (`git status` shows nothing to commit) and run the tests first.
- Know where your key file is, and its alias and password. If you did not set an alias, the build uses `upload`.
- JDK 21 or newer (Android Studio's bundled one works) and the Android SDK.

## Steps (Windows PowerShell, from the project folder)

```powershell
# 1. Tell the build where the key is. The password is typed in, hidden, and exists only in this window.
$env:KEYSTORE_PATH = "<full path to upload-keystore.jks>"
$env:KEY_ALIAS     = "<alias, or leave unset to use 'upload'>"
$pw = Read-Host "Key password" -AsSecureString
$env:STORE_PASSWORD = [System.Net.NetworkCredential]::new("", $pw).Password
$env:KEY_PASSWORD   = $env:STORE_PASSWORD   # only if the store and key password are the same

# 2. Run the tests, then build the signed release APK.
.\gradlew.bat testDebugUnitTest assembleRelease

# 3. Clear the secrets from this window when done.
Remove-Item Env:STORE_PASSWORD, Env:KEY_PASSWORD, Env:KEYSTORE_PATH, Env:KEY_ALIAS
```

The APK appears in `app\build\outputs\apk\release\` as `SouthernIndianaLore-<version>-release.apk`.

## Check the result

```powershell
# Size and hash (put both in the release notes)
Get-Item .\app\build\outputs\apk\release\SouthernIndianaLore-1.0.1-release.apk | Select-Object Name, Length
Get-FileHash .\app\build\outputs\apk\release\SouthernIndianaLore-1.0.1-release.apk -Algorithm SHA256

# The signer must match the fingerprint above (apksigner is in the Android SDK build-tools folder)
apksigner verify --print-certs .\app\build\outputs\apk\release\SouthernIndianaLore-1.0.1-release.apk
```

If the "certificate SHA-256 digest" does not match the fingerprint above, **stop**: it was signed with a different key and
cannot update existing installs.

## Publish

1. Copy the APK to a folder outside the repository and write a `SHA256SUMS.txt` beside it.
2. Fill in the placeholders in the release notes (`docs/RELEASE_NOTES_v<version>.md`), remove the draft box, and merge that
   change through a pull request.
3. Tag the commit (`git tag -a v1.0.1 -m "..."`, then push the tag). CI builds a debug APK and creates the release;
   edit that release, paste the notes, upload the signed APK and `SHA256SUMS.txt`, and delete the debug APK.
4. Download the APK from the public release page and check that its hash matches.

## Never

- Never commit the key, a file containing its password, `local.properties`, or an APK.
- Never paste the password into a chat, an issue or a pull request.
