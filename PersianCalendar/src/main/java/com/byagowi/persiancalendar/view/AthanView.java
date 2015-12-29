package com.byagowi.persiancalendar.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.byagowi.persiancalendar.AlarmReceiver;
import com.byagowi.persiancalendar.R;
import com.github.praytimes.PrayTime;
import com.malinskiy.materialicons.widget.IconTextView;

public class AthanView extends AppCompatActivity {
    private static final String TAG = "AthanView";
    public static boolean displayed = false;
    private static AthanView instance;

    private int layoutId = R.layout.activity_athan_fajr;
    private int athanIcon = R.string.azan1_icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String prayerKey = getIntent().getStringExtra(AlarmReceiver.KEY_EXTRA_PRAYER_KEY);
        setFlavor(prayerKey);
        setContentView(layoutId);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        instance = this;
        displayed = true;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        TextView textAlarmName = (TextView) findViewById(R.id.textAlarmName);
        TextView textCityName = (TextView) findViewById(R.id.textCityName);
        textAlarmName.setText(getPrayerName(prayerKey));

        IconTextView athanIconView = (IconTextView) findViewById(R.id.athan_icon);
        athanIconView.setText(getString(athanIcon));

        String cityName = "";
        String cityKey = sharedPreferences.getString("Location", "");
        String[] cityKeys = getResources().getStringArray(R.array.locationKeys);
        String[] cityNames = getResources().getStringArray(R.array.locationNames);

        if (!TextUtils.isEmpty(cityKey)) {
            for (int i = 0; i < cityKeys.length; ++i) {
                if (cityKey.equals(cityKeys[i])) {
                    cityName = cityNames[i];
                    break;
                }
            }
        } else {
            float latitude = sharedPreferences.getFloat("Latitude", 0);
            float longitude = sharedPreferences.getFloat("Longitude", 0);
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
                    athanIcon = R.string.azan1_icon;
                    break;
                case DHUHR:
                    layoutId = R.layout.activity_athan_dhuhr;
                    athanIcon = R.string.azan2_icon;
                    break;
                case ASR:
                    layoutId = R.layout.activity_athan_asr;
                    athanIcon = R.string.azan2_icon;
                    break;
                case MAGHRIB:
                    layoutId = R.layout.activity_athan_maghrib;
                    athanIcon = R.string.azan3_icon;
                    break;
                case ISHA:
                    layoutId = R.layout.activity_athan_isha;
                    athanIcon = R.string.azan3_icon;
                    break;
            }
        }
    }

    public static void hideAthan() {
        if (instance != null) {
            displayed = false;
            instance.finish();
        }
    }
}
