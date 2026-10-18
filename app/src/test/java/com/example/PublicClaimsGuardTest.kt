package com.example

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Guards the public claims in README.md and SECURITY.md: no network access, no camera, least-privilege
 * permissions, no cloud backup. If one of these fails, either fix the app or change the docs on purpose.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PublicClaimsGuardTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun requestedPermissions(): Set<String> {
        val info = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
        return info.requestedPermissions?.toSet().orEmpty()
    }

    @Test
    fun `app declares no network, camera or wake lock permission`() {
        val forbidden = setOf(
            "android.permission.INTERNET",
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.CAMERA",
            "android.permission.WAKE_LOCK"
        )
        val found = requestedPermissions().intersect(forbidden)
        assertTrue("Forbidden permissions declared: $found", found.isEmpty())
    }

    @Test
    fun `only the documented permissions are requested`() {
        val documented = setOf(
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_BACKGROUND_LOCATION",
            "android.permission.POST_NOTIFICATIONS",
            "android.permission.FOREGROUND_SERVICE",
            "android.permission.FOREGROUND_SERVICE_LOCATION",
            "android.permission.VIBRATE",
            "android.permission.RECORD_AUDIO"
        )
        val unexpected = requestedPermissions().filter { it.startsWith("android.permission.") && it !in documented }
        assertTrue(
            "Undocumented permissions requested: $unexpected. Document them in README.md, SECURITY.md and " +
                "the Play data-safety notes, then add them here.",
            unexpected.isEmpty()
        )
    }

    @Test
    fun `cloud backup and device transfer are disabled`() {
        val allowsBackup = context.applicationInfo.flags and ApplicationInfo.FLAG_ALLOW_BACKUP
        assertEquals("android:allowBackup must stay false (journal and voice memos stay on the device)", 0, allowsBackup)
    }
}
