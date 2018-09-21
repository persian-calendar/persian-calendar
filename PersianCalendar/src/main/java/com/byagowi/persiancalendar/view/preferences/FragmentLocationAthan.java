package com.byagowi.persiancalendar.view.preferences;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.util.UIUtils;
import com.byagowi.persiancalendar.util.Utils;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import static android.app.Activity.RESULT_OK;
import static com.byagowi.persiancalendar.Constants.ATHAN_RINGTONE_REQUEST_CODE;
import static com.byagowi.persiancalendar.Constants.PREF_ATHAN_NAME;
import static com.byagowi.persiancalendar.Constants.PREF_ATHAN_URI;

public class FragmentLocationAthan extends PreferenceFragmentCompat {
    private Preference categoryAthan;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        UIUtils.setActivityTitleAndSubtitle(getActivity(), getString(R.string.settings), "");

        addPreferencesFromResource(R.xml.preferences_location_athan);

        categoryAthan = findPreference(Constants.PREF_KEY_ATHAN);
        updateAthanPreferencesState();

        Context context = getContext();
        if (context == null) return;

        LocalBroadcastManager.getInstance(context).registerReceiver(preferenceUpdateReceiver,
                new IntentFilter(Constants.LOCAL_INTENT_UPDATE_PREFERENCE));

        putAthanNameOnSummary(PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_ATHAN_NAME, getDefaultAthanName()));
    }

    private BroadcastReceiver preferenceUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateAthanPreferencesState();
        }
    };

    @Override
    public void onDestroyView() {
        Context context = getContext();
        if (context != null)
            LocalBroadcastManager.getInstance(context).unregisterReceiver(preferenceUpdateReceiver);
        super.onDestroyView();
    }

    private void updateAthanPreferencesState() {
        Context context = getContext();
        if (context == null) return;

        boolean locationEmpty = Utils.getCoordinate(context) == null;
        categoryAthan.setEnabled(!locationEmpty);
        if (locationEmpty) {
            categoryAthan.setSummary(R.string.athan_disabled_summary);
        } else {
            categoryAthan.setSummary("");
        }
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment fragment = null;
        if (preference instanceof PrayerSelectPreference) {
            fragment = new PrayerSelectDialog();
        } else if (preference instanceof AthanVolumePreference) {
            fragment = new AthanVolumeDialog();
        } else if (preference instanceof LocationPreference) {
            fragment = new LocationPreferenceDialog();
        } else if (preference instanceof NumericPreference) {
            fragment = new NumericDialog();
        } else if (preference instanceof GPSLocationPreference) {
            //check whether gps provider and network providers are enabled or not
            FragmentActivity activity = getActivity();
            if (activity == null) return;

            try {
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    UIUtils.askForLocationPermission(activity);
                    return;
                } else {
                    fragment = new GPSLocationDialog();
                }
            } catch (Exception e) {
                // Do whatever we were doing till now
            }

        } else {
            super.onDisplayPreferenceDialog(preference);
        }

        if (fragment != null) {
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            fragment.setArguments(bundle);
            fragment.setTargetFragment(this, 0);
            try {
                fragment.show(getFragmentManager(), fragment.getClass().getName());
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        Context context = getContext();
        if (context == null) return true;

        switch (preference.getKey()) {
            case "pref_key_ringtone":
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                        .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
                        .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                        .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                        .putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                                Settings.System.DEFAULT_NOTIFICATION_URI);
                Uri customAthanUri = Utils.getCustomAthanUri(context);
                if (customAthanUri != null) {
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, customAthanUri);
                }
                startActivityForResult(intent, ATHAN_RINGTONE_REQUEST_CODE);
                return true;
            case "pref_key_ringtone_default":
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(context).edit();
                editor.remove(PREF_ATHAN_URI);
                editor.remove(PREF_ATHAN_NAME);
                editor.apply();
                Toast.makeText(context, R.string.returned_to_default, Toast.LENGTH_SHORT).show();
                putAthanNameOnSummary(getDefaultAthanName());
                return true;
            default:
                return super.onPreferenceTreeClick(preference);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Context context = getContext();
        if (context == null) return;

        if (requestCode == ATHAN_RINGTONE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Parcelable uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                if (uri != null) {
                    SharedPreferences.Editor editor = PreferenceManager
                            .getDefaultSharedPreferences(context).edit();

                    String ringtoneTitle = RingtoneManager
                            .getRingtone(context, Uri.parse(uri.toString()))
                            .getTitle(context);
                    if (TextUtils.isEmpty(ringtoneTitle)) {
                        ringtoneTitle = "";
                    }
                    editor.putString(PREF_ATHAN_NAME, ringtoneTitle);
                    editor.putString(PREF_ATHAN_URI, uri.toString());
                    editor.apply();
                    Toast.makeText(context, R.string.custom_notification_is_set,
                            Toast.LENGTH_SHORT).show();
                    putAthanNameOnSummary(ringtoneTitle);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String getDefaultAthanName() {
        Context context = getContext();
        if (context == null) return "";

        return context.getString(R.string.default_athan_name);
    }

    private void putAthanNameOnSummary(String athanName) {
        findPreference("pref_key_ringtone").setSummary(athanName);
    }
}
