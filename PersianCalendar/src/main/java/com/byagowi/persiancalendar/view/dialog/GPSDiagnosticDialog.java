package com.byagowi.persiancalendar.view.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.byagowi.persiancalendar.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class GPSDiagnosticDialog extends DialogFragment {

    // This is a workaround for the strange behavior of onCreateView (which doesn't show dialog's layout)
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getContext();
        if (context == null)
            return null;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.dialog_access, null);
        dialogBuilder.setView(dialogView);

        // check whether gps provider and network providers are enabled or not
        LocationManager gps = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo info = null;
        if (connectivityManager != null) {
            info = connectivityManager.getActiveNetworkInfo();
        }

        boolean gpsEnabled = false;

        if (gps != null) {
            try {
                gpsEnabled = gps.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception ignored) {
            }
        }

        Button mBtnExit = dialogView.findViewById(R.id.dialogButtonExit);
        Button mBtGPS = dialogView.findViewById(R.id.dialogButtonGPS);
        Button mBtnWiFi = dialogView.findViewById(R.id.dialogButtonWiFi);
        Button mBtnGPRS = dialogView.findViewById(R.id.dialogButtonGPRS);

        mBtGPS.setOnClickListener(v -> {
            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            getActivity().startActivity(myIntent);
            getDialog().dismiss();
            // get gps
        });

        mBtnWiFi.setOnClickListener(v -> {
            Intent myIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            getActivity().startActivity(myIntent);
            getDialog().dismiss();
            // get wifi
        });

        mBtnGPRS.setOnClickListener(v -> {
            Intent myIntent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
            getActivity().startActivity(myIntent);
            getDialog().dismiss();
            // get gprs
        });

        mBtnExit.setOnClickListener(v -> {
            getDialog().dismiss();
            // exit
        });

        if (!gpsEnabled && info == null) {
            Toast.makeText(getActivity(), R.string.internet_location_enable, Toast.LENGTH_SHORT).show();
            mBtGPS.setVisibility(View.VISIBLE);
            mBtnWiFi.setVisibility(View.VISIBLE);
            mBtnGPRS.setVisibility(View.VISIBLE);
        } else if (!gpsEnabled) {
            Toast.makeText(getActivity(), R.string.location_enable, Toast.LENGTH_SHORT).show();
            mBtnGPRS.setVisibility(View.GONE);
            mBtnWiFi.setVisibility(View.GONE);
        } else if (info == null) {
            Toast.makeText(getActivity(), R.string.internet_enable, Toast.LENGTH_SHORT).show();
            mBtGPS.setVisibility(View.GONE);
        }

        setCancelable(true);

        return dialogBuilder.create();
    }
}