package com.byagowi.persiancalendar.view.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.Utils;
import com.byagowi.persiancalendar.view.fragment.ApplicationPreferenceFragment;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
        preference.listLocations = (ListView) view.findViewById(R.id.list);

        final CityNameAdapter locationsAdapter = new CityNameAdapter(getContext(), R.layout.list_item_city_name);
        preference.listLocations.setAdapter(locationsAdapter);
        preference.listLocations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // fish fish
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                // fish fish
            }
        });

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

    private class CityNameAdapter extends ArrayAdapter {

        private Utils utils;

        private Map<String, String> calendarTypes = new HashMap<>();
        private Map<String, String> calendarTypesKeys = new HashMap<>();
        private int spinnerResource;

        public CityNameAdapter(Context context, int resource) {
            super(context, resource);
            utils = Utils.getInstance();

            spinnerResource = resource;
            calendarTypes.put("a", "a");
        }

        @Override
        public int getCount() {
            return calendarTypes.size();
        }

        @Override
        public Object getItem(int position) {
            return "a";//calendarTypeKeys[position];
        }

        public View getSpinnerItemView(int position, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(spinnerResource, parent, false);

            TextView city = (TextView) view.findViewById(R.id.text1);
            city.setText(calendarTypes.get("a"));
            TextView country = (TextView) view.findViewById(R.id.text2);
            country.setText(calendarTypes.get("a"));
            return view;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getSpinnerItemView(position, parent);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getSpinnerItemView(position, parent);
        }
    }
}
