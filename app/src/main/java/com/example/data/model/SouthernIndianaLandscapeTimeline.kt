package com.example.data.model

/**
 * Historical features and landscape evolution records specifically tied to geographic coordinates
 * and historical years for the "What Changed Here?" inquiry engine.
 *
 * Data classification:
 * - isDemonstrationData: Distinguishes prototype curated demonstration records from fully verified archive ingestions.
 * - historicalConfidence: Rigor of documentary evidence.
 * - locationConfidence: Spatial accuracy of the site coordinate.
 */
data class HistoricalLandscapeFeature(
    val id: String,
    val title: String,
    val yearStart: Int,
    val yearEnd: Int,
    val displayEra: String,
    val latitude: Double,
    val longitude: Double,
    val category: String, // LOST_LANDSCAPE, RAILROAD, INDUSTRY, CIVIC, FLOOD
    val whatStoodHere: String,
    val whatChanged: String,
    val whatExistsToday: String,
    val primaryMapSource: String,
    val alignmentConfidence: MapAlignmentConfidence,
    val historicalConfidence: ConfidenceLevel = ConfidenceLevel.VERIFIED,
    val locationConfidence: ConfidenceLevel = ConfidenceLevel.VERIFIED,
    val isDemonstrationData: Boolean = false,
    val relatedSiteId: String? = null
)

object SouthernIndianaLandscapeTimeline {

    val features: List<HistoricalLandscapeFeature> = listOf(
        HistoricalLandscapeFeature(
            id = "feat_monon_cut",
            title = "Monon Railroad Knobs Grade & Trestle",
            yearStart = 1882,
            yearEnd = 1971,
            displayEra = "1882–1971",
            latitude = 38.2932,
            longitude = -85.8340,
            category = "LOST_LANDSCAPE",
            whatStoodHere = "Deep cut through Knobstone siltstone and high timber trestle carrying Louisville, New Albany & Chicago trains.",
            whatChanged = "Flooding, bridge collapses, and route consolidations in 1971 ended service. Rails pulled up in 1980s.",
            whatExistsToday = "Wooded ravine, exposed bedrock cliff cut, and abandoned stone abutments visible from State Street.",
            primaryMapSource = "USGS 1:24,000 New Albany Quadrangle (1950/1955) & 1905 Sanborn Map",
            alignmentConfidence = MapAlignmentConfidence.SURVEY_GRADE,
            historicalConfidence = ConfidenceLevel.VERIFIED,
            locationConfidence = ConfidenceLevel.VERIFIED,
            isDemonstrationData = false,
            relatedSiteId = "falling_run_trestle"
        ),
        HistoricalLandscapeFeature(
            id = "feat_scribner_wharf",
            title = "Packet Boat Landing & High Street Shipyards",
            yearStart = 1813,
            yearEnd = 1910,
            displayEra = "1813–1910",
            latitude = 38.2818,
            longitude = -85.8195,
            category = "LOST_LANDSCAPE",
            whatStoodHere = "Active steamboat landing, wooden hull shipyards, grain warehouses, and ferry slip to Portland, KY.",
            whatChanged = "Steam river trade eclipsed by rail; riverside destroyed by 1937 flood; US Army Corps erected earthen floodwall.",
            whatExistsToday = "Riverfront concrete floodwall, modern amphitheater, and Greenway trail.",
            primaryMapSource = "1886 Sanborn Fire Insurance Map (Sheet 3)",
            alignmentConfidence = MapAlignmentConfidence.GOOD,
            historicalConfidence = ConfidenceLevel.VERIFIED,
            locationConfidence = ConfidenceLevel.VERIFIED,
            isDemonstrationData = false,
            relatedSiteId = "scribner_house"
        ),
        HistoricalLandscapeFeature(
            id = "feat_rose_island",
            title = "Rose Island Summer Resort & Zoo",
            yearStart = 1923,
            yearEnd = 1937,
            displayEra = "1923–1937",
            latitude = 38.4239,
            longitude = -85.6268,
            category = "LOST_LANDSCAPE",
            whatStoodHere = "Thronged amusement park with wooden roller coaster ('The Devil's Backbone'), Olympic pool, hotel, zoo, and ferry landing.",
            whatChanged = "Submerged under 15 feet of mud and debris during the 1937 Ohio River flood. Abandoned completely.",
            whatExistsToday = "Eerie stone archways, drained swimming pool outline, and overgrown concrete foundations in Charlestown State Park.",
            primaryMapSource = "USGS WSP 838 Flood Report & Charlestown State Park Trail Records",
            alignmentConfidence = MapAlignmentConfidence.SURVEY_GRADE,
            historicalConfidence = ConfidenceLevel.VERIFIED,
            locationConfidence = ConfidenceLevel.VERIFIED,
            isDemonstrationData = false,
            relatedSiteId = "rose_island_ruins"
        ),
        HistoricalLandscapeFeature(
            id = "feat_plate_glass_works",
            title = "W.C. DePauw American Plate Glass Works",
            yearStart = 1869,
            yearEnd = 1903,
            displayEra = "1869–1903",
            latitude = 38.2860,
            longitude = -85.8280,
            category = "INDUSTRY",
            whatStoodHere = "Largest plate glass manufacturing facility in North America, employing over 1,200 craftsmen and glassblowers.",
            whatChanged = "Natural gas depletion in Indiana and rise of Pittsburgh plate glass trust forced closure in 1903. Razed.",
            whatExistsToday = "Commercial street grid, secondary warehouses, and residential parking.",
            primaryMapSource = "1886 Sanborn Map Sheet 7 & 1905 Sanborn Sheet 14",
            alignmentConfidence = MapAlignmentConfidence.GOOD,
            historicalConfidence = ConfidenceLevel.VERIFIED,
            locationConfidence = ConfidenceLevel.PLAUSIBLE,
            isDemonstrationData = false,
            relatedSiteId = "depauw_glass_site"
        ),
        HistoricalLandscapeFeature(
            id = "feat_town_clock_freedom",
            title = "Second Presbyterian 'Town Clock Church' Underground Railroad Station",
            yearStart = 1852,
            yearEnd = 2026,
            displayEra = "1852–Present",
            latitude = 38.2847,
            longitude = -85.8239,
            category = "CIVIC",
            whatStoodHere = "160-foot steeple clock church serving as beacon for freedom seekers crossing Ohio River from enslaved Kentucky.",
            whatChanged = "Survives today! Steeple damaged in 1915 windstorm, rebuilt in 2016 through community restoration.",
            whatExistsToday = "Second Baptist Church / Historic Town Clock Church; active congregation and Underground Railroad sanctuary.",
            primaryMapSource = "1886 Sanborn Map Sheet 2 & USGS 1:24,000 New Albany Quad",
            alignmentConfidence = MapAlignmentConfidence.SURVEY_GRADE,
            historicalConfidence = ConfidenceLevel.VERIFIED,
            locationConfidence = ConfidenceLevel.VERIFIED,
            isDemonstrationData = false,
            relatedSiteId = "town_clock_church"
        )
    )
}
