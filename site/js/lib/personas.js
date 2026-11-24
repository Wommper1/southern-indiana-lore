// Narrator personas, ported from the Android app's NarratorPersona.kt. Each one changes the voice's pitch and speed,
// prefers certain English accents when the device has them, and opens with its own short line. The reading itself
// is always trust.spokenText(): the site name, then its trust level, then the narration, then "not fully verified"
// when it is not documented. A persona never drops or softens those parts.

import { speak } from "./speech.js";
import { spokenText } from "./trust.js";
import * as journal from "./journal.js";

export const PERSONAS = [
  {
    id: "HISTORIAN",
    name: "Archivist & Historian",
    emoji: "📜",
    description: "Clear, scholarly cadence.",
    pitch: 1.05,
    rate: 0.98,
    langs: ["en-GB", "en-US"],
    intro: "Archivist's note.",
    sample: "Archivist dispatch. Consulting county archives and the recorded lore of southern Indiana.",
  },
  {
    id: "FOLKLORE_SAGE",
    name: "Folklore Sage",
    emoji: "🕯️",
    description: "Slow, low fireside storyteller.",
    pitch: 0.6,
    rate: 0.8,
    langs: ["en-US", "en-GB"],
    intro: "Listen close, traveler.",
    sample: "Listen closely, traveler. The old hills hold their secrets, and the stories walk in the shadows.",
  },
  {
    id: "FIELD_EXPLORER",
    name: "Expedition Guide",
    emoji: "🧭",
    description: "Quick, upbeat trail scout.",
    pitch: 1.35,
    rate: 1.15,
    langs: ["en-AU", "en-US"],
    intro: "Field scout here!",
    sample: "Field scout online! Compass calibrated and trail pack ready. Let's explore!",
  },
  {
    id: "ADVENTURE_CHRONICLER",
    name: "Adventure Chronicler",
    emoji: "🎙️",
    description: "Low, cinematic documentary pacing.",
    pitch: 0.78,
    rate: 0.9,
    langs: ["en-US", "en-GB"],
    intro: "Frontier dispatch.",
    sample: "Frontier dispatch. Chronicling limestone caverns, ghost rails and old river crossings.",
  },
  {
    id: "WIZARD_NARRATOR",
    name: "Karst Lore Wizard",
    emoji: "🧙",
    description: "Slow, mysterious and old-fashioned.",
    pitch: 0.65,
    rate: 0.82,
    langs: ["en-GB", "en-IE", "en-US"],
    intro: "Halt, wanderer.",
    sample: "Halt, wanderer. By limestone root and hidden spring, old lore wakes beneath your feet.",
  },
  {
    id: "RETRO_RADIO",
    name: "Retro Radio (1940s)",
    emoji: "📻",
    description: "Brisk newsreel announcer.",
    pitch: 1.2,
    rate: 1.12,
    langs: ["en-US"],
    intro: "We interrupt this broadcast for a special bulletin.",
    sample: "We interrupt this broadcast for a special bulletin! You have entered historic southern Indiana.",
  },
];

const byId = new Map(PERSONAS.map((p) => [p.id, p]));
let currentId = "HISTORIAN";
const listeners = new Set();

export function getPersona(id) {
  return byId.get(id || currentId) || PERSONAS[0];
}

export function current() {
  return getPersona(currentId);
}

export function subscribe(listener) {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

/** Choose a narrator. The choice is remembered in this browser (with your journal) when it allows saving. */
export function setCurrent(id) {
  if (!byId.has(id)) return;
  currentId = id;
  for (const listener of listeners) listener(current());
  if (journal.supported()) journal.setPref("narrator", id).catch(() => {});
}

/** Load the remembered choice, if any. */
export async function restore() {
  if (!journal.supported()) return;
  try {
    const id = await journal.getPref("narrator");
    if (id && byId.has(id)) {
      currentId = id;
      for (const listener of listeners) listener(current());
    }
  } catch (error) {
    /* keep the default */
  }
}

/** The full text a persona reads for a site. The trust parts come from spokenText unchanged. */
export function narrationText(site, level, persona) {
  const p = persona || current();
  return p.intro + " " + spokenText(site, level);
}

const voiceOf = (p) => ({ pitch: p.pitch, rate: p.rate, langs: p.langs });

export function narrate(site, level) {
  const p = current();
  speak(narrationText(site, level, p), voiceOf(p));
}

export function audition(id) {
  const p = getPersona(id);
  speak(p.sample, voiceOf(p));
}
