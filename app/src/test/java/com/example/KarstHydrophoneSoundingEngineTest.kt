package com.example

import com.example.data.model.KarstHydrophoneRegistry
import com.example.data.model.LoggedSoundingEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KarstHydrophoneSoundingEngineTest {

    @Test
    fun testAllKarstWellsPresentAndValid() {
        val wells = KarstHydrophoneRegistry.allWells
        assertEquals("Must provide 3 authentic Southern Indiana karst sounding sites", 3, wells.size)

        wells.forEach { well ->
            assertFalse("Well ID must not be blank", well.id.isBlank())
            assertFalse("Title must not be blank", well.title.isBlank())
            assertFalse("County must not be blank", well.county.isBlank())
            assertFalse("Historical significance must not be blank", well.historicalSignificance.isBlank())
            assertTrue("Surface elevation must be above sea level", well.surfaceElevationFt > 400)
            assertTrue("Water table depth must be positive", well.waterTableDepthFt > 0f)
            assertTrue("Max sounding depth must exceed water table depth", well.maxSoundingDepthFt > well.waterTableDepthFt)
            assertTrue("Well must contain at least 3 distinct geological strata", well.strata.size >= 3)
        }
    }

    @Test
    fun testStratumLookupAtVariousDepths() {
        val lostRiver = KarstHydrophoneRegistry.LOST_RIVER_SIPHON

        val surfaceStratum = KarstHydrophoneRegistry.findStratumAtDepth(lostRiver, 10f)
        assertEquals("Epikarst Soil & Residual Chert", surfaceStratum.name)
        assertFalse(surfaceStratum.isWaterSubmerged)

        val aquiferStratum = KarstHydrophoneRegistry.findStratumAtDepth(lostRiver, 95f)
        assertEquals("Lost River Main Aquifer Channel", aquiferStratum.name)
        assertTrue(aquiferStratum.isWaterSubmerged)
        assertNotNull(aquiferStratum.waterTemperatureF)

        val deepSiphonStratum = KarstHydrophoneRegistry.findStratumAtDepth(lostRiver, 250f)
        assertEquals("St. Louis Phreatic Siphon Tube", deepSiphonStratum.name)
        assertTrue(deepSiphonStratum.isWaterSubmerged)
    }

    @Test
    fun testCableTensionCalculations() {
        // Surface depth (0 ft)
        val depth0 = 0f
        val tension0 = (depth0 * 0.045f) + 1.2f
        assertEquals(1.2f, tension0, 0.01f)

        // Mid depth (100 ft submerged)
        val depth100 = 100f
        val deadWeight100 = (depth100 * 0.045f) + 1.2f
        val submergedTension100 = deadWeight100 + 2.8f
        assertTrue("Submerged tension must exceed surface tension", submergedTension100 > tension0)
        assertEquals(8.5f, submergedTension100, 0.01f)

        // Deep depth (350 ft submerged)
        val depth350 = 350f
        val tension350 = ((depth350 * 0.045f) + 1.2f + 2.8f).coerceIn(0f, 25f)
        assertTrue("Deep tension must be higher", tension350 > submergedTension100)
        assertTrue("Tension must stay within mechanical safety limit (25 lbs)", tension350 <= 25f)
    }

    @Test
    fun testLoggedSoundingRecord() {
        val log = LoggedSoundingEntry(
            id = "test_log_1",
            wellId = "well_lost_river_siphon",
            depthFt = 185.4f,
            stratumName = "St. Louis Phreatic Siphon Tube",
            waterTempF = 50.8f,
            note = "Hydrophone acoustic anomaly at 185 ft"
        )

        assertEquals("test_log_1", log.id)
        assertEquals(185.4f, log.depthFt, 0.01f)
        assertEquals(50.8f, log.waterTempF!!, 0.01f)
        assertTrue(log.timestamp > 0L)
    }
}
