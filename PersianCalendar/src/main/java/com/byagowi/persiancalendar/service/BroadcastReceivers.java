package com.byagowi.persiancalendar.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.Utils;
import com.byagowi.persiancalendar.util.UpdateUtils;
import com.byagowi.persiancalendar.view.AthanView;

import java.io.IOException;

/**
 * Startup broadcast receiver
 *
 * @author ebraminio
 */
public class BroadcastReceivers extends BroadcastReceiver implements MediaPlayer.OnCompletionListener {
    private static final String TAG = BroadcastReceivers.class.getName();
    private MediaPlayer mediaPlayer;
    private Context context;
    private UpdateUtils updateUtils;
    private Utils utils;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        utils = Utils.getInstance(context);
        updateUtils = UpdateUtils.getInstance(context);

        if (intent != null && intent.getAction() != null && !TextUtils.isEmpty(intent.getAction())) {
            if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) ||
                    intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED) ||
                    intent.getAction().equals(Constants.BROADCAST_RESTART_APP)) {

                if (!Utils.getInstance(context).isServiceRunning(ApplicationService.class)) {
                    context.startService(new Intent(context, ApplicationService.class));
                }

            } else if (intent.getAction().equals(Intent.ACTION_TIME_TICK) ||
                    intent.getAction().equals(Intent.ACTION_TIME_CHANGED) ||
                    intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {

                updateUtils.update(false);

            } else if (intent.getAction().equals(Intent.ACTION_DATE_CHANGED) ||
                    intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {

                updateUtils.update(true);
                utils.loadApp();

            } else if (intent.getAction().equals(Constants.BROADCAST_ALARM)) {
                startAthanActivity(intent.getStringExtra(Constants.KEY_EXTRA_PRAYER_KEY));
                play();

            } else if (intent.getAction().equals(Constants.ACTION_STOP_ALARM)) {
                stop();
            }
        }
    }

    private void startAthanActivity(String string) {
        Intent intent = new Intent(context, AthanView.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.KEY_EXTRA_PRAYER_KEY, string);
        context.startActivity(intent);
    }

    private void play() {
        try {
            AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setDataSource(context, utils.getAthanUri());
            mediaPlayer.prepare();
            mediaPlayer.start();
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, utils.getAthanVolume(), 0);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.release();
    }

    private void stop() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
