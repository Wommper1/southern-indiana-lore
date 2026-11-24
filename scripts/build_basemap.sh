#!/usr/bin/env bash
# Builds the web atlas's street map: cuts southern Indiana out of the latest Protomaps OpenStreetMap build and writes
#   site/map/southern-indiana.pmtiles   one file, read by the browser in small pieces (HTTP range requests)
#   site/map/basemap.json               its size, date, area and attribution; the page shows the street map only if
#                                       this file exists
# The map is not committed to git (it is tens of megabytes). The "Deploy web atlas" workflow runs this script before
# every publish. If it fails, the site still publishes and the Map tab shows the schematic map instead.
#
#   bash scripts/build_basemap.sh                 # needs curl, python3 and network access
#   BASEMAP_MAXZOOM=14 bash scripts/build_basemap.sh
#
# Map data © OpenStreetMap contributors, under the Open Database License (ODbL); packaged by Protomaps.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/site/map"
MAXZOOM="${BASEMAP_MAXZOOM:-13}"   # 13 keeps the file small; the browser can still zoom further in, just less detail
# The lore sites span about 37.91..39.08 N and 87.94..85.39 W. Add a margin so the edges are not bare.
BBOX="${BASEMAP_BBOX:--88.10,37.75,-85.25,39.25}"   # west,south,east,north
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

# 1. The pmtiles command-line tool (go-pmtiles), from its latest GitHub release.
auth=()
[ -n "${GITHUB_TOKEN:-}" ] && auth=(-H "Authorization: Bearer $GITHUB_TOKEN")
asset_url="$(curl -fsSL "${auth[@]}" https://api.github.com/repos/protomaps/go-pmtiles/releases/latest \
  | python3 -c 'import json,sys; a=[x["browser_download_url"] for x in json.load(sys.stdin)["assets"] if x["name"].endswith("_Linux_x86_64.tar.gz")]; print(a[0])')"
echo "pmtiles tool: $asset_url"
curl -fsSL "$asset_url" | tar xz -C "$WORK"
PMTILES="$WORK/pmtiles"

# 2. The newest daily planet build. Protomaps lists them; fall back to trying recent dates.
build=""
if list="$(curl -fsSL https://build-metadata.protomaps.dev/builds.json 2>/dev/null)"; then
  build="$(printf '%s' "$list" | python3 -c 'import json,sys; b=sorted(x["key"] for x in json.load(sys.stdin) if x["key"].endswith(".pmtiles")); print(b[-1] if b else "")')"
fi
if [ -z "$build" ]; then
  for days in $(seq 0 14); do
    candidate="$(date -u -d "-$days day" +%Y%m%d).pmtiles"
    if curl -fsI "https://build.protomaps.com/$candidate" >/dev/null 2>&1; then build="$candidate"; break; fi
  done
fi
[ -n "$build" ] || { echo "No Protomaps build found" >&2; exit 1; }
echo "Protomaps build: $build"

# 3. Cut out the region.
mkdir -p "$OUT"
"$PMTILES" extract "https://build.protomaps.com/$build" "$OUT/southern-indiana.pmtiles" --bbox="$BBOX" --maxzoom="$MAXZOOM"
"$PMTILES" verify "$OUT/southern-indiana.pmtiles"

bytes="$(stat -c %s "$OUT/southern-indiana.pmtiles")"
built="${build%.pmtiles}"
built="${built:0:4}-${built:4:2}-${built:6:2}"
python3 - "$OUT/basemap.json" "$bytes" "$built" "$BBOX" "$MAXZOOM" <<'EOF'
import json, sys
path, size, built, bbox, maxzoom = sys.argv[1:]
info = {
    "file": "map/southern-indiana.pmtiles",
    "bytes": int(size),
    "built": built,
    "bbox": [float(x) for x in bbox.split(",")],
    "maxzoom": int(maxzoom),
    "attribution": '<a href="https://www.openstreetmap.org/copyright" target="_blank" rel="noopener noreferrer">© OpenStreetMap contributors</a> · <a href="https://protomaps.com" target="_blank" rel="noopener noreferrer">Protomaps</a>',
}
with open(path, "w", encoding="utf-8") as f:
    json.dump(info, f, indent=2)
    f.write("\n")
EOF
echo "Wrote $OUT/southern-indiana.pmtiles ($bytes bytes) and $OUT/basemap.json"
