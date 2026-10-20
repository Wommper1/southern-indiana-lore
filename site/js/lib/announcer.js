// Optional "read it aloud when I arrive" for the Near me tab. It only works while this page is open.
// Each site is announced at most once per visit to the page. Nothing is stored.

import { distanceMeters } from "./geo.js";
import { speak, isSpeaking, speechSupported } from "./speech.js";
import { spokenText } from "./trust.js";

// GPS on the web can be coarse. If the fix is wider than this, we cannot tell which site you are at.
const MAX_ACCURACY_M = 150;

let enabled = false;
let atlas = null;
let levelTable = null;
let lastAnnounced = null;
const announced = new Set();
const listeners = new Set();

export function configure(nextAtlas, nextLevelTable) {
  atlas = nextAtlas;
  levelTable = nextLevelTable;
}

export function isEnabled() {
  return enabled;
}

export function onAnnounce(listener) {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

export function lastSite() {
  return lastAnnounced;
}

export function setEnabled(value) {
  enabled = Boolean(value) && speechSupported;
  if (!enabled) announced.clear();
}

/** Called on every new position. Returns the site that was announced, if any. */
export function handleFix(fix) {
  if (!enabled || !atlas || !fix || fix.accuracy > MAX_ACCURACY_M || isSpeaking()) return null;
  let best = null;
  for (const site of atlas.sites) {
    if (announced.has(site.id)) continue;
    const d = distanceMeters(fix.lat, fix.lon, site.latitude, site.longitude);
    if (d <= site.geofenceRadiusMeters && (!best || d < best.d)) best = { site, d };
  }
  if (!best) return null;
  announced.add(best.site.id);
  lastAnnounced = best.site;
  speak(spokenText(best.site, levelTable[best.site.confidenceLevel]));
  for (const listener of listeners) listener(best.site);
  return best.site;
}
