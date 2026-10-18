package com.example.data.pack

import android.content.Context
import android.util.Log
import com.example.data.local.InstalledPackEntity
import com.example.data.local.LoreDao
import com.example.data.local.LoreSeedData
import com.example.data.local.LoreSiteEntity
import com.example.data.model.ConfidenceLevel
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.InputStreamReader
import java.security.MessageDigest

enum class PackValidationStatus {
    VALID,
    INVALID,
    FALLBACK,
    NOT_LOADED
}

data class PackFileManifestEntry(
    val path: String,
    val sha256: String,
    val bytes: Long
)

data class LorePackManifest(
    val packId: String,
    val version: String,
    val title: String,
    val region: String,
    val schemaVersion: String,
    val siteCount: Int,
    val files: List<PackFileManifestEntry>
)

data class PackFileIntegrity(
    val path: String,
    val expectedHash: String,
    val actualHash: String,
    val expectedBytes: Long,
    val actualBytes: Long,
    val matches: Boolean
)

data class PackValidationResult(
    val status: PackValidationStatus,
    val packId: String = "",
    val version: String = "",
    val schemaVersion: String = "",
    val region: String = "",
    val siteCount: Int = 0,
    val parsedSiteCount: Int = 0,
    val integrityReport: List<PackFileIntegrity> = emptyList(),
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val validatedSites: List<LoreSiteEntity> = emptyList()
) {
    val isValid: Boolean get() = status == PackValidationStatus.VALID
}

object LorePackManager {

    private const val TAG = "LorePackManager"
    const val PACK_ROOT = "lorepacks/southern_indiana"

    /**
     * Reads stream, computing its SHA-256 hex string and byte count.
     */
    fun calculateSha256(stream: InputStream): Pair<String, Long> {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(8192)
        var totalBytes = 0L
        var read: Int
        while (stream.read(buffer).also { read = it } != -1) {
            digest.update(buffer, 0, read)
            totalBytes += read
        }
        val hashBytes = digest.digest()
        val hexString = hashBytes.joinToString("") { "%02x".format(it) }
        return Pair(hexString, totalBytes)
    }

    /**
     * Parses pack_manifest.json from assets. Returns null if missing or malformed.
     */
    fun loadManifest(context: Context): LorePackManifest? {
        return try {
            val jsonStr = context.assets.open("$PACK_ROOT/pack_manifest.json").use { stream ->
                InputStreamReader(stream).readText()
            }
            val obj = JSONObject(jsonStr)
            val filesArray = obj.getJSONArray("files")
            val filesList = mutableListOf<PackFileManifestEntry>()
            for (i in 0 until filesArray.length()) {
                val item = filesArray.get(i)
                if (item is JSONObject) {
                    filesList.add(
                        PackFileManifestEntry(
                            path = item.getString("path"),
                            sha256 = item.optString("sha256", ""),
                            bytes = item.optLong("bytes", 0L)
                        )
                    )
                } else if (item is String) {
                    filesList.add(
                        PackFileManifestEntry(
                            path = item,
                            sha256 = "",
                            bytes = 0L
                        )
                    )
                }
            }
            LorePackManifest(
                packId = obj.getString("packId"),
                version = obj.getString("version"),
                title = obj.getString("title"),
                region = obj.getString("region"),
                schemaVersion = obj.optString("schemaVersion", "1.0.0"),
                siteCount = obj.getInt("siteCount"),
                files = filesList
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load pack_manifest.json", e)
            null
        }
    }

    /**
     * Full sanity and integrity validation of the Lore Pack:
     * - Manifest structure and required fields
     * - Physical asset existence in assets/
     * - SHA-256 file checksum matches
     * - sites.json schema compliance (id uniqueness, bounds, confidence level, sources)
     * Fails closed: any error marks status as INVALID.
     */
    fun validatePack(context: Context): PackValidationResult {
        val manifest = loadManifest(context)
        if (manifest == null) {
            return PackValidationResult(
                status = PackValidationStatus.INVALID,
                errors = listOf("Pack manifest 'pack_manifest.json' is missing or corrupted.")
            )
        }

        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        val integrityReport = mutableListOf<PackFileIntegrity>()

        // 1. Check required manifest fields
        if (manifest.packId.isBlank()) errors.add("packId is blank.")
        if (manifest.version.isBlank()) errors.add("version is blank.")
        if (manifest.siteCount <= 0) errors.add("siteCount must be positive (found ${manifest.siteCount}).")
        if (manifest.files.isEmpty()) errors.add("Manifest files list is empty.")

        // 2. Validate all manifest files exist and match SHA-256
        for (fileEntry in manifest.files) {
            val assetPath = "$PACK_ROOT/${fileEntry.path}"
            try {
                val (actualHash, actualBytes) = context.assets.open(assetPath).use { stream ->
                    calculateSha256(stream)
                }
                val hashMatch = fileEntry.sha256.isBlank() || fileEntry.sha256.equals(actualHash, ignoreCase = true)
                val bytesMatch = fileEntry.bytes == 0L || fileEntry.bytes == actualBytes

                integrityReport.add(
                    PackFileIntegrity(
                        path = fileEntry.path,
                        expectedHash = fileEntry.sha256,
                        actualHash = actualHash,
                        expectedBytes = fileEntry.bytes,
                        actualBytes = actualBytes,
                        matches = hashMatch && bytesMatch
                    )
                )

                if (!hashMatch) {
                    errors.add("SHA-256 hash mismatch for '${fileEntry.path}'. Expected: ${fileEntry.sha256}, Actual: $actualHash")
                }
                if (!bytesMatch) {
                    errors.add("Byte size mismatch for '${fileEntry.path}'. Expected: ${fileEntry.bytes}, Actual: $actualBytes")
                }
            } catch (e: Exception) {
                errors.add("File '${fileEntry.path}' listed in manifest could not be opened: ${e.message}")
                integrityReport.add(
                    PackFileIntegrity(
                        path = fileEntry.path,
                        expectedHash = fileEntry.sha256,
                        actualHash = "FILE_NOT_FOUND",
                        expectedBytes = fileEntry.bytes,
                        actualBytes = 0L,
                        matches = false
                    )
                )
            }
        }

        // 3. Parse and validate sites.json
        val parsedSites = mutableListOf<LoreSiteEntity>()
        try {
            val jsonStr = context.assets.open("$PACK_ROOT/sites.json").use { stream ->
                InputStreamReader(stream).readText()
            }
            val array = JSONArray(jsonStr)

            if (array.length() != manifest.siteCount) {
                errors.add("Manifest siteCount (${manifest.siteCount}) does not match sites.json count (${array.length()}).")
            }

            val seenIds = mutableSetOf<String>()

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.optString("id", "")
                val title = obj.optString("title", "")
                val category = obj.optString("category", "")
                val county = obj.optString("county", "")
                val lat = obj.optDouble("latitude", Double.NaN)
                val lng = obj.optDouble("longitude", Double.NaN)
                val fullStory = obj.optString("fullStory", "")
                val rawConfidence = obj.optString("confidenceLevel", "")
                val sources = obj.optString("sources", "")
                val radius = obj.optInt("geofenceRadiusMeters", 100)

                // Validation rules
                if (id.isBlank()) errors.add("Site at index $i has blank ID.")
                if (seenIds.contains(id)) errors.add("Duplicate site ID found: '$id'.")
                seenIds.add(id)

                if (title.isBlank()) errors.add("Site '$id' has blank title.")
                if (category.isBlank()) errors.add("Site '$id' has blank category.")
                if (county.isBlank()) errors.add("Site '$id' has blank county.")
                if (lat.isNaN() || lat < 37.0 || lat > 40.5) {
                    errors.add("Site '$id' latitude ($lat) is outside valid Southern Indiana bounds [37.0, 40.5].")
                }
                if (lng.isNaN() || lng < -89.0 || lng > -84.0) {
                    errors.add("Site '$id' longitude ($lng) is outside valid Southern Indiana bounds [-89.0, -84.0].")
                }

                // Historical confidence fails closed to UNVERIFIED on unknown/blank string
                val parsedConfidence = ConfidenceLevel.fromString(rawConfidence)

                // Official & Verified records must have actual sources cited
                if ((parsedConfidence == ConfidenceLevel.OFFICIAL_SOURCE || parsedConfidence == ConfidenceLevel.VERIFIED) && sources.isBlank()) {
                    errors.add("Site '$id' is marked $parsedConfidence but has empty sources.")
                }

                if (radius < 20 || radius > 5000) {
                    warnings.add("Site '$id' geofence radius $radius m is outside typical range [20, 5000].")
                }

                parsedSites.add(
                    LoreSiteEntity(
                        id = id,
                        title = title,
                        subtitle = obj.optString("subtitle", ""),
                        category = category,
                        county = county,
                        latitude = lat,
                        longitude = lng,
                        address = obj.optString("address", ""),
                        era = obj.optString("era", ""),
                        accessStatus = obj.optString("accessStatus", ""),
                        fullStory = fullStory,
                        verifiedHistory = obj.optString("verifiedHistory", fullStory),
                        folkloreTradition = obj.optString("folkloreTradition", ""),
                        confidenceLevel = parsedConfidence.name,
                        sources = sources,
                        sourceNotes = obj.optString("sourceNotes", sources),
                        verificationGrade = obj.optString("verificationGrade", parsedConfidence.label),
                        geofenceRadiusMeters = radius,
                        audioNarration = obj.optString("audioNarration", obj.optString("subtitle", "")),
                        isCustom = false,
                        photoUri = null,
                        alternativeNames = obj.optString("alternativeNames", ""),
                        siteType = obj.optString("siteType", "POINT"),
                        subType = obj.optString("subType", ""),
                        triggerRadiusType = obj.optString("triggerRadiusType", "LOCAL_SITE"),
                        driveStory = obj.optString("driveStory", obj.optString("subtitle", "")),
                        whyHere = obj.optString("whyHere", ""),
                        historyNerdDiscovery = obj.optString("historyNerdDiscovery", ""),
                        aboveLayer = obj.optString("aboveLayer", title),
                        surfaceLayer = obj.optString("surfaceLayer", obj.optString("address", "")),
                        belowLayer = obj.optString("belowLayer", ""),
                        time1800 = obj.optString("time1800", ""),
                        time1850 = obj.optString("time1850", ""),
                        time1900 = obj.optString("time1900", ""),
                        time1950 = obj.optString("time1950", ""),
                        timeToday = obj.optString("timeToday", "Modern landscape & preserved historical site"),
                        whatMayBeUnderground = obj.optString("whatMayBeUnderground", ""),
                        visibleRemains = obj.optString("visibleRemains", ""),
                        relatedLocations = obj.optString("relatedLocations", ""),
                        relatedPeople = obj.optString("relatedPeople", ""),
                        relatedEvents = obj.optString("relatedEvents", ""),
                        mapEvidence = obj.optString("mapEvidence", ""),
                        unresolvedQuestions = obj.optString("unresolvedQuestions", ""),
                        accessLevel = obj.optString("accessLevel", obj.optString("accessStatus", "PUBLIC ACCESS")),
                        isSensitiveLocation = obj.optBoolean("isSensitiveLocation", false),
                        isExactCoordinateSafe = obj.optBoolean("isExactCoordinateSafe", true),
                        radarType = obj.optString("radarType", "HISTORIC_NEARBY"),
                        youJustCrossedEvent = obj.optString("youJustCrossedEvent", ""),
                        geometryCoordinates = obj.optString("geometryCoordinates", ""),
                        undergroundConfidence = obj.optString("undergroundConfidence", "")
                    )
                )
            }
        } catch (e: Exception) {
            errors.add("Failed to parse sites.json: ${e.message}")
        }

        val finalStatus = if (errors.isEmpty()) PackValidationStatus.VALID else PackValidationStatus.INVALID

        return PackValidationResult(
            status = finalStatus,
            packId = manifest.packId,
            version = manifest.version,
            schemaVersion = manifest.schemaVersion,
            region = manifest.region,
            siteCount = manifest.siteCount,
            parsedSiteCount = parsedSites.size,
            integrityReport = integrityReport,
            errors = errors,
            warnings = warnings,
            validatedSites = parsedSites
        )
    }

    /**
     * Authoritative Pack Installation & Sync:
     * - Validates the pack against manifest and SHA-256 hashes.
     * - If VALID:
     *     - Checks if current installed version/hash matches. If not, atomically replaces
     *       pack-owned sites via dao.replacePackSitesSafely (leaving user-created lore intact).
     *     - Records InstalledPackEntity in Room.
     * - If INVALID:
     *     - Fails closed. Does NOT overwrite existing database.
     *     - If database has 0 sites, populates with emergency LoreSeedData as FALLBACK.
     */
    suspend fun syncAuthoritativePack(context: Context, dao: LoreDao): PackValidationResult {
        val validation = validatePack(context)

        if (validation.isValid) {
            val installed = dao.getInstalledPack(validation.packId)
            val sitesFileHash = validation.integrityReport.firstOrNull { it.path == "sites.json" }?.actualHash ?: ""

            val needsInstall = installed == null ||
                    installed.contentHash != sitesFileHash ||
                    installed.version != validation.version ||
                    installed.status != PackValidationStatus.VALID.name

            if (needsInstall) {
                Log.i(TAG, "Installing authoritative lore pack '${validation.packId}' v${validation.version} (${validation.validatedSites.size} sites)")
                dao.replacePackSitesSafely(validation.validatedSites)

                dao.insertInstalledPack(
                    InstalledPackEntity(
                        packId = validation.packId,
                        version = validation.version,
                        schemaVersion = validation.schemaVersion,
                        contentHash = sitesFileHash,
                        installedTimestamp = System.currentTimeMillis(),
                        siteCount = validation.validatedSites.size,
                        status = PackValidationStatus.VALID.name
                    )
                )
            } else {
                Log.d(TAG, "Installed pack '${validation.packId}' v${validation.version} matches current asset hash; no re-install needed.")
            }
            return validation
        } else {
            Log.w(TAG, "Lore pack validation failed with ${validation.errors.size} errors: ${validation.errors.take(3)}")

            // If database is completely empty, provide fallback seed data so app remains operable
            if (dao.getSiteCount() == 0) {
                Log.w(TAG, "Database is empty and pack is invalid. Using emergency LoreSeedData as fallback.")
                dao.insertAll(emergencyFallbackSites())
                dao.insertInstalledPack(
                    InstalledPackEntity(
                        packId = validation.packId.ifBlank { "fallback_seed_pack" },
                        version = "0.0.0-fallback",
                        schemaVersion = "fallback",
                        contentHash = "FALLBACK_SEED",
                        installedTimestamp = System.currentTimeMillis(),
                        siteCount = LoreSeedData.sites.size,
                        status = PackValidationStatus.FALLBACK.name
                    )
                )
                return validation.copy(status = PackValidationStatus.FALLBACK)
            }

            return validation
        }
    }

    /**
     * The emergency copy of the lore data, used only when the pack fails validation on an empty database.
     * It is never presented as verified, whatever its older labels say.
     */
    internal fun emergencyFallbackSites(): List<LoreSiteEntity> = LoreSeedData.sites.map {
        it.copy(
            confidenceLevel = ConfidenceLevel.UNVERIFIED.name,
            verificationGrade = "Offline fallback copy (not independently verified)"
        )
    }

    /**
     * Convenience method to load raw sites without replacing or mutating DB.
     */
    fun loadSitesFromPack(context: Context): List<LoreSiteEntity> {
        val res = validatePack(context)
        return if (res.isValid) res.validatedSites else emptyList()
    }
}
