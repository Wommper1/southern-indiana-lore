package com.example.service
 
import com.example.data.local.LoreSiteEntity
import com.example.data.model.LoreSite
import com.example.util.GeoPoint
import com.example.util.LocationUtils

/**
 * LocalProximityEngine: Offline-first candidate scoring, distance calculation,
 * trajectory crossing detection, and geometry-aware proximity detection for Southern Indiana historic sites.
 *
 * Supports:
 * - POINT: discrete historic structures, cave entrances, cemeteries, springs, markers
 * - LINE: linear corridors (Buffalo Trace, abandoned Monon/Pennsylvania railroads, canals, stage roads)
 * - POLYGON: areas (Rose Island grounds, battlefields, vanished towns, karst sinkhole plains)
 * - Predictive Trajectory Crossing: line-segment intersection against corridor geometries
 * - State Machine: FAR -> APPROACHING -> NEAR -> CROSSED / INSIDE -> LEAVING -> COOLDOWN
 *
 * Runs locally on device using GPS coordinates and Room database records.
 * Completely independent of Google Play Services Geofencing and network connectivity.
 */
class LocalProximityEngine(
    private val debounceCooldownMs: Long = 90 * 1000L // 90-second cooldown per site
) {
    // Tracks the timestamp of the last alert per siteId
    private val lastTriggerTimestamps = mutableMapOf<String, Long>()

    // Set of site IDs that the user is currently inside or crossing (for boundary hysteresis)
    private val insideSiteIds = mutableSetOf<String>()

    // Cache parsed coordinates to prevent re-parsing strings on every GPS tick
    private val parsedCoordinatesCache = mutableMapOf<String, List<GeoPoint>>()

    // Previous user coordinates for trajectory intersection checks
    private var lastUserLat: Double? = null
    private var lastUserLng: Double? = null

    /**
     * Calculates distance from user to site respecting POINT, LINE, or POLYGON geometry.
     */
    fun calculateDistanceToSite(
        userLat: Double,
        userLng: Double,
        site: LoreSiteEntity
    ): Float {
        val geometryType = site.siteType.uppercase()
        if (geometryType == "LINE" && site.geometryCoordinates.isNotBlank()) {
            val points = getOrParseCoordinates(site.id, site.geometryCoordinates)
            if (points.isNotEmpty()) {
                return LocationUtils.calculateDistanceToPolylineMeters(userLat, userLng, points)
            }
        } else if (geometryType == "POLYGON" && site.geometryCoordinates.isNotBlank()) {
            val polygon = getOrParseCoordinates(site.id, site.geometryCoordinates)
            if (polygon.isNotEmpty()) {
                return LocationUtils.calculateDistanceToPolygonMeters(userLat, userLng, polygon)
            }
        }

        // Default or POINT
        return LocationUtils.calculateDistanceMeters(userLat, userLng, site.latitude, site.longitude)
    }

    fun calculateDistanceToSite(
        userLat: Double,
        userLng: Double,
        site: LoreSite
    ): Float {
        val geometryType = site.siteType.uppercase()
        if (geometryType == "LINE" && site.geometryCoordinates.isNotBlank()) {
            val points = getOrParseCoordinates(site.id, site.geometryCoordinates)
            if (points.isNotEmpty()) {
                return LocationUtils.calculateDistanceToPolylineMeters(userLat, userLng, points)
            }
        } else if (geometryType == "POLYGON" && site.geometryCoordinates.isNotBlank()) {
            val polygon = getOrParseCoordinates(site.id, site.geometryCoordinates)
            if (polygon.isNotEmpty()) {
                return LocationUtils.calculateDistanceToPolygonMeters(userLat, userLng, polygon)
            }
        }

        return LocationUtils.calculateDistanceMeters(userLat, userLng, site.latitude, site.longitude)
    }

    /**
     * Checks whether the user's travel vector (prev -> curr) intersected the LINE or POLYGON boundary
     * of a historic corridor, even if neither GPS fix fell directly inside the geofence radius.
     */
    fun checkTrajectoryIntersection(
        prevLat: Double,
        prevLng: Double,
        currLat: Double,
        currLng: Double,
        site: LoreSite
    ): Boolean {
        if (site.geometryCoordinates.isBlank()) return false
        val coords = getOrParseCoordinates(site.id, site.geometryCoordinates)
        if (coords.size < 2) return false

        val isPolygon = site.siteType.equals("POLYGON", ignoreCase = true)
        val segmentsCount = if (isPolygon) coords.size else coords.size - 1

        for (i in 0 until segmentsCount) {
            val p1 = coords[i]
            val p2 = coords[(i + 1) % coords.size]

            if (LocationUtils.doSegmentsIntersect(
                    prevLat, prevLng, currLat, currLng,
                    p1.lat, p1.lng, p2.lat, p2.lng
                )
            ) {
                return true
            }
        }

        return false
    }

    /**
     * Evaluates crossing state transitions for state machine and anti-spam verification.
     */
    fun evaluateCrossingState(
        site: LoreSite,
        currLat: Double,
        currLng: Double,
        prevLat: Double? = null,
        prevLng: Double? = null,
        currentState: CrossingState = CrossingState.FAR
    ): CrossingState {
        val distance = calculateDistanceToSite(currLat, currLng, site)
        val triggerRadius = if (site.geofenceRadiusMeters > 0) site.geofenceRadiusMeters.toFloat() else 75f

        // Check if just crossed
        if (currentState == CrossingState.CROSSED || currentState == CrossingState.COOLDOWN) {
            if (distance > triggerRadius * 2.5f) {
                return CrossingState.FAR
            }
            return CrossingState.COOLDOWN
        }

        // Polygon containment
        if (site.siteType.equals("POLYGON", ignoreCase = true) && site.geometryCoordinates.isNotBlank()) {
            val poly = getOrParseCoordinates(site.id, site.geometryCoordinates)
            val isInside = LocationUtils.isPointInPolygon(currLat, currLng, poly)
            val wasInside = if (prevLat != null && prevLng != null) {
                LocationUtils.isPointInPolygon(prevLat, prevLng, poly)
            } else false

            return when {
                isInside && !wasInside -> CrossingState.INSIDE
                isInside -> CrossingState.INSIDE
                !isInside && wasInside -> CrossingState.LEAVING
                distance <= triggerRadius -> CrossingState.NEAR
                distance <= triggerRadius * 3f -> CrossingState.APPROACHING
                else -> CrossingState.FAR
            }
        }

        // Line corridor trajectory intersection
        if (prevLat != null && prevLng != null && site.siteType.equals("LINE", ignoreCase = true)) {
            val crossedTrajectory = checkTrajectoryIntersection(prevLat, prevLng, currLat, currLng, site)
            if (crossedTrajectory) {
                return CrossingState.CROSSED
            }
        }

        return when {
            distance <= triggerRadius -> CrossingState.NEAR
            distance <= triggerRadius * 3f -> CrossingState.APPROACHING
            else -> CrossingState.FAR
        }
    }

    private fun getOrParseCoordinates(siteId: String, coordsStr: String): List<GeoPoint> {
        return parsedCoordinatesCache.getOrPut(siteId) {
            LocationUtils.parseCoordinates(coordsStr)
        }
    }

    /**
     * Evaluates candidate sites against current coordinates with geometry-aware
     * trigger radius and historical significance ranking.
     * Returns the list of sites that qualify for a new proximity trigger.
     */
    fun evaluate(
        userLat: Double,
        userLng: Double,
        sites: List<LoreSiteEntity>,
        currentTimeMs: Long = System.currentTimeMillis()
    ): List<Pair<LoreSiteEntity, Float>> {
        val triggered = mutableListOf<Pair<LoreSiteEntity, Float>>()
        val prevLat = lastUserLat
        val prevLng = lastUserLng

        for (site in sites) {
            val distance = calculateDistanceToSite(userLat, userLng, site)

            val isUnderground = site.radarType.equals("UNDERGROUND", ignoreCase = true) ||
                    site.whatMayBeUnderground.isNotBlank() ||
                    site.title.contains("Cave", ignoreCase = true) ||
                    site.title.contains("Tunnel", ignoreCase = true) ||
                    site.title.contains("Caverns", ignoreCase = true)

            val isBridge = site.radarType.equals("BRIDGE", ignoreCase = true) ||
                    site.title.contains("Bridge", ignoreCase = true) ||
                    site.title.contains("Trestle", ignoreCase = true) ||
                    site.title.contains("Viaduct", ignoreCase = true)

            // Shape-aware trigger radius according to Proximity Historian taxonomy
            val effectiveRadius = when {
                isUnderground -> {
                    // Generous radius (450m-600m) for subterranean caverns, underground rivers & tunnels
                    // ensuring drivers passing over karst voids at driving speeds are guaranteed to trigger
                    if (site.geofenceRadiusMeters > 0) maxOf(site.geofenceRadiusMeters.toFloat(), 450f) else 450f
                }
                isBridge -> {
                    // Generous 350m detection span for historic river bridges and rail viaducts
                    if (site.geofenceRadiusMeters > 0) maxOf(site.geofenceRadiusMeters.toFloat(), 350f) else 350f
                }
                site.geofenceRadiusMeters > 0 -> site.geofenceRadiusMeters.toFloat()
                site.siteType.equals("LINE", ignoreCase = true) -> 150f // Accurate line corridor crossing
                site.siteType.equals("POLYGON", ignoreCase = true) -> 200f // Area boundary / interior
                site.triggerRadiusType.equals("EXACT_SITE", ignoreCase = true) -> 120f // 50-150m
                site.triggerRadiusType.equals("LOCAL_SITE", ignoreCase = true) -> 300f // 150-500m
                site.triggerRadiusType.equals("LANDSCAPE", ignoreCase = true) -> 1000f // 250m-2km
                site.triggerRadiusType.equals("REGIONAL_STORY", ignoreCase = true) -> 2000f
                else -> 180f
            }

            // Check trajectory crossing for linear corridors (tunnels, bridges, traces)
            var crossedTrajectory = false
            if (prevLat != null && prevLng != null && site.geometryCoordinates.isNotBlank()) {
                val coords = getOrParseCoordinates(site.id, site.geometryCoordinates)
                if (coords.size >= 2) {
                    val segmentsCount = if (site.siteType.equals("POLYGON", ignoreCase = true)) coords.size else coords.size - 1
                    for (i in 0 until segmentsCount) {
                        val p1 = coords[i]
                        val p2 = coords[(i + 1) % coords.size]
                        if (LocationUtils.doSegmentsIntersect(
                                prevLat, prevLng, userLat, userLng,
                                p1.lat, p1.lng, p2.lat, p2.lng
                            )
                        ) {
                            crossedTrajectory = true
                            break
                        }
                    }
                }
            }

            val isInside = distance <= effectiveRadius || crossedTrajectory
            val isWellOutside = distance > (effectiveRadius * 1.5f)

            if (isInside) {
                val lastTrigger = lastTriggerTimestamps[site.id] ?: 0L
                val isCoolingDown = (currentTimeMs - lastTrigger) < debounceCooldownMs

                if (!insideSiteIds.contains(site.id) && !isCoolingDown) {
                    insideSiteIds.add(site.id)
                    lastTriggerTimestamps[site.id] = currentTimeMs
                    triggered.add(Pair(site, distance))
                }
            } else if (isWellOutside) {
                // User has moved sufficiently far away; reset boundary flag for hysteresis
                insideSiteIds.remove(site.id)
            }
        }

        lastUserLat = userLat
        lastUserLng = userLng

        // Notification Priority Ranking:
        // 1. Highest priority to "You Just Crossed..." line events & Under Your Feet (Karst/Caves)
        // 2. High priority to River Crossings and Vanished Landscapes
        // 3. High priority to Official/Verified confidence
        // 4. Proximity distance
        return triggered.sortedBy { (site, distance) ->
            var score = distance
            if (site.youJustCrossedEvent.isNotBlank()) score -= 300f
            if (site.radarType.equals("UNDERGROUND", ignoreCase = true)) score -= 250f
            if (site.radarType.equals("CROSSING", ignoreCase = true)) score -= 200f
            if (site.radarType.equals("LOST_LANDSCAPE", ignoreCase = true)) score -= 150f
            if (site.confidenceLevel.equals("OFFICIAL_SOURCE", ignoreCase = true) ||
                site.confidenceLevel.equals("VERIFIED", ignoreCase = true)) score -= 50f
            score
        }
    }

    fun isSiteInCooldown(siteId: String, currentTimeMs: Long = System.currentTimeMillis()): Boolean {
        val last = lastTriggerTimestamps[siteId] ?: return false
        return (currentTimeMs - last) < debounceCooldownMs
    }

    fun reset() {
        insideSiteIds.clear()
        lastTriggerTimestamps.clear()
        parsedCoordinatesCache.clear()
    }
}
