package com.byagowi.persiancalendar.view.preferences;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.TextView;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.util.UIUtils;
import com.byagowi.persiancalendar.util.Utils;
import com.github.praytimes.Coordinate;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceManager;

public class GPSLocationDialog extends PreferenceDialogFragmentCompat {

    private LocationManager locationManager;
    private Context context;
    private TextView textView;
    private Handler handler = new Handler();
    private String latitude;
    private String longitude;
    private String cityName;

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        context = builder.getContext();

        textView = new TextView(context);
        textView.setPadding(32, 32, 32, 32);
        textView.setText(R.string.pleasewaitgps);

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        getLocation();
        if (lacksPermission) {
            UIUtils.askForLocationPermission(getActivity());
        }

        handler.postDelayed(checkGPSProviderCallback, TimeUnit.SECONDS.toMillis(30));

        builder.setPositiveButton("", null);
        builder.setNegativeButton("", null);
        builder.setView(textView);
    }

    private void checkGPSProvider() {
        if (latitude != null && longitude != null) return;

        FragmentActivity activity = getActivity();
        if (activity == null) return;

        try {
            LocationManager gps = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

            if (gps != null && !gps.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                new AlertDialog.Builder(activity)
                        .setMessage(R.string.gps_internet_desc)
                        .setPositiveButton(R.string.accept, (dialogInterface, i) -> {
                            try {
                                activity.startActivity(
                                        new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            } catch (Exception ignore) {
                            }
                        }).create().show();
            }
        } catch (Exception ignore) {
        }

    }

    private Runnable checkGPSProviderCallback = this::checkGPSProvider;

    private boolean lacksPermission = false;
    private boolean everRegisteredCallback = false;

    private void getLocation() {
        if (locationManager == null) {
            return;
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            lacksPermission = true;
            return;
        }

        if (locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            everRegisteredCallback = true;
        }

        if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            everRegisteredCallback = true;
        }
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null)
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

    private void showLocation(Location location) {
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
        if (!TextUtils.isEmpty(cityName)) {
            result = cityName + "\n\n";
        }
        // this time, with native digits
        result += Utils.formatCoordinate(context,
                new Coordinate(location.getLatitude(), location.getLongitude()), "\n");
        textView.setText(result);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (latitude != null && longitude != null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constants.PREF_LATITUDE, latitude);
            editor.putString(Constants.PREF_LONGITUDE, longitude);
            if (cityName != null) {
                editor.putString(Constants.PREF_GEOCODED_CITYNAME, cityName);
            } else {
                editor.putString(Constants.PREF_GEOCODED_CITYNAME, "");
            }
            editor.putString(Constants.PREF_SELECTED_LOCATION, Constants.DEFAULT_CITY);
            editor.apply();

            LocalBroadcastManager.getInstance(context)
                    .sendBroadcast(new Intent(Constants.LOCAL_INTENT_UPDATE_PREFERENCE));
        }

        if (everRegisteredCallback && locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }

        handler.removeCallbacks(checkGPSProviderCallback);
    }

    @Override
    public void onPause() {
        onDialogClosed(false);
        dismiss();
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Constants.LOCATION_PERMISSION_REQUEST_CODE) {
            getLocation();
            if (lacksPermission) // request for permission is rejected
                dismiss();
        }
    }
}
