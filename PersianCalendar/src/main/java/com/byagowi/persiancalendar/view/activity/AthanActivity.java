package com.byagowi.persiancalendar.view.activity;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
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

import java.io.IOException;
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
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, Utils.getAthanVolume(this), 0);
        }


        Uri customAthanUri = Utils.getCustomAthanUri(this);
        if (customAthanUri != null) {
            ringtone = RingtoneManager.getRingtone(this, customAthanUri);
            ringtone.setStreamType(AudioManager.STREAM_ALARM);
            ringtone.play();
        } else {
            MediaPlayer player = new MediaPlayer();
            try {
                player.setDataSource(this, Utils.getDefaultAthanUri(this));
                player.setAudioStreamType(AudioManager.STREAM_ALARM);
                player.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                player.start();
                mediaPlayer = player;
            } catch (Exception e) {
                e.printStackTrace();
            }
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

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (ringtone == null && mediaPlayer == null) {
                    cancel();
                    finish();
                }
                try {
                    if (ringtone != null) {
                        if (!ringtone.isPlaying()) {
                            cancel();
                            finish();
                        }
                    }
                    if (mediaPlayer != null) {
                        if (!mediaPlayer.isPlaying()) {
                            cancel();
                            finish();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    cancel();
                    finish();
                }
            }
        }, TimeUnit.SECONDS.toMillis(10), TimeUnit.SECONDS.toMillis(5));

        try {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            }
        } catch (Exception e) {
            Log.e(TAG, "TelephonyManager handling fail", e);
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

    private void stop() {
        if (ringtone != null) {
            ringtone.stop();
        }
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }
}
