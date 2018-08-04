package com.byagowi.persiancalendar.view.activity;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.util.Utils;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

public class AthanActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = AthanActivity.class.getName();
    private TextView textAlarmName;
    private AppCompatImageView athanIconView;
    private Ringtone ringtone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ringtone = RingtoneManager.getRingtone(this, Utils.getAthanUri(this));
        ringtone.setStreamType(AudioManager.STREAM_ALARM);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, Utils.getAthanVolume(this), 0);
        }

        Utils.changeAppLanguage(this);

        setContentView(R.layout.activity_athan);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        textAlarmName = findViewById(R.id.athan_name);
        TextView textCityName = findViewById(R.id.place);
        athanIconView = findViewById(R.id.background_image);
        athanIconView.setOnClickListener(this);

        String prayerKey = getIntent().getStringExtra(Constants.KEY_EXTRA_PRAYER_KEY);
        textAlarmName.setText(Utils.getPrayTimeText(prayerKey));
        athanIconView.setImageResource(Utils.getPrayTimeImage(prayerKey));

        textCityName.setText(getString(R.string.in_city_time) + " " + Utils.getCityName(this, true));

        play();

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (ringtone != null && !ringtone.isPlaying()) {
                    timer.cancel();
                    finish();
                }
            }
        };
        timer.scheduleAtFixedRate(task, TimeUnit.SECONDS.toMillis(10),
                TimeUnit.SECONDS.toMillis(5));

        try {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            }
        } catch (Exception e) {
            Log.e("AthanActivity", "TelephonyManager handling fail", e);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            stop();
            finish();
        }
    }

    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = focusChange -> {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            stop();
            finish();
        }
    };

    private PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (state == TelephonyManager.CALL_STATE_RINGING ||
                    state == TelephonyManager.CALL_STATE_OFFHOOK) {
                mOnAudioFocusChangeListener.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS);
            }
        }
    };

    @Override
    public void onClick(View v) {
        stop();
        finish();
    }

    @Override
    public void onBackPressed() {
        stop();
        finish();
    }

    private void play() {
        if (ringtone != null) {
            ringtone.play();
        }
    }

    private void stop() {
        if (ringtone != null) {
            ringtone.stop();
        }
    }
}
