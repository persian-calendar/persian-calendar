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
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.ui.preferences.build
import com.byagowi.persiancalendar.ui.preferences.clickable
import com.byagowi.persiancalendar.ui.preferences.dialogTitle
import com.byagowi.persiancalendar.ui.preferences.locationathan.athan.showAthanGapDialog
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

class LocationAthanFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val defaultAthanName get() = context?.getString(R.string.default_athan_name) ?: ""

    private var coordinatesPreference: Preference? = null
    private var ringtonePreference: Preference? = null
    private var selectedLocationPreference: Preference? = null
    private var athanPreferenceCategory: PreferenceCategory? = null
    private var asrCalculationHanafiJuristic: Preference? = null
    private var defaultAthanPreference: Preference? = null

    // Thee same order as http://praytimes.org/code/v2/js/examples/monthly.htm
    private val prayTimeCalculationMethods = listOf(
        CalculationMethod.MWL to R.string.method_mwl,
        CalculationMethod.ISNA to R.string.method_isna,
        CalculationMethod.Egypt to R.string.method_egypt,
        CalculationMethod.Makkah to R.string.method_makkah,
        CalculationMethod.Karachi to R.string.method_karachi,
        CalculationMethod.Jafari to R.string.method_jafari,
        CalculationMethod.Tehran to R.string.method_tehran
    )

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val handler = Handler(Looper.getMainLooper()) // for deferred dependency wire ups
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
                    prayTimeCalculationMethods.map { (_, title) -> getString(title) },
                    prayTimeCalculationMethods.map { (method, _) -> method.name },
                    DEFAULT_PRAY_TIME_METHOD
                ) {
                    title(R.string.pray_methods)
                    dialogTitle(R.string.pray_methods_calculation)
                    summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
                }
                switch(PREF_ASR_HANAFI_JURISTIC, language.isHanafiMajority) {
                    asrCalculationHanafiJuristic = this
                    title(R.string.asr_hanafi_juristic)
                    isVisible = !calculationMethod.isShia
                }
                clickable(onClick = { showAthanGapDialog(activity) }) {
                    title(R.string.athan_gap)
                    summary(R.string.athan_gap_summary)
                }
                clickable(onClick = { showPrayerSelectDialog(activity) }) {
                    title(R.string.athan_alarm)
                    summary(R.string.athan_alarm_summary)
                }
                switch(PREF_NOTIFICATION_ATHAN, false) {
                    title(R.string.notification_athan)
                    summary(R.string.enable_notification_athan)
                    disableDependentsState = true
                }
                clickable(onClick = {
                    runCatching { pickRingtone.launch(getCustomAthanUri(layoutInflater.context)) }
                        .onFailure(logException).getOrNull()
                }) {
                    title(R.string.custom_athan)
                    this@LocationAthanFragment.ringtonePreference = this
                    handler.post { dependency = PREF_NOTIFICATION_ATHAN }
                }
                clickable(onClick = ::restoreDefaultAthan) {
                    defaultAthanPreference = this
                    title(R.string.default_athan)
                    summary(R.string.default_athan_summary)
                    handler.post { dependency = PREF_NOTIFICATION_ATHAN }
                    isVisible = PREF_ATHAN_URI in activity.appPrefs
                }
                switch(PREF_ASCENDING_ATHAN_VOLUME, false) {
                    title(R.string.ascending_athan_volume)
                    summary(R.string.enable_ascending_athan_volume)
                    disableDependentsState = true
                    handler.post { dependency = PREF_NOTIFICATION_ATHAN }
                }
                clickable(onClick = { showAthanVolumeDialog(activity) }) {
                    title(R.string.athan_volume)
                    summary(R.string.athan_volume_summary)
                    handler.post { dependency = PREF_ASCENDING_ATHAN_VOLUME }
                }
                clickable(onClick = { showPrayerSelectPreviewDialog(activity) }) {
                    title(R.string.preview)
                }
            }
        }

        onSharedPreferenceChanged(null, null)
        layoutInflater.context.appPrefs.registerOnSharedPreferenceChangeListener(this)

        updateAthanSummaries(activity.appPrefs.getString(PREF_ATHAN_NAME, defaultAthanName))
        updateLocationOnSummaries()
    }

    private fun restoreDefaultAthan() {
        val activity = activity ?: return
        AlertDialog.Builder(activity)
            .setTitle(R.string.default_athan_summary)
            .setMessage(R.string.are_you_sure)
            .setPositiveButton(R.string.accept) { _, _ ->
                activity.appPrefs.edit {
                    remove(PREF_ATHAN_URI)
                    remove(PREF_ATHAN_NAME)
                }
                view?.let { v ->
                    Snackbar.make(
                        v,
                        R.string.returned_to_default,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                updateAthanSummaries(defaultAthanName)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
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
        updateAthanSummaries(ringtoneTitle)
    }

    private fun updateAthanSummaries(athanName: String?) {
        ringtonePreference?.summary = athanName
        defaultAthanPreference?.isVisible = PREF_ATHAN_URI in (context ?: return).appPrefs
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        updateLocationOnSummaries()
        asrCalculationHanafiJuristic?.isVisible = !calculationMethod.isShia
    }

    private fun updateLocationOnSummaries() {
        val context = context ?: return
        updateStoredPreference(context) // So vital to have this to have updated preferences here
        val cityName = context.appPrefs.cityName
        selectedLocationPreference?.summary = cityName ?: context.getString(R.string.location_help)
        athanPreferenceCategory?.isEnabled = coordinates != null
        athanPreferenceCategory?.setSummary(
            if (coordinates == null) R.string.athan_disabled_summary else R.string.empty
        )
        val selectedLocation = context.appPrefs.getString(PREF_SELECTED_LOCATION, null)
            ?.takeIf { it.isNotEmpty() && it != DEFAULT_CITY }
        coordinatesPreference?.isEnabled = selectedLocation == null
        coordinatesPreference?.summary = coordinates
            ?.run { formatCoordinateISO6709(latitude, longitude, elevation.takeIf { it != .0 }) }
    }
}
