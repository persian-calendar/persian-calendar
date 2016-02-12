package com.byagowi.persiancalendar.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.Utils;
import com.byagowi.persiancalendar.util.NotificationID;
import com.byagowi.persiancalendar.view.AthanView;

import java.io.IOException;

public class AlarmReceiver extends BroadcastReceiver implements MediaPlayer.OnCompletionListener {
    private static final String TAG = "AlarmReceiver";
    private static MediaPlayer mediaPlayer;
    private static NotificationManager notificationManager;
    private Utils utils = Utils.getInstance();

    public static final int NOTIFICATION_ID = NotificationID.getID();
    public static final String ACTION_STOP_ALARM = "com.byagowi.persiancalendar.stop_athan";
    public static final String KEY_EXTRA_PRAYER_KEY = "prayer_name";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "received an alarm trigger. playing sound file.");
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent athanViewIntent = new Intent(context, AthanView.class);
        athanViewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        athanViewIntent.putExtra(KEY_EXTRA_PRAYER_KEY, intent.getStringExtra(KEY_EXTRA_PRAYER_KEY));
        context.startActivity(athanViewIntent);

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.playing_athan))
                .setSmallIcon(R.mipmap.new_icon)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel,
                        context.getString(R.string.tap_to_stop_athan),
                        PendingIntent.getBroadcast(context, 0, new Intent(ACTION_STOP_ALARM), 0));
        notificationManager.notify(NOTIFICATION_ID, builder.build());

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setDataSource(context, utils.getAthanUri(context));
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            Log.e(TAG, "", e);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.release();
        mp = null;

        hideAthan();
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private static void hideAthan() {
        if (AthanView.displayed) {
            AthanView.hideAthan();
        }
    }

    public static class StopAthanPlaybackReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "received request to stop alarm");
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
                hideAthan();
                notificationManager.cancel(NOTIFICATION_ID);
            }
        }
    }
}
