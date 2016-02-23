package com.byagowi.persiancalendar.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.Utils;
import com.byagowi.persiancalendar.entity.City;
import com.byagowi.persiancalendar.service.BroadcastReceivers;
import com.github.praytimes.Coordinate;
import com.github.praytimes.PrayTime;

import java.util.concurrent.TimeUnit;

public class AthanView extends AppCompatActivity implements View.OnClickListener {
    private TextView textAlarmName;
    private AppCompatImageView athanIconView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String prayerKey = getIntent().getStringExtra(Constants.KEY_EXTRA_PRAYER_KEY);
        Utils utils = Utils.getInstance(getApplicationContext());

        setContentView(R.layout.activity_athan);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        textAlarmName = (TextView) findViewById(R.id.athan_name);
        TextView textCityName = (TextView) findViewById(R.id.place);
        athanIconView = (AppCompatImageView) findViewById(R.id.background_image);
        athanIconView.setOnClickListener(this);

        setPrayerView(prayerKey);

        City city = utils.getCityFromPreference();
        if (city != null) {
            String cityName = utils.getAppLanguage().equals("en") ? city.getEn() : city.getFa();
            textCityName.setText(getString(R.string.in_city_time) + " " + cityName);
        } else {
            Coordinate coordinate = utils.getCoordinate();
            textCityName.setText(getString(R.string.in_city_time) + " "
                    + coordinate.getLatitude() + ", " + coordinate.getLongitude());
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, TimeUnit.SECONDS.toMillis(45));
    }

    private void setPrayerView(String key) {
        if (!TextUtils.isEmpty(key)) {
            PrayTime prayTime = PrayTime.valueOf(key);

            switch (prayTime) {
                case IMSAK:
                    textAlarmName.setText(getString(R.string.azan1));
                    athanIconView.setImageResource(R.drawable.zohr);
                    break;

                case DHUHR:
                    textAlarmName.setText(getString(R.string.azan2));
                    athanIconView.setImageResource(R.drawable.zohr);
                    break;

                case ASR:
                    textAlarmName.setText(getString(R.string.azan3));
                    athanIconView.setImageResource(R.drawable.zohr);
                    break;

                case MAGHRIB:
                    textAlarmName.setText(getString(R.string.azan4));
                    athanIconView.setImageResource(R.drawable.zohr);
                    break;

                case ISHA:
                    textAlarmName.setText(getString(R.string.azan5));
                    athanIconView.setImageResource(R.drawable.zohr);
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        sendBroadcastStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sendBroadcastStop();
    }

    @Override
    public void onBackPressed() {
        sendBroadcastStop();
    }

    private void sendBroadcastStop() {
        Intent intent = new Intent(getBaseContext(), BroadcastReceivers.class);
        intent.setAction(Constants.ACTION_STOP_ALARM);
        AthanView.this.sendBroadcast(intent);
        finish();
    }

}
