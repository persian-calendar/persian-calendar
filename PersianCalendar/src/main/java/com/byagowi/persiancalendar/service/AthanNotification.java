package com.byagowi.persiancalendar.service;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.IBinder;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.util.Utils;

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

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    public void onCreate() {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_MAX);

            AudioAttributes att = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            notificationChannel.setDescription(getString(R.string.app_name));
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationChannel.setSound(Utils.getAthanUri(getApplicationContext()),att);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.kaaba)
                .setSound(Utils.getAthanUri(getApplicationContext()))
                .setContentTitle(getString(R.string.in_city_time))
                .setContentText(Utils.getCityName(this, true));

        assert notificationManager != null;
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

        this.stopSelf();
    }

}