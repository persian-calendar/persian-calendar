package com.byagowi.persiancalendar.ui.preferences.locationathan

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.text.TextUtils
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.byagowi.persiancalendar.Constants.*
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.di.dependencies.AppDependency
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency
import com.byagowi.persiancalendar.ui.MainActivityModel
import com.byagowi.persiancalendar.ui.preferences.locationathan.athan.AthanVolumeDialog
import com.byagowi.persiancalendar.ui.preferences.locationathan.athan.AthanVolumePreference
import com.byagowi.persiancalendar.ui.preferences.locationathan.athan.PrayerSelectDialog
import com.byagowi.persiancalendar.ui.preferences.locationathan.athan.PrayerSelectPreference
import com.byagowi.persiancalendar.ui.preferences.locationathan.location.GPSLocationDialog
import com.byagowi.persiancalendar.ui.preferences.locationathan.location.LocationPreference
import com.byagowi.persiancalendar.ui.preferences.locationathan.location.LocationPreferenceDialog
import com.byagowi.persiancalendar.ui.preferences.locationathan.numeric.NumericDialog
import com.byagowi.persiancalendar.ui.preferences.locationathan.numeric.NumericPreference
import com.byagowi.persiancalendar.utils.Utils
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class FragmentLocationAthan : PreferenceFragmentCompat() {

    @Inject
    lateinit var appDependency: AppDependency
    @Inject
    lateinit var mainActivityDependency: MainActivityDependency

    private var categoryAthan: Preference? = null

    private val defaultAthanName: String
        get() {
            val context = context ?: return ""
            return context.getString(R.string.default_athan_name)
        }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        AndroidSupportInjection.inject(this)

        addPreferencesFromResource(R.xml.preferences_location_athan)

        categoryAthan = findPreference(PREF_KEY_ATHAN)
        updateAthanPreferencesState()

        updateAthanPreferencesState()
        ViewModelProviders.of(mainActivityDependency.mainActivity).get(MainActivityModel::class.java)
                .preferenceUpdateHandler.observe(this, Observer { updateAthanPreferencesState() })

        putAthanNameOnSummary(appDependency.sharedPreferences
                .getString(PREF_ATHAN_NAME, defaultAthanName))
    }

    private fun updateAthanPreferencesState() {
        val context = context ?: return

        val locationEmpty = Utils.getCoordinate(context) == null
        categoryAthan?.isEnabled = !locationEmpty
        if (locationEmpty) {
            categoryAthan?.setSummary(R.string.athan_disabled_summary)
        } else {
            categoryAthan?.summary = ""
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference?) {
        var fragment: DialogFragment? = null
        when (preference) {
            is PrayerSelectPreference -> fragment = PrayerSelectDialog()
            is AthanVolumePreference -> fragment = AthanVolumeDialog()
            is LocationPreference -> fragment = LocationPreferenceDialog()
            is NumericPreference -> fragment = NumericDialog()
            else -> super.onDisplayPreferenceDialog(preference)
        }
        if (fragment != null) {
            val bundle = Bundle(1)
            bundle.putString("key", preference?.key)
            fragment.arguments = bundle
            fragment.setTargetFragment(this, 0)
            val fragmentManager = fragmentManager
            if (fragmentManager != null) {
                fragment.show(fragmentManager, fragment.javaClass.name)
            }
        }
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        val context = context ?: return true

        when (preference?.key) {
            "pref_key_ringtone" -> {
                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                        .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
                        .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                        .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                        .putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                                Settings.System.DEFAULT_NOTIFICATION_URI)
                val customAthanUri = Utils.getCustomAthanUri(context)
                if (customAthanUri != null) {
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, customAthanUri)
                }
                startActivityForResult(intent, ATHAN_RINGTONE_REQUEST_CODE)
                return true
            }
            "pref_key_ringtone_default" -> {
                appDependency.sharedPreferences.edit {
                    remove(PREF_ATHAN_URI)
                    remove(PREF_ATHAN_NAME)
                }
                Utils.createAndShowShortSnackbar(view, R.string.returned_to_default)
                putAthanNameOnSummary(defaultAthanName)
                return true
            }
            "pref_gps_location" -> {
                try {
                    val activity = mainActivityDependency.mainActivity

                    if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Utils.askForLocationPermission(activity)
                    } else {
                        GPSLocationDialog().show(childFragmentManager,
                                GPSLocationDialog::class.java.name)
                    }
                } catch (e: Exception) {
                    // Do whatever we were doing till now
                }

                return super.onPreferenceTreeClick(preference)
            }
            else -> return super.onPreferenceTreeClick(preference)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val context = context ?: return

        if (requestCode == ATHAN_RINGTONE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val uri: Parcelable? = data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                if (uri != null) {
                    var ringtoneTitle = RingtoneManager
                            .getRingtone(context, uri.toString().toUri())
                            .getTitle(context)
                    if (TextUtils.isEmpty(ringtoneTitle)) {
                        ringtoneTitle = ""
                    }

                    appDependency.sharedPreferences.edit {
                        putString(PREF_ATHAN_NAME, ringtoneTitle)
                        putString(PREF_ATHAN_URI, uri.toString())
                    }

                    Utils.createAndShowShortSnackbar(view, R.string.custom_notification_is_set)
                    putAthanNameOnSummary(ringtoneTitle)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun putAthanNameOnSummary(athanName: String?) {
        findPreference("pref_key_ringtone").summary = athanName
    }
}
