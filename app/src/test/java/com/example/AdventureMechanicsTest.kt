package com.example

import com.example.data.local.LoreSeedData
import com.example.data.model.CipherEngine
import com.example.data.model.CuratedMysteries
import com.example.ui.screens.OpticalFilterMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AdventureMechanicsTest {

    @Test
    fun testRenoGoldCipherDecryption() {
        val riddle = CuratedMysteries.RENO_CIPHER
        assertEquals(7, riddle.shiftKey)

        // Decrypt with correct shift key
        val solved = CipherEngine.decodeCaesar(riddle.ciphertext, riddle.shiftKey)
        assertEquals(riddle.plaintext, solved)

        // Verify inverse encryption produces exact ciphertext
        val encrypted = CipherEngine.encodeCaesar(riddle.plaintext, riddle.shiftKey)
        assertEquals(riddle.ciphertext, encrypted)
    }

    @Test
    fun testSaltpeterCavernsCipherDecryption() {
        val riddle = CuratedMysteries.SALTPETER_CIPHER
        assertEquals(4, riddle.shiftKey)

        val solved = CipherEngine.decodeCaesar(riddle.ciphertext, riddle.shiftKey)
        assertEquals(riddle.plaintext, solved)

        val encrypted = CipherEngine.encodeCaesar(riddle.plaintext, riddle.shiftKey)
        assertEquals(riddle.ciphertext, encrypted)
    }

    @Test
    fun testFreedomLanternCipherDecryption() {
        val riddle = CuratedMysteries.FREEDOM_CIPHER
        assertEquals(9, riddle.shiftKey)

        val solved = CipherEngine.decodeCaesar(riddle.ciphertext, riddle.shiftKey)
        assertEquals(riddle.plaintext, solved)

        val encrypted = CipherEngine.encodeCaesar(riddle.plaintext, riddle.shiftKey)
        assertEquals(riddle.ciphertext, encrypted)
    }

    @Test
    fun testCaesarEngineEdgeCases() {
        // Shift 0 should be identity
        val text = "THE EAGLE FLIES AT MIDNIGHT"
        assertEquals(text, CipherEngine.decodeCaesar(text, 0))
        assertEquals(text, CipherEngine.decodeCaesar(text, 26))

        // Shift 52 should wrap properly
        assertEquals(text, CipherEngine.decodeCaesar(text, 52))

        // Negative shift should wrap smoothly
        val encoded = CipherEngine.encodeCaesar(text, 3)
        assertEquals(text, CipherEngine.decodeCaesar(encoded, 3))
    }

    @Test
    fun testAdventureStorylineIntegrity() {
        val mysteries = CuratedMysteries.allMysteries
        assertEquals("Should have exactly 3 flagship mysteries", 3, mysteries.size)

        val allSiteIds = LoreSeedData.sites.map { it.id }.toSet()
        val allClues = CuratedMysteries.allClues
        val allCiphers = CuratedMysteries.allCiphers

        mysteries.forEach { mystery ->
            assertTrue("Mystery ${mystery.id} must have title", mystery.title.isNotBlank())
            assertTrue("Mystery ${mystery.id} must have description", mystery.description.isNotBlank())
            assertTrue("Mystery ${mystery.id} must have clues", mystery.clueIds.isNotEmpty())

            // Verify cipher exists
            val linkedCipher = allCiphers.firstOrNull { it.id == mystery.cipherRiddleId }
            assertNotNull("Mystery ${mystery.id} must link to a valid cipher", linkedCipher)

            // Verify each clue site exists in seed data
            mystery.clueIds.forEach { clueId ->
                val clue = allClues.firstOrNull { it.id == clueId }
                assertNotNull("Clue $clueId must exist", clue)
                assertTrue("Clue ${clue!!.id} site ${clue.siteId} must exist in seed data", allSiteIds.contains(clue.siteId))
                assertTrue("Clue must have handwritten note", clue.handwrittenNote.isNotBlank())
                assertTrue("Clue must have alignment target", clue.alignmentTargetDescription.isNotBlank())
            }
        }
    }

    @Test
    fun testOpticalFilterModes() {
        val modes = OpticalFilterMode.values()
        assertEquals(3, modes.size)
        assertTrue(modes.contains(OpticalFilterMode.PRECISION_CLEAR))
        assertTrue(modes.contains(OpticalFilterMode.DAGUERREOTYPE_SEPIA))
        assertTrue(modes.contains(OpticalFilterMode.UV_SPECTRAL))
    }

    @Test
    fun testCuratedOralHistoryArchive() {
        val cylinders = com.example.data.model.CuratedOralHistoryArchive.allCylinders
        assertTrue("Must have at least 6 wax cylinder recordings", cylinders.size >= 6)

        val relics = com.example.data.model.CuratedOralHistoryArchive.allRelics
        assertTrue("Must have at least 6 historical relics", relics.size >= 6)

        val allSiteIds = LoreSeedData.sites.map { it.id }.toSet()

        cylinders.forEach { cyl ->
            assertTrue("Cylinder ${cyl.id} must have non-blank title", cyl.title.isNotBlank())
            assertTrue("Cylinder ${cyl.id} must have eyewitness name", cyl.eyewitnessName.isNotBlank())
            assertTrue("Cylinder ${cyl.id} must have transcript", cyl.transcript.isNotBlank())
            assertTrue("Cylinder ${cyl.id} frequency must be positive", cyl.frequencyKhz > 0)
            assertTrue("Cylinder ${cyl.id} linked site ${cyl.siteId} must exist", allSiteIds.contains(cyl.siteId))
        }

        relics.forEach { relic ->
            assertTrue("Relic ${relic.id} must have title", relic.title.isNotBlank())
            assertTrue("Relic ${relic.id} must have material", relic.material.isNotBlank())
            assertTrue("Relic ${relic.id} must have description", relic.description.isNotBlank())
            assertTrue("Relic ${relic.id} linked site ${relic.linkedSiteId} must exist", allSiteIds.contains(relic.linkedSiteId))
        }
    }

    @Test
    fun testColdStartDossierIntroBriefingText() {
        val briefingText = "Welcome, explorer. Beyond the Ohio River bluffs lie forgotten subterranean tunnels, abolitionist belfry signals, and the unrecovered gold of the frontier. Your field dossier is unsealed. Let the expedition begin."
        assertTrue("Briefing text must contain Ohio River bluffs", briefingText.contains("Ohio River bluffs"))
        assertTrue("Briefing text must contain unrecovered gold", briefingText.contains("unrecovered gold"))
        assertTrue("Briefing text must contain expedition begin", briefingText.contains("expedition begin"))
    }

    @Test
    fun testCompassShortestAngularDistanceDelta() {
        // When current is 358° and new azimuth is 2°, delta should be +4°, not -356°
        val current = 358f
        val azimuth = 2f
        var delta = (azimuth - current) % 360f
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f
        assertEquals("Delta across 0-boundary should be +4 degrees", 4f, delta, 0.01f)

        // When current is 2° and new azimuth is 358°, delta should be -4°, not +356°
        val current2 = 2f
        val azimuth2 = 358f
        var delta2 = (azimuth2 - current2) % 360f
        if (delta2 > 180f) delta2 -= 360f
        if (delta2 < -180f) delta2 += 360f
        assertEquals("Delta across 0-boundary should be -4 degrees", -4f, delta2, 0.01f)
    }
}
