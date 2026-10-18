package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.LoreSeedData
import com.example.data.pack.LorePackManager
import com.example.data.pack.PackValidationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Keeps the lore pack's labels honest (see docs/VERIFICATION_LOG.md). A site may only claim a strong
 * confidence level once it has actually been checked, and claims that were removed after fact-checking must
 * not quietly come back.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LorePackHonestyTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val validation = LorePackManager.validatePack(context)

    private val strongLevels = setOf("OFFICIAL_SOURCE", "VERIFIED", "WELL_SUPPORTED", "PLAUSIBLE")
    private val checkedPrefix = "Checked "
    private val uncheckedPrefix = "Not yet independently verified"

    @Test
    fun `pack passes validation against its manifest hashes`() {
        assertEquals("Errors: ${validation.errors}", PackValidationStatus.VALID, validation.status)
        assertTrue(validation.errors.isEmpty())
        assertEquals(validation.siteCount, validation.parsedSiteCount)
        assertTrue(validation.integrityReport.isNotEmpty() && validation.integrityReport.all { it.matches })
    }

    @Test
    fun `every entry says whether it was checked`() {
        for (site in validation.validatedSites) {
            val grade = site.verificationGrade
            assertTrue(
                "${site.id}: verificationGrade must start with '$checkedPrefix' or '$uncheckedPrefix' but was '$grade'",
                grade.startsWith(checkedPrefix) || grade.startsWith(uncheckedPrefix)
            )
        }
    }

    @Test
    fun `strong confidence levels are only used by entries that were checked`() {
        for (site in validation.validatedSites) {
            if (site.confidenceLevel in strongLevels) {
                assertTrue(
                    "${site.id} is labeled ${site.confidenceLevel} but its grade is '${site.verificationGrade}'",
                    site.verificationGrade.startsWith(checkedPrefix)
                )
            }
        }
    }

    @Test
    fun `no entry claims archival verification`() {
        for (site in validation.validatedSites) {
            assertTrue("${site.id}: grade claims archival verification", !site.verificationGrade.contains("Archivally"))
        }
    }

    @Test
    fun `source notes disclose the verification status`() {
        for (site in validation.validatedSites) {
            val notes = site.sourceNotes
            assertTrue(
                "${site.id}: sourceNotes must say the citations were checked or not verified",
                notes.contains("Checked October 2026") || notes.contains("not been independently verified")
            )
        }
    }

    @Test
    fun `claims removed after fact-checking do not return`() {
        val raw = context.assets.open("${LorePackManager.PACK_ROOT}/sites.json").bufferedReader().use { it.readText() }
        val removed = listOf(
            "132-foot Constitution Elm",
            "Pope Paul VI",
            "K.C. Henderson",
            "Strobel Steel",
            "second longest railroad viaduct",
            "reached 85.4 feet",
            "Samuel Adams",
            "Ruby Rogers",
            "Thomas Harkins",
            "George Beck II",
            "Phoenix Bridge Company",
            "Archivally Verified"
        )
        for (phrase in removed) {
            assertTrue("Removed claim is back in sites.json: '$phrase' (see docs/VERIFICATION_LOG.md)", !raw.contains(phrase))
        }
    }

    @Test
    fun `emergency fallback data is never presented as verified`() {
        val fallback = LorePackManager.emergencyFallbackSites()
        assertEquals(LoreSeedData.sites.size, fallback.size)
        assertTrue(fallback.all { it.confidenceLevel == "UNVERIFIED" })
        assertTrue(fallback.all { it.verificationGrade.contains("not independently verified") })
    }
}
