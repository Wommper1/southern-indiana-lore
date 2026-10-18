package com.example.util

import com.example.data.model.LoreSite
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Topographic Elevation Engine for Southern Indiana:
 * Accurately models the Knobstone Escarpment (rising 300-400+ ft from the Ohio River trench
 * up to Skyline Drive summit at 1,020 ft) and subterranean limestone strata.
 *
 * Provides real-time altitude lookup, geological stratum classification,
 * trail profile cross-sections, and slope/grade calculation.
 */
object TopoElevationEngine {

    // Southern Indiana Geological Stratigraphy
    enum class GeologicalStratum(
        val displayName: String,
        val ageEra: String,
        val description: String,
        val colorHex: Long,
        val iconEmoji: String
    ) {
        EDWARDSVILLE_SILTSTONE(
            "Edwardsville Siltstone",
            "Mississippian (~345 Ma)",
            "Resistant knob-forming cap rock creating Southern Indiana's highest summits.",
            0xFF8D6E63,
            "⛰️"
        ),
        FLOYDS_KNOB_FORMATION(
            "Floyds Knob Formation",
            "Middle Mississippian (~348 Ma)",
            "Glauconitic crinoidal limestone bench tracing the escarpment face.",
            0xFFA1887F,
            "🪨"
        ),
        HARRODSBURG_LIMESTONE(
            "Harrodsburg & Salem Karst",
            "Mississippian (~350 Ma)",
            "Fossil-rich karst limestone harboring sinkholes, springs and caverns.",
            0xFFBCAAA4,
            "🏛️"
        ),
        NEW_ALBANY_SHALE(
            "New Albany Black Shale",
            "Devonian–Mississippian (~360 Ma)",
            "Pyritic, organic-rich bituminous dark shale beneath the valley bluffs.",
            0xFF4E342E,
            "⬛"
        ),
        ALLUVIAL_RIVER_TERRACE(
            "Ohio River Alluvium",
            "Quaternary (~10 Ka)",
            "Glacial outwash gravels, sands and flood terraces along the river pool.",
            0xFFD7CCC8,
            "🌊"
        )
    }

    data class ElevationPoint(
        val distanceMiles: Float,
        val elevationFt: Int,
        val lat: Double,
        val lng: Double,
        val stratum: GeologicalStratum,
        val landmarkName: String? = null
    )

    data class TrailProfileTelemetry(
        val points: List<ElevationPoint>,
        val currentElevationFt: Int,
        val maxSummitElevationFt: Int,
        val minValleyElevationFt: Int,
        val totalAscentFt: Int,
        val totalDescentFt: Int,
        val maxGradePercent: Float,
        val currentStratum: GeologicalStratum
    )

    // Key topographic anchor points in Southern Indiana
    private val topoAnchors = listOf(
        // High Knobstone Escarpment summits
        TopoAnchor(38.3180, -85.9080, 980, "Skyline Drive Summit"),
        TopoAnchor(38.3350, -85.8900, 950, "Spickert Knob"),
        TopoAnchor(38.3520, -85.8750, 1020, "Bald Knob Peak"),
        TopoAnchor(38.3750, -85.8650, 960, "Round Top Knob"),
        TopoAnchor(38.2910, -85.8450, 780, "Silver Hills Bluff"),
        TopoAnchor(38.4100, -85.8600, 970, "Knobstone Trail South Trailhead"),
        // Rolling knobs & mid-slopes
        TopoAnchor(38.3000, -85.8600, 680, "State Street Knob Foothills"),
        TopoAnchor(38.2800, -85.8800, 720, "Grant Line Knobs Slope"),
        // Karst plateau (Harrison / Crawford)
        TopoAnchor(38.2120, -86.1260, 680, "Corydon Karst Plateau"),
        TopoAnchor(38.3700, -86.3400, 620, "Marengo Cave Ravine"),
        TopoAnchor(38.2280, -86.2950, 540, "Wyandotte Cave Blue River Gorge"),
        TopoAnchor(38.2600, -86.0200, 710, "Lanesville Limestone Ridge"),
        // Ohio River Trench (pool elevation ~420-435 ft)
        TopoAnchor(38.2830, -85.8200, 430, "New Albany Riverfront"),
        TopoAnchor(38.2750, -85.7620, 425, "Falls of the Ohio Fossil Beds"),
        TopoAnchor(38.2700, -85.7400, 435, "Jeffersonville River Shore"),
        TopoAnchor(38.3200, -85.7000, 440, "Utica River Terrace"),
        TopoAnchor(38.2400, -85.8700, 430, "Ohio River Horseshoe Bend")
    )

    private data class TopoAnchor(
        val lat: Double,
        val lng: Double,
        val elevationFt: Int,
        val name: String
    )

    /**
     * Calculates elevation in feet above sea level for any coordinate in Southern Indiana.
     * Uses inverse-distance weighted interpolation combined with a steep escarpment ridge model
     * (the escarpment rises sharply west of longitude -85.845).
     */
    fun getElevationFeet(lat: Double, lng: Double): Int {
        // Escarpment slope model:
        // As longitude moves west from the Ohio River (-85.82) toward the Knobs (-85.90),
        // elevation increases drastically from 430 ft to 980+ ft.
        val escarpmentFactor = (( -85.830 - lng ) / 0.075).coerceIn(0.0, 1.0)
        val baseEscarpmentElevation = 430.0 + (520.0 * escarpmentFactor.pow(1.3))

        // Distance-weighted influence from known surveyor points
        var totalWeight = 0.0
        var weightedSum = 0.0

        for (anchor in topoAnchors) {
            val dKm = distanceKm(lat, lng, anchor.lat, anchor.lng)
            if (dKm < 0.05) {
                return anchor.elevationFt
            }
            val weight = 1.0 / (dKm.pow(2.2) + 0.01)
            weightedSum += anchor.elevationFt * weight
            totalWeight += weight
        }

        val anchorEstimated = if (totalWeight > 0.0) weightedSum / totalWeight else baseEscarpmentElevation

        // Blend local anchor points with the regional escarpment slope
        val blended = (anchorEstimated * 0.7) + (baseEscarpmentElevation * 0.3)
        return blended.toInt().coerceIn(415, 1050)
    }

    /**
     * Determines geological stratum based on elevation and escarpment depth.
     */
    fun getStratum(elevationFt: Int): GeologicalStratum {
        return when {
            elevationFt >= 860 -> GeologicalStratum.EDWARDSVILLE_SILTSTONE
            elevationFt >= 720 -> GeologicalStratum.FLOYDS_KNOB_FORMATION
            elevationFt >= 550 -> GeologicalStratum.HARRODSBURG_LIMESTONE
            elevationFt >= 460 -> GeologicalStratum.NEW_ALBANY_SHALE
            else -> GeologicalStratum.ALLUVIAL_RIVER_TERRACE
        }
    }

    /**
     * Generates a trail or driving elevation profile between two points or a sequence of sites.
     */
    fun generateTrailProfile(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double,
        waypoints: List<LoreSite> = emptyList(),
        steps: Int = 32
    ): TrailProfileTelemetry {
        val pathCoords = mutableListOf<Pair<Double, Double>>()
        pathCoords.add(Pair(startLat, startLng))
        waypoints.forEach { site ->
            pathCoords.add(Pair(site.latitude, site.longitude))
        }
        pathCoords.add(Pair(endLat, endLng))

        // Calculate total distance along path
        var totalDistMiles = 0.0f
        val legDistances = mutableListOf<Float>()
        for (i in 0 until pathCoords.size - 1) {
            val leg = distanceMiles(
                pathCoords[i].first, pathCoords[i].second,
                pathCoords[i + 1].first, pathCoords[i + 1].second
            )
            legDistances.add(leg)
            totalDistMiles += leg
        }

        if (totalDistMiles < 0.01f) {
            totalDistMiles = 1.0f
        }

        val points = mutableListOf<ElevationPoint>()
        var currentAscent = 0
        var currentDescent = 0
        var maxGrade = 0.0f
        var prevElev: Int? = null

        val numSamples = max(steps, 16)
        for (step in 0..numSamples) {
            val progressRatio = step.toFloat() / numSamples.toFloat()
            val currentMile = progressRatio * totalDistMiles

            // Interpolate position along segmented polyline
            val (lat, lng) = interpolatePolyline(pathCoords, legDistances, progressRatio)
            val elev = getElevationFeet(lat, lng)
            val stratum = getStratum(elev)

            // Check if near any waypoint
            val nearestWaypoint = waypoints.firstOrNull { site ->
                distanceMiles(lat, lng, site.latitude, site.longitude) < 0.25f
            }

            points.add(
                ElevationPoint(
                    distanceMiles = currentMile,
                    elevationFt = elev,
                    lat = lat,
                    lng = lng,
                    stratum = stratum,
                    landmarkName = nearestWaypoint?.title
                )
            )

            if (prevElev != null) {
                val delta = elev - prevElev
                if (delta > 0) currentAscent += delta else currentDescent += -delta
                val distStepMiles = totalDistMiles / numSamples
                if (distStepMiles > 0.001f) {
                    val grade = (delta / (distStepMiles * 5280f)) * 100f
                    if (kotlin.math.abs(grade) > kotlin.math.abs(maxGrade)) {
                        maxGrade = grade
                    }
                }
            }
            prevElev = elev
        }

        val minElev = points.minOfOrNull { it.elevationFt } ?: 430
        val maxElev = points.maxOfOrNull { it.elevationFt } ?: 980
        val currentElev = points.firstOrNull()?.elevationFt ?: 430

        return TrailProfileTelemetry(
            points = points,
            currentElevationFt = currentElev,
            maxSummitElevationFt = maxElev,
            minValleyElevationFt = minElev,
            totalAscentFt = currentAscent,
            totalDescentFt = currentDescent,
            maxGradePercent = maxGrade,
            currentStratum = getStratum(currentElev)
        )
    }

    private fun interpolatePolyline(
        coords: List<Pair<Double, Double>>,
        legs: List<Float>,
        fraction: Float
    ): Pair<Double, Double> {
        if (coords.isEmpty()) return Pair(38.2830, -85.8200)
        if (coords.size == 1 || legs.isEmpty()) return coords.first()

        val totalDist = legs.sum()
        val targetDist = fraction * totalDist
        var accumDist = 0.0f

        for (i in legs.indices) {
            val legLen = legs[i]
            if (targetDist <= accumDist + legLen || i == legs.lastIndex) {
                val legFrac = if (legLen > 0.0001f) ((targetDist - accumDist) / legLen).coerceIn(0.0f, 1.0f) else 0.0f
                val p1 = coords[i]
                val p2 = coords[i + 1]
                val lat = p1.first + (p2.first - p1.first) * legFrac
                val lng = p1.second + (p2.second - p1.second) * legFrac
                return Pair(lat, lng)
            }
            accumDist += legLen
        }
        return coords.last()
    }

    private fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    private fun distanceMiles(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        return (distanceKm(lat1, lon1, lat2, lon2) * 0.621371).toFloat()
    }
}
