package com.byagowi.persiancalendar.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.Utils;
import com.byagowi.persiancalendar.entity.City;
import com.byagowi.persiancalendar.service.AlarmReceiver;
import com.byagowi.persiancalendar.view.fragment.ApplicationPreferenceFragment;
import com.github.praytimes.PrayTime;

import java.util.concurrent.TimeUnit;

public class AthanView extends AppCompatActivity {
    private static final String TAG = "AthanView";
    public static boolean displayed = false;
    private static boolean handlerRunning = false;
    private static AthanView instance;

    private int layoutId = R.layout.activity_athan_dhuhr;
    private int athanIcon = R.drawable.ic_brightness_2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String prayerKey = getIntent().getStringExtra(AlarmReceiver.KEY_EXTRA_PRAYER_KEY);
        setFlavor(prayerKey);
        setContentView(layoutId);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Utils utils = Utils.getInstance(getApplicationContext());

        instance = this;
        displayed = true;

        handlerRunning = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                handlerRunning = false;
                hideAthan();
            }
        }, TimeUnit.SECONDS.toMillis(45));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        TextView textAlarmName = (TextView) findViewById(R.id.textAlarmName);
        TextView textCityName = (TextView) findViewById(R.id.textCityName);
        textAlarmName.setText(getPrayerName(prayerKey));

        AppCompatImageView athanIconView = (AppCompatImageView) findViewById(R.id.athan_icon);
        athanIconView.setImageResource(R.drawable.ic_brightness_2);

        String cityName;
        String cityKey = prefs.getString(ApplicationPreferenceFragment.PREF_KEY_LOCATION, "");
        if (!TextUtils.isEmpty(cityKey)) {
            City city = utils.getCityByKey(cityKey);
            cityName = prefs.getString("AppLanguage", "fa").equals("en") ? city.getEn() : city.getFa();
        } else {
            float latitude = prefs.getFloat(ApplicationPreferenceFragment.PREF_KEY_LATITUDE, 0);
            float longitude = prefs.getFloat(ApplicationPreferenceFragment.PREF_KEY_LONGITUDE, 0);
            cityName = latitude + ", " + longitude;
        }
        textCityName.setText(cityName);

        View view = findViewById(R.id.ctlStopAlarm);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AthanView.this.sendBroadcast(new Intent(AlarmReceiver.ACTION_STOP_ALARM));
                finish();
            }
        });
    }

    private String getPrayerName(String key) {
        String name = "";
        if (!TextUtils.isEmpty(key)) {
            PrayTime prayTime = PrayTime.valueOf(key);
            int stringId = 0;
            switch (prayTime) {
                case FAJR:
                    stringId = R.string.azan1;
                    break;
                case DHUHR:
                    stringId = R.string.azan2;
                    break;
                case ASR:
                    stringId = R.string.azan3;
                    break;
                case MAGHRIB:
                    stringId = R.string.azan4;
                    break;
                case ISHA:
                    stringId = R.string.azan5;
                    break;
            }
            name = getString(stringId);
        }
        return name;
    }

    private void setFlavor(String key) {
        if (!TextUtils.isEmpty(key)) {
            PrayTime prayTime = PrayTime.valueOf(key);
            switch (prayTime) {
                case FAJR:
                    layoutId = R.layout.activity_athan_fajr;
                    athanIcon = R.drawable.ic_brightness_4;
                    break;
                case DHUHR:
                    layoutId = R.layout.activity_athan_dhuhr;
                    athanIcon = R.drawable.ic_brightness_5;
                    break;
                case ASR:
                    layoutId = R.layout.activity_athan_asr;
                    athanIcon = R.drawable.ic_brightness_5;
                    break;
                case MAGHRIB:
                    layoutId = R.layout.activity_athan_maghrib;
                    athanIcon = R.drawable.ic_brightness_3;
                    break;
                case ISHA:
                    layoutId = R.layout.activity_athan_isha;
                    athanIcon = R.drawable.ic_brightness_3;
                    break;
            }
        }
    }

    public static void hideAthan() {
        Log.d(TAG, "trying to hide athan screen. handler running: " + handlerRunning);
        if (instance != null && !handlerRunning) {
            displayed = false;
            instance.finish();
        }
    }
}
