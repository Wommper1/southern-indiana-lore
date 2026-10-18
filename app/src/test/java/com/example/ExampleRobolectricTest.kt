package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    // Debug builds append " (debug)" (see src/debug/res/values/strings.xml) so they can sit next
    // to the real app on one phone.
    assertTrue("unexpected app name: $appName", appName.startsWith("Southern Indiana Lore"))
  }
}
