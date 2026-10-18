package com.example.data.local

/**
 * Seed data for rich offline media assets linked directly to Southern Indiana coordinate triggers.
 * Provides custom wizard narration, retro radio broadcasts, documentary chronicler dispatches,
 * and procedural acoustic audio assets.
 */
object LoreMediaSeedData {

    val assets: List<LoreMediaAssetEntity> = listOf(
        // --- Culbertson Mansion (Mansion Row, New Albany) ---
        LoreMediaAssetEntity(
            id = "media_culbertson_arrival_wizard",
            siteId = "floyd_culbertson_mansion",
            triggerTier = "ARRIVAL_100M",
            mediaType = "NARRATION_TTS",
            mediaPath = "narrator://wizard_narrator",
            personaId = "WIZARD_NARRATOR",
            title = "The Alchemist of Mansion Row: Whispers of Culbertson",
            description = "Eerie esoteric chronicle of William Culbertson's gilded fortune and phantom third-floor ballroom waltzes.",
            priority = 10,
            durationSec = 75,
            duckAmbientPercent = 0.15f
        ),
        LoreMediaAssetEntity(
            id = "media_culbertson_approach_radio",
            siteId = "floyd_culbertson_mansion",
            triggerTier = "APPROACH_500M",
            mediaType = "RADIO_BROADCAST",
            mediaPath = "narrator://retro_radio",
            personaId = "RETRO_RADIO",
            title = "1940s Historic Society Bulletin: Gilded Mansions on Main",
            description = "Vintage AM radio broadcast highlighting New Albany's historic architectural grandeur.",
            priority = 6,
            durationSec = 45,
            duckAmbientPercent = 0.30f
        ),

        // --- Town Clock Church (Freedom Beacon) ---
        LoreMediaAssetEntity(
            id = "media_town_clock_arrival_chronicler",
            siteId = "floyd_town_clock_church",
            triggerTier = "ARRIVAL_100M",
            mediaType = "NARRATION_TTS",
            mediaPath = "narrator://adventure_chronicler",
            personaId = "ADVENTURE_CHRONICLER",
            title = "Belfry Lantern Across the Ohio: Sanctuary at Town Clock",
            description = "Documentary chronicler retelling of freedom seekers crossing from Louisville into safe cellars.",
            priority = 10,
            durationSec = 90,
            duckAmbientPercent = 0.20f
        ),
        LoreMediaAssetEntity(
            id = "media_town_clock_approach_ambient",
            siteId = "floyd_town_clock_church",
            triggerTier = "APPROACH_1500M",
            mediaType = "PROCEDURAL_ACOUSTIC",
            mediaPath = "procedural://cavern_drip",
            personaId = "HISTORIAN",
            title = "Freedom Network Acoustic Horizon",
            description = "Atmospheric chime cue preparing the driver for approach toward the riverbank sanctuary.",
            priority = 4,
            durationSec = 15,
            duckAmbientPercent = 0.50f
        ),

        // --- Buffalo Trace (Ancient Bison Migration & Pioneer Highway) ---
        LoreMediaAssetEntity(
            id = "media_buffalo_trace_crossing_wizard",
            siteId = "clark_buffalo_trace",
            triggerTier = "CORRIDOR_CROSS",
            mediaType = "NARRATION_TTS",
            mediaPath = "narrator://wizard_narrator",
            personaId = "WIZARD_NARRATOR",
            title = "Corridor Cross: The Ancient Bison Trail Beneath Your Wheels",
            description = "Immediate crossing event when vehicle vectors intersect the 10,000-year-old animal highway.",
            priority = 10,
            durationSec = 60,
            duckAmbientPercent = 0.15f
        ),
        LoreMediaAssetEntity(
            id = "media_buffalo_trace_approach_sage",
            siteId = "clark_buffalo_trace",
            triggerTier = "APPROACH_500M",
            mediaType = "NARRATION_TTS",
            mediaPath = "narrator://folklore_sage",
            personaId = "FOLKLORE_SAGE",
            title = "Fireside Legend of the Great Trampleway",
            description = "Atmospheric sage account of mammoth herds carving sunken limestone corridors.",
            priority = 7,
            durationSec = 50,
            duckAmbientPercent = 0.25f
        ),

        // --- Rose Island Resort (Charlestown State Park / Ohio River) ---
        LoreMediaAssetEntity(
            id = "media_rose_island_arrival_radio",
            siteId = "clark_rose_island",
            triggerTier = "ARRIVAL_100M",
            mediaType = "RADIO_BROADCAST",
            mediaPath = "narrator://retro_radio",
            personaId = "RETRO_RADIO",
            title = "1923 Steamboat Dispatch: Fern Grove & Rose Island Escapades",
            description = "Crisp vintage newsreel announcing the landing of the Steamer Idlewild at Rose Island amusement park.",
            priority = 10,
            durationSec = 70,
            duckAmbientPercent = 0.20f
        ),
        LoreMediaAssetEntity(
            id = "media_rose_island_approach_wizard",
            siteId = "clark_rose_island",
            triggerTier = "APPROACH_1500M",
            mediaType = "NARRATION_TTS",
            mediaPath = "narrator://wizard_narrator",
            personaId = "WIZARD_NARRATOR",
            title = "The Vanished Kingdom of Fourteen Mile Creek",
            description = "Wizard narrator summoning echoes of the 1937 Great Flood that swallowed the island whole.",
            priority = 8,
            durationSec = 65,
            duckAmbientPercent = 0.20f
        ),

        // --- Falling Run Train Wreck (Ghost Rails of Floyd County) ---
        LoreMediaAssetEntity(
            id = "media_falling_run_crossing_chronicler",
            siteId = "floyd_falling_run_train",
            triggerTier = "CORRIDOR_CROSS",
            mediaType = "NARRATION_TTS",
            mediaPath = "narrator://adventure_chronicler",
            personaId = "ADVENTURE_CHRONICLER",
            title = "Iron Across the Trestle: 1893 Falling Run Derailment",
            description = "High-priority narrative triggered when traversing the historic rail crossing ravine.",
            priority = 10,
            durationSec = 80,
            duckAmbientPercent = 0.15f
        ),

        // --- Scribner House (New Albany Founders, 1814) ---
        LoreMediaAssetEntity(
            id = "media_scribner_arrival_historian",
            siteId = "floyd_scribner_house",
            triggerTier = "ARRIVAL_100M",
            mediaType = "NARRATION_TTS",
            mediaPath = "narrator://historian",
            personaId = "HISTORIAN",
            title = "The Joel Scribner Hearth: 1814 Frontier Homestead",
            description = "Documented history of New Albany's oldest surviving hand-hewn ash and poplar residence.",
            priority = 9,
            durationSec = 60,
            duckAmbientPercent = 0.20f
        ),

        // --- Knobs Ridge Mystery (Floyd Knobs nocturnal stone cairns) ---
        LoreMediaAssetEntity(
            id = "media_knobs_ridge_arrival_wizard",
            siteId = "floyd_knobs_ridge_mystery",
            triggerTier = "ARRIVAL_100M",
            mediaType = "NARRATION_TTS",
            mediaPath = "narrator://wizard_narrator",
            personaId = "WIZARD_NARRATOR",
            title = "The Sentinel Cairns of the Knobstone Escarpment",
            description = "Arch-wizard invocation of anomalous sandstone formations and nocturnal lights along the ridge.",
            priority = 10,
            durationSec = 85,
            duckAmbientPercent = 0.15f
        )
    )
}
