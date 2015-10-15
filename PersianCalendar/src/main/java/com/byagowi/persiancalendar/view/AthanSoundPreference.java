package com.byagowi.persiancalendar.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.util.AttributeSet;
import android.util.Log;

import com.byagowi.persiancalendar.Utils;

public class AthanSoundPreference extends RingtonePreference {
    private static final String TAG = "AthanSoundPreference";
    private Boolean firstRun;

    public AthanSoundPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        // the user might see the default alarm selected and close
        // the dialog without tapping OK, save it just in case
        if (isFirstRun(context)) {
            persistString(Utils.athanFileUri.getPath());
        }
    }

    public AthanSoundPreference(Context context) {
        super(context, null);
    }

    @Override
    protected void onPrepareRingtonePickerIntent(Intent ringtonePickerIntent) {
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, RingtoneManager.getDefaultUri(getRingtoneType()));

        Uri prevRingtone = onRestoreRingtone();
        if (prevRingtone == null && isFirstRun(getContext())) {
            prevRingtone = Utils.athanFileUri;
        }

        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, prevRingtone);
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, getShowDefault());
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, getShowSilent());
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, getRingtoneType());
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getTitle());
    }

    public boolean isFirstRun(Context context) {
        final String FIRST_RUN_PREF_KEY = "FirstRunVersionCode";
        if (firstRun == null) {
            try {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

                int currentVersionCode = packageInfo.versionCode;
                int prefVersionCode = prefs.getInt(FIRST_RUN_PREF_KEY, 0);
                firstRun = (currentVersionCode != prefVersionCode);

                if (firstRun) {
                    SharedPreferences.Editor prefEditor = prefs.edit();
                    prefEditor.putInt(FIRST_RUN_PREF_KEY, currentVersionCode);
                    prefEditor.commit();
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "", e);
            }
        }
        Log.d(TAG, "first run: " + firstRun);
        return firstRun;
    }
}
