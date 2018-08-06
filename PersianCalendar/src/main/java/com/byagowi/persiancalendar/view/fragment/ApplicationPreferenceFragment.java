package com.byagowi.persiancalendar.view.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.preferences.AthanNumericDialog;
import com.byagowi.persiancalendar.view.preferences.AthanNumericPreference;
import com.byagowi.persiancalendar.view.preferences.AthanVolumeDialog;
import com.byagowi.persiancalendar.view.preferences.AthanVolumePreference;
import com.byagowi.persiancalendar.view.preferences.GPSLocationDialog;
import com.byagowi.persiancalendar.view.preferences.GPSLocationPreference;
import com.byagowi.persiancalendar.view.preferences.LocationPreference;
import com.byagowi.persiancalendar.view.preferences.LocationPreferenceDialog;
import com.byagowi.persiancalendar.view.preferences.PrayerSelectDialog;
import com.byagowi.persiancalendar.view.preferences.PrayerSelectPreference;

import androidx.fragment.app.DialogFragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import static android.app.Activity.RESULT_OK;
import static com.byagowi.persiancalendar.Constants.ATHAN_RINGTONE_REQUEST_CODE;
import static com.byagowi.persiancalendar.Constants.PREF_ATHAN_NAME;
import static com.byagowi.persiancalendar.Constants.PREF_ATHAN_URI;

/**
 * Preference activity
 *
 * @author ebraminio
 */
public class ApplicationPreferenceFragment extends PreferenceFragmentCompat {
    private Preference categoryAthan;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        Utils.setActivityTitleAndSubtitle(getActivity(), getString(R.string.settings), "");

        addPreferencesFromResource(R.xml.preferences);

        categoryAthan = findPreference(Constants.PREF_KEY_ATHAN);
        updateAthanPreferencesState();

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(preferenceUpdateReceiver,
                new IntentFilter(Constants.LOCAL_INTENT_UPDATE_PREFERENCE));

        putAthanNameOnSummary(PreferenceManager.getDefaultSharedPreferences(getContext())
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
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(preferenceUpdateReceiver);
        super.onDestroyView();
    }

    private void updateAthanPreferencesState() {
        boolean locationEmpty = Utils.getCoordinate(getContext()) == null;
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
        } else if (preference instanceof AthanNumericPreference) {
            fragment = new AthanNumericDialog();
        } else if (preference instanceof GPSLocationPreference) {
            fragment = new GPSLocationDialog();
        } else {
            super.onDisplayPreferenceDialog(preference);
        }

        if (fragment != null) {
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            fragment.setArguments(bundle);
            fragment.setTargetFragment(this, 0);
            try {
                fragment.show(getActivity().getSupportFragmentManager(), fragment.getClass().getName());
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        switch (preference.getKey()) {
            case "pref_key_ringtone":
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                        .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
                        .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                        .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                        .putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                                Settings.System.DEFAULT_NOTIFICATION_URI);
                Uri customAthanUri = Utils.getCustomAthanUri(getContext());
                if (customAthanUri != null) {
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, customAthanUri);
                }
                startActivityForResult(intent, ATHAN_RINGTONE_REQUEST_CODE);
                return true;
            case "pref_key_ringtone_default":
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(getContext()).edit();
                editor.remove(PREF_ATHAN_URI);
                editor.remove(PREF_ATHAN_NAME);
                editor.apply();
                Toast.makeText(getContext(), R.string.returned_to_default, Toast.LENGTH_SHORT).show();
                putAthanNameOnSummary(getDefaultAthanName());
                return true;
            default:
                return super.onPreferenceTreeClick(preference);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ATHAN_RINGTONE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Context context = getContext();
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
        return getContext().getString(R.string.default_athan_name);
    }

    private void putAthanNameOnSummary(String athanName) {
        findPreference("pref_key_ringtone").setSummary(athanName);
    }
}
