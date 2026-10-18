# 02 - PACK AUTHORITY & INTEGRITY
## Neighborhood Lore Machine - Foundation Freeze

---

## 1. The Need for Pack Authority

Prior to Sprint 1.5, data loading was split across multiple non-authoritative sources: hardcoded seeds in `LoreSeedData.kt`, JSON assets in `assets/lorepacks/`, and runtime Room entities. Hardcoded seeds would unconditionally overwrite user modifications or pack updates on every cold start.

Sprint 1.5 established **Pack Authority**:
1. All canonical historical data originates from an authoritative, cryptographic Lore Pack (`assets/lorepacks/southern_indiana/`).
2. The pack manifest (`pack_manifest.json`) defines strict metadata, required files, expected SHA-256 checksums, and record counts.
3. Database synchronization is atomic and strictly partitioned: pack updates **replace pack-owned sites (`isCustom = 0`)** but **never touch user-created sites (`isCustom = 1`) or user journal records** (visits, favorites, photos, and personal field notes).

---

## 2. Pack Structure & Manifest Specification

The authoritative pack resides in `app/src/main/assets/lorepacks/southern_indiana/`:

```
assets/lorepacks/southern_indiana/
├── pack_manifest.json          # Authoritative manifest, version, checksums
├── lore_site.schema.json       # JSON schema contract
├── sites.json                  # Complete dataset of 21 canonical sites
├── geofences.json              # 21 GPS geofence zones
├── sources.json                # Archival citations and repository bibliography
└── licenses.json               # Open data & public domain licensing disclosures
```

### `pack_manifest.json` Structure:
```json
{
  "packId": "southern_indiana",
  "version": "1.0.0",
  "schemaVersion": "1.0.0",
  "title": "Southern Indiana Lore & Historical Expeditions",
  "description": "Authoritative historical and folklore pack covering Floyd County, Clark County, Harrison County, and the Knobstone Escarpment.",
  "region": "Southern Indiana (Ohio River Valley / Knobs)",
  "siteCount": 21,
  "files": {
    "sites": "sites.json",
    "schema": "lore_site.schema.json",
    "geofences": "geofences.json",
    "sources": "sources.json",
    "licenses": "licenses.json"
  },
  "contentHashes": {
    "sites.json": "6a968bb03b4131df4545ca8fc31b14a682ff9d71c1b35ddad22ad0090ff39ef2"
  }
}
```

---

## 3. Cryptographic & Schema Validation Pipeline

The `LorePackManager` validates packs before any data touches the SQLite database:

```
+---------------------------+
| LorePackManager.validate  |
+---------------------------+
              |
              v
[1. Manifest Check] ---------> Read pack_manifest.json; verify required fields
              |
              v
[2. File Existence] ---------> Ensure sites.json and schema exist in assets
              |
              v
[3. SHA-256 Check] ----------> Compute SHA-256 digest of sites.json;
              |                Compare against manifest.contentHashes["sites.json"]
              |                (Mismatches log error and fail validation)
              v
[4. Schema & Format] --------> Parse JSON array; enforce required attributes:
              |                id, title, category, county, latitude, longitude,
              |                verifiedHistory, folkloreTradition, sources
              v
[5. Site Count Match] -------> Ensure parsed array size == manifest.siteCount (21)
              |
              v
[6. Validation Result] ------> PackValidationResult(status = VALID / INVALID, errors)
```

### SHA-256 Digest Implementation:
```kotlin
fun computeSha256(bytes: ByteArray): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(bytes)
    return hash.joinToString("") { "%02x".format(it) }
}
```

---

## 4. Atomic & Safe Database Sync (`replacePackSitesSafely`)

To prevent pack synchronization from wiping user field discoveries or personal journals, `LoreDao` executes a transactional replacement:

```kotlin
@Transaction
suspend fun replacePackSitesSafely(newSites: List<LoreSiteEntity>) {
    // 1. Delete ONLY pack-owned sites
    deletePackOwnedSites() // SQL: DELETE FROM lore_sites WHERE isCustom = 0
    
    // 2. Ensure all incoming pack sites are tagged isCustom = false
    val sanitized = newSites.map { it.copy(isCustom = false) }
    
    // 3. Insert validated pack sites
    insertAll(sanitized)
}
```

### What is preserved across pack updates:
- User-created sites (`isCustom = 1`) remain untouched.
- The `user_site_journal` table (favorites, visit timestamps, user notes, custom photo URIs) is completely preserved.
- Because `getAllSites()` uses a `LEFT JOIN` between `lore_sites` and `user_site_journal`, user notes and favorites automatically re-attach to pack sites by `id`.

---

## 5. Tracking Pack Authority via `InstalledPackEntity`

The database records pack installation metadata in the `installed_pack_metadata` table:

```kotlin
@Entity(tableName = "installed_pack_metadata")
data class InstalledPackEntity(
    @PrimaryKey val packId: String,
    val version: String,
    val schemaVersion: String,
    val contentHash: String,
    val siteCount: Int,
    val installedTimestamp: Long,
    val status: String // "VALID", "INVALID", "FALLBACK", "NOT_LOADED"
)
```

This table empowers `ArchivePackScreen` to truthfully display:
- Installed Pack ID and Version
- Schema Contract Version
- Content SHA-256 Checksum prefix
- Database Site Count breakdown (Pack Sites vs. User Field Reports)
- Validation Status Badge (`VALID`, `INVALID`, `FALLBACK`)
- Exact validation error details if a pack has been tampered with
