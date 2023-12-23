package com.byagowi.persiancalendar.ui

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.edit
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import com.byagowi.persiancalendar.DEFAULT_NOTIFY_DATE
import com.byagowi.persiancalendar.LAST_CHOSEN_TAB_KEY
import com.byagowi.persiancalendar.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.PREF_EASTERN_GREGORIAN_ARABIC_MONTHS
import com.byagowi.persiancalendar.PREF_ENGLISH_GREGORIAN_PERSIAN_MONTHS
import com.byagowi.persiancalendar.PREF_HAS_EVER_VISITED
import com.byagowi.persiancalendar.PREF_ISLAMIC_OFFSET
import com.byagowi.persiancalendar.PREF_ISLAMIC_OFFSET_SET_DATE
import com.byagowi.persiancalendar.PREF_LAST_APP_VISIT_VERSION
import com.byagowi.persiancalendar.PREF_MIDNIGHT_METHOD
import com.byagowi.persiancalendar.PREF_NOTIFY_DATE
import com.byagowi.persiancalendar.PREF_PRAY_TIME_METHOD
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.configureCalendarsAndLoadEvents
import com.byagowi.persiancalendar.global.initGlobal
import com.byagowi.persiancalendar.global.loadLanguageResources
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.service.ApplicationService
import com.byagowi.persiancalendar.ui.theme.AppTheme
import com.byagowi.persiancalendar.ui.utils.isLight
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.applyLanguageToConfiguration
import com.byagowi.persiancalendar.utils.putJdn
import com.byagowi.persiancalendar.utils.readAndStoreDeviceCalendarEventsOfTheDay
import com.byagowi.persiancalendar.utils.startWorker
import com.byagowi.persiancalendar.utils.update
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainActivity : ComponentActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Just to make sure we have an initial transparent system bars
        // System bars are tweaked later with project's own transparentSystemBars also
        enableEdgeToEdge()
        setTheme(R.style.BaseTheme)
        applyAppLanguage(this)
        super.onCreate(savedInstanceState)

        initGlobal(this)

        startWorker(this)

        readAndStoreDeviceCalendarEventsOfTheDay(applicationContext)
        update(applicationContext, false)

        val intentStartDestination = intent?.action
        intent?.action = ""

        setContent {
            AppTheme {
                val isBackgroundColorLight = MaterialTheme.colorScheme.background.isLight
                val isSurfaceColorLight = MaterialTheme.colorScheme.surface.isLight
                LaunchedEffect(isBackgroundColorLight, isSurfaceColorLight) {
                    transparentSystemBars(window, isBackgroundColorLight, isSurfaceColorLight)
                }

                App(intentStartDestination, ::finish)
            }
        }

        appPrefs.registerOnSharedPreferenceChangeListener(this)

        applyAppLanguage(this)

        // There is a window:enforceNavigationBarContrast set to false in styles.xml as the following
        // isn't as effective in dark themes.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }

    //        if (settingHasChanged) { // update when checked menu item is changed
//            applyAppLanguage(this)
//            update(applicationContext, true)
//            settingHasChanged = false // reset for the next time
//        }
    override fun onSharedPreferenceChanged(prefs: SharedPreferences?, key: String?) {
        prefs ?: return

        // If it is the first initiation of preference, don't call the rest multiple times
        if (key == PREF_HAS_EVER_VISITED || PREF_HAS_EVER_VISITED !in prefs) return

        when (key) {
            PREF_LAST_APP_VISIT_VERSION -> return // nothing needs to be updated
            LAST_CHOSEN_TAB_KEY -> return // don't run the expensive update and etc on tab changes
            PREF_ISLAMIC_OFFSET -> prefs.edit { putJdn(PREF_ISLAMIC_OFFSET_SET_DATE, Jdn.today()) }
            PREF_PRAY_TIME_METHOD -> prefs.edit { remove(PREF_MIDNIGHT_METHOD) }
            PREF_NOTIFY_DATE -> {
                if (!prefs.getBoolean(PREF_NOTIFY_DATE, DEFAULT_NOTIFY_DATE)) {
                    stopService(Intent(this, ApplicationService::class.java))
                    startWorker(applicationContext)
                }
            }
        }

        configureCalendarsAndLoadEvents(this)
        updateStoredPreference(this)
        update(applicationContext, true)

        if (key == PREF_APP_LANGUAGE) {
            applyAppLanguage(this)
            loadLanguageResources(this)
        }

        if (key == PREF_EASTERN_GREGORIAN_ARABIC_MONTHS || key == PREF_ENGLISH_GREGORIAN_PERSIAN_MONTHS) {
            loadLanguageResources(this)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(applyLanguageToConfiguration(newConfig))
        applyAppLanguage(this)
    }

    override fun onResume() {
        super.onResume()
        applyAppLanguage(this)
        update(applicationContext, false)
        ++resumeToken_.value
    }
}

private val resumeToken_ = MutableStateFlow(0)
val resumeToken: StateFlow<Int> = resumeToken_

/**
 * Make system bars (status and navigation bars) transparent as far as possible, also disables
 * decor view insets so we should consider the insets ourselves.
 *
 * From https://stackoverflow.com/a/76018821 with some modifications
 * Also have a look at [androidx.activity.enableEdgeToEdge] which provides the same functionality
 * but in non-gesture navigation is less immersive.
 */
private fun transparentSystemBars(
    window: Window,
    isBackgroundColorLight: Boolean,
    isSurfaceColorLight: Boolean,
) {
    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
    insetsController.isAppearanceLightStatusBars = isBackgroundColorLight
    insetsController.isAppearanceLightNavigationBars = isSurfaceColorLight

    val isLightStatusBarAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    val isLightNavigationBarAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    // Either primary color, what we use behind above status icons, isn't light so we don't need to worry
    // about not being able to set isAppearanceLightStatusBars or let's check the sdk version so
    // we at least use isAppearanceLightStatusBars.
    val shouldStatusBarBeTransparent = !isBackgroundColorLight || isLightStatusBarAvailable

    // Either surface color, what we use behind below navigation icons, isn't light so we don't need to worry
    // about not being able to set isAppearanceLightNavigationBars or let's check the sdk version so
    // we at least use isAppearanceLightStatusBars.
    val shouldNavigationBarBeTransparent = !isSurfaceColorLight || isLightNavigationBarAvailable

    val systemUiScrim = ColorUtils.setAlphaComponent(Color.BLACK, 0x40) // 25% black
    window.statusBarColor = if (shouldStatusBarBeTransparent) Color.TRANSPARENT else systemUiScrim
    window.navigationBarColor =
        if (shouldNavigationBarBeTransparent) Color.TRANSPARENT else systemUiScrim
}
