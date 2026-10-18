package com.example.data.model

/**
 * Historical Ground Control Point (GCP) used to georeference an authentic Sanborn map sheet.
 * Records precise pixel coords on the historical archival raster and modern geodetic WGS84 coordinates.
 */
data class GroundControlPoint(
    val id: String,
    val pixelX: Double,
    val pixelY: Double,
    val latitude: Double,
    val longitude: Double,
    val featureDescription: String,
    val selectionRationale: String,
    val residualErrorMeters: Double,
    val confidence: MapAlignmentConfidence
)

/**
 * Individual Georeferenced Sanborn Sheet Metadata.
 * Ensures each sheet retains its own mathematical transformation, bounding geometry, and error audit.
 */
data class SanbornSheetRecord(
    val sheetNumber: Int,
    val editionYear: Int,
    val sheetTitle: String,
    val sourceInstitution: String,
    val archiveIdentifier: String,
    val locDigitalId: String,
    val sourceHashSha256: String,
    val originalScale: String, // e.g. "1 inch = 50 feet (1:600)"
    val processingHistory: String,
    val georeferenceStatus: GeoreferenceStatus,
    val transformationType: String, // "Affine 1st Order Polynomial" or "Thin Plate Spline"
    val gcpList: List<GroundControlPoint>,
    val rmsErrorMeters: Double,
    val maxResidualMeters: Double,
    val medianResidualMeters: Double,
    val alignmentClassification: MapAlignmentConfidence,
    val minLat: Double,
    val maxLat: Double,
    val minLng: Double,
    val maxLng: Double,
    val pmtilesSizeKb: Long = 420L
) {
    // Alias for compatibility with test & provenance assertions
    val gcps: List<GroundControlPoint> get() = gcpList
    val yearSurveyed: Int get() = editionYear

    /**
     * Checks if coordinates fall within this specific sheet's survey bounds.
     */
    fun contains(lat: Double, lng: Double): Boolean {
        return lat in minLat..maxLat && lng in minLng..maxLng
    }
}
