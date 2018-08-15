package com.byagowi.persiancalendar.view.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.databinding.DialogAccessBinding;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

public class GPSDiagnosticDialog extends DialogFragment {

    public static boolean needsDiagnostic(Context context) {
        try {
            LocationManager gps = (LocationManager)
                    context.getSystemService(Context.LOCATION_SERVICE);
            ConnectivityManager connectivityManager = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo info = null;
            if (connectivityManager != null) info = connectivityManager.getActiveNetworkInfo();

            boolean gpsEnabled = false;

            if (gps != null) {
                try {
                    gpsEnabled = gps.isProviderEnabled(LocationManager.GPS_PROVIDER);
                } catch (Exception ignored) {
                }
            }

            return !gpsEnabled || info == null;
        } catch (Exception e) {
            // Do whatever we were doing till now
            return false;
        }
    }

    // This is a workaround for the strange behavior of onCreateView (which doesn't show dialog's layout)
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        if (activity == null) return null;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        DialogAccessBinding binding = DataBindingUtil.inflate(LayoutInflater.from(activity),
                R.layout.dialog_access, null, false);
        dialogBuilder.setView(binding.getRoot());

        boolean gpsEnabled = false;
        try {
            // check whether gps provider and network providers are enabled or not
            LocationManager gps = (LocationManager)
                    activity.getSystemService(Context.LOCATION_SERVICE);
            ConnectivityManager connectivityManager = (ConnectivityManager)
                    activity.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo info = null;
            if (connectivityManager != null) {
                info = connectivityManager.getActiveNetworkInfo();
            }
            if (gps != null) {
                try {
                    gpsEnabled = gps.isProviderEnabled(LocationManager.GPS_PROVIDER);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignore) {
        }

        binding.dialogButtonGPS.setOnClickListener(v -> {
            activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            dismiss();
            // get gps
        });

        binding.dialogButtonWiFi.setOnClickListener(v -> {
            activity.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            dismiss();
            // get wifi
        });

        binding.dialogButtonGPRS.setOnClickListener(v -> {
            activity.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
            dismiss();
            // get gprs
        });

        binding.dialogButtonExit.setOnClickListener(v -> {
            dismiss();
            // exit
        });

        return dialogBuilder.create();
    }
}