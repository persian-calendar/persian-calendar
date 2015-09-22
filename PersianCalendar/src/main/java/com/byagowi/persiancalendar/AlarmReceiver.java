package com.byagowi.persiancalendar;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.byagowi.persiancalendar.util.NotificationID;

import java.io.IOException;

public class AlarmReceiver extends BroadcastReceiver implements MediaPlayer.OnCompletionListener {
    private static final String TAG = "AlarmReceiver";
    private static final int NOTIFICATION_ID = NotificationID.getID();
    private static MediaPlayer mediaPlayer;
    private static NotificationManager notificationManager;
    private Utils utils = Utils.getInstance();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "received an alarm trigger. playing sound file.");

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.playing_athan))
                .setSmallIcon(R.drawable.launcher_icon)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel,
                        context.getString(R.string.tap_to_stop_athan),
                        PendingIntent.getBroadcast(context, 0, new Intent(context, StopAthanPlaybackReceiver.class), 0));
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

        notificationManager.cancel(NOTIFICATION_ID);
    }

    public static class StopAthanPlaybackReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();

                notificationManager.cancel(NOTIFICATION_ID);
            }
        }
    }
}
