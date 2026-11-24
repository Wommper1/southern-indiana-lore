// Keep the screen on while Near me or a tour is following the visitor, because a web page stops when the phone
// locks. Uses the Screen Wake Lock API (Chrome on Android, Safari on iOS 16.4 and later); where it is missing,
// nothing happens and the page says the screen must stay on. Released as soon as location stops.

import * as position from "./position.js";

let sentinel = null;
let wanted = false;
const listeners = new Set();

export function supported() {
  return typeof navigator !== "undefined" && "wakeLock" in navigator;
}

export function isHeld() {
  return Boolean(sentinel && !sentinel.released);
}

export function subscribe(listener) {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

function emit() {
  for (const listener of listeners) listener(isHeld());
}

async function acquire() {
  if (!supported() || isHeld() || document.visibilityState !== "visible") return;
  try {
    sentinel = await navigator.wakeLock.request("screen");
    sentinel.addEventListener("release", emit);
  } catch (error) {
    sentinel = null; // low battery mode or a browser policy can refuse; the page keeps working
  }
  emit();
}

async function release() {
  const held = sentinel;
  sentinel = null;
  if (held && !held.released) {
    try {
      await held.release();
    } catch (error) {
      /* already gone */
    }
  }
  emit();
}

function sync() {
  wanted = position.isActive();
  if (wanted) acquire();
  else release();
}

if (typeof document !== "undefined") {
  position.subscribe(sync);
  // Browsers drop the lock when the page is hidden; take it again when the visitor comes back.
  document.addEventListener("visibilitychange", () => {
    if (wanted && document.visibilityState === "visible") acquire();
  });
}
