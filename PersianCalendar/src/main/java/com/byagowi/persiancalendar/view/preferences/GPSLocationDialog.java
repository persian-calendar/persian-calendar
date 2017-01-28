package com.byagowi.persiancalendar.view.preferences;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.widget.TextView;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.util.Utils;
import com.github.praytimes.Coordinate;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by ebrahim on 3/26/16.
 */
public class GPSLocationDialog extends PreferenceDialogFragmentCompat {

    LocationManager locationManager;
    Context context;
    TextView textView;
    Utils utils;

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        context = getContext();
        utils = Utils.getInstance(context);

        textView = new TextView(context);
        textView.setPadding(32, 32, 32, 32);
        textView.setTextSize(20);
        textView.setText(R.string.pleasewaitgps);
        utils.setFontAndShape(textView);

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        tryRetrieveLocation();
        LocalBroadcastManager.getInstance(context).registerReceiver(permissionGrantReceiver,
                new IntentFilter(Constants.LOCATION_PERMISSION_RESULT));

        builder.setPositiveButton("", null);
        builder.setNegativeButton("", null);
        builder.setView(textView);
    }

    BroadcastReceiver permissionGrantReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            tryRetrieveLocation();
        }
    };

    // Just ask for permission once, if we couldn't get it, nvm
    public boolean first = true;

    public void tryRetrieveLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }

            if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            }
        } else if (first) {
            first = false;
            ActivityCompat.requestPermissions(getActivity(),
                    new String[] {
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    Constants.LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            dismiss();
        }
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    };

    String latitude;
    String longitude;
    String cityName;

    public void showLocation(Location location) {
        latitude = String.format(Locale.ENGLISH, "%f", location.getLatitude());
        longitude = String.format(Locale.ENGLISH, "%f", location.getLongitude());
        Geocoder gcd = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses.size() > 0) {
                cityName = addresses.get(0).getLocality();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String result = "";
        if (cityName != null) {
            result = cityName + "\n\n";
        }
        // this time, with native digits
        result += utils.formatCoordinate(
                new Coordinate(location.getLatitude(), location.getLongitude()),
                "\n");
        textView.setText(utils.shape(result));
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(permissionGrantReceiver);

        if (latitude != null && longitude != null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constants.PREF_LATITUDE, latitude);
            editor.putString(Constants.PREF_LONGITUDE, longitude);
            if (cityName != null) {
                editor.putString(Constants.PREF_GEOCODED_CITYNAME, cityName);
            } else {
                editor.putString(Constants.PREF_GEOCODED_CITYNAME, "");
            }
            editor.putString(Constants.PREF_SELECTED_LOCATION, Constants.DEFAULT_CITY);
            editor.commit();
        }

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(locationListener);
        }
    }
}
