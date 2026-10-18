package com.example.data.model

import androidx.compose.ui.graphics.Color

/**
 * Geological cross-section stratum encountered while lowering
 * the hydrophone into Southern Indiana karst limestone.
 */
data class KarstStratum(
    val depthRangeFeet: ClosedFloatingPointRange<Float>,
    val name: String,
    val geologicalEra: String,
    val lithologyDescription: String,
    val acousticReverbSeconds: Float,
    val isWaterSubmerged: Boolean,
    val waterTemperatureF: Float? = null,
    val discoveredArtifactOrAnomaly: String? = null,
    val stratumColor: Color = Color(0xFF2C251D)
)

/**
 * Historical subterranean well or sinkhole sounding station.
 */
data class KarstWellSpecimen(
    val id: String,
    val title: String,
    val county: String,
    val historicalSignificance: String,
    val surfaceElevationFt: Int,
    val waterTableDepthFt: Float,
    val maxSoundingDepthFt: Float,
    val strata: List<KarstStratum>
)

data class LoggedSoundingEntry(
    val id: String,
    val wellId: String,
    val depthFt: Float,
    val stratumName: String,
    val waterTempF: Float?,
    val note: String,
    val timestamp: Long = System.currentTimeMillis()
)

object KarstHydrophoneRegistry {

    val LOST_RIVER_SIPHON = KarstWellSpecimen(
        id = "well_lost_river_siphon",
        title = "Lost River Subterranean Siphon",
        county = "Orange County",
        historicalSignificance = "The sunken river flows 23 miles completely underground through subterranean Mitchell limestone caverns before rising near Orangeville.",
        surfaceElevationFt = 640,
        waterTableDepthFt = 74f,
        maxSoundingDepthFt = 360f,
        strata = listOf(
            KarstStratum(
                depthRangeFeet = 0f..35f,
                name = "Epikarst Soil & Residual Chert",
                geologicalEra = "Quaternary Alluvium",
                lithologyDescription = "Dry sinkhole topsoil with angular nodules of Indian Creek fossilized chert.",
                acousticReverbSeconds = 0.3f,
                isWaterSubmerged = false,
                stratumColor = Color(0xFF4A3728)
            ),
            KarstStratum(
                depthRangeFeet = 35f..74f,
                name = "Upper Paoli Limestone Fissure",
                geologicalEra = "Mississippian • 330 Million B.C.",
                lithologyDescription = "Vertical vadose dissolution joint. Cave crickets and dry lime drip echoes.",
                acousticReverbSeconds = 0.9f,
                isWaterSubmerged = false,
                stratumColor = Color(0xFF382F24)
            ),
            KarstStratum(
                depthRangeFeet = 74f..180f,
                name = "Lost River Main Aquifer Channel",
                geologicalEra = "Ste. Genevieve Karst Conduit",
                lithologyDescription = "Subterranean roaring watercourse carrying subterranean blind crayfish and cave salamanders.",
                acousticReverbSeconds = 1.8f,
                isWaterSubmerged = true,
                waterTemperatureF = 52.4f,
                discoveredArtifactOrAnomaly = "Subterranean acoustic frequency: 54 Hz river rushing resonance.",
                stratumColor = Color(0xFF133E4D)
            ),
            KarstStratum(
                depthRangeFeet = 180f..360f,
                name = "St. Louis Phreatic Siphon Tube",
                geologicalEra = "Deep Lithostrotion Reef Bed",
                lithologyDescription = "Completely submerged, pressurized limestone tube with fossil colonial coral colonies.",
                acousticReverbSeconds = 2.6f,
                isWaterSubmerged = true,
                waterTemperatureF = 50.8f,
                discoveredArtifactOrAnomaly = "Hydrophone acoustic detection: Echoing hydro-sonar pulse reflections at 285 ft.",
                stratumColor = Color(0xFF0C2431)
            )
        )
    )

    val WYANDOTTE_CAVE_WELL = KarstWellSpecimen(
        id = "well_wyandotte_saltpeter",
        title = "Wyandotte Cavern Subterranean Shaft",
        county = "Crawford County",
        historicalSignificance = "Prehistoric Native Americans quarried Wyandotte chert here 4,000 years ago; War of 1812 miners extracted gunpowder saltpeter leaching vats.",
        surfaceElevationFt = 580,
        waterTableDepthFt = 110f,
        maxSoundingDepthFt = 310f,
        strata = listOf(
            KarstStratum(
                depthRangeFeet = 0f..45f,
                name = "Rothrock Entrance Colluvium",
                geologicalEra = "Pleistocene Slump",
                lithologyDescription = "Loose limestone scree with dry saltpeter nitrous earth deposits.",
                acousticReverbSeconds = 0.4f,
                isWaterSubmerged = false,
                stratumColor = Color(0xFF3E3224)
            ),
            KarstStratum(
                depthRangeFeet = 45f..110f,
                name = "Monument Mountain Vadose Void",
                geologicalEra = "Upper Mitchell Formation",
                lithologyDescription = "Enormous 135-ft cavern rotunda void. Resonant drip echoes.",
                acousticReverbSeconds = 2.4f,
                isWaterSubmerged = false,
                stratumColor = Color(0xFF26201B)
            ),
            KarstStratum(
                depthRangeFeet = 110f..310f,
                name = "Pillared Palace Submerged Sump",
                geologicalEra = "Deep Salem Limestone Karst",
                lithologyDescription = "Massive submerged calcite columns and quiet phreatic lake.",
                acousticReverbSeconds = 2.1f,
                isWaterSubmerged = true,
                waterTemperatureF = 51.5f,
                discoveredArtifactOrAnomaly = "1812 saltpeter wooden vat timber reflection detected at 124 ft.",
                stratumColor = Color(0xFF102D36)
            )
        )
    )

    val MARENGO_CRYSTAL_SIPHON = KarstWellSpecimen(
        id = "well_marengo_crystal",
        title = "Marengo Cave Underground Creek Siphon",
        county = "Crawford County",
        historicalSignificance = "Discovered in 1883 by Orris and Blanche Hiestand chasing a rabbit down a sinkhole. Features the subterranean Old Town Spring creek.",
        surfaceElevationFt = 610,
        waterTableDepthFt = 65f,
        maxSoundingDepthFt = 280f,
        strata = listOf(
            KarstStratum(
                depthRangeFeet = 0f..30f,
                name = "Hiestand Sinkhole Chimney",
                geologicalEra = "Recent Karst Collapse",
                lithologyDescription = "Vertical collapse funnel choked with river gravel and fallen cedar roots.",
                acousticReverbSeconds = 0.5f,
                isWaterSubmerged = false,
                stratumColor = Color(0xFF453629)
            ),
            KarstStratum(
                depthRangeFeet = 30f..65f,
                name = "Crystal Palace Dripstone Chamber",
                geologicalEra = "Middle Mitchell Carbonate",
                lithologyDescription = "Active soda-straw stalactites and flowstone draperies.",
                acousticReverbSeconds = 1.6f,
                isWaterSubmerged = false,
                stratumColor = Color(0xFF33271F)
            ),
            KarstStratum(
                depthRangeFeet = 65f..280f,
                name = "Old Town Spring Underground Creek",
                geologicalEra = "Sub-Mitchell Gravel Conduit",
                lithologyDescription = "Fast-flowing subterranean stream cutting through dark bituminous limestone.",
                acousticReverbSeconds = 2.0f,
                isWaterSubmerged = true,
                waterTemperatureF = 53.1f,
                discoveredArtifactOrAnomaly = "Continuous subterranean creek flow acoustic ripple: 112 Hz.",
                stratumColor = Color(0xFF0F323D)
            )
        )
    )

    val allWells = listOf(LOST_RIVER_SIPHON, WYANDOTTE_CAVE_WELL, MARENGO_CRYSTAL_SIPHON)

    fun getById(id: String): KarstWellSpecimen = allWells.find { it.id == id } ?: LOST_RIVER_SIPHON

    fun findStratumAtDepth(well: KarstWellSpecimen, depthFt: Float): KarstStratum {
        return well.strata.firstOrNull { depthFt in it.depthRangeFeet }
            ?: well.strata.last()
    }
}
