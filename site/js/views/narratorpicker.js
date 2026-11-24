// A small "Narrator" chooser with a "Hear a sample" button. Used on site pages, Near me and About.

import { h } from "../lib/dom.js";
import * as personas from "../lib/personas.js";
import { speechSupported, unlock, stop as stopSpeech } from "../lib/speech.js";

let counter = 0;

export function narratorPicker(options) {
  if (!speechSupported) return { el: null, destroy() {} };
  const id = "narrator-" + ++counter;
  const select = h("select", { class: "input narrator-select", id },
    personas.PERSONAS.map((p) => h("option", { value: p.id }, p.emoji + " " + p.name)));
  const desc = h("span", { class: "fine narrator-desc" });
  const sample = h("button", { type: "button", class: "btn btn-small" }, "Hear a sample");
  const sync = () => {
    const p = personas.current();
    select.value = p.id;
    desc.textContent = p.description;
  };
  select.addEventListener("change", () => {
    personas.setCurrent(select.value);
    stopSpeech();
  });
  sample.addEventListener("click", () => {
    unlock();
    personas.audition(select.value);
  });
  const unsubscribe = personas.subscribe(sync);
  sync();
  const el = h("div", { class: "narrator" + (options && options.compact ? " narrator-compact" : "") },
    h("label", { for: id, class: "field-label" }, "Narrator"),
    h("div", { class: "toolbar-row" }, select, sample),
    desc,
    options && options.compact ? null : h("p", { class: "fine" }, "Every narrator still says each site's trust level first. Voices come from your device, so they vary by phone and browser."));
  return { el, destroy: unsubscribe };
}
