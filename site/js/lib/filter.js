// Search and filter for the site list. Pure functions, no DOM.

/** Lowercase and strip accents so "Cañon" matches "canon". */
export function normalizeText(text) {
  return String(text || "")
    .normalize("NFD")
    .replace(/[̀-ͯ]/g, "")
    .toLowerCase();
}

function searchBlob(site) {
  return normalizeText(
    [site.title, site.subtitle, site.county, site.category, site.era, site.address, site.alternativeNames].join(" ")
  );
}

/**
 * @param sites       every site
 * @param criteria    { q: string, county: string, levels: string[] }  (empty means "any")
 */
export function filterSites(sites, criteria) {
  const words = normalizeText(criteria.q).split(/\s+/).filter(Boolean);
  const levels = criteria.levels && criteria.levels.length ? new Set(criteria.levels) : null;
  return sites.filter((site) => {
    if (criteria.county && site.county !== criteria.county) return false;
    if (levels && !levels.has(site.confidenceLevel)) return false;
    if (!words.length) return true;
    const blob = site._search || searchBlob(site);
    return words.every((word) => blob.includes(word));
  });
}

export function indexSites(sites) {
  for (const site of sites) site._search = searchBlob(site);
  return sites;
}

const collator = new Intl.Collator("en", { sensitivity: "base", numeric: true });

export function sortSites(sites, mode, distances) {
  const list = sites.slice();
  if (mode === "near" && distances) {
    list.sort((a, b) => (distances.get(a.id) ?? Infinity) - (distances.get(b.id) ?? Infinity));
  } else if (mode === "county") {
    list.sort((a, b) => collator.compare(a.county, b.county) || collator.compare(a.title, b.title));
  } else {
    list.sort((a, b) => collator.compare(a.title, b.title));
  }
  return list;
}

/** Counts per value of a field, for filter labels. */
export function countBy(sites, field) {
  const counts = new Map();
  for (const site of sites) counts.set(site[field], (counts.get(site[field]) || 0) + 1);
  return counts;
}
