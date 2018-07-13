package com.byagowi.persiancalendar.view.preferences;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceDialogFragmentCompat;

import com.byagowi.persiancalendar.R;

import java.util.Set;

public class PrayerSelectDialog extends PreferenceDialogFragmentCompat {

    Set<String> prayers;

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        PrayerSelectPreference prayerspref = (PrayerSelectPreference) getPreference();

        CharSequence[] entriesKeys = getResources().getStringArray(R.array.prayerTimeKeys);

        prayers = prayerspref.getPrayers();

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
