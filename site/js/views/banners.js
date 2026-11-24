// Two small notices under the top bar: "a new version is ready" and "install the atlas". Both can be closed.

import { h, clear } from "../lib/dom.js";
import * as offline from "../lib/offline.js";
import * as journal from "../lib/journal.js";

export function mountBanners(slot) {
  let installClosed = false;

  async function loadClosed() {
    try {
      installClosed = journal.supported() && (await journal.getPref("installHint")) === "closed";
    } catch (error) {
      installClosed = false;
    }
    paint();
  }

  function closeInstall() {
    installClosed = true;
    if (journal.supported()) journal.setPref("installHint", "closed").catch(() => {});
    paint();
  }

  function paint() {
    clear(slot);
    const state = offline.getState();
    if (state.updateReady) {
      slot.appendChild(h("div", { class: "banner", role: "status" },
        h("p", null, "A new version of the atlas is ready."),
        h("button", { type: "button", class: "btn btn-small btn-primary", onclick: () => location.reload() }, "Reload")));
      return;
    }
    if (installClosed || offline.isStandalone()) return;
    if (offline.canInstall()) {
      slot.appendChild(h("div", { class: "banner" },
        h("p", null, "Install the atlas for quick access and offline use. No app store, no account."),
        h("button", { type: "button", class: "btn btn-small btn-primary", onclick: () => offline.promptInstall() }, "Install"),
        h("button", { type: "button", class: "banner-close", "aria-label": "Close", onclick: closeInstall }, "×")));
    } else if (offline.isAppleMobile()) {
      slot.appendChild(h("div", { class: "banner" },
        h("p", null, "On iPhone or iPad: tap ", h("strong", null, "Share"), " then ", h("strong", null, "Add to Home Screen"),
          ". It opens like an app, works offline, and Safari keeps your passport and notes."),
        h("button", { type: "button", class: "banner-close", "aria-label": "Close", onclick: closeInstall }, "×")));
    }
  }

  offline.subscribe(paint);
  loadClosed();
}
