package com.byagowi.persiancalendar.util

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
import android.view.View
import android.widget.RemoteViews

import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.Constants
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.Widget1x1
import com.byagowi.persiancalendar.Widget2x2
import com.byagowi.persiancalendar.Widget4x1
import com.byagowi.persiancalendar.entity.AbstractEvent
import com.byagowi.persiancalendar.service.ApplicationService
import com.byagowi.persiancalendar.view.activity.MainActivity
import com.github.praytimes.Clock
import com.google.android.apps.dashclock.api.ExtensionData

import java.util.Calendar
import java.util.Date

import androidx.core.app.NotificationCompat
import calendar.AbstractDate
import calendar.CalendarType

object UpdateUtils {
  private val NOTIFICATION_ID = 1001
  private var pastDate: AbstractDate? = null
  var extensionData: ExtensionData? = null
    private set

  fun update(context: Context, argUpdateDate: Boolean) {
    var updateDate = argUpdateDate
    Log.d("UpdateUtils", "update")
    val calendar = CalendarUtils.makeCalendarFromDate(Date())
    val mainCalendar = Utils.mainCalendar
    val date = CalendarUtils.getTodayOfCalendar(mainCalendar)
    val jdn = CalendarUtils.getJdnDate(date)

    val intent = Intent(context, MainActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val launchAppPendingIntent = PendingIntent.getActivity(context, 0,
        intent, PendingIntent.FLAG_UPDATE_CURRENT)

    //
    // Widgets
    //
    //
    val manager = AppWidgetManager.getInstance(context)
    val colorInt = Utils.selectedWidgetTextColor
    val color = Color.parseColor(colorInt)

    // en-US is our only real LTR language for now
    val isRTL = Utils.isLocaleRTL

    // Widget 1x1
    val widget1x1 = ComponentName(context, Widget1x1::class.java)
    val widget4x1 = ComponentName(context, Widget4x1::class.java)
    val widget2x2 = ComponentName(context, Widget2x2::class.java)

    if (manager.getAppWidgetIds(widget1x1).size != 0) {
      val remoteViews1 = RemoteViews(context.packageName, R.layout.widget1x1)
      remoteViews1.setTextColor(R.id.textPlaceholder1_1x1, color)
      remoteViews1.setTextColor(R.id.textPlaceholder2_1x1, color)
      remoteViews1.setTextViewText(R.id.textPlaceholder1_1x1,
          Utils.formatNumber(date.dayOfMonth))
      remoteViews1.setTextViewText(R.id.textPlaceholder2_1x1,
          CalendarUtils.getMonthName(date))
      remoteViews1.setOnClickPendingIntent(R.id.widget_layout1x1, launchAppPendingIntent)
      manager.updateAppWidget(widget1x1, remoteViews1)
    }

    if (pastDate == null || pastDate != date || updateDate) {
      Log.d("UpdateUtils", "date has changed")

      Utils.changeAppLanguage(context)
      Utils.loadAlarms(context)
      pastDate = date
      updateDate = true
    }

    val weekDayName = Utils.getWeekDayName(date)
    val status = CalendarUtils.getMonthName(date)
    var title = CalendarUtils.dayTitleSummary(date)
    var subtitle = CalendarUtils.dateStringOfOtherCalendar(mainCalendar, jdn)

    val currentClock = Clock(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
    var owghat = Utils.getNextOwghatTime(context, currentClock, updateDate)
    if (Utils.isShownOnWidgets("owghat_location") && !TextUtils.isEmpty(owghat)) {
      val cityName = Utils.getCityName(context, false)
      if (!TextUtils.isEmpty(cityName)) {
        owghat = "$owghat ($cityName)"
      }
    }
    val events = Utils.getEvents(jdn)

    if (manager.getAppWidgetIds(widget4x1).size != 0 || manager.getAppWidgetIds(widget2x2).size != 0) {
      val remoteViews4: RemoteViews
      val remoteViews2: RemoteViews
      val enableClock = Utils.isWidgetClock && Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1
      if (enableClock) {
        if (!Utils.isIranTime) {
          remoteViews4 = RemoteViews(context.packageName, R.layout.widget4x1_clock)
          remoteViews2 = RemoteViews(context.packageName, R.layout.widget2x2_clock)
        } else {
          remoteViews4 = RemoteViews(context.packageName, R.layout.widget4x1_clock_iran)
          remoteViews2 = RemoteViews(context.packageName, R.layout.widget2x2_clock_iran)
        }
      } else {
        remoteViews4 = RemoteViews(context.packageName, R.layout.widget4x1)
        remoteViews2 = RemoteViews(context.packageName, R.layout.widget2x2)
      }

      val mainDateString = Utils.dateToString(date)

      run {
        // Widget 4x1
        remoteViews4.setTextColor(R.id.textPlaceholder1_4x1, color)
        remoteViews4.setTextColor(R.id.textPlaceholder2_4x1, color)
        remoteViews4.setTextColor(R.id.textPlaceholder3_4x1, color)

        var text2: String
        var text3 = ""

        if (enableClock) {
          text2 = title
          if (Utils.isIranTime) {
            text3 = "(" + context.getString(R.string.iran_time) + ")"
          }
        } else {
          remoteViews4.setTextViewText(R.id.textPlaceholder1_4x1, weekDayName)
          text2 = mainDateString
        }
        if (Utils.isShownOnWidgets("other_calendars")) {
          text2 += Utils.comma + " " + subtitle
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
          text2 = text2 + "\n" + subtitle
        }
        remoteViews2.setTextViewText(R.id.date_2x2, text2)

        remoteViews2.setOnClickPendingIntent(R.id.widget_layout2x2, launchAppPendingIntent)
        manager.updateAppWidget(widget2x2, remoteViews2)
      }
    }

    //
    // Permanent Notification Bar and DashClock Data Extension Update
    //
    //

    // Prepend a right-to-left mark character to Android with sane text rendering stack
    // to resolve a bug seems some Samsung devices have with characters with weak direction,
    // digits being at the first of string on
    if (isRTL && Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN &&
        Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
      title = Constants.RLM + title
      subtitle = Constants.RLM + subtitle
    }

    val icon = Utils.getDayIconResource(date.dayOfMonth)

    extensionData = ExtensionData().visible(true).icon(icon)
        .status(status)
        .expandedTitle(title)
        .expandedBody(subtitle).clickIntent(intent)

    if (Utils.isNotifyDate) {
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(NOTIFICATION_ID.toString(), context.getString(R.string.app_name), importance)
        channel.setShowBadge(false)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager?.createNotificationChannel(channel)
      }

      var builder: NotificationCompat.Builder = NotificationCompat.Builder(context, NOTIFICATION_ID.toString())
          .setPriority(NotificationCompat.PRIORITY_LOW)
          .setSmallIcon(icon)
          .setOngoing(true)
          .setWhen(0)
          .setContentIntent(launchAppPendingIntent)
          .setVisibility(if (Utils.isNotifyDateOnLockScreen)
            NotificationCompat.VISIBILITY_PUBLIC
          else
            NotificationCompat.VISIBILITY_SECRET)
          .setColor(-0x9f8275)
          .setContentTitle(title)
          .setContentText(subtitle)

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N || BuildConfig.DEBUG) {
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
        bcv.setTextViewText(R.id.body, subtitle)

        val holidays = Utils.getEventsTitle(events, true, true, true, isRTL)
        if (!TextUtils.isEmpty(holidays))
          bcv.setTextViewText(R.id.holidays, holidays)
        else
          bcv.setViewVisibility(R.id.holidays, View.GONE)
        val nonHolidays = Utils.getEventsTitle(events, false, true, true, isRTL)
        if (Utils.isShownOnWidgets("non_holiday_events") && !TextUtils.isEmpty(nonHolidays))
          bcv.setTextViewText(R.id.nonholidays, nonHolidays.trim { it <= ' ' })
        else
          bcv.setViewVisibility(R.id.nonholidays, View.GONE)

        if (Utils.isShownOnWidgets("owghat") && !TextUtils.isEmpty(owghat))
          bcv.setTextViewText(R.id.owghat, owghat)
        else
          bcv.setViewVisibility(R.id.owghat, View.GONE)

        builder = builder
            .setCustomContentView(cv)
            .setCustomBigContentView(bcv)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
      }

      if (BuildConfig.DEBUG) {
        builder = builder.setWhen(Calendar.getInstance().timeInMillis)
      }

      //            if (Utils.goForWorker()) {
      //                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
      //                if (notificationManager != null)
      //                    notificationManager.notify(NOTIFICATION_ID, builder.build());
      //            } else {
      try {
        val applicationService = ApplicationService.getInstance()
        applicationService?.startForeground(NOTIFICATION_ID, builder.build())
      } catch (e: Exception) {
        Log.e("UpdateUtils", "failed to start service with the notification", e)
      }

      //            }
    }
    //        else {
    //            if (Utils.goForWorker()) {
    //                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    //                if (notificationManager != null)
    //                    notificationManager.cancel(NOTIFICATION_ID);
    //            }
    //        }
  }

}
