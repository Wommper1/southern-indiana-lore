package com.example

import com.example.data.model.CharcoalPigment
import com.example.data.model.RelicRegistry
import com.example.data.model.RubbingPaperType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RelicLabExaminationTest {

    @Test
    fun testAllCuratedSpecimensPresentAndValid() {
        val specimens = RelicRegistry.allSpecimens
        assertEquals("Should have 8 authentic Southern Indiana relics", 8, specimens.size)

        specimens.forEach { specimen ->
            assertNotNull(specimen.id)
            assertFalse("Title must not be empty", specimen.title.isBlank())
            assertFalse("Era must not be empty", specimen.era.isBlank())
            assertFalse("Material must not be empty", specimen.material.isBlank())
            assertFalse("Provenance must not be empty", specimen.provenance.isBlank())
            assertTrue("Radio frequency should be in AM band (540 - 1600 kHz)", specimen.archivalFrequencyKhz in 540..1600)
            assertFalse("Archival transcript must not be empty", specimen.archivalDispatchTranscript.isBlank())
            assertFalse("Speaker name must not be blank", specimen.speakerName.isBlank())
            assertTrue("Must contain at least 2 relief features for tactile rubbing", specimen.reliefFeatures.size >= 2)
            assertTrue("Must contain inscriptions", specimen.inscriptions.isNotEmpty())
        }
    }

    @Test
    fun testRelicRegistryLookup() {
        val crinoid = RelicRegistry.getById("relic_fossil_horn_coral")
        assertEquals("Devonian Crinoid Stem & Horn Coral", crinoid.title)
        assertEquals(1040, crinoid.archivalFrequencyKhz)

        val steamboat = RelicRegistry.getById("relic_steamboat_gold_token")
        assertEquals("1853 Steamboat Eclipse Gilded Medallion", steamboat.title)
        assertEquals(1120, steamboat.archivalFrequencyKhz)

        val fallback = RelicRegistry.getById("non_existent_relic_id")
        assertNotNull(fallback)
    }

    @Test
    fun testCharcoalPigmentsAndPaperTypes() {
        val pigments = CharcoalPigment.values()
        assertTrue("Should have multiple charcoal pigment choices", pigments.size >= 4)
        pigments.forEach {
            assertNotNull(it.displayName)
            assertNotNull(it.color)
        }

        val papers = RubbingPaperType.values()
        assertTrue("Should have multiple vellum paper stocks", papers.size >= 3)
        papers.forEach {
            assertNotNull(it.displayName)
            assertNotNull(it.tintColor)
        }
    }
}
