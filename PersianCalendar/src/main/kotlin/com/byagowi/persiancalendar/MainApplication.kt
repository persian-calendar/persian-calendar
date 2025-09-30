package com.byagowi.persiancalendar

import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import androidx.compose.runtime.Composer
import androidx.compose.runtime.ExperimentalComposeRuntimeApi
import androidx.core.content.edit
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.configureCalendarsAndLoadEvents
import com.byagowi.persiancalendar.global.initGlobal
import com.byagowi.persiancalendar.global.loadLanguageResources
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.service.ApplicationService
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.putJdn
import com.byagowi.persiancalendar.utils.startWorker
import com.byagowi.persiancalendar.utils.update

/**
 * Main application class.
 *
 * Notes on changes made while improving robustness and UX-related hooks:
 * - Wrapped preference change handling in safe null checks and try/catch to avoid crashes.
 * - Registered and unregistered the SharedPreferences listener to avoid leaks.
 * - Added lifecycle helpers to trigger workers and language reloads safely from tests or debug tools.
 */
class MainApplication : Application(), SharedPreferences.OnSharedPreferenceChangeListener {
    @OptIn(ExperimentalComposeRuntimeApi::class)
    override fun onCreate() {
        super.onCreate()
        // Enable diagnostic stack traces in development builds for better debugging
        if (BuildConfig.DEVELOPMENT) Composer.setDiagnosticStackTraceEnabled(BuildConfig.DEBUG)

        // Initialize global resources (locales, calendars, etc.)
        initGlobal(applicationContext)

        // Ensure preferences listener is registered once
        preferences.registerOnSharedPreferenceChangeListener(this)

        // Perform an initial update (safe; wrapped internally)
        update(this, true)

        // Trigger background worker so widgets/notifications are scheduled on app start
        try {
            startWorker(applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        // Unregister listener to avoid leaks in environments where onTerminate is called (e.g., tests)
        try {
            preferences.unregisterOnSharedPreferenceChangeListener(this)
        } catch (_: Exception) {
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        // Guard against null key — nothing to do
        if (key == null) return

        try {
            when (key) {
                PREF_LAST_APP_VISIT_VERSION -> return // no heavy work required
                EXPANDED_TIME_STATE_KEY -> return // UI state only
                LAST_PLAYED_ATHAN_JDN, LAST_PLAYED_ATHAN_KEY -> return // playback state only
                LAST_CHOSEN_TAB_KEY -> return // UI tab change
                PREF_ISLAMIC_OFFSET -> {
                    // record the date when user set custom islamic offset
                    this.preferences.edit { putJdn(PREF_ISLAMIC_OFFSET_SET_DATE, Jdn.today()) }
                }
                PREF_PRAY_TIME_METHOD -> this.preferences.edit { remove(PREF_MIDNIGHT_METHOD) }
                PREF_NOTIFY_DATE -> {
                    // If user disabled date notifications we stop the service; otherwise ensure worker is scheduled
                    if (!this.preferences.getBoolean(PREF_NOTIFY_DATE, DEFAULT_NOTIFY_DATE)) {
                        try {
                            stopService(Intent(this, ApplicationService::class.java))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        startWorker(applicationContext)
                    }
                }
            }

            // Reconfigure calendars and reload any events that depend on preferences
            configureCalendarsAndLoadEvents(this)

            // Persist computed preference-related values if needed
            updateStoredPreference(this)

            // Language related changes require applying language and reloading resources
            if (key == PREF_APP_LANGUAGE) {
                applyAppLanguage(this)
                loadLanguageResources(this.resources)
            }

            // Some month name toggles affect language resources as well
            if (key == PREF_EASTERN_GREGORIAN_ARABIC_MONTHS ||
                key == PREF_ENGLISH_GREGORIAN_PERSIAN_MONTHS ||
                key == PREF_AZERI_ALTERNATIVE_PERSIAN_MONTHS
            ) {
                loadLanguageResources(this.resources)
            }

            // Trigger a final update to reflect preference changes across app/widgets
            update(this, true)
        } catch (e: Exception) {
            // Catch-all to avoid crashing the app on unexpected preference-related errors
            e.printStackTrace()
        }
    }

    // ------------------ Utility helpers exposed for tests / debug UI ------------------

    /** Reset preferences to defaults and refresh app state. Useful for tests and "Reset" UI actions. */
    fun resetPreferences() {
        preferences.edit { clear() }
        configureCalendarsAndLoadEvents(this)
        updateStoredPreference(this)
        update(this, true)
    }

    /** Re-apply current app language and reload language resources. */
    fun reloadLanguage() {
        applyAppLanguage(this)
        loadLanguageResources(this.resources)
    }

    /** Trigger background workers (scheduling for notifications/widgets). */
    fun triggerBackgroundWorker() {
        try {
            startWorker(applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** Force re-configuration of calendars and events. */
    fun refreshCalendarsAndEvents() {
        configureCalendarsAndLoadEvents(this)
        update(this, true)
    }

    // ------------------ Convenience getters/setters for common preferences ------------------

    fun isNotificationEnabled(): Boolean =
        preferences.getBoolean(PREF_NOTIFY_DATE, DEFAULT_NOTIFY_DATE)

    fun setNotificationEnabled(enabled: Boolean) {
        preferences.edit { putBoolean(PREF_NOTIFY_DATE, enabled) }
        if (!enabled) {
            try {
                stopService(Intent(this, ApplicationService::class.java))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            triggerBackgroundWorker()
        }
    }

    fun getCurrentAppLanguage(): String? = preferences.getString(PREF_APP_LANGUAGE, null)

    fun setAppLanguage(languageCode: String) {
        preferences.edit { putString(PREF_APP_LANGUAGE, languageCode) }
        reloadLanguage()
    }

    fun getLastVisitedVersion(): Int = preferences.getInt(PREF_LAST_APP_VISIT_VERSION, 0)

    fun setLastVisitedVersion(version: Int) {
        preferences.edit { putInt(PREF_LAST_APP_VISIT_VERSION, version) }
    }
}
 
