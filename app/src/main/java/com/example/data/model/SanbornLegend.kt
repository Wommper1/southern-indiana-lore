package com.example.data.model

/**
 * Standard Sanborn Fire Insurance Map Color & Construction Legend.
 * Strictly adheres to 19th and early 20th century Sanborn Map Company publishing standards.
 */
enum class SanbornBuildingMaterial(
    val code: String,
    val colorHex: Long, // ARGB
    val description: String,
    val fireResistance: String
) {
    WOOD_FRAME("YELLOW", 0xFFEAB308, "Frame / Wood Construction (Yellow)", "Combustible"),
    BRICK("RED_PINK", 0xFFEF4444, "Brick Wall Construction (Red / Pink)", "Fire-Resistant Outer Walls"),
    STONE_OR_CONCRETE("BLUE", 0xFF3B82F6, "Stone, Concrete, or Cement Block (Blue)", "Non-Combustible"),
    IRON_OR_METAL("GRAY", 0xFF94A3B8, "Iron or Sheet Metal Clad / Steel Frame (Gray)", "Non-Combustible Frame"),
    ADOBE("BROWN", 0xFFB45309, "Adobe / Composite (Brown)", "Moderate"),
    UNKNOWN("TRANSPARENT", 0x4094A3B8, "Map symbol interpretation uncertain", "Uncertain");

    val materialName: String get() = when (this) {
        WOOD_FRAME -> "Frame / Wood"
        BRICK -> "Brick Wall"
        STONE_OR_CONCRETE -> "Stone / Concrete"
        IRON_OR_METAL -> "Iron / Sheet Metal"
        ADOBE -> "Adobe / Composite"
        UNKNOWN -> "Unknown"
    }

    val significance: String get() = fireResistance
}

/**
 * Authentic Sanborn Symbol Conventions extracted from historical map legends.
 */
object SanbornLegendGuide {

    val materials: List<SanbornBuildingMaterial> = SanbornBuildingMaterial.values().filter { it != SanbornBuildingMaterial.UNKNOWN }

    /**
     * Translates building stories notation (e.g., '1', '2', '2 1/2', 'B-3')
     */
    fun parseStories(storiesNotation: String): String {
        return when {
            storiesNotation.contains("1/2") -> "$storiesNotation Stories (Attic / Half-story under roof)"
            storiesNotation.startsWith("B-") -> "${storiesNotation.substring(2)} Stories + Basement"
            else -> "$storiesNotation Stories"
        }
    }

    /**
     * Map symbols for roof types, fire walls, openings, and industrial equipment.
     */
    val commonSymbols = mapOf(
        "SOLID_LINE_BLACK" to "Exterior continuous brick or stone wall",
        "DASHED_LINE" to "Frame partition or wooden outbuilding wall",
        "DOTS_ON_LINE" to "Parapet fire wall projecting 6 to 12 inches above roofline",
        "CIRCLE_INSIDE" to "Steam boiler, engine, or cupola furnace",
        "X_IN_BOX" to "Stable or carriage house",
        "D" to "Dwelling / Private Residence",
        "S" to "Store / Retail Merchant",
        "FLATS" to "Tenement / Multi-family residential flats",
        "OFF" to "Office / Commercial Administration",
        "W.Ho." to "Warehouse / Freight storage",
        "B" to "Basement present under footprint"
    )
}
