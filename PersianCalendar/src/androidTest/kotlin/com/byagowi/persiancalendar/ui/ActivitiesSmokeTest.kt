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
import com.byagowi.persiancalendar.ui.settings.agewidget.WidgetAgeConfigureActivity
import com.byagowi.persiancalendar.ui.settings.wallpaper.ScreensaverConfigurationActivity
import com.byagowi.persiancalendar.ui.settings.wallpaper.WallpaperConfigurationActivity
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class ActivitiesSmokeTest {
    @Test
    fun test() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java))
        ActivityScenario.launch<AthanActivity>(
            Intent(context, AthanActivity::class.java)
                .putExtra(KEY_EXTRA_PRAYER, PrayTime.ASR.name)
        )
//        ActivityScenario.launch<WidgetConfigurationActivity>(
//            Intent(context, WidgetConfigurationActivity::class.java)
//        )
        ActivityScenario.launch<WallpaperConfigurationActivity>(
            Intent(context, WallpaperConfigurationActivity::class.java)
        )
        ActivityScenario.launch<ScreensaverConfigurationActivity>(
            Intent(context, ScreensaverConfigurationActivity::class.java)
        )
        ActivityScenario.launch<WidgetAgeConfigureActivity>(
            Intent(context, WidgetAgeConfigureActivity::class.java)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0)
        )
    }
}
