// Shared building blocks used by several views: the trust badge and the status card.

import { h, svg } from "./lib/dom.js";

// Each level gets a shape as well as a colour, so the badge and the map pins never rely on colour alone.
const SHAPE_BY_LEVEL = {
  OFFICIAL_SOURCE: "doc",
  VERIFIED: "doc",
  WELL_SUPPORTED: "doc",
  PLAUSIBLE: "plaus",
  PROBABLE: "plaus",
  ORAL_HISTORY: "lore",
  FOLKLORE: "lore",
  LEGEND: "lore",
  DISPUTED: "unver",
  DEBUNKED: "unver",
  UNVERIFIED: "unver",
};

export function shapeOf(levelId) {
  return SHAPE_BY_LEVEL[levelId] || "unver";
}

export const SHAPE_NAMES = {
  doc: "Documented",
  plaus: "Checked, partly confirmed",
  lore: "Folklore or oral tradition",
  unver: "Unverified or disputed",
};

/** A 12x12 icon for a shape. */
export function shapeIcon(shape, cls) {
  const attrs = { viewBox: "0 0 12 12", width: "12", height: "12", "aria-hidden": "true", focusable: "false", class: cls };
  if (shape === "doc") {
    return svg("svg", attrs, svg("circle", { cx: 6, cy: 6, r: 5.5, fill: "currentColor" }),
      svg("path", { d: "M3.2 6.3 5.2 8.2 8.9 4", fill: "none", stroke: "#06101c", "stroke-width": 1.6, "stroke-linecap": "round", "stroke-linejoin": "round" }));
  }
  if (shape === "plaus") {
    return svg("svg", attrs, svg("rect", { x: 0.8, y: 0.8, width: 10.4, height: 10.4, rx: 2.4, fill: "currentColor" }),
      svg("path", { d: "M3.2 6.3 5.2 8.2 8.9 4", fill: "none", stroke: "#06101c", "stroke-width": 1.6, "stroke-linecap": "round", "stroke-linejoin": "round" }));
  }
  if (shape === "lore") {
    return svg("svg", attrs, svg("path", { d: "M6 0.4 11.6 6 6 11.6 0.4 6Z", fill: "currentColor" }));
  }
  return svg("svg", attrs, svg("circle", { cx: 6, cy: 6, r: 4.8, fill: "none", stroke: "currentColor", "stroke-width": 1.6 }),
    svg("path", { d: "M4.9 4.7c0-1.6 2.3-1.6 2.3 0 0 1-1.1 1-1.1 2M6.1 8.3v.1", fill: "none", stroke: "currentColor", "stroke-width": 1.2, "stroke-linecap": "round" }));
}

/** The trust badge: colour, shape and the app's own label text. */
export function badge(level, options) {
  const compact = options && options.compact;
  return h(
    "span",
    { class: "badge" + (compact ? " badge-compact" : ""), "data-level": level.id },
    shapeIcon(shapeOf(level.id), "badge-icon"),
    h("span", { class: "badge-label" }, level.label)
  );
}

/** The "Epistemic confidence" card shown at the top of every site page. */
export function statusCard(site, level) {
  return h(
    "section",
    { class: "status-card", "data-level": level.id, "aria-label": "How far to trust this entry" },
    shapeIcon(shapeOf(level.id), "status-icon"),
    h(
      "div",
      { class: "status-body" },
      h("p", { class: "status-title" }, "Epistemic confidence: " + level.label),
      h("p", { class: "status-desc" }, level.description),
      site.verificationGrade ? h("p", { class: "status-note" }, h("strong", null, "Verification note: "), site.verificationGrade) : null
    )
  );
}

export function section(title, className, ...children) {
  return h("section", { class: "panel " + (className || "") }, h("h2", { class: "panel-title" }, title), ...children);
}
