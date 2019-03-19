package com.byagowi.persiancalendar.ui.preferences.locationathan.location;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.byagowi.persiancalendar.di.dependencies.AppDependency;
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency;
import com.byagowi.persiancalendar.praytimes.Coordinate;
import com.byagowi.persiancalendar.utils.Utils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import dagger.android.support.DaggerAppCompatDialogFragment;

public class GPSLocationDialog extends DaggerAppCompatDialogFragment {
    @Inject
    AppDependency appDependency;
    @Inject
    MainActivityDependency mainActivityDependency;
    private LocationManager locationManager;
    private TextView textView;
    private Handler handler = new Handler();
    private String latitude;
    private String longitude;
    private String cityName;
    private Runnable checkGPSProviderCallback = this::checkGPSProvider;
    private boolean lacksPermission = false;
    private boolean everRegisteredCallback = false;
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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        textView = new TextView(mainActivityDependency.getMainActivity());
        textView.setPadding(32, 32, 32, 32);
        textView.setText(R.string.pleasewaitgps);

        locationManager = (LocationManager) mainActivityDependency.getMainActivity()
                .getSystemService(Context.LOCATION_SERVICE);

        getLocation();
        if (lacksPermission) {
            Utils.askForLocationPermission(mainActivityDependency.getMainActivity());
        }

        handler.postDelayed(checkGPSProviderCallback, TimeUnit.SECONDS.toMillis(30));

        return new AlertDialog.Builder(mainActivityDependency.getMainActivity())
                .setPositiveButton("", null)
                .setNegativeButton("", null)
                .setView(textView)
                .create();
    }

    private void checkGPSProvider() {
        if (latitude != null && longitude != null) return;

        try {
            LocationManager gps = (LocationManager) mainActivityDependency.getMainActivity()
                    .getSystemService(Context.LOCATION_SERVICE);

            if (gps != null && !gps.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                new AlertDialog.Builder(mainActivityDependency.getMainActivity())
                        .setMessage(R.string.gps_internet_desc)
                        .setPositiveButton(R.string.accept, (dialogInterface, i) -> {
                            try {
                                mainActivityDependency.getMainActivity().startActivity(
                                        new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            } catch (Exception ignore) {
                            }
                        }).create().show();
            }
        } catch (Exception ignore) {
        }

    }

    private void getLocation() {
        if (locationManager == null) {
            return;
        }

        if (ActivityCompat.checkSelfPermission(mainActivityDependency.getMainActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mainActivityDependency.getMainActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    private void showLocation(Location location) {
        latitude = String.format(Locale.ENGLISH, "%f", location.getLatitude());
        longitude = String.format(Locale.ENGLISH, "%f", location.getLongitude());
        Geocoder gcd = new Geocoder(mainActivityDependency.getMainActivity(), Locale.getDefault());
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
        result += Utils.formatCoordinate(mainActivityDependency.getMainActivity(),
                new Coordinate(location.getLatitude(), location.getLongitude(),
                        location.getAltitude()), "\n");
        textView.setText(result);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        if (latitude != null && longitude != null) {
            SharedPreferences.Editor editor = appDependency.getSharedPreferences().edit();
            editor.putString(Constants.PREF_LATITUDE, latitude);
            editor.putString(Constants.PREF_LONGITUDE, longitude);
            if (cityName != null) {
                editor.putString(Constants.PREF_GEOCODED_CITYNAME, cityName);
            } else {
                editor.putString(Constants.PREF_GEOCODED_CITYNAME, "");
            }
            editor.putString(Constants.PREF_SELECTED_LOCATION, Constants.DEFAULT_CITY);
            editor.apply();
        }

        if (everRegisteredCallback && locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }

        handler.removeCallbacks(checkGPSProviderCallback);

        super.onDismiss(dialog);
    }

    @Override
    public void onPause() {
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
