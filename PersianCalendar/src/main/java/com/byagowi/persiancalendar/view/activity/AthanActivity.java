package com.byagowi.persiancalendar.view.activity;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.entity.CityEntity;
import com.byagowi.persiancalendar.util.Utils;
import com.github.praytimes.Coordinate;
import com.github.praytimes.PrayTime;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AthanActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnCompletionListener {
    private static final String TAG = AthanActivity.class.getName();
    private TextView textAlarmName;
    private AppCompatImageView athanIconView;
    private MediaPlayer mediaPlayer;
    private Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String prayerKey = getIntent().getStringExtra(Constants.KEY_EXTRA_PRAYER_KEY);
        utils = Utils.getInstance(getApplicationContext());

        utils.changeAppLanguage(this);
        utils.loadLanguageResource();

        setContentView(R.layout.activity_athan);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        textAlarmName = (TextView) findViewById(R.id.athan_name);
        TextView textCityName = (TextView) findViewById(R.id.place);
        athanIconView = (AppCompatImageView) findViewById(R.id.background_image);
        athanIconView.setOnClickListener(this);

        setPrayerView(prayerKey);

        textCityName.setText(getString(R.string.in_city_time) + " " + utils.getCityName(true));

        play();

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
                case FAJR:
                    textAlarmName.setText(getString(R.string.azan1));
                    athanIconView.setImageResource(R.drawable.fajr);
                    break;

                case DHUHR:
                    textAlarmName.setText(getString(R.string.azan2));
                    athanIconView.setImageResource(R.drawable.dhuhr);
                    break;

                case ASR:
                    textAlarmName.setText(getString(R.string.azan3));
                    athanIconView.setImageResource(R.drawable.asr);
                    break;

                case MAGHRIB:
                    textAlarmName.setText(getString(R.string.azan4));
                    athanIconView.setImageResource(R.drawable.maghrib);
                    break;

                case ISHA:
                    textAlarmName.setText(getString(R.string.azan5));
                    athanIconView.setImageResource(R.drawable.isha);
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        stop();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        stop();
        finish();
    }

    private void play() {
        try {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setDataSource(this, utils.getAthanUri());
            mediaPlayer.prepare();
            mediaPlayer.start();
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, utils.getAthanVolume(), 0);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void stop() {
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        } catch (IllegalStateException ignored) {
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.release();
    }
}
