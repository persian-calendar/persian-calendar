package com.byagowi.persiancalendar.view.custom;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;

public class DialogPref extends PreferenceDialogFragmentCompat {


    public static DialogPref newInstance(Preference preference) {
        DialogPref fragment = new DialogPref();
        Bundle bundle = new Bundle(1);
        bundle.putString("key", preference.getKey());
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {

    }

}
