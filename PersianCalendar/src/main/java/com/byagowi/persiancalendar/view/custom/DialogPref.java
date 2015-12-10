package com.byagowi.persiancalendar.view.custom;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;

import com.byagowi.persiancalendar.R;

import java.util.HashSet;
import java.util.Set;

public class DialogPref extends PreferenceDialogFragmentCompat {

    public static DialogPref newInstance(Preference preference) {

//        mEntries = context.getResources().getStringArray(R.array.prayerTimeNames);
//        mEntryValues = context.getResources().getStringArray(R.array.prayerTimeKeys);

        DialogPref fragment = new DialogPref();
        Bundle bundle = new Bundle(1);
        bundle.putString("key", preference.getKey());
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Dialogpref1)getPreference()).mEntries = getResources().getStringArray(R.array.prayerTimeNames);
        ((Dialogpref1)getPreference()).mEntryValues = getResources().getStringArray(R.array.prayerTimeKeys);

    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        ((Dialogpref1)getPreference()).close(positiveResult);
//        if (positiveResult && mPreferenceChanged) {
//            final Set<String> values = mNewValues;
//            if (getPreference().callChangeListener(values)) {
//                getPreference().persistString(TextUtils.join(",", values));
//            }
//        } else {
//            String prefString = getPreference().getPersistedString("");
//            mNewValues = new HashSet<>();
//            mNewValues.addAll(Arrays.asList(TextUtils.split(prefString, ",")));
//        }

    }


    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        if (((Dialogpref1)getPreference()).mEntries == null || ((Dialogpref1)getPreference()).mEntryValues == null) {
            throw new IllegalStateException(
                    "MultiSelectListPreference requires an entries array and "
                            + "an entryValues array.");
        }

        if (((Dialogpref1)getPreference()).mNewValues == null) {
            ((Dialogpref1)getPreference()).mNewValues = new HashSet<>();
            ((Dialogpref1)getPreference()).mNewValues.addAll(((Dialogpref1)getPreference()).mValues);
            ((Dialogpref1)getPreference()).mPreferenceChanged = false;
        }

        final boolean[] checkedItems = getSelectedItems(((Dialogpref1)getPreference()).mNewValues);
        builder.setMultiChoiceItems(((Dialogpref1)getPreference()).mEntries, checkedItems,
                new DialogInterface.OnMultiChoiceClickListener() {
                    public void onClick(DialogInterface dialog, int which,
                                        boolean isChecked) {
                        if (isChecked) {
                            ((Dialogpref1)getPreference()).mPreferenceChanged |= ((Dialogpref1)getPreference()).mNewValues
                                    .add(((Dialogpref1)getPreference()).mEntryValues[which].toString());
                        } else {
                            ((Dialogpref1)getPreference()).mPreferenceChanged |= ((Dialogpref1)getPreference()).mNewValues
                                    .remove(((Dialogpref1)getPreference()).mEntryValues[which].toString());
                        }
                    }
                });
    }

    private boolean[] getSelectedItems(final Set<String> values) {
        final CharSequence[] entries = ((Dialogpref1)getPreference()).mEntryValues;
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
