package com.byagowi.persiancalendar.view.fragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.Utils;
import com.byagowi.persiancalendar.view.dialog.AthanNumericDialog;
import com.byagowi.persiancalendar.view.dialog.AthanNumericPreference;
import com.byagowi.persiancalendar.view.dialog.AthanVolumeDialog;
import com.byagowi.persiancalendar.view.dialog.AthanVolumePreference;
import com.byagowi.persiancalendar.view.dialog.LocationPreference;
import com.byagowi.persiancalendar.view.dialog.LocationPreferenceDialog;
import com.byagowi.persiancalendar.view.dialog.PrayerSelectDialog;
import com.byagowi.persiancalendar.view.dialog.PrayerSelectPreference;
import com.byagowi.persiancalendar.view.dialog.ShapedListDialog;
import com.byagowi.persiancalendar.view.dialog.ShapedListPreference;

/**
 * Preference activity
 *
 * @author ebraminio
 */
public class ApplicationPreferenceFragment extends PreferenceFragmentCompat {
    public static final String INTENT_ACTION_PREFERENCES_CHANGED = "com.byagowi.persiancalendar.intent.action.PREFERENCES_CHANGED";

    public static final String PREF_KEY_ATHAN = "Athan";

    //    private final Utils utils = Utils.getInstance();
    private Preference categoryAthan;
    Utils utils;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        utils = Utils.getInstance(getContext());
        utils.setActivityTitleAndSubtitle(getActivity(), getString(R.string.settings), "");

        addPreferencesFromResource(R.xml.preferences);

        categoryAthan = findPreference(PREF_KEY_ATHAN);
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
