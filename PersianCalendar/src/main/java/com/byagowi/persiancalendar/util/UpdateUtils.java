package com.byagowi.persiancalendar.util;

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
    private static UpdateUtils myInstance;
    private Context context;
    private PersianDate pastDate;

    private ExtensionData mExtensionData;

    private UpdateUtils(Context context) {
        this.context = context;
    }

    public static UpdateUtils getInstance(Context context) {
        if (myInstance == null) {
            myInstance = new UpdateUtils(context);
        }
        return myInstance;
    }

    boolean firstTime = true;

    public void update(boolean updateDate) {
        Log.d("UpdateUtils", "update");
        Utils utils = Utils.getInstance(context);
        utils.changeAppLanguage(context);
        if (firstTime) {
            utils.loadLanguageResource();
            firstTime = false;
        }
        Calendar calendar = utils.makeCalendarFromDate(new Date());
        CivilDate civil = new CivilDate(calendar);
        PersianDate persian = utils.getToday();

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
        String colorInt = utils.getSelectedWidgetTextColor();
        int color = Color.parseColor(colorInt);

        // Widget 1x1
        remoteViews1.setTextColor(R.id.textPlaceholder1_1x1, color);
        remoteViews1.setTextColor(R.id.textPlaceholder2_1x1, color);
        remoteViews1.setTextViewText(R.id.textPlaceholder1_1x1,
                utils.formatNumber(persian.getDayOfMonth()));
        remoteViews1.setTextViewText(R.id.textPlaceholder2_1x1,
                utils.shape(utils.getMonthName(persian)));
        remoteViews1.setOnClickPendingIntent(R.id.widget_layout1x1, launchAppPendingIntent);
        manager.updateAppWidget(new ComponentName(context, Widget1x1.class), remoteViews1);

        // Widget 4x1
        remoteViews4.setTextColor(R.id.textPlaceholder1_4x1, color);
        remoteViews4.setTextColor(R.id.textPlaceholder2_4x1, color);
        remoteViews4.setTextColor(R.id.textPlaceholder3_4x1, color);

        String text1;
        String text2;
        String text3 = "";
        String weekDayName = utils.getWeekDayName(civil);
        String persianDate = utils.dateToString(persian);
        String civilDate = utils.dateToString(civil);
        String date = persianDate + Constants.PERSIAN_COMMA + " " + civilDate;

        String time = utils.getPersianFormattedClock(calendar);
        boolean enableClock = utils.isWidgetClock();

        if (enableClock) {
            text2 = weekDayName + " " + date;
            text1 = time;
            if (utils.iranTime) {
                text3 = "(" + context.getString(R.string.iran_time) + ")";
            }
        } else {
            text1 = weekDayName;
            text2 = date;
        }

        remoteViews4.setTextViewText(R.id.textPlaceholder1_4x1, utils.shape(text1));
        remoteViews4.setTextViewText(R.id.textPlaceholder2_4x1, utils.shape(text2));
        remoteViews4.setTextViewText(R.id.textPlaceholder3_4x1, utils.shape(text3));
        remoteViews4.setOnClickPendingIntent(R.id.widget_layout4x1, launchAppPendingIntent);
        manager.updateAppWidget(new ComponentName(context, Widget4x1.class), remoteViews4);


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

        if (pastDate == null || !pastDate.equals(persian) || updateDate) {
            Log.d("UpdateUtils", "change date");
            pastDate = persian;

            utils.loadAlarms();

            owghat = utils.getNextOghatTime(currentClock, true);

            String holidays = utils.getEventsTitle(persian, true);

            if (!TextUtils.isEmpty(holidays)) {
                remoteViews2.setTextViewText(R.id.holiday_2x2, utils.shape(holidays));
                remoteViews2.setViewVisibility(R.id.holiday_2x2, View.VISIBLE);
            } else {
                remoteViews2.setViewVisibility(R.id.holiday_2x2, View.GONE);
            }

            String events = utils.getEventsTitle(persian, false);

            if (!TextUtils.isEmpty(events)) {
                remoteViews2.setTextViewText(R.id.event_2x2, utils.shape(events));
                remoteViews2.setViewVisibility(R.id.event_2x2, View.VISIBLE);
            } else {
                remoteViews2.setViewVisibility(R.id.event_2x2, View.GONE);
            }
        } else {
            owghat = utils.getNextOghatTime(currentClock, false);
        }

        if (owghat != null) {
            remoteViews2.setTextViewText(R.id.owghat_2x2, utils.shape(owghat));
            remoteViews2.setViewVisibility(R.id.owghat_2x2, View.VISIBLE);
        } else {
            remoteViews2.setViewVisibility(R.id.owghat_2x2, View.GONE);
        }

        remoteViews2.setTextViewText(R.id.time_2x2, utils.shape(text1));
        remoteViews2.setTextViewText(R.id.date_2x2, utils.shape(text2));

        remoteViews2.setOnClickPendingIntent(R.id.widget_layout2x2, launchAppPendingIntent);
        manager.updateAppWidget(new ComponentName(context, Widget2x2.class), remoteViews2);

        //
        // Permanent Notification Bar and DashClock Data Extension Update
        //
        //
        String status = utils.getMonthName(persian);

        String title = utils.getWeekDayName(civil) + Constants.PERSIAN_COMMA + " " +
                utils.dateToString(persian);

        String body = utils.dateToString(civil) + Constants.PERSIAN_COMMA + " "
                + utils.dateToString(DateConverter.civilToIslamic(civil, utils.getIslamicOffset()));

        // Prepend a right-to-left mark character to Android with sane text rendering stack
        // to resolve a bug seems some Samsung devices have with characters with weak direction,
        // digits being at the first of string on
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            title = Constants.RLM + title;
            body = Constants.RLM + body;
        }

        int icon = utils.getDayIconResource(persian.getDayOfMonth());

        ApplicationService applicationService = ApplicationService.getInstance();
        if (applicationService != null && utils.isNotifyDate()) {
            applicationService.startForeground(
                    NOTIFICATION_ID,
                    new NotificationCompat.Builder(context)
                            .setPriority(NotificationCompat.PRIORITY_LOW)
                            .setOngoing(true)
                            .setSmallIcon(icon)
                            .setWhen(0)
                            .setContentIntent(launchAppPendingIntent)
                            .setContentText(utils.shape(body))
                            .setContentTitle(utils.shape(title))
                            .setColor(0xFF607D8B) // permanent services color
                            .build());
        }

        mExtensionData = new ExtensionData().visible(true).icon(icon)
                .status(utils.shape(status))
                .expandedTitle(utils.shape(title))
                .expandedBody(utils.shape(body)).clickIntent(intent);
    }

    public ExtensionData getExtensionData() {
        return mExtensionData;
    }

}
