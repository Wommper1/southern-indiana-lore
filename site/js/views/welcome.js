// The welcome screen: shown once, on a visitor's first time, and any time from Help. Three big choices, a text-size
// switch, and one line about privacy. Written for people who do not use many apps.

import { h } from "../lib/dom.js";
import * as prefs from "../lib/prefs.js";
import * as position from "../lib/position.js";
import { unlock } from "../lib/speech.js";

/** "Text size: Normal / Larger", used here and on the Help page. */
export function textSizeSwitch() {
  const normal = h("button", { type: "button", class: "seg" }, "Normal");
  const larger = h("button", { type: "button", class: "seg" }, "Larger");
  const sync = () => {
    normal.setAttribute("aria-pressed", String(!prefs.get("largeText")));
    larger.setAttribute("aria-pressed", String(prefs.get("largeText")));
  };
  normal.addEventListener("click", () => prefs.set("largeText", false));
  larger.addEventListener("click", () => prefs.set("largeText", true));
  const unsubscribe = prefs.subscribe(sync);
  sync();
  const el = h("div", { class: "text-size", role: "group", "aria-label": "Text size" },
    h("span", { class: "text-size-label" }, "Text size:"), normal, larger);
  return { el, destroy: unsubscribe };
}

export function renderWelcome(ctx) {
  const done = () => prefs.set("welcomeSeen", true);
  const go = (hash) => {
    done();
    location.hash = hash;
  };

  const nearBtn = h("button", { type: "button", class: "big-choice" },
    h("span", { class: "big-choice-icon", "aria-hidden": "true" }, "📍"),
    h("span", { class: "big-choice-text" }, h("strong", null, "See what's near me"),
      h("span", null, "Your phone will ask to share your location. It stays on your phone.")));
  nearBtn.addEventListener("click", () => {
    unlock();
    if (window.isSecureContext) position.start(); // started from this tap so the phone shows its permission question
    go("#/near");
  });

  const tourBtn = h("button", { type: "button", class: "big-choice", onclick: () => go("#/tours") },
    h("span", { class: "big-choice-icon", "aria-hidden": "true" }, "🚗"),
    h("span", { class: "big-choice-text" }, h("strong", null, "Take a driving tour"),
      h("span", null, (ctx.tours || []).length + " ready-made routes with stops in order.")));

  const browseBtn = h("button", { type: "button", class: "big-choice", onclick: () => go("#/") },
    h("span", { class: "big-choice-icon", "aria-hidden": "true" }, "📖"),
    h("span", { class: "big-choice-text" }, h("strong", null, "Browse all the places"),
      h("span", null, ctx.sites.length + " places, from ghost stories to the first state capitol.")));

  const size = textSizeSwitch();

  const el = h("div", { class: "view view-welcome" },
    h("h1", { class: "page-title" }, "Welcome"),
    h("p", { class: "welcome-lede" },
      "Discover the history and legends of " + ctx.sites.length + " places in southern Indiana. It's free, there's nothing to sign up for, and nothing about you is collected."),
    h("p", { class: "welcome-ask" }, "What would you like to do?"),
    h("div", { class: "big-choices" }, nearBtn, tourBtn, browseBtn),
    size.el,
    h("p", { class: "welcome-foot" },
      h("a", { href: "#/help", onclick: done }, "How to use this site"),
      " · ",
      h("a", { href: "#/", onclick: done }, "Skip")),
    h("p", { class: "fine" }, "You can come back to this screen any time from the Help tab."));

  return { el, title: "Welcome", destroy: size.destroy };
}
