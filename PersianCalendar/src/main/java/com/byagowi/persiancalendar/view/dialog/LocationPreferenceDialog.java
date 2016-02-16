package com.byagowi.persiancalendar.view.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.Utils;
import com.byagowi.persiancalendar.adapter.LocationAdapter;
import com.byagowi.persiancalendar.adapter.MonthAdapter;
import com.byagowi.persiancalendar.view.fragment.ApplicationPreferenceFragment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

    public void selectItem(int position) {

    }

//    private class CityNameAdapter extends ArrayAdapter {
//
//        private Utils utils;
//
//        private Map<String, Utils.City> cityMap = new HashMap<>();
//        List<Utils.City> cities;
//        private int spinnerResource;
//        Context context;
//        String locale;
//        LayoutInflater inflater;
//
//        public CityNameAdapter(Context context, int resource) {
//            super(context, resource);
//            utils = Utils.getInstance();
//            cities = Arrays.asList(utils.getAllCities(context));
//            spinnerResource = resource;
//            this.context = context;
//            this.inflater = (LayoutInflater)
//                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//            this.locale = prefs.getString("AppLanguage", "fa");
//        }
//
//        @Override
//        public int getCount() {
//            return cities.size();
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return cities.get(position);
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            ViewHolder holder;
//
//            if (convertView == null) {
//                convertView = inflater.inflate(spinnerResource, null);
//
//                holder = new ViewHolder();
//                holder.city = (TextView) convertView.findViewById(R.id.text1);
//                holder.country = (TextView) convertView.findViewById(R.id.text2);
//
//                convertView.setTag(holder);
//
//            } else {
//                holder = (ViewHolder) convertView.getTag();
//            }
//
//            holder.city.setText(locale.equals("en")
//                    ? cities.get(position).en
//                    : cities.get(position).fa);
//
//            utils.prepareShapeTextView(context, holder.city);
//
//            holder.country.setText(locale.equals("en")
//                    ? cities.get(position).countryEn
//                    : cities.get(position).countryFa);
//            utils.prepareShapeTextView(context, holder.country);
//
//
//            return convertView;
//        }
//    }
//
//    static class ViewHolder {
//        TextView country;
//        TextView city;
//    }
}
