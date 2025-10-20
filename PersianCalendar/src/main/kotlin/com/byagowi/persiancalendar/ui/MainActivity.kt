package com.byagowi.persiancalendar.ui

import android.content.ContentUris
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.content.edit
import androidx.core.content.getSystemService
import com.byagowi.persiancalendar.DEFAULT_NOTIFY_DATE
import com.byagowi.persiancalendar.EXPANDED_TIME_STATE_KEY
import com.byagowi.persiancalendar.LAST_CHOSEN_TAB_KEY
import com.byagowi.persiancalendar.LAST_PLAYED_ATHAN_JDN
import com.byagowi.persiancalendar.LAST_PLAYED_ATHAN_KEY
import com.byagowi.persiancalendar.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.PREF_AZERI_ALTERNATIVE_PERSIAN_MONTHS
import com.byagowi.persiancalendar.PREF_EASTERN_GREGORIAN_ARABIC_MONTHS
import com.byagowi.persiancalendar.PREF_ENGLISH_GREGORIAN_PERSIAN_MONTHS
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
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.loadLanguageResources
import com.byagowi.persiancalendar.global.updateAccessibilityFlows
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.service.ApplicationService
import com.byagowi.persiancalendar.ui.theme.AppTheme
import com.byagowi.persiancalendar.ui.utils.isLight
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.applyLanguageToConfiguration
import com.byagowi.persiancalendar.utils.eventKey
import com.byagowi.persiancalendar.utils.jdnActionKey
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.putJdn
import com.byagowi.persiancalendar.utils.readAndStoreDeviceCalendarEventsOfTheDay
import com.byagowi.persiancalendar.utils.startWorker
import com.byagowi.persiancalendar.utils.toCivilDate
import com.byagowi.persiancalendar.utils.toGregorianCalendar
import com.byagowi.persiancalendar.utils.update
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date


class MainActivity : ComponentActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Just to make sure we have an initial transparent system bars
        // System bars are tweaked later with project's with real values
        applyEdgeToEdge(isBackgroundColorLight = false, isSurfaceColorLight = true)

        setTheme(R.style.BaseTheme)
        applyAppLanguage(this)
        super.onCreate(savedInstanceState)

        intent.getLongExtra(eventKey, -1L).takeIf { it != -1L }?.let { eventId ->
            val intent = Intent(Intent.ACTION_VIEW).setData(
                ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
            )
            runCatching { startActivity(intent) }.onFailure(logException)
            return finish()
        }

        initGlobal(this)

        startWorker(this)

        readAndStoreDeviceCalendarEventsOfTheDay(applicationContext)
        update(applicationContext, false)

        val initialJdn = run {
            // Follows https://github.com/FossifyOrg/Calendar/blob/fb56145d/app/src/main/kotlin/org/fossify/calendar/activities/MainActivity.kt#L531-L554
            // Receives content://com.android.calendar/time/1740774600000 or content://0@com.android.calendar/time/1740774600000
            intent?.data?.takeIf {
                when (CalendarContract.AUTHORITY) {
                    it.authority, it.authority?.substringAfter("@") -> true
                    else -> false
                } && when {
                    it.path?.startsWith("/time") == true -> true
                    intent?.extras?.getBoolean("DETAIL_VIEW", false) == true -> true
                    else -> false
                }
            }?.pathSegments?.last()?.toLongOrNull()?.let {
                Jdn(Date(it).toGregorianCalendar().toCivilDate())
            } ?: (intent.getLongExtra(jdnActionKey, -1L).takeIf { it != -1L }
                ?: intent.action?.takeIf {
                    it.startsWith(jdnActionKey)
                }?.replace(jdnActionKey, "")?.toLongOrNull())?.let(::Jdn)
        }
        setContent {
            AppTheme {
                val isBackgroundColorLight = MaterialTheme.colorScheme.background.isLight
                val isSurfaceColorLight = MaterialTheme.colorScheme.surface.isLight
                LaunchedEffect(isBackgroundColorLight, isSurfaceColorLight) {
                    applyEdgeToEdge(isBackgroundColorLight, isSurfaceColorLight)
                }

                val view = LocalView.current
                LaunchedEffect(Unit) {
                    language.collect {
                        onConfigurationChanged(resources.configuration)
                        view.dispatchConfigurationChanged(resources.configuration)
                    }
                }

                App(intent?.action, initialJdn, ::finish)
            }
        }

        applyAppLanguage(this)

        // There is a window:enforceNavigationBarContrast set to false in styles.xml as the following
        // isn't as effective in dark themes.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences?, key: String?) {
        when (key) {
            PREF_LAST_APP_VISIT_VERSION -> return // nothing needs to be updated
            EXPANDED_TIME_STATE_KEY -> return // nothing needs to be updated
            LAST_PLAYED_ATHAN_JDN, LAST_PLAYED_ATHAN_KEY -> return // nothing needs to be updated
            LAST_CHOSEN_TAB_KEY -> return // don't run the expensive update and etc on tab changes
            PREF_ISLAMIC_OFFSET -> {
                this.preferences.edit { putJdn(PREF_ISLAMIC_OFFSET_SET_DATE, Jdn.today()) }
            }

            PREF_PRAY_TIME_METHOD -> this.preferences.edit { remove(PREF_MIDNIGHT_METHOD) }
            PREF_NOTIFY_DATE -> {
                if (!this.preferences.getBoolean(PREF_NOTIFY_DATE, DEFAULT_NOTIFY_DATE)) {
                    stopService(Intent(this, ApplicationService::class.java))
                    startWorker(applicationContext)
                }
            }
        }

        configureCalendarsAndLoadEvents(this)
        updateStoredPreference(this)

        if (key == PREF_APP_LANGUAGE) {
            applyAppLanguage(this)
            loadLanguageResources(this.resources)
        }

        if (key == PREF_EASTERN_GREGORIAN_ARABIC_MONTHS ||
            key == PREF_ENGLISH_GREGORIAN_PERSIAN_MONTHS ||
            key == PREF_AZERI_ALTERNATIVE_PERSIAN_MONTHS
        ) {
            loadLanguageResources(this.resources)
        }

        update(this, true)
    }

    private fun applyEdgeToEdge(isBackgroundColorLight: Boolean, isSurfaceColorLight: Boolean) {
        val statusBarStyle =
            if (isBackgroundColorLight) SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            else SystemBarStyle.dark(Color.TRANSPARENT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) enableEdgeToEdge(
            statusBarStyle,
            if (isSurfaceColorLight) SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            else SystemBarStyle.dark(Color.TRANSPARENT),
        ) else enableEdgeToEdge(
            statusBarStyle,
            // Just don't tweak navigation bar in older Android versions, leave it to default
        )
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

    override fun onStart() {
        super.onStart()
        val accessibilityService = getSystemService<AccessibilityManager>()
        accessibilityService?.addTouchExplorationStateChangeListener(
            touchExplorationStateChangeListener
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) highContrastTextStateChangeListener?.let {
            accessibilityService?.addHighContrastTextStateChangeListener(
                mainExecutor, it
            )
        }
    }

    override fun onStop() {
        super.onStop()
        val accessibilityService = getSystemService<AccessibilityManager>()
        accessibilityService?.removeTouchExplorationStateChangeListener(
            touchExplorationStateChangeListener
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) highContrastTextStateChangeListener?.let {
            accessibilityService?.removeHighContrastTextStateChangeListener(it)
        }
    }

    private val touchExplorationStateChangeListener =
        AccessibilityManager.TouchExplorationStateChangeListener {
            getSystemService<AccessibilityManager>()?.updateAccessibilityFlows()
            update(this, true)
        }
    private val highContrastTextStateChangeListener =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            AccessibilityManager.HighContrastTextStateChangeListener {
                getSystemService<AccessibilityManager>()?.updateAccessibilityFlows()
            }
        } else null
}

private val resumeToken_ = MutableStateFlow(0)
val resumeToken: StateFlow<Int> = resumeToken_
