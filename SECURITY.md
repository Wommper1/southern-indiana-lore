# Security Policy

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |

## Security & Privacy Commitments

1. **No Data Collection**: Southern Indiana Lore does not transmit user GPS coordinates, journal entries, voice memos, or personal information anywhere. The app declares no `INTERNET` permission.
2. **Offline-First Resilience**: All core lore exploration, geofencing calculations, map rendering, audio synthesis, and narrative features operate completely offline on the local device.
3. **Secret Isolation**: The app uses no API keys. Release signing keys (`*.jks`, `*.keystore`) and local configuration (`local.properties`) are git-ignored and must never be committed.
4. **Least-Privilege Android Declarations**: Permissions are limited to features directly used: location (including background location for the driving tour), notifications and a location foreground service for proximity alerts, vibration, and the microphone for the Oral History Tape Deck only. The camera is not used.
5. **Local-Only Storage**: Android cloud backup and device-to-device transfer are disabled, so journal entries, custom sites, and voice memos are not copied off the device by the system.
6. **Known third-party hand-off**: Field-note voice dictation and spoken narration use the phone's system speech services, which may use the network depending on the device and its settings. The app does not control that.

## Reporting a Vulnerability

If you discover a potential security vulnerability within this repository, please report it privately:

- **Contact**: Alex Shade Productions
- **Method**: Open a private security advisory on GitHub or contact the maintainer directly.
- **Response SLA**: Vulnerability reports will be acknowledged within 48 hours, with a remediation timeline provided following technical verification.

Please do not disclose security vulnerabilities publicly until a fix has been verified and released.
