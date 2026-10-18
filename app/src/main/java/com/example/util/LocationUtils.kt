package com.example.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

data class GeoPoint(val lat: Double, val lng: Double)

object LocationUtils {
    // Default reference location: New Albany City Center, Floyd County, IN
    const val DEFAULT_LATITUDE = 38.2856
    const val DEFAULT_LONGITUDE = -85.8241

    /**
     * Calculates distance between two coordinates in meters using Haversine formula
     */
    fun calculateDistanceMeters(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Float {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (r * c).toFloat()
    }

    /**
     * Calculates initial bearing in degrees from point 1 to point 2 (0° = North, 90° = East)
     */
    fun calculateBearing(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Float {
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val lambdaDiff = Math.toRadians(lon2 - lon1)

        val y = sin(lambdaDiff) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(lambdaDiff)
        val initialBearing = Math.toDegrees(atan2(y, x))
        return ((initialBearing + 360) % 360).toFloat()
    }

    fun formatDistance(meters: Float): String {
        return if (meters < 1000) {
            "${meters.toInt()} m"
        } else {
            val miles = meters * 0.000621371f
            String.format("%.1f mi", miles)
        }
    }

    /**
     * Parses semicolon-separated lat,lng coordinates: "38.28,-85.82;38.29,-85.83"
     */
    fun parseCoordinates(coordsStr: String): List<GeoPoint> {
        if (coordsStr.isBlank()) return emptyList()
        return coordsStr.split(";").mapNotNull { pairStr ->
            val parts = pairStr.trim().split(",")
            if (parts.size >= 2) {
                val lat = parts[0].trim().toDoubleOrNull()
                val lng = parts[1].trim().toDoubleOrNull()
                if (lat != null && lng != null) GeoPoint(lat, lng) else null
            } else null
        }
    }

    /**
     * Calculates the minimum perpendicular distance in meters from a user point to a polyline (LINE)
     */
    fun calculateDistanceToPolylineMeters(
        userLat: Double,
        userLng: Double,
        points: List<GeoPoint>
    ): Float {
        if (points.isEmpty()) return Float.MAX_VALUE
        if (points.size == 1) return calculateDistanceMeters(userLat, userLng, points[0].lat, points[0].lng)

        var minDistance = Float.MAX_VALUE
        for (i in 0 until points.size - 1) {
            val p1 = points[i]
            val p2 = points[i + 1]
            val dist = distanceToSegmentMeters(userLat, userLng, p1.lat, p1.lng, p2.lat, p2.lng)
            if (dist < minDistance) {
                minDistance = dist
            }
        }
        return minDistance
    }

    /**
     * Ray-casting algorithm to test if point is inside a polygon (AREA)
     */
    fun isPointInPolygon(userLat: Double, userLng: Double, polygon: List<GeoPoint>): Boolean {
        if (polygon.size < 3) return false
        var inside = false
        var j = polygon.size - 1
        for (i in polygon.indices) {
            val pi = polygon[i]
            val pj = polygon[j]
            if ((pi.lng > userLng) != (pj.lng > userLng) &&
                (userLat < (pj.lat - pi.lat) * (userLng - pi.lng) / (pj.lng - pi.lng) + pi.lat)
            ) {
                inside = !inside
            }
            j = i
        }
        return inside
    }

    /**
     * Distance to polygon: 0 if inside, otherwise shortest distance to boundary
     */
    fun calculateDistanceToPolygonMeters(
        userLat: Double,
        userLng: Double,
        polygon: List<GeoPoint>
    ): Float {
        if (polygon.isEmpty()) return Float.MAX_VALUE
        if (isPointInPolygon(userLat, userLng, polygon)) return 0f

        // Loop perimeter
        val closedPoly = if (polygon.first() != polygon.last()) polygon + polygon.first() else polygon
        return calculateDistanceToPolylineMeters(userLat, userLng, closedPoly)
    }

    /**
     * Calculates distance from point (pLat, pLng) to line segment between (aLat, aLng) and (bLat, bLng)
     */
    private fun distanceToSegmentMeters(
        pLat: Double, pLng: Double,
        aLat: Double, aLng: Double,
        bLat: Double, bLng: Double
    ): Float {
        // Convert to local meter space with equirectangular projection centered at A
        val midLat = Math.toRadians((aLat + bLat) / 2.0)
        val mPerLat = 111132.954 - 559.822 * cos(2 * midLat) + 1.175 * cos(4 * midLat)
        val mPerLng = 111412.84 * cos(midLat) - 93.5 * cos(3 * midLat)

        val ax = 0.0
        val ay = 0.0
        val bx = (bLng - aLng) * mPerLng
        val by = (bLat - aLat) * mPerLat
        val px = (pLng - aLng) * mPerLng
        val py = (pLat - aLat) * mPerLat

        val segLenSq = bx * bx + by * by
        if (segLenSq == 0.0) {
            return sqrt(px * px + py * py).toFloat()
        }

        // Project point P onto line AB, computing parameterized t
        val t = max(0.0, min(1.0, (px * bx + py * by) / segLenSq))
        val projX = ax + t * (bx - ax)
        val projY = ay + t * (by - ay)

        val dx = px - projX
        val dy = py - projY
        return sqrt(dx * dx + dy * dy).toFloat()
    }

    /**
     * Tests whether line segment (p1 -> p2) intersects line segment (p3 -> p4).
     * Essential for highway crossing detection where a user crosses a corridor between GPS samples.
     */
    fun doSegmentsIntersect(
        p1Lat: Double, p1Lng: Double,
        p2Lat: Double, p2Lng: Double,
        p3Lat: Double, p3Lng: Double,
        p4Lat: Double, p4Lng: Double
    ): Boolean {
        fun ccw(ax: Double, ay: Double, bx: Double, by: Double, cx: Double, cy: Double): Double {
            return (cy - ay) * (bx - ax) - (by - ay) * (cx - ax)
        }

        val d1 = ccw(p3Lng, p3Lat, p4Lng, p4Lat, p1Lng, p1Lat)
        val d2 = ccw(p3Lng, p3Lat, p4Lng, p4Lat, p2Lng, p2Lat)
        val d3 = ccw(p1Lng, p1Lat, p2Lng, p2Lat, p3Lng, p3Lat)
        val d4 = ccw(p1Lng, p1Lat, p2Lng, p2Lat, p4Lng, p4Lat)

        if (((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) &&
            ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0))) {
            return true
        }

        return false
    }
}
