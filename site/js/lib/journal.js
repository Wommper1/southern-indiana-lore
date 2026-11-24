// My Expedition: the visitor's own saved sites, visits, notes and photos.
// Everything is kept in this browser's IndexedDB on this device. It is never sent anywhere; there is no account and
// no server. A backup is a file the visitor downloads and can load again on another device.
// This is the ONLY module allowed to use browser storage (a test enforces that). It stores no location: a visit is a
// date plus a yes/no "was near the site when checked in", never coordinates.

const DB_NAME = "si-lore-journal";
const DB_VERSION = 2; // 2 added "prefs" (small settings such as the chosen narrator)
export const BACKUP_FORMAT = "southern-indiana-lore-journal";
export const BACKUP_VERSION = 1;
const PHOTO_MAX_PX = 1600; // photos are shrunk before saving: smaller, and re-encoding drops camera metadata (GPS)

let dbPromise = null;
const listeners = new Set();
let cache = null; // Map siteId -> entry, loaded once

export function supported() {
  try {
    return typeof indexedDB !== "undefined" && indexedDB !== null;
  } catch (error) {
    return false; // some private modes throw on access
  }
}

function open() {
  if (!dbPromise) {
    dbPromise = new Promise((resolve, reject) => {
      const request = indexedDB.open(DB_NAME, DB_VERSION);
      request.onupgradeneeded = () => {
        const db = request.result;
        if (!db.objectStoreNames.contains("sites")) db.createObjectStore("sites", { keyPath: "siteId" });
        if (!db.objectStoreNames.contains("photos")) {
          const photos = db.createObjectStore("photos", { keyPath: "id", autoIncrement: true });
          photos.createIndex("siteId", "siteId");
        }
        if (!db.objectStoreNames.contains("prefs")) db.createObjectStore("prefs", { keyPath: "key" });
      };
      request.onsuccess = () => resolve(request.result);
      request.onerror = () => reject(request.error);
    });
    dbPromise.catch(() => {
      dbPromise = null;
    });
  }
  return dbPromise;
}

function done(request) {
  return new Promise((resolve, reject) => {
    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error);
  });
}

function txDone(tx) {
  return new Promise((resolve, reject) => {
    tx.oncomplete = () => resolve();
    tx.onerror = () => reject(tx.error);
    tx.onabort = () => reject(tx.error);
  });
}

async function store(name, mode) {
  const db = await open();
  const tx = db.transaction(name, mode);
  return { tx, store: tx.objectStore(name) };
}

function emit() {
  for (const listener of listeners) listener();
}

export function subscribe(listener) {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

export function blankEntry(siteId) {
  return { siteId, favorite: false, visitedAt: null, visitedNear: false, note: "", updatedAt: null };
}

export function isEmptyEntry(entry) {
  return !entry.favorite && !entry.visitedAt && !(entry.note || "").trim();
}

/** All entries as a Map keyed by site id. */
export async function all() {
  if (cache) return cache;
  if (!supported()) return (cache = new Map());
  try {
    const { store: s } = await store("sites", "readonly");
    const rows = await done(s.getAll());
    cache = new Map(rows.map((row) => [row.siteId, Object.assign(blankEntry(row.siteId), row)]));
  } catch (error) {
    cache = new Map();
  }
  return cache;
}

export async function get(siteId) {
  return (await all()).get(siteId) || blankEntry(siteId);
}

/** Ask the browser to keep this data even when space runs low. Best effort; some browsers ignore it. */
async function askToPersist() {
  try {
    if (navigator.storage && navigator.storage.persist && !(await navigator.storage.persisted())) await navigator.storage.persist();
  } catch (error) {
    /* not supported; fine */
  }
}

async function update(siteId, change) {
  const entries = await all();
  const next = Object.assign(blankEntry(siteId), entries.get(siteId), change, { siteId, updatedAt: new Date().toISOString() });
  const { tx, store: s } = await store("sites", "readwrite");
  if (isEmptyEntry(next)) {
    s.delete(siteId);
    entries.delete(siteId);
  } else {
    s.put(next);
    entries.set(siteId, next);
  }
  await txDone(tx);
  askToPersist();
  emit();
  return next;
}

export const setFavorite = (siteId, favorite) => update(siteId, { favorite: Boolean(favorite) });
export const setNote = (siteId, note) => update(siteId, { note: String(note || "").slice(0, 20000) });
export function setVisited(siteId, visited, near) {
  return update(siteId, visited ? { visitedAt: new Date().toISOString(), visitedNear: Boolean(near) } : { visitedAt: null, visitedNear: false });
}

// ---------- preferences ----------

const PREF_KEYS = new Set(["narrator"]);

export async function getPref(key) {
  if (!supported() || !PREF_KEYS.has(key)) return null;
  const { store: s } = await store("prefs", "readonly");
  const row = await done(s.get(key));
  return row ? row.value : null;
}

export async function setPref(key, value) {
  if (!PREF_KEYS.has(key)) throw new Error("unknown preference " + key);
  const { tx, store: s } = await store("prefs", "readwrite");
  s.put({ key, value: String(value) });
  await txDone(tx);
}

// ---------- photos ----------

/** Shrink a picture and re-encode it as JPEG. Re-encoding drops EXIF data, including any GPS position. */
export async function shrinkPhoto(file) {
  const bitmap = await createImageBitmap(file);
  const scale = Math.min(1, PHOTO_MAX_PX / Math.max(bitmap.width, bitmap.height));
  const canvas = document.createElement("canvas");
  canvas.width = Math.round(bitmap.width * scale);
  canvas.height = Math.round(bitmap.height * scale);
  canvas.getContext("2d").drawImage(bitmap, 0, 0, canvas.width, canvas.height);
  if (bitmap.close) bitmap.close();
  return new Promise((resolve, reject) =>
    canvas.toBlob((blob) => (blob ? resolve(blob) : reject(new Error("could not encode photo"))), "image/jpeg", 0.85)
  );
}

export async function addPhoto(siteId, file) {
  const blob = await shrinkPhoto(file);
  const { tx, store: s } = await store("photos", "readwrite");
  s.add({ siteId, blob, type: blob.type, addedAt: new Date().toISOString() });
  await txDone(tx);
  askToPersist();
  emit();
}

export async function photos(siteId) {
  if (!supported()) return [];
  try {
    const { store: s } = await store("photos", "readonly");
    return await done(siteId ? s.index("siteId").getAll(siteId) : s.getAll());
  } catch (error) {
    return [];
  }
}

export async function deletePhoto(id) {
  const { tx, store: s } = await store("photos", "readwrite");
  s.delete(id);
  await txDone(tx);
  emit();
}

/** Count of photos per site id. */
export async function photoCounts() {
  const counts = new Map();
  for (const photo of await photos()) counts.set(photo.siteId, (counts.get(photo.siteId) || 0) + 1);
  return counts;
}

// ---------- backup ----------

function blobToDataUrl(blob) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result);
    reader.onerror = () => reject(reader.error);
    reader.readAsDataURL(blob);
  });
}

function dataUrlToBlob(dataUrl) {
  const match = /^data:(image\/(?:jpeg|png|webp));base64,([A-Za-z0-9+/=]+)$/.exec(dataUrl || "");
  if (!match) return null;
  const bytes = Uint8Array.from(atob(match[2]), (c) => c.charCodeAt(0));
  return new Blob([bytes], { type: match[1] });
}

/** Everything as one JSON-able object. */
export async function exportBackup() {
  const entries = [...(await all()).values()];
  const pics = await photos();
  return {
    format: BACKUP_FORMAT,
    version: BACKUP_VERSION,
    exportedAt: new Date().toISOString(),
    sites: entries,
    photos: await Promise.all(pics.map(async (p) => ({ siteId: p.siteId, addedAt: p.addedAt, data: await blobToDataUrl(p.blob) }))),
  };
}

/**
 * Check a backup before loading it. Returns { ok, error, sites, photos } with only well-formed rows kept.
 * knownSiteIds: a Set of site ids in this atlas; rows for other ids are still kept (a custom or future site).
 */
export function validateBackup(data) {
  if (!data || data.format !== BACKUP_FORMAT) return { ok: false, error: "This file is not a Southern Indiana Lore backup." };
  if (typeof data.version !== "number" || data.version > BACKUP_VERSION) {
    return { ok: false, error: "This backup was made by a newer version of the atlas. Reload the atlas and try again." };
  }
  const idOk = (id) => typeof id === "string" && /^[A-Za-z0-9_-]{1,120}$/.test(id);
  const dateOk = (d) => d === null || (typeof d === "string" && !Number.isNaN(Date.parse(d)));
  const sites = (Array.isArray(data.sites) ? data.sites : [])
    .filter((row) => row && idOk(row.siteId) && dateOk(row.visitedAt ?? null))
    .map((row) => ({
      siteId: row.siteId,
      favorite: row.favorite === true,
      visitedAt: row.visitedAt || null,
      visitedNear: row.visitedNear === true,
      note: typeof row.note === "string" ? row.note.slice(0, 20000) : "",
      updatedAt: dateOk(row.updatedAt ?? null) ? row.updatedAt || null : null,
    }));
  const pics = (Array.isArray(data.photos) ? data.photos : []).filter(
    (p) => p && idOk(p.siteId) && typeof p.data === "string" && p.data.startsWith("data:image/")
  );
  return { ok: true, sites, photos: pics };
}

/** Merge a validated backup into this device: notes are combined, a visit or save on either side is kept. */
export async function importBackup(data) {
  const checked = validateBackup(data);
  if (!checked.ok) return checked;
  const entries = await all();
  const db = await open();
  const tx = db.transaction(["sites", "photos"], "readwrite");
  const siteStore = tx.objectStore("sites");
  for (const row of checked.sites) {
    const mine = entries.get(row.siteId) || blankEntry(row.siteId);
    const notes = [mine.note, row.note].map((n) => (n || "").trim()).filter(Boolean);
    const merged = {
      siteId: row.siteId,
      favorite: mine.favorite || row.favorite,
      visitedAt: [mine.visitedAt, row.visitedAt].filter(Boolean).sort()[0] || null, // keep the first visit
      visitedNear: mine.visitedNear || row.visitedNear,
      note: [...new Set(notes)].join("\n\n"),
      updatedAt: new Date().toISOString(),
    };
    if (isEmptyEntry(merged)) continue;
    siteStore.put(merged);
    entries.set(row.siteId, merged);
  }
  const photoStore = tx.objectStore("photos");
  let added = 0;
  for (const p of checked.photos) {
    const blob = dataUrlToBlob(p.data);
    if (!blob) continue;
    photoStore.add({ siteId: p.siteId, blob, type: blob.type, addedAt: p.addedAt || new Date().toISOString() });
    added += 1;
  }
  await txDone(tx);
  askToPersist();
  emit();
  return { ok: true, sites: checked.sites.length, photos: added };
}

export async function clearAll() {
  if (!supported()) return;
  const db = await open();
  const tx = db.transaction(["sites", "photos", "prefs"], "readwrite");
  tx.objectStore("sites").clear();
  tx.objectStore("photos").clear();
  tx.objectStore("prefs").clear();
  await txDone(tx);
  cache = new Map();
  emit();
}
