package com.byagowi.persiancalendar.view.dialog;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.adapter.LocationAdapter;
import com.byagowi.persiancalendar.view.fragment.ApplicationPreferenceFragment;

import java.util.Locale;

/**
 * persian_calendar
 * Author: hamidsafdari22@gmail.com
 * Date: 1/17/16
 */
public class LocationPreferenceDialog extends PreferenceDialogFragmentCompat {
    private String selectedCity;

    public static LocationPreferenceDialog newInstance(Preference preference) {
        Bundle args = new Bundle(1);
        args.putString("key", preference.getKey());
        LocationPreferenceDialog fragment = new LocationPreferenceDialog();
        fragment.setArguments(args);
        return fragment;
    }

    public String getCurrentLangCode() {
        return Locale.getDefault().getLanguage();
    }

    @Override
    public LocationPreference getPreference() {
        return (LocationPreference) super.getPreference();
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        LocationPreference preference = getPreference();

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.preference_location, (ViewGroup) getView(), false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.RecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new LocationAdapter(this));

        builder.setPositiveButton("", null);
        builder.setNegativeButton("", null);
        builder.setView(view);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        getPreference().newValue = selectedCity;

        Intent intent = new Intent(ApplicationPreferenceFragment.INTENT_ACTION_PREFERENCES_CHANGED);
        intent.putExtra(ApplicationPreferenceFragment.PREF_KEY_LOCATION, selectedCity);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

        // we haven't included buttons so we always send true
        getPreference().close(true);
    }

    public void selectItem(String city) {
        selectedCity = city;
        dismiss();
    }
}
