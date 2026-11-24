// GPX 1.1 export for a tour: one waypoint per stop plus a route through them in order, for Garmin units, OsmAnd,
// Gaia, Google Earth or QGIS. Built entirely on the device. The route joins stops in straight lines; a GPS unit or
// navigation app works out the actual roads.

const esc = (text) =>
  String(text == null ? "" : text)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    // XML 1.0 cannot hold most control characters at all
    .replace(/[\u0000-\u0008\u000B\u000C\u000E-\u001F]/g, "");

const coord = (n) => Number(n).toFixed(6);

/**
 * tour: { id, title, summary }, sites: the stop site objects in order, levelLabel(site) -> trust label text,
 * link(site) -> absolute URL of the site page (optional).
 */
export function tourToGpx(tour, sites, levelLabel, link) {
  const desc = (site) => [levelLabel(site), site.county, site.subtitle].filter(Boolean).join(" · ");
  const linkTag = (site) => (link ? '\n    <link href="' + esc(link(site)) + '"><text>Site page</text></link>' : "");
  const wpts = sites.map((site, i) =>
    '  <wpt lat="' + coord(site.latitude) + '" lon="' + coord(site.longitude) + '">\n' +
    "    <name>" + esc(i + 1 + ". " + site.title) + "</name>\n" +
    "    <desc>" + esc(desc(site)) + "</desc>" + linkTag(site) + "\n" +
    "    <sym>Waypoint</sym>\n" +
    "  </wpt>"
  );
  const rtepts = sites.map((site, i) =>
    '    <rtept lat="' + coord(site.latitude) + '" lon="' + coord(site.longitude) + '"><name>' + esc(i + 1 + ". " + site.title) + "</name></rtept>"
  );
  return [
    '<?xml version="1.0" encoding="UTF-8"?>',
    '<gpx version="1.1" creator="Southern Indiana Lore Atlas" xmlns="http://www.topografix.com/GPX/1/1">',
    "  <metadata>",
    "    <name>" + esc(tour.title) + "</name>",
    "    <desc>" + esc(tour.summary + " Pins are approximate; many sites are on private land or near active rail lines.") + "</desc>",
    "  </metadata>",
    ...wpts,
    "  <rte>",
    "    <name>" + esc(tour.title) + "</name>",
    ...rtepts,
    "  </rte>",
    "</gpx>",
    "",
  ].join("\n");
}

export function gpxFileName(tour) {
  return "southern-indiana-lore-" + tour.id.replace(/[^a-z0-9_-]/gi, "-") + ".gpx";
}
