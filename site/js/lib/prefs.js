// Small display preferences: whether the welcome screen was seen, and larger text.
// Saved with the visitor's journal in this browser (lib/journal.js) when it allows saving; otherwise they last
// until the page is closed. Nothing is sent anywhere.

import * as journal from "./journal.js";

const memory = { welcomeSeen: false, largeText: false };
const listeners = new Set();

export function get(key) {
  return memory[key];
}

export function subscribe(listener) {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

function applyLargeText() {
  if (typeof document !== "undefined") document.documentElement.classList.toggle("large-text", memory.largeText);
}

export function set(key, value) {
  memory[key] = Boolean(value);
  if (key === "largeText") applyLargeText();
  for (const listener of listeners) listener(key, memory[key]);
  if (journal.supported()) journal.setPref(key, memory[key] ? "1" : "0").catch(() => {});
}

/** Load saved preferences. Never waits more than about half a second, so a slow browser cannot block the page. */
export async function load() {
  if (!journal.supported()) return;
  const read = Promise.all(["welcomeSeen", "largeText"].map(async (key) => {
    try {
      const value = await journal.getPref(key);
      if (value !== null) memory[key] = value === "1";
    } catch (error) {
      /* keep the default */
    }
  }));
  await Promise.race([read, new Promise((resolve) => setTimeout(resolve, 600))]);
  applyLargeText();
}
