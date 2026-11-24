// The Map tab. Shows the street map when this site has published one and the browser can draw it; otherwise the
// schematic pin map. Anyone can switch between them. ?tour=<id> shows one tour's stops, ?site=<id> centres a site.

import { h, clear } from "../lib/dom.js";
import * as basemap from "../lib/basemap.js";
import { renderSchematicMap } from "./map.js";
import { renderStreetMap } from "./streetmap.js";

export function renderMap(ctx, params) {
  const tourId = params.get("tour");
  const tour = tourId && ctx.tours ? ctx.tours.find((t) => t.id === tourId) || null : null;
  const slot = h("div", { class: "map-slot" }, h("p", { class: "loading" }, "Loading the map…"));
  const switcher = h("p", { class: "map-switch" });
  let inner = null;
  let destroyed = false;

  const linkFor = (view) => {
    const q = new URLSearchParams(params);
    if (view) q.set("view", view); else q.delete("view");
    const s = q.toString();
    return "#/map" + (s ? "?" + s : "");
  };

  function show(view) {
    if (destroyed) return;
    if (inner && inner.destroy) inner.destroy();
    inner = view;
    clear(slot);
    slot.appendChild(view.el);
    if (view.mounted) view.mounted();
  }

  function schematic(reason) {
    const note = reason ? h("p", { class: "notice" }, reason) : null;
    show(renderSchematicMap(ctx, params, note));
  }

  const el = h(
    "div",
    { class: "view view-map" },
    h("h1", { class: "page-title" }, tour ? tour.title : "Atlas map"),
    tour ? h("p", null, h("a", { href: "#/tour/" + encodeURIComponent(tour.id) }, "← Back to the tour")) : null,
    switcher,
    slot
  );

  return {
    el,
    title: tour ? tour.title + " map" : "Map",
    async mounted() {
      const info = await basemap.loadInfo();
      if (destroyed) return;
      const webgl = basemap.webglSupported();
      const wantSchematic = params.get("view") === "schematic";
      if (info && webgl) {
        switcher.appendChild(wantSchematic
          ? h("a", { href: linkFor(null) }, "Switch to the street map")
          : h("a", { href: linkFor("schematic") }, "Switch to the simple schematic map"));
      }
      if (wantSchematic) return schematic(null);
      if (!info) return schematic(navigator.onLine === false
        ? "You are offline and the street map is not saved on this device, so this is the simple schematic map."
        : null);
      if (!webgl) return schematic("This browser cannot draw the street map (it needs WebGL), so this is the simple schematic map.");
      if (navigator.onLine === false && !(await basemap.isSaved())) {
        return schematic("You are offline and the street map is not saved on this device, so this is the simple schematic map. Next time you are online, open the street map and tap \"Save map for offline\".");
      }
      show(renderStreetMap(ctx, params, info, {
        tour,
        onSchematic: () => schematic("The street map could not start in this browser, so this is the simple schematic map."),
      }));
    },
    destroy() {
      destroyed = true;
      if (inner && inner.destroy) inner.destroy();
    },
  };
}
