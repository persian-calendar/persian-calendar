//package com.byagowi.persiancalendar.ui
//
//import android.os.Build
//import androidx.fragment.app.testing.launchFragmentInContainer
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import androidx.test.filters.LargeTest
//import com.byagowi.persiancalendar.R
//import com.byagowi.persiancalendar.ui.about.AboutFragment
//import com.byagowi.persiancalendar.ui.astronomy.AstronomyFragment
//import com.byagowi.persiancalendar.ui.calendar.CalendarFragment
//import com.byagowi.persiancalendar.ui.compass.CompassFragment
//import com.byagowi.persiancalendar.ui.converter.ConverterFragment
//import com.byagowi.persiancalendar.ui.level.LevelFragment
//import com.byagowi.persiancalendar.ui.map.MapFragment
//import com.byagowi.persiancalendar.ui.settings.INTERFACE_CALENDAR_TAB
//import com.byagowi.persiancalendar.ui.settings.LOCATION_ATHAN_TAB
//import com.byagowi.persiancalendar.ui.settings.SettingsFragment
//import com.byagowi.persiancalendar.ui.settings.SettingsFragmentArgs
//import com.byagowi.persiancalendar.ui.settings.WIDGET_NOTIFICATION_TAB
//import org.junit.Test
//import org.junit.runner.RunWith
//
//@LargeTest
//@RunWith(AndroidJUnit4::class)
//class FragmentsSmokeTest {
//    @Test
//    fun themesSmokeTest() {
//        listOf(
//            R.style.LightTheme, R.style.DarkTheme, R.style.ModernTheme, R.style.AquaTheme,
//            R.style.BlackTheme
//        ).let {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) it + listOf(
//                R.style.DynamicLightTheme, R.style.DynamicBlackTheme, R.style.DynamicDarkTheme,
//                R.style.DynamicModernTheme
//            ) else it
//        }.forEach { launchFragmentInContainer<CalendarFragment>(themeResId = it) }
//    }
//
//    @Test
//    fun fragmentsSmokeTest() {
//        launchFragmentInContainer<CalendarFragment>(themeResId = R.style.LightTheme)
//        launchFragmentInContainer<AboutFragment>(themeResId = R.style.LightTheme)
//        launchFragmentInContainer<CompassFragment>(themeResId = R.style.LightTheme)
//        launchFragmentInContainer<ConverterFragment>(themeResId = R.style.LightTheme)
//        launchFragmentInContainer<LevelFragment>(themeResId = R.style.LightTheme)
//        launchFragmentInContainer<AstronomyFragment>(themeResId = R.style.LightTheme)
//        launchFragmentInContainer<MapFragment>(themeResId = R.style.LightTheme)
//        listOf(INTERFACE_CALENDAR_TAB, WIDGET_NOTIFICATION_TAB, LOCATION_ATHAN_TAB).forEach {
//            launchFragmentInContainer<SettingsFragment>(
//                themeResId = R.style.LightTheme,
//                fragmentArgs = SettingsFragmentArgs(it).toBundle()
//            )
//        }
//    }
//}
