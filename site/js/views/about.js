// About: what the badges mean, how to install, privacy, sources, and licenses.

import { h } from "../lib/dom.js";
import { countBy } from "../lib/filter.js";
import * as offline from "../lib/offline.js";
import {
  VERIFICATION_LOG_URL, ISSUES_URL, LICENSE_URL, NOTICES_URL, CC_BY_SA_URL, OFL_URL, OWNER, REPO_URL,
} from "../lib/config.js";
import { badge } from "../ui.js";

// What a label means *in this pack*, from docs/VERIFICATION_LOG.md ("How the labels work").
const PACK_MEANING = {
  OFFICIAL_SOURCE: "An official designation (for example National Historic Landmark) was confirmed against an independent source.",
  PLAUSIBLE: "Checked against independent sources and corrected where they disagreed. Some other details are still unconfirmed.",
  UNVERIFIED: "Not confirmed: either not checked yet, or checked and no supporting source was found.",
  PROBABLE: "An older label that already admitted incomplete verification. Not re-labeled.",
  ORAL_HISTORY: "The entry's own nature: oral tradition. Not re-labeled.",
  LEGEND: "The entry's own nature: folklore. Not re-labeled.",
};

function externalLink(href, text) {
  return h("a", { href, target: "_blank", rel: "noopener noreferrer" }, text);
}

function block(id, title, ...children) {
  return h("section", { class: "panel about-section", id: "about-" + id }, h("h2", { class: "panel-title" }, title), ...children);
}

export function renderAbout(ctx, params) {
  const levelCounts = countBy(ctx.sites, "confidenceLevel");

  const badgeRows = ctx.levelOrder
    .filter((level) => levelCounts.has(level.id))
    .map((level) =>
      h(
        "li",
        { class: "legend-row" },
        h("div", null, badge(level), h("span", { class: "legend-count" }, levelCounts.get(level.id) + (levelCounts.get(level.id) === 1 ? " site" : " sites"))),
        h("p", null, level.description + "."),
        PACK_MEANING[level.id] ? h("p", { class: "fine" }, "In this pack: " + PACK_MEANING[level.id]) : null
      )
    );

  const offlineLine = h("p", { class: "offline-line", role: "status", "aria-live": "polite" });
  const installButton = h("button", { type: "button", class: "btn", hidden: true }, "Install this app");
  installButton.addEventListener("click", () => offline.promptInstall());
  const syncOffline = () => {
    offlineLine.textContent = "Offline copy: " + offline.getState().message;
    installButton.hidden = !offline.canInstall();
  };
  syncOffline();
  const unsubscribe = offline.subscribe(syncOffline);

  const sourceItems = ctx.atlas.sources.map((source) =>
    h(
      "li",
      null,
      source.archiveUrl ? externalLink(source.archiveUrl, source.title) : h("span", null, source.title),
      [source.author, source.publisher, source.year].filter(Boolean).length
        ? h("span", { class: "fine" }, " — " + [source.author, source.publisher, source.year].filter(Boolean).join(", "))
        : null
    )
  );

  const el = h(
    "div",
    { class: "view view-about" },
    h("h1", { class: "page-title" }, "About, sources & licenses"),
    h(
      "p",
      { class: "lede" },
      "The Southern Indiana Lore Atlas is the free web edition of the Southern Indiana Lore Android app by " + OWNER + ". It shows the same " +
        ctx.sites.length + "-site lore pack (version " + ctx.atlas.pack.version + ") in your browser and works offline once opened."
    ),

    block(
      "beta",
      "Beta: not yet tried on an iPhone",
      h("p", null, "This web edition is new. It has been tested in Chromium at phone and desktop sizes, but not yet on a real iPhone, so Add to Home Screen, narration voices, photos and the location prompt may behave differently there. Near me and tours only work while the page is open and the screen is on; background drive-by alerts are a feature of the Android app."),
      h("p", null, externalLink(ISSUES_URL, "Tell us if something looks wrong"), h("span", { class: "fine" }, " (on the project's GitHub page)"))
    ),

    block(
      "badges",
      "How far to trust each entry",
      h("p", null, "Every site carries a trust badge, and history that is not fully verified is labeled that way on its page. \"Plausible\" does not mean proven. These are the badges in use:"),
      h("ul", { class: "legend" }, badgeRows),
      h("p", null, "The history was compared with public sources (mostly Wikipedia, state historical markers, and National Park Service pages), not with primary archival records, and corrected where the sources disagreed. \"Plausible\" means the main facts matched, not that every sentence was confirmed. The Android app's relics and oral-history recordings are dramatized, and its Archivist answers are hand-written and mostly unchecked; none of that is part of this atlas."),
      h("p", null, externalLink(VERIFICATION_LOG_URL, "Read the full verification log"), h("span", { class: "fine" }, " (on the project's GitHub page)"))
    ),

    block(
      "android",
      "Atlas or Android app?",
      h("ul", { class: "plain" },
        h("li", null, h("strong", null, "This atlas:"), " browse and search, a street map you can save for offline use, driving tours with GPX downloads, a Near me list, six narrator voices, and your own passport, notes and photos kept in your browser. It all works while the page is open."),
        h("li", null, h("strong", null, "The Android app:"), " background drive-by alerts with the screen off, an oral-history tape deck, relics and the Archivist. Get it from the project's Releases page.")
      )
    ),

    block(
      "install",
      "Install it and use it offline",
      h("ul", { class: "plain" },
        h("li", null, h("strong", null, "iPhone or iPad (Safari):"), " tap the Share button, then \"Add to Home Screen\"."),
        h("li", null, h("strong", null, "Android (Chrome):"), " open the menu, then \"Install app\" or \"Add to Home screen\"."),
        h("li", null, "Open it once while online. After that the whole atlas works with no connection.")
      ),
      offlineLine,
      h("div", { class: "actions" }, installButton)
    ),

    block(
      "privacy",
      "Privacy",
      h("p", null, h("strong", null, "This atlas collects no data. Nothing you do here is sent to anyone.")),
      h("ul", { class: "plain" },
        h("li", null, "No analytics, advertising, accounts, or cookies. No fonts, scripts, images, or map data come from any other website; the page's security policy only lets it load files from this site."),
        h("li", null, "The street map is one file published with this atlas. Your browser reads the parts it needs from this site, so no map company sees where you look."),
        h("li", null, "Your passport, saved sites, notes, photos and narrator choice are kept in your browser's storage on this device only. They never leave it unless you download a backup file yourself. Photos are shrunk and stripped of their location data before saving. Check-ins record a date and whether you were at the site, never your position. \"Delete everything\" on the Mine tab removes them."),
        h("li", null, "Near me reads your position in your browser only while you use it. It is never saved, put in a link, or sent anywhere by this page."),
        h("li", null, "The offline copy (and the street map, if you save it) is stored in your browser on your device. Clearing the site's data in your browser settings removes it, along with your passport and notes."),
        h("li", null, "Map, directions, source and \"Suggest\" links open other websites or apps (such as GitHub, where posts are public) only when you tap them. Share and QR codes are made on your device."),
        h("li", null, "Read-aloud uses your browser's built-in voices. Some browsers offer online voices; that is the browser's choice, not this page's."),
        h("li", null, "Whatever hosts this site keeps its own standard server logs. This page adds nothing to them.")
      )
    ),

    block(
      "sources",
      "Sources in the lore pack",
      h("p", { class: "fine" }, "This is the pack's own source list. Each site page shows the citations for that entry. Some citations in the pack have not been independently verified; the verification log says which."),
      h("ul", { class: "source-list" }, sourceItems)
    ),

    block(
      "licenses",
      "Licenses and attribution",
      h("ul", { class: "plain" },
        h("li", null, h("strong", null, "Lore pack text and data: "), "\"Southern Indiana Historical Expeditions\" lore pack by " + OWNER + ", licensed ", externalLink(CC_BY_SA_URL, "CC BY-SA 4.0"),
          ". This atlas reformats the pack for the web without changing its text. If you reuse or adapt it, credit " + OWNER + " and share your version under the same license. The facts come from public records and the cited sources, which keep their own copyrights."),
        h("li", null, h("strong", null, "Code: "), "MIT License, © 2026 " + OWNER + ". ", externalLink(LICENSE_URL, "License text")),
        h("li", null, h("strong", null, "Fonts: "), "Cinzel (© 2020 The Cinzel Project Authors) and Plus Jakarta Sans (© 2020 The Plus Jakarta Sans Project Authors), under the ", externalLink(OFL_URL, "SIL Open Font License 1.1"), ". The fonts and the license are included with this site in the fonts folder."),
        h("li", null, h("strong", null, "Street map: "), "map data © ", externalLink("https://www.openstreetmap.org/copyright", "OpenStreetMap contributors"), ", available under the Open Database License, packaged by ", externalLink("https://protomaps.com", "Protomaps"), ". Drawn with MapLibre GL JS (BSD-3-Clause) and the PMTiles and Protomaps basemaps libraries (BSD-3-Clause); map labels use Noto Sans (SIL Open Font License) and the map icons derive from tangrams/icons (MIT). The schematic map is drawn from the pack's coordinates only."),
        h("li", null, h("strong", null, "Other libraries: "), "fflate (MIT) and qrcode-generator (MIT). Every library is copied into this site's vendor folder; versions and checksums are listed in vendor/VENDORED.md."),
        h("li", null, "More detail: ", externalLink(NOTICES_URL, "third-party notices"), ".")
      )
    ),

    block(
      "feedback",
      "Found a mistake?",
      h("p", null, "Corrections with a source are the most useful thing you can send. ", externalLink(ISSUES_URL, "Open an issue"), " or see the ", externalLink(REPO_URL, "project page"), ".")
    )
  );

  return {
    el,
    title: "About",
    destroy: unsubscribe,
    mounted() {
      const section = params.get("section");
      const target = section && document.getElementById("about-" + section);
      if (target) target.scrollIntoView();
    },
  };
}
