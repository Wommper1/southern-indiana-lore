// The "My expedition" panel on each site page: save, mark visited, a private note and photos.
// Everything stays in this browser (see lib/journal.js).

import { h, clear } from "../lib/dom.js";
import * as journal from "../lib/journal.js";
import * as position from "../lib/position.js";
import { distanceMeters } from "../lib/geo.js";

const NEAR_SLACK_M = 150; // a check-in counts as "at the site" within its alert radius plus this much GPS slack

export function formatDate(iso) {
  try {
    return new Date(iso).toLocaleDateString(undefined, { year: "numeric", month: "short", day: "numeric" });
  } catch (error) {
    return iso;
  }
}

export function journalPanel(site) {
  if (!journal.supported()) {
    return {
      el: h("section", { class: "panel panel-journal" }, h("h2", { class: "panel-title" }, "My visit"),
        h("p", { class: "fine" }, "This browser is not letting the atlas save anything (private browsing often blocks it), so saving sites, visits, notes and photos is off.")),
      destroy() {},
    };
  }

  const urls = [];
  const visitBtn = h("button", { type: "button", class: "btn" });
  const visitLine = h("p", { class: "fine" });
  const note = h("textarea", { class: "input note", id: "note-" + site.id, rows: "4", maxlength: "20000", placeholder: "What you saw, who you went with, what to look for next time…" });
  const noteStatus = h("span", { class: "fine", role: "status", "aria-live": "polite" });
  const photoInput = h("input", { type: "file", accept: "image/*", id: "photo-" + site.id, class: "visually-hidden", multiple: true });
  const photoLabel = h("label", { class: "btn", for: "photo-" + site.id }, "Add photos");
  const photoStatus = h("p", { class: "fine", role: "status", "aria-live": "polite" });
  const gallery = h("ul", { class: "gallery" });
  let entry = journal.blankEntry(site.id);
  let noteTimer = null;
  let destroyed = false;

  function paint() {
    visitBtn.textContent = entry.visitedAt ? "Undo visit" : "I've been here";
    visitLine.textContent = entry.visitedAt
      ? "Visited " + formatDate(entry.visitedAt) + (entry.visitedNear ? " · checked in at the site" : "") + "."
      : "Been here? Tap the button to stamp your passport on the Saved tab.";
  }

  async function paintPhotos() {
    const list = await journal.photos(site.id);
    if (destroyed) return;
    while (urls.length) URL.revokeObjectURL(urls.pop());
    clear(gallery);
    list.forEach((photo, index) => {
      const url = URL.createObjectURL(photo.blob);
      urls.push(url);
      gallery.appendChild(
        h("li", null,
          h("img", { src: url, alt: "Your photo " + (index + 1) + " of " + site.title, loading: "lazy" }),
          h("button", { type: "button", class: "link-button", onclick: async () => {
            if (!confirm("Delete this photo from this device?")) return;
            await journal.deletePhoto(photo.id);
          } }, "Delete"))
      );
    });
  }

  visitBtn.addEventListener("click", async () => {
    if (entry.visitedAt) {
      if (!confirm("Remove the visit stamp for this site?")) return;
      entry = await journal.setVisited(site.id, false);
    } else {
      // If Near me is on, note whether you are actually at the site. Only yes/no is kept, never the position.
      const fix = position.getState().fix;
      const near = Boolean(fix && !position.getState().demo &&
        distanceMeters(fix.lat, fix.lon, site.latitude, site.longitude) <= site.geofenceRadiusMeters + NEAR_SLACK_M);
      entry = await journal.setVisited(site.id, true, near);
    }
    paint();
  });
  note.addEventListener("input", () => {
    noteStatus.textContent = "";
    clearTimeout(noteTimer);
    noteTimer = setTimeout(async () => {
      entry = await journal.setNote(site.id, note.value);
      noteStatus.textContent = note.value.trim() ? "Saved on this device." : "";
    }, 600);
  });
  photoInput.addEventListener("change", async () => {
    const files = [...photoInput.files];
    photoInput.value = "";
    let failed = 0;
    for (let i = 0; i < files.length; i += 1) {
      photoStatus.textContent = "Saving photo " + (i + 1) + " of " + files.length + "…";
      try {
        await journal.addPhoto(site.id, files[i]);
      } catch (error) {
        failed += 1;
      }
    }
    photoStatus.textContent = failed ? failed + " photo(s) could not be read. Try a JPEG or PNG." : "";
  });

  const unsubscribe = journal.subscribe(paintPhotos);
  journal.get(site.id).then((loaded) => {
    if (destroyed) return;
    entry = loaded;
    note.value = entry.note || "";
    paint();
  });
  paint();
  paintPhotos();

  const el = h(
    "section",
    { class: "panel panel-journal", "aria-label": "My visit" },
    h("h2", { class: "panel-title" }, "My visit"),
    h("div", { class: "actions" }, visitBtn),
    visitLine,
    h("label", { class: "field-label", for: "note-" + site.id }, "Private note"),
    note,
    noteStatus,
    h("div", { class: "actions" }, photoLabel, photoInput),
    photoStatus,
    gallery,
    h("p", { class: "fine" }, "Kept only in this browser on this device. Photos are shrunk and stripped of their location data before saving. ",
      h("a", { href: "#/mine" }, "See everything you've saved"), ".")
  );
  return {
    el,
    destroy() {
      destroyed = true;
      unsubscribe();
      clearTimeout(noteTimer);
      if (note.value !== (entry.note || "")) journal.setNote(site.id, note.value); // keep a note typed just before leaving
      while (urls.length) URL.revokeObjectURL(urls.pop());
    },
  };
}
