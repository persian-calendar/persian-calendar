package com.byagowi.persiancalendar.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.RemoteViews;

import com.byagowi.persiancalendar.BuildConfig;
import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.Widget1x1;
import com.byagowi.persiancalendar.Widget2x2;
import com.byagowi.persiancalendar.Widget4x1;
import com.byagowi.persiancalendar.Widget4x2;
import com.byagowi.persiancalendar.calendar.AbstractDate;
import com.byagowi.persiancalendar.entities.AbstractEvent;
import com.byagowi.persiancalendar.entities.DeviceCalendarEvent;
import com.byagowi.persiancalendar.praytimes.Clock;
import com.byagowi.persiancalendar.service.ApplicationService;
import com.byagowi.persiancalendar.ui.MainActivity;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.IdRes;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;

import static com.byagowi.persiancalendar.utils.Utils.getClockFromStringId;
import static com.byagowi.persiancalendar.utils.Utils.getSpacedComma;
import static java.util.concurrent.TimeUnit.MINUTES;

public class UpdateUtils {
    private static final int NOTIFICATION_ID = 1001;
    private static AbstractDate pastDate;
    private static SparseArray<List<DeviceCalendarEvent>> deviceCalendarEvents = new SparseArray<>();
    @StringRes
    private static int[] timesOn4x2Shia = {R.string.fajr, R.string.dhuhr, R.string.sunset, R.string.maghrib, R.string.midnight};
    @StringRes
    private static int[] timesOn4x2Sunna = {R.string.fajr, R.string.dhuhr, R.string.asr, R.string.maghrib, R.string.isha};
    @IdRes
    private static int[] owghatPlaceHolderId = {
            R.id.textPlaceholder4owghat_1_4x2, R.id.textPlaceholder4owghat_2_4x2,
            R.id.textPlaceholder4owghat_3_4x2, R.id.textPlaceholder4owghat_4_4x2,
            R.id.textPlaceholder4owghat_5_4x2
    };

    public static void setDeviceCalendarEvents(Context context) {
        try {
            deviceCalendarEvents = Utils.readDayDeviceEvents(context, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void update(Context context, boolean updateDate) {
        Log.d("UpdateUtils", "update");
        Utils.applyAppLanguage(context);
        Calendar calendar = Utils.makeCalendarFromDate(new Date());
        CalendarType mainCalendar = Utils.getMainCalendar();
        AbstractDate date = Utils.getTodayOfCalendar(mainCalendar);
        long jdn = date.toJdn();

        PendingIntent launchAppPendingIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_UPDATE_CURRENT);

        //
        // Widgets
        //
        //
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        String colorInt = Utils.getSelectedWidgetTextColor();
        int color = Color.parseColor(colorInt);

        // en-US is our only real LTR language for now
        boolean isRTL = Utils.isLocaleRTL();

        // Widget 1x1
        ComponentName widget1x1 = new ComponentName(context, Widget1x1.class),
                widget4x1 = new ComponentName(context, Widget4x1.class),
                widget4x2 = new ComponentName(context, Widget4x2.class),
                widget2x2 = new ComponentName(context, Widget2x2.class);

        if (manager.getAppWidgetIds(widget1x1).length != 0) {
            RemoteViews remoteViews1 = new RemoteViews(context.getPackageName(), R.layout.widget1x1);
            remoteViews1.setTextColor(R.id.textPlaceholder1_1x1, color);
            remoteViews1.setTextColor(R.id.textPlaceholder2_1x1, color);
            remoteViews1.setTextViewText(R.id.textPlaceholder1_1x1,
                    Utils.formatNumber(date.getDayOfMonth()));
            remoteViews1.setTextViewText(R.id.textPlaceholder2_1x1,
                    Utils.getMonthName(date));
            remoteViews1.setOnClickPendingIntent(R.id.widget_layout1x1, launchAppPendingIntent);
            manager.updateAppWidget(widget1x1, remoteViews1);
        }

        if (pastDate == null || !pastDate.equals(date) || updateDate) {
            Log.d("UpdateUtils", "date has changed");

            Utils.loadAlarms(context);
            pastDate = date;
            updateDate = true;
            setDeviceCalendarEvents(context);
        }

        String weekDayName = Utils.getWeekDayName(date);
        String title = Utils.dayTitleSummary(date);
        String shiftWorkTitle = Utils.getShiftWorkTitle(jdn, false);
        if (!TextUtils.isEmpty(shiftWorkTitle))
            title += " (" + shiftWorkTitle + ")";
        String subtitle = Utils.dateStringOfOtherCalendars(jdn, getSpacedComma());

        Clock currentClock = new Clock(calendar);
        String owghat = "";
        @StringRes
        int nextOwghatId = Utils.getNextOwghatTimeId(currentClock, updateDate);
        if (nextOwghatId != 0) {
            owghat = context.getString(nextOwghatId) + ": " +
                    Utils.getFormattedClock(Utils.getClockFromStringId(nextOwghatId), false);
            if (Utils.isShownOnWidgets("owghat_location")) {
                String cityName = Utils.getCityName(context, false);
                if (!TextUtils.isEmpty(cityName)) {
                    owghat = owghat + " (" + cityName + ")";
                }
            }
        }
        List<AbstractEvent> events = Utils.getEvents(jdn, deviceCalendarEvents);

        boolean enableClock = Utils.isWidgetClock() && Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1;
        boolean isCenterAligned = Utils.isCenterAlignWidgets();

        if (manager.getAppWidgetIds(widget4x1).length != 0 ||
                manager.getAppWidgetIds(widget2x2).length != 0) {
            RemoteViews remoteViews4, remoteViews2;
            if (enableClock) {
                if (!Utils.isIranTime()) {
                    remoteViews4 = new RemoteViews(context.getPackageName(), isCenterAligned ? R.layout.widget4x1_clock_center : R.layout.widget4x1_clock);
                    remoteViews2 = new RemoteViews(context.getPackageName(), isCenterAligned ? R.layout.widget2x2_clock_center : R.layout.widget2x2_clock);
                } else {
                    remoteViews4 = new RemoteViews(context.getPackageName(), isCenterAligned ? R.layout.widget4x1_clock_iran_center : R.layout.widget4x1_clock_iran);
                    remoteViews2 = new RemoteViews(context.getPackageName(), isCenterAligned ? R.layout.widget2x2_clock_iran_center : R.layout.widget2x2_clock_iran);
                }
            } else {
                remoteViews4 = new RemoteViews(context.getPackageName(), isCenterAligned ? R.layout.widget4x1_center : R.layout.widget4x1);
                remoteViews2 = new RemoteViews(context.getPackageName(), isCenterAligned ? R.layout.widget2x2_center : R.layout.widget2x2);
            }

            String mainDateString = Utils.formatDate(date);

            {
                // Widget 4x1
                remoteViews4.setTextColor(R.id.textPlaceholder1_4x1, color);
                remoteViews4.setTextColor(R.id.textPlaceholder2_4x1, color);
                remoteViews4.setTextColor(R.id.textPlaceholder3_4x1, color);

                String text2;
                String text3 = "";

                if (enableClock) {
                    text2 = title;
                    if (Utils.isIranTime()) {
                        text3 = "(" + context.getString(R.string.iran_time) + ")";
                    }
                } else {
                    remoteViews4.setTextViewText(R.id.textPlaceholder1_4x1, weekDayName);
                    text2 = mainDateString;
                }
                if (Utils.isShownOnWidgets("other_calendars")) {
                    text2 += getSpacedComma() + subtitle;
                }

                remoteViews4.setTextViewText(R.id.textPlaceholder2_4x1, text2);
                remoteViews4.setTextViewText(R.id.textPlaceholder3_4x1, text3);
                remoteViews4.setOnClickPendingIntent(R.id.widget_layout4x1, launchAppPendingIntent);
                manager.updateAppWidget(widget4x1, remoteViews4);
            }

            {
                String text2;
                // Widget 2x2
                remoteViews2.setTextColor(R.id.time_2x2, color);
                remoteViews2.setTextColor(R.id.date_2x2, color);
                remoteViews2.setTextColor(R.id.event_2x2, color);
                remoteViews2.setTextColor(R.id.owghat_2x2, color);

                if (enableClock) {
                    text2 = title;
                } else {
                    remoteViews2.setTextViewText(R.id.time_2x2, weekDayName);
                    text2 = mainDateString;
                }

                String holidays = Utils.getEventsTitle(events, true, true, true, isRTL);
                if (!TextUtils.isEmpty(holidays)) {
                    remoteViews2.setTextViewText(R.id.holiday_2x2, holidays);
                    if (Utils.isTalkBackEnabled()) {
                        remoteViews2.setContentDescription(R.id.holiday_2x2,
                                context.getString(R.string.holiday_reason) + " " +
                                        holidays);
                    }
                    remoteViews2.setViewVisibility(R.id.holiday_2x2, View.VISIBLE);
                } else {
                    remoteViews2.setViewVisibility(R.id.holiday_2x2, View.GONE);
                }

                String nonHolidays = Utils.getEventsTitle(events, false, true, true, isRTL);
                if (Utils.isShownOnWidgets("non_holiday_events") &&
                        !TextUtils.isEmpty(nonHolidays)) {
                    remoteViews2.setTextViewText(R.id.event_2x2, nonHolidays);
                    remoteViews2.setViewVisibility(R.id.event_2x2, View.VISIBLE);
                } else {
                    remoteViews2.setViewVisibility(R.id.event_2x2, View.GONE);
                }

                if (Utils.isShownOnWidgets("owghat") && !TextUtils.isEmpty(owghat)) {
                    remoteViews2.setTextViewText(R.id.owghat_2x2, owghat);
                    remoteViews2.setViewVisibility(R.id.owghat_2x2, View.VISIBLE);
                } else {
                    remoteViews2.setViewVisibility(R.id.owghat_2x2, View.GONE);
                }

                if (Utils.isShownOnWidgets("other_calendars")) {
                    text2 = text2 + "\n" + subtitle + "\n" +
                            AstronomicalUtils.INSTANCE.getZodiacInfo(context, jdn, true);
                }
                remoteViews2.setTextViewText(R.id.date_2x2, text2);

                remoteViews2.setOnClickPendingIntent(R.id.widget_layout2x2, launchAppPendingIntent);
                manager.updateAppWidget(widget2x2, remoteViews2);
            }
        }

        //region Widget 4x2
        if (manager.getAppWidgetIds(widget4x2).length != 0) {
            RemoteViews remoteViews4x2;
            if (enableClock) {
                if (!Utils.isIranTime()) {
                    remoteViews4x2 = new RemoteViews(context.getPackageName(), R.layout.widget4x2_clock);
                } else {
                    remoteViews4x2 = new RemoteViews(context.getPackageName(), R.layout.widget4x2_clock_iran);
                }
            } else {
                remoteViews4x2 = new RemoteViews(context.getPackageName(), R.layout.widget4x2);
            }

            remoteViews4x2.setTextColor(R.id.textPlaceholder0_4x2, color);
            remoteViews4x2.setTextColor(R.id.textPlaceholder1_4x2, color);
            remoteViews4x2.setTextColor(R.id.textPlaceholder2_4x2, color);

            String text2 = Utils.formatDate(date);
            if (enableClock)
                text2 = Utils.getWeekDayName(date) + "\n" + text2;
            else
                remoteViews4x2.setTextViewText(R.id.textPlaceholder0_4x2, weekDayName);

            if (Utils.isShownOnWidgets("other_calendars")) {
                text2 = text2 + "\n" + Utils.dateStringOfOtherCalendars(jdn, "\n");
            }

            remoteViews4x2.setTextViewText(R.id.textPlaceholder1_4x2, text2);

            if (nextOwghatId != 0) {
                @StringRes
                int[] timesOn4x2 = Utils.isShiaPrayTimeCalculationSelected() ? timesOn4x2Shia : timesOn4x2Sunna;
                // Set text of owghats
                for (int i = 0; i < owghatPlaceHolderId.length; ++i) {
                    remoteViews4x2.setTextViewText(owghatPlaceHolderId[i],
                            context.getString(timesOn4x2[i]) + "\n" +
                                    Utils.getFormattedClock(getClockFromStringId(timesOn4x2[i]), false));
                    remoteViews4x2.setTextColor(owghatPlaceHolderId[i],
                            timesOn4x2[i] == nextOwghatId ?
                                    Color.RED : color);
                }

                int difference = Utils.getClockFromStringId(nextOwghatId).toInt() - currentClock.toInt();
                if (difference < 0) difference = 60 * 24 - difference;

                int hrs = (int) (MINUTES.toHours(difference) % 24);
                int min = (int) (MINUTES.toMinutes(difference) % 60);

                String remainingTime;
                if (hrs == 0)
                    remainingTime = String.format(context.getString(R.string.n_minutes), Utils.formatNumber(min));
                else if (min == 0)
                    remainingTime = String.format(context.getString(R.string.n_hours), Utils.formatNumber(hrs));
                else
                    remainingTime = String.format(context.getString(R.string.n_minutes_and_hours), Utils.formatNumber(hrs), Utils.formatNumber(min));

                remoteViews4x2.setTextViewText(R.id.textPlaceholder2_4x2,
                        String.format(context.getString(R.string.n_till),
                                remainingTime, context.getString(nextOwghatId)));
                remoteViews4x2.setTextColor(R.id.textPlaceholder2_4x2, color);
            } else {
                remoteViews4x2.setTextViewText(R.id.textPlaceholder2_4x2, context.getString(R.string.ask_user_to_set_location));
                remoteViews4x2.setTextColor(R.id.textPlaceholder2_4x2, color);
            }

            remoteViews4x2.setOnClickPendingIntent(R.id.widget_layout4x2, launchAppPendingIntent);

            manager.updateAppWidget(widget4x2, remoteViews4x2);
        }
        //endregion


        //
        // Permanent Notification Bar and DashClock Data Extension Update
        //
        //

        // Prepend a right-to-left mark character to Android with sane text rendering stack
        // to resolve a bug seems some Samsung devices have with characters with weak direction,
        // digits being at the first of string on
        if (isRTL && (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) &&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)) {
            title = Constants.RLM + title;
            if (!TextUtils.isEmpty(subtitle)) {
                subtitle = Constants.RLM + subtitle;
            }
        }

        if (Utils.isNotifyDate()) {
            NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_LOW;
                NotificationChannel channel = new NotificationChannel(String.valueOf(NOTIFICATION_ID),
                        context.getString(R.string.app_name), importance);
                channel.setShowBadge(false);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
            }

            // Don't remove this condition checking ever
            if (Utils.isTalkBackEnabled()) {
                // Don't use isToday, per a feedback
                subtitle = Utils.getA11yDaySummary(context, jdn, false,
                        deviceCalendarEvents,
                        true, true, false);
                if (!TextUtils.isEmpty(owghat)) {
                    subtitle += getSpacedComma();
                    subtitle += owghat;
                }
            }

            NotificationCompat.Builder builder = new NotificationCompat
                    .Builder(context, String.valueOf(NOTIFICATION_ID))
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setSmallIcon(Utils.getDayIconResource(date.getDayOfMonth()))
                    .setOngoing(true)
                    .setWhen(0)
                    .setContentIntent(launchAppPendingIntent)
                    .setVisibility(Utils.isNotifyDateOnLockScreen()
                            ? NotificationCompat.VISIBILITY_PUBLIC
                            : NotificationCompat.VISIBILITY_SECRET)
                    .setColor(0xFF607D8B)
                    .setContentTitle(title)
                    .setContentText(subtitle);

            if (!Utils.isTalkBackEnabled() &&
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N || BuildConfig.DEBUG)) {
                RemoteViews cv = new RemoteViews(context.getPackageName(), isRTL
                        ? R.layout.custom_notification
                        : R.layout.custom_notification_ltr);
                cv.setTextViewText(R.id.title, title);
                cv.setTextViewText(R.id.body, subtitle);

                RemoteViews bcv = new RemoteViews(context.getPackageName(), isRTL
                        ? R.layout.custom_notification_big
                        : R.layout.custom_notification_big_ltr);
                bcv.setTextViewText(R.id.title, title);

                if (!TextUtils.isEmpty(subtitle)) {
                    bcv.setTextViewText(R.id.body, subtitle);
                } else {
                    bcv.setViewVisibility(R.id.body, View.GONE);
                }

                String holidays = Utils.getEventsTitle(events, true, true, true, isRTL);
                if (!TextUtils.isEmpty(holidays)) {
                    bcv.setTextViewText(R.id.holidays, holidays);
                } else {
                    bcv.setViewVisibility(R.id.holidays, View.GONE);
                }
                String nonHolidays = Utils.getEventsTitle(events, false, true, true, isRTL);
                if (Utils.isShownOnWidgets("non_holiday_events") &&
                        !TextUtils.isEmpty(nonHolidays)) {
                    bcv.setTextViewText(R.id.nonholidays, nonHolidays.trim());
                } else {
                    bcv.setViewVisibility(R.id.nonholidays, View.GONE);
                }

                if (Utils.isShownOnWidgets("owghat") && !TextUtils.isEmpty(owghat)) {
                    bcv.setTextViewText(R.id.owghat, owghat);
                } else {
                    bcv.setViewVisibility(R.id.owghat, View.GONE);
                }

                builder = builder
                        .setCustomContentView(cv)
                        .setCustomBigContentView(bcv)
                        .setStyle(new NotificationCompat.DecoratedCustomViewStyle());
            }

            if (BuildConfig.DEBUG) {
                builder = builder.setWhen(Calendar.getInstance().getTimeInMillis());
            }

            if (Utils.goForWorker()) {
                if (notificationManager != null)
                    notificationManager.notify(NOTIFICATION_ID, builder.build());
            } else {
                try {
                    ApplicationService applicationService = ApplicationService.getInstance();
                    if (applicationService != null) {
                        applicationService.startForeground(NOTIFICATION_ID, builder.build());
                    }
                } catch (Exception e) {
                    Log.e("UpdateUtils", "failed to start service with the notification", e);
                }
            }
        } else {
            if (Utils.goForWorker()) {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null)
                    notificationManager.cancel(NOTIFICATION_ID);
            }
        }
    }
}
