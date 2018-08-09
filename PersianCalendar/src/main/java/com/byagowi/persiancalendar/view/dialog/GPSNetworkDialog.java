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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.DialogFragment;
import com.byagowi.persiancalendar.R;

import java.util.Objects;

public class GPSNetworkDialog extends DialogFragment {

    // This is a workaround for the strange behavior of onCreateView (which doesn't show dialog's layout)
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getContext();
        assert context != null;
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.dialog_accses, null);
        dialogBuilder.setView(dialogView);

        //check whether gps provider and network providers are enabled or not
        LocationManager gps = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        NetworkInfo info = ((ConnectivityManager)
                Objects.requireNonNull(context.getSystemService(Context.CONNECTIVITY_SERVICE))).getActiveNetworkInfo();
        boolean gps_enabled = false;

        try {
            assert gps != null;
            gps_enabled = gps.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ignored) {}

        Button mBtGPS = dialogView.findViewById(R.id.dialogButtonGPS);
        Button mBtnWiFi = dialogView.findViewById(R.id.dialogButtonWiFi);
        Button mBtnGPRS = dialogView.findViewById(R.id.dialogButtonGPRS);
        AppCompatImageView mBtnExit = dialogView.findViewById(R.id.dialogButtonExit);


        mBtGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                getActivity().startActivity(myIntent);
                getDialog().dismiss();
                //get gps
            }
        });

        mBtnWiFi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent myIntent = new Intent( Settings.ACTION_WIFI_SETTINGS);
                getActivity().startActivity(myIntent);
                getDialog().dismiss();
                //get wifi
            }
        });

        mBtnGPRS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent myIntent = new Intent( Settings.ACTION_WIRELESS_SETTINGS);
                getActivity().startActivity(myIntent);
                getDialog().dismiss();
                //get gprs
            }
        });

        mBtnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
                //exit
            }
        });

        if (!gps_enabled && info == null){
            Toast.makeText(getActivity(),R.string.internet_location_enable, Toast.LENGTH_SHORT).show();
            mBtGPS.setVisibility(View.VISIBLE);
            mBtnWiFi.setVisibility(View.VISIBLE);
            mBtnGPRS.setVisibility(View.VISIBLE);
        } else if(!gps_enabled){
            Toast.makeText(getActivity(),R.string.location_enable, Toast.LENGTH_SHORT).show();
            mBtnGPRS.setVisibility(View.GONE);
            mBtnWiFi.setVisibility(View.GONE);
        } else if (info == null) {
            Toast.makeText(getActivity(),R.string.internet_enable, Toast.LENGTH_SHORT).show();
            mBtGPS.setVisibility(View.GONE);
        }

        setCancelable(true);

        return dialogBuilder.create();
    }
}