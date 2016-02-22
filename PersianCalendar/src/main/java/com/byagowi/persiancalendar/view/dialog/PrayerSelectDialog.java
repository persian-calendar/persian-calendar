package com.byagowi.persiancalendar.view.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;

import com.byagowi.persiancalendar.R;

import java.util.Set;

public class PrayerSelectDialog extends PreferenceDialogFragmentCompat {

    public static PrayerSelectDialog newInstance(Preference preference) {
        PrayerSelectDialog fragment = new PrayerSelectDialog();
        Bundle bundle = new Bundle(1);
        bundle.putString("key", preference.getKey());
        fragment.setArguments(bundle);
        return fragment;
    }

    Set<String> prayers;
    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        PrayerSelectPreference prayerspref = (PrayerSelectPreference)getPreference();

        CharSequence[] entries = getResources().getStringArray(R.array.prayerTimeNames);
        final CharSequence[] entriesKeys = getResources().getStringArray(R.array.prayerTimeKeys);

        prayers = prayerspref.getPrayers();

        boolean[] checked = new boolean[entriesKeys.length];
        for (int i = 0; i < entriesKeys.length; ++i) {
            checked[i] = prayers.contains(entriesKeys[i]);
        }

        builder.setMultiChoiceItems(entries, checked, new DialogInterface.OnMultiChoiceClickListener() {
            public void onClick(DialogInterface dialog, int which,
                                boolean isChecked) {
                if (isChecked) {
                    prayers.add(entriesKeys[which].toString());
                } else {
                    prayers.remove(entriesKeys[which].toString());
                }
            }
        });
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            ((PrayerSelectPreference)getPreference()).setPrayers(prayers);
        }
    }
}
