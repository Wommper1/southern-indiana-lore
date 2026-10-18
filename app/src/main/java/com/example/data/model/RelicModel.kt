package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class ReliefTextureType {
    CRINOID_FOSSIL,
    STEAMBOAT_MEDALLION,
    SALTPETER_POWDER_HORN,
    FREEDOM_COMPASS,
    TUNNEL_STAR_CHISEL,
    FRENCH_BUHR_MILLSTONE,
    ABOLITIONIST_SEAL,
    CULBERTSON_GILDED_KEY
}

enum class CharcoalPigment(
    val displayName: String,
    val color: Color,
    val hexValue: Long
) {
    GRAPHITE_EBONY("Graphite Ebony", Color(0xFF1E1C1A), 0xFF1E1C1A),
    SEPIA_UMBER("Sepia Umber", Color(0xFF3E2723), 0xFF3E2723),
    BURNT_SIENNA("Burnt Sienna", Color(0xFF5D2E1F), 0xFF5D2E1F),
    CHARCOAL_ASH("Charcoal Ash", Color(0xFF374151), 0xFF374151)
}

enum class RubbingPaperType(
    val displayName: String,
    val tintColor: Color,
    val textureDescription: String
) {
    AGED_PARCHMENT("Aged Parchment", Color(0xFFF5EACB), "Warm 1850s rag linen with natural watermarks"),
    VELLUM_TRACING("Vellum Tracing", Color(0xFFF3EED9), "Translucent drafting vellum with faint grid fibers"),
    CARBON_TISSUE("Carbon Tissue", Color(0xFFECE4D0), "Heavy rag stock with deckled deck edges")
}

data class RelicReliefFeature(
    val x: Float, // Relative X (0.0 to 1.0)
    val y: Float, // Relative Y (0.0 to 1.0)
    val radius: Float, // Relative radius
    val strokeWidth: Float,
    val featureType: FeatureType,
    val textLabel: String = ""
) {
    enum class FeatureType {
        CONCENTRIC_RINGS,
        STAR_EMBOSS,
        COMPASS_ROSE,
        GEAR_TEETH,
        INSCRIPTION_ARC,
        CHISEL_BLADE,
        HORN_OUTLINE,
        KEY_BIT,
        FLORAL_FILIGREE,
        CROSS_HATCH
    }
}

data class RelicSpecimen(
    val id: String,
    val title: String,
    val era: String,
    val material: String,
    val provenance: String,
    val siteId: String,
    val siteName: String,
    val lat: Double,
    val lng: Double,
    val reliefType: ReliefTextureType,
    val rarity: RelicRarity = RelicRarity.RARE,
    val primaryColorHex: Long = 0xFFD4AF37,
    val inscriptions: List<String>,
    val curatorNotes: String,
    val archivalFrequencyKhz: Int,
    val archivalBroadcastTitle: String,
    val archivalDispatchTranscript: String,
    val speakerName: String,
    val speakerRole: String,
    val reliefFeatures: List<RelicReliefFeature>
)

data class RubbingPoint(
    val x: Float,
    val y: Float,
    val pressure: Float = 1.0f,
    val pigment: CharcoalPigment = CharcoalPigment.GRAPHITE_EBONY
)

data class SavedRubbingRecord(
    val relicId: String,
    val timestamp: Long,
    val completionPercent: Int,
    val pigmentUsed: CharcoalPigment,
    val paperType: RubbingPaperType,
    val notes: String = ""
)
