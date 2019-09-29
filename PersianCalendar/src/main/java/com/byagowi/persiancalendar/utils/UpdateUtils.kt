package com.byagowi.persiancalendar.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.calendar.AbstractDate
import com.byagowi.persiancalendar.entities.DeviceCalendarEvent
import com.byagowi.persiancalendar.praytimes.Clock
import com.byagowi.persiancalendar.service.ApplicationService
import com.byagowi.persiancalendar.ui.MainActivity
import com.byagowi.persiancalendar.utils.Utils.getClockFromStringId
import com.byagowi.persiancalendar.utils.Utils.getSpacedComma
import java.util.*
import java.util.concurrent.TimeUnit.MINUTES

object UpdateUtils {
    private val NOTIFICATION_ID = 1001
    private var pastDate: AbstractDate? = null
    private var deviceCalendarEvents = SparseArray<List<DeviceCalendarEvent>>()
    @StringRes
    private val timesOn4x2Shia = intArrayOf(R.string.fajr, R.string.dhuhr, R.string.sunset, R.string.maghrib, R.string.midnight)
    @StringRes
    private val timesOn4x2Sunna = intArrayOf(R.string.fajr, R.string.dhuhr, R.string.asr, R.string.maghrib, R.string.isha)
    @IdRes
    private val owghatPlaceHolderId = intArrayOf(R.id.textPlaceholder4owghat_1_4x2, R.id.textPlaceholder4owghat_2_4x2, R.id.textPlaceholder4owghat_3_4x2, R.id.textPlaceholder4owghat_4_4x2, R.id.textPlaceholder4owghat_5_4x2)

    fun setDeviceCalendarEvents(context: Context) {
        try {
            deviceCalendarEvents = Utils.readDayDeviceEvents(context, -1)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun update(context: Context, updateDate: Boolean) {
        var updateDate = updateDate
        Log.d("UpdateUtils", "update")
        Utils.applyAppLanguage(context)
        val calendar = Utils.makeCalendarFromDate(Date())
        val mainCalendar = Utils.getMainCalendar()
        val date = Utils.getTodayOfCalendar(mainCalendar)
        val jdn = date.toJdn()

        val launchAppPendingIntent = PendingIntent.getActivity(context, 0,
                Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_UPDATE_CURRENT)

        //
        // Widgets
        //
        //
        val manager = AppWidgetManager.getInstance(context)
        val colorInt = Utils.getSelectedWidgetTextColor()
        val color = Color.parseColor(colorInt)

        // en-US is our only real LTR language for now
        val isRTL = Utils.isLocaleRTL()

        // Widget 1x1
        val widget1x1 = ComponentName(context, Widget1x1::class.java)
        val widget4x1 = ComponentName(context, Widget4x1::class.java)
        val widget4x2 = ComponentName(context, Widget4x2::class.java)
        val widget2x2 = ComponentName(context, Widget2x2::class.java)

        if (manager.getAppWidgetIds(widget1x1).size != 0) {
            val remoteViews1 = RemoteViews(context.packageName, R.layout.widget1x1)
            remoteViews1.setTextColor(R.id.textPlaceholder1_1x1, color)
            remoteViews1.setTextColor(R.id.textPlaceholder2_1x1, color)
            remoteViews1.setTextViewText(R.id.textPlaceholder1_1x1,
                    Utils.formatNumber(date.dayOfMonth))
            remoteViews1.setTextViewText(R.id.textPlaceholder2_1x1,
                    Utils.getMonthName(date))
            remoteViews1.setOnClickPendingIntent(R.id.widget_layout1x1, launchAppPendingIntent)
            manager.updateAppWidget(widget1x1, remoteViews1)
        }

        if (pastDate == null || pastDate != date || updateDate) {
            Log.d("UpdateUtils", "date has changed")

            Utils.loadAlarms(context)
            pastDate = date
            updateDate = true
            setDeviceCalendarEvents(context)
        }

        val weekDayName = Utils.getWeekDayName(date)
        var title = Utils.dayTitleSummary(date)
        val shiftWorkTitle = Utils.getShiftWorkTitle(jdn, false)
        if (!TextUtils.isEmpty(shiftWorkTitle))
            title += " ($shiftWorkTitle)"
        var subtitle = Utils.dateStringOfOtherCalendars(jdn, getSpacedComma())

        val currentClock = Clock(calendar)
        var owghat = ""
        @StringRes
        val nextOwghatId = Utils.getNextOwghatTimeId(currentClock, updateDate)
        if (nextOwghatId != 0) {
            owghat = context.getString(nextOwghatId) + ": " +
                    Utils.getFormattedClock(Utils.getClockFromStringId(nextOwghatId), false)
            if (Utils.isShownOnWidgets("owghat_location")) {
                val cityName = Utils.getCityName(context, false)
                if (!TextUtils.isEmpty(cityName)) {
                    owghat = "$owghat ($cityName)"
                }
            }
        }
        val events = Utils.getEvents(jdn, deviceCalendarEvents)

        val enableClock = Utils.isWidgetClock() && Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1
        val isCenterAligned = Utils.isCenterAlignWidgets()

        if (manager.getAppWidgetIds(widget4x1).size != 0 || manager.getAppWidgetIds(widget2x2).size != 0) {
            val remoteViews4: RemoteViews
            val remoteViews2: RemoteViews
            if (enableClock) {
                if (!Utils.isIranTime()) {
                    remoteViews4 = RemoteViews(context.packageName, if (isCenterAligned) R.layout.widget4x1_clock_center else R.layout.widget4x1_clock)
                    remoteViews2 = RemoteViews(context.packageName, if (isCenterAligned) R.layout.widget2x2_clock_center else R.layout.widget2x2_clock)
                } else {
                    remoteViews4 = RemoteViews(context.packageName, if (isCenterAligned) R.layout.widget4x1_clock_iran_center else R.layout.widget4x1_clock_iran)
                    remoteViews2 = RemoteViews(context.packageName, if (isCenterAligned) R.layout.widget2x2_clock_iran_center else R.layout.widget2x2_clock_iran)
                }
            } else {
                remoteViews4 = RemoteViews(context.packageName, if (isCenterAligned) R.layout.widget4x1_center else R.layout.widget4x1)
                remoteViews2 = RemoteViews(context.packageName, if (isCenterAligned) R.layout.widget2x2_center else R.layout.widget2x2)
            }

            val mainDateString = Utils.formatDate(date)

            run {
                // Widget 4x1
                remoteViews4.setTextColor(R.id.textPlaceholder1_4x1, color)
                remoteViews4.setTextColor(R.id.textPlaceholder2_4x1, color)
                remoteViews4.setTextColor(R.id.textPlaceholder3_4x1, color)

                var text2: String
                var text3 = ""

                if (enableClock) {
                    text2 = title
                    if (Utils.isIranTime()) {
                        text3 = "(" + context.getString(R.string.iran_time) + ")"
                    }
                } else {
                    remoteViews4.setTextViewText(R.id.textPlaceholder1_4x1, weekDayName)
                    text2 = mainDateString
                }
                if (Utils.isShownOnWidgets("other_calendars")) {
                    text2 += getSpacedComma() + subtitle
                }

                remoteViews4.setTextViewText(R.id.textPlaceholder2_4x1, text2)
                remoteViews4.setTextViewText(R.id.textPlaceholder3_4x1, text3)
                remoteViews4.setOnClickPendingIntent(R.id.widget_layout4x1, launchAppPendingIntent)
                manager.updateAppWidget(widget4x1, remoteViews4)
            }

            run {
                var text2: String
                // Widget 2x2
                remoteViews2.setTextColor(R.id.time_2x2, color)
                remoteViews2.setTextColor(R.id.date_2x2, color)
                remoteViews2.setTextColor(R.id.event_2x2, color)
                remoteViews2.setTextColor(R.id.owghat_2x2, color)

                if (enableClock) {
                    text2 = title
                } else {
                    remoteViews2.setTextViewText(R.id.time_2x2, weekDayName)
                    text2 = mainDateString
                }

                val holidays = Utils.getEventsTitle(events, true, true, true, isRTL)
                if (!TextUtils.isEmpty(holidays)) {
                    remoteViews2.setTextViewText(R.id.holiday_2x2, holidays)
                    if (Utils.isTalkBackEnabled()) {
                        remoteViews2.setContentDescription(R.id.holiday_2x2,
                                context.getString(R.string.holiday_reason) + " " +
                                        holidays)
                    }
                    remoteViews2.setViewVisibility(R.id.holiday_2x2, View.VISIBLE)
                } else {
                    remoteViews2.setViewVisibility(R.id.holiday_2x2, View.GONE)
                }

                val nonHolidays = Utils.getEventsTitle(events, false, true, true, isRTL)
                if (Utils.isShownOnWidgets("non_holiday_events") && !TextUtils.isEmpty(nonHolidays)) {
                    remoteViews2.setTextViewText(R.id.event_2x2, nonHolidays)
                    remoteViews2.setViewVisibility(R.id.event_2x2, View.VISIBLE)
                } else {
                    remoteViews2.setViewVisibility(R.id.event_2x2, View.GONE)
                }

                if (Utils.isShownOnWidgets("owghat") && !TextUtils.isEmpty(owghat)) {
                    remoteViews2.setTextViewText(R.id.owghat_2x2, owghat)
                    remoteViews2.setViewVisibility(R.id.owghat_2x2, View.VISIBLE)
                } else {
                    remoteViews2.setViewVisibility(R.id.owghat_2x2, View.GONE)
                }

                if (Utils.isShownOnWidgets("other_calendars")) {
                    text2 = text2 + "\n" + subtitle + "\n" +
                            AstronomicalUtils.getZodiacInfo(context, jdn, true)
                }
                remoteViews2.setTextViewText(R.id.date_2x2, text2)

                remoteViews2.setOnClickPendingIntent(R.id.widget_layout2x2, launchAppPendingIntent)
                manager.updateAppWidget(widget2x2, remoteViews2)
            }
        }

        //region Widget 4x2
        if (manager.getAppWidgetIds(widget4x2).size != 0) {
            val remoteViews4x2: RemoteViews
            if (enableClock) {
                if (!Utils.isIranTime()) {
                    remoteViews4x2 = RemoteViews(context.packageName, R.layout.widget4x2_clock)
                } else {
                    remoteViews4x2 = RemoteViews(context.packageName, R.layout.widget4x2_clock_iran)
                }
            } else {
                remoteViews4x2 = RemoteViews(context.packageName, R.layout.widget4x2)
            }

            remoteViews4x2.setTextColor(R.id.textPlaceholder0_4x2, color)
            remoteViews4x2.setTextColor(R.id.textPlaceholder1_4x2, color)
            remoteViews4x2.setTextColor(R.id.textPlaceholder2_4x2, color)

            var text2 = Utils.formatDate(date)
            if (enableClock)
                text2 = Utils.getWeekDayName(date) + "\n" + text2
            else
                remoteViews4x2.setTextViewText(R.id.textPlaceholder0_4x2, weekDayName)

            if (Utils.isShownOnWidgets("other_calendars")) {
                text2 = text2 + "\n" + Utils.dateStringOfOtherCalendars(jdn, "\n")
            }

            remoteViews4x2.setTextViewText(R.id.textPlaceholder1_4x2, text2)

            if (nextOwghatId != 0) {
                @StringRes
                val timesOn4x2 = if (Utils.isShiaPrayTimeCalculationSelected()) timesOn4x2Shia else timesOn4x2Sunna
                // Set text of owghats
                for (i in owghatPlaceHolderId.indices) {
                    remoteViews4x2.setTextViewText(owghatPlaceHolderId[i],
                            context.getString(timesOn4x2[i]) + "\n" +
                                    Utils.getFormattedClock(getClockFromStringId(timesOn4x2[i]), false))
                    remoteViews4x2.setTextColor(owghatPlaceHolderId[i],
                            if (timesOn4x2[i] == nextOwghatId)
                                Color.RED
                            else
                                color)
                }

                var difference = Utils.getClockFromStringId(nextOwghatId).toInt() - currentClock.toInt()
                if (difference < 0) difference = 60 * 24 - difference

                val hrs = (MINUTES.toHours(difference.toLong()) % 24).toInt()
                val min = (MINUTES.toMinutes(difference.toLong()) % 60).toInt()

                val remainingTime: String
                if (hrs == 0)
                    remainingTime = String.format(context.getString(R.string.n_minutes), Utils.formatNumber(min))
                else if (min == 0)
                    remainingTime = String.format(context.getString(R.string.n_hours), Utils.formatNumber(hrs))
                else
                    remainingTime = String.format(context.getString(R.string.n_minutes_and_hours), Utils.formatNumber(hrs), Utils.formatNumber(min))

                remoteViews4x2.setTextViewText(R.id.textPlaceholder2_4x2,
                        String.format(context.getString(R.string.n_till),
                                remainingTime, context.getString(nextOwghatId)))
                remoteViews4x2.setTextColor(R.id.textPlaceholder2_4x2, color)
            } else {
                remoteViews4x2.setTextViewText(R.id.textPlaceholder2_4x2, context.getString(R.string.ask_user_to_set_location))
                remoteViews4x2.setTextColor(R.id.textPlaceholder2_4x2, color)
            }

            remoteViews4x2.setOnClickPendingIntent(R.id.widget_layout4x2, launchAppPendingIntent)

            manager.updateAppWidget(widget4x2, remoteViews4x2)
        }
        //endregion


        //
        // Permanent Notification Bar and DashClock Data Extension Update
        //
        //

        // Prepend a right-to-left mark character to Android with sane text rendering stack
        // to resolve a bug seems some Samsung devices have with characters with weak direction,
        // digits being at the first of string on
        if (isRTL && Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN &&
                Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            title = RLM + title
            if (!TextUtils.isEmpty(subtitle)) {
                subtitle = RLM + subtitle
            }
        }

        if (Utils.isNotifyDate()) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val importance = NotificationManager.IMPORTANCE_LOW
                val channel = NotificationChannel(NOTIFICATION_ID.toString(),
                        context.getString(R.string.app_name), importance)
                channel.setShowBadge(false)
                notificationManager.createNotificationChannel(channel)
            }

            // Don't remove this condition checking ever
            if (Utils.isTalkBackEnabled()) {
                // Don't use isToday, per a feedback
                subtitle = Utils.getA11yDaySummary(context, jdn, false,
                        deviceCalendarEvents,
                        true, true, false)
                if (!TextUtils.isEmpty(owghat)) {
                    subtitle += getSpacedComma()
                    subtitle += owghat
                }
            }

            var builder: NotificationCompat.Builder = NotificationCompat.Builder(context, NOTIFICATION_ID.toString())
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setSmallIcon(Utils.getDayIconResource(date.dayOfMonth))
                    .setOngoing(true)
                    .setWhen(0)
                    .setContentIntent(launchAppPendingIntent)
                    .setVisibility(if (Utils.isNotifyDateOnLockScreen())
                        NotificationCompat.VISIBILITY_PUBLIC
                    else
                        NotificationCompat.VISIBILITY_SECRET)
                    .setColor(-0x9f8275)
                    .setColorized(true)
                    .setContentTitle(title)
                    .setContentText(subtitle)

            // Night mode doesn't our custom notification in Samsung, let's detect it
            val isSamsungNightMode = Build.BRAND == "samsung" && Utils.isNightModeEnabled(context)

            if (!Utils.isTalkBackEnabled() && !isSamsungNightMode &&
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N || BuildConfig.DEBUG)) {
                val cv = RemoteViews(context.packageName, if (isRTL)
                    R.layout.custom_notification
                else
                    R.layout.custom_notification_ltr)
                cv.setTextViewText(R.id.title, title)
                cv.setTextViewText(R.id.body, subtitle)

                val bcv = RemoteViews(context.packageName, if (isRTL)
                    R.layout.custom_notification_big
                else
                    R.layout.custom_notification_big_ltr)
                bcv.setTextViewText(R.id.title, title)

                if (!TextUtils.isEmpty(subtitle)) {
                    bcv.setTextViewText(R.id.body, subtitle)
                } else {
                    bcv.setViewVisibility(R.id.body, View.GONE)
                }

                val holidays = Utils.getEventsTitle(events, true, true, true, isRTL)
                if (!TextUtils.isEmpty(holidays)) {
                    bcv.setTextViewText(R.id.holidays, holidays)
                } else {
                    bcv.setViewVisibility(R.id.holidays, View.GONE)
                }
                val nonHolidays = Utils.getEventsTitle(events, false, true, true, isRTL)
                if (Utils.isShownOnWidgets("non_holiday_events") && !TextUtils.isEmpty(nonHolidays)) {
                    bcv.setTextViewText(R.id.nonholidays, nonHolidays.trim { it <= ' ' })
                } else {
                    bcv.setViewVisibility(R.id.nonholidays, View.GONE)
                }

                if (Utils.isShownOnWidgets("owghat") && !TextUtils.isEmpty(owghat)) {
                    bcv.setTextViewText(R.id.owghat, owghat)
                } else {
                    bcv.setViewVisibility(R.id.owghat, View.GONE)
                }

                builder = builder
                        .setCustomContentView(cv)
                        .setCustomBigContentView(bcv)
                        .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            }

            if (BuildConfig.DEBUG) {
                builder = builder.setWhen(Calendar.getInstance().timeInMillis)
            }

            if (Utils.goForWorker()) {
                notificationManager.notify(NOTIFICATION_ID, builder.build())
            } else {
                try {
                    val applicationService = ApplicationService.getInstance()
                    applicationService?.startForeground(NOTIFICATION_ID, builder.build())
                } catch (e: Exception) {
                    Log.e("UpdateUtils", "failed to start service with the notification", e)
                }

            }
        } else {
            if (Utils.goForWorker()) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(NOTIFICATION_ID)
            }
        }
    }
}
