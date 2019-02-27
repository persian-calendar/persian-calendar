package com.byagowi.persiancalendar.ui.preferences.locationathan.athan;

import com.byagowi.persiancalendar.R;

import java.util.Set;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceDialogFragmentCompat;

public class PrayerSelectDialog extends PreferenceDialogFragmentCompat {

    private Set<String> prayers;

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        PrayerSelectPreference prayerSelectPreference = (PrayerSelectPreference) getPreference();

        CharSequence[] entriesKeys = getResources().getStringArray(R.array.prayerTimeKeys);

        prayers = prayerSelectPreference.getPrayers();

        boolean[] checked = new boolean[entriesKeys.length];
        for (int i = 0; i < entriesKeys.length; ++i) {
            checked[i] = prayers.contains(entriesKeys[i]);
        }

        builder.setMultiChoiceItems(R.array.prayerTimeNames, checked, (dialog, which, isChecked) -> {
            if (isChecked) {
                prayers.add(entriesKeys[which].toString());
            } else {
                prayers.remove(entriesKeys[which].toString());
            }
        });
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            ((PrayerSelectPreference) getPreference()).setPrayers(prayers);
        }
    }
}
