package com.example.data.model

import com.example.service.CrossingState
import com.example.util.LocationUtils

enum class CrossingCategory(val label: String, val icon: String) {
    SUBTERRANEAN_VOID("Subterranean Karst & Cave", "🕳️"),
    HISTORIC_BRIDGE("Historic River & Rail Bridge", "🌉"),
    CORRIDOR_CROSSING("Historic Trail & Rail Corridor", "⚡")
}

data class CrossingCountdown(
    val site: LoreSite,
    val distanceMeters: Float,
    val bearingDegrees: Float,
    val estimatedSeconds: Int,
    val category: CrossingCategory,
    val progressPercent: Float,
    val state: CrossingState
) {
    val formattedDistance: String get() = LocationUtils.formatDistance(distanceMeters)

    val formattedEta: String
        get() = when {
            distanceMeters <= (site.geofenceRadiusMeters.coerceAtLeast(30)) -> "CROSSING NOW"
            estimatedSeconds < 60 -> "${estimatedSeconds}s away"
            else -> {
                val mins = estimatedSeconds / 60
                val secs = estimatedSeconds % 60
                "${mins}m ${secs}s away"
            }
        }

    val isImminent: Boolean
        get() = distanceMeters <= (site.geofenceRadiusMeters * 1.5f).coerceAtLeast(150f)
}
