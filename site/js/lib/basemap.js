// The street map: a single map file of southern Indiana (OpenStreetMap data packaged by Protomaps) that is published
// next to this page. It is read in small pieces straight from this site, so no map company sees where anyone looks.
// When someone taps "Save map for offline", the whole file is copied into this browser's offline storage; the
// service worker (sw.js) then answers map requests from that copy. Nothing about the visitor is stored or sent.

export const MAP_FILE = "map/southern-indiana.pmtiles";
export const MAP_INFO = "map/basemap.json";
export const MAP_CACHE = "atlas-basemap-v1"; // must match sw.js

let infoPromise = null;

/** The published map's description ({ bytes, built, bbox, maxzoom, attribution }), or null if there is no map. */
export function loadInfo() {
  if (!infoPromise) {
    infoPromise = (async () => {
      try {
        const response = await fetch(MAP_INFO, { cache: "no-cache" });
        if (!response.ok) return null;
        const info = await response.json();
        return info && info.file && info.bytes > 0 ? info : null;
      } catch (error) {
        return null;
      }
    })();
  }
  return infoPromise;
}

export function webglSupported() {
  try {
    const canvas = document.createElement("canvas");
    return Boolean(canvas.getContext("webgl2") || canvas.getContext("webgl"));
  } catch (error) {
    return false;
  }
}

function cachesSupported() {
  return typeof caches !== "undefined" && window.isSecureContext;
}

export async function isSaved() {
  if (!cachesSupported()) return false;
  try {
    const cache = await caches.open(MAP_CACHE);
    return Boolean(await cache.match(MAP_FILE));
  } catch (error) {
    return false;
  }
}

export function canSave() {
  return cachesSupported() && "serviceWorker" in navigator;
}

/** Download the whole map into offline storage. onProgress(fraction 0..1). Resolves true when saved. */
export async function save(onProgress, signal) {
  const info = await loadInfo();
  if (!info || !canSave()) return false;
  const response = await fetch(MAP_FILE, { cache: "no-store", signal });
  if (!response.ok || !response.body) throw new Error("HTTP " + response.status);
  const total = Number(response.headers.get("Content-Length")) || info.bytes;
  const reader = response.body.getReader();
  const chunks = [];
  let received = 0;
  for (;;) {
    const { done, value } = await reader.read();
    if (done) break;
    chunks.push(value);
    received += value.length;
    if (onProgress) onProgress(Math.min(1, received / total));
  }
  const blob = new Blob(chunks, { type: "application/octet-stream" });
  const cache = await caches.open(MAP_CACHE);
  await cache.put(MAP_FILE, new Response(blob, { headers: { "Content-Type": "application/octet-stream", "Content-Length": String(blob.size) } }));
  await cache.put(MAP_INFO, new Response(JSON.stringify(info), { headers: { "Content-Type": "application/json" } }));
  return true;
}

export async function remove() {
  if (!cachesSupported()) return;
  await caches.delete(MAP_CACHE);
}

export function formatBytes(bytes) {
  if (bytes >= 1e6) return (bytes / 1e6).toFixed(bytes >= 1e8 ? 0 : 1) + " MB";
  return Math.max(1, Math.round(bytes / 1e3)) + " KB";
}
