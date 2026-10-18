package com.example.data.model

/**
 * Historical Building Footprint manually audited from verified Sanborn Fire Insurance Maps.
 * Captures construction, stories, verified occupancy, and provenance.
 */
data class HistoricalBuildingFootprint(
    val id: String,
    val sourceYear: Int,
    val sourceSheetNumber: Int,
    val originalMapLabel: String,
    val modernAddressOrLocation: String,
    val centerLat: Double,
    val centerLng: Double,
    // Footprint perimeter polygon vertices (lat, lng pairs)
    val boundaryPolygon: List<Pair<Double, Double>>,
    val material: SanbornBuildingMaterial,
    val storiesNotation: String,
    val primaryUse: String,
    val interpretationNotes: String,
    val geometryConfidence: MapAlignmentConfidence,
    val historicalConfidence: ConfidenceLevel,
    val isManualReviewComplete: Boolean = true
) {
    // Convenience aliases for UI layers and legacy test accessors
    val footprintId: String get() = id
    val buildingName: String get() = originalMapLabel
    val yearSurveyed: Int get() = sourceYear
    val boundaryCoordinates: List<Pair<Double, Double>> get() = boundaryPolygon
    val constructionMaterial: String get() = when (material) {
        SanbornBuildingMaterial.WOOD_FRAME -> "FRAME_WOOD"
        SanbornBuildingMaterial.BRICK -> "BRICK"
        SanbornBuildingMaterial.STONE_OR_CONCRETE -> "STONE_CONCRETE"
        SanbornBuildingMaterial.IRON_OR_METAL -> "IRON_METAL"
        SanbornBuildingMaterial.ADOBE -> "ADOBE"
        SanbornBuildingMaterial.UNKNOWN -> "UNKNOWN"
    }
    val occupancyType: String get() = if (primaryUse.contains("Residence", ignoreCase = true) || primaryUse.contains("Dwelling", ignoreCase = true)) "DWELLING" else primaryUse
}

/**
 * Types of urban and infrastructural historical change across periods.
 */
enum class HistoricalChangeType {
    BUILT,
    DEMOLISHED,
    DEMOLISHED_FOR_FLOODWALL,
    EXPANDED,
    RENAMED,
    REPURPOSED,
    ROAD_ADDED,
    ROAD_REMOVED,
    RAILROAD_ADDED,
    RAILROAD_REMOVED,
    WATERWAY_CHANGED,
    INDUSTRIAL_USE_CHANGED,
    PROPERTY_CHANGED,
    UNKNOWN_CHANGE
}

/**
 * Change detection model tracking what stood here between historical periods.
 */
data class HistoricalChangeEvent(
    val id: String,
    val locationDescription: String,
    val latitude: Double,
    val longitude: Double,
    val startYear: Int,
    val endYear: Int,
    val changeType: HistoricalChangeType,
    val sourceBefore: String,
    val sourceAfter: String,
    val description: String,
    val whatStoodBefore: String,
    val whatStoodAfter: String,
    val whatExistsToday: String,
    val historicalConfidence: ConfidenceLevel,
    val locationConfidence: ConfidenceLevel,
    val curatorNotes: String
) {
    val eventType: HistoricalChangeType get() = changeType
}

/**
 * Historical street designation lineage tracking renamed, removed, or widened corridors.
 */
data class LostStreetRecord(
    val id: String,
    val currentStreetName: String,
    val formerHistoricName: String,
    val alternateDesignation: String? = null,
    val status: String, // SURVIVES_RENAMED, REMOVED_BY_HIGHWAY, CONSUMED_BY_DEVELOPMENT, FLOODWALL_VACATED
    val startCoord: Pair<Double, Double>,
    val endCoord: Pair<Double, Double>,
    val historicalSignificance: String,
    val primarySource: String
)
