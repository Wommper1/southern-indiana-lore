// Share a site or tour: the phone's share sheet (or copy the link), and a QR code drawn on this device, which can be
// downloaded and printed (for a club handout or a sign at a public site). Nothing is sent anywhere by this page.

import { h, svg, clear } from "../lib/dom.js";
import qrcode from "../../vendor/qrcode-generator/qrcode.mjs";

/** The public address of a page in this atlas, for example #/site/<id>. */
export function pageUrl(hash) {
  return location.href.split("#")[0] + hash;
}

/** A QR code's dark modules for text, as a square boolean grid. */
export function qrMatrix(text) {
  const qr = qrcode(0, "M"); // automatic size, medium error correction (survives a smudge on a printed sign)
  qr.addData(text, "Byte");
  qr.make();
  const n = qr.getModuleCount();
  const rows = [];
  for (let r = 0; r < n; r += 1) {
    const row = [];
    for (let c = 0; c < n; c += 1) row.push(qr.isDark(r, c));
    rows.push(row);
  }
  return rows;
}

const QUIET = 4; // blank border, in modules, that scanners need

function qrSvg(matrix, label) {
  const n = matrix.length + QUIET * 2;
  let d = "";
  matrix.forEach((row, r) => row.forEach((dark, c) => {
    if (dark) d += "M" + (c + QUIET) + " " + (r + QUIET) + "h1v1h-1z";
  }));
  return svg("svg", { viewBox: "0 0 " + n + " " + n, class: "qr", role: "img", "aria-label": label, "shape-rendering": "crispEdges" },
    svg("rect", { width: n, height: n, fill: "#ffffff" }),
    svg("path", { d, fill: "#000000" }));
}

function qrPng(matrix, scale) {
  const n = matrix.length + QUIET * 2;
  const canvas = document.createElement("canvas");
  canvas.width = canvas.height = n * scale;
  const g = canvas.getContext("2d");
  g.fillStyle = "#fff";
  g.fillRect(0, 0, canvas.width, canvas.height);
  g.fillStyle = "#000";
  matrix.forEach((row, r) => row.forEach((dark, c) => {
    if (dark) g.fillRect((c + QUIET) * scale, (r + QUIET) * scale, scale, scale);
  }));
  return canvas;
}

/** options: { title, hash, fileName, extra (optional node, e.g. a correction link) } */
export function sharePanel(options) {
  const url = pageUrl(options.hash);
  const status = h("span", { class: "fine", role: "status", "aria-live": "polite" });
  const qrSlot = h("div", { class: "qr-slot", hidden: true });
  const shareBtn = h("button", { type: "button", class: "btn" }, typeof navigator.share === "function" ? "Share" : "Copy link");
  const qrBtn = h("button", { type: "button", class: "btn", "aria-expanded": "false" }, "QR code");

  shareBtn.addEventListener("click", async () => {
    status.textContent = "";
    if (typeof navigator.share === "function") {
      try {
        await navigator.share({ title: options.title, url });
        return;
      } catch (error) {
        if (error && error.name === "AbortError") return; // the person closed the share sheet
      }
    }
    try {
      await navigator.clipboard.writeText(url);
      status.textContent = "Link copied.";
    } catch (error) {
      status.textContent = url; // last resort: show it so it can be copied by hand
    }
  });

  qrBtn.addEventListener("click", () => {
    const open = qrSlot.hidden;
    qrSlot.hidden = !open;
    qrBtn.setAttribute("aria-expanded", String(open));
    if (!open || qrSlot.firstChild) return;
    const matrix = qrMatrix(url);
    const save = h("button", { type: "button", class: "btn btn-small" }, "Download for printing");
    save.addEventListener("click", () => {
      const a = h("a", { href: qrPng(matrix, 16).toDataURL("image/png"), download: options.fileName + "-qr.png", class: "visually-hidden" }, "download");
      document.body.appendChild(a);
      a.click();
      a.remove();
    });
    clear(qrSlot);
    qrSlot.append(qrSvg(matrix, "QR code that opens " + options.title + " in the atlas"),
      h("p", { class: "fine" }, "Scanning it opens this page in the atlas. Made on this device."), save);
  });

  return h("section", { class: "panel panel-share", "aria-label": "Share" },
    h("h2", { class: "panel-title" }, "Share"),
    h("div", { class: "actions" }, shareBtn, qrBtn, status),
    qrSlot,
    options.extra || null);
}
