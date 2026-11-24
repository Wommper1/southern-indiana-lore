// Driving tours: a list of tours, and a page per tour with its stops in order, a "next stop" panel while the tour
// runs, directions handed off to a map app, a GPX download, and a simulated drive for trying it from a desk.

import { h, clear } from "../lib/dom.js";
import { distanceMeters, formatDistance, cardinal, bearingDegrees } from "../lib/geo.js";
import * as position from "../lib/position.js";
import * as announcer from "../lib/announcer.js";
import * as journal from "../lib/journal.js";
import * as tourrun from "../lib/tourrun.js";
import { unlock, speechSupported } from "../lib/speech.js";
import { tourToGpx, gpxFileName } from "../lib/gpx.js";
import { badge } from "../ui.js";

export function tourSites(ctx, tour) {
  return tour.stops.map((id) => ctx.byId.get(id)).filter(Boolean);
}

/** Straight-line length of a tour in meters (the drive is always longer). */
export function tourLength(sites) {
  let total = 0;
  for (let i = 1; i < sites.length; i += 1) {
    total += distanceMeters(sites[i - 1].latitude, sites[i - 1].longitude, sites[i].latitude, sites[i].longitude);
  }
  return total;
}

function directionsLinks(site) {
  const ll = site.latitude + "," + site.longitude;
  return h("p", { class: "map-links" },
    h("a", { href: "https://www.google.com/maps/dir/?api=1&destination=" + ll, target: "_blank", rel: "noopener noreferrer" }, "Directions in Google Maps"),
    " · ",
    h("a", { href: "https://maps.apple.com/?daddr=" + ll + "&q=" + encodeURIComponent(site.title), target: "_blank", rel: "noopener noreferrer" }, "Directions in Apple Maps"),
    h("span", { class: "fine" }, " These open another app. Pins are approximate; confirm before you go."));
}

function download(name, text, type) {
  const url = URL.createObjectURL(new Blob([text], { type }));
  const a = h("a", { href: url, download: name, class: "visually-hidden" }, "download");
  document.body.appendChild(a);
  a.click();
  a.remove();
  setTimeout(() => URL.revokeObjectURL(url), 10000);
}

export function renderTours(ctx) {
  const list = h("ul", { class: "card-list" });
  const tours = ctx.tours || [];
  function paint(entries) {
    clear(list);
    for (const tour of tours) {
      const sites = tourSites(ctx, tour);
      const seen = sites.filter((s) => entries && entries.get(s.id) && entries.get(s.id).visitedAt).length;
      const running = tourrun.current() && tourrun.current().tour.id === tour.id;
      list.appendChild(h("li", null,
        h("a", { class: "card tour-card", href: "#/tour/" + encodeURIComponent(tour.id) },
          h("div", { class: "card-top" }, h("span", { class: "tour-kicker" }, sites.length + " stops · " + tour.duration),
            running ? h("span", { class: "card-marks" }, "In progress") : seen ? h("span", { class: "card-marks" }, seen + "/" + sites.length + " visited") : null),
          h("h3", { class: "card-title" }, tour.title),
          h("p", { class: "card-sub" }, tour.summary),
          h("p", { class: "card-meta" }, "About " + formatDistance(tourLength(sites)) + " between stops in a straight line; the drive is longer."))));
    }
  }
  paint(null);
  journal.all().then(paint);
  const el = h("div", { class: "view view-tours" },
    h("h1", { class: "page-title" }, "Driving tours"),
    h("p", { class: "lede" }, "Ready-made routes through the sites. Follow one with your location on and the atlas tells you when you reach each stop, or download it for your GPS."),
    tours.length ? list : h("p", { class: "empty" }, "The tours could not be loaded. Reload the page to try again."),
    h("p", { class: "fine" }, "Tours only follow you while this page is open and the screen is on. Background alerts with the screen off are an Android-app feature."));
  return { el, title: "Tours" };
}

export function renderTour(ctx, id) {
  const tour = (ctx.tours || []).find((t) => t.id === id);
  if (!tour) {
    return { el: h("div", { class: "view" }, h("h1", { class: "page-title" }, "Tour not found"), h("p", null, h("a", { href: "#/tours" }, "All tours"))), title: "Tour not found" };
  }
  const sites = tourSites(ctx, tour);
  const stopsList = h("ol", { class: "tour-stops" });
  const runPanel = h("section", { class: "panel tour-run", "aria-live": "polite" });
  const startBtn = h("button", { type: "button", class: "btn btn-primary" }, "Start this tour");
  let entries = new Map();

  const isRunning = () => Boolean(tourrun.current() && tourrun.current().tour.id === tour.id);

  function paintStops() {
    clear(stopsList);
    const run = isRunning() ? tourrun.current() : null;
    const next = run ? tourrun.nextStop() : null;
    sites.forEach((site, i) => {
      const level = ctx.levelTable[site.confidenceLevel];
      const hop = i > 0 ? distanceMeters(sites[i - 1].latitude, sites[i - 1].longitude, site.latitude, site.longitude) : 0;
      const e = entries.get(site.id);
      const marks = [
        run && run.reached.has(site.id) ? "✓ Reached" : "",
        next && next.id === site.id ? "→ Next" : "",
        e && e.visitedAt ? "★ In passport" : "",
      ].filter(Boolean).join(" · ");
      stopsList.appendChild(h("li", { class: "tour-stop" + (next && next.id === site.id ? " is-next" : "") },
        h("a", { class: "card", href: "#/site/" + encodeURIComponent(site.id), "data-level": level.id },
          h("div", { class: "card-top" }, badge(level, { compact: true }), marks ? h("span", { class: "card-marks" }, marks) : null),
          h("h3", { class: "card-title" }, h("span", { class: "stop-num" }, String(i + 1)), site.title),
          h("p", { class: "card-meta" }, site.county + (i > 0 ? " · " + formatDistance(hop) + " from the last stop (straight line)" : " · start here")))));
    });
  }

  function paintRun() {
    clear(runPanel);
    startBtn.hidden = isRunning();
    if (!isRunning()) {
      runPanel.hidden = true;
      return;
    }
    runPanel.hidden = false;
    const run = tourrun.current();
    const next = tourrun.nextStop();
    const pos = position.getState();
    const fix = pos.fix;

    const readBox = h("input", { type: "checkbox", id: "tour-read" });
    readBox.checked = announcer.isEnabled();
    readBox.disabled = !speechSupported;
    readBox.addEventListener("change", () => {
      unlock();
      announcer.setEnabled(readBox.checked);
      if (fix) announcer.handleFix(fix);
    });
    const stampBox = h("input", { type: "checkbox", id: "tour-stamp" });
    stampBox.checked = run.stampPassport;
    stampBox.disabled = !journal.supported();
    stampBox.addEventListener("change", () => tourrun.setStampPassport(stampBox.checked));

    const locationLine = position.isActive()
      ? (pos.demo ? (run.simulating ? "Simulated drive: a pretend car is moving between the stops." : "Using a demo location, not your real one.") : fix ? "Following your location." : "Waiting for your location…")
      : "Location is off, so the tour cannot notice when you arrive. Turn it on below, or tap \"I'm here\" at each stop.";

    runPanel.append(
      h("h2", { class: "panel-title" }, "Tour in progress · " + run.reached.size + " of " + sites.length + " reached"),
      next
        ? h("div", { class: "next-stop" },
            h("p", { class: "fine" }, "Next stop"),
            h("h3", { class: "next-title" }, h("a", { href: "#/site/" + encodeURIComponent(next.id) }, next.title)),
            fix ? h("p", { class: "card-distance" }, formatDistance(distanceMeters(fix.lat, fix.lon, next.latitude, next.longitude)) + " " +
              cardinal(bearingDegrees(fix.lat, fix.lon, next.latitude, next.longitude)) + " (straight line)") : null,
            directionsLinks(next),
            h("div", { class: "actions" },
              h("button", { type: "button", class: "btn", onclick: () => {
                tourrun.markReached(next.id);
                if (run.stampPassport && journal.supported()) journal.setVisited(next.id, true, false);
              } }, "I'm here"),
              h("button", { type: "button", class: "btn", onclick: () => tourrun.markReached(next.id) }, "Skip this stop")))
        : h("p", { class: "tour-done" }, "Tour complete. Well driven!"),
      h("p", { class: "fine" }, locationLine),
      h("div", { class: "actions" },
        !position.isActive() || pos.demo
          ? h("button", { type: "button", class: "btn", onclick: () => {
              unlock();
              tourrun.stopSimulation();
              position.start();
            } }, "Use my location")
          : null,
        run.simulating
          ? h("button", { type: "button", class: "btn", onclick: () => { tourrun.stopSimulation(); position.stop(); } }, "Stop the simulation")
          : h("button", { type: "button", class: "btn", onclick: () => { unlock(); tourrun.simulate(); } }, "Simulate the drive")),
      h("label", { class: "check", for: "tour-read" }, readBox, h("span", null, speechSupported ? "Read each stop aloud when I arrive" : "Read aloud is not available in this browser")),
      h("label", { class: "check", for: "tour-stamp" }, stampBox, h("span", null, "Stamp my passport when I really arrive (never during a simulation)")),
      h("div", { class: "actions" }, h("button", { type: "button", class: "btn btn-danger", onclick: () => {
        tourrun.end();
        if (position.getState().demo) position.stop();
      } }, "End tour"))
    );
  }

  startBtn.addEventListener("click", () => {
    unlock();
    tourrun.start(tour, sites, { stampPassport: journal.supported() });
  });

  const gpxBtn = h("button", { type: "button", class: "btn" }, "Download GPX");
  gpxBtn.addEventListener("click", () => {
    const base = location.href.split("#")[0];
    const gpx = tourToGpx(tour, sites, (s) => ctx.levelTable[s.confidenceLevel].label, (s) => base + "#/site/" + encodeURIComponent(s.id));
    download(gpxFileName(tour), gpx, "application/gpx+xml");
  });

  const paintAll = () => {
    paintRun();
    paintStops();
  };
  const unsubscribers = [tourrun.subscribe(paintAll), position.subscribe(paintRun), journal.subscribe(async () => {
    entries = await journal.all();
    paintStops();
  })];
  journal.all().then((loaded) => {
    entries = loaded;
    paintStops();
  });
  paintAll();

  const el = h("div", { class: "view view-tour" },
    h("a", { class: "back", href: "#/tours" }, "← All tours"),
    h("h1", { class: "page-title" }, tour.title),
    h("p", { class: "lede" }, tour.summary),
    h("p", { class: "meta" }, sites.length + " stops · " + tour.duration + " · about " + formatDistance(tourLength(sites)) + " between stops in a straight line"),
    h("div", { class: "actions" }, startBtn, h("a", { class: "btn", href: "#/map?tour=" + encodeURIComponent(tour.id) }, "Show on map"), gpxBtn),
    runPanel,
    h("h2", { class: "panel-title section-gap" }, "Stops in order"),
    stopsList,
    h("section", { class: "panel honest" },
      h("h2", { class: "panel-title" }, "Before you drive"),
      h("ul", { class: "plain" },
        h("li", null, "The tour follows you only while this page is open and the screen is on. Keep your eyes on the road; let a passenger handle the phone."),
        h("li", null, "Distances are straight lines between pins. Use the directions links or the GPX file for real roads."),
        h("li", null, "Many sites are on private land or near active rail lines. View them from public places and obey posted signs."),
        h("li", null, "Your location stays on this device. The tour remembers nothing after you close the page, apart from passport stamps you choose to keep."))));

  return { el, title: tour.title, destroy: () => unsubscribers.forEach((fn) => fn()) };
}
