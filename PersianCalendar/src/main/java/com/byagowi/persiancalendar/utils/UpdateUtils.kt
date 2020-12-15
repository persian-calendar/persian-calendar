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
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.service.ApplicationService
import com.byagowi.persiancalendar.ui.MainActivity
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.praytimes.CalculationMethod
import io.github.persiancalendar.praytimes.Clock
import java.util.*
import java.util.concurrent.TimeUnit.MINUTES

private const val NOTIFICATION_ID = 1001
private var pastDate: AbstractDate? = null
private var deviceCalendarEvents: DeviceCalendarEventsStore = emptyEventsStore()

fun setDeviceCalendarEvents(context: Context) = try {
    deviceCalendarEvents = readDayDeviceEvents(context, -1)
} catch (e: Exception) {
    e.printStackTrace()
}

var latestFiredUpdate = 0L

fun update(context: Context, updateDate: Boolean) {
    val now = System.currentTimeMillis()
    if (!updateDate && now - latestFiredUpdate < HALF_SECOND_IN_MILLIS) {
        Log.d("UpdateUtils", "skip update")
        return
    }
    latestFiredUpdate = now

    Log.d("UpdateUtils", "update")
    applyAppLanguage(context)
    val calendar = makeCalendarFromDate(Date())
    val date = getTodayOfCalendar(mainCalendar)
    val jdn = date.toJdn()

    val launchAppPendingIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT
    )

    //
    // Widgets
    //
    //
    val manager = AppWidgetManager.getInstance(context) ?: return
    val color = Color.parseColor(selectedWidgetTextColor)

    // en-US is our only real LTR language for now
    val isRTL = isLocaleRTL()

    // Widget 1x1
    val widget1x1 = ComponentName(context, Widget1x1::class.java)
    val widget4x1 = ComponentName(context, Widget4x1::class.java)
    val widget4x2 = ComponentName(context, Widget4x2::class.java)
    val widget2x2 = ComponentName(context, Widget2x2::class.java)

    fun RemoteViews.setBackgroundColor(@IdRes layoutId: Int): Unit =
            this.setInt(
                    layoutId, "setBackgroundColor",
                    Color.parseColor(selectedWidgetBackgroundColor)
            )

    //region Widget 1x1
    if (manager.getAppWidgetIds(widget1x1)?.isNotEmpty() == true) {
        RemoteViews(context.packageName, R.layout.widget1x1).apply {
            setBackgroundColor(R.id.widget_layout1x1)
            setTextColor(R.id.textPlaceholder1_1x1, color)
            setTextColor(R.id.textPlaceholder2_1x1, color)
            setTextViewText(
                    R.id.textPlaceholder1_1x1,
                    formatNumber(date.dayOfMonth)
            )
            setTextViewText(
                    R.id.textPlaceholder2_1x1,
                    getMonthName(date)
            )
            setOnClickPendingIntent(R.id.widget_layout1x1, launchAppPendingIntent)
            manager.updateAppWidget(widget1x1, this)
        }
    }
    //endregion

    var dateHasChanged = false
    if (pastDate == null || pastDate != date || updateDate) {
        Log.d("UpdateUtils", "date has changed")

        loadAlarms(context)
        pastDate = date
        dateHasChanged = true
        setDeviceCalendarEvents(context)
    }

    val showOtherCalendars = "other_calendars" in whatToShowOnWidgets

    val weekDayName = getWeekDayName(date)
    var title = dayTitleSummary(date)
    var widgetTitle = dayTitleSummary(date, calendarNameInLinear = showOtherCalendars)
    val shiftWorkTitle = getShiftWorkTitle(jdn, false)
    if (shiftWorkTitle.isNotEmpty()) {
        title += " ($shiftWorkTitle)"
        widgetTitle += " ($shiftWorkTitle)"
    }
    var subtitle = dateStringOfOtherCalendars(jdn, spacedComma)

    val currentClock = Clock(calendar)
    var owghat = ""

    @StringRes
    val nextOwghatId = getNextOwghatTimeId(currentClock, dateHasChanged)
    if (nextOwghatId != 0) {
        owghat = context.getString(nextOwghatId) + ": " +
                getClockFromStringId(nextOwghatId).toFormattedString()
        if ("owghat_location" in whatToShowOnWidgets) {
            val cityName = getCityName(context, false)
            if (cityName.isNotEmpty()) {
                owghat = "$owghat ($cityName)"
            }
        }
    }
    val events = getEvents(jdn, deviceCalendarEvents)

    val enableClock = isWidgetClock && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
    val isCenterAligned = isCenterAlignWidgets

    //region Widget 4x1 and 2x2
    if (manager.getAppWidgetIds(widget4x1)?.isNotEmpty() == true || manager.getAppWidgetIds(
                    widget2x2
            )?.isNotEmpty() == true
    ) {
        val remoteViews4: RemoteViews
        val remoteViews2: RemoteViews
        if (enableClock) {
            if (isForcedIranTimeEnabled) {
                remoteViews4 = RemoteViews(
                        context.packageName,
                        if (isCenterAligned) R.layout.widget4x1_clock_iran_center else R.layout.widget4x1_clock_iran
                )
                remoteViews2 = RemoteViews(
                        context.packageName,
                        if (isCenterAligned) R.layout.widget2x2_clock_iran_center else R.layout.widget2x2_clock_iran
                )
            } else {
                remoteViews4 = RemoteViews(
                        context.packageName,
                        if (isCenterAligned) R.layout.widget4x1_clock_center else R.layout.widget4x1_clock
                )
                remoteViews2 = RemoteViews(
                        context.packageName,
                        if (isCenterAligned) R.layout.widget2x2_clock_center else R.layout.widget2x2_clock
                )
            }
        } else {
            remoteViews4 = RemoteViews(
                    context.packageName,
                    if (isCenterAligned) R.layout.widget4x1_center else R.layout.widget4x1
            )
            remoteViews2 = RemoteViews(
                    context.packageName,
                    if (isCenterAligned) R.layout.widget2x2_center else R.layout.widget2x2
            )
        }

        val mainDateString = formatDate(date, calendarNameInLinear = showOtherCalendars)

        remoteViews4.run {
            // Widget 4x1
            setBackgroundColor(R.id.widget_layout4x1)
            setTextColor(R.id.textPlaceholder1_4x1, color)
            setTextColor(R.id.textPlaceholder2_4x1, color)
            setTextColor(R.id.textPlaceholder3_4x1, color)

            var text2: String
            var text3 = ""

            if (enableClock) {
                text2 = widgetTitle
                if (isForcedIranTimeEnabled) text3 =
                        "(" + context.getString(R.string.iran_time) + ")"
            } else {
                remoteViews4.setTextViewText(R.id.textPlaceholder1_4x1, weekDayName)
                text2 = mainDateString
            }
            if (showOtherCalendars) {
                text2 += spacedComma + subtitle
            }

            setTextViewText(R.id.textPlaceholder2_4x1, text2)
            setTextViewText(R.id.textPlaceholder3_4x1, text3)
            setOnClickPendingIntent(R.id.widget_layout4x1, launchAppPendingIntent)
            manager.updateAppWidget(widget4x1, this)
        }

        remoteViews2.run {
            var text2: String
            // Widget 2x2
            setBackgroundColor(R.id.widget_layout2x2)
            setTextColor(R.id.time_2x2, color)
            setTextColor(R.id.date_2x2, color)
            setTextColor(R.id.event_2x2, color)
            setTextColor(R.id.owghat_2x2, color)

            text2 = if (enableClock) {
                widgetTitle
            } else {
                setTextViewText(R.id.time_2x2, weekDayName)
                mainDateString
            }

            val holidays = getEventsTitle(
                    events,
                    holiday = true,
                    compact = true,
                    showDeviceCalendarEvents = true,
                    insertRLM = isRTL,
                    addIsHoliday = isHighTextContrastEnabled
            )
            if (holidays.isNotEmpty()) {
                setTextViewText(R.id.holiday_2x2, holidays)
                if (isTalkBackEnabled) setContentDescription(
                        R.id.holiday_2x2,
                        context.getString(R.string.holiday_reason) + " " + holidays
                )
                setViewVisibility(R.id.holiday_2x2, View.VISIBLE)
            } else {
                setViewVisibility(R.id.holiday_2x2, View.GONE)
            }

            val nonHolidays = getEventsTitle(
                    events,
                    holiday = false,
                    compact = true,
                    showDeviceCalendarEvents = true,
                    insertRLM = isRTL,
                    addIsHoliday = false
            )
            if ("non_holiday_events" in whatToShowOnWidgets && nonHolidays.isNotEmpty()) {
                setTextViewText(R.id.event_2x2, nonHolidays)
                setViewVisibility(R.id.event_2x2, View.VISIBLE)
            } else {
                setViewVisibility(R.id.event_2x2, View.GONE)
            }

            if ("owghat" in whatToShowOnWidgets && owghat.isNotEmpty()) {
                setTextViewText(R.id.owghat_2x2, owghat)
                setViewVisibility(R.id.owghat_2x2, View.VISIBLE)
            } else {
                setViewVisibility(R.id.owghat_2x2, View.GONE)
            }

            if (showOtherCalendars) {
                text2 = text2 + "\n" + subtitle + "\n" + getZodiacInfo(context, jdn, true)
            }
            setTextViewText(R.id.date_2x2, text2)

            setOnClickPendingIntent(R.id.widget_layout2x2, launchAppPendingIntent)
            manager.updateAppWidget(widget2x2, this)
        }
    }
    //endregion

    //region Widget 4x2
    if (manager.getAppWidgetIds(widget4x2)?.isNotEmpty() == true) {
        val remoteViews4x2 = RemoteViews(
                context.packageName,
                if (enableClock) {
                    if (isForcedIranTimeEnabled) R.layout.widget4x2_clock_iran else R.layout.widget4x2_clock
                } else R.layout.widget4x2
        )

        remoteViews4x2.run {
            setBackgroundColor(R.id.widget_layout4x2)

            setTextColor(R.id.textPlaceholder0_4x2, color)
            setTextColor(R.id.textPlaceholder1_4x2, color)
            setTextColor(R.id.textPlaceholder2_4x2, color)
            setTextColor(R.id.textPlaceholder4owghat_3_4x2, color)
            setTextColor(R.id.textPlaceholder4owghat_1_4x2, color)
            setTextColor(R.id.textPlaceholder4owghat_4_4x2, color)
            setTextColor(R.id.textPlaceholder4owghat_2_4x2, color)
            setTextColor(R.id.textPlaceholder4owghat_5_4x2, color)

            var text2 = formatDate(date, calendarNameInLinear = showOtherCalendars)
            if (enableClock)
                text2 = getWeekDayName(date) + "\n" + text2
            else
                setTextViewText(R.id.textPlaceholder0_4x2, weekDayName)

            if (showOtherCalendars)
                text2 = text2 + "\n" + dateStringOfOtherCalendars(jdn, "\n")

            setTextViewText(R.id.textPlaceholder1_4x2, text2)

            if (nextOwghatId != 0) {
                // Set text of owghats
                listOf(
                        R.id.textPlaceholder4owghat_1_4x2, R.id.textPlaceholder4owghat_2_4x2,
                        R.id.textPlaceholder4owghat_3_4x2, R.id.textPlaceholder4owghat_4_4x2,
                        R.id.textPlaceholder4owghat_5_4x2
                ).zip(
                        when (calculationMethod) {
                            CalculationMethod.Tehran, CalculationMethod.Jafari -> listOf(
                                    R.string.fajr, R.string.dhuhr,
                                    R.string.sunset, R.string.maghrib,
                                    R.string.midnight
                            )
                            else -> listOf(
                                    R.string.fajr, R.string.dhuhr,
                                    R.string.asr, R.string.maghrib,
                                    R.string.isha
                            )
                        }
                ) { textHolderViewId, owghatStringId ->
                    setTextViewText(
                            textHolderViewId,
                            context.getString(owghatStringId) + "\n" +
                                    getClockFromStringId(owghatStringId).toFormattedString()
                    )
                    setTextColor(
                            textHolderViewId,
                            if (owghatStringId == nextOwghatId)
                                Color.RED
                            else
                                color
                    )
                }

                var difference = getClockFromStringId(nextOwghatId).toInt() - currentClock.toInt()
                if (difference < 0) difference += 60 * 24

                val hrs = (MINUTES.toHours(difference.toLong()) % 24).toInt()
                val min = (MINUTES.toMinutes(difference.toLong()) % 60).toInt()

                val remainingTime = when {
                    hrs == 0 -> context.getString(R.string.n_minutes).format(formatNumber(min))
                    min == 0 -> context.getString(R.string.n_hours).format(formatNumber(hrs))
                    else -> context.getString(R.string.n_minutes_and_hours)
                            .format(formatNumber(hrs), formatNumber(min))
                }

                setTextViewText(
                        R.id.textPlaceholder2_4x2,
                        context.getString(R.string.n_till)
                                .format(remainingTime, context.getString(nextOwghatId))
                )
                setTextColor(R.id.textPlaceholder2_4x2, color)
            } else {
                setTextViewText(
                        R.id.textPlaceholder2_4x2,
                        context.getString(R.string.ask_user_to_set_location)
                )
                setTextColor(R.id.textPlaceholder2_4x2, color)
            }

            setOnClickPendingIntent(R.id.widget_layout4x2, launchAppPendingIntent)

            manager.updateAppWidget(widget4x2, this)
        }
    }
    //endregion


    //
    // Permanent Notification Bar and DashClock Data Extension Update
    //
    //

    // Prepend a right-to-left mark character to Android with sane text rendering stack
    // to resolve a bug seems some Samsung devices have with characters with weak direction,
    // digits being at the first of string on
    if (isRTL && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
        title = RLM + title
        if (subtitle.isNotEmpty()) {
            subtitle = RLM + subtitle
        }
    }

    if (isNotifyDate) {
        val notificationManager = context.getSystemService<NotificationManager>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(
                    NOTIFICATION_ID.toString(),
                    context.getString(R.string.app_name), importance
            )
            channel.setShowBadge(false)
            notificationManager?.createNotificationChannel(channel)
        }

        // Don't remove this condition checking ever
        if (isTalkBackEnabled) {
            // Don't use isToday, per a feedback
            subtitle = getA11yDaySummary(
                    context, jdn, false,
                    deviceCalendarEvents,
                    withZodiac = true, withOtherCalendars = true, withTitle = false
            )
            if (owghat.isNotEmpty()) {
                subtitle += spacedComma
                subtitle += owghat
            }
        }

        val builder = NotificationCompat.Builder(context, NOTIFICATION_ID.toString())
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSmallIcon(getDayIconResource(date.dayOfMonth))
                .setOngoing(true)
                .setWhen(0)
                .setContentIntent(launchAppPendingIntent)
                .setVisibility(
                        if (isNotifyDateOnLockScreen)
                            NotificationCompat.VISIBILITY_PUBLIC
                        else
                            NotificationCompat.VISIBILITY_SECRET
                )
                .setColor(-0x9f8275)
                .setColorized(true)
                .setContentTitle(title)
                .setContentText(subtitle)

        // Night mode doesn't like our custom notification in Samsung and HTC One UI
        val shouldDisableCustomNotification =
                (Build.BRAND in listOf("samsung", "htc")) && isNightModeEnabled(context)

        if (!isTalkBackEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val holidays = getEventsTitle(
                    events,
                    holiday = true,
                    compact = true,
                    showDeviceCalendarEvents = true,
                    insertRLM = isRTL,
                    addIsHoliday = shouldDisableCustomNotification || isHighTextContrastEnabled
            )

            val nonHolidays = getEventsTitle(
                    dayEvents = events,
                    holiday = false,
                    compact = true,
                    showDeviceCalendarEvents = true,
                    insertRLM = isRTL,
                    addIsHoliday = false
            )

            if (shouldDisableCustomNotification) {
                builder.setStyle(
                        NotificationCompat.BigTextStyle().bigText(
                                listOf(
                                        subtitle,
                                        holidays.trim(),
                                        if ("non_holiday_events" in whatToShowOnWidgets) nonHolidays else "",
                                        if ("owghat" in whatToShowOnWidgets) owghat else ""
                                ).filter { it.isNotBlank() }.joinToString("\n")
                        )
                )
            } else {
                val cv = RemoteViews(
                        context.packageName,
                        if (isRTL) R.layout.custom_notification else R.layout.custom_notification_ltr
                ).apply {
                    setTextViewText(R.id.title, title)
                    setTextViewText(R.id.body, subtitle)
                }

                val bcv = RemoteViews(
                        context.packageName,
                        if (isRTL) R.layout.custom_notification_big else R.layout.custom_notification_big_ltr
                ).apply {
                    setTextViewText(R.id.title, title)

                    fun RemoteViews.setTextViewTextOrIfEmpty(viewId: Int, text: CharSequence) =
                            if (text.trim().isEmpty()) setViewVisibility(viewId, View.GONE)
                            else setTextViewText(viewId, text.trim())

                    setTextViewTextOrIfEmpty(R.id.body, subtitle)
                    setTextViewTextOrIfEmpty(R.id.holidays, holidays)
                    setTextViewTextOrIfEmpty(
                            R.id.nonholidays,
                            if ("non_holiday_events" in whatToShowOnWidgets) nonHolidays else ""
                    )
                    setTextViewTextOrIfEmpty(
                            R.id.owghat,
                            if ("owghat" in whatToShowOnWidgets) owghat else ""
                    )
                }

                builder
                        .setCustomContentView(cv)
                        .setCustomBigContentView(bcv)
                        .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            }
        }

        if (BuildConfig.DEBUG) builder.setWhen(Calendar.getInstance().timeInMillis)

        if (goForWorker())
            notificationManager?.notify(NOTIFICATION_ID, builder.build())
        else
            try {
                ApplicationService.getInstance()?.startForeground(NOTIFICATION_ID, builder.build())
            } catch (e: Exception) {
                Log.e("UpdateUtils", "failed to start service with the notification", e)
            }
    } else if (goForWorker()) {
        context.getSystemService<NotificationManager>()?.cancel(NOTIFICATION_ID)
    }
}
