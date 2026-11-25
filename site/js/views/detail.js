// One page per site: badge, split history/folklore panels, open questions, sources, and visiting notes.

import { h } from "../lib/dom.js";
import {
  historyHeading, historyText, historyCaveat, showsFolklorePanel, folkloreText, sourcesText, paragraphs,
  HEADING_FOLKLORE, HEADING_SOURCES, FOLKLORE_ADVISORY,
} from "../lib/trust.js";
import { stop as stopSpeech, speechSupported, onSpeakingChange, isSpeaking, unlock } from "../lib/speech.js";
import { narrate } from "../lib/personas.js";
import { narratorPicker } from "./narratorpicker.js";
import { sharePanel, pageUrl } from "./share.js";
import { correctionUrl } from "../lib/config.js";
import { formatMeters } from "../lib/geo.js";
import { badge, statusCard, section } from "../ui.js";
import { journalPanel } from "./journalpanel.js";
import * as journal from "../lib/journal.js";

const KARST_DISCLOSURE =
  "Karst survey disclosure: karst conduit pathways and subterranean hydrology are mapped from geological surveys and spring dye traces. Cave protection regulations apply.";
const SAFETY_NOTE =
  "Be careful out there. Many sites are on private land or active rail lines. This page is not directions or permission to go anywhere; use public viewpoints and obey posted signs and the law.";

function paras(text, cls) {
  return paragraphs(text).map((p) => h("p", cls ? { class: cls } : null, p));
}

function labeled(label, text) {
  return text ? h("div", { class: "kv" }, h("dt", null, label), h("dd", null, text)) : null;
}

function mapLinks(site) {
  const ll = site.latitude + "," + site.longitude;
  const apple = "https://maps.apple.com/?ll=" + ll + "&q=" + encodeURIComponent(site.title);
  const google = "https://www.google.com/maps/search/?api=1&query=" + ll;
  return h(
    "p",
    { class: "map-links" },
    h("a", { href: apple, target: "_blank", rel: "noopener noreferrer" }, "Open in Apple Maps"),
    " · ",
    h("a", { href: google, target: "_blank", rel: "noopener noreferrer" }, "Open in Google Maps"),
    h("span", { class: "fine" }, " These open another app or website when you tap them. This atlas sends nothing.")
  );
}

function contextPanel(site) {
  const layers = [
    ["Above", site.aboveLayer], ["Surface", site.surfaceLayer], ["Below", site.belowLayer],
  ].filter(([, text]) => text);
  const times = [
    ["1800", site.time1800], ["1850", site.time1850], ["1900", site.time1900], ["1950", site.time1950], ["Today", site.timeToday],
  ].filter(([, text]) => text);

  const body = [
    site.driveStory ? h("div", { class: "ctx" }, h("h3", null, "Drive-by summary"), h("p", null, site.driveStory)) : null,
    site.whyHere ? h("div", { class: "ctx" }, h("h3", null, "Why did this exist here?"), h("p", null, site.whyHere)) : null,
    layers.length
      ? h("div", { class: "ctx" }, h("h3", null, "Landscape layers"),
          h("dl", { class: "kv-list" }, layers.map(([label, text]) => labeled(label, text))))
      : null,
    times.length
      ? h("div", { class: "ctx" }, h("h3", null, "Time layers"),
          h("dl", { class: "kv-list" }, times.map(([label, text]) => labeled(label, text))))
      : null,
    site.whatMayBeUnderground
      ? h("div", { class: "ctx" }, h("h3", null, "Under your feet"), h("p", null, site.whatMayBeUnderground),
          site.undergroundConfidence ? h("p", { class: "fine" }, "Survey confidence: " + site.undergroundConfidence.replace(/_/g, " ")) : null,
          h("p", { class: "fine" }, KARST_DISCLOSURE))
      : null,
    site.historyNerdDiscovery
      ? h("div", { class: "ctx" }, h("h3", null, "History nerd discovery"), h("p", null, site.historyNerdDiscovery))
      : null,
    site.relatedLocations ? h("div", { class: "ctx" }, h("h3", null, "Related places"), h("p", null, site.relatedLocations)) : null,
    site.visibleRemains ? h("div", { class: "ctx" }, h("h3", null, "What can still be seen"), h("p", null, site.visibleRemains)) : null,
    site.relatedPeople ? h("div", { class: "ctx" }, h("h3", null, "Related people"), h("p", null, site.relatedPeople)) : null,
    site.relatedEvents ? h("div", { class: "ctx" }, h("h3", null, "Related events"), h("p", null, site.relatedEvents)) : null,
  ].filter(Boolean);
  if (!body.length) return null;
  return h(
    "section",
    { class: "more-part" },
    h("h3", { class: "more-h" }, "Background"),
    h("p", { class: "fine" }, "These notes carry the same confidence level as the badge above. They are not separately verified."),
    body
  );
}

export function renderDetail(ctx, id, canGoBack) {
  const site = ctx.byId.get(id);
  if (!site) {
    return {
      el: h("div", { class: "view" }, h("h1", { class: "page-title" }, "Site not found"),
        h("p", null, "That site is not in this atlas."), h("p", null, h("a", { href: "#/" }, "Back to all sites"))),
      title: "Site not found",
    };
  }
  const level = ctx.levelTable[site.confidenceLevel];

  const back = canGoBack
    ? h("button", { type: "button", class: "back", onclick: () => history.back() }, "← Back")
    : h("a", { class: "back", href: "#/" }, "← All sites");

  const speakButton = speechSupported
    ? h("button", { type: "button", class: "btn btn-big" }, "🔊 Read it to me")
    : null;
  let unsubscribe = null;
  let startedHere = false;
  if (speechSupported) {
    const refresh = () => {
      speakButton.textContent = isSpeaking() ? "⏹ Stop reading" : "🔊 Read it to me";
    };
    speakButton.addEventListener("click", () => {
      if (isSpeaking()) {
        stopSpeech();
      } else {
        unlock();
        startedHere = true;
        narrate(site, level);
      }
      refresh();
    });
    unsubscribe = onSpeakingChange(refresh);
  }

  const caveat = historyCaveat(site.confidenceLevel);
  const historyPanel = section(
    historyHeading(site.confidenceLevel),
    "panel-history",
    caveat ? h("p", { class: "fine caveat" }, caveat) : null,
    paras(historyText(site))
  );
  const folkPanel = showsFolklorePanel(site)
    ? section(HEADING_FOLKLORE, "panel-folklore", h("p", { class: "fine" }, FOLKLORE_ADVISORY), paras(folkloreText(site)))
    : null;
  const openQuestions = site.unresolvedQuestions
    ? section("Open questions", "panel-open", h("p", { class: "fine" }, "What is not settled about this entry."), paras(site.unresolvedQuestions))
    : null;

  const narrative = h(
    "section",
    { class: "more-part" },
    h("h3", { class: "more-h" }, "The whole story (history and legend mixed)"),
    h("p", { class: "fine" }, "This overview blends documented history with tradition. Use the labeled sections above to tell them apart."),
    paras(site.fullStory)
  );

  const visiting = section(
    "Visiting",
    "panel-visit",
    h(
      "dl",
      { class: "kv-list" },
      labeled("Address", site.address),
      labeled("What's there", [site.accessStatus, site.accessLevel].filter(Boolean).join(" · "))
    ),
    h(
      "p",
      { class: "fine" },
      site.siteType === "LINE"
        ? "The map pin marks one point along a route, not an exact spot. "
        : "The map pin is approximate. ",
      "Check before you go."
    ),
    mapLinks(site),
    h("p", null, h("a", { href: "#/map?site=" + encodeURIComponent(site.id) }, "Show it on the map")),
    h("p", { class: "safety" }, SAFETY_NOTE)
  );

  const mine = journalPanel(site);
  const picker = narratorPicker({ compact: true });
  const hash = "#/site/" + encodeURIComponent(site.id);
  const share = sharePanel({ title: site.title, hash, fileName: site.id });

  // The three things most people want, as big buttons at the top.
  const ll = site.latitude + "," + site.longitude;
  const apple = /iPhone|iPad|iPod|Macintosh/.test(navigator.userAgent);
  const directions = h("a", {
    class: "btn btn-big btn-primary",
    href: apple ? "https://maps.apple.com/?daddr=" + ll + "&q=" + encodeURIComponent(site.title) : "https://www.google.com/maps/dir/?api=1&destination=" + ll,
    target: "_blank", rel: "noopener noreferrer",
  }, "🧭 Directions");
  const saveBtn = h("button", { type: "button", class: "btn btn-big", "aria-pressed": "false" }, "☆ Save");
  let favorite = false;
  const paintSave = () => {
    saveBtn.textContent = favorite ? "★ Saved" : "☆ Save";
    saveBtn.setAttribute("aria-pressed", String(favorite));
  };
  const loadSave = () => journal.get(site.id).then((e) => { favorite = e.favorite; paintSave(); });
  saveBtn.hidden = !journal.supported();
  saveBtn.addEventListener("click", async () => {
    favorite = (await journal.setFavorite(site.id, !favorite)).favorite;
    paintSave();
  });
  const unsubscribeSave = journal.subscribe(loadSave);
  loadSave();

  const more = h("details", { class: "panel more more-details" },
    h("summary", null, "More details: background, the whole story, and sources"),
    contextPanel(site),
    narrative,
    h("section", { class: "more-part" }, h("h3", { class: "more-h" }, HEADING_SOURCES), paras(sourcesText(site), "small"),
      h("p", { class: "fine" }, "Spotted a mistake? ",
        h("a", { href: correctionUrl(site, pageUrl(hash)), target: "_blank", rel: "noopener noreferrer" }, "Suggest a correction"),
        " (opens GitHub; needs a free account, and what you write there is public). A source makes a correction much more useful.")),
    h("section", { class: "more-part" }, h("h3", { class: "more-h" }, "Exact map position"),
      h("p", { class: "small" }, site.latitude.toFixed(4) + ", " + site.longitude.toFixed(4) + " (approximate)")));
  const voice = picker.el ? h("details", { class: "voice-choice" }, h("summary", null, "Change the reading voice"), picker.el) : null;

  const el = h(
    "article",
    { class: "view view-detail", "data-level": level.id },
    back,
    h("h1", { class: "page-title" }, site.title),
    h("p", { class: "subtitle" }, site.subtitle),
    h("p", { class: "meta" }, badge(level), h("span", null, site.county + " · " + site.era + " · " + site.category)),
    h("div", { class: "actions quick-actions" }, directions, speakButton, saveBtn),
    voice,
    statusCard(site, level),
    historyPanel,
    folkPanel,
    openQuestions,
    visiting,
    mine.el,
    more,
    share
  );
  const destroy = () => {
    mine.destroy();
    unsubscribeSave();
    picker.destroy();
    if (unsubscribe) unsubscribe();
    if (startedHere) stopSpeech(); // leaving the page ends the reading it started
  };
  return { el, destroy, title: site.title };
}
