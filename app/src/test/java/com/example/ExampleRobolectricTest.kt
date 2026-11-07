package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.hardware.SteeringWheelAction
import com.example.hardware.SteeringWheelManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Car Launcher", appName)
  }

  @Test
  fun `verify steering wheel action mapping`() {
    val manager = SteeringWheelManager()
    val mappedNext = manager.getMappedKeyName(SteeringWheelAction.NEXT_TRACK)
    assertNotEquals("Not Assigned", mappedNext)

    val mappedPlayPause = manager.getMappedKeyName(SteeringWheelAction.PLAY_PAUSE)
    assertNotEquals("Not Assigned", mappedPlayPause)
  }

  @Test
  fun `verify apps manager loads apps and categories`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appsManager = com.example.launcher.AppsManager(context)
    appsManager.loadInstalledApps()
    val apps = appsManager.installedApps.value
    assertTrue("Should have fallback or installed apps", apps.isNotEmpty())
  }
}
