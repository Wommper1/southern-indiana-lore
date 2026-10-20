// Browse and search the sites; filter by county and by trust badge.

import { h, clear } from "../lib/dom.js";
import { filterSites, sortSites, countBy } from "../lib/filter.js";
import { distanceMeters, formatDistance } from "../lib/geo.js";
import * as position from "../lib/position.js";
import { badge } from "../ui.js";

function readCriteria(params) {
  return {
    q: params.get("q") || "",
    county: params.get("county") || "",
    levels: (params.get("level") || "").split(",").filter(Boolean),
    sort: params.get("sort") || "name",
  };
}

function writeCriteria(criteria) {
  const params = new URLSearchParams();
  if (criteria.q) params.set("q", criteria.q);
  if (criteria.county) params.set("county", criteria.county);
  if (criteria.levels.length) params.set("level", criteria.levels.join(","));
  if (criteria.sort && criteria.sort !== "name") params.set("sort", criteria.sort);
  const query = params.toString();
  // replaceState keeps the filters in the address (so Back returns to them) without re-rendering the whole page.
  history.replaceState(null, "", "#/" + (query ? "?" + query : ""));
}

export function renderList(ctx, params) {
  const criteria = readCriteria(params);
  const countyCounts = countBy(ctx.sites, "county");
  const levelCounts = countBy(ctx.sites, "confidenceLevel");

  const search = h("input", {
    id: "search",
    type: "search",
    class: "input",
    placeholder: "Search by name, place, or topic",
    autocomplete: "off",
    autocapitalize: "off",
    spellcheck: "false",
    enterkeyhint: "search",
    value: criteria.q,
    "aria-label": "Search sites",
  });
  const countySelect = h(
    "select",
    { id: "county", class: "input", "aria-label": "Filter by county" },
    h("option", { value: "" }, "All counties (" + countyCounts.size + ")"),
    [...countyCounts.keys()].sort().map((county) =>
      h("option", { value: county }, county + " (" + countyCounts.get(county) + ")")
    )
  );
  countySelect.value = criteria.county;
  const sortSelect = h("select", { id: "sort", class: "input", "aria-label": "Sort sites" });

  const chips = ctx.levelOrder
    .filter((level) => levelCounts.has(level.id))
    .map((level) => {
      const chip = h(
        "button",
        {
          type: "button",
          class: "chip",
          "data-level": level.id,
          "aria-pressed": String(criteria.levels.includes(level.id)),
          title: level.description,
        },
        h("span", { class: "chip-dot", "aria-hidden": "true" }),
        level.label,
        h("span", { class: "chip-count" }, String(levelCounts.get(level.id)))
      );
      chip.addEventListener("click", () => {
        const on = criteria.levels.includes(level.id);
        criteria.levels = on ? criteria.levels.filter((id) => id !== level.id) : criteria.levels.concat(level.id);
        chip.setAttribute("aria-pressed", String(!on));
        update();
      });
      return chip;
    });

  const count = h("p", { class: "result-count", role: "status", "aria-live": "polite" });
  const clearButton = h("button", { type: "button", class: "link-button" }, "Clear filters");
  const results = h("ul", { class: "card-list" });
  const empty = h("p", { class: "empty" }, "No sites match. Try a shorter search or clear the filters.");

  function distances() {
    const fix = position.getState().fix;
    if (!fix) return null;
    const map = new Map();
    for (const site of ctx.sites) map.set(site.id, distanceMeters(fix.lat, fix.lon, site.latitude, site.longitude));
    return map;
  }

  function fillSortOptions(hasFix) {
    clear(sortSelect);
    sortSelect.appendChild(h("option", { value: "name" }, "Sort: name"));
    sortSelect.appendChild(h("option", { value: "county" }, "Sort: county"));
    if (hasFix) sortSelect.appendChild(h("option", { value: "near" }, "Sort: nearest to me"));
    if (criteria.sort === "near" && !hasFix) criteria.sort = "name";
    sortSelect.value = criteria.sort;
  }

  function update() {
    const dist = distances();
    fillSortOptions(Boolean(dist));
    const filtered = filterSites(ctx.sites, criteria);
    const sorted = sortSites(filtered, criteria.sort, dist);
    clear(results);
    for (const site of sorted) {
      const level = ctx.levelTable[site.confidenceLevel];
      results.appendChild(
        h(
          "li",
          null,
          h(
            "a",
            { class: "card", href: "#/site/" + encodeURIComponent(site.id), "data-level": level.id },
            h("div", { class: "card-top" }, badge(level, { compact: true }),
              dist ? h("span", { class: "card-distance" }, formatDistance(dist.get(site.id))) : null),
            h("h3", { class: "card-title" }, site.title),
            h("p", { class: "card-sub" }, site.subtitle),
            h("p", { class: "card-meta" }, site.county + " · " + site.era)
          )
        )
      );
    }
    count.textContent = "Showing " + sorted.length + " of " + ctx.sites.length + " sites";
    empty.hidden = sorted.length > 0;
    const filtering = Boolean(criteria.q || criteria.county || criteria.levels.length);
    clearButton.hidden = !filtering;
    writeCriteria(criteria);
  }

  search.addEventListener("input", () => {
    criteria.q = search.value;
    update();
  });
  countySelect.addEventListener("change", () => {
    criteria.county = countySelect.value;
    update();
  });
  sortSelect.addEventListener("change", () => {
    criteria.sort = sortSelect.value;
    update();
  });
  clearButton.addEventListener("click", () => {
    criteria.q = "";
    criteria.county = "";
    criteria.levels = [];
    search.value = "";
    countySelect.value = "";
    for (const chip of chips) chip.setAttribute("aria-pressed", "false");
    update();
  });

  const unsubscribe = position.subscribe(() => update());

  const el = h(
    "div",
    { class: "view view-list" },
    h("h1", { class: "page-title" }, "Southern Indiana lore sites"),
    h(
      "p",
      { class: "lede" },
      ctx.sites.length + " sites in " + countyCounts.size + " counties. Every site shows how far its history can be trusted. ",
      h("a", { href: "#/about?section=badges" }, "What do the badges mean?")
    ),
    h("div", { class: "toolbar", role: "search" }, search, h("div", { class: "toolbar-row" }, countySelect, sortSelect)),
    h("div", { class: "chips", role: "group", "aria-label": "Filter by trust badge" }, chips),
    h("div", { class: "result-row" }, count, clearButton),
    results,
    empty
  );
  update();
  return { el, destroy: unsubscribe, title: "Sites" };
}
