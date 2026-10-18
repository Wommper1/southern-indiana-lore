package com.example.data.model

/**
 * Alignment accuracy and spatial certainty of historical cartographic materials.
 * Separated strictly from Historical Confidence and Location Confidence.
 */
enum class MapAlignmentConfidence {
    SURVEY_GRADE, // Contemporary geodetic survey with high GCP correlation (<10m error)
    GOOD,         // High-quality municipal/USGS survey (10-30m spatial variance)
    APPROXIMATE,  // Pre-geodetic 19th-century county atlas or hand-drawn plat (30-100m variance)
    LOW,          // Generalized historical sketch, distortion along river bends (>100m variance)
    UNREFERENCED; // Schematic / artistic / non-rectified map

    companion object {
        // Compatibility aliases
        val HIGH: MapAlignmentConfidence get() = SURVEY_GRADE
        val VERY_HIGH: MapAlignmentConfidence get() = SURVEY_GRADE
    }
}

/**
 * Explicit status tracking for georeferencing to prevent describing proposed work as completed.
 */
enum class GeoreferenceStatus {
    VERIFIED_GEOREFERENCED,  // Transformation completed, GCPs recorded, RMS error verified
    GEOREFERENCING_PLANNED,  // Raw archive scan located, control point rectification planned
    APPROXIMATE_GEOMETRY,    // Digitized schematic boundary or visual approximation
    UNVERIFIED               // Archive record unverified or pending acquisition
}

enum class MapType {
    USGS_TOPO,
    SANBORN_FIRE_INSURANCE,
    COUNTY_ATLAS,
    PLAT_MAP,
    RAILROAD_ALIGNMENT,
    AERIAL_PHOTOGRAPH,
    HYDROLOGICAL_SURVEY,
    LOST_LANDSCAPE_RECONSTRUCTION
}

/**
 * Model representing an authentic georeferenced historical cartographic layer.
 * Includes complete provenance, rights, temporal bounds, and alignment confidence.
 */
data class HistoricalMapLayer(
    val id: String,
    val name: String,
    val yearStart: Int,
    val yearEnd: Int,
    val displayYear: String,
    val sourceInstitution: String,
    val sourceIdentifier: String,
    val sourceUrl: String,
    val rightsStatus: String, // e.g. "Public Domain", "US Government Work"
    val mapType: MapType,
    val coverageDescription: String,
    val minLat: Double,
    val maxLat: Double,
    val minLng: Double,
    val maxLng: Double,
    val minZoom: Int = 11,
    val maxZoom: Int = 17,
    val defaultOpacity: Float = 0.85f,
    val alignmentConfidence: MapAlignmentConfidence,
    val georeferenceStatus: GeoreferenceStatus = GeoreferenceStatus.GEOREFERENCING_PLANNED,
    val georeferencingMethod: String,
    val citation: String,
    val curatorialNotes: String,
    val isAvailableOffline: Boolean = true,
    // Local tile archive path or vector geometry identifier
    val localTileUri: String? = null,
    // Historical Lineage & Edition Chain metadata (for multi-edition evolution)
    val editionSeries: String? = null, // e.g. "Sanborn Map Company: New Albany Series"
    val sheetCount: Int? = null,
    val locDigitalId: String? = null,
    val iiifManifestUrl: String? = null
) {
    /**
     * Checks if a coordinate is within the valid coverage geometry of this historical map layer.
     */
    fun contains(lat: Double, lng: Double): Boolean {
        return lat in minLat..maxLat && lng in minLng..maxLng
    }
}
