package com.byagowi.persiancalendar.ui

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.byagowi.persiancalendar.KEY_EXTRA_PRAYER
import com.byagowi.persiancalendar.entities.PrayTime
import com.byagowi.persiancalendar.ui.athan.AthanActivity
import com.byagowi.persiancalendar.ui.settings.agewidget.AgeWidgetConfigureActivity
import com.byagowi.persiancalendar.ui.settings.wallpaper.DreamSettingsActivity
import com.byagowi.persiancalendar.ui.settings.wallpaper.WallpaperSettingsActivity
import com.byagowi.persiancalendar.ui.settings.widgetnotification.WidgetConfigurationActivity
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class ActivitiesSmokeTest {

    private fun getAppContext(): Context = ApplicationProvider.getApplicationContext()

    private fun launchActivity(intent: Intent) {
        ActivityScenario.launch(intent)
    }

    @Test
    fun testLaunchMainActivity() {
        launchActivity(Intent(getAppContext(), MainActivity::class.java))
    }

    @Test
    fun testLaunchAthanActivities() {
        val context = getAppContext()
        PrayTime.values().forEach { prayTime ->
            launchActivity(Intent(context, AthanActivity::class.java).apply {
                putExtra(KEY_EXTRA_PRAYER, prayTime.name)
            })
        }
    }

    @Test
    fun testLaunchWidgetConfigurations() {
        val context = getAppContext()
        (1..5).forEach { id ->
            launchActivity(Intent(context, AgeWidgetConfigureActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
            })
        }
    }

    @Test
    fun testLaunchSettingsActivitiesMultipleTimes() {
        val context = getAppContext()
        repeat(3) {
            launchActivity(Intent(context, WallpaperSettingsActivity::class.java))
            launchActivity(Intent(context, DreamSettingsActivity::class.java))
        }
    }

    @Test
    fun testWidgetAndSettingsIntegration() {
        val context = getAppContext()
        listOf(
            WidgetConfigurationActivity::class.java,
            WallpaperSettingsActivity::class.java,
            DreamSettingsActivity::class.java
        ).forEach { activity ->
            launchActivity(Intent(context, activity))
        }
    }

    @Test
    fun testSequentialActivityLaunches() {
        val context = getAppContext()
        val activities = listOf(
            MainActivity::class.java,
            AthanActivity::class.java,
            WidgetConfigurationActivity::class.java,
            WallpaperSettingsActivity::class.java,
            DreamSettingsActivity::class.java,
            AgeWidgetConfigureActivity::class.java
        )
        activities.forEach { activityClass ->
            val intent = Intent(context, activityClass)
            when (activityClass) {
                AthanActivity::class.java -> intent.putExtra(KEY_EXTRA_PRAYER, PrayTime.MAGHRIB.name)
                AgeWidgetConfigureActivity::class.java -> intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 99)
            }
            launchActivity(intent)
        }
    }

    @Test
    fun testRapidLaunchAndClose() {
        val context = getAppContext()
        repeat(5) {
            launchActivity(Intent(context, MainActivity::class.java))
            launchActivity(Intent(context, AthanActivity::class.java).apply {
                putExtra(KEY_EXTRA_PRAYER, PrayTime.FAJR.name)
            })
        }
    }

    // UI improvements for testing purposes
    @Test
    fun testActivityAppearanceEnhancements() {
        val context = getAppContext()
        val intent = Intent(context, MainActivity::class.java)
        launchActivity(intent)
        // Hypothetical UI enhancement logic, e.g., setting theme or dark mode for better visual testing
        // This is placeholder since actual UI modifications require activity runtime code
    }
}
 
