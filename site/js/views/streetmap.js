// The street map: real roads, rivers and towns from OpenStreetMap, drawn on this device with MapLibre.
// The map file and every library are served from this site, so no map company learns where anyone looks.
// Pins keep the trust badge's colour and shape. Tours draw their stops in order.

import { h, clear } from "../lib/dom.js";
import { distanceMeters, formatDistance } from "../lib/geo.js";
import * as position from "../lib/position.js";
import * as basemap from "../lib/basemap.js";
import { badge, shapeOf, shapeIcon, SHAPE_NAMES } from "../ui.js";

let libsPromise = null;

/** Load MapLibre, the PMTiles reader and the style builder once, from this site's vendor folder. */
function loadLibs() {
  if (!libsPromise) {
    if (!document.querySelector("link[data-maplibre]")) {
      document.head.appendChild(h("link", { rel: "stylesheet", href: "vendor/maplibre-gl/maplibre-gl.css", "data-maplibre": "1" }));
    }
    libsPromise = Promise.all([
      import("../../vendor/maplibre-gl/maplibre-gl.mjs"),
      import("../../vendor/pmtiles/pmtiles.mjs"),
      import("../../vendor/protomaps-basemaps/basemaps.mjs"),
    ]).then(([maplibregl, pmtiles, basemaps]) => {
      const protocol = new pmtiles.Protocol();
      maplibregl.addProtocol("pmtiles", protocol.tile);
      return { maplibregl, basemaps };
    });
    libsPromise.catch(() => {
      libsPromise = null;
    });
  }
  return libsPromise;
}

const abs = (path) => new URL(path, document.baseURI).href;

function buildStyle(basemaps, info) {
  return {
    version: 8,
    glyphs: abs("vendor/basemap-assets/fonts/") + "{fontstack}/{range}.pbf",
    sprite: abs("vendor/basemap-assets/sprites/dark"),
    sources: {
      protomaps: {
        type: "vector",
        url: "pmtiles://" + abs(info.file),
        attribution: info.attribution || "© OpenStreetMap contributors · Protomaps",
      },
    },
    layers: basemaps.layers("protomaps", basemaps.namedFlavor("dark"), { lang: "en" }),
  };
}

function bounds(sites) {
  let w = Infinity, s = Infinity, e = -Infinity, n = -Infinity;
  for (const site of sites) {
    w = Math.min(w, site.longitude); e = Math.max(e, site.longitude);
    s = Math.min(s, site.latitude); n = Math.max(n, site.latitude);
  }
  return [[w, s], [e, n]];
}

/**
 * options.tour: { id, title, stops: [siteId...] } to draw a tour's stops in order.
 * options.onSchematic: called if the street map cannot start, so the caller can fall back.
 */
export function renderStreetMap(ctx, params, info, options) {
  const opts = options || {};
  const tour = opts.tour || null;
  const hiddenLevels = new Set();
  let map = null;
  let destroyed = false;
  let selected = null;
  const markers = new Map();

  const container = h("div", { class: "street-map", role: "region", "aria-label": "Street map of the lore sites. The site list has the same information." });
  const statusLine = h("p", { class: "fine", role: "status", "aria-live": "polite" }, "Loading the street map…");
  const card = h("div", { class: "map-card", "aria-live": "polite" });

  // ---- the selected-site card under the map ----
  function updateCard() {
    clear(card);
    if (!selected) {
      card.appendChild(h("p", { class: "fine" }, tour
        ? "Numbers show the tour order. Tap a pin to see the site. The dashed line joins the stops in order; it is not a driving route."
        : "Tap a pin to see the site. Drag to move, pinch or use + and − to zoom."));
      return;
    }
    const site = ctx.byId.get(selected);
    const level = ctx.levelTable[site.confidenceLevel];
    const fix = position.getState().fix;
    const dist = fix ? formatDistance(distanceMeters(fix.lat, fix.lon, site.latitude, site.longitude)) + " from you" : null;
    card.appendChild(
      h("div", { class: "map-card-main" },
        badge(level, { compact: true }),
        h("h2", { class: "map-card-title" }, site.title),
        h("p", { class: "card-meta" }, [site.county, dist].filter(Boolean).join(" · ")),
        h("a", { class: "btn btn-primary", href: "#/site/" + encodeURIComponent(site.id) }, "Open site page"))
    );
  }

  function select(id, fly) {
    selected = id;
    for (const [siteId, m] of markers) m.getElement().classList.toggle("selected", siteId === id);
    if (fly && map && id) {
      const site = ctx.byId.get(id);
      map.flyTo({ center: [site.longitude, site.latitude], zoom: Math.max(map.getZoom(), 13) });
    }
    updateCard();
  }

  // ---- pins: HTML buttons so they keep the badge's shape, take keyboard focus and read well to screen readers ----
  function makeMarkers(maplibregl) {
    const order = tour ? new Map(tour.stops.map((id, i) => [id, i + 1])) : null;
    for (const site of ctx.sites) {
      if (order && !order.has(site.id)) continue;
      const level = ctx.levelTable[site.confidenceLevel];
      const number = order ? order.get(site.id) : null;
      const el = h(
        "button",
        {
          type: "button",
          class: "mpin" + (number ? " mpin-numbered" : ""),
          "data-level": level.id,
          "aria-label": (number ? "Stop " + number + ": " : "") + site.title + ", " + site.county + ", " + level.label,
        },
        number ? h("span", { class: "mpin-num" }, String(number)) : shapeIcon(shapeOf(level.id), "mpin-shape")
      );
      el.addEventListener("click", (event) => {
        event.stopPropagation();
        select(site.id, false);
      });
      const marker = new maplibregl.Marker({ element: el }).setLngLat([site.longitude, site.latitude]).addTo(map);
      markers.set(site.id, marker);
    }
  }

  function applyFilter() {
    for (const [id, marker] of markers) {
      const hide = hiddenLevels.has(ctx.byId.get(id).confidenceLevel);
      marker.getElement().hidden = hide;
      if (hide && selected === id) select(null);
    }
  }

  // ---- your position (or the demo position), drawn as a dot with an accuracy ring ----
  const youGeo = () => {
    const fix = position.getState().fix;
    return {
      type: "FeatureCollection",
      features: fix ? [{ type: "Feature", geometry: { type: "Point", coordinates: [fix.lon, fix.lat] }, properties: { accuracy: fix.accuracy } }] : [],
    };
  };
  function syncYou() {
    if (!map || !map.getSource("you")) return;
    map.getSource("you").setData(youGeo());
    meBtn.hidden = !position.getState().fix;
    updateCard();
  }

  function addOverlays() {
    if (tour) {
      const coords = tour.stops.map((id) => ctx.byId.get(id)).filter(Boolean).map((s) => [s.longitude, s.latitude]);
      map.addSource("tour", { type: "geojson", data: { type: "Feature", geometry: { type: "LineString", coordinates: coords }, properties: {} } });
      map.addLayer({ id: "tour-line", type: "line", source: "tour", paint: { "line-color": "#fbbf24", "line-width": 3, "line-dasharray": [2, 1.5], "line-opacity": 0.85 } });
    }
    map.addSource("you", { type: "geojson", data: youGeo() });
    map.addLayer({ id: "you-acc", type: "circle", source: "you", paint: { "circle-radius": 18, "circle-color": "#38bdf8", "circle-opacity": 0.18 } });
    map.addLayer({ id: "you-dot", type: "circle", source: "you", paint: { "circle-radius": 7, "circle-color": "#38bdf8", "circle-stroke-color": "#ffffff", "circle-stroke-width": 2 } });
  }

  const fitSites = () => (tour ? tour.stops.map((id) => ctx.byId.get(id)).filter(Boolean) : ctx.sites);
  const fitBtn = h("button", { type: "button", class: "map-btn map-btn-wide", onclick: () => map && map.fitBounds(bounds(fitSites()), { padding: 40 }) }, tour ? "Whole tour" : "Fit all");
  const meBtn = h("button", { type: "button", class: "map-btn map-btn-wide", onclick: () => {
    const fix = position.getState().fix;
    if (fix && map) map.flyTo({ center: [fix.lon, fix.lat], zoom: Math.max(map.getZoom(), 14) });
  } }, "Center on me");
  meBtn.hidden = !position.getState().fix;
  const controls = h("div", { class: "map-controls" }, fitBtn, meBtn);

  // ---- legend filter, same as the schematic map ----
  const levelCounts = new Map();
  for (const site of fitSites()) levelCounts.set(site.confidenceLevel, (levelCounts.get(site.confidenceLevel) || 0) + 1);
  const legend = h(
    "div",
    { class: "chips map-legend", role: "group", "aria-label": "Show or hide pins by trust badge" },
    ctx.levelOrder.filter((l) => levelCounts.has(l.id)).map((level) => {
      const chip = h("button", { type: "button", class: "chip", "data-level": level.id, "aria-pressed": "true", title: level.description },
        shapeIcon(shapeOf(level.id), "chip-shape"), level.label, h("span", { class: "chip-count" }, String(levelCounts.get(level.id))));
      chip.addEventListener("click", () => {
        const on = chip.getAttribute("aria-pressed") === "true";
        chip.setAttribute("aria-pressed", String(!on));
        if (on) hiddenLevels.add(level.id); else hiddenLevels.delete(level.id);
        applyFilter();
      });
      return chip;
    })
  );
  const shapeKey = h("p", { class: "fine" }, "Shapes repeat the colors: ",
    Object.keys(SHAPE_NAMES).map((shape, index) => [index ? " · " : "", h("span", { class: "shape-key" }, shapeIcon(shape, "key-icon"), " " + SHAPE_NAMES[shape])]));

  // ---- save for offline ----
  const offlineNote = h("p", { class: "fine", role: "status", "aria-live": "polite" });
  const saveBtn = h("button", { type: "button", class: "btn" }, "Save map for offline (" + basemap.formatBytes(info.bytes) + ")");
  const removeBtn = h("button", { type: "button", class: "btn" }, "Remove saved map");
  const progress = h("progress", { max: "1", value: "0", hidden: true, "aria-label": "Map download progress" });
  async function syncOffline() {
    if (!basemap.canSave()) {
      saveBtn.hidden = true;
      removeBtn.hidden = true;
      offlineNote.textContent = "This browser cannot keep the street map offline. Without a connection the atlas falls back to the schematic map.";
      return;
    }
    const saved = await basemap.isSaved();
    if (destroyed) return;
    saveBtn.hidden = saved;
    removeBtn.hidden = !saved;
    offlineNote.textContent = saved
      ? "The street map is saved on this device and works with no connection."
      : "Without saving, the street map needs a connection. Saving downloads the whole southern Indiana map once.";
  }
  saveBtn.addEventListener("click", async () => {
    saveBtn.disabled = true;
    progress.hidden = false;
    offlineNote.textContent = "Downloading the map…";
    try {
      await basemap.save((f) => { progress.value = f; });
    } catch (error) {
      offlineNote.textContent = "The download did not finish. Check your connection and try again.";
    }
    saveBtn.disabled = false;
    progress.hidden = true;
    syncOffline();
  });
  removeBtn.addEventListener("click", async () => {
    await basemap.remove();
    syncOffline();
  });

  const built = info.built ? " Map data as of " + info.built + "." : "";
  const el = h(
    "div",
    { class: "view-map-street" },
    h("p", { class: "lede" },
      tour ? "The stops of " + tour.title + " on the street map." : "Street map with every lore site. Pins are approximate, so confirm before you go.",
      " The map is served from this site; no map company sees where you look."),
    h("div", { class: "map-wrap map-wrap-street" }, container, controls),
    statusLine,
    card,
    h("h2", { class: "panel-title" }, "Pins"),
    legend,
    shapeKey,
    h("section", { class: "panel" },
      h("h2", { class: "panel-title" }, "Offline map"),
      offlineNote, progress,
      h("div", { class: "actions" }, saveBtn, removeBtn),
      h("p", { class: "fine" }, "Map data © OpenStreetMap contributors (ODbL), packaged by Protomaps." + built))
  );

  const unsubscribe = position.subscribe(syncYou);

  return {
    el,
    mounted() {
      updateCard();
      syncOffline();
      loadLibs()
        .then(({ maplibregl, basemaps }) => {
          if (destroyed) return;
          map = new maplibregl.Map({
            container,
            style: buildStyle(basemaps, info),
            bounds: bounds(fitSites()),
            fitBoundsOptions: { padding: 40 },
            maxZoom: 17,
            attributionControl: { compact: true },
            dragRotate: false,
            pitchWithRotate: false,
          });
          map.touchZoomRotate.disableRotation();
          map.addControl(new maplibregl.NavigationControl({ showCompass: false }), "top-right");
          map.addControl(new maplibregl.ScaleControl({ unit: "imperial" }), "bottom-left");
          map.on("load", () => {
            if (destroyed) return;
            statusLine.textContent = "";
            addOverlays();
            makeMarkers(maplibregl);
            const want = params.get("site");
            if (want && ctx.byId.has(want)) {
              const site = ctx.byId.get(want);
              map.jumpTo({ center: [site.longitude, site.latitude], zoom: 14 });
              select(want, false);
            }
          });
          map.on("click", () => select(null));
          map.on("error", () => {
            if (!map.loaded()) statusLine.textContent = "Some map pieces could not load. If you are offline, save the map next time you have a connection.";
          });
        })
        .catch(() => {
          if (destroyed) return;
          if (opts.onSchematic) opts.onSchematic();
          else statusLine.textContent = "The street map could not start in this browser.";
        });
    },
    destroy() {
      destroyed = true;
      unsubscribe();
      if (map) map.remove();
    },
  };
}
