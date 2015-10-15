package com.byagowi.persiancalendar.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import com.byagowi.persiancalendar.R;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PrayerSelectPreference extends DialogPreference {
    private static final String TAG = "PrayerSelectPreference";

    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private Set<String> mValues = new HashSet<String>();
    private Set<String> mNewValues;
    private boolean mPreferenceChanged;

    public PrayerSelectPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        mEntries = context.getResources().getStringArray(R.array.prayerTimeNames);
        mEntryValues = context.getResources().getStringArray(R.array.prayerTimeKeys);
    }

    public PrayerSelectPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        if (mEntries == null || mEntryValues == null) {
            throw new IllegalStateException(
                    "MultiSelectListPreference requires an entries array and "
                            + "an entryValues array.");
        }

        if (mNewValues == null) {
            mNewValues = new HashSet<String>();
            mNewValues.addAll(mValues);
            mPreferenceChanged = false;
        }

        final boolean[] checkedItems = getSelectedItems(mNewValues);
        builder.setMultiChoiceItems(mEntries, checkedItems,
                new DialogInterface.OnMultiChoiceClickListener() {
                    public void onClick(DialogInterface dialog, int which,
                                        boolean isChecked) {
                        if (isChecked) {
                            mPreferenceChanged |= mNewValues
                                    .add(mEntryValues[which].toString());
                        } else {
                            mPreferenceChanged |= mNewValues
                                    .remove(mEntryValues[which].toString());
                        }
                    }
                });
    }

    private boolean[] getSelectedItems(final Set<String> values) {
        final CharSequence[] entries = mEntryValues;
        final int entryCount = entries.length;
        boolean[] result = new boolean[entryCount];

        for (int i = 0; i < entryCount; i++) {
            result[i] = values.contains(entries[i].toString());
        }

        return result;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult && mPreferenceChanged) {
            final Set<String> values = mNewValues;
            if (callChangeListener(values)) {
                // persist string should be here
                persistString(TextUtils.join(",", values));
            }
        } else {
            String prefString = getPersistedString("");
            mNewValues = new HashSet<>();
            mNewValues.addAll(Arrays.asList(TextUtils.split(prefString, ",")));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String alarmTimes = getPersistedString("");
        setValues(restoreValue ? Arrays.asList(TextUtils.split(alarmTimes, ",")) : (Set<String>) defaultValue);
    }

    public void setValues(Collection<String> values) {
        mValues.addAll(values);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        final SavedState myState = new SavedState(superState);
        myState.values = mValues;
        myState.newValues = mNewValues;
        myState.preferenceChanged = mPreferenceChanged;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        try {
            if (state instanceof SavedState) {
                final SavedState myState = (SavedState) state;
                if (myState.values != null) {
                    mValues = myState.values;
                }
                if (myState.newValues != null) {
                    mNewValues = myState.newValues;
                }
                mPreferenceChanged = myState.preferenceChanged;

                super.onRestoreInstanceState(myState.getSuperState());
            } else {
                super.onRestoreInstanceState(state);
            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    private static class SavedState extends BaseSavedState {
        public Set<String> values;
        public Set<String> newValues;
        public boolean preferenceChanged;

        public SavedState(Parcel source) {
            super(source);
            values = readStringSet(source);
            newValues = readStringSet(source);
            preferenceChanged = readBoolean(source);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);

            writeStringSet(dest, values);
            writeStringSet(dest, newValues);
            writeBoolean(dest, preferenceChanged);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        private static Set<String> readStringSet(Parcel source) {
            final int n = source.readInt();
            final String[] strings = new String[n];
            final Set<String> values = new HashSet<String>(n);

            source.readStringArray(strings);

            final int stringCount = strings.length;
            for (int i = 0; i < stringCount; i++) {
                values.add(strings[i]);
            }

            return values;
        }

        private static void writeStringSet(Parcel dest, Set<String> values) {
            final int n = (values == null) ? 0 : values.size();
            final String[] arrayValues = new String[n];

            if (values != null) {
                values.toArray(arrayValues);
            }

            dest.writeInt(n);
            dest.writeStringArray(arrayValues);
        }

        private static boolean readBoolean(Parcel source) {
            return source.readInt() != 0;
        }

        private static void writeBoolean(Parcel dest, boolean value) {
            dest.writeInt((value) ? 1 : 0);
        }
    }
}
