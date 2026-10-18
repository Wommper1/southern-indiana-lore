package com.example

import com.example.data.local.LoreSeedData
import com.example.data.model.CuratedExpeditionTrails
import com.example.data.model.LoreTome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AthenaeumLibraryAndPassportTest {

    @Test
    fun testAllSitesMapToValidLoreTomes() {
        val allSites = LoreSeedData.sites
        assertTrue("Seed data must contain all 52 sites", allSites.size >= 52)

        val tomes = allSites.mapIndexed { index, site ->
            LoreTome.fromSite(site, index)
        }

        assertEquals(allSites.size, tomes.size)

        // Verify roman numerals are sequential and valid
        assertEquals("I", tomes[0].romanNumeral)
        assertEquals("II", tomes[1].romanNumeral)
        assertEquals("V", tomes[4].romanNumeral)
        assertEquals("X", tomes[9].romanNumeral)
        assertEquals("L", tomes[49].romanNumeral)
        assertEquals("LII", tomes[51].romanNumeral)

        // Verify physical book aesthetics: realistic height between 0.88 and 1.0
        tomes.forEach { tome ->
            assertTrue("Height variation should be between 0.88 and 1.0", tome.heightFraction in 0.88f..1.0f)
            assertTrue("Ribs should be between 3 and 5", tome.spineRibs in 3..5)
            assertNotNull("Leather color must not be null", tome.leatherColor)
            assertTrue("Title must not be empty", tome.title.isNotBlank())
            assertTrue("County must not be empty", tome.county.isNotBlank())
        }
    }

    @Test
    fun testCuratedExpeditionTrailsIntegrity() {
        val trails = CuratedExpeditionTrails.allTrails
        assertEquals("Should have exactly 4 curated expedition trails", 4, trails.size)

        val trailIds = trails.map { it.id }.toSet()
        assertTrue(trailIds.contains("freedom_corridor"))
        assertTrue(trailIds.contains("karst_caverns"))
        assertTrue(trailIds.contains("river_and_rail"))
        assertTrue(trailIds.contains("pioneer_founders"))

        val allSiteIds = LoreSeedData.sites.map { it.id }.toSet()

        trails.forEach { trail ->
            assertTrue("Trail ${trail.id} must have title", trail.title.isNotBlank())
            assertTrue("Trail ${trail.id} must have waypoints", trail.orderedSiteIds.isNotEmpty())
            assertTrue("Trail ${trail.id} distance must be positive", trail.estimatedDistanceKm > 0)
            assertTrue("Trail ${trail.id} must have badge id", trail.badgeId.isNotBlank())
            assertTrue("Trail ${trail.id} must have badge title", trail.badgeTitle.isNotBlank())

            // Verify all trail waypoints exist in seed data
            trail.orderedSiteIds.forEach { siteId ->
                assertTrue("Site $siteId in trail ${trail.id} must exist in seed data", allSiteIds.contains(siteId))
            }
        }
    }

    @Test
    fun testExpeditionTrailProgressCalculation() {
        val trail = CuratedExpeditionTrails.allTrails.first { it.id == "freedom_corridor" }
        val waypoints = trail.orderedSiteIds

        // Test 0% progress
        val emptyProgress = CuratedExpeditionTrails.calculateProgress(trail, emptySet())
        assertEquals(0, emptyProgress.visitedCount)
        assertEquals(waypoints.size, emptyProgress.totalCount)
        assertEquals(0, emptyProgress.progressPercent)
        assertFalse(emptyProgress.isCompleted)
        assertFalse(emptyProgress.badge.isUnlocked)

        // Test partial progress
        val partialVisited = setOf(waypoints[0], waypoints[1])
        val partialProgress = CuratedExpeditionTrails.calculateProgress(trail, partialVisited)
        assertEquals(2, partialProgress.visitedCount)
        assertEquals((2f / waypoints.size * 100).toInt(), partialProgress.progressPercent)
        assertFalse(partialProgress.isCompleted)
        assertFalse(partialProgress.badge.isUnlocked)

        // Test 100% completed progress
        val fullVisited = waypoints.toSet()
        val fullProgress = CuratedExpeditionTrails.calculateProgress(trail, fullVisited)
        assertEquals(waypoints.size, fullProgress.visitedCount)
        assertEquals(100, fullProgress.progressPercent)
        assertTrue(fullProgress.isCompleted)
        assertTrue(fullProgress.badge.isUnlocked)
    }

    @Test
    fun testThematicShelfCategorization() {
        val allSites = LoreSeedData.sites
        val tomes = allSites.mapIndexed { index, site -> LoreTome.fromSite(site, index) }

        // Underground Railroad shelf
        val urrTomes = tomes.filter { tome ->
            tome.category.contains("Underground", ignoreCase = true) ||
            tome.title.contains("Underground", ignoreCase = true) ||
            tome.subtitle.contains("Abolition", ignoreCase = true)
        }
        assertTrue("Should have multiple Underground Railroad tomes", urrTomes.size >= 4)

        // Subterranean Karst shelf
        val karstTomes = tomes.filter { tome ->
            tome.category.contains("Cave", ignoreCase = true) ||
            tome.title.contains("Cave", ignoreCase = true) ||
            tome.site.whatMayBeUnderground.isNotBlank()
        }
        assertTrue("Should have multiple Subterranean Karst tomes", karstTomes.size >= 10)

        // Riverboat & Rail shelf
        val railTomes = tomes.filter { tome ->
            tome.category.contains("Steam", ignoreCase = true) ||
            tome.title.contains("Rail", ignoreCase = true) ||
            tome.title.contains("Depot", ignoreCase = true) ||
            tome.title.contains("Bridge", ignoreCase = true)
        }
        assertTrue("Should have multiple Riverboat & Rail tomes", railTomes.size >= 5)
    }
}
