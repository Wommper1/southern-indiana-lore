package com.example.service

import com.example.data.local.LoreMediaAssetEntity
import com.example.data.local.LoreSiteEntity
import com.example.data.repository.LoreRepository
import com.example.util.LocationUtils
import kotlin.math.cos

/**
 * Proximity tiers for multi-tier spatial geofencing in Southern Indiana Lore:
 * - FAR: Outside active horizon (> 1.5 km). Low battery polling rate.
 * - APPROACH_HORIZON: 500m to 1.5 km. Ambient audio cue pre-buffering, tighter GPS polling.
 * - APPROACH_NEAR: 150m to 500m. Pre-arrival audio dispatch, radar alignment.
 * - IMMEDIATE_TRIGGER: Within site trigger radius (< 100m) or linear trajectory crossing.
 * - DEPARTED: Leaving the trigger perimeter, entering anti-chatter cooldown.
 */
enum class ProximityTier(val priorityLevel: Int) {
    FAR(0),
    APPROACH_HORIZON(1),
    APPROACH_NEAR(2),
    IMMEDIATE_TRIGGER(3),
    DEPARTED(0)
}

data class SpatialBoundingBox(
    val minLat: Double,
    val maxLat: Double,
    val minLng: Double,
    val maxLng: Double
)

data class TierEvaluation(
    val site: LoreSiteEntity,
    val distanceMeters: Float,
    val currentTier: ProximityTier,
    val previousTier: ProximityTier,
    val isNewTierEntry: Boolean,
    val isTrajectoryCrossed: Boolean,
    val effectiveTriggerRadius: Float,
    val linkedMediaAsset: LoreMediaAssetEntity? = null
)

/**
 * High-performance spatial indexing and multi-tier geofence coordinator.
 * Employs bounding-box database filtering to avoid O(N) evaluation on every GPS tick,
 * calculates adaptive polling recommendations, and orchestrates media triggers.
 */
class SpatialProximityCoordinator(
    private val repository: LoreRepository,
    private val debounceCooldownMs: Long = 3 * 60 * 1000L // 3-minute anti-chatter debounce per site
) {
    private val siteTierHistory = mutableMapOf<String, ProximityTier>()
    private val lastTriggerTimestamps = mutableMapOf<String, Long>()
    private var lastUserLat: Double? = null
    private var lastUserLng: Double? = null

    // Cache of spatial candidates to avoid querying SQLite if user hasn't moved far
    private var cachedCandidates: List<LoreSiteEntity> = emptyList()
    private var lastQueryLat: Double? = null
    private var lastQueryLng: Double? = null
    private val spatialQueryThresholdMeters = 300f // Re-query bounding box only after 300m movement

    companion object {
        const val HORIZON_SEARCH_RADIUS_METERS = 3000.0 // 3 km regional search window
        const val METERS_PER_DEGREE_LAT = 111320.0
    }

    /**
     * Computes the bounding box coordinates given a center lat/lng and search radius.
     */
    fun computeBoundingBox(lat: Double, lng: Double, radiusMeters: Double = HORIZON_SEARCH_RADIUS_METERS): SpatialBoundingBox {
        val deltaLat = radiusMeters / METERS_PER_DEGREE_LAT
        val latRad = Math.toRadians(lat)
        val cosLat = cos(latRad).coerceAtLeast(0.01)
        val deltaLng = radiusMeters / (METERS_PER_DEGREE_LAT * cosLat)
        return SpatialBoundingBox(
            minLat = lat - deltaLat,
            maxLat = lat + deltaLat,
            minLng = lng - deltaLng,
            maxLng = lng + deltaLng
        )
    }

    /**
     * Evaluates current user coordinate against spatial indexed sites.
     * Returns all sites that changed proximity tiers or satisfied immediate triggers.
     */
    suspend fun evaluateUserLocation(
        userLat: Double,
        userLng: Double,
        speedMps: Float,
        currentTimeMs: Long = System.currentTimeMillis()
    ): Pair<List<TierEvaluation>, Long> {
        val prevLat = lastUserLat
        val prevLng = lastUserLng

        // 1. Check if spatial bounding box query needs refreshing
        val distSinceLastQuery = if (lastQueryLat != null && lastQueryLng != null) {
            LocationUtils.calculateDistanceMeters(lastQueryLat!!, lastQueryLng!!, userLat, userLng)
        } else {
            Float.MAX_VALUE
        }

        val candidates = if (distSinceLastQuery >= spatialQueryThresholdMeters || cachedCandidates.isEmpty()) {
            val bbox = computeBoundingBox(userLat, userLng, HORIZON_SEARCH_RADIUS_METERS)
            val sitesInBox = repository.getSitesInBoundingBox(
                minLat = bbox.minLat,
                maxLat = bbox.maxLat,
                minLng = bbox.minLng,
                maxLng = bbox.maxLng
            )
            cachedCandidates = sitesInBox
            lastQueryLat = userLat
            lastQueryLng = userLng
            sitesInBox
        } else {
            cachedCandidates
        }

        // 2. Multi-tier evaluation for each spatial candidate
        val evaluations = mutableListOf<TierEvaluation>()
        var minDistanceToAnyCandidate = Float.MAX_VALUE

        for (site in candidates) {
            val distance = calculateAccurateDistance(userLat, userLng, site)
            if (distance < minDistanceToAnyCandidate) {
                minDistanceToAnyCandidate = distance
            }

            val effectiveRadius = getEffectiveRadius(site)

            // Trajectory corridor crossing check for linear features (e.g. Buffalo Trace, abandoned rail)
            var isTrajectoryCrossed = false
            if (prevLat != null && prevLng != null && site.siteType.equals("LINE", ignoreCase = true) && site.geometryCoordinates.isNotBlank()) {
                val coords = LocationUtils.parseCoordinates(site.geometryCoordinates)
                if (coords.size >= 2) {
                    for (i in 0 until coords.size - 1) {
                        if (LocationUtils.doSegmentsIntersect(
                                prevLat, prevLng, userLat, userLng,
                                coords[i].lat, coords[i].lng,
                                coords[i + 1].lat, coords[i + 1].lng
                            )
                        ) {
                            isTrajectoryCrossed = true
                            break
                        }
                    }
                }
            }

            // Determine current tier based on distance and corridor crossing
            val currentTier = when {
                isTrajectoryCrossed || distance <= effectiveRadius -> ProximityTier.IMMEDIATE_TRIGGER
                distance <= 500f -> ProximityTier.APPROACH_NEAR
                distance <= 1500f -> ProximityTier.APPROACH_HORIZON
                else -> ProximityTier.FAR
            }

            val prevTier = siteTierHistory[site.id] ?: ProximityTier.FAR
            val isNewTierEntry = currentTier != prevTier
            siteTierHistory[site.id] = currentTier

            // If entering a higher-priority tier or crossing into an immediate trigger
            if (isNewTierEntry || isTrajectoryCrossed) {
                val lastTrigger = lastTriggerTimestamps[site.id] ?: 0L
                val isCoolingDown = (currentTimeMs - lastTrigger) < debounceCooldownMs

                // Resolve matching media asset from offline database
                val triggerTierKey = when (currentTier) {
                    ProximityTier.IMMEDIATE_TRIGGER -> if (isTrajectoryCrossed) "CORRIDOR_CROSS" else "ARRIVAL_100M"
                    ProximityTier.APPROACH_NEAR -> "APPROACH_500M"
                    ProximityTier.APPROACH_HORIZON -> "APPROACH_1500M"
                    else -> "AMBIENT_PAD"
                }

                val mediaAsset = repository.getMediaAssetForTier(site.id, triggerTierKey)

                if (currentTier == ProximityTier.IMMEDIATE_TRIGGER && !isCoolingDown) {
                    lastTriggerTimestamps[site.id] = currentTimeMs
                    evaluations.add(
                        TierEvaluation(
                            site = site,
                            distanceMeters = distance,
                            currentTier = currentTier,
                            previousTier = prevTier,
                            isNewTierEntry = true,
                            isTrajectoryCrossed = isTrajectoryCrossed,
                            effectiveTriggerRadius = effectiveRadius,
                            linkedMediaAsset = mediaAsset
                        )
                    )
                } else if (currentTier != ProximityTier.FAR && isNewTierEntry) {
                    evaluations.add(
                        TierEvaluation(
                            site = site,
                            distanceMeters = distance,
                            currentTier = currentTier,
                            previousTier = prevTier,
                            isNewTierEntry = true,
                            isTrajectoryCrossed = isTrajectoryCrossed,
                            effectiveTriggerRadius = effectiveRadius,
                            linkedMediaAsset = mediaAsset
                        )
                    )
                }
            }
        }

        lastUserLat = userLat
        lastUserLng = userLng

        // 3. Compute optimal adaptive GPS polling interval (in milliseconds)
        val recommendedPollingInterval = computeAdaptivePollingInterval(minDistanceToAnyCandidate, speedMps)

        return Pair(evaluations, recommendedPollingInterval)
    }

    private fun calculateAccurateDistance(userLat: Double, userLng: Double, site: LoreSiteEntity): Float {
        val geometryType = site.siteType.uppercase()
        if (geometryType == "LINE" && site.geometryCoordinates.isNotBlank()) {
            val points = LocationUtils.parseCoordinates(site.geometryCoordinates)
            if (points.isNotEmpty()) {
                return LocationUtils.calculateDistanceToPolylineMeters(userLat, userLng, points)
            }
        } else if (geometryType == "POLYGON" && site.geometryCoordinates.isNotBlank()) {
            val polygon = LocationUtils.parseCoordinates(site.geometryCoordinates)
            if (polygon.isNotEmpty()) {
                return LocationUtils.calculateDistanceToPolygonMeters(userLat, userLng, polygon)
            }
        }
        return LocationUtils.calculateDistanceMeters(userLat, userLng, site.latitude, site.longitude)
    }

    private fun getEffectiveRadius(site: LoreSiteEntity): Float {
        val isUnderground = site.radarType.equals("UNDERGROUND", ignoreCase = true) ||
                site.whatMayBeUnderground.isNotBlank() ||
                site.title.contains("Cave", ignoreCase = true) ||
                site.title.contains("Tunnel", ignoreCase = true) ||
                site.title.contains("Caverns", ignoreCase = true)

        val isBridge = site.radarType.equals("BRIDGE", ignoreCase = true) ||
                site.title.contains("Bridge", ignoreCase = true) ||
                site.title.contains("Trestle", ignoreCase = true)

        return when {
            isUnderground -> if (site.geofenceRadiusMeters > 0) maxOf(site.geofenceRadiusMeters.toFloat(), 450f) else 450f
            isBridge -> if (site.geofenceRadiusMeters > 0) maxOf(site.geofenceRadiusMeters.toFloat(), 350f) else 350f
            site.geofenceRadiusMeters > 0 -> site.geofenceRadiusMeters.toFloat()
            site.siteType.equals("LINE", ignoreCase = true) -> 120f
            site.siteType.equals("POLYGON", ignoreCase = true) -> 180f
            else -> 100f
        }
    }

    /**
     * Dynamically adjusts polling interval to conserve battery without missing POIs:
     * - Far from any POI (> 2 km): 30s to 60s
     * - Approaching POI (500m - 2 km): 10s to 15s
     * - Close / In Envelope (< 500m): 2s to 4s
     */
    private fun computeAdaptivePollingInterval(minDistanceMeters: Float, speedMps: Float): Long {
        return when {
            minDistanceMeters > 2500f -> if (speedMps > 20f) 30000L else 45000L
            minDistanceMeters > 1000f -> 12000L
            minDistanceMeters > 400f -> 6000L
            else -> 2500L
        }
    }

    fun reset() {
        siteTierHistory.clear()
        lastTriggerTimestamps.clear()
        cachedCandidates = emptyList()
        lastQueryLat = null
        lastQueryLng = null
    }
}
