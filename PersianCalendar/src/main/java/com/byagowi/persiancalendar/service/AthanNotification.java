package com.byagowi.persiancalendar.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.byagowi.persiancalendar.BuildConfig;
import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.utils.Utils;

import java.util.concurrent.TimeUnit;

import androidx.core.app.NotificationCompat;

public class AthanNotification extends Service {

    private static final int NOTIFICATION_ID = 1002;
    private static String NOTIFICATION_CHANNEL_ID = String.valueOf(NOTIFICATION_ID);

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel =
                        new NotificationChannel(NOTIFICATION_CHANNEL_ID, getString(R.string.app_name),
                                NotificationManager.IMPORTANCE_DEFAULT);

                notificationChannel.setDescription(getString(R.string.app_name));
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.GREEN);
                notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }

            String title = intent == null
                    ? ""
                    : getString(Utils.getPrayTimeText(intent.getStringExtra(Constants.KEY_EXTRA_PRAYER_KEY)));
            String cityName = Utils.getCityName(this, false);
            String subtitle = TextUtils.isEmpty(cityName)
                    ? ""
                    : getString(R.string.in_city_time) + " " + cityName;

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,
                    NOTIFICATION_CHANNEL_ID);
            notificationBuilder.setAutoCancel(true)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.sun)
                    .setContentTitle(title)
                    .setContentText(subtitle);

            notificationBuilder.setDefaults(NotificationCompat.DEFAULT_VIBRATE);
            notificationBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND);

            Context appContext = getApplicationContext();
            if (appContext != null &&
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N || BuildConfig.DEBUG)) {
                RemoteViews cv = new RemoteViews(appContext.getPackageName(), Utils.isLocaleRTL()
                        ? R.layout.custom_notification
                        : R.layout.custom_notification_ltr);
                cv.setTextViewText(R.id.title, title);
                if (TextUtils.isEmpty(subtitle)) {
                    cv.setViewVisibility(R.id.body, View.GONE);
                } else {
                    cv.setTextViewText(R.id.body, subtitle);
                }

                notificationBuilder = notificationBuilder
                        .setCustomContentView(cv)
                        .setStyle(new NotificationCompat.DecoratedCustomViewStyle());
            }

            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

            new Handler().postDelayed(() -> {
                notificationManager.cancel(NOTIFICATION_ID);
                stopSelf();
            }, TimeUnit.MINUTES.toMillis(5));
        }


        return super.onStartCommand(intent, flags, startId);
    }
}
