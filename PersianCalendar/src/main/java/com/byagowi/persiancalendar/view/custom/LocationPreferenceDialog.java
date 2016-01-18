package com.byagowi.persiancalendar.view.custom;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.db.LocationsHelper;
import com.byagowi.persiancalendar.view.Fragment.ApplicationPreferenceFragment;

import java.util.Locale;

/**
 * persian_calendar
 * Author: hamidsafdari22@gmail.com
 * Date: 1/17/16
 */
public class LocationPreferenceDialog extends PreferenceDialogFragmentCompat {
    private static final String TAG = "LocPrefDialog";
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

        LocationsHelper helper = new LocationsHelper(getContext());
        final Cursor locationsCursor = helper.listCities(getCurrentLangCode());

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.preference_location, (ViewGroup) getView(), false);
        preference.listLocations = (ListView) view.findViewById(R.id.list);

        final CityNameAdapter locationsAdapter = new CityNameAdapter(
                getContext(),
                R.layout.list_item_city_name,
                locationsCursor,
                new String[]{
                        BaseColumns._ID,
                        LocationsHelper.TABLE_NAME_CITIES + LocationsHelper.COLUMN_KEY,
                        LocationsHelper.TABLE_NAME_CITIES + LocationsHelper.COLUMN_COUNTRY,
                        LocationsHelper.TABLE_NAME_CITIES + LocationsHelper.COLUMN_NAME_EN,
                        LocationsHelper.TABLE_NAME_CITIES + LocationsHelper.COLUMN_NAME_FA,
                        LocationsHelper.TABLE_NAME_COUNTRIES + BaseColumns._ID,
                        LocationsHelper.TABLE_NAME_COUNTRIES + LocationsHelper.COLUMN_KEY,
                        LocationsHelper.TABLE_NAME_COUNTRIES + LocationsHelper.COLUMN_NAME_EN,
                        LocationsHelper.TABLE_NAME_COUNTRIES + LocationsHelper.COLUMN_NAME_FA
                },
                new int[]{R.id.text1, R.id.text2},
                0);
        preference.listLocations.setAdapter(locationsAdapter);
        preference.listLocations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                locationsCursor.moveToPosition(position);
                selectedCity = locationsCursor.getString(locationsCursor.getColumnIndex(LocationsHelper.TABLE_NAME_CITIES + LocationsHelper.COLUMN_KEY));
                dismiss();
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                locationsCursor.close();
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

        LocalBroadcastManager.getInstance(getContext())
                .sendBroadcast(intent);
        getPreference().close(positiveResult);
    }

    private class CityNameAdapter extends SimpleCursorAdapter {
        public CityNameAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView textCityName = (TextView) view.findViewById(R.id.text1);
            TextView textCountryName = (TextView) view.findViewById(R.id.text2);

            String langCode = getCurrentLangCode();
            String columnCityName;
            String columnCountryName;
            if (!TextUtils.isEmpty(langCode) && langCode.equalsIgnoreCase("fa")) {
                columnCityName = LocationsHelper.TABLE_NAME_CITIES + LocationsHelper.COLUMN_NAME_FA;
                columnCountryName = LocationsHelper.TABLE_NAME_COUNTRIES + LocationsHelper.COLUMN_NAME_FA;
            } else {
                columnCityName = LocationsHelper.TABLE_NAME_CITIES + LocationsHelper.COLUMN_NAME_EN;
                columnCountryName = LocationsHelper.TABLE_NAME_COUNTRIES + LocationsHelper.COLUMN_NAME_EN;
            }

            String cityName = cursor.getString(cursor.getColumnIndex(columnCityName));
            String countryName = cursor.getString(cursor.getColumnIndex(columnCountryName));

            textCityName.setText(cityName);
            textCountryName.setText(countryName);
        }
    }
}
