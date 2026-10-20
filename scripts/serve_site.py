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
}


class Handler(http.server.SimpleHTTPRequestHandler):
    extensions_map = {**http.server.SimpleHTTPRequestHandler.extensions_map, **TYPES}

    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=str(SITE_DIR), **kwargs)

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
