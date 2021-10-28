package com.byagowi.persiancalendar.ui.preferences.locationathan

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.global.calculationMethod
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.ui.preferences.build
import com.byagowi.persiancalendar.ui.preferences.clickable
import com.byagowi.persiancalendar.ui.preferences.dialogTitle
import com.byagowi.persiancalendar.ui.preferences.locationathan.athan.showAthanGapDialog
import com.byagowi.persiancalendar.ui.preferences.locationathan.athan.showAthanSelectDialog
import com.byagowi.persiancalendar.ui.preferences.locationathan.athan.showAthanVolumeDialog
import com.byagowi.persiancalendar.ui.preferences.locationathan.athan.showPrayerSelectDialog
import com.byagowi.persiancalendar.ui.preferences.locationathan.athan.showPrayerSelectPreviewDialog
import com.byagowi.persiancalendar.ui.preferences.locationathan.location.showCoordinatesDialog
import com.byagowi.persiancalendar.ui.preferences.locationathan.location.showGPSLocationDialog
import com.byagowi.persiancalendar.ui.preferences.locationathan.location.showLocationPreferenceDialog
import com.byagowi.persiancalendar.ui.preferences.section
import com.byagowi.persiancalendar.ui.preferences.singleSelect
import com.byagowi.persiancalendar.ui.preferences.summary
import com.byagowi.persiancalendar.ui.preferences.switch
import com.byagowi.persiancalendar.ui.preferences.title
import com.byagowi.persiancalendar.utils.*
import com.google.android.material.snackbar.Snackbar
import io.github.persiancalendar.praytimes.CalculationMethod
import io.github.persiancalendar.praytimes.HighLatitudesMethod

class LocationAthanFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val defaultAthanName get() = context?.getString(R.string.default_athan) ?: ""

    private var coordinatesPreference: Preference? = null
    private var ringtonePreference: Preference? = null
    private var selectedLocationPreference: Preference? = null
    private var athanPreferenceCategory: PreferenceCategory? = null
    private var asrCalculationHanafiJuristicPreference: Preference? = null
    private var highLatitudesMethodPreference: Preference? = null
    private var ascendingAthanVolumePreference: Preference? = null
    private var athanVolumeDialogPreference: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val activity = activity ?: return
        preferenceScreen = preferenceManager.createPreferenceScreen(context).build {
            section(R.string.location) {
                clickable(onClick = { showGPSLocationDialog(activity, viewLifecycleOwner) }) {
                    title(R.string.gps_location)
                    summary(R.string.gps_location_help)
                }
                clickable(onClick = { showLocationPreferenceDialog(activity) }) {
                    title(R.string.location)
                    summary(R.string.location_help)
                    this@LocationAthanFragment.selectedLocationPreference = this
                }
                clickable(onClick = { showCoordinatesDialog(activity) }) {
                    title(R.string.coordination)
                    this@LocationAthanFragment.coordinatesPreference = this
                }
            }
            section(R.string.athan) {
                this@LocationAthanFragment.athanPreferenceCategory = this
                singleSelect(
                    PREF_PRAY_TIME_METHOD,
                    CalculationMethod.values().map { getString(it.titleStringId) },
                    CalculationMethod.values().map { it.name },
                    DEFAULT_PRAY_TIME_METHOD
                ) {
                    title(R.string.pray_methods)
                    dialogTitle(R.string.pray_methods_calculation)
                    summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
                }
                singleSelect(
                    PREF_HIGH_LATITUDES_METHOD,
                    HighLatitudesMethod.values().map { getString(it.titleStringId) },
                    HighLatitudesMethod.values().map { it.name },
                    DEFAULT_HIGH_LATITUDES_METHOD
                ) {
                    title(R.string.high_latitudes_method)
                    dialogTitle(R.string.high_latitudes_method)
                    summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
                    this@LocationAthanFragment.highLatitudesMethodPreference = this
                }
                switch(PREF_ASR_HANAFI_JURISTIC, language.isHanafiMajority) {
                    asrCalculationHanafiJuristicPreference = this
                    title(R.string.asr_hanafi_juristic)
                }
                clickable(onClick = { showAthanGapDialog(activity) }) {
                    title(R.string.athan_gap)
                    summary(R.string.athan_gap_summary)
                }
                clickable(onClick = { showPrayerSelectDialog(activity) }) {
                    title(R.string.athan_alarm)
                    summary(R.string.athan_alarm_summary)
                }
                clickable(onClick = { showAthanSelectDialog(activity, pickRingtone) }) {
                    title(R.string.custom_athan)
                    this@LocationAthanFragment.ringtonePreference = this
                }
                switch(PREF_NOTIFICATION_ATHAN, DEFAULT_NOTIFICATION_ATHAN) {
                    title(R.string.notification_athan)
                    summary(R.string.enable_notification_athan)
                    disableDependentsState = true
                }
                switch(PREF_ASCENDING_ATHAN_VOLUME, DEFAULT_ASCENDING_ATHAN_VOLUME) {
                    title(R.string.ascending_athan_volume)
                    summary(R.string.enable_ascending_athan_volume)
                    ascendingAthanVolumePreference = this
                }
                clickable(onClick = { showAthanVolumeDialog(activity) }) {
                    title(R.string.athan_volume)
                    summary(R.string.athan_volume_summary)
                    athanVolumeDialogPreference = this
                }
                clickable(onClick = { showPrayerSelectPreviewDialog(activity) }) {
                    title(R.string.preview)
                }
            }
        }

        onSharedPreferenceChanged(null, null)
        layoutInflater.context.appPrefs.registerOnSharedPreferenceChangeListener(this)
        updatePreferencesItems()
    }

    private class PickRingtoneContract : ActivityResultContract<Uri?, String?>() {
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
            if (resultCode == Activity.RESULT_OK)
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
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) =
        updatePreferencesItems()

    private fun updatePreferencesItems() {
        val context = context ?: return
        updateStoredPreference(context) // So vital to have this to have updated preferences here
        asrCalculationHanafiJuristicPreference?.isVisible = !calculationMethod.isJafari
        highLatitudesMethodPreference?.isVisible = enableHighLatitudesConfiguration

        val appPrefs = context.appPrefs

        val isNotificationAthan =
            appPrefs.getBoolean(PREF_NOTIFICATION_ATHAN, DEFAULT_NOTIFICATION_ATHAN)
        ascendingAthanVolumePreference?.isVisible = !isNotificationAthan
        athanVolumeDialogPreference?.isVisible = !isNotificationAthan &&
                !appPrefs.getBoolean(PREF_ASCENDING_ATHAN_VOLUME, DEFAULT_ASCENDING_ATHAN_VOLUME)

        ringtonePreference?.summary = appPrefs.getString(PREF_ATHAN_NAME, defaultAthanName)
        val cityName = appPrefs.cityName
        selectedLocationPreference?.summary = cityName ?: context.getString(R.string.location_help)
        athanPreferenceCategory?.isEnabled = coordinates != null
        athanPreferenceCategory?.setSummary(
            if (coordinates == null) R.string.athan_disabled_summary else R.string.empty
        )
        coordinatesPreference?.isEnabled = cityName == null
        coordinatesPreference?.summary = coordinates
            ?.run { formatCoordinateISO6709(latitude, longitude, elevation.takeIf { it != .0 }) }
    }
}
