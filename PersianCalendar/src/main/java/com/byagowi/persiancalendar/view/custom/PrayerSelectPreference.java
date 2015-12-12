package com.byagowi.persiancalendar.view.custom;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.support.v7.preference.DialogPreference;
import android.text.TextUtils;
import android.util.AttributeSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PrayerSelectPreference extends DialogPreference {
    public CharSequence[] mEntries;
    public CharSequence[] mEntryValues;
    public Set<String> mValues = new HashSet<>();
    public Set<String> mNewValues;
    public boolean mPreferenceChanged;

    public PrayerSelectPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        String alarmTimes = getPersistedString("");
        setValues(restorePersistedValue ?
                Arrays.asList(TextUtils.split(alarmTimes, ",")) : (Set<String>) defaultValue);
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

    public void close(boolean positiveResult) {

        if (positiveResult && mPreferenceChanged) {
            final Set<String> values = mNewValues;
            if (callChangeListener(values)) {
                persistString(TextUtils.join(",", values));
            }
        } else {
            String prefString = getPersistedString("");
            mNewValues = new HashSet<>();
            mNewValues.addAll(Arrays.asList(TextUtils.split(prefString, ",")));
        }
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
        } catch (Exception ignored) {
        }
    }

    private static class SavedState extends Preference.BaseSavedState {
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
        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
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
            final Set<String> values = new HashSet<>(n);

            source.readStringArray(strings);

            final int stringCount = strings.length;
            values.addAll(Arrays.asList(strings).subList(0, stringCount));

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
