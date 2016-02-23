package com.byagowi.persiancalendar.view.fragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.preferences.AthanNumericDialog;
import com.byagowi.persiancalendar.view.preferences.AthanNumericPreference;
import com.byagowi.persiancalendar.view.preferences.AthanVolumeDialog;
import com.byagowi.persiancalendar.view.preferences.AthanVolumePreference;
import com.byagowi.persiancalendar.view.preferences.LocationPreference;
import com.byagowi.persiancalendar.view.preferences.LocationPreferenceDialog;
import com.byagowi.persiancalendar.view.preferences.PrayerSelectDialog;
import com.byagowi.persiancalendar.view.preferences.PrayerSelectPreference;
import com.byagowi.persiancalendar.view.preferences.ShapedListDialog;
import com.byagowi.persiancalendar.view.preferences.ShapedListPreference;

/**
 * Preference activity
 *
 * @author ebraminio
 */
public class ApplicationPreferenceFragment extends PreferenceFragmentCompat {
    private Preference categoryAthan;
    private Utils utils;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        utils = Utils.getInstance(getContext());
        utils.setActivityTitleAndSubtitle(getActivity(), getString(R.string.settings), "");

        addPreferencesFromResource(R.xml.preferences);

        categoryAthan = findPreference(Constants.PREF_KEY_ATHAN);
        updateAthanPreferencesState();

        instance = this;
    }

    public void updateAthanPreferencesState() {
        boolean locationEmpty = utils.getCoordinate() == null;
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
            fragment = PrayerSelectDialog.newInstance(preference);
        } else if (preference instanceof AthanVolumePreference) {
            fragment = AthanVolumeDialog.newInstance(preference);
        } else if (preference instanceof LocationPreference) {
            fragment = LocationPreferenceDialog.newInstance(preference);
        } else if (preference instanceof AthanNumericPreference) {
            fragment = AthanNumericDialog.newInstance(preference);
        } else if (preference instanceof ShapedListPreference) {
            fragment = ShapedListDialog.newInstance(preference);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }

        if (fragment != null) {
            fragment.setTargetFragment(this, 0);
            fragment.show(getFragmentManager(),
                    "android.support.v7.preference.PreferenceFragment.DIALOG");
        }
    }

    private static ApplicationPreferenceFragment instance;
    public static void update() {
        // Total hack but better than using broadcast on wrong places
        if (instance != null) {
            instance.updateAthanPreferencesState();
        }
    }
}
