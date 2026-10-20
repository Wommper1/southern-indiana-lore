// Small geographic helpers. Everything here runs on the device; nothing is sent anywhere.

const EARTH_RADIUS_M = 6371008.8;
const METERS_PER_MILE = 1609.344;
const FEET_PER_METER = 3.28084;
const rad = (deg) => (deg * Math.PI) / 180;
const deg = (radians) => (radians * 180) / Math.PI;

/** Great-circle distance in meters. */
export function distanceMeters(lat1, lon1, lat2, lon2) {
  const dLat = rad(lat2 - lat1);
  const dLon = rad(lon2 - lon1);
  const a = Math.sin(dLat / 2) ** 2 + Math.cos(rad(lat1)) * Math.cos(rad(lat2)) * Math.sin(dLon / 2) ** 2;
  return 2 * EARTH_RADIUS_M * Math.asin(Math.min(1, Math.sqrt(a)));
}

/** Initial compass bearing from point 1 to point 2, in degrees from north (0-360). */
export function bearingDegrees(lat1, lon1, lat2, lon2) {
  const dLon = rad(lon2 - lon1);
  const y = Math.sin(dLon) * Math.cos(rad(lat2));
  const x = Math.cos(rad(lat1)) * Math.sin(rad(lat2)) - Math.sin(rad(lat1)) * Math.cos(rad(lat2)) * Math.cos(dLon);
  return (deg(Math.atan2(y, x)) + 360) % 360;
}

const POINTS = ["N", "NE", "E", "SE", "S", "SW", "W", "NW"];
export function cardinal(bearing) {
  return POINTS[Math.round((((bearing % 360) + 360) % 360) / 45) % 8];
}

/** US-style distance: feet when very close, otherwise miles. */
export function formatDistance(meters) {
  const miles = meters / METERS_PER_MILE;
  if (miles < 0.1) return Math.max(10, Math.round((meters * FEET_PER_METER) / 10) * 10) + " ft";
  if (miles < 10) return miles.toFixed(1) + " mi";
  return Math.round(miles) + " mi";
}

export function formatMeters(meters) {
  return Math.round(meters) + " m";
}

/** Equirectangular projection around a middle latitude: good enough for a region this small. */
export function makeProjection(sites) {
  let minLat = Infinity;
  let maxLat = -Infinity;
  let minLon = Infinity;
  let maxLon = -Infinity;
  for (const site of sites) {
    minLat = Math.min(minLat, site.latitude);
    maxLat = Math.max(maxLat, site.latitude);
    minLon = Math.min(minLon, site.longitude);
    maxLon = Math.max(maxLon, site.longitude);
  }
  const k = Math.cos(rad((minLat + maxLat) / 2));
  // Unit: degrees of latitude. x grows east, y grows north.
  const project = (lat, lon) => ({ x: (lon - minLon) * k, y: lat - minLat });
  const unproject = (x, y) => ({ lat: y + minLat, lon: x / k + minLon });
  return { project, unproject, width: (maxLon - minLon) * k, height: maxLat - minLat, minLat, maxLat, minLon, maxLon, k };
}

/** Meters per degree of latitude, for the map's scale bar. */
export const METERS_PER_DEGREE_LAT = (Math.PI * EARTH_RADIUS_M) / 180;

/** A "nice" round length (1, 2, 5 times a power of ten) no longer than maxMeters. */
export function niceLength(maxMeters) {
  const power = 10 ** Math.floor(Math.log10(maxMeters));
  for (const step of [5, 2, 1]) {
    if (step * power <= maxMeters) return step * power;
  }
  return power;
}

export function formatScale(meters) {
  const miles = meters / METERS_PER_MILE;
  if (miles >= 1) return Math.round(miles) + " mi";
  return miles.toFixed(1) + " mi";
}
