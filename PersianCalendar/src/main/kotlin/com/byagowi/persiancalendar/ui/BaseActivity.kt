package com.byagowi.persiancalendar.ui

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.annotation.CallSuper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.lifecycle.Lifecycle
import com.byagowi.persiancalendar.DEFAULT_NOTIFY_DATE
import com.byagowi.persiancalendar.EXPANDED_TIME_STATE_KEY
import com.byagowi.persiancalendar.LAST_CHOSEN_TAB_KEY
import com.byagowi.persiancalendar.LAST_PLAYED_ATHAN_JDN
import com.byagowi.persiancalendar.LAST_PLAYED_ATHAN_KEY
import com.byagowi.persiancalendar.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.PREF_AZERI_ALTERNATIVE_PERSIAN_MONTHS
import com.byagowi.persiancalendar.PREF_CALCULATOR_INPUT
import com.byagowi.persiancalendar.PREF_EASTERN_GREGORIAN_ARABIC_MONTHS
import com.byagowi.persiancalendar.PREF_ENGLISH_GREGORIAN_PERSIAN_MONTHS
import com.byagowi.persiancalendar.PREF_ENGLISH_WEEKDAYS_IN_IRAN_ENGLISH
import com.byagowi.persiancalendar.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.PREF_ISLAMIC_OFFSET
import com.byagowi.persiancalendar.PREF_ISLAMIC_OFFSET_SET_DATE
import com.byagowi.persiancalendar.PREF_LAST_APP_VISIT_VERSION
import com.byagowi.persiancalendar.PREF_MIDNIGHT_METHOD
import com.byagowi.persiancalendar.PREF_NOTIFY_DATE
import com.byagowi.persiancalendar.PREF_PRAY_TIME_METHOD
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.configureCalendarsAndLoadEvents
import com.byagowi.persiancalendar.global.loadLanguageResources
import com.byagowi.persiancalendar.global.updateAccessibilityFlows
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.service.ApplicationService
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.applyLanguageToConfiguration
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.putJdn
import com.byagowi.persiancalendar.utils.startWorker
import com.byagowi.persiancalendar.utils.update

abstract class BaseActivity : ComponentActivity(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        applyAppLanguage(this)
        super.onCreate(savedInstanceState)
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    final override fun onDestroy() {
        super.onDestroy()
        preferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    final override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(applyLanguageToConfiguration(newConfig))
        applyAppLanguage(this)
    }

    final override fun onSharedPreferenceChanged(preferences: SharedPreferences?, key: String?) {
        if (this !is MainActivity && !lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) return
        ++preferencesUpdateToken_.intValue
        when (key) {
            PREF_CALCULATOR_INPUT -> return // nothing needs to be updated
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

        if (when (key) {
                PREF_EASTERN_GREGORIAN_ARABIC_MONTHS -> true
                PREF_ENGLISH_GREGORIAN_PERSIAN_MONTHS -> true
                PREF_AZERI_ALTERNATIVE_PERSIAN_MONTHS -> true
                PREF_ENGLISH_WEEKDAYS_IN_IRAN_ENGLISH -> true
                PREF_HOLIDAY_TYPES -> true
                else -> false
            }
        ) loadLanguageResources(this.resources)

        update(this, true)
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        applyAppLanguage(this)
        val accessibilityService = getSystemService<AccessibilityManager>()
        accessibilityService?.addTouchExplorationStateChangeListener(
            touchExplorationStateChangeListener
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) highContrastTextStateChangeListener?.let {
            accessibilityService?.addHighContrastTextStateChangeListener(mainExecutor, it)
        }
    }

    override fun onPause() {
        super.onPause()
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

private val preferencesUpdateToken_ = mutableIntStateOf(0)
val preferencesUpdateToken by preferencesUpdateToken_
