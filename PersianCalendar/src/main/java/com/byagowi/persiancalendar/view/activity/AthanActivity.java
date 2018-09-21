package com.byagowi.persiancalendar.view.activity;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.databinding.ActivityAthanBinding;
import com.byagowi.persiancalendar.util.UIUtils;
import com.byagowi.persiancalendar.util.Utils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

public class AthanActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = AthanActivity.class.getName();
    private Ringtone ringtone;
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, Utils.getAthanVolume(this), 0);
        }

        Uri customAthanUri = Utils.getCustomAthanUri(this);
        if (customAthanUri != null) {
            try {
                ringtone = RingtoneManager.getRingtone(this, customAthanUri);
                ringtone.setStreamType(AudioManager.STREAM_ALARM);
                ringtone.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            MediaPlayer player = new MediaPlayer();
            try {
                player.setDataSource(this, UIUtils.getDefaultAthanUri(this));
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

        Utils.applyAppLanguage(this);

        ActivityAthanBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_athan);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        String prayerKey = getIntent().getStringExtra(Constants.KEY_EXTRA_PRAYER_KEY);
        binding.athanName.setText(UIUtils.getPrayTimeText(prayerKey));

        View root = binding.getRoot();
        root.setOnClickListener(this);
        root.setBackgroundResource(UIUtils.getPrayTimeImage(prayerKey));

        binding.place.setText(String.format("%s %s",
                getString(R.string.in_city_time),
                Utils.getCityName(this, true)));
        handler.postDelayed(stopTask, TimeUnit.SECONDS.toMillis(10));

        try {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            }
        } catch (Exception e) {
            Log.e(TAG, "TelephonyManager handling fail", e);
        }
    }

    Runnable stopTask = new Runnable() {
        @Override
        public void run() {
            if (ringtone == null && mediaPlayer == null) {
                AthanActivity.this.finish();
                return;
            }
            try {
                if (ringtone != null) {
                    if (!ringtone.isPlaying()) {
                        AthanActivity.this.finish();
                        return;
                    }
                }
                if (mediaPlayer != null) {
                    if (!mediaPlayer.isPlaying()) {
                        AthanActivity.this.finish();
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                AthanActivity.this.finish();
                return;
            }
            handler.postDelayed(this, TimeUnit.SECONDS.toMillis(5));
        }
    };

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            stop();
        }
    }

    private PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (state == TelephonyManager.CALL_STATE_RINGING ||
                    state == TelephonyManager.CALL_STATE_OFFHOOK) {
                stop();
            }
        }
    };

    @Override
    public void onClick(View v) {
        stop();
    }

    @Override
    public void onBackPressed() {
        stop();
    }

    private void stop() {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
            }
            phoneStateListener = null;
        } catch (RuntimeException e) {
            Log.e(TAG, "TelephonyManager handling fail", e);
        }

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
        handler.removeCallbacks(stopTask);
        finish();
    }
}
