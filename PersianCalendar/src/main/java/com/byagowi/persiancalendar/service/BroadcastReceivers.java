package com.byagowi.persiancalendar.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
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
    private static MediaPlayer mediaPlayer;
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
                    intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {

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

            } else if (intent.getAction().equals(Constants.BROADCAST_ALARM)) {
                startAthanActivity(intent.getStringExtra(Constants.KEY_EXTRA_PRAYER_KEY));
                play();

            } else if (intent.getAction().equals(Constants.ACTION_STOP_ALARM)) {
                stop();
            }
        }
    }

    private void startAthanActivity(String string) {
        Intent athanViewIntent = new Intent(context, AthanView.class);
        athanViewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        athanViewIntent.putExtra(Constants.KEY_EXTRA_PRAYER_KEY, string);
        context.startActivity(athanViewIntent);
    }

    private void play() {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setDataSource(context, utils.getAthanUri());
            mediaPlayer.prepare();
            mediaPlayer.start();
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
