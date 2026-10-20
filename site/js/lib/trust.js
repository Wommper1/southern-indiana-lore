// Trust layer: how the atlas words and groups history, mirroring the Android app.
// Source of truth for labels and descriptions is ConfidenceLevel.kt (copied into data/atlas.json by
// scripts/build_site_data.py). The wording below mirrors SiteDetailDialog.kt. The rule is the app's rule:
// never claim more certainty than the data supports, and unknown values fail closed to UNVERIFIED.

export const FALLBACK_LEVEL = "UNVERIFIED";

// Only these levels get the "documented" heading. Everything else, including Plausible, is "not fully verified".
const DOCUMENTED = new Set(["OFFICIAL_SOURCE", "VERIFIED", "WELL_SUPPORTED"]);

export const HEADING_DOCUMENTED = "Documented archival history";
export const HEADING_NOT_VERIFIED = "History (not fully verified)";
export const HEADING_FOLKLORE = "Folklore & oral tradition";
export const HEADING_SOURCES = "Primary sources & citations";

export const NO_RECORD_TEXT =
  "No independent archival record or public deed established for this site. Historical claims remain unverified.";
export const FOLKLORE_ADVISORY =
  "Preserved as regional cultural heritage, community memory, or folklore. Not established as archival fact.";
export const NO_SOURCES_TEXT = "No primary sources or archival deeds cited. Documented from oral or field testimony.";

/** Fail closed: anything not in the table is UNVERIFIED. */
export function resolveLevel(levelId, table) {
  const id = typeof levelId === "string" ? levelId.trim().toUpperCase().replace(/ /g, "_") : "";
  if (table[id]) return table[id];
  return (
    table[FALLBACK_LEVEL] || {
      id: FALLBACK_LEVEL,
      label: "Unverified",
      description: "Not yet independently verified against archival or historical documentation",
    }
  );
}

export function isDocumented(levelId) {
  return DOCUMENTED.has(levelId);
}

export function historyHeading(levelId) {
  return isDocumented(levelId) ? HEADING_DOCUMENTED : HEADING_NOT_VERIFIED;
}

/** The text under the history heading, with the app's fallbacks when the field is blank. */
export function historyText(site) {
  if (site.verifiedHistory && site.verifiedHistory.trim()) return site.verifiedHistory;
  if (site.confidenceLevel === "LEGEND" || site.confidenceLevel === "UNVERIFIED") return NO_RECORD_TEXT;
  return site.fullStory;
}

/** A short reminder shown inside the history panel, so the text is never read as more certain than its badge. */
export function historyCaveat(levelId) {
  if (isDocumented(levelId)) return null;
  if (levelId === "PLAUSIBLE" || levelId === "PROBABLE") {
    return "Checked against public sources, but some details are unconfirmed. Read this as supported, not proven.";
  }
  return "Not confirmed by independent sources. Read these details as unverified claims, not established fact.";
}

export function showsFolklorePanel(site) {
  return Boolean(
    (site.folkloreTradition && site.folkloreTradition.trim()) ||
      site.confidenceLevel === "LEGEND" ||
      site.confidenceLevel === "ORAL_HISTORY"
  );
}

export function folkloreText(site) {
  return site.folkloreTradition && site.folkloreTradition.trim() ? site.folkloreTradition : site.fullStory;
}

export function sourcesText(site) {
  let text = site.sources && site.sources.trim() ? site.sources : "";
  if (site.sourceNotes && site.sourceNotes.trim() && site.sourceNotes !== site.sources) {
    text = text ? text + "\n\nNotes: " + site.sourceNotes : site.sourceNotes;
  }
  return text || NO_SOURCES_TEXT;
}

/** What read-aloud says. It always names the trust level, so the audio never sounds more certain than the page. */
export function spokenText(site, level) {
  const parts = [site.title + ".", "Trust level: " + level.label + "."];
  if (site.audioNarration) parts.push(site.audioNarration);
  if (!isDocumented(site.confidenceLevel)) parts.push("Note: this history is not fully verified.");
  return parts.join(" ");
}

/** Split a block of text into paragraphs on blank lines. */
export function paragraphs(text) {
  return String(text || "")
    .split(/\n\s*\n/)
    .map((part) => part.trim())
    .filter(Boolean);
}
