package com.byagowi.persiancalendar.ui.preferences.locationathan

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.generated.citiesStore
import com.byagowi.persiancalendar.ui.preferences.locationathan.athan.showAthanGapDialog
import com.byagowi.persiancalendar.ui.preferences.locationathan.athan.showAthanVolumeDialog
import com.byagowi.persiancalendar.ui.preferences.locationathan.athan.showPrayerSelectDialog
import com.byagowi.persiancalendar.ui.preferences.locationathan.location.GPSLocationDialog
import com.byagowi.persiancalendar.ui.preferences.locationathan.location.showCoordinatesDialog
import com.byagowi.persiancalendar.ui.preferences.locationathan.location.showLocationPreferenceDialog
import com.byagowi.persiancalendar.utils.*
import com.google.android.material.snackbar.Snackbar

class LocationAthanFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private var categoryAthan: Preference? = null

    private val defaultAthanName: String
        get() {
            val context = context ?: return ""
            return context.getString(R.string.default_athan_name)
        }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        addPreferencesFromResource(R.xml.preferences_location_athan)

        findPreference<ListPreference>(PREF_PRAY_TIME_METHOD)?.summaryProvider =
            ListPreference.SimpleSummaryProvider.getInstance()

        categoryAthan = findPreference(PREF_KEY_ATHAN)

        onSharedPreferenceChanged(null, null)
        activity?.appPrefs?.registerOnSharedPreferenceChangeListener(this)

        putAthanNameOnSummary(context?.appPrefs?.getString(PREF_ATHAN_NAME, defaultAthanName))
        updateLocationOnSummary()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        val locationEmpty = getCoordinate(activity ?: return) == null
        categoryAthan?.isEnabled = !locationEmpty
        categoryAthan?.setSummary(if (locationEmpty) R.string.athan_disabled_summary else R.string.empty)
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

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        val context = context ?: return super.onPreferenceTreeClick(preference)
        return when (preference?.key) {
            "pref_key_ringtone" -> {
                runCatching { pickRingtone.launch(getCustomAthanUri(context)) }
                    .onFailure(logException)
                true
            }
            "pref_key_ringtone_default" -> {
                context.appPrefs.edit {
                    remove(PREF_ATHAN_URI)
                    remove(PREF_ATHAN_NAME)
                }
                view?.let {
                    Snackbar.make(it, R.string.returned_to_default, Snackbar.LENGTH_SHORT).show()
                }
                putAthanNameOnSummary(defaultAthanName)
                true
            }
            "pref_gps_location" -> {
                runCatching {
                    if (ActivityCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) askForLocationPermission(activity) else {
                        GPSLocationDialog().show(
                            childFragmentManager, GPSLocationDialog::class.java.name
                        )
                        // TODO: #updateCoordination() on its callback
                    }
                }.onFailure(logException)
                true
            }
            PREF_SELECTED_LOCATION -> showLocationPreferenceDialog(::updateLocationOnSummary)
            "Coordination" -> showCoordinatesDialog(::updateCoordination)
            PREF_ATHAN_VOLUME -> showAthanVolumeDialog()
            PREF_ATHAN_ALARM -> showPrayerSelectDialog()
            PREF_ATHAN_GAP -> showAthanGapDialog()
            else -> super.onPreferenceTreeClick(preference)
        }
    }

    private fun updateCoordination() {
        val coordinates = getCoordinate(context ?: return)
        findPreference<Preference>("Coordination")?.summary =
            if (coordinates == null) ""
            else formatCoordinateISO6709(
                coordinates.latitude, coordinates.longitude, coordinates.elevation
            )
    }

    private fun putAthanNameOnSummary(athanName: String?) {
        findPreference<Preference>("pref_key_ringtone")?.summary = athanName
    }

    private fun updateLocationOnSummary() {
        val context = context ?: return
        val selected = context.appPrefs.getString(PREF_SELECTED_LOCATION, null) ?: DEFAULT_CITY
        findPreference<Preference>(PREF_SELECTED_LOCATION)?.summary =
            (if (selected == DEFAULT_CITY) null
            else citiesStore[selected]?.let {
                when (language) {
                    LANG_EN_IR, LANG_EN_US, LANG_JA -> it.en
                    LANG_CKB -> it.ckb
                    LANG_AR -> it.ar
                    else -> it.fa
                }
            }) ?: context.getString(R.string.location_help)
        updateCoordination()
    }
}
