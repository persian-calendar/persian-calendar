package com.byagowi.persiancalendar.ui.settings.locationathan

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.forEach
import com.byagowi.persiancalendar.DEFAULT_ASCENDING_ATHAN_VOLUME
import com.byagowi.persiancalendar.DEFAULT_HIGH_LATITUDES_METHOD
import com.byagowi.persiancalendar.DEFAULT_NOTIFICATION_ATHAN
import com.byagowi.persiancalendar.DEFAULT_PRAY_TIME_METHOD
import com.byagowi.persiancalendar.PREF_ASCENDING_ATHAN_VOLUME
import com.byagowi.persiancalendar.PREF_ASR_HANAFI_JURISTIC
import com.byagowi.persiancalendar.PREF_ATHAN_NAME
import com.byagowi.persiancalendar.PREF_ATHAN_URI
import com.byagowi.persiancalendar.PREF_HIGH_LATITUDES_METHOD
import com.byagowi.persiancalendar.PREF_NOTIFICATION_ATHAN
import com.byagowi.persiancalendar.PREF_PRAY_TIME_METHOD
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.calculationMethod
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.ui.settings.build
import com.byagowi.persiancalendar.ui.settings.clickable
import com.byagowi.persiancalendar.ui.settings.locationathan.athan.showAthanGapDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.athan.showAthanSelectDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.athan.showAthanVolumeDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.athan.showPrayerSelectDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.athan.showPrayerSelectPreviewDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.location.showCoordinatesDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.location.showGPSLocationDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.location.showLocationPreferenceDialog
import com.byagowi.persiancalendar.ui.settings.section
import com.byagowi.persiancalendar.ui.settings.singleSelect
import com.byagowi.persiancalendar.ui.settings.summary
import com.byagowi.persiancalendar.ui.settings.switch
import com.byagowi.persiancalendar.ui.settings.title
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.cityName
import com.byagowi.persiancalendar.utils.enableHighLatitudesConfiguration
import com.byagowi.persiancalendar.utils.formatCoordinateISO6709
import com.byagowi.persiancalendar.utils.titleStringId
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
        preferenceScreen = preferenceManager.createPreferenceScreen(activity).build {
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
                clickable(onClick = { showCoordinatesDialog(activity, viewLifecycleOwner) }) {
                    title(R.string.coordination)
                    this@LocationAthanFragment.coordinatesPreference = this
                }
            }
            section(R.string.athan) {
                this@LocationAthanFragment.athanPreferenceCategory = this
                singleSelect(
                    PREF_PRAY_TIME_METHOD,
                    enumValues<CalculationMethod>().map { getString(it.titleStringId) },
                    enumValues<CalculationMethod>().map { it.name },
                    DEFAULT_PRAY_TIME_METHOD,
                    R.string.pray_methods_calculation
                ) { title(R.string.pray_methods) }
                singleSelect(
                    PREF_HIGH_LATITUDES_METHOD,
                    enumValues<HighLatitudesMethod>().map { getString(it.titleStringId) },
                    enumValues<HighLatitudesMethod>().map { it.name },
                    DEFAULT_HIGH_LATITUDES_METHOD,
                    R.string.high_latitudes_method
                ) {
                    title(R.string.high_latitudes_method)
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
                clickable(onClick = { showPrayerSelectPreviewDialog(activity) }) {
                    title(R.string.preview)
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
            }
        }

        val appPrefs = activity.appPrefs
        onSharedPreferenceChanged(appPrefs, null)
        appPrefs.registerOnSharedPreferenceChangeListener(this)
    }

    private class PickRingtoneContract : ActivityResultContract<Unit, String?>() {
        override fun createIntent(context: Context, input: Unit): Intent =
            Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
                .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                .putExtra(
                    RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                    Settings.System.DEFAULT_NOTIFICATION_URI
                )

        override fun parseResult(resultCode: Int, intent: Intent?): String? =
            if (resultCode == AppCompatActivity.RESULT_OK)
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        val context = context ?: return
        sharedPreferences ?: return
        updateStoredPreference(context) // So vital to have this to have updated preferences here
        athanPreferenceCategory?.forEach { it.isVisible = coordinates.value != null }
        asrCalculationHanafiJuristicPreference?.isVisible = !calculationMethod.isJafari
        highLatitudesMethodPreference?.isVisible = enableHighLatitudesConfiguration

        val isNotificationAthan =
            sharedPreferences.getBoolean(PREF_NOTIFICATION_ATHAN, DEFAULT_NOTIFICATION_ATHAN)
        ascendingAthanVolumePreference?.isVisible = !isNotificationAthan
        athanVolumeDialogPreference?.isVisible = !isNotificationAthan &&
                !sharedPreferences.getBoolean(
                    PREF_ASCENDING_ATHAN_VOLUME, DEFAULT_ASCENDING_ATHAN_VOLUME
                )

        ringtonePreference?.summary = sharedPreferences.getString(PREF_ATHAN_NAME, defaultAthanName)
        val cityName = sharedPreferences.cityName
        selectedLocationPreference?.summary = cityName ?: context.getString(R.string.location_help)
        athanPreferenceCategory?.setSummary(
            if (coordinates.value == null) R.string.athan_disabled_summary else R.string.empty
        )
        coordinatesPreference?.summary = coordinates.value
            ?.run { formatCoordinateISO6709(latitude, longitude, elevation.takeIf { it != .0 }) }
        athanPreferenceCategory?.forEach {
            it.isVisible = it.isVisible && coordinates.value != null
        }
    }
}
