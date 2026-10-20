// Offline support: registers the service worker, reports whether the offline copy is complete,
// and handles the "install to home screen" prompt on browsers that offer one (Chrome on Android).

let state = { status: "checking", version: "", message: "checking…" };
let installEvent = null;
const listeners = new Set();

function set(next) {
  state = Object.assign({}, state, next);
  for (const listener of listeners) listener(state);
}

export function getState() {
  return state;
}

export function subscribe(listener) {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

export function canInstall() {
  return installEvent !== null;
}

export async function promptInstall() {
  if (!installEvent) return;
  const event = installEvent;
  installEvent = null;
  for (const listener of listeners) listener(state);
  event.prompt();
  try {
    await event.userChoice;
  } catch (error) {
    /* the person closed the prompt; nothing to do */
  }
}

window.addEventListener("beforeinstallprompt", (event) => {
  event.preventDefault();
  installEvent = event;
  for (const listener of listeners) listener(state);
});

window.addEventListener("appinstalled", () => {
  installEvent = null;
  for (const listener of listeners) listener(state);
});

function askWorkerForStatus(worker) {
  if (worker) worker.postMessage({ type: "status" });
}

export async function register() {
  if (!("serviceWorker" in navigator)) {
    set({ status: "unsupported", message: "not available in this browser." });
    return;
  }
  if (!window.isSecureContext) {
    set({ status: "unsupported", message: "needs a secure (https) page." });
    return;
  }
  navigator.serviceWorker.addEventListener("message", (event) => {
    const data = event.data || {};
    if (data.type === "status") {
      set(
        data.complete
          ? { status: "ready", version: data.version, message: "saved on this device (" + data.count + " files). The atlas works without a connection." }
          : { status: "saving", version: data.version, message: "saving…" }
      );
    }
  });
  try {
    const registration = await navigator.serviceWorker.register("sw.js", { scope: "./", updateViaCache: "none" });
    await navigator.serviceWorker.ready;
    askWorkerForStatus(registration.active);
    // A new version may still be installing; ask again when it takes over.
    registration.addEventListener("updatefound", () => {
      const next = registration.installing;
      if (!next) return;
      set({ status: "saving", message: "updating…" });
      next.addEventListener("statechange", () => {
        if (next.state === "activated") askWorkerForStatus(next);
      });
    });
  } catch (error) {
    set({ status: "error", message: "could not be set up in this browser." });
  }
}
