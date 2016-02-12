package com.byagowi.persiancalendar.view.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;

import com.byagowi.persiancalendar.R;

import java.util.HashSet;
import java.util.Set;

public class PrayerSelectDialog extends PreferenceDialogFragmentCompat {

    public static PrayerSelectDialog newInstance(Preference preference) {
        PrayerSelectDialog fragment = new PrayerSelectDialog();
        Bundle bundle = new Bundle(1);
        bundle.putString("key", preference.getKey());
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((PrayerSelectPreference)getPreference()).mEntries = getResources()
                .getStringArray(R.array.prayerTimeNames);

        ((PrayerSelectPreference)getPreference()).mEntryValues = getResources()
                .getStringArray(R.array.prayerTimeKeys);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        ((PrayerSelectPreference)getPreference()).close(positiveResult);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        if (((PrayerSelectPreference)getPreference()).mEntries == null
                || ((PrayerSelectPreference)getPreference()).mEntryValues == null) {
            throw new IllegalStateException(
                    "MultiSelectListPreference requires an entries array and "
                            + "an entryValues array.");
        }

        if (((PrayerSelectPreference)getPreference()).mNewValues == null) {
            ((PrayerSelectPreference)getPreference()).mNewValues = new HashSet<>();
            ((PrayerSelectPreference)getPreference()).mNewValues.addAll(
                    ((PrayerSelectPreference)getPreference()).mValues);

            ((PrayerSelectPreference)getPreference()).mPreferenceChanged = false;
        }

        final boolean[] checkedItems = getSelectedItems(
                ((PrayerSelectPreference)getPreference()).mNewValues);

        builder.setMultiChoiceItems(
                ((PrayerSelectPreference)getPreference()).mEntries, checkedItems,
                new DialogInterface.OnMultiChoiceClickListener() {
                    public void onClick(DialogInterface dialog, int which,
                                        boolean isChecked) {
                        if (isChecked) {
                            ((PrayerSelectPreference)getPreference()).mPreferenceChanged
                                    |= ((PrayerSelectPreference)getPreference()).mNewValues
                                    .add(((PrayerSelectPreference)getPreference())
                                            .mEntryValues[which].toString());
                        } else {
                            ((PrayerSelectPreference)getPreference()).mPreferenceChanged
                                    |= ((PrayerSelectPreference)getPreference()).mNewValues
                                    .remove(((PrayerSelectPreference)getPreference())
                                            .mEntryValues[which].toString());
                        }
                    }
                });
    }

    private boolean[] getSelectedItems(final Set<String> values) {
        final CharSequence[] entries = ((PrayerSelectPreference)getPreference()).mEntryValues;
        final int entryCount = entries.length;
        boolean[] result = new boolean[entryCount];

        for (int i = 0; i < entryCount; i++) {
            result[i] = values.contains(entries[i].toString());
        }

        return result;
    }

    @Override
    public void setInitialSavedState(Fragment.SavedState state) {
        super.setInitialSavedState(state);
    }
}
