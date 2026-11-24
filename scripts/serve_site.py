#!/usr/bin/env python3
"""Preview the web atlas on your own computer.

    python scripts/serve_site.py            # then open http://localhost:8080/
    python scripts/serve_site.py 9000       # use another port

It serves the site/ folder with the right file types (some Windows setups serve .js as plain text, which stops
the atlas from loading) and tells the browser not to reuse old copies. To try it on a phone on the same Wi-Fi,
note that location and the offline copy only work on https pages or on localhost, so use a deployed copy for that.
Uses only the Python standard library.
"""
import http.server
import os
import re
import sys
from pathlib import Path

SITE_DIR = Path(__file__).resolve().parent.parent / "site"
TYPES = {
    ".js": "text/javascript; charset=utf-8",
    ".json": "application/json; charset=utf-8",
    ".webmanifest": "application/manifest+json; charset=utf-8",
    ".css": "text/css; charset=utf-8",
    ".html": "text/html; charset=utf-8",
    ".png": "image/png",
    ".ttf": "font/ttf",
    ".txt": "text/plain; charset=utf-8",
    ".svg": "image/svg+xml",
    ".mjs": "text/javascript; charset=utf-8",
    ".pbf": "application/x-protobuf",
    ".pmtiles": "application/octet-stream",
}
RANGE = re.compile(r"^bytes=(\d+)-(\d*)$")


class _Slice:
    """A file object that stops after `length` bytes, for range responses."""

    def __init__(self, f, length):
        self.f, self.left = f, length

    def read(self, n=-1):
        n = self.left if n < 0 else min(n, self.left)
        data = self.f.read(n)
        self.left -= len(data)
        return data

    def close(self):
        self.f.close()


class Handler(http.server.SimpleHTTPRequestHandler):
    extensions_map = {**http.server.SimpleHTTPRequestHandler.extensions_map, **TYPES}

    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=str(SITE_DIR), **kwargs)

    def send_head(self):
        # The street map is read in small pieces (HTTP range requests), as GitHub Pages allows. Support that here.
        match = RANGE.match(self.headers.get("Range", ""))
        path = self.translate_path(self.path)
        if not match or not os.path.isfile(path):
            return super().send_head()
        size = os.path.getsize(path)
        start = int(match.group(1))
        end = min(int(match.group(2)), size - 1) if match.group(2) else size - 1
        if start >= size or end < start:
            self.send_response(416)
            self.send_header("Content-Range", "bytes */%d" % size)
            self.end_headers()
            return None
        f = open(path, "rb")
        f.seek(start)
        self.send_response(206)
        self.send_header("Content-Type", self.guess_type(path))
        self.send_header("Content-Range", "bytes %d-%d/%d" % (start, end, size))
        self.send_header("Content-Length", str(end - start + 1))
        self.end_headers()
        return _Slice(f, end - start + 1)

    def end_headers(self):
        self.send_header("Cache-Control", "no-cache")
        super().end_headers()


def main():
    port = int(sys.argv[1]) if len(sys.argv) > 1 else 8080
    server = http.server.ThreadingHTTPServer(("127.0.0.1", port), Handler)
    print("Serving %s at http://localhost:%d/  (Ctrl+C to stop)" % (SITE_DIR, port))
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nStopped.")


if __name__ == "__main__":
    main()
