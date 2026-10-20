// Southern Indiana Lore Atlas: entry point. Loads the data, then shows one view at a time based on the address
// after the "#" (for example #/site/floyd_culbertson_mansion), so it works from any folder with no server rules.

import { h, clear } from "./lib/dom.js";
import { resolveLevel } from "./lib/trust.js";
import { indexSites } from "./lib/filter.js";
import { makeProjection } from "./lib/geo.js";
import { APP_NAME } from "./lib/config.js";
import * as position from "./lib/position.js";
import * as announcer from "./lib/announcer.js";
import * as offline from "./lib/offline.js";
import { renderList } from "./views/list.js";
import { renderDetail } from "./views/detail.js";
import { renderMap } from "./views/map.js";
import { renderNear } from "./views/near.js";
import { renderAbout } from "./views/about.js";

const app = document.getElementById("app");
const main = document.getElementById("main");
let ctx = null;
let current = null;
let navigations = 0;

function parseHash() {
  const raw = location.hash.replace(/^#/, "") || "/";
  const [path, query = ""] = raw.split("?");
  const parts = path.split("/").filter(Boolean);
  let arg = parts[1] || "";
  try {
    arg = decodeURIComponent(arg);
  } catch (error) {
    /* keep the raw text */
  }
  return { name: parts[0] || "list", arg, params: new URLSearchParams(query) };
}

function showError(title, message) {
  clear(app);
  app.appendChild(
    h("div", { class: "view" }, h("h1", { class: "page-title" }, title), h("p", null, message),
      h("p", null, h("button", { type: "button", class: "btn", onclick: () => location.reload() }, "Try again")))
  );
}

function setActiveTab(name) {
  const tab = { list: "list", site: "list", map: "map", near: "near", about: "about" }[name] || "list";
  for (const link of document.querySelectorAll(".tabbar a")) {
    const active = link.dataset.tab === tab;
    if (active) link.setAttribute("aria-current", "page");
    else link.removeAttribute("aria-current");
  }
}

function route() {
  if (!ctx) return;
  if (current && current.destroy) current.destroy();
  const { name, arg, params } = parseHash();
  let view;
  try {
    if (name === "site") view = renderDetail(ctx, arg, navigations > 0);
    else if (name === "map") view = renderMap(ctx, params);
    else if (name === "near") view = renderNear(ctx);
    else if (name === "about") view = renderAbout(ctx, params);
    else view = renderList(ctx, params);
  } catch (error) {
    console.error(error);
    current = null;
    showError("Something went wrong", "This page could not be shown. Reloading usually fixes it.");
    return;
  }
  current = view;
  clear(app);
  app.appendChild(view.el);
  if (view.mounted) view.mounted();
  document.title = (view.title ? view.title + " · " : "") + APP_NAME;
  setActiveTab(name);
  navigations += 1;
  if (!(name === "about" && params.get("section"))) window.scrollTo(0, 0);
  const heading = view.el.querySelector("h1");
  if (heading) {
    heading.setAttribute("tabindex", "-1");
    heading.focus({ preventScroll: true });
  }
}

// ---- the "location on" pill, so it is always obvious when the page is following your position ----
function wireLocationPill() {
  const pill = document.getElementById("location-pill");
  const stop = document.getElementById("location-stop");
  const text = document.getElementById("location-text");
  const sync = () => {
    const state = position.getState();
    const on = position.isActive();
    pill.hidden = !on;
    text.textContent = state.demo ? "Demo" : "Location on";
  };
  stop.addEventListener("click", () => position.stop());
  position.subscribe(sync);
  sync();
}

function wireFooterStatus() {
  const el = document.getElementById("offline-status");
  const sync = () => {
    const state = offline.getState();
    el.textContent = state.status === "ready" ? "Offline copy saved" : state.status === "saving" ? "Saving offline copy…" : "";
  };
  offline.subscribe(sync);
  sync();
}

async function boot() {
  document.getElementById("skip-link").addEventListener("click", (event) => {
    event.preventDefault();
    main.focus();
  });
  wireFooterStatus();
  offline.register();

  let atlas;
  try {
    const response = await fetch("data/atlas.json");
    if (!response.ok) throw new Error("HTTP " + response.status);
    atlas = await response.json();
  } catch (error) {
    showError("The atlas data could not be loaded", "Check your connection and try again. Once the atlas has loaded once, it works offline.");
    return;
  }

  const levelOrder = atlas.confidenceLevels;
  const levelTable = Object.fromEntries(levelOrder.map((level) => [level.id, level]));
  // Fail closed again in the browser: a level the table does not know becomes Unverified.
  for (const site of atlas.sites) site.confidenceLevel = resolveLevel(site.confidenceLevel, levelTable).id;
  const sites = indexSites(atlas.sites);
  ctx = {
    atlas,
    sites,
    byId: new Map(sites.map((site) => [site.id, site])),
    levelOrder,
    levelTable,
    projection: makeProjection(sites),
  };

  announcer.configure(atlas, levelTable);
  position.subscribe((state) => {
    if (state.fix) announcer.handleFix(state.fix);
  });
  wireLocationPill();

  if ("scrollRestoration" in history) history.scrollRestoration = "manual";
  window.addEventListener("hashchange", route);
  route();
}

boot();
