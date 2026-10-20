// A schematic pin map drawn in SVG. No tiles, no network, no third-party code.
// Pins sit on a plain latitude/longitude grid. No roads, rivers, or borders are drawn, so nothing here is
// presented as surveyed geography. It is not a navigation map.

import { h, svg, clear } from "../lib/dom.js";
import { distanceMeters, formatDistance, METERS_PER_DEGREE_LAT, niceLength, formatScale } from "../lib/geo.js";
import * as position from "../lib/position.js";
import { badge, shapeOf, shapeIcon, SHAPE_NAMES } from "../ui.js";

const PAD = 28; // px of breathing room around the fitted data
const TAP_SLOP = 6; // px a finger may wobble and still count as a tap
const NEIGHBOR_PX = 24; // pins this close on screen are listed together under the selected pin
const GRID_STEPS = [0.01, 0.02, 0.05, 0.1, 0.25, 0.5, 1, 2];

function pinShape(shape) {
  if (shape === "doc") return svg("circle", { class: "shape-fill", r: 7 });
  if (shape === "plaus") return svg("rect", { class: "shape-fill", x: -6.5, y: -6.5, width: 13, height: 13, rx: 3 });
  if (shape === "lore") return svg("path", { class: "shape-fill", d: "M0 -9 9 0 0 9 -9 0Z" });
  return svg("circle", { class: "shape-ring", r: 6.5 });
}

function degreesLabel(value, positive, negative, step) {
  const decimals = step >= 1 ? 0 : step >= 0.5 ? 1 : 2;
  return Math.abs(value).toFixed(decimals) + "°" + (value >= 0 ? positive : negative);
}

export function renderMap(ctx, params) {
  const proj = ctx.projection;
  const world = new Map(ctx.sites.map((site) => [site.id, proj.project(site.latitude, site.longitude)]));
  const hiddenLevels = new Set();
  let W = 0;
  let H = 0;
  let view = { cx: proj.width / 2, cy: proj.height / 2, scale: 1 };
  let fitScale = 1;
  let selected = null;
  let moved = false; // true after a drag, so the click that ends it is ignored

  // ---- structure ----
  const bg = svg("rect", { class: "map-bg" });
  const gridLayer = svg("g", { class: "map-grid", "aria-hidden": "true" });
  const countyLayer = svg("g", { class: "map-counties", "aria-hidden": "true" });
  const pinLayer = svg("g", { class: "map-pins" });
  const youLayer = svg("g", { class: "map-you" });
  const scaleLayer = svg("g", { class: "map-scale", "aria-hidden": "true" });
  const mapSvg = svg(
    "svg",
    { class: "map", role: "group", "aria-label": "Schematic map of " + ctx.sites.length + " lore sites. The site list has the same information." },
    bg, gridLayer, countyLayer, pinLayer, youLayer, scaleLayer
  );

  const pins = new Map();
  for (const site of ctx.sites) {
    const level = ctx.levelTable[site.confidenceLevel];
    const g = svg(
      "g",
      {
        class: "pin",
        "data-level": level.id,
        tabindex: "0",
        role: "button",
        "aria-label": site.title + ", " + site.county + ", " + level.label,
      },
      svg("circle", { class: "pin-hit", r: 16 }),
      svg("circle", { class: "pin-ring", r: 13 }),
      pinShape(shapeOf(level.id))
    );
    g.addEventListener("click", () => {
      if (!moved) select(site.id);
    });
    g.addEventListener("keydown", (event) => {
      if (event.key === "Enter" || event.key === " ") {
        event.preventDefault();
        select(site.id);
      }
    });
    pins.set(site.id, g);
    pinLayer.appendChild(g);
  }

  // County labels sit at the middle of each county's pins.
  const countyCentroids = [];
  {
    const sums = new Map();
    for (const site of ctx.sites) {
      const p = world.get(site.id);
      const s = sums.get(site.county) || { x: 0, y: 0, n: 0 };
      s.x += p.x; s.y += p.y; s.n += 1;
      sums.set(site.county, s);
    }
    for (const [county, s] of sums) countyCentroids.push({ name: county.replace(/ County$/, ""), x: s.x / s.n, y: s.y / s.n, n: s.n });
    countyCentroids.sort((a, b) => b.n - a.n);
  }

  // ---- coordinate helpers ----
  const toScreen = (p) => ({ x: (p.x - view.cx) * view.scale + W / 2, y: H / 2 - (p.y - view.cy) * view.scale });
  const toWorld = (sx, sy) => ({ x: view.cx + (sx - W / 2) / view.scale, y: view.cy - (sy - H / 2) / view.scale });

  function clampView() {
    const mx = proj.width * 0.3 + 0.05;
    const my = proj.height * 0.3 + 0.05;
    view.cx = Math.min(proj.width + mx, Math.max(-mx, view.cx));
    view.cy = Math.min(proj.height + my, Math.max(-my, view.cy));
    view.scale = Math.min(fitScale * 80, Math.max(fitScale * 0.7, view.scale));
  }

  function fit() {
    fitScale = Math.min((W - 2 * PAD) / Math.max(proj.width, 0.01), (H - 2 * PAD) / Math.max(proj.height, 0.01));
    view = { cx: proj.width / 2, cy: proj.height / 2, scale: fitScale };
  }

  function zoomAt(factor, sx, sy) {
    const anchor = toWorld(sx, sy);
    view.scale *= factor;
    clampView();
    view.cx = anchor.x - (sx - W / 2) / view.scale;
    view.cy = anchor.y + (sy - H / 2) / view.scale;
    clampView();
    draw();
  }

  // ---- drawing ----
  function drawGrid() {
    clear(gridLayer);
    const tl = toWorld(0, 0);
    const br = toWorld(W, H);
    const topLat = proj.unproject(0, tl.y).lat;
    const botLat = proj.unproject(0, br.y).lat;
    const leftLon = proj.unproject(tl.x, 0).lon;
    const rightLon = proj.unproject(br.x, 0).lon;
    const latStep = GRID_STEPS.find((s) => s * view.scale >= 70) || 2;
    const lonStep = GRID_STEPS.find((s) => s * proj.k * view.scale >= 70) || 2;
    for (let lat = Math.ceil(botLat / latStep) * latStep; lat <= topLat; lat += latStep) {
      const y = toScreen(proj.project(lat, proj.minLon)).y;
      gridLayer.appendChild(svg("line", { x1: 0, x2: W, y1: y, y2: y }));
      gridLayer.appendChild(svg("text", { x: 6, y: y - 4 }, degreesLabel(lat, "N", "S", latStep)));
    }
    for (let lon = Math.ceil(leftLon / lonStep) * lonStep; lon <= rightLon; lon += lonStep) {
      const x = toScreen(proj.project(proj.minLat, lon)).x;
      gridLayer.appendChild(svg("line", { x1: x, x2: x, y1: 0, y2: H }));
      gridLayer.appendChild(svg("text", { x: x + 4, y: H - 8 }, degreesLabel(lon, "E", "W", lonStep)));
    }
  }

  function drawCounties() {
    clear(countyLayer);
    const placed = [];
    for (const c of countyCentroids) {
      const p = toScreen(c);
      const width = c.name.length * 6.2 + 6;
      const box = { x1: p.x - width / 2, x2: p.x + width / 2, y1: p.y - 34, y2: p.y - 20 };
      if (box.x2 < 0 || box.x1 > W || box.y2 < 0 || box.y1 > H) continue;
      if (placed.some((o) => box.x1 < o.x2 && box.x2 > o.x1 && box.y1 < o.y2 && box.y2 > o.y1)) continue;
      placed.push(box);
      countyLayer.appendChild(svg("text", { x: p.x, y: p.y - 24, "text-anchor": "middle" }, c.name));
    }
  }

  function drawScale() {
    clear(scaleLayer);
    const metersPerPx = METERS_PER_DEGREE_LAT / view.scale;
    const meters = niceLength(metersPerPx * 110);
    const px = meters / metersPerPx;
    const x = 58; // right of the latitude labels
    const y = H - 44; // above the longitude labels, clear of the latitude labels on the left edge
    scaleLayer.appendChild(svg("path", { d: "M" + x + " " + (y - 5) + "V" + y + "H" + (x + px) + "V" + (y - 5) }));
    scaleLayer.appendChild(svg("text", { x: x + px / 2, y: y + 14, "text-anchor": "middle" }, formatScale(meters)));
    scaleLayer.appendChild(svg("text", { class: "north", x: W - 20, y: 26, "text-anchor": "middle" }, "N ↑"));
  }

  function drawYou() {
    clear(youLayer);
    const fix = position.getState().fix;
    if (!fix) return;
    const p = toScreen(proj.project(fix.lat, fix.lon));
    const accuracyPx = Math.min(200, (fix.accuracy / METERS_PER_DEGREE_LAT) * view.scale);
    youLayer.appendChild(svg("circle", { class: "you-acc", cx: p.x, cy: p.y, r: Math.max(accuracyPx, 8) }));
    youLayer.appendChild(svg("circle", { class: "you-dot", cx: p.x, cy: p.y, r: 6 }));
    youLayer.appendChild(svg("text", { class: "you-label", x: p.x + 10, y: p.y + 4 }, position.getState().demo ? "Demo location" : "You"));
  }

  function draw() {
    if (!W || !H) return;
    bg.setAttribute("width", W);
    bg.setAttribute("height", H);
    mapSvg.setAttribute("viewBox", "0 0 " + W + " " + H);
    drawGrid();
    drawCounties();
    for (const site of ctx.sites) {
      const g = pins.get(site.id);
      const hidden = hiddenLevels.has(site.confidenceLevel);
      g.style.display = hidden ? "none" : "";
      const p = toScreen(world.get(site.id));
      g.setAttribute("transform", "translate(" + p.x.toFixed(1) + " " + p.y.toFixed(1) + ")");
    }
    drawYou();
    drawScale();
    updateCard();
  }

  // ---- selection card ----
  const card = h("div", { class: "map-card", "aria-live": "polite" });

  function neighborsOf(siteId) {
    const here = toScreen(world.get(siteId));
    return ctx.sites
      .filter((s) => s.id !== siteId && !hiddenLevels.has(s.confidenceLevel))
      .map((s) => {
        const p = toScreen(world.get(s.id));
        return { site: s, d: Math.hypot(p.x - here.x, p.y - here.y) };
      })
      .filter((n) => n.d <= NEIGHBOR_PX)
      .sort((a, b) => a.d - b.d)
      .map((n) => n.site);
  }

  function updateCard() {
    clear(card);
    if (!selected) {
      card.appendChild(h("p", { class: "fine" }, "Tap a pin to see the site. Drag to move the map, pinch or use + and − to zoom. Overlapping pins are listed together under the one you tap."));
      return;
    }
    const site = ctx.byId.get(selected);
    const level = ctx.levelTable[site.confidenceLevel];
    const fix = position.getState().fix;
    const dist = fix ? formatDistance(distanceMeters(fix.lat, fix.lon, site.latitude, site.longitude)) + " from you" : null;
    const near = neighborsOf(selected);
    card.appendChild(
      h(
        "div",
        { class: "map-card-main" },
        badge(level, { compact: true }),
        h("h2", { class: "map-card-title" }, site.title),
        h("p", { class: "card-meta" }, [site.county, dist].filter(Boolean).join(" · ")),
        h("a", { class: "btn btn-primary", href: "#/site/" + encodeURIComponent(site.id) }, "Open site page")
      )
    );
    if (near.length) {
      card.appendChild(
        h(
          "div",
          { class: "map-neighbors" },
          h("p", { class: "fine" }, "Also here on the map:"),
          near.map((s) =>
            h("button", { type: "button", class: "chip", "data-level": s.confidenceLevel, onclick: () => select(s.id) },
              h("span", { class: "chip-dot", "aria-hidden": "true" }), s.title)
          )
        )
      );
    }
  }

  function select(id, options) {
    selected = id;
    for (const [siteId, g] of pins) g.classList.toggle("selected", siteId === id);
    if (id) pinLayer.appendChild(pins.get(id)); // draw the chosen pin on top
    if (options && options.center) {
      const p = world.get(id);
      view.cx = p.x;
      view.cy = p.y;
      view.scale = Math.max(view.scale, fitScale * 14);
      clampView();
    }
    draw();
  }

  // ---- gestures ----
  const pointers = new Map();
  let lastPinch = null;

  mapSvg.addEventListener("pointerdown", (event) => {
    pointers.set(event.pointerId, { x: event.clientX, y: event.clientY, sx: event.clientX, sy: event.clientY });
    if (pointers.size === 1) moved = false;
    lastPinch = null;
  });
  mapSvg.addEventListener("pointermove", (event) => {
    const p = pointers.get(event.pointerId);
    if (!p) return;
    const dx = event.clientX - p.x;
    const dy = event.clientY - p.y;
    const rect = mapSvg.getBoundingClientRect();
    if (pointers.size === 1) {
      if (!moved && Math.hypot(event.clientX - p.sx, event.clientY - p.sy) < TAP_SLOP) return;
      if (!moved) {
        moved = true;
        try { mapSvg.setPointerCapture(event.pointerId); } catch (error) { /* ignore */ }
      }
      view.cx -= dx / view.scale;
      view.cy += dy / view.scale;
      clampView();
      p.x = event.clientX;
      p.y = event.clientY;
      draw();
    } else if (pointers.size === 2) {
      moved = true;
      p.x = event.clientX;
      p.y = event.clientY;
      const [a, b] = [...pointers.values()];
      const dist = Math.hypot(a.x - b.x, a.y - b.y);
      const midX = (a.x + b.x) / 2 - rect.left;
      const midY = (a.y + b.y) / 2 - rect.top;
      if (lastPinch && dist > 0) zoomAt(dist / lastPinch.dist, midX, midY);
      lastPinch = { dist };
    }
  });
  function endPointer(event) {
    pointers.delete(event.pointerId);
    lastPinch = null;
    // The click that follows a drag must not select a pin; clear the flag after that click has been delivered.
    if (moved && pointers.size === 0) setTimeout(() => { moved = false; }, 0);
  }
  mapSvg.addEventListener("pointerup", endPointer);
  mapSvg.addEventListener("pointercancel", endPointer);
  mapSvg.addEventListener(
    "wheel",
    (event) => {
      event.preventDefault();
      const rect = mapSvg.getBoundingClientRect();
      zoomAt(event.deltaY < 0 ? 1.25 : 0.8, event.clientX - rect.left, event.clientY - rect.top);
    },
    { passive: false }
  );

  // ---- controls and legend ----
  const zoomIn = h("button", { type: "button", class: "map-btn", "aria-label": "Zoom in", onclick: () => zoomAt(1.6, W / 2, H / 2) }, "+");
  const zoomOut = h("button", { type: "button", class: "map-btn", "aria-label": "Zoom out", onclick: () => zoomAt(1 / 1.6, W / 2, H / 2) }, "−");
  const fitBtn = h("button", { type: "button", class: "map-btn map-btn-wide", onclick: () => { fit(); draw(); } }, "Fit all");
  const meBtn = h("button", { type: "button", class: "map-btn map-btn-wide", onclick: () => {
    const fix = position.getState().fix;
    if (!fix) return;
    const p = proj.project(fix.lat, fix.lon);
    view.cx = p.x;
    view.cy = p.y;
    view.scale = Math.max(view.scale, fitScale * 6);
    clampView();
    draw();
  } }, "Center on me");
  const controls = h("div", { class: "map-controls" }, zoomIn, zoomOut, fitBtn, meBtn);

  const levelCounts = new Map();
  for (const site of ctx.sites) levelCounts.set(site.confidenceLevel, (levelCounts.get(site.confidenceLevel) || 0) + 1);
  const legend = h(
    "div",
    { class: "chips map-legend", role: "group", "aria-label": "Show or hide pins by trust badge" },
    ctx.levelOrder.filter((l) => levelCounts.has(l.id)).map((level) => {
      const chip = h(
        "button",
        { type: "button", class: "chip", "data-level": level.id, "aria-pressed": "true", title: level.description },
        shapeIcon(shapeOf(level.id), "chip-shape"),
        level.label,
        h("span", { class: "chip-count" }, String(levelCounts.get(level.id)))
      );
      chip.addEventListener("click", () => {
        const on = chip.getAttribute("aria-pressed") === "true";
        chip.setAttribute("aria-pressed", String(!on));
        if (on) hiddenLevels.add(level.id); else hiddenLevels.delete(level.id);
        if (selected && hiddenLevels.has(ctx.byId.get(selected).confidenceLevel)) selected = null;
        draw();
      });
      return chip;
    })
  );

  const shapeKey = h(
    "p",
    { class: "fine" },
    "Shapes repeat the colors: ",
    // The separators sit outside the no-wrap spans so the line can wrap between items on a narrow phone.
    Object.keys(SHAPE_NAMES).map((shape, index) => [
      index ? " · " : "",
      h("span", { class: "shape-key" }, shapeIcon(shape, "key-icon"), " " + SHAPE_NAMES[shape]),
    ])
  );

  const wrap = h("div", { class: "map-wrap" }, mapSvg, controls);
  const el = h(
    "div",
    { class: "view view-map" },
    h("h1", { class: "page-title" }, "Atlas map"),
    h("p", { class: "lede" }, "A schematic pin map. It draws no roads, rivers, or borders and loads no map tiles, so it is not for navigation. Use it to see how the sites are spread out."),
    wrap,
    card,
    h("h2", { class: "panel-title" }, "Pins"),
    legend,
    shapeKey
  );

  // Redraw the "you" dot and distances when the position changes.
  const unsubscribe = position.subscribe(() => {
    meBtn.hidden = !position.getState().fix;
    draw();
  });
  meBtn.hidden = !position.getState().fix;

  let observer = null;
  function measure() {
    const rect = wrap.getBoundingClientRect();
    const nextW = Math.round(rect.width);
    const nextH = Math.round(rect.height);
    if (!nextW || !nextH || (nextW === W && nextH === H)) return;
    const first = !W;
    W = nextW;
    H = nextH;
    if (first) {
      fit();
      const want = params.get("site");
      if (want && ctx.byId.has(want)) {
        select(want, { center: true });
        return;
      }
    } else {
      fitScale = Math.min((W - 2 * PAD) / Math.max(proj.width, 0.01), (H - 2 * PAD) / Math.max(proj.height, 0.01));
    }
    draw();
  }

  return {
    el,
    title: "Map",
    mounted() {
      measure();
      if (typeof ResizeObserver === "function") {
        observer = new ResizeObserver(measure);
        observer.observe(wrap);
      } else {
        window.addEventListener("resize", measure);
      }
    },
    destroy() {
      unsubscribe();
      if (observer) observer.disconnect();
      window.removeEventListener("resize", measure);
    },
  };
}
