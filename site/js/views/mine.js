// The Mine tab: your passport (visited sites and county stamps), saved sites, notes, and backup.
// Everything shown here lives only in this browser (see lib/journal.js).

import { h, clear } from "../lib/dom.js";
import * as journal from "../lib/journal.js";
import { badge } from "../ui.js";
import { formatDate } from "./journalpanel.js";

function siteCard(ctx, site, extra) {
  const level = ctx.levelTable[site.confidenceLevel];
  return h("li", null,
    h("a", { class: "card", href: "#/site/" + encodeURIComponent(site.id), "data-level": level.id },
      h("div", { class: "card-top" }, badge(level, { compact: true })),
      h("h3", { class: "card-title" }, site.title),
      h("p", { class: "card-meta" }, [site.county, extra].filter(Boolean).join(" · "))));
}

function download(name, text) {
  const url = URL.createObjectURL(new Blob([text], { type: "application/json" }));
  const a = h("a", { href: url, download: name, class: "visually-hidden" }, "download");
  document.body.appendChild(a);
  a.click();
  a.remove();
  setTimeout(() => URL.revokeObjectURL(url), 10000);
}

export function renderMine(ctx) {
  const body = h("div", null, h("p", { class: "loading" }, "Opening your expedition…"));
  const backupStatus = h("p", { class: "fine", role: "status", "aria-live": "polite" });
  let destroyed = false;

  async function paint() {
    const entries = await journal.all();
    const counts = await journal.photoCounts();
    if (destroyed) return;
    const known = (id) => ctx.byId.get(id);
    const rows = [...entries.values()].filter((e) => known(e.siteId));
    const visited = rows.filter((e) => e.visitedAt).sort((a, b) => b.visitedAt.localeCompare(a.visitedAt));
    const saved = rows.filter((e) => e.favorite);
    const noted = rows.filter((e) => (e.note || "").trim());
    const photoTotal = [...counts.values()].reduce((a, b) => a + b, 0);
    const visitedIds = new Set(visited.map((e) => e.siteId));

    // County stamps
    const byCounty = new Map();
    for (const site of ctx.sites) {
      const c = byCounty.get(site.county) || { total: 0, seen: 0 };
      c.total += 1;
      if (visitedIds.has(site.id)) c.seen += 1;
      byCounty.set(site.county, c);
    }
    const counties = [...byCounty.entries()].sort((a, b) => a[0].localeCompare(b[0]));
    const complete = counties.filter(([, c]) => c.seen === c.total).length;

    clear(body);
    body.append(
      h("section", { class: "panel passport", "aria-label": "Passport" },
        h("h2", { class: "panel-title" }, "Passport"),
        h("p", { class: "passport-count" }, h("strong", null, String(visited.length)), " of " + ctx.sites.length + " sites visited"),
        h("progress", { max: String(ctx.sites.length), value: String(visited.length), "aria-label": "Sites visited" }),
        h("p", { class: "fine" }, complete + " of " + counties.length + " counties complete. A stamp turns gold when you have visited every site in that county."),
        h("ul", { class: "stamps" },
          counties.map(([county, c]) =>
            h("li", { class: "stamp" + (c.seen === c.total ? " stamp-done" : c.seen ? " stamp-started" : "") },
              h("span", { class: "stamp-name" }, county.replace(/ County$/, "")),
              h("span", { class: "stamp-count" }, c.seen + "/" + c.total))))),

      h("h2", { class: "panel-title section-gap" }, "Saved sites (" + saved.length + ")"),
      saved.length
        ? h("ul", { class: "card-list" }, saved.map((e) => siteCard(ctx, known(e.siteId), counts.get(e.siteId) ? counts.get(e.siteId) + " photo(s)" : null)))
        : h("p", { class: "empty" }, "Nothing saved yet. Tap \"Save this site\" on any site page."),

      h("h2", { class: "panel-title section-gap" }, "Visited (" + visited.length + ")"),
      visited.length
        ? h("ul", { class: "card-list" }, visited.map((e) => siteCard(ctx, known(e.siteId), "visited " + formatDate(e.visitedAt) + (e.visitedNear ? " ✓ checked in" : ""))))
        : h("p", { class: "empty" }, "No visits yet. Tap \"I've been here\" on a site page."),

      noted.length
        ? h("div", null, h("h2", { class: "panel-title" }, "Notes (" + noted.length + ")"),
            h("ul", { class: "card-list" }, noted.map((e) => siteCard(ctx, known(e.siteId), e.note.trim().split("\n")[0].slice(0, 90)))))
        : "",

      h("section", { class: "panel", id: "backup" },
        h("h2", { class: "panel-title" }, "Backup"),
        h("p", null, "Your saves, visits, notes and " + photoTotal + " photo(s) are stored only in this browser on this device. There is no account and nothing is uploaded, so nobody can restore them for you. Download a backup now and then."),
        h("ul", { class: "plain fine" },
          h("li", null, "On iPhone and iPad, Safari may delete a website's saved data after about a week without a visit. Adding the atlas to your Home Screen avoids that."),
          h("li", null, "Clearing this site's data in your browser settings deletes everything here.")),
        h("div", { class: "actions" }, backupButton, restoreLabel, restoreInput),
        backupStatus,
        h("details", { class: "danger-zone" },
          h("summary", null, "Delete everything"),
          h("p", { class: "fine" }, "Removes all saves, visits, notes and photos from this device. Download a backup first if you might want them."),
          h("button", { type: "button", class: "btn btn-danger", onclick: async () => {
            if (!confirm("Delete all your saves, visits, notes and photos from this device? This cannot be undone.")) return;
            await journal.clearAll();
            backupStatus.textContent = "Everything was deleted from this device.";
          } }, "Delete everything on this device")))
    );
  }

  const backupButton = h("button", { type: "button", class: "btn btn-primary" }, "Download backup");
  backupButton.addEventListener("click", async () => {
    backupStatus.textContent = "Preparing your backup…";
    try {
      const data = await journal.exportBackup();
      const day = new Date().toISOString().slice(0, 10);
      download("southern-indiana-lore-backup-" + day + ".json", JSON.stringify(data));
      backupStatus.textContent = "Backup downloaded (" + data.sites.length + " site(s), " + data.photos.length + " photo(s)). Keep the file somewhere safe, such as your cloud drive.";
    } catch (error) {
      backupStatus.textContent = "The backup could not be made in this browser.";
    }
  });
  const restoreInput = h("input", { type: "file", accept: "application/json,.json", id: "restore-file", class: "visually-hidden" });
  const restoreLabel = h("label", { class: "btn", for: "restore-file" }, "Restore from a backup file");
  restoreInput.addEventListener("change", async () => {
    const file = restoreInput.files[0];
    restoreInput.value = "";
    if (!file) return;
    backupStatus.textContent = "Reading the backup…";
    try {
      const result = await journal.importBackup(JSON.parse(await file.text()));
      backupStatus.textContent = result.ok
        ? "Restored " + result.sites + " site(s) and " + result.photos + " photo(s). Anything already on this device was kept."
        : result.error;
    } catch (error) {
      backupStatus.textContent = "That file could not be read as a backup.";
    }
  });

  const el = h(
    "div",
    { class: "view view-mine" },
    h("h1", { class: "page-title" }, "My expedition"),
    journal.supported()
      ? body
      : h("p", { class: "notice" }, "This browser is not letting the atlas save anything (private browsing often blocks it). Open the atlas in a normal window to keep a passport, notes and photos.")
  );
  const unsubscribe = journal.subscribe(paint);
  if (journal.supported()) paint();
  return { el, title: "Mine", destroy() { destroyed = true; unsubscribe(); } };
}
