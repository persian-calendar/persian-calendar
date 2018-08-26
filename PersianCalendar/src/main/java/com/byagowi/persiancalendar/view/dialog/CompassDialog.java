package com.byagowi.persiancalendar.view.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.byagowi.persiancalendar.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;


public class CompassDialog extends DialogFragment {

    AnimationDrawable rocketAnimation;

    // This is a workaround for the strange behavior of onCreateView (which doesn't show dialog's layout)
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getContext();
        assert context != null;
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.dialog_compass, null);
        dialogBuilder.setView(dialogView);

        ImageView rocketImage = dialogView.findViewById(R.id.calibration_view);
        rocketImage.setBackgroundResource(R.drawable.calibrate_your_compass);
        rocketAnimation = (AnimationDrawable) rocketImage.getBackground();
        rocketAnimation.start();

        CheckBox mCheckBox = dialogView.findViewById(R.id.dont_show_again);
        Button mBtnPositive = dialogView.findViewById(R.id.button_ok);


        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                // Store the isChecked to Preference here
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("DONT_SHOW_DIALOG", isChecked);
                editor.apply();
            }
        });

        mBtnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        setCancelable(true);

        return dialogBuilder.create();
    }
}
