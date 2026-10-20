// "Near me": sort the sites by distance from you, and optionally read one aloud.
// Works only while this page is open and the screen is on. Your position stays on this device.

import { h, clear } from "../lib/dom.js";
import { distanceMeters, bearingDegrees, cardinal, formatDistance, formatMeters } from "../lib/geo.js";
import * as position from "../lib/position.js";
import * as announcer from "../lib/announcer.js";
import { speak, stop as stopSpeech, unlock, speechSupported, isSpeaking, onSpeakingChange } from "../lib/speech.js";
import { spokenText } from "../lib/trust.js";
import { badge } from "../ui.js";

const SHOW_FIRST = 10;
const FAR_AWAY_M = 80 * 1609.344; // beyond this, you are probably not in southern Indiana

// A few well-known places to try the page from a desktop, taken from the pack's own sites (no real location needed).
const DEMO_SITE_IDS = ["floyd_culbertson_mansion", "lawrence_spring_mill_twin_caves", "perry_tell_city_swiss_enclave", "vincennes_fort_sackville"];

export function renderNear(ctx) {
  let showAll = false;

  const status = h("p", { class: "near-status", role: "status", "aria-live": "polite" });
  const announceNote = h("p", { class: "fine", role: "status", "aria-live": "polite" });
  const startButton = h("button", { type: "button", class: "btn btn-primary" }, "Start Near me");
  const stopButton = h("button", { type: "button", class: "btn" }, "Stop");
  const nearest = h("div", { class: "near-nearest" });
  const list = h("ol", { class: "near-list" });
  const moreButton = h("button", { type: "button", class: "btn" });

  // ---- read aloud controls ----
  const autoBox = h("input", { type: "checkbox", id: "auto-read" });
  autoBox.checked = announcer.isEnabled();
  const readNearestButton = h("button", { type: "button", class: "btn" }, "Read nearest aloud");
  const speechControls = speechSupported
    ? h(
        "div",
        { class: "panel" },
        h("h2", { class: "panel-title" }, "Read aloud"),
        h("label", { class: "check", for: "auto-read" }, autoBox, h("span", null, "Read a site aloud when I arrive at it (inside its alert radius)")),
        h("p", { class: "fine" }, "Only while this page is open. Each site is read once per visit. Phones may keep the sound low until you raise the volume. The reading always names the site's trust level."),
        h("div", { class: "actions" }, readNearestButton)
      )
    : h("div", { class: "panel" }, h("h2", { class: "panel-title" }, "Read aloud"), h("p", null, "This browser has no built-in speech, so reading aloud is not available. Near me still works."));

  // ---- demo location ----
  const demoSelect = h(
    "select",
    { class: "input", id: "demo-site", "aria-label": "Pretend to be near this site" },
    DEMO_SITE_IDS.filter((id) => ctx.byId.has(id)).map((id) => h("option", { value: id }, ctx.byId.get(id).title))
  );
  const demoButton = h("button", { type: "button", class: "btn" }, "Use demo location");

  function ranked(fix) {
    return ctx.sites
      .map((site) => ({
        site,
        d: distanceMeters(fix.lat, fix.lon, site.latitude, site.longitude),
        bearing: bearingDegrees(fix.lat, fix.lon, site.latitude, site.longitude),
      }))
      .sort((a, b) => a.d - b.d);
  }

  function row(entry) {
    const level = ctx.levelTable[entry.site.confidenceLevel];
    const inside = entry.d <= entry.site.geofenceRadiusMeters;
    return h(
      "li",
      null,
      h(
        "a",
        { class: "card near-row", href: "#/site/" + encodeURIComponent(entry.site.id), "data-level": level.id },
        h("div", { class: "card-top" }, badge(level, { compact: true }),
          h("span", { class: "card-distance" }, formatDistance(entry.d) + " " + cardinal(entry.bearing))),
        h("h3", { class: "card-title" }, entry.site.title),
        h("p", { class: "card-meta" }, entry.site.county + (inside ? " · you are inside its alert radius" : ""))
      )
    );
  }

  function refresh() {
    const state = position.getState();
    const active = position.isActive();
    startButton.hidden = active;
    stopButton.hidden = !active;
    clear(nearest);
    clear(list);
    moreButton.hidden = true;

    if (state.status === "on" && state.fix) {
      const fix = state.fix;
      const time = new Date(fix.time).toLocaleTimeString();
      status.textContent = state.demo
        ? "Demo location: this is a pretend position, not where you are."
        : "Located. Accuracy about " + formatMeters(fix.accuracy) + ". Updated " + time + ".";
      const all = ranked(fix);
      const first = all[0];
      if (first.d > FAR_AWAY_M) {
        nearest.appendChild(
          h("p", { class: "notice" }, "You seem to be far from southern Indiana: the nearest site is " + formatDistance(first.d) + " away. The list below is still sorted by distance. You can try a demo location to see how Near me works.")
        );
      }
      const shown = showAll ? all : all.slice(0, SHOW_FIRST);
      shown.forEach((entry) => list.appendChild(row(entry)));
      if (all.length > SHOW_FIRST) {
        moreButton.hidden = false;
        moreButton.textContent = showAll ? "Show only the nearest " + SHOW_FIRST : "Show all " + all.length + " sites";
      }
      readNearestButton.disabled = false;
    } else {
      status.textContent = state.message || (state.status === "off" ? "Near me is off. Nothing is being read from your device." : "");
      readNearestButton.disabled = true;
    }
    const last = announcer.lastSite();
    announceNote.textContent = last ? "Last read aloud on arrival: " + last.title : "";
  }

  startButton.addEventListener("click", () => {
    unlock();
    if (!window.isSecureContext) {
      status.textContent = "Location only works on secure (https) pages. If you are testing locally, use localhost.";
      return;
    }
    position.start();
  });
  stopButton.addEventListener("click", () => {
    position.stop();
    stopSpeech();
  });
  demoButton.addEventListener("click", () => {
    unlock();
    const site = ctx.byId.get(demoSelect.value);
    // Park the pretend position a short walk from the site so it lands inside the site's alert radius.
    position.setDemo(site.latitude + 0.0001, site.longitude);
  });
  moreButton.addEventListener("click", () => {
    showAll = !showAll;
    refresh();
  });
  autoBox.addEventListener("change", () => {
    unlock();
    announcer.setEnabled(autoBox.checked);
    // Check right away in case you are already standing at a site.
    const fix = position.getState().fix;
    if (fix) announcer.handleFix(fix);
  });
  readNearestButton.addEventListener("click", () => {
    const fix = position.getState().fix;
    if (!fix) return;
    if (isSpeaking()) {
      stopSpeech();
      return;
    }
    unlock();
    const first = ranked(fix)[0].site;
    speak(spokenText(first, ctx.levelTable[first.confidenceLevel]));
  });
  const syncReadButton = () => {
    readNearestButton.textContent = isSpeaking() ? "Stop reading" : "Read nearest aloud";
  };

  const unsubscribers = [
    position.subscribe(refresh),
    announcer.onAnnounce(refresh),
    onSpeakingChange(syncReadButton),
  ];

  const el = h(
    "div",
    { class: "view view-near" },
    h("h1", { class: "page-title" }, "Near me"),
    h(
      "section",
      { class: "panel honest", "aria-label": "How Near me works" },
      h("h2", { class: "panel-title" }, "Read this first"),
      h("ul", { class: "plain" },
        h("li", null, h("strong", null, "Works only while this page is open and your screen is on."), " A web page cannot run in the background."),
        h("li", null, "Background drive-by alerts with the screen off are a feature of the Android app only. They are not possible in an iPhone web page."),
        h("li", null, "Your location is used on this device only. This page never sends it anywhere and never saves it. (Your phone or browser may use network services to work out where you are; that happens outside this page.)"),
        h("li", null, "Pins are approximate and many sites are on private land or near active rail lines. Do not trespass.")
      )
    ),
    h("div", { class: "actions" }, startButton, stopButton),
    status,
    nearest,
    speechControls,
    announceNote,
    h("h2", { class: "panel-title" }, "Sites by distance"),
    list,
    h("div", { class: "actions" }, moreButton),
    h(
      "section",
      { class: "panel" },
      h("h2", { class: "panel-title" }, "No GPS handy? Try a demo"),
      h("p", { class: "fine" }, "Pretend to stand next to a site. This uses no real location."),
      h("div", { class: "toolbar-row" }, demoSelect, demoButton)
    )
  );
  refresh();
  syncReadButton();
  return { el, title: "Near me", destroy: () => unsubscribers.forEach((fn) => fn()) };
}
