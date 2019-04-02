//package com.byagowi.persiancalendar.service;
//
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.Color;
//import android.os.Build;
//import android.os.IBinder;
//import android.widget.RemoteViews;
//
//import com.byagowi.persiancalendar.BuildConfig;
//import com.byagowi.persiancalendar.Constants;
//import com.byagowi.persiancalendar.R;
//import com.byagowi.persiancalendar.entities.Reminder;
//import com.byagowi.persiancalendar.utils.ReminderUtils;
//import com.byagowi.persiancalendar.utils.Utils;
//
//import java.util.Random;
//import java.util.concurrent.TimeUnit;
//
//import androidx.core.app.NotificationCompat;
//
//public class ReminderNotification extends Service {
//
//    private static final int NOTIFICATION_ID = 1003;
//    private static String NOTIFICATION_CHANNEL_ID = String.valueOf(NOTIFICATION_ID);
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        if (notificationManager != null) {
//            String title = getString(R.string.reminder);
//            String subtitle = "";
//            if (intent != null) {
//                Reminder reminder = Utils.getReminderById(intent.getIntExtra(Constants.REMINDER_ID, -1));
//                if (reminder != null) {
//                    ReminderUtils.increaseReminderCount(getApplicationContext(), reminder.id);
//                    subtitle = reminder.name;
//                }
//            }
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                NotificationChannel notificationChannel =
//                        new NotificationChannel(NOTIFICATION_CHANNEL_ID, getString(R.string.app_name),
//                                NotificationManager.IMPORTANCE_DEFAULT);
//
//                notificationChannel.setDescription(getString(R.string.app_name));
//                notificationChannel.enableLights(true);
//                notificationChannel.setLightColor(Color.GREEN);
//                notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
//                notificationChannel.enableVibration(true);
//                notificationManager.createNotificationChannel(notificationChannel);
//            }
//
//            NotificationCompat.Builder notificationBuilder =
//                    new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
//                            .setAutoCancel(true)
//                            .setTimeoutAfter(TimeUnit.HOURS.toMillis(12))
//                            .setWhen(System.currentTimeMillis())
//                            .setShowWhen(true)
//                            .setSmallIcon(R.drawable.ic_alarm_raster)
//                            .setContentTitle(title)
//                            .setContentText(subtitle)
//                            .setDefaults(NotificationCompat.DEFAULT_VIBRATE | NotificationCompat.DEFAULT_SOUND);
//
//            Context appContext = getApplicationContext();
//            if (appContext != null &&
//                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N || BuildConfig.DEBUG)) {
//                RemoteViews cv = new RemoteViews(appContext.getPackageName(), Utils.isLocaleRTL()
//                        ? R.layout.custom_notification
//                        : R.layout.custom_notification_ltr);
//                cv.setTextViewText(R.id.title, title);
//                cv.setTextViewText(R.id.body, subtitle);
//
//                notificationBuilder = notificationBuilder
//                        .setCustomContentView(cv)
//                        .setStyle(new NotificationCompat.DecoratedCustomViewStyle());
//            }
//
//            notificationManager.notify(new Random().nextInt(), notificationBuilder.build());
//        }
//        return super.onStartCommand(intent, flags, startId);
//    }
//}
