package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.LoreSeedData
import com.example.data.model.LoreCounties
import com.example.data.pack.LorePackManager
import com.example.data.pack.PackValidationStatus
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HistoricalResearchIntegrationTest {

    @Test
    fun verifyLoreSeedDataSitesAndCounties() {
        val seedSites = LoreSeedData.sites
        assertTrue("Seed data should contain at least 31 historical sites", seedSites.size >= 31)

        val siteIds = seedSites.map { it.id }.toSet()
        val expectedNewIds = listOf(
            "rose_island_resort",
            "marshfield_train_robbery",
            "lake_iola_interurban",
            "freeman_field_mutiny",
            "cannelton_cotton_mill",
            "uss_argosy_disaster",
            "new_harmony_historic_district",
            "evansville_p47_factory",
            "evansville_lincoln_gardens",
            "vincennes_fort_sackville",
            "washington_becks_mill",
            "washington_salem_underground_railroad",
            "dubois_st_meinrad_archabbey",
            "dubois_jasper_patoka_train_depot",
            "perry_rome_courthouse",
            "perry_tell_city_swiss_enclave",
            "jefferson_madison_incline_cut",
            "jefferson_eleutherian_college"
        )

        for (expectedId in expectedNewIds) {
            assertTrue("Expected site $expectedId to be present in LoreSeedData", siteIds.contains(expectedId))
        }

        // Verify counties coverage
        val expectedCounties = listOf(
            "Clark County", "Floyd County", "Harrison County", "Crawford County", "Lawrence County",
            "Jefferson County", "Perry County", "Spencer County", "Vanderburgh County", "Posey County",
            "Knox County", "Scott County", "Jackson County", "Dubois County", "Washington County"
        )
        for (county in expectedCounties) {
            assertTrue(
                "Expected LoreCounties to contain $county",
                LoreCounties.list.contains(county)
            )
        }
    }

    @Test
    fun verifyPackManifestAndSitesValidation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val result = LorePackManager.validatePack(context)

        assertEquals(
            "Pack validation should succeed with VALID status: ${result.errors.joinToString()}",
            PackValidationStatus.VALID,
            result.status
        )
        assertTrue("Pack should have at least 52 sites", result.siteCount >= 52)
        assertEquals("Pack manifest siteCount should match parsed sites count", result.siteCount, result.parsedSiteCount)

        val parsedIds = result.validatedSites.map { it.id }.toSet()
        assertTrue("Pack must include Rose Island", parsedIds.contains("rose_island_resort"))
        assertTrue("Pack must include Marshfield Robbery", parsedIds.contains("marshfield_train_robbery"))
        assertTrue("Pack must include New Harmony", parsedIds.contains("new_harmony_historic_district"))
        assertTrue("Pack must include Fort Sackville", parsedIds.contains("vincennes_fort_sackville"))
        assertTrue("Pack must include Freeman Field", parsedIds.contains("freeman_field_mutiny"))
        assertTrue("Pack must include Cannelton Cotton Mill", parsedIds.contains("cannelton_cotton_mill"))
        assertTrue("Pack must include P-47 Factory", parsedIds.contains("evansville_p47_factory"))
        assertTrue("Pack must include Lincoln Gardens", parsedIds.contains("evansville_lincoln_gardens"))
        assertTrue("Pack must include Beck's Mill", parsedIds.contains("washington_becks_mill"))
        assertTrue("Pack must include Salem Underground Railroad", parsedIds.contains("washington_salem_underground_railroad"))
        assertTrue("Pack must include Saint Meinrad Archabbey", parsedIds.contains("dubois_st_meinrad_archabbey"))
        assertTrue("Pack must include Jasper Train Depot", parsedIds.contains("dubois_jasper_patoka_train_depot"))
        assertTrue("Pack must include Rome Courthouse", parsedIds.contains("perry_rome_courthouse"))
        assertTrue("Pack must include Tell City Swiss Enclave", parsedIds.contains("perry_tell_city_swiss_enclave"))
        assertTrue("Pack must include Madison Incline Cut", parsedIds.contains("jefferson_madison_incline_cut"))
        assertTrue("Pack must include Eleutherian College", parsedIds.contains("jefferson_eleutherian_college"))
    }

    @Test
    fun verifySubterraneanAndBridgeCrossingCountdownTaxonomy() {
        val seedSites = LoreSeedData.sites

        val subterraneanSites = seedSites.filter {
            it.radarType.equals("UNDERGROUND", ignoreCase = true) ||
            it.whatMayBeUnderground.isNotBlank()
        }
        assertTrue("Should have at least 10 subterranean void sites", subterraneanSites.size >= 10)

        val bridgeSites = seedSites.filter {
            it.radarType.equals("BRIDGE", ignoreCase = true) ||
            it.title.contains("Bridge", ignoreCase = true) ||
            it.title.contains("Trestle", ignoreCase = true)
        }
        assertTrue("Should have at least 7 historic bridge sites", bridgeSites.size >= 7)

        // Verify Madison Incline has subterranean underground radar type
        val madisonIncline = seedSites.firstOrNull { it.id == "jefferson_madison_incline_cut" }
        assertNotNull(madisonIncline)
        assertEquals("UNDERGROUND", madisonIncline?.radarType)

        // Verify Jasper Depot Patoka span has bridge radar type
        val jasperDepot = seedSites.firstOrNull { it.id == "dubois_jasper_patoka_train_depot" }
        assertNotNull(jasperDepot)
        assertEquals("BRIDGE", jasperDepot?.radarType)
    }
}
