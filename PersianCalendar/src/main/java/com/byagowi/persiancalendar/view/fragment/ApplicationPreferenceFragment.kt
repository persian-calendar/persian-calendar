package com.byagowi.persiancalendar.view.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.media.RingtoneManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.preference.PreferenceManager
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast

import com.byagowi.persiancalendar.Constants
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.util.UIUtils
import com.byagowi.persiancalendar.util.Utils
import com.byagowi.persiancalendar.view.dialog.GPSDiagnosticDialog
import com.byagowi.persiancalendar.view.preferences.AthanNumericDialog
import com.byagowi.persiancalendar.view.preferences.AthanNumericPreference
import com.byagowi.persiancalendar.view.preferences.AthanVolumeDialog
import com.byagowi.persiancalendar.view.preferences.AthanVolumePreference
import com.byagowi.persiancalendar.view.preferences.GPSLocationDialog
import com.byagowi.persiancalendar.view.preferences.GPSLocationPreference
import com.byagowi.persiancalendar.view.preferences.LocationPreference
import com.byagowi.persiancalendar.view.preferences.LocationPreferenceDialog
import com.byagowi.persiancalendar.view.preferences.PrayerSelectDialog
import com.byagowi.persiancalendar.view.preferences.PrayerSelectPreference

import androidx.fragment.app.DialogFragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

import android.app.Activity.RESULT_OK
import com.byagowi.persiancalendar.Constants.ATHAN_RINGTONE_REQUEST_CODE
import com.byagowi.persiancalendar.Constants.PREF_ATHAN_NAME
import com.byagowi.persiancalendar.Constants.PREF_ATHAN_URI

/**
 * Preference activity
 *
 * @author ebraminio
 */
class ApplicationPreferenceFragment : PreferenceFragmentCompat() {
  private lateinit var categoryAthan: Preference

  private val preferenceUpdateReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      updateAthanPreferencesState()
    }
  }

  private val defaultAthanName: String
    get() = context?.getString(R.string.default_athan_name) ?: ""

  override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
    val localActivity = activity ?: return
    UIUtils.setActivityTitleAndSubtitle(localActivity, getString(R.string.settings), "")

    addPreferencesFromResource(R.xml.preferences)

    categoryAthan = findPreference(Constants.PREF_KEY_ATHAN)
    updateAthanPreferencesState()

    val ctx = context
    if (ctx != null)
      LocalBroadcastManager.getInstance(ctx).registerReceiver(preferenceUpdateReceiver,
          IntentFilter(Constants.LOCAL_INTENT_UPDATE_PREFERENCE))

    putAthanNameOnSummary(PreferenceManager.getDefaultSharedPreferences(context)
        .getString(PREF_ATHAN_NAME, defaultAthanName))
  }

  override fun onDestroyView() {
    val ctx = context
    if (ctx != null) {
      LocalBroadcastManager.getInstance(ctx).unregisterReceiver(preferenceUpdateReceiver)
    }
    super.onDestroyView()
  }

  private fun updateAthanPreferencesState() {
    val ctx = context ?: return
    val locationEmpty = Utils.getCoordinate(ctx) == null
    categoryAthan.isEnabled = !locationEmpty
    if (locationEmpty) {
      categoryAthan.setSummary(R.string.athan_disabled_summary)
    } else {
      categoryAthan.summary = ""
    }
  }

  override fun onDisplayPreferenceDialog(preference: Preference) {
    var fragment: DialogFragment? = null
    if (preference is PrayerSelectPreference) {
      fragment = PrayerSelectDialog()
    } else if (preference is AthanVolumePreference) {
      fragment = AthanVolumeDialog()
    } else if (preference is LocationPreference) {
      fragment = LocationPreferenceDialog()
    } else if (preference is AthanNumericPreference) {
      fragment = AthanNumericDialog()
    } else if (preference is GPSLocationPreference) {
      //check whether gps provider and network providers are enabled or not
      val ctx = context
      val localActivity = activity
      if (ctx != null && localActivity != null) {
        val gps = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        val connectivityManager = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val info: NetworkInfo? = connectivityManager?.activeNetworkInfo

        var gpsEnabled = false
        try {
          gpsEnabled = gps?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
        } catch (ignored: Exception) {
        }

        if (!gpsEnabled || info == null) {
          // Custom Android Alert Dialog Title
          val frag = GPSDiagnosticDialog()
          frag.show(localActivity.supportFragmentManager, "GPSDiagnosticDialog")
        } else {
          fragment = GPSLocationDialog()
        }
      }
    } else {
      super.onDisplayPreferenceDialog(preference)
    }

    val localActivity = activity
    if (fragment != null && localActivity != null) {
      val bundle = Bundle(1)
      bundle.putString("key", preference.key)
      fragment.arguments = bundle
      fragment.setTargetFragment(this, 0)
      try {
        fragment.show(localActivity.getSupportFragmentManager(), fragment.javaClass.name)
      } catch (e: NullPointerException) {
        e.printStackTrace()
      }

    }
  }

  override fun onPreferenceTreeClick(preference: Preference): Boolean {
    when (preference.key) {
      "pref_key_ringtone" -> {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
            .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
            .putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                Settings.System.DEFAULT_NOTIFICATION_URI)
        val ctx = context
        if (ctx != null) {
          val customAthanUri = Utils.getCustomAthanUri(ctx)
          if (customAthanUri != null) {
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, customAthanUri)
          }
        }
        startActivityForResult(intent, ATHAN_RINGTONE_REQUEST_CODE)
        return true
      }
      "pref_key_ringtone_default" -> {
        val editor = PreferenceManager
            .getDefaultSharedPreferences(context).edit()
        editor.remove(PREF_ATHAN_URI)
        editor.remove(PREF_ATHAN_NAME)
        editor.apply()
        Toast.makeText(context, R.string.returned_to_default, Toast.LENGTH_SHORT).show()
        putAthanNameOnSummary(defaultAthanName)
        return true
      }
      else -> return super.onPreferenceTreeClick(preference)
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == ATHAN_RINGTONE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
      val context = context
      val uri = data.getParcelableExtra<Parcelable>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
      if (uri != null) {
        val editor = PreferenceManager
            .getDefaultSharedPreferences(context).edit()

        var ringtoneTitle = RingtoneManager
            .getRingtone(context, Uri.parse(uri.toString()))
            .getTitle(context)
        if (TextUtils.isEmpty(ringtoneTitle)) {
          ringtoneTitle = ""
        }
        editor.putString(PREF_ATHAN_NAME, ringtoneTitle)
        editor.putString(PREF_ATHAN_URI, uri.toString())
        editor.apply()
        Toast.makeText(context, R.string.custom_notification_is_set,
            Toast.LENGTH_SHORT).show()
        putAthanNameOnSummary(ringtoneTitle)
      }
    }
    super.onActivityResult(requestCode, resultCode, data)
  }

  private fun putAthanNameOnSummary(athanName: String?) {
    findPreference("pref_key_ringtone").summary = athanName
  }
}
