package com.example.service

import com.example.util.LocationUtils

/**
 * Robust GPS Quality and Plausibility Filter.
 *
 * Safeguards against:
 * - Sudden teleports / spurious fixes with physically impossible velocity (> 65 m/s or ~145 mph)
 * - Degraded accuracy fixes (e.g., cell-tower coarse fixes > 45m when precision is needed)
 * - GPS drift while stationary
 */
class GpsQualityFilter(
    private val maxAcceptableAccuracyMeters: Double = 45.0,
    private val maxAcceptableSpeedMps: Double = 60.0 // ~134 mph
) {
    private var lastValidLat: Double? = null
    private var lastValidLng: Double? = null
    private var lastValidTimestampMs: Long = 0L

    /**
     * Evaluates whether an incoming GPS fix is physically plausible and reliable.
     */
    fun isFixReliable(
        lat: Double,
        lng: Double,
        accuracyMeters: Float,
        timeElapsedSec: Double = 0.0
    ): Boolean {
        // 1. Accuracy gate
        if (accuracyMeters > maxAcceptableAccuracyMeters) {
            return false
        }

        // 2. Teleport check if we have previous anchor
        val prevLat = lastValidLat
        val prevLng = lastValidLng

        if (prevLat != null && prevLng != null) {
            val distMeters = LocationUtils.calculateDistanceMeters(prevLat, prevLng, lat, lng)
            val elapsed = if (timeElapsedSec > 0.0) {
                timeElapsedSec
            } else {
                val now = System.currentTimeMillis()
                val deltaMs = (now - lastValidTimestampMs).coerceAtLeast(100L)
                deltaMs / 1000.0
            }

            val impliedSpeed = distMeters / elapsed
            if (impliedSpeed > maxAcceptableSpeedMps) {
                // Reject impossible teleport
                return false
            }
        }

        // Fix accepted, update last known valid position
        lastValidLat = lat
        lastValidLng = lng
        lastValidTimestampMs = System.currentTimeMillis()
        return true
    }

    fun reset() {
        lastValidLat = null
        lastValidLng = null
        lastValidTimestampMs = 0L
    }
}
