#!/usr/bin/env python3
"""Regenerate the web atlas data from the lore pack, so the website never drifts from the app.

The web atlas in site/ reads one generated file, site/data/atlas.json, built from the lore pack in
app/src/main/assets/lorepacks/southern_indiana/ (sites, geofences, sources, licenses) plus the confidence-level
labels and descriptions declared in the Android app's ConfidenceLevel.kt. This script also writes
site/precache.js, the list of files the offline service worker saves. Run it:

    python scripts/build_site_data.py           # rewrite site/data/atlas.json and site/precache.js
    python scripts/build_site_data.py --check   # verify only; exit 1 if either file is out of date

Run it again after you change the pack (after scripts/update_pack_manifest.py) OR any file inside site/, because
the offline file list includes a fingerprint of every site file; that fingerprint is what tells phones to fetch a
fresh copy. The script refuses to run when the pack files do not match pack_manifest.json.

It uses only the Python standard library and never touches the pack or the app source.
"""
import argparse
import hashlib
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
PACK_DIR = ROOT / "app" / "src" / "main" / "assets" / "lorepacks" / "southern_indiana"
CONFIDENCE_KT = ROOT / "app" / "src" / "main" / "java" / "com" / "example" / "data" / "model" / "ConfidenceLevel.kt"
SITE_DIR = ROOT / "site"
ATLAS_PATH = SITE_DIR / "data" / "atlas.json"
PRECACHE_PATH = SITE_DIR / "precache.js"

# Text files are fingerprinted with CRLF turned into LF, so the fingerprint is the same on Windows and Linux checkouts.
TEXT_SUFFIXES = {".html", ".css", ".js", ".json", ".webmanifest", ".svg", ".txt", ".md"}
# Not listed for offline use: the worker itself, the list we are generating, and editor/OS leftovers.
PRECACHE_SKIP = {"sw.js", "precache.js"}

# Fields the web atlas keeps from each site. Anything else in sites.json (photo paths, custom-site flags,
# map-evidence notes that the app never shows on a detail screen) is left out on purpose.
REQUIRED_SITE_FIELDS = [
    "id", "title", "subtitle", "category", "county", "latitude", "longitude", "address", "era", "accessStatus",
    "fullStory", "verifiedHistory", "folkloreTradition", "confidenceLevel", "sources", "sourceNotes",
    "verificationGrade", "geofenceRadiusMeters", "audioNarration",
]
OPTIONAL_SITE_FIELDS = [
    "alternativeNames", "driveStory", "whyHere", "historyNerdDiscovery", "aboveLayer", "surfaceLayer", "belowLayer",
    "time1800", "time1850", "time1900", "time1950", "timeToday", "whatMayBeUnderground", "undergroundConfidence",
    "visibleRemains", "relatedLocations", "relatedPeople", "relatedEvents", "unresolvedQuestions", "accessLevel",
    "siteType", "radarType",
]
# The app's ConfidenceLevel.fromString also accepts these spellings.
LEVEL_ALIASES = {"FOLKLORE_TRADITION": "FOLKLORE", "FOLK_LORE": "FOLKLORE"}


class BuildError(Exception):
    pass


def load_json(name):
    return json.loads((PACK_DIR / name).read_bytes().decode("utf-8"))


def verify_pack_hashes():
    manifest = load_json("pack_manifest.json")
    for entry in manifest["files"]:
        path = PACK_DIR / entry["path"]
        if not path.is_file():
            raise BuildError("pack file is missing: %s" % entry["path"])
        blob = path.read_bytes()
        if hashlib.sha256(blob).hexdigest() != entry["sha256"] or len(blob) != entry["bytes"]:
            raise BuildError(
                "%s does not match pack_manifest.json. Run: python scripts/update_pack_manifest.py" % entry["path"]
            )
    return manifest


def read_confidence_levels(schema_levels):
    """Read each level's label and description from ConfidenceLevel.kt so the web wording is the app's wording."""
    source = CONFIDENCE_KT.read_text(encoding="utf-8")
    pattern = re.compile(
        r'^\s*([A-Z_]+)\(\s*label\s*=\s*"([^"]*)",\s*description\s*=\s*"([^"]*)"', re.MULTILINE
    )
    levels = [{"id": m.group(1), "label": m.group(2), "description": m.group(3)} for m in pattern.finditer(source)]
    if {level["id"] for level in levels} != set(schema_levels) or len(levels) != len(schema_levels):
        raise BuildError(
            "ConfidenceLevel.kt levels %s do not match the pack schema's confidenceLevel enum %s"
            % (sorted(level["id"] for level in levels), sorted(schema_levels))
        )
    return levels


def normalize_level(raw, known_ids, warnings, site_id):
    """Fail closed exactly like the app: null, blank or unrecognized values become UNVERIFIED."""
    if not isinstance(raw, str) or not raw.strip():
        warnings.append("%s: no confidenceLevel; treated as UNVERIFIED" % site_id)
        return "UNVERIFIED"
    normalized = raw.strip().upper().replace(" ", "_")
    normalized = LEVEL_ALIASES.get(normalized, normalized)
    if normalized not in known_ids:
        warnings.append("%s: unknown confidenceLevel %r; treated as UNVERIFIED" % (site_id, raw))
        return "UNVERIFIED"
    return normalized


def parse_geometry(text):
    """'lat,lon;lat,lon' becomes [[lat, lon], ...]."""
    points = []
    for part in text.split(";"):
        lat, lon = (float(value) for value in part.split(","))
        points.append([lat, lon])
    return points


def build_sites(raw_sites, geofences, known_ids, warnings):
    fences = {}
    for fence in geofences:
        if fence["siteId"] in fences:
            raise BuildError("duplicate geofence for %s" % fence["siteId"])
        fences[fence["siteId"]] = fence

    sites, seen = [], set()
    for raw in raw_sites:
        site_id = raw.get("id")
        missing = [field for field in REQUIRED_SITE_FIELDS if field not in raw]
        if missing:
            raise BuildError("site %s is missing required fields: %s" % (site_id, ", ".join(missing)))
        if site_id in seen:
            raise BuildError("duplicate site id %s" % site_id)
        seen.add(site_id)
        if not (-90 <= raw["latitude"] <= 90 and -180 <= raw["longitude"] <= 180):
            raise BuildError("site %s has coordinates out of range" % site_id)

        fence = fences.get(site_id)
        if fence is None:
            warnings.append("%s: no geofence entry" % site_id)
        elif (fence["latitude"], fence["longitude"], fence["radiusMeters"]) != (
            raw["latitude"], raw["longitude"], raw["geofenceRadiusMeters"]
        ):
            warnings.append("%s: geofences.json disagrees with sites.json; using sites.json" % site_id)

        site = {field: raw[field] for field in REQUIRED_SITE_FIELDS}
        site["confidenceLevel"] = normalize_level(raw["confidenceLevel"], known_ids, warnings, site_id)
        for field in OPTIONAL_SITE_FIELDS:
            value = raw.get(field)
            if isinstance(value, str) and value.strip():
                site[field] = value
        if isinstance(raw.get("geometryCoordinates"), str) and raw["geometryCoordinates"].strip():
            site["geometry"] = parse_geometry(raw["geometryCoordinates"])
        sites.append(site)

    orphans = sorted(set(fences) - seen)
    if orphans:
        warnings.append("geofences.json lists unknown sites: %s" % ", ".join(orphans))
    return sites


def dumps_atlas(atlas):
    """Stable, diff-friendly JSON: one top-level key per line and one site per line."""
    lines = []
    for key, value in atlas.items():
        if key == "sites":
            body = ",\n".join("  " + json.dumps(site, ensure_ascii=False, separators=(",", ":")) for site in value)
            lines.append('"sites": [\n%s\n]' % body)
        else:
            lines.append("%s: %s" % (json.dumps(key), json.dumps(value, ensure_ascii=False, separators=(",", ":"))))
    return ("{\n" + ",\n".join(lines) + "\n}\n").encode("utf-8")


def file_fingerprint(path):
    blob = path.read_bytes()
    if path.suffix.lower() in TEXT_SUFFIXES:
        blob = blob.replace(b"\r\n", b"\n")
    return hashlib.sha256(blob).hexdigest()


def build_precache(atlas_bytes, pack_version):
    """List every file the service worker should save, with a fingerprint that changes when any of them does."""
    entries = {}
    for path in sorted(SITE_DIR.rglob("*")):
        if not path.is_file() or path.name.startswith(".") or path.name in PRECACHE_SKIP or path.suffix == ".map":
            continue
        entries[path.relative_to(SITE_DIR).as_posix()] = path
    atlas_rel = ATLAS_PATH.relative_to(SITE_DIR).as_posix()
    digests = {rel: file_fingerprint(path) for rel, path in entries.items() if rel != atlas_rel}
    digests[atlas_rel] = hashlib.sha256(atlas_bytes).hexdigest()  # the atlas about to be written, not the old one
    version = hashlib.sha256("\n".join("%s %s" % (rel, digests[rel]) for rel in sorted(digests)).encode("utf-8"))
    payload = {"version": version.hexdigest()[:12], "packVersion": pack_version, "files": sorted(digests)}
    text = (
        "// Generated by scripts/build_site_data.py. Do not edit by hand.\n"
        "self.ATLAS_PRECACHE = %s;\n" % json.dumps(payload, indent=2)
    )
    return text.encode("utf-8"), payload


def build():
    """Return ({path: bytes} to write, warnings, precache payload)."""
    manifest = verify_pack_hashes()
    schema = load_json("lore_site.schema.json")
    schema_levels = schema["properties"]["confidenceLevel"]["enum"]
    levels = read_confidence_levels(schema_levels)
    known_ids = {level["id"] for level in levels}

    raw_sites = load_json("sites.json")
    warnings = []
    sites = build_sites(raw_sites, load_json("geofences.json"), known_ids, warnings)
    if manifest.get("siteCount") != len(sites):
        raise BuildError("pack_manifest.json says %s sites but sites.json has %d" % (manifest.get("siteCount"), len(sites)))

    atlas = {
        "pack": {key: manifest[key] for key in ("packId", "version", "title", "region", "schemaVersion", "siteCount")},
        "licenses": load_json("licenses.json"),
        "confidenceLevels": levels,
        "sources": load_json("sources.json"),
        "sites": sites,
    }
    atlas_bytes = dumps_atlas(atlas)
    precache_bytes, payload = build_precache(atlas_bytes, manifest["version"])
    return {ATLAS_PATH: atlas_bytes, PRECACHE_PATH: precache_bytes}, warnings, payload


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--check", action="store_true", help="verify only; do not modify anything")
    args = parser.parse_args()

    try:
        outputs, warnings, payload = build()
    except (BuildError, KeyError, ValueError, OSError) as error:
        print("error: %s" % error, file=sys.stderr)
        return 2

    for warning in warnings:
        print("warning: %s" % warning, file=sys.stderr)

    stale = [path for path, blob in outputs.items() if not path.is_file() or path.read_bytes() != blob]
    if args.check:
        if stale:
            for path in stale:
                print("out of date: %s" % path.relative_to(ROOT).as_posix())
            print("Run: python scripts/build_site_data.py")
            return 1
        print("site data is up to date (pack %s, offline version %s)" % (payload["packVersion"], payload["version"]))
        return 0

    for path in stale:
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(outputs[path])
        print("wrote %s (%d bytes)" % (path.relative_to(ROOT).as_posix(), len(outputs[path])))
    if not stale:
        print("nothing to do; site data already matches the pack")
    print("pack %s, offline version %s, %d files listed for offline use"
          % (payload["packVersion"], payload["version"], len(payload["files"])))
    return 0


if __name__ == "__main__":
    sys.exit(main())
