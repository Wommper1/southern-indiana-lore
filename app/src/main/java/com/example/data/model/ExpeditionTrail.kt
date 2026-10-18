package com.example.data.model

data class ExpeditionTrail(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val thematicEra: String,
    val orderedSiteIds: List<String>,
    val estimatedDistanceKm: Int,
    val badgeId: String,
    val badgeTitle: String,
    val badgeDescription: String,
    val themeColorHex: Long = 0xFFD4AF37
)

data class ExplorerBadge(
    val id: String,
    val title: String,
    val description: String,
    val trailId: String,
    val isUnlocked: Boolean,
    val visitedCount: Int,
    val totalCount: Int,
    val progressPercent: Int
)

data class TrailProgress(
    val trail: ExpeditionTrail,
    val visitedCount: Int,
    val totalCount: Int,
    val progressPercent: Int,
    val isCompleted: Boolean,
    val badge: ExplorerBadge
)

object CuratedExpeditionTrails {

    val FREEDOM_CORRIDOR = ExpeditionTrail(
        id = "freedom_corridor",
        title = "Freedom Seeker Underground Railroad Trail",
        subtitle = "Ohio River Crossings & Quaker Abolitionist Sanctuaries",
        description = "Trace the clandestine paths of freedom seekers escaping enslavement across the Ohio River. Journey from New Albany's sentinel Town Clock Church through Salem's Quaker network to the heroic integrated classrooms of Eleutherian College.",
        thematicEra = "1830–1865 Antebellum & Abolition Era",
        orderedSiteIds = listOf(
            "floyd_town_clock_church",
            "washington_salem_underground_railroad",
            "jefferson_eleutherian_college",
            "evansville_lincoln_gardens"
        ),
        estimatedDistanceKm = 145,
        badgeId = "badge_freedom_conductor",
        badgeTitle = "Freedom Conductor",
        badgeDescription = "Awarded for tracing all verified Underground Railroad sanctuaries and abolition landmarks across Southern Indiana.",
        themeColorHex = 0xFFD4AF37 // Gilded Gold
    )

    val KARST_CAVERNS = ExpeditionTrail(
        id = "karst_caverns",
        title = "Subterranean Karst & Deep Cavern Odyssey",
        subtitle = "Underground Rivers, Saltpeter Caves & Karst Sinks",
        description = "Descend into the limestone underbelly of Southern Indiana. Explore historic show caves, subterranean lost rivers, 1812 saltpeter powder mills, and natural springs carved out of Mississippian bedrock.",
        thematicEra = "Geological Deep Time & Pioneer Industry",
        orderedSiteIds = listOf(
            "harrison_squire_boone_caverns",
            "crawford_marengo_cave",
            "crawford_wyandotte_marengo_karst",
            "crawford_wyandotte_saltpeter",
            "lawrence_bluespring_caverns",
            "lawrence_spring_mill_twin_caves",
            "orange_lost_river_system",
            "washington_becks_mill"
        ),
        estimatedDistanceKm = 190,
        badgeId = "badge_karst_cartographer",
        badgeTitle = "Karst Cartographer",
        badgeDescription = "Awarded for exploring all 8 major subterranean limestone voids, lost river sinks, and historic caverns.",
        themeColorHex = 0xFF10B981 // Knobstone Forest
    )

    val RIVER_AND_RAIL = ExpeditionTrail(
        id = "river_and_rail",
        title = "Riverboat & Iron Rail Industrial Frontier",
        subtitle = "Ohio River Landings, Mountain Incline Cuts & Legendary Trestles",
        description = "Experience the titanic engineering that conquered the frontier: the 5.89% grade Madison Incline rail cut, Cannelton's sandstone cotton fortress, the monumental Tulip Viaduct, and the Reno Gang train robbery site.",
        thematicEra = "1836–1910 Industrial Steam & Riverboat Era",
        orderedSiteIds = listOf(
            "clark_howard_steamboat_museum",
            "clark_big_four_bridge",
            "jefferson_madison_incline_cut",
            "cannelton_cotton_mill",
            "dubois_jasper_patoka_train_depot",
            "greene_tulip_trestle",
            "marshfield_train_robbery",
            "floyd_duncan_tunnel",
            "perry_rome_courthouse",
            "perry_tell_city_swiss_enclave"
        ),
        estimatedDistanceKm = 260,
        badgeId = "badge_river_rail_pilot",
        badgeTitle = "River & Rail Pilot",
        badgeDescription = "Awarded for navigating the legendary Ohio River boat wharves, rail cuts, and timber trestles.",
        themeColorHex = 0xFF38BDF8 // River Mist Blue
    )

    val PIONEER_FOUNDERS = ExpeditionTrail(
        id = "pioneer_founders",
        title = "Pioneer Founders & Territorial Borderlands",
        subtitle = "First Capitols, Utopian Colonies & Ancient Bison Corridors",
        description = "Travel the footsteps of early pioneers, territorial governors, and utopian dreamers. From Indiana's first state capitol in Corydon and Clark's territorial post to the mystical Harmonist Labyrinth in New Harmony.",
        thematicEra = "1779–1850 Early Statehood & Frontier Settlements",
        orderedSiteIds = listOf(
            "harrison_corydon_capitol",
            "vincennes_fort_sackville",
            "new_harmony_historic_district",
            "indiana_buffalo_trace_corridor",
            "clark_george_rogers_clark_cabin",
            "floyd_scribner_house",
            "martin_hindostan_falls_ghost_town",
            "dubois_st_meinrad_archabbey"
        ),
        estimatedDistanceKm = 240,
        badgeId = "badge_territorial_archivist",
        badgeTitle = "Territorial Archivist",
        badgeDescription = "Awarded for visiting the founding capitols, frontier forts, and early pioneer sanctuaries of Indiana.",
        themeColorHex = 0xFFF59E0B // Lantern Amber
    )

    val allTrails: List<ExpeditionTrail> = listOf(
        FREEDOM_CORRIDOR,
        KARST_CAVERNS,
        RIVER_AND_RAIL,
        PIONEER_FOUNDERS
    )

    fun calculateProgress(trail: ExpeditionTrail, visitedSiteIds: Set<String>): TrailProgress {
        val total = trail.orderedSiteIds.size
        val visited = trail.orderedSiteIds.count { visitedSiteIds.contains(it) }
        val percent = if (total > 0) ((visited.toFloat() / total) * 100).toInt() else 0
        val isCompleted = visited == total && total > 0

        val badge = ExplorerBadge(
            id = trail.badgeId,
            title = trail.badgeTitle,
            description = trail.badgeDescription,
            trailId = trail.id,
            isUnlocked = isCompleted,
            visitedCount = visited,
            totalCount = total,
            progressPercent = percent
        )

        return TrailProgress(
            trail = trail,
            visitedCount = visited,
            totalCount = total,
            progressPercent = percent,
            isCompleted = isCompleted,
            badge = badge
        )
    }
}
