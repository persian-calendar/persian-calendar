package com.byagowi.persiancalendar.service;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.byagowi.persiancalendar.BuildConfig;
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
//            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//            if (audioManager != null) {
//                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, Utils.getAthanVolume(this), 0);
//            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel =
                        new NotificationChannel(NOTIFICATION_CHANNEL_ID, getString(R.string.app_name),
                                NotificationManager.IMPORTANCE_DEFAULT);

//                AudioAttributes att = new AudioAttributes.Builder()
//                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
//                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
//                        .setLegacyStreamType(AudioManager.STREAM_ALARM)
//                        .build();
                notificationChannel.setDescription(getString(R.string.app_name));
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.GREEN);
                notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                notificationChannel.enableVibration(true);
//                notificationChannel.setSound(Utils.getAthanUri(getApplicationContext()), att);
                notificationManager.createNotificationChannel(notificationChannel);
            }

            String title = getString(Utils.getPrayTimeText(
                    intent.getStringExtra(Constants.KEY_EXTRA_PRAYER_KEY)));
            String subtitle = getString(R.string.in_city_time) + " " +
                    Utils.getCityName(this, true);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,
                    NOTIFICATION_CHANNEL_ID);
            notificationBuilder.setAutoCancel(true)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.sun)
//                    .setSound(Utils.getAthanUri(getApplicationContext()), AudioManager.STREAM_ALARM)
                    .setContentTitle(title)
                    .setContentText(subtitle);

            notificationBuilder.setDefaults(NotificationCompat.DEFAULT_VIBRATE);
            notificationBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N || BuildConfig.DEBUG) {
                RemoteViews cv = new RemoteViews(getApplicationContext().getPackageName(),
                        Utils.isLocaleRTL()
                                ? R.layout.custom_notification
                                : R.layout.custom_notification_ltr);
                cv.setTextViewText(R.id.title, title);
                cv.setTextViewText(R.id.body, subtitle);

                notificationBuilder = notificationBuilder
                        .setCustomContentView(cv)
                        .setStyle(new NotificationCompat.DecoratedCustomViewStyle());
            }

            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    notificationManager.cancel(NOTIFICATION_ID);
                    stopSelf();
                }
            }, TimeUnit.MINUTES.toMillis(5));
        }


        return super.onStartCommand(intent, flags, startId);
    }

}