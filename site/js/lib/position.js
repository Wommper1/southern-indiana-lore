// Where the visitor is, kept in memory only. It is never saved, logged, put in a URL, or sent anywhere.
// The browser's own geolocation feature reads the position (and your phone or browser may use network services to
// work it out; that happens outside this page). This module just holds the latest fix while the page is open.

let state = { status: "off", fix: null, message: "", demo: false };
let watchId = null;
const listeners = new Set();

function set(next) {
  state = Object.assign({}, state, next);
  for (const listener of listeners) listener(state);
}

export function getState() {
  return state;
}

export function subscribe(listener) {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

export function geolocationSupported() {
  return typeof navigator !== "undefined" && "geolocation" in navigator;
}

function onFix(position) {
  const c = position.coords;
  set({
    status: "on",
    demo: false,
    message: "",
    fix: { lat: c.latitude, lon: c.longitude, accuracy: c.accuracy, time: position.timestamp || Date.now() },
  });
}

function onError(error) {
  if (error.code === 1) {
    stopWatch();
    set({
      status: "denied",
      fix: null,
      message:
        "Location permission was blocked. To use Near me, allow location for this site in your browser or phone settings, then try again.",
    });
  } else if (error.code === 2) {
    set({ status: "waiting", message: "Your device could not work out its position yet. Still trying." });
  } else {
    set({ status: "waiting", message: "Finding your position is taking a while. Still trying." });
  }
}

function stopWatch() {
  if (watchId !== null) {
    navigator.geolocation.clearWatch(watchId);
    watchId = null;
  }
}

/** Start following the real position. Call this from a tap so the browser will show its permission prompt. */
export function start() {
  if (!geolocationSupported()) {
    set({ status: "unavailable", fix: null, message: "This browser cannot share a location with web pages." });
    return;
  }
  stopWatch();
  set({ status: "asking", demo: false, fix: null, message: "Waiting for your browser to share a location..." });
  watchId = navigator.geolocation.watchPosition(onFix, onError, {
    enableHighAccuracy: true,
    maximumAge: 5000,
    timeout: 30000,
  });
}

export function stop() {
  stopWatch();
  set({ status: "off", fix: null, demo: false, message: "" });
}

/** Pretend to be somewhere (for trying the atlas on a desktop). Uses no real location. */
export function setDemo(lat, lon) {
  stopWatch();
  set({ status: "on", demo: true, message: "", fix: { lat, lon, accuracy: 15, time: Date.now() } });
}

export function isActive() {
  return state.status === "on" || state.status === "asking" || state.status === "waiting";
}
