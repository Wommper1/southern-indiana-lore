package com.example.data.model

data class LoreSite(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val county: String = "Floyd County",
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val era: String,
    val accessStatus: String,
    val fullStory: String,
    val verifiedHistory: String = "",
    val folkloreTradition: String = "",
    val confidenceLevel: ConfidenceLevel = ConfidenceLevel.UNVERIFIED,
    val sources: String = "",
    val sourceNotes: String,
    val verificationGrade: String,
    val geofenceRadiusMeters: Int,
    val audioNarration: String,
    val isFavorite: Boolean = false,
    val isVisited: Boolean = false,
    val userNotes: String = "",
    val lastVisitedTimestamp: Long = 0L,
    val distanceMeters: Float = 0f,
    val isCustom: Boolean = false,
    val photoUri: String? = null,
    // Proximity Historian Dimensions:
    val alternativeNames: String = "",
    val siteType: String = "POINT", // POINT, LINE, POLYGON
    val subType: String = "",
    val triggerRadiusType: String = "LOCAL_SITE", // EXACT_SITE (50-150m), LOCAL_SITE (150-500m), LANDSCAPE (250m-2km), REGIONAL_STORY
    val driveStory: String = "", // 1-2 sentence Drive Version for fast reading / HUD / TTS
    val whyHere: String = "", // "Why did this exist here?"
    val historyNerdDiscovery: String = "", // 🧠 HISTORY NERD DISCOVERY note
    val aboveLayer: String = "", // Three-dimensional: Above
    val surfaceLayer: String = "", // Three-dimensional: Surface
    val belowLayer: String = "", // Three-dimensional: Below
    val time1800: String = "", // Time layer 1800
    val time1850: String = "", // Time layer 1850
    val time1900: String = "", // Time layer 1900
    val time1950: String = "", // Time layer 1950
    val timeToday: String = "", // Time layer Today
    val whatMayBeUnderground: String = "", // "Under Your Feet" subterranean details
    val visibleRemains: String = "", // Visible remains
    val relatedLocations: String = "", // Connected history chain locations
    val relatedPeople: String = "",
    val relatedEvents: String = "",
    val mapEvidence: String = "",
    val unresolvedQuestions: String = "",
    val accessLevel: String = "PUBLIC ACCESS", // PUBLIC ACCESS, PUBLIC VIEWING, PRIVATE PROPERTY, RESTRICTED, SENSITIVE LOCATION, UNKNOWN
    val isSensitiveLocation: Boolean = false,
    val isExactCoordinateSafe: Boolean = true,
    val radarType: String = "HISTORIC_NEARBY", // UNDERGROUND, LOST_LANDSCAPE, RIVER_MEMORY, ROAD_DETECTIVE, FOLKLORE, HISTORIC_NEARBY, MAP_ANOMALY
    val youJustCrossedEvent: String = "", // "You just crossed the Buffalo Trace"
    val geometryCoordinates: String = "", // Semicolon-separated "lat,lng;lat,lng" for LINE or POLYGON
    val undergroundConfidence: String = "", // "Confirmed passage nearby", "Mapped cave system in this area", "Documented karst terrain", "Possible underground connection"
    val encounterCount: Int = 0,
    val firstDiscoveredTimestamp: Long = 0L,
    val lastEncounterTimestamp: Long = 0L,
    val hasListenedAudio: Boolean = false
)

object UndergroundConfidenceLevels {
    const val CONFIRMED_PASSAGE = "Confirmed passage nearby"
    const val MAPPED_CAVE_SYSTEM = "Mapped cave system in this area"
    const val DOCUMENTED_KARST = "Documented karst terrain"
    const val POSSIBLE_CONNECTION = "Possible underground connection"
}

object LoreRadarTypes {
    const val ALL = "All Radar"
    const val UNDERGROUND = "Under Your Feet 🕳️"
    const val LOST_LANDSCAPE = "Lost Landscape 🚂"
    const val RIVER_MEMORY = "River Memory 🌊"
    const val ROAD_DETECTIVE = "Road Detective 🛣️"
    const val HISTORIC_NEARBY = "History Nearby 📍"
    const val FOLKLORE = "Local Lore 👻"
    const val MAP_ANOMALY = "Map Anomaly 🗺️"
    const val YOU_JUST_CROSSED = "Crossings ⚡"

    val list = listOf(
        ALL,
        UNDERGROUND,
        LOST_LANDSCAPE,
        RIVER_MEMORY,
        ROAD_DETECTIVE,
        HISTORIC_NEARBY,
        FOLKLORE,
        MAP_ANOMALY
    )

    fun getDisplayName(radarType: String): String {
        return when (radarType.uppercase()) {
            "UNDERGROUND" -> "🕳️ BENEATH YOU"
            "LOST_LANDSCAPE" -> "🚂 LOST LANDSCAPE"
            "RIVER_MEMORY" -> "🌊 RIVER MEMORY"
            "ROAD_DETECTIVE" -> "🛣️ ROAD DETECTIVE"
            "FOLKLORE" -> "👻 LOCAL LORE"
            "MAP_ANOMALY" -> "🗺️ MAP ANOMALY"
            "CROSSING" -> "⚡ YOU JUST CROSSED"
            else -> "📍 HISTORY NEARBY"
        }
    }
}

object LoreAccessLevels {
    const val PUBLIC_ACCESS = "PUBLIC ACCESS"
    const val PUBLIC_VIEWING = "PUBLIC VIEWING"
    const val PRIVATE_PROPERTY = "PRIVATE PROPERTY"
    const val RESTRICTED = "RESTRICTED"
    const val SENSITIVE_LOCATION = "SENSITIVE LOCATION"
    const val UNKNOWN = "UNKNOWN"
}

object LoreCounties {
    const val ALL = "All Counties"
    const val FLOYD = "Floyd County"
    const val CLARK = "Clark County"
    const val HARRISON = "Harrison County"
    const val LAWRENCE = "Lawrence County"
    const val CRAWFORD = "Crawford County"
    const val ORANGE = "Orange County"
    const val MARTIN = "Martin County"
    const val JEFFERSON = "Jefferson County"
    const val PERRY = "Perry County"
    const val SPENCER = "Spencer County"
    const val VANDERBURGH = "Vanderburgh County"
    const val POSEY = "Posey County"
    const val KNOX = "Knox County"
    const val SCOTT = "Scott County"
    const val JACKSON = "Jackson County"
    const val DUBOIS = "Dubois County"
    const val WASHINGTON = "Washington County"

    val list = listOf(
        ALL, FLOYD, CLARK, HARRISON, LAWRENCE, CRAWFORD, ORANGE, MARTIN,
        JEFFERSON, PERRY, SPENCER, VANDERBURGH, POSEY, KNOX, SCOTT, JACKSON,
        DUBOIS, WASHINGTON
    )
}

object LoreCategories {
    const val ALL = "All Sites"
    const val HAUNTED = "Haunted & Legends"
    const val UNDERGROUND_RR = "Underground Railroad"
    const val STEAM_AND_RAIL = "Steamboats & Rail"
    const val PIONEER = "Pioneers & Founders"
    const val CURIOSITIES = "Lost Curiosities"

    val list = listOf(ALL, HAUNTED, UNDERGROUND_RR, STEAM_AND_RAIL, PIONEER, CURIOSITIES)
}

object LoreEras {
    const val ALL = "All Eras"
    const val INDIGENOUS_TERRITORIAL = "Pre-1820 (Territory & Frontier)"
    const val ANTEBELLUM_RIVER = "1820–1860 (Antebellum & Riverboat)"
    const val CIVIL_WAR_GILDED = "1861–1900 (Civil War & Gilded Age)"
    const val EARLY_TWENTIETH = "1901–1950 (Industrial & Modern)"

    val list = listOf(
        ALL,
        INDIGENOUS_TERRITORIAL,
        ANTEBELLUM_RIVER,
        CIVIL_WAR_GILDED,
        EARLY_TWENTIETH
    )

    fun matchesEra(eraText: String, selectedEra: String): Boolean {
        if (selectedEra == ALL) return true
        val lower = eraText.lowercase()
        val yearMatch = Regex("""\b(1[6-9]\d{2}|20\d{2})\b""").find(eraText)?.value?.toIntOrNull()

        return when (selectedEra) {
            INDIGENOUS_TERRITORIAL -> {
                lower.contains("frontier") || lower.contains("territor") || lower.contains("indigenous") ||
                        (yearMatch != null && yearMatch < 1820)
            }
            ANTEBELLUM_RIVER -> {
                lower.contains("antebellum") || lower.contains("steamboat") || lower.contains("185") || lower.contains("183") || lower.contains("184") ||
                        (yearMatch != null && yearMatch in 1820..1860)
            }
            CIVIL_WAR_GILDED -> {
                lower.contains("civil war") || lower.contains("gilded") || lower.contains("victorian") || lower.contains("186") || lower.contains("187") || lower.contains("188") || lower.contains("189") ||
                        (yearMatch != null && yearMatch in 1861..1900)
            }
            EARLY_TWENTIETH -> {
                lower.contains("industrial") || lower.contains("ww") || lower.contains("190") || lower.contains("191") || lower.contains("192") || lower.contains("193") || lower.contains("194") ||
                        (yearMatch != null && yearMatch > 1900)
            }
            else -> true
        }
    }
}
