package com.byagowi.persiancalendar.ui

import android.os.Build
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.about.AboutScreen
import com.byagowi.persiancalendar.ui.astronomy.AstronomyScreen
import com.byagowi.persiancalendar.ui.calendar.CalendarScreen
import com.byagowi.persiancalendar.ui.compass.CompassScreen
import com.byagowi.persiancalendar.ui.converter.ConverterScreen
import com.byagowi.persiancalendar.ui.level.LevelScreen
import com.byagowi.persiancalendar.ui.map.MapScreen
import com.byagowi.persiancalendar.ui.map.SkyRendererScreen
import com.byagowi.persiancalendar.ui.settings.SettingsScreen
import com.byagowi.persiancalendar.ui.settings.SettingsScreen.Companion.INTERFACE_CALENDAR_TAB
import com.byagowi.persiancalendar.ui.settings.SettingsScreen.Companion.LOCATION_ATHAN_TAB
import com.byagowi.persiancalendar.ui.settings.SettingsScreen.Companion.WIDGET_NOTIFICATION_TAB
import com.byagowi.persiancalendar.ui.settings.SettingsScreenArgs
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class FragmentsSmokeTest {

    @Test
    fun themesSmokeTest() {
        listOf(
            R.style.LightTheme, R.style.DarkTheme, R.style.ModernTheme, R.style.AquaTheme,
            R.style.BlackTheme
        ).let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) it + listOf(
                R.style.DynamicLightTheme, R.style.DynamicBlackTheme, R.style.DynamicDarkTheme,
                R.style.DynamicModernTheme
            ) else it
        }.forEach { launchFragmentInContainer<CalendarScreen>(themeResId = it) }
    }

    @Test
    fun fragmentsSmokeTest() {
        launchFragmentInContainer<CalendarScreen>(themeResId = R.style.LightTheme)
        launchFragmentInContainer<AboutScreen>(themeResId = R.style.LightTheme)
        launchFragmentInContainer<CompassScreen>(themeResId = R.style.LightTheme)
        launchFragmentInContainer<ConverterScreen>(themeResId = R.style.LightTheme)
        launchFragmentInContainer<LevelScreen>(themeResId = R.style.LightTheme)
        launchFragmentInContainer<AstronomyScreen>(themeResId = R.style.LightTheme)
        launchFragmentInContainer<MapScreen>(themeResId = R.style.LightTheme)
        launchFragmentInContainer<SkyRendererScreen>(themeResId = R.style.LightTheme)
        listOf(INTERFACE_CALENDAR_TAB, WIDGET_NOTIFICATION_TAB, LOCATION_ATHAN_TAB).forEach {
            launchFragmentInContainer<SettingsScreen>(
                themeResId = R.style.LightTheme,
                fragmentArgs = SettingsScreenArgs(it).toBundle()
            )
        }
    }
}
