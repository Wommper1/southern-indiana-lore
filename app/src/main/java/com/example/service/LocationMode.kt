package com.example.service

enum class LocationMode(val title: String, val description: String) {
    BROWSE(
        title = "Browse Mode",
        description = "Manual exploration and map browsing. No continuous background GPS polling."
    ),
    EXPLORE(
        title = "Explore Mode (Walking)",
        description = "On-foot historical walking tour. Polling every 12s / 10m to preserve battery while tracking close proximity."
    ),
    ROAD_TRIP(
        title = "Road Trip Mode (Driving)",
        description = "Speed-adaptive vehicle tour. Adapts sampling rate dynamically to vehicle speed."
    ),
    PASSIVE(
        title = "Passive Mode (Geofence Only)",
        description = "Low-power hardware geofence monitoring without active GPS polling."
    )
}
