package com.example

import com.example.data.model.ConfidenceLevel
import com.example.data.model.LoreSite
import com.example.util.ExpeditionDossierExporter
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpeditionDossierExporterTest {

    private fun createSampleSite(
        id: String = "test_culbertson",
        title: String = "Culbertson Mansion",
        county: String = "Floyd County",
        lat: Double = 38.2861,
        lng: Double = -85.8239,
        isVisited: Boolean = true,
        userNotes: String = "Noticed cold draft near ballroom stairs."
    ): LoreSite {
        return LoreSite(
            id = id,
            title = title,
            subtitle = "Victorian Mansion & Folklore",
            category = "HAUNTED_HISTORY",
            county = county,
            latitude = lat,
            longitude = lng,
            address = "914 E Main St, New Albany, IN",
            era = "Gilded Age (1867)",
            accessStatus = "State Historic Site",
            fullStory = "Built by William S. Culbertson.",
            verifiedHistory = "Completed in 1869 with 25 rooms.",
            folkloreTradition = "Local lore recounts carriage sounds and cold spots.",
            confidenceLevel = ConfidenceLevel.VERIFIED,
            sources = "Indiana State Museum and Historic Sites",
            sourceNotes = "HABS IN-104",
            verificationGrade = "A",
            geofenceRadiusMeters = 150,
            audioNarration = "William Culbertson was once Indiana's wealthiest man.",
            isVisited = isVisited,
            userNotes = userNotes,
            lastVisitedTimestamp = 1700000000000L,
            encounterCount = 2,
            driveStory = "Approaching Culbertson Victorian estate."
        )
    }

    @Test
    fun testGenerateMarkdownDossier_containsCoreHeadersAndData() {
        val site = createSampleSite()
        val dossier = ExpeditionDossierExporter.generateMarkdownDossier(
            sites = listOf(site),
            title = "Test Expedition Dossier"
        )

        assertNotNull(dossier)
        assertTrue(dossier.contains("# Test Expedition Dossier"))
        assertTrue(dossier.contains("Alex Shade Productions"))
        assertTrue(dossier.contains("Culbertson Mansion"))
        assertTrue(dossier.contains("Floyd County"))
        assertTrue(dossier.contains("38.28610, -85.82390"))
        assertTrue(dossier.contains("Noticed cold draft near ballroom stairs."))
        assertTrue(dossier.contains("HABS IN-104"))
        assertTrue(dossier.contains("Total Mapped POIs**: 1"))
    }

    @Test
    fun testGenerateGpxTrack_producesValidGpxXml() {
        val site1 = createSampleSite(id = "s1", title = "Culbertson Mansion", lat = 38.2861, lng = -85.8239)
        val site2 = createSampleSite(
            id = "s2",
            title = "Squire Boone Caverns & Mill",
            county = "Harrison County",
            lat = 38.0042,
            lng = -86.1383,
            isVisited = false,
            userNotes = ""
        )

        val gpx = ExpeditionDossierExporter.generateGpxTrack(
            sites = listOf(site1, site2),
            trackName = "Harrison & Floyd Lore Route"
        )

        assertNotNull(gpx)
        assertTrue(gpx.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"))
        assertTrue(gpx.contains("""<gpx version="1.1""""))
        assertTrue(gpx.contains("<name>Harrison &amp; Floyd Lore Route</name>"))
        assertTrue(gpx.contains("""<wpt lat="38.286100" lon="-85.823900">"""))
        assertTrue(gpx.contains("<name>Culbertson Mansion</name>"))
        assertTrue(gpx.contains("""<wpt lat="38.004200" lon="-86.138300">"""))
        assertTrue(gpx.contains("<name>Squire Boone Caverns &amp; Mill</name>"))
        assertTrue(gpx.endsWith("</gpx>\n"))
    }
}
