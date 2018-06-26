package com.byagowi.persiancalendar.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.Widget1x1;
import com.byagowi.persiancalendar.Widget2x2;
import com.byagowi.persiancalendar.Widget4x1;
import com.byagowi.persiancalendar.service.ApplicationService;
import com.byagowi.persiancalendar.view.activity.MainActivity;
import com.github.praytimes.Clock;
import com.google.android.apps.dashclock.api.ExtensionData;

import java.util.Calendar;
import java.util.Date;

import calendar.CivilDate;
import calendar.DateConverter;
import calendar.PersianDate;

public class UpdateUtils {
    private static final int NOTIFICATION_ID = 1001;
    private static PersianDate pastDate;
    private static ExtensionData mExtensionData;

    public static void update(Context context, boolean updateDate) {
        Log.d("UpdateUtils", "update");
        Utils.changeAppLanguage(context);
        Calendar calendar = Utils.makeCalendarFromDate(new Date());
        CivilDate civil = new CivilDate(calendar);
        PersianDate persian = Utils.getToday();

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent launchAppPendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        //
        // Widgets
        //
        //
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        RemoteViews remoteViews1 = new RemoteViews(context.getPackageName(), R.layout.widget1x1);
        RemoteViews remoteViews4 = new RemoteViews(context.getPackageName(), R.layout.widget4x1);
        RemoteViews remoteViews2 = new RemoteViews(context.getPackageName(), R.layout.widget2x2);
        String colorInt = Utils.getSelectedWidgetTextColor();
        int color = Color.parseColor(colorInt);

        // Widget 1x1
        ComponentName widget1x1 = new ComponentName(context, Widget1x1.class),
                widget4x1 = new ComponentName(context, Widget4x1.class),
                widget2x2 = new ComponentName(context, Widget2x2.class);

        if (manager.getAppWidgetIds(widget1x1).length != 0) {
            remoteViews1.setTextColor(R.id.textPlaceholder1_1x1, color);
            remoteViews1.setTextColor(R.id.textPlaceholder2_1x1, color);
            remoteViews1.setTextViewText(R.id.textPlaceholder1_1x1,
                    Utils.formatNumber(persian.getDayOfMonth()));
            remoteViews1.setTextViewText(R.id.textPlaceholder2_1x1,
                    Utils.getMonthName(persian));
            remoteViews1.setOnClickPendingIntent(R.id.widget_layout1x1, launchAppPendingIntent);
            manager.updateAppWidget(widget1x1, remoteViews1);
        }

        if (pastDate == null || !pastDate.equals(persian) || updateDate) {
            Log.d("UpdateUtils", "date has changed");
            pastDate = persian;

            Utils.initUtils(context);
            updateDate = true;
        }

        if (manager.getAppWidgetIds(widget4x1).length != 0 ||
                manager.getAppWidgetIds(widget2x2).length != 0) {
            // Widget 4x1
            remoteViews4.setTextColor(R.id.textPlaceholder1_4x1, color);
            remoteViews4.setTextColor(R.id.textPlaceholder2_4x1, color);
            remoteViews4.setTextColor(R.id.textPlaceholder3_4x1, color);

            String text1;
            String text2;
            String text3 = "";
            String weekDayName = Utils.getWeekDayName(civil);
            String persianDate = Utils.dateToString(context, persian);
            String civilDate = Utils.dateToString(context, civil);
            String date = persianDate + Constants.PERSIAN_COMMA + " " + civilDate;

            String time = Utils.getPersianFormattedClock(calendar);
            boolean enableClock = Utils.isWidgetClock();

            if (enableClock) {
                text2 = weekDayName + " " + date;
                text1 = time;
                if (Utils.isIranTime()) {
                    text3 = "(" + context.getString(R.string.iran_time) + ")";
                }
            } else {
                text1 = weekDayName;
                text2 = date;
            }

            remoteViews4.setTextViewText(R.id.textPlaceholder1_4x1, text1);
            remoteViews4.setTextViewText(R.id.textPlaceholder2_4x1, text2);
            remoteViews4.setTextViewText(R.id.textPlaceholder3_4x1, text3);
            remoteViews4.setOnClickPendingIntent(R.id.widget_layout4x1, launchAppPendingIntent);
            manager.updateAppWidget(widget4x1, remoteViews4);

            // Widget 2x2
            remoteViews2.setTextColor(R.id.time_2x2, color);
            remoteViews2.setTextColor(R.id.date_2x2, color);
            remoteViews2.setTextColor(R.id.event_2x2, color);
            remoteViews2.setTextColor(R.id.owghat_2x2, color);

            if (enableClock) {
                text2 = weekDayName + " " + persianDate;
                text1 = time;
            } else {
                text1 = weekDayName;
                text2 = persianDate;
            }

            Clock currentClock =
                    new Clock(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));

            String owghat;

            if (updateDate) {
                owghat = Utils.getNextOghatTime(context, currentClock, true);

                String holidays = Utils.getEventsTitle(context, persian, true);

                if (!TextUtils.isEmpty(holidays)) {
                    remoteViews2.setTextViewText(R.id.holiday_2x2, holidays);
                    remoteViews2.setViewVisibility(R.id.holiday_2x2, View.VISIBLE);
                } else {
                    remoteViews2.setViewVisibility(R.id.holiday_2x2, View.GONE);
                }

                String events = Utils.getEventsTitle(context, persian, false);

                if (!TextUtils.isEmpty(events)) {
                    remoteViews2.setTextViewText(R.id.event_2x2, events);
                    remoteViews2.setViewVisibility(R.id.event_2x2, View.VISIBLE);
                } else {
                    remoteViews2.setViewVisibility(R.id.event_2x2, View.GONE);
                }
            } else {
                owghat = Utils.getNextOghatTime(context, currentClock, false);
            }

            if (owghat != null) {
                remoteViews2.setTextViewText(R.id.owghat_2x2, owghat);
                remoteViews2.setViewVisibility(R.id.owghat_2x2, View.VISIBLE);
            } else {
                remoteViews2.setViewVisibility(R.id.owghat_2x2, View.GONE);
            }

            remoteViews2.setTextViewText(R.id.time_2x2, text1);
            remoteViews2.setTextViewText(R.id.date_2x2, text2);

            remoteViews2.setOnClickPendingIntent(R.id.widget_layout2x2, launchAppPendingIntent);
            manager.updateAppWidget(widget2x2, remoteViews2);
        }

        //
        // Permanent Notification Bar and DashClock Data Extension Update
        //
        //
        String status = Utils.getMonthName(persian);

        String title = Utils.getWeekDayName(civil) + Constants.PERSIAN_COMMA + " " +
                Utils.dateToString(context, persian);

        String body = Utils.dateToString(context, civil) + Constants.PERSIAN_COMMA + " "
                + Utils.dateToString(context, DateConverter.civilToIslamic(civil, Utils.getIslamicOffset()));

        // Prepend a right-to-left mark character to Android with sane text rendering stack
        // to resolve a bug seems some Samsung devices have with characters with weak direction,
        // digits being at the first of string on
        if ((Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) &&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)) {
            title = Constants.RLM + title;
            body = Constants.RLM + body;
        }

        int icon = Utils.getDayIconResource(persian.getDayOfMonth());

        ApplicationService applicationService = ApplicationService.getInstance();
        if (applicationService != null && Utils.isNotifyDate()) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_LOW;
                NotificationChannel mChannel = new NotificationChannel(String.valueOf(NOTIFICATION_ID), context.getString(R.string.app_name), importance);
                mChannel.setShowBadge(false);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(mChannel);
                }
            }

            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.custom_notification);
            rv.setTextViewText(R.id.title, title);
            rv.setTextViewText(R.id.body, body);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, String.valueOf(NOTIFICATION_ID))
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setSmallIcon(icon)
                    .setOngoing(true)
                    .setWhen(0)
                    .setContentIntent(launchAppPendingIntent)
                    .setVisibility(Utils.isNotifyDateOnLockScreen()
                            ? NotificationCompat.VISIBILITY_PUBLIC
                            : NotificationCompat.VISIBILITY_SECRET)
                    .setColor(0xFF607D8B)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setCustomContentView(rv)
                    .setStyle(new NotificationCompat.DecoratedCustomViewStyle());
            applicationService.startForeground(NOTIFICATION_ID, builder.build());
        }

        mExtensionData = new ExtensionData().visible(true).icon(icon)
                .status(status)
                .expandedTitle(title)
                .expandedBody(body).clickIntent(intent);
    }

    public static ExtensionData getExtensionData() {
        return mExtensionData;
    }

}
