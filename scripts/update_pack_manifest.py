#!/usr/bin/env python3
"""Keep the lore pack manifest in step with the pack files.

pack_manifest.json lists a SHA-256 hash and byte size for every file in
app/src/main/assets/lorepacks/southern_indiana/. The app refuses to load a pack whose files do not match
(and falls back to its emergency copy), so run this after you edit any pack file.

    python scripts/update_pack_manifest.py                      # rewrite hashes and sizes
    python scripts/update_pack_manifest.py --bump-version 1.10.1  # ...and set the pack version
    python scripts/update_pack_manifest.py --check              # verify only; exit 1 on mismatch

Pack files must keep LF line endings (the repository's .gitattributes enforces this on checkout).
"""
import argparse
import hashlib
import json
import re
import sys
from pathlib import Path

PACK_DIR = Path(__file__).resolve().parent.parent / "app" / "src" / "main" / "assets" / "lorepacks" / "southern_indiana"
MANIFEST = PACK_DIR / "pack_manifest.json"


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--check", action="store_true", help="verify only; do not modify anything")
    parser.add_argument("--bump-version", metavar="X.Y.Z", help="also set the pack version")
    args = parser.parse_args()

    text = MANIFEST.read_bytes().decode("utf-8")
    manifest = json.loads(text)
    problems = []

    for entry in manifest["files"]:
        path = PACK_DIR / entry["path"]
        if not path.is_file():
            problems.append("missing file: %s" % entry["path"])
            continue
        blob = path.read_bytes()
        if b"\r" in blob:
            problems.append("%s contains CR characters (it must use LF line endings)" % entry["path"])
        digest = hashlib.sha256(blob).hexdigest()
        if digest == entry["sha256"] and len(blob) == entry["bytes"]:
            continue
        problems.append("%s: manifest is out of date" % entry["path"])
        if not args.check:
            pattern = re.compile(
                r'("path": "%s",\s*"sha256": ")[0-9a-f]{64}(",\s*"bytes": )\d+' % re.escape(entry["path"])
            )
            if len(pattern.findall(text)) != 1:
                print("cannot locate %s in the manifest text" % entry["path"], file=sys.stderr)
                return 2
            text = pattern.sub(lambda m: m.group(1) + digest + m.group(2) + str(len(blob)), text)
            print("updated %s (%d bytes)" % (entry["path"], len(blob)))

    if args.bump_version and not args.check:
        text = re.sub(r'"version": "[^"]*"', '"version": "%s"' % args.bump_version, text, count=1)
        print("pack version set to %s" % args.bump_version)

    if args.check:
        for p in problems:
            print(p)
        print("OK: manifest matches the pack files" if not problems else "FAILED")
        return 1 if problems else 0

    MANIFEST.write_bytes(text.encode("utf-8"))
    cr_problems = [p for p in problems if "CR characters" in p or p.startswith("missing")]
    for p in cr_problems:
        print(p, file=sys.stderr)
    return 1 if cr_problems else 0


if __name__ == "__main__":
    sys.exit(main())
