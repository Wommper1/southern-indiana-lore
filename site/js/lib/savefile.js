// Hand a file to the visitor: a backup, a GPX tour, a QR code.
// iPhone and iPad save downloads unreliably, and not at all from a Home Screen app, so there the phone's share
// sheet is used instead ("Save to Files", AirDrop, Mail...). Everywhere else it is an ordinary download.
// The file is made on this device and goes only where the visitor sends it.

function isAppleMobile() {
  const ua = (typeof navigator !== "undefined" && navigator.userAgent) || "";
  return /iPhone|iPad|iPod/.test(ua) || (/Macintosh/.test(ua) && navigator.maxTouchPoints > 1);
}

function download(name, blob) {
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = name;
  a.className = "visually-hidden";
  document.body.appendChild(a);
  a.click();
  a.remove();
  setTimeout(() => URL.revokeObjectURL(url), 10000);
}

/** Resolves to "shared", "downloaded" or "cancelled". Call it from a tap. */
export async function saveFile(name, blob) {
  if (isAppleMobile() && typeof navigator.share === "function" && typeof File === "function") {
    const file = new File([blob], name, { type: blob.type || "application/octet-stream" });
    if (!navigator.canShare || navigator.canShare({ files: [file] })) {
      try {
        await navigator.share({ files: [file], title: name });
        return "shared";
      } catch (error) {
        if (error && error.name === "AbortError") return "cancelled";
        // Share failed for another reason (for example the tap expired): fall back to a download.
      }
    }
  }
  download(name, blob);
  return "downloaded";
}
