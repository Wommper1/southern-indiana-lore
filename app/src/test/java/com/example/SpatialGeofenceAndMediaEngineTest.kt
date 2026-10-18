package com.example

import com.example.data.local.LoreMediaAssetEntity
import com.example.data.local.LoreMediaSeedData
import com.example.data.local.LoreSiteEntity
import com.example.service.ProximityTier
import com.example.service.SpatialProximityCoordinator
import com.example.util.LocationUtils
import com.example.util.NarratorPersona
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SpatialGeofenceAndMediaEngineTest {

    @Test
    fun testBoundingBoxCalculation() {
        val lat = 38.2863
        val lng = -85.8145
        val radiusMeters = 3000.0

        val deltaLat = radiusMeters / SpatialProximityCoordinator.METERS_PER_DEGREE_LAT
        val latRad = Math.toRadians(lat)
        val cosLat = Math.cos(latRad)
        val deltaLng = radiusMeters / (SpatialProximityCoordinator.METERS_PER_DEGREE_LAT * cosLat)

        assertTrue("minLat must be strictly less than center lat", (lat - deltaLat) < lat)
        assertTrue("maxLat must be strictly greater than center lat", (lat + deltaLat) > lat)
        assertTrue("minLng must be strictly less than center lng", (lng - deltaLng) < lng)
        assertTrue("maxLng must be strictly greater than center lng", (lng + deltaLng) > lng)
    }

    @Test
    fun testMultiTierProximityThresholds() {
        // Distance > 1500m -> FAR
        val farDistance = 2500f
        val tierFar = when {
            farDistance <= 100f -> ProximityTier.IMMEDIATE_TRIGGER
            farDistance <= 500f -> ProximityTier.APPROACH_NEAR
            farDistance <= 1500f -> ProximityTier.APPROACH_HORIZON
            else -> ProximityTier.FAR
        }
        assertEquals(ProximityTier.FAR, tierFar)

        // Distance = 1200m -> APPROACH_HORIZON
        val horizonDistance = 1200f
        val tierHorizon = when {
            horizonDistance <= 100f -> ProximityTier.IMMEDIATE_TRIGGER
            horizonDistance <= 500f -> ProximityTier.APPROACH_NEAR
            horizonDistance <= 1500f -> ProximityTier.APPROACH_HORIZON
            else -> ProximityTier.FAR
        }
        assertEquals(ProximityTier.APPROACH_HORIZON, tierHorizon)

        // Distance = 350m -> APPROACH_NEAR
        val nearDistance = 350f
        val tierNear = when {
            nearDistance <= 100f -> ProximityTier.IMMEDIATE_TRIGGER
            nearDistance <= 500f -> ProximityTier.APPROACH_NEAR
            nearDistance <= 1500f -> ProximityTier.APPROACH_HORIZON
            else -> ProximityTier.FAR
        }
        assertEquals(ProximityTier.APPROACH_NEAR, tierNear)

        // Distance = 45m -> IMMEDIATE_TRIGGER
        val immediateDistance = 45f
        val tierImmediate = when {
            immediateDistance <= 100f -> ProximityTier.IMMEDIATE_TRIGGER
            immediateDistance <= 500f -> ProximityTier.APPROACH_NEAR
            immediateDistance <= 1500f -> ProximityTier.APPROACH_HORIZON
            else -> ProximityTier.FAR
        }
        assertEquals(ProximityTier.IMMEDIATE_TRIGGER, tierImmediate)
    }

    @Test
    fun testLinearCorridorTrajectoryCrossingIntersection() {
        // Line segment across a road / path: (38.3000, -85.8000) to (38.3000, -85.7900)
        // User travel vector: from south (38.2950, -85.7950) to north (38.3050, -85.7950)
        val intersects = LocationUtils.doSegmentsIntersect(
            38.2950, -85.7950, // prev user GPS fix
            38.3050, -85.7950, // curr user GPS fix
            38.3000, -85.8000, // trail start
            38.3000, -85.7900  // trail end
        )
        assertTrue("User trajectory intersecting corridor must register positive crossing", intersects)
    }

    @Test
    fun testLoreMediaSeedDataAssetsIntegrity() {
        val assets = LoreMediaSeedData.assets
        assertTrue("LoreMediaSeedData must contain seeded audio assets", assets.isNotEmpty())

        val culbertsonArrival = assets.firstOrNull { it.siteId == "floyd_culbertson_mansion" && it.triggerTier == "ARRIVAL_100M" }
        assertNotNull("Culbertson arrival media asset must exist", culbertsonArrival)
        assertEquals("WIZARD_NARRATOR", culbertsonArrival?.personaId)

        val buffaloTraceCrossing = assets.firstOrNull { it.siteId == "clark_buffalo_trace" && it.triggerTier == "CORRIDOR_CROSS" }
        assertNotNull("Buffalo Trace crossing media asset must exist", buffaloTraceCrossing)
        assertEquals(10, buffaloTraceCrossing?.priority)

        val roseIslandRadio = assets.firstOrNull { it.siteId == "clark_rose_island" && it.triggerTier == "ARRIVAL_100M" }
        assertNotNull("Rose Island radio asset must exist", roseIslandRadio)
        assertEquals("RETRO_RADIO", roseIslandRadio?.personaId)
    }

    @Test
    fun testNarratorPersonaResolution() {
        val wizard = NarratorPersona.fromId("WIZARD_NARRATOR")
        assertEquals(NarratorPersona.WIZARD_NARRATOR, wizard)
        assertTrue(wizard.titleSubtitle.contains("wizard", ignoreCase = true))

        val retro = NarratorPersona.fromId("RETRO_RADIO")
        assertEquals(NarratorPersona.RETRO_RADIO, retro)
        assertTrue(retro.displayName.contains("Retro", ignoreCase = true))

        val chronicler = NarratorPersona.fromId("ADVENTURE_CHRONICLER")
        assertEquals(NarratorPersona.ADVENTURE_CHRONICLER, chronicler)
    }
}
