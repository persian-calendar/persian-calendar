package com.byagowi.persiancalendar.ui.preferences.locationathan

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.ui.preferences.locationathan.athan.showAthanGapDialog
import com.byagowi.persiancalendar.ui.preferences.locationathan.athan.showAthanVolumeDialog
import com.byagowi.persiancalendar.ui.preferences.locationathan.athan.showPrayerSelectDialog
import com.byagowi.persiancalendar.ui.preferences.locationathan.location.showCoordinatesDialog
import com.byagowi.persiancalendar.ui.preferences.locationathan.location.showGPSLocationDialog
import com.byagowi.persiancalendar.ui.preferences.locationathan.location.showLocationPreferenceDialog
import com.byagowi.persiancalendar.utils.*
import com.google.android.material.snackbar.Snackbar

class LocationAthanFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val defaultAthanName: String
        get() {
            val context = context ?: return ""
            return context.getString(R.string.default_athan_name)
        }

    private val coordinatesPref = "PREF_COORDINATES"
    private val ringtonePref = "PREF_KEY_RINGTONE"

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = context ?: return

        val handler = Handler(Looper.getMainLooper()) // for deferred dependency wire ups
        val screen = preferenceManager.createPreferenceScreen(context)
        listOf(
            R.string.location to listOf(
                Preference(context).also {
                    it.setTitle(R.string.gps_location)
                    it.setSummary(R.string.gps_location_help)
                    it.onClick { showGPSLocationDialog() }
                },
                Preference(context).also {
                    it.setTitle(R.string.location)
                    it.setSummary(R.string.location_help)
                    it.onClick { showLocationPreferenceDialog() }
                },
                Preference(context).also {
                    it.key = coordinatesPref
                    it.setTitle(R.string.coordination)
                    it.onClick { showCoordinatesDialog() }
                }
            ),
            R.string.athan to listOf(
                ListPreference(context).also {
                    it.key = PREF_PRAY_TIME_METHOD
                    it.setDialogTitle(R.string.pray_methods_calculation)
                    it.setTitle(R.string.pray_methods)
                    it.setDefaultValue("Tehran")
                    it.setEntries(R.array.prayMethodsNames)
                    it.setEntryValues(R.array.prayMethodsKeys)
                    it.setNegativeButtonText(R.string.cancel)
                    it.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
                },
                Preference(context).also {
                    it.setTitle(R.string.athan_gap)
                    it.setSummary(R.string.athan_gap_summary)
                    it.onClick { showAthanGapDialog() }
                },
                Preference(context).also {
                    it.setTitle(R.string.athan_alarm)
                    it.setSummary(R.string.athan_alarm_summary)
                    it.onClick { showPrayerSelectDialog() }
                },
                SwitchPreferenceCompat(context).also {
                    it.key = PREF_NOTIFICATION_ATHAN
                    it.setTitle(R.string.notification_athan)
                    it.setSummary(R.string.enable_notification_athan)
                    it.setDefaultValue(false)
                    it.disableDependentsState = true
                },
                Preference(context).also {
                    handler.post { it.dependency = PREF_NOTIFICATION_ATHAN }
                    it.setTitle(R.string.athan_alarm)
                    it.setSummary(R.string.athan_alarm_summary)
                    it.onClick { showPrayerSelectDialog() }
                },
                Preference(context).also {
                    handler.post { it.dependency = PREF_NOTIFICATION_ATHAN }
                    it.setTitle(R.string.custom_athan)
                    it.key = ringtonePref
                    it.onClick {
                        runCatching { pickRingtone.launch(getCustomAthanUri(context)) }
                            .onFailure(logException).getOrNull()
                    }
                },
                Preference(context).also {
                    handler.post { it.dependency = PREF_NOTIFICATION_ATHAN }
                    it.setTitle(R.string.default_athan)
                    it.setTitle(R.string.default_athan_summary)
                    it.onClick {
                        context.appPrefs.edit {
                            remove(PREF_ATHAN_URI)
                            remove(PREF_ATHAN_NAME)
                        }
                        view?.let { v ->
                            Snackbar.make(v, R.string.returned_to_default, Snackbar.LENGTH_SHORT)
                                .show()
                        }
                        putAthanNameOnSummary(defaultAthanName)
                    }
                },
                SwitchPreferenceCompat(context).also {
                    handler.post { it.dependency = PREF_NOTIFICATION_ATHAN }
                    it.setDefaultValue(false)
                    it.disableDependentsState = true
                    it.key = PREF_ASCENDING_ATHAN_VOLUME
                    it.setTitle(R.string.ascending_athan_volume)
                    it.setSummary(R.string.enable_ascending_athan_volume)
                },
                Preference(context).also {
                    handler.post { it.dependency = PREF_ASCENDING_ATHAN_VOLUME }
                    it.setTitle(R.string.athan_volume)
                    it.setSummary(R.string.athan_volume_summary)
                    it.onClick { showAthanVolumeDialog() }
                }
            )
        ).forEach { (title, preferences) ->
            val category = PreferenceCategory(context)
            // Needed for expandable categories, also R.string.athan.toString() is used below
            category.key = title.toString()
            category.setTitle(title)
            category.isIconSpaceReserved = false
            screen.addPreference(category)
            preferences.onEach { it.isIconSpaceReserved = false }.forEach(category::addPreference)
        }
        preferenceScreen = screen

        onSharedPreferenceChanged(null, null)
        context.appPrefs.registerOnSharedPreferenceChangeListener(this)

        putAthanNameOnSummary(context.appPrefs.getString(PREF_ATHAN_NAME, defaultAthanName))
        updateLocationOnSummaries()
    }

    private class PickRingtoneContract : ActivityResultContract<Uri?, String>() {
        override fun createIntent(context: Context, input: Uri?): Intent =
            Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
                .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                .putExtra(
                    RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                    Settings.System.DEFAULT_NOTIFICATION_URI
                )
                .also { intent ->
                    input?.let { intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, it) }
                }

        override fun parseResult(resultCode: Int, intent: Intent?): String? =
            if (resultCode == RESULT_OK)
                intent
                    ?.getParcelableExtra<Parcelable?>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                    ?.toString()
            else null
    }

    private val pickRingtone = registerForActivityResult(PickRingtoneContract()) { uri ->
        uri ?: return@registerForActivityResult
        val context = context ?: return@registerForActivityResult
        val ringtone = RingtoneManager.getRingtone(context, uri.toUri())
        // If no ringtone has been found better to skip touching preferences store
        ringtone ?: return@registerForActivityResult
        val ringtoneTitle = ringtone.getTitle(context) ?: ""
        context.appPrefs.edit {
            putString(PREF_ATHAN_NAME, ringtoneTitle)
            putString(PREF_ATHAN_URI, uri)
        }
        view?.let {
            Snackbar.make(it, R.string.custom_notification_is_set, Snackbar.LENGTH_SHORT).show()
        }
        putAthanNameOnSummary(ringtoneTitle)
    }

    private fun putAthanNameOnSummary(athanName: String?) {
        findPreference<Preference>(ringtonePref)?.summary = athanName
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) =
        updateLocationOnSummaries()

    private fun updateLocationOnSummaries() {
        val context = context ?: return
        val cityName = getCityName(context, false).takeIf { it.isNotEmpty() }
        findPreference<Preference>(PREF_SELECTED_LOCATION)?.summary =
            cityName ?: context.getString(R.string.location_help)
        val coordinates = getCoordinate(context)
        findPreference<Preference>(R.string.athan.toString())?.isEnabled = coordinates != null
        findPreference<Preference>(R.string.athan.toString())?.setSummary(
            if (coordinates == null) R.string.athan_disabled_summary else R.string.empty
        )
        val selectedLocation = context.appPrefs.getString(PREF_SELECTED_LOCATION, null)
            ?.takeIf { it.isNotEmpty() && it != DEFAULT_CITY }
        findPreference<Preference>(coordinatesPref)?.isEnabled = selectedLocation == null
        findPreference<Preference>(coordinatesPref)?.summary = coordinates?.let {
            formatCoordinateISO6709(
                coordinates.latitude, coordinates.latitude,
                coordinates.elevation.takeIf { it != .0 }
            )
        }
    }
}
