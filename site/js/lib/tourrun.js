// A running tour: which stop is next, which have been reached, and the optional simulated drive.
// It keeps going while you open site pages, as long as the atlas stays open. Nothing here is saved; reaching a stop
// can optionally stamp your passport (lib/journal.js stores only the date and a yes/no "at the site").

import { distanceMeters } from "./geo.js";
import * as position from "./position.js";
import * as journal from "./journal.js";

const MAX_ACCURACY_M = 150;
const SIM_STEP_MS = 2500; // the simulated car moves every 2.5 seconds
const SIM_STEPS_BETWEEN = 3; // in-between positions on the way to each stop, so arrivals happen one by one

let state = null; // { tour, sites, reached: Set, stampPassport, simulating }
let simTimer = null;
const listeners = new Set();

function emit() {
  for (const listener of listeners) listener(state);
}

export function subscribe(listener) {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

export function current() {
  return state;
}

export function nextStop() {
  if (!state) return null;
  return state.sites.find((site) => !state.reached.has(site.id)) || null;
}

/** Start following a tour. sites: the stop site objects in order. */
export function start(tour, sites, options) {
  stopSimulation();
  state = { tour, sites, reached: new Set(), stampPassport: Boolean(options && options.stampPassport), simulating: false };
  emit();
  const fix = position.getState().fix;
  if (fix) handleFix(fix);
}

export function end() {
  stopSimulation();
  state = null;
  emit();
}

export function setStampPassport(value) {
  if (state) {
    state.stampPassport = Boolean(value);
    emit();
  }
}

/** Mark a stop reached by hand ("skip" or "I'm here"). */
export function markReached(siteId) {
  if (!state) return;
  state.reached.add(siteId);
  emit();
}

function handleFix(fix) {
  if (!state || !fix || fix.accuracy > MAX_ACCURACY_M) return;
  let changed = false;
  for (const site of state.sites) {
    if (state.reached.has(site.id)) continue;
    if (distanceMeters(fix.lat, fix.lon, site.latitude, site.longitude) <= site.geofenceRadiusMeters) {
      state.reached.add(site.id);
      changed = true;
      // A real arrival can stamp the passport. A demo or simulated drive never does.
      if (state.stampPassport && !position.getState().demo && journal.supported()) {
        journal.get(site.id).then((entry) => {
          if (!entry.visitedAt) journal.setVisited(site.id, true, true);
        });
      }
    }
  }
  if (changed) emit();
}

position.subscribe((s) => {
  if (s.status === "off" && simTimer) stopSimulation(); // the "Stop" button in the top bar ends a simulated drive too
  if (s.fix) handleFix(s.fix);
});

// ---------- simulated drive (for trying a tour from a desk) ----------

/** The pretend positions: a few points on the way to each stop, then the stop itself. */
export function simulationPath(sites) {
  const path = [];
  for (let i = 0; i < sites.length; i += 1) {
    const to = sites[i];
    if (i > 0) {
      const from = sites[i - 1];
      for (let k = 1; k <= SIM_STEPS_BETWEEN; k += 1) {
        const t = k / (SIM_STEPS_BETWEEN + 1);
        path.push({ lat: from.latitude + (to.latitude - from.latitude) * t, lon: from.longitude + (to.longitude - from.longitude) * t });
      }
    }
    path.push({ lat: to.latitude + 0.0001, lon: to.longitude }); // just inside the stop's alert radius
  }
  return path;
}

export function simulate() {
  if (!state) return;
  stopSimulation();
  state.reached.clear();
  state.simulating = true;
  const path = simulationPath(state.sites);
  let i = 0;
  const step = () => {
    if (!state || i >= path.length) {
      stopSimulation();
      return;
    }
    position.setDemo(path[i].lat, path[i].lon);
    i += 1;
    simTimer = setTimeout(step, SIM_STEP_MS);
  };
  step();
  emit();
}

export function stopSimulation() {
  if (simTimer) clearTimeout(simTimer);
  simTimer = null;
  if (state && state.simulating) {
    state.simulating = false;
    emit();
  }
}

export function isSimulating() {
  return Boolean(state && state.simulating);
}

