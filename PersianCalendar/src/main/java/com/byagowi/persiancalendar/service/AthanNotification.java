package com.byagowi.persiancalendar.service;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.IBinder;
import android.util.TimeUtils;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.util.Utils;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

@SuppressLint("Registered")
public class AthanNotification extends Service {

    @Nullable
    public static AthanNotification getInstance() {
        return null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static final int NOTIFICATION_ID = 1002;
    String NOTIFICATION_CHANNEL_ID = "1002";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel =
                        new NotificationChannel(NOTIFICATION_CHANNEL_ID, getString(R.string.app_name),
                                NotificationManager.IMPORTANCE_DEFAULT);

                AudioAttributes att = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build();
                notificationChannel.setDescription(getString(R.string.app_name));
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.GREEN);
                notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                notificationChannel.enableVibration(true);
                notificationChannel.setSound(Utils.getAthanUri(getApplicationContext()), att);
                notificationManager.createNotificationChannel(notificationChannel);
            }

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,
                    NOTIFICATION_CHANNEL_ID);
            notificationBuilder.setAutoCancel(true)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.kaaba)
                    .setSound(Utils.getAthanUri(getApplicationContext()))
                    .setContentTitle(getString(Utils.getPrayTimeText(
                            intent.getStringExtra(Constants.KEY_EXTRA_PRAYER_KEY))))
                    .setContentText(getString(R.string.in_city_time) + " " +
                            Utils.getCityName(this, true));

            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    notificationManager.cancel(NOTIFICATION_ID);
                    stopSelf();
                }
            };
            timer.schedule(task, TimeUnit.MINUTES.toMillis(1));
        }


        return super.onStartCommand(intent, flags, startId);
    }

}