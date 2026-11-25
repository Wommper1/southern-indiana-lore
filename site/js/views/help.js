// Help: short questions and plain answers for people who do not use many apps. Each answer opens on tap.
// The longer About page (sources, licenses, full privacy details) is linked from here.

import { h } from "../lib/dom.js";
import * as offline from "../lib/offline.js";
import { suggestSiteUrl, ISSUES_URL } from "../lib/config.js";
import { badge } from "../ui.js";
import { textSizeSwitch } from "./welcome.js";

const ext = (href, text) => h("a", { href, target: "_blank", rel: "noopener noreferrer" }, text);

function qa(question, ...answer) {
  return h("details", { class: "qa" }, h("summary", null, question), h("div", { class: "qa-body" }, ...answer));
}

const steps = (...items) => h("ol", { class: "steps" }, items.map((item) => h("li", null, item)));

// What each label means, in everyday words. The label names themselves match the Android app.
const PLAIN = {
  OFFICIAL_SOURCE: "Backed by an official record, such as a National Historic Landmark listing.",
  PLAUSIBLE: "The main facts check out against other sources, but not every detail has been confirmed.",
  PROBABLE: "Likely true, but only partly checked.",
  LEGEND: "A local legend or ghost story. Enjoy it as a story, not as proven history.",
  FOLKLORE: "Folklore: a story people tell, not proven history.",
  ORAL_HISTORY: "Passed down by word of mouth; not written records.",
  UNVERIFIED: "We looked and could not find proof yet. Treat it with caution.",
};

export function renderHelp(ctx) {
  const size = textSizeSwitch();
  const used = new Set(ctx.sites.map((s) => s.confidenceLevel));
  const labels = ctx.levelOrder.filter((l) => used.has(l.id));

  const installBtn = h("button", { type: "button", class: "btn btn-primary", hidden: !offline.canInstall() }, "Add it to this phone now");
  installBtn.addEventListener("click", () => offline.promptInstall());
  const unsubscribe = offline.subscribe(() => {
    installBtn.hidden = !offline.canInstall();
  });

  const el = h("div", { class: "view view-help" },
    h("h1", { class: "page-title" }, "Help"),
    h("p", { class: "lede" }, "Tap a question to see the answer."),
    h("section", { class: "panel" },
      h("h2", { class: "panel-title" }, "Make it easier to read"),
      size.el,
      h("p", null, h("a", { href: "#/welcome" }, "Show the welcome screen again"))),

    h("div", { class: "qa-list" },
      qa("What is this?",
        h("p", null, "A free guide to " + ctx.sites.length + " places in southern Indiana: real history, old buildings, caves, railroads and local legends. Each place has a colored label that tells you how sure we are about its story."),
        h("p", null, "Tap ", h("strong", null, "Places"), " to browse, ", h("strong", null, "Map"), " to see them on a map, ", h("strong", null, "Tours"), " for driving routes, and ", h("strong", null, "Near me"), " to find what's close to you.")),

      qa("How do I put it on my phone like an app?",
        h("p", null, h("strong", null, "iPhone or iPad (Safari):")),
        steps("Open this website in Safari.", "Tap the Share button (a square with an arrow pointing up).", "Scroll down and tap \"Add to Home Screen\".", "Tap \"Add\". An icon appears on your home screen."),
        h("p", null, h("strong", null, "Android (Chrome):")),
        steps("Open this website in Chrome.", "Tap the three dots in the top corner.", "Tap \"Install app\" or \"Add to Home screen\".", "Tap \"Install\"."),
        installBtn,
        h("p", { class: "fine" }, "There's no app store and no account. It's the same website, just one tap away.")),

      qa("Does it cost anything? Is it tracking me?",
        h("p", null, h("strong", null, "It's free, and it doesn't track you."), " There are no ads, no sign-up and no accounts. Nothing you do here is sent to us or anyone else."),
        h("p", null, "Anything you save (places, visits, notes, photos) stays on your own phone. ", h("a", { href: "#/about?section=privacy" }, "Full privacy details"))),

      qa("Why does it ask for my location?",
        h("p", null, "Only so it can show the places closest to you and tell you when you arrive at one. Your location stays on your phone and is never saved or sent anywhere."),
        h("p", null, "It only asks when you tap \"Find places near me\" or start a tour. You can say no and still use everything else.")),

      qa("What do the colored labels mean?",
        h("p", null, "Every place has a label saying how sure we are about its history:"),
        h("ul", { class: "legend plain" }, labels.map((level) =>
          h("li", { class: "legend-row" }, badge(level), h("p", null, PLAIN[level.id] || level.description + ".")))),
        h("p", null, h("a", { href: "#/about?section=badges" }, "More about how places were checked"))),

      qa("How do I take a driving tour?",
        steps(
          ["Tap ", h("strong", null, "Tours"), " at the bottom and pick a tour."],
          ["Tap ", h("strong", null, "Start this tour"), ", then ", h("strong", null, "Use my location"), "."],
          ["Tap ", h("strong", null, "Directions"), " to open your map app and drive to the next stop."],
          "When you arrive, the atlas notices and can read the story aloud. Then it shows the next stop."),
        h("p", null, "Want to see how it works from your couch first? Tap ", h("strong", null, "Try a pretend drive"), "."),
        h("p", { class: "fine" }, "Please let a passenger handle the phone while driving.")),

      qa("It stopped talking, or stopped noticing where I am.",
        h("p", null, "A website stops when your phone's screen turns off. Keep the atlas open with the screen on; most newer phones let it keep the screen awake for you during a tour."),
        h("p", null, "No sound? Turn up the volume. On an iPhone, also check the silent switch on the side. Some phones only allow talking after you tap a button once, so tap \"Read it to me\" on any place.")),

      qa("Does it work without internet?",
        h("p", null, "Yes, after you've opened it once with a connection. The stories, tours and the simple map work offline. To use the street map offline too, open ", h("strong", null, "Map"), " and tap \"Save map for offline\" while you're on Wi-Fi.")),

      qa("How do I keep my visits, notes and photos safe?",
        h("p", null, "They're saved on this phone only. If you delete the website's data or get a new phone, they're gone, unless you saved a backup."),
        steps(["Tap ", h("strong", null, "Saved"), " at the bottom."], ["Tap ", h("strong", null, "Save a backup copy"), "."], "Keep the file somewhere safe, like your Files, Google Drive or email it to yourself.", ["On a new phone, open Saved and tap ", h("strong", null, "Restore from a backup file"), "."]),
        h("p", { class: "fine" }, "On iPhone, adding the atlas to your home screen helps Safari keep your saved things.")),

      qa("Something is wrong, or I know a place that should be here.",
        h("p", null, "Thank you! On any place, scroll down and tap \"Suggest a correction\". To suggest a new place: ", ext(suggestSiteUrl(), "Suggest a place"), "."),
        h("p", { class: "fine" }, "These open GitHub, a free website where the project lives. It needs a free account, and what you write there is public. ", ext(ISSUES_URL, "Other ways to get in touch")))),

    h("p", { class: "help-about" }, h("a", { href: "#/about" }, "About this atlas: sources, licenses and privacy")));

  return { el, title: "Help", destroy() { size.destroy(); unsubscribe(); } };
}
