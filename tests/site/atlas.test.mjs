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
  assert.equal(filterSites(sites, { q: "", county: "", levels: [] }).length, 52);
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

test("location is never stored or logged", () => {
  for (const file of walk(join(SITE, "js"))) {
    const text = readFileSync(file, "utf8");
    assert.ok(!/(localStorage|sessionStorage|indexedDB|document\.cookie)/.test(text), relative(SITE, file) + " uses browser storage");
    assert.ok(!/console\.(log|info|debug)/.test(text), relative(SITE, file) + " logs to the console");
  }
});

test("every file the page needs is in the offline list", () => {
  const precache = readFileSync(join(SITE, "precache.js"), "utf8");
  const listed = new Set(JSON.parse(precache.slice(precache.indexOf("{"), precache.lastIndexOf("}") + 1)).files);
  for (const file of walk(SITE)) {
    const name = relative(SITE, file).split("\\").join("/");
    if (["sw.js", "precache.js"].includes(name)) continue;
    assert.ok(listed.has(name), name + " is not in precache.js (run scripts/build_site_data.py)");
  }
  for (const name of listed) assert.ok(statSync(join(SITE, name)).isFile(), name + " is listed but missing");
});
