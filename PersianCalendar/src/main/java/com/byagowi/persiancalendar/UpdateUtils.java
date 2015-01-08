package com.byagowi.persiancalendar;

import java.util.Calendar;
import java.util.Date;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import calendar.CivilDate;
import calendar.DateConverter;
import calendar.PersianDate;

import com.google.android.apps.dashclock.api.ExtensionData;

public class UpdateUtils {
    private static UpdateUtils myInstance;

    public static UpdateUtils getInstance() {
        if (myInstance == null) {
            myInstance = new UpdateUtils();
        }
        return myInstance;
    }

    private UpdateUtils() {
    }

    //

    private final Utils utils = Utils.getInstance();

    private static final int NOTIFICATION_ID = 1001;
    private NotificationManager mNotificationManager;
    private Bitmap largeIcon;

    public void update(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        char[] digits = utils.preferredDigits(context);
        boolean iranTime = prefs.getBoolean("IranTime", false);
        Calendar calendar = utils.makeCalendarFromDate(new Date(), iranTime);
        CivilDate civil = new CivilDate(calendar);
        PersianDate persian = DateConverter.civilToPersian(civil);
        persian.setDari(utils.isDariVersion(context));

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent launchAppPendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //
        // Widgets
        //
        //
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        RemoteViews remoteViews1 = new RemoteViews(context.getPackageName(),
                R.layout.widget1x1);
        RemoteViews remoteViews4 = new RemoteViews(context.getPackageName(),
                R.layout.widget4x1);
        int color = prefs.getInt("WidgetTextColor", Color.WHITE);

        // Widget 1x1
        remoteViews1.setTextColor(R.id.textPlaceholder1_1x1, color);
        remoteViews1.setTextColor(R.id.textPlaceholder2_1x1, color);
        remoteViews1.setTextViewText(R.id.textPlaceholder1_1x1,
                utils.formatNumber(persian.getDayOfMonth(), digits));
        remoteViews1.setTextViewText(R.id.textPlaceholder2_1x1,
                utils.textShaper(persian.getMonthName()));
        remoteViews1.setOnClickPendingIntent(R.id.widget_layout1x1,
                launchAppPendingIntent);
        manager.updateAppWidget(new ComponentName(context, Widget1x1.class),
                remoteViews1);

        // Widget 4x1
        remoteViews4.setTextColor(R.id.textPlaceholder1_4x1, color);
        remoteViews4.setTextColor(R.id.textPlaceholder2_4x1, color);
        remoteViews4.setTextColor(R.id.textPlaceholder3_4x1, color);

        String text1;
        String text2;
        String text3 = "";
        text1 = utils.getDayOfWeekName(civil.getDayOfWeek());
        String dayTitle = utils.dateToString(persian, digits);
        text2 = dayTitle + utils.PERSIAN_COMMA + " "
                + utils.dateToString(civil, digits);

        boolean enableClock = prefs.getBoolean("WidgetClock", true);
        if (enableClock) {
            text2 = text1 + " " + text2;
            boolean in24 = prefs.getBoolean("WidgetIn24", true);
            text1 = utils.getPersianFormattedClock(calendar, digits, in24);
            if (iranTime) {
                text3 = "(" + utils.irdt + ")";
            }
        }

        remoteViews4.setTextViewText(R.id.textPlaceholder1_4x1,
                utils.textShaper(text1));
        remoteViews4.setTextViewText(R.id.textPlaceholder2_4x1,
                utils.textShaper(text2));
        remoteViews4.setTextViewText(R.id.textPlaceholder3_4x1,
                utils.textShaper(text3));

        remoteViews4.setOnClickPendingIntent(R.id.widget_layout4x1,
                launchAppPendingIntent);
        manager.updateAppWidget(new ComponentName(context, Widget4x1.class),
                remoteViews4);

        //
        // Permanent Notification Bar and DashClock Data Extension Update
        //
        //
        String status = persian.getMonthName();

        String title = utils.getDayOfWeekName(civil.getDayOfWeek()) + " "
                + utils.dateToString(persian, digits);

        String body = utils.dateToString(civil, digits)
                + utils.PERSIAN_COMMA
                + " "
                + utils.dateToString(DateConverter.civilToIslamic(civil),
                digits);

        int icon = utils.getDayIconResource(persian.getDayOfMonth());

        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (prefs.getBoolean("NotifyDate", true)) {
            if (largeIcon == null)
                largeIcon = BitmapFactory.decodeResource(
                        context.getResources(), R.drawable.launcher_icon);

            mNotificationManager.notify(
                    NOTIFICATION_ID,
                    new NotificationCompat.Builder(context)
                            .setPriority(NotificationCompat.PRIORITY_LOW)
                            .setOngoing(true).setLargeIcon(largeIcon)
                            .setSmallIcon(icon)
                            .setContentIntent(launchAppPendingIntent)
                            .setContentText(utils.textShaper(body))
                            .setContentTitle(utils.textShaper(title)).build());
        } else {
            mNotificationManager.cancel(NOTIFICATION_ID);
        }

        mExtensionData = new ExtensionData().visible(true).icon(icon)
                .status(utils.textShaper(status))
                .expandedTitle(utils.textShaper(title))
                .expandedBody(utils.textShaper(body)).clickIntent(intent);
    }

    private ExtensionData mExtensionData;

    public ExtensionData getExtensionData() {
        return mExtensionData;
    }

}
