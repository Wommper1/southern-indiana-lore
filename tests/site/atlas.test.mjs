// Tests for the web atlas. Run with:  node --test tests/site/
// Needs Node 22 or newer (it reads the atlas's ES modules directly) and Python 3 (for the data build check).

import test from "node:test";
import assert from "node:assert/strict";
import { readFileSync, readdirSync, statSync } from "node:fs";
import { execFileSync } from "node:child_process";
import { dirname, join, relative } from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";

const ROOT = join(dirname(fileURLToPath(import.meta.url)), "..", "..");
const SITE = join(ROOT, "site");
const PACK = join(ROOT, "app", "src", "main", "assets", "lorepacks", "southern_indiana");
const load = (path) => import(pathToFileURL(join(SITE, "js", "lib", path)).href);
const json = (path) => JSON.parse(readFileSync(path, "utf8"));

const atlas = json(join(SITE, "data", "atlas.json"));
const packSites = json(join(PACK, "sites.json"));
const levelTable = Object.fromEntries(atlas.confidenceLevels.map((level) => [level.id, level]));

// ---------- data drift ----------

test("atlas data is exactly what the build script produces from the pack", () => {
  execFileSync("python", [join(ROOT, "scripts", "build_site_data.py"), "--check"], { stdio: "pipe" });
});

test("every pack site is in the atlas, with the pack's own text", () => {
  assert.equal(atlas.sites.length, packSites.length);
  assert.equal(atlas.pack.siteCount, packSites.length);
  for (const raw of packSites) {
    const site = atlas.sites.find((candidate) => candidate.id === raw.id);
    assert.ok(site, "missing " + raw.id);
    for (const field of ["title", "subtitle", "county", "latitude", "longitude", "verifiedHistory", "folkloreTradition", "fullStory", "sources", "sourceNotes", "verificationGrade", "audioNarration"]) {
      assert.equal(site[field], raw[field], raw.id + "." + field + " differs from the pack");
    }
    assert.equal(site.confidenceLevel, raw.confidenceLevel, raw.id + " badge differs from the pack");
  }
});

test("confidence labels and descriptions are the Android app's", () => {
  const kotlin = readFileSync(join(ROOT, "app", "src", "main", "java", "com", "example", "data", "model", "ConfidenceLevel.kt"), "utf8");
  for (const level of atlas.confidenceLevels) {
    assert.ok(kotlin.includes('label = "' + level.label + '"'), level.id + " label not found in ConfidenceLevel.kt");
    assert.ok(kotlin.includes('description = "' + level.description + '"'), level.id + " description not found in ConfidenceLevel.kt");
  }
});

// ---------- trust layer ----------

test("history heading: only documented levels get the documented heading", async () => {
  const trust = await load("trust.js");
  for (const level of ["OFFICIAL_SOURCE", "VERIFIED", "WELL_SUPPORTED"]) {
    assert.equal(trust.historyHeading(level), "Documented archival history");
    assert.equal(trust.historyCaveat(level), null);
  }
  for (const level of ["PLAUSIBLE", "PROBABLE", "ORAL_HISTORY", "FOLKLORE", "LEGEND", "DISPUTED", "DEBUNKED", "UNVERIFIED"]) {
    assert.equal(trust.historyHeading(level), "History (not fully verified)", level);
    assert.ok(trust.historyCaveat(level), level + " needs a caveat");
  }
});

test("unknown or missing levels fail closed to Unverified", async () => {
  const trust = await load("trust.js");
  for (const bad of [undefined, null, "", "  ", "VERIFIED!", "official", 42, "TOTALLY_TRUE"]) {
    assert.equal(trust.resolveLevel(bad, levelTable).id, "UNVERIFIED", String(bad));
  }
  assert.equal(trust.resolveLevel("plausible", levelTable).id, "PLAUSIBLE");
});

test("history and folklore text follow the app's fallbacks", async () => {
  const trust = await load("trust.js");
  assert.equal(trust.historyText({ verifiedHistory: "Checked facts.", confidenceLevel: "PLAUSIBLE", fullStory: "x" }), "Checked facts.");
  assert.equal(trust.historyText({ verifiedHistory: "", confidenceLevel: "UNVERIFIED", fullStory: "x" }), trust.NO_RECORD_TEXT);
  assert.equal(trust.historyText({ verifiedHistory: "", confidenceLevel: "LEGEND", fullStory: "x" }), trust.NO_RECORD_TEXT);
  assert.equal(trust.historyText({ verifiedHistory: "", confidenceLevel: "PLAUSIBLE", fullStory: "story" }), "story");
  assert.equal(trust.showsFolklorePanel({ folkloreTradition: "", confidenceLevel: "PLAUSIBLE" }), false);
  assert.equal(trust.showsFolklorePanel({ folkloreTradition: "", confidenceLevel: "LEGEND" }), true);
  assert.equal(trust.showsFolklorePanel({ folkloreTradition: "tale", confidenceLevel: "PLAUSIBLE" }), true);
  assert.equal(trust.sourcesText({ sources: "", sourceNotes: "" }), trust.NO_SOURCES_TEXT);
  assert.equal(trust.sourcesText({ sources: "A", sourceNotes: "A" }), "A");
  assert.equal(trust.sourcesText({ sources: "A", sourceNotes: "B" }), "A\n\nNotes: B");
});

test("no entry is presented as documented unless its badge says so", async () => {
  const trust = await load("trust.js");
  const documented = new Set(["OFFICIAL_SOURCE", "VERIFIED", "WELL_SUPPORTED"]);
  for (const site of atlas.sites) {
    const heading = trust.historyHeading(site.confidenceLevel);
    assert.equal(heading === "Documented archival history", documented.has(site.confidenceLevel), site.id);
    assert.ok(levelTable[site.confidenceLevel], site.id + " has an unknown badge");
  }
});

test("read-aloud text always names the trust level and flags unverified history", async () => {
  const trust = await load("trust.js");
  const plausible = atlas.sites.find((site) => site.confidenceLevel === "PLAUSIBLE");
  const spoken = trust.spokenText(plausible, levelTable.PLAUSIBLE);
  assert.ok(spoken.includes("Trust level: Plausible."));
  assert.ok(spoken.endsWith("Note: this history is not fully verified."));
  const official = atlas.sites.find((site) => site.confidenceLevel === "OFFICIAL_SOURCE");
  const spokenOfficial = trust.spokenText(official, levelTable.OFFICIAL_SOURCE);
  assert.ok(spokenOfficial.includes("Trust level: Official Source."));
  assert.ok(!spokenOfficial.includes("not fully verified"));
});

// ---------- geography ----------

test("distance, bearing and formatting", async () => {
  const geo = await load("geo.js");
  // One degree of latitude is about 111.2 km.
  assert.ok(Math.abs(geo.distanceMeters(38, -86, 39, -86) - 111195) < 200);
  assert.equal(geo.distanceMeters(38.2863, -85.8145, 38.2863, -85.8145), 0);
  assert.equal(Math.round(geo.bearingDegrees(38, -86, 39, -86)), 0);
  assert.equal(Math.round(geo.bearingDegrees(38, -86, 38, -85)), 90);
  assert.equal(geo.cardinal(0), "N");
  assert.equal(geo.cardinal(44), "NE");
  assert.equal(geo.cardinal(181), "S");
  assert.equal(geo.cardinal(359), "N");
  assert.equal(geo.formatDistance(30), "100 ft"); // 30 m is about 98 ft, shown to the nearest 10
  assert.equal(geo.formatDistance(1609.344), "1.0 mi");
  assert.equal(geo.formatDistance(40000), "25 mi");
  assert.equal(geo.niceLength(7300), 5000);
  assert.equal(geo.niceLength(1900), 1000);
});

test("map projection keeps every site inside its box", async () => {
  const geo = await load("geo.js");
  const proj = geo.makeProjection(atlas.sites);
  for (const site of atlas.sites) {
    const p = proj.project(site.latitude, site.longitude);
    assert.ok(p.x >= -1e-9 && p.x <= proj.width + 1e-9 && p.y >= -1e-9 && p.y <= proj.height + 1e-9, site.id);
    const back = proj.unproject(p.x, p.y);
    assert.ok(Math.abs(back.lat - site.latitude) < 1e-9 && Math.abs(back.lon - site.longitude) < 1e-9);
  }
});

// ---------- search and filter ----------

test("search, county and badge filters", async () => {
  const { filterSites, indexSites, sortSites } = await load("filter.js");
  const sites = indexSites(structuredClone(atlas.sites));
  assert.equal(filterSites(sites, { q: "", county: "", levels: [] }).length, packSites.length);
  const floyd = filterSites(sites, { q: "", county: "Floyd County", levels: [] });
  assert.ok(floyd.length > 0 && floyd.every((site) => site.county === "Floyd County"));
  const unverified = filterSites(sites, { q: "", county: "", levels: ["UNVERIFIED"] });
  assert.equal(unverified.length, atlas.sites.filter((site) => site.confidenceLevel === "UNVERIFIED").length);
  const mansion = filterSites(sites, { q: "CULBERTSON  mansion", county: "", levels: [] });
  assert.equal(mansion.length, 1);
  assert.equal(filterSites(sites, { q: "zzzz-not-here", county: "", levels: [] }).length, 0);
  const sorted = sortSites(sites, "name");
  assert.deepEqual(sorted.map((site) => site.title), sorted.map((site) => site.title).slice().sort((a, b) => a.localeCompare(b, "en", { sensitivity: "base", numeric: true })));
});

// ---------- privacy guards ----------

function walk(dir) {
  return readdirSync(dir).flatMap((name) => {
    const path = join(dir, name);
    return statSync(path).isDirectory() ? walk(path) : [path];
  });
}

test("the page can only load files from its own site", () => {
  const html = readFileSync(join(SITE, "index.html"), "utf8");
  const csp = html.match(/Content-Security-Policy"\s+content="([^"]+)"/);
  assert.ok(csp, "index.html needs a Content-Security-Policy");
  for (const directive of ["default-src 'self'", "script-src 'self'", "connect-src 'self'", "img-src 'self' data:", "font-src 'self'"]) {
    assert.ok(csp[1].includes(directive), "CSP is missing " + directive);
  }
  assert.ok(!/unsafe-inline|unsafe-eval|https?:\/\//.test(csp[1]), "CSP must not allow other origins or inline code");
  assert.ok(!/<script(?![^>]*type="module" src="js\/app\.js")[^>]*>/.test(html), "only js/app.js may be loaded as a script");
  assert.ok(!/\sstyle="/.test(html), "inline styles would be blocked by the CSP");
});

test("no source file loads, fetches or sends anything to another site", () => {
  for (const file of walk(SITE)) {
    if (!/\.(html|css|js)$/.test(file)) continue;
    const text = readFileSync(file, "utf8");
    const name = relative(SITE, file);
    assert.ok(!/@import/.test(text), name + " uses @import");
    assert.ok(!/url\(\s*["']?https?:/.test(text), name + " loads a stylesheet resource from another site");
    assert.ok(!/(XMLHttpRequest|WebSocket|sendBeacon|EventSource)/.test(text), name + " uses a network API other than fetch");
    for (const match of text.matchAll(/fetch\(([^)]*)\)/g)) {
      assert.ok(!/https?:/.test(match[1]), name + " fetches an absolute URL: " + match[0]);
    }
    if (name.endsWith("index.html")) {
      assert.ok(!/<(script|link|img|iframe)[^>]+(src|href)="https?:/.test(text), "index.html loads from another site");
    }
  }
});

test("external links open safely and are only things a person taps", () => {
  for (const file of walk(join(SITE, "js"))) {
    const text = readFileSync(file, "utf8");
    for (const match of text.matchAll(/target:\s*"_blank"[^}]*/g)) {
      assert.ok(/noopener noreferrer/.test(match[0]), relative(SITE, file) + ": _blank link without noopener noreferrer");
    }
  }
});

test("location is never stored or logged; only the journal module uses browser storage", () => {
  for (const file of walk(join(SITE, "js"))) {
    const text = readFileSync(file, "utf8");
    const name = relative(SITE, file).split("\\").join("/");
    assert.ok(!/(localStorage|sessionStorage|document\.cookie)/.test(text), name + " uses browser storage");
    if (name !== "js/lib/journal.js") assert.ok(!/indexedDB/.test(text), name + " uses IndexedDB (only js/lib/journal.js may)");
    assert.ok(!/console\.(log|info|debug)/.test(text), relative(SITE, file) + " logs to the console");
  }
});

test("every file the page needs is in the offline list", () => {
  const precache = readFileSync(join(SITE, "precache.js"), "utf8");
  const listed = new Set(JSON.parse(precache.slice(precache.indexOf("{"), precache.lastIndexOf("}") + 1)).files);
  for (const file of walk(SITE)) {
    const name = relative(SITE, file).split("\\").join("/");
    if (["sw.js", "precache.js"].includes(name) || name.startsWith("map/")) continue; // the street map is saved on request
    assert.ok(listed.has(name), name + " is not in precache.js (run scripts/build_site_data.py)");
  }
  for (const name of listed) assert.ok(statSync(join(SITE, name)).isFile(), name + " is listed but missing");
});

test("vendored libraries are exactly the pinned, hashed copies", async () => {
  const { createHash } = await import("node:crypto");
  const record = readFileSync(join(SITE, "vendor", "VENDORED.md"), "utf8");
  const lines = [...record.matchAll(/^([0-9a-f]{64}) {2}\.\/(.+)$/gm)];
  assert.ok(lines.length > 10, "VENDORED.md lists the vendored files");
  const listed = new Set();
  for (const [, hash, name] of lines) {
    listed.add(name);
    const actual = createHash("sha256").update(readFileSync(join(SITE, "vendor", name))).digest("hex");
    assert.equal(actual, hash, "vendor/" + name + " was changed by hand (re-run scripts/vendor_web_libs.sh)");
  }
  for (const file of walk(join(SITE, "vendor"))) {
    const name = relative(join(SITE, "vendor"), file).split("\\").join("/");
    if (name !== "VENDORED.md") assert.ok(listed.has(name), "vendor/" + name + " is not in VENDORED.md");
  }
});

test("the street map only reads its map file from this site", () => {
  const sw = readFileSync(join(SITE, "sw.js"), "utf8");
  const basemap = readFileSync(join(SITE, "js", "lib", "basemap.js"), "utf8");
  const street = readFileSync(join(SITE, "js", "views", "streetmap.js"), "utf8");
  assert.ok(/MAP_FILE = "map\/southern-indiana\.pmtiles"/.test(basemap), "map file is a relative path on this site");
  assert.ok(/"pmtiles:\/\/" \+ abs\(info\.file\)/.test(street), "map source resolves against this page");
  assert.ok(/abs\("vendor\/basemap-assets\/fonts\/"\)/.test(street) && /abs\("vendor\/basemap-assets\/sprites\/dark"\)/.test(street), "fonts and icons are vendored");
  assert.ok(sw.includes('const MAP_CACHE = "atlas-basemap-v1"') && basemap.includes('export const MAP_CACHE = "atlas-basemap-v1"'), "page and worker agree on the saved-map store");
  const precache = readFileSync(join(SITE, "precache.js"), "utf8");
  assert.ok(!/"map\//.test(precache), "the big map file is never saved for every visitor");
});

test("the journal never stores a position", () => {
  const text = readFileSync(join(SITE, "js", "lib", "journal.js"), "utf8");
  assert.ok(!/\b(lat|lon|latitude|longitude|coords)\b/.test(text), "journal.js must not handle coordinates");
  const panel = readFileSync(join(SITE, "js", "views", "journalpanel.js"), "utf8");
  assert.ok(/setVisited\(site\.id, true, near\)/.test(panel), "a check-in stores only a yes/no 'near'");
});

test("journal backups are validated before loading", async () => {
  globalThis.indexedDB = undefined;
  const journal = await load("journal.js");
  assert.equal(journal.validateBackup({ format: "other" }).ok, false);
  assert.equal(journal.validateBackup({ format: journal.BACKUP_FORMAT, version: 99 }).ok, false);
  const checked = journal.validateBackup({
    format: journal.BACKUP_FORMAT,
    version: 1,
    sites: [
      { siteId: "floyd_culbertson_mansion", favorite: true, visitedAt: "2026-05-01T12:00:00Z", note: "x", extra: "<script>" },
      { siteId: "../../etc", favorite: true },
      { siteId: "bad_date", visitedAt: "not a date" },
      null,
    ],
    photos: [{ siteId: "a", data: "data:image/jpeg;base64,AAAA" }, { siteId: "b", data: "javascript:alert(1)" }],
  });
  assert.ok(checked.ok);
  assert.deepEqual(checked.sites.map((s) => s.siteId), ["floyd_culbertson_mansion"]);
  assert.equal(checked.sites[0].extra, undefined, "unknown fields are dropped");
  assert.equal(checked.photos.length, 1);
});

// ---------- tours ----------

const tours = json(join(SITE, "data", "tours.json")).tours;
const siteById = new Map(atlas.sites.map((s) => [s.id, s]));

test("every tour stop is a real site, with no repeats", () => {
  assert.ok(tours.length >= 3);
  const ids = new Set();
  for (const tour of tours) {
    assert.ok(/^[a-z0-9_]+$/.test(tour.id) && !ids.has(tour.id), "tour id " + tour.id);
    ids.add(tour.id);
    assert.ok(tour.title && tour.summary && tour.duration, tour.id + " needs a title, summary and duration");
    assert.ok(tour.stops.length >= 3, tour.id + " needs at least three stops");
    assert.equal(new Set(tour.stops).size, tour.stops.length, tour.id + " repeats a stop");
    for (const stop of tour.stops) assert.ok(siteById.has(stop), tour.id + ": unknown site " + stop);
  }
});

test("GPX export is well-formed and escapes text", async () => {
  const { tourToGpx, gpxFileName } = await load("gpx.js");
  const sites = [
    { title: "A & B <\"quoted\">", latitude: 38.1, longitude: -85.9, county: "Floyd County", subtitle: "x\u0001y" },
    { title: "C", latitude: 38.2, longitude: -86, county: "Clark County", subtitle: "" },
  ];
  const gpx = tourToGpx({ id: "t1", title: "T & T", summary: "S" }, sites, () => "Plausible", (s) => "https://example.test/#/site/" + s.title);
  assert.ok(gpx.startsWith('<?xml version="1.0" encoding="UTF-8"?>'));
  assert.equal((gpx.match(/<wpt /g) || []).length, 2);
  assert.equal((gpx.match(/<rtept /g) || []).length, 2);
  assert.ok(gpx.includes("1. A &amp; B &lt;&quot;quoted&quot;&gt;"));
  assert.ok(!gpx.includes("\u0001"), "control characters are removed");
  assert.ok(gpx.includes('lat="38.100000" lon="-85.900000"'));
  assert.equal(gpxFileName({ id: "a/b" }), "southern-indiana-lore-a-b.gpx");
});

test("the simulated drive passes inside every stop's alert radius, in order", async () => {
  const { simulationPath } = await load("tourrun.js");
  const { distanceMeters } = await load("geo.js");
  for (const tour of tours) {
    const sites = tour.stops.map((id) => siteById.get(id));
    const path = simulationPath(sites);
    let cursor = 0;
    for (const site of sites) {
      const hit = path.findIndex((p, i) => i >= cursor && distanceMeters(p.lat, p.lon, site.latitude, site.longitude) <= site.geofenceRadiusMeters);
      assert.ok(hit >= 0, tour.id + ": the simulation never reaches " + site.id);
      cursor = hit;
    }
  }
});

// ---------- narrators ----------

test("every narrator keeps the trust level and the not-verified warning", async () => {
  globalThis.indexedDB = undefined;
  const { PERSONAS, narrationText } = await load("personas.js");
  const { spokenText } = await load("trust.js");
  assert.equal(PERSONAS.length, 6);
  for (const persona of PERSONAS) {
    assert.ok(persona.pitch >= 0 && persona.pitch <= 2 && persona.rate >= 0.5 && persona.rate <= 2, persona.id + " voice settings");
    for (const site of atlas.sites) {
      const level = levelTable[site.confidenceLevel];
      const text = narrationText(site, level, persona);
      assert.ok(text.endsWith(spokenText(site, level)), persona.id + " changes the reading for " + site.id);
      assert.ok(text.includes("Trust level: " + level.label), persona.id + " drops the trust level");
    }
  }
});

test("all site readings go through the narrator", () => {
  for (const file of walk(join(SITE, "js"))) {
    const name = relative(SITE, file).split("\\").join("/");
    const text = readFileSync(file, "utf8");
    if (name === "js/lib/personas.js") continue;
    assert.ok(!/speak\(spokenText/.test(text), name + " reads a site without the narrator");
  }
});

// ---------- sharing and suggestions ----------

test("QR codes are built on the device with a valid size", async () => {
  const { qrMatrix } = await import(pathToFileURL(join(SITE, "js", "views", "share.js")).href);
  const m = qrMatrix("https://wommper1.github.io/southern-indiana-lore/#/site/floyd_town_clock_church");
  assert.ok(m.length >= 21 && (m.length - 17) % 4 === 0, "QR size is 17 + 4 x version");
  assert.ok(m.every((row) => row.length === m.length));
  // the three finder patterns' corners are dark
  assert.ok(m[0][0] && m[0][m.length - 1] && m[m.length - 1][0]);
});

test("correction and suggestion links pre-fill real issue forms", async () => {
  const { correctionUrl, suggestSiteUrl, REPO_URL } = await load("config.js");
  const url = new URL(correctionUrl({ title: "Duncan Tunnel" }, "https://example.test/#/site/x"));
  assert.ok(url.href.startsWith(REPO_URL + "/issues/new?"));
  assert.equal(url.searchParams.get("template"), "data_correction.yml");
  assert.equal(url.searchParams.get("site"), "Duncan Tunnel");
  assert.equal(new URL(suggestSiteUrl()).searchParams.get("template"), "new_site.yml");
  for (const [template, ids] of [["data_correction.yml", ["site", "wrong"]], ["new_site.yml", []]]) {
    const form = readFileSync(join(ROOT, ".github", "ISSUE_TEMPLATE", template), "utf8");
    for (const id of ids) assert.ok(new RegExp("id: " + id + "\\b").test(form), template + " has no field " + id);
  }
});
