# Case study: turning nearby history into something to explore

## At a glance
**Alex Shade · independent, AI-assisted project.** [Android v1.0.1](https://github.com/Wommper1/southern-indiana-lore/releases/tag/v1.0.1) and a [live browser atlas beta](https://wommper1.github.io/southern-indiana-lore/). Evidence reviewed October 28, 2026; no new phone or field tests were performed for this document.

## The challenge
Help someone notice local history and folklore around them, while showing how much confidence to place in each account. Alex's interest in exploring Southern Indiana provides the connection between place-based curiosity and an application.

## The investigation
The [verification log](docs/VERIFICATION_LOG.md) records checks against public sources, corrections and uncertainty. This is public-source research, not a claim of archival verification or professional historian credentials. The README describes 52 sites, mostly marked Plausible, with unsupported entries left Unverified.

## The approach and build
The Android repository uses Kotlin, Jetpack Compose and Room/SQLite for bundled data, journal entries and location-aware exploration. The separate `site/` folder contains an HTML/CSS/JavaScript web atlas. Confidence badges and source notes are part of the interface. The map is schematic; external navigation is a separate hand-off.

<img src="docs/screenshots/atlas-site-page.jpg" width="260" alt="Actual atlas site page with source notes and a Plausible confidence badge">

Alex's contribution is the idea, project direction, AI-assisted development and hands-on exploration/testing. Source references, third-party components and AI assistance remain distinct from original creative direction.

## The obstacles
Offline behavior, background location and historical accuracy are different kinds of problem. The repository's [contribution guide](CONTRIBUTING.md) guards offline and permission claims; its verification log documents historical corrections. The browser cannot offer Android's screen-off drive-by alert behavior. These are documented design boundaries, not claims that every battery or GPS condition has been tested.

## The result and limits
A signed Android APK and checksum file are public; the atlas is live. The README records owner use on a Samsung S25 Ultra, while other devices and real iPhone use remain untested. `tests/site/atlas.test.mjs` and Android tests provide source evidence of automated checks; they were not rerun for this documentation change. The Android app itself has no INTERNET permission, but system speech services and links may use the network. Neither an alert nor a map pin grants access to private land.

## Try it / what comes next
Open the [browser atlas](https://wommper1.github.io/southern-indiana-lore/) without installing anything, or follow [Try it](README.md#try-it) for Android's tabletop simulation. Check sources and confidence labels rather than taking every story as established fact.

The most valuable help is a sourced correction, a specific usability report or a documented device test. Use [CONTRIBUTING.md](CONTRIBUTING.md) and the [issue forms](https://github.com/Wommper1/southern-indiana-lore/issues/new/choose); keep sensitive locations and personal field notes private. Follow [SECURITY.md](SECURITY.md) for vulnerabilities. Broader field/device testing is still needed; no new release date is promised here.

[Meet Alex](https://github.com/Wommper1) · [The Level Everything Company](https://leveleverythingcompany.github.io/)
