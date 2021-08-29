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
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.IconCompat
import com.byagowi.persiancalendar.AgeWidget
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
import com.byagowi.persiancalendar.DEFAULT_SELECTED_WIDGET_TEXT_COLOR
import com.byagowi.persiancalendar.NON_HOLIDAYS_EVENTS_KEY
import com.byagowi.persiancalendar.OTHER_CALENDARS_KEY
import com.byagowi.persiancalendar.OWGHAT_KEY
import com.byagowi.persiancalendar.OWGHAT_LOCATION_KEY
import com.byagowi.persiancalendar.PREF_SELECTED_DATE_AGE_WIDGET
import com.byagowi.persiancalendar.PREF_SELECTED_WIDGET_BACKGROUND_COLOR
import com.byagowi.persiancalendar.PREF_SELECTED_WIDGET_TEXT_COLOR
import com.byagowi.persiancalendar.PREF_TITLE_AGE_WIDGET
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.RLM
import com.byagowi.persiancalendar.Variants.logDebug
import com.byagowi.persiancalendar.Widget1x1
import com.byagowi.persiancalendar.Widget2x2
import com.byagowi.persiancalendar.Widget4x1
import com.byagowi.persiancalendar.Widget4x2
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.service.ApplicationService
import com.byagowi.persiancalendar.ui.MainActivity
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.praytimes.CalculationMethod
import io.github.persiancalendar.praytimes.Clock
import java.util.*

private const val NOTIFICATION_ID = 1001
private var pastDate: AbstractDate? = null
private var deviceCalendarEvents: DeviceCalendarEventsStore = EventsStore.empty()

// Is called from MainActivity to make sure is updated, probably should be removed however
fun readAndStoreDeviceCalendarEventsOfTheDay(context: Context) = runCatching {
    deviceCalendarEvents = context.readDayDeviceEvents(Jdn.today)
}.onFailure(logException).let {}

private var latestFiredUpdate = 0L

fun update(context: Context, updateDate: Boolean) {
    val now = System.currentTimeMillis()
    if (!updateDate && now - latestFiredUpdate < HALF_SECOND_IN_MILLIS) {
        logDebug("UpdateUtils", "skip update")
        return
    }
    latestFiredUpdate = now

    logDebug("UpdateUtils", "update")
    applyAppLanguage(context)

    val manager = AppWidgetManager.getInstance(context)

    val jdn = Jdn.today
    val date = jdn.toCalendar(mainCalendar)

    val dateHasChanged = if (pastDate == null || pastDate != date || updateDate) {
        logDebug("UpdateUtils", "date has changed")
        scheduleAlarms(context)
        pastDate = date
        readAndStoreDeviceCalendarEventsOfTheDay(context)
        true
    } else false

    val shiftWorkTitle = getShiftWorkTitle(jdn, false)
    val title = dayTitleSummary(jdn, date) +
            if (shiftWorkTitle.isEmpty()) "" else " ($shiftWorkTitle)"
    val widgetTitle = dayTitleSummary(
        jdn, date, calendarNameInLinear = OTHER_CALENDARS_KEY in whatToShowOnWidgets
    ) + if (shiftWorkTitle.isEmpty()) "" else " ($shiftWorkTitle)"
    val subtitle = dateStringOfOtherCalendars(jdn, spacedComma)

    val nowClock = Clock(Date().toJavaCalendar(forceLocalTime = true))

    @StringRes
    val nextOwghatId = getNextOwghatTimeId(nowClock, dateHasChanged)
    val owghat = if (nextOwghatId == 0) "" else buildString {
        append(context.getString(nextOwghatId))
        append(": ")
        append(getOwghatTimeOfStringId(nextOwghatId).toFormattedString())
        if (OWGHAT_LOCATION_KEY in whatToShowOnWidgets) {
            getCityName(context, false).takeIf { it.isNotEmpty() }.also {
                append(" ($it)")
            }
        }
    }

    context.updateAgeWidgets(manager)
    context.update1x1Widget(manager, date)
    context.update4x1Widget(manager, jdn, date, widgetTitle, subtitle)
    context.update2x2Widget(manager, jdn, date, widgetTitle, subtitle, owghat)
    context.update4x2Widget(manager, jdn, date, nextOwghatId, nowClock)
    context.updateNotification(title, subtitle, jdn, date, owghat)
}

private fun Context.updateAgeWidgets(manager: AppWidgetManager) {
    manager.getAppWidgetIds(ComponentName(this, AgeWidget::class.java))?.forEach { widgetId ->
        val baseJdn = appPrefs.getJdnOrNull(PREF_SELECTED_DATE_AGE_WIDGET + widgetId) ?: Jdn.today
        val title = appPrefs.getString(PREF_TITLE_AGE_WIDGET + widgetId, "")
        val subtitle = calculateDaysDifference(resources, baseJdn)
        val textColor = appPrefs.getString(PREF_SELECTED_WIDGET_TEXT_COLOR + widgetId, null)
            ?: DEFAULT_SELECTED_WIDGET_TEXT_COLOR
        val bgColor = appPrefs.getString(PREF_SELECTED_WIDGET_BACKGROUND_COLOR + widgetId, null)
            ?: DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
        val remoteViews = RemoteViews(packageName, R.layout.widget_age)
        remoteViews.setBackgroundColor(R.id.age_widget_root, bgColor)
        remoteViews.setTextViewTextOrHideIfEmpty(R.id.textview_age_widget_title, title ?: "")
        remoteViews.setTextColor(R.id.textview_age_widget_title, Color.parseColor(textColor))
        remoteViews.setTextViewText(R.id.textview_age_widget, subtitle)
        remoteViews.setTextColor(R.id.textview_age_widget, Color.parseColor(textColor))
        manager.updateAppWidget(widgetId, remoteViews)
    }
}

private fun Context.update1x1Widget(manager: AppWidgetManager, date: AbstractDate) {
    val widget1x1 = ComponentName(this, Widget1x1::class.java)
    if (manager.getAppWidgetIds(widget1x1).isNullOrEmpty()) return
    val color = Color.parseColor(selectedWidgetTextColor)
    val remoteViews = RemoteViews(packageName, R.layout.widget1x1)
    remoteViews.setBackgroundColor(R.id.widget_layout1x1)
    remoteViews.setTextColor(R.id.textPlaceholder1_1x1, color)
    remoteViews.setTextColor(R.id.textPlaceholder2_1x1, color)
    remoteViews.setTextViewText(R.id.textPlaceholder1_1x1, formatNumber(date.dayOfMonth))
    remoteViews.setTextViewText(R.id.textPlaceholder2_1x1, date.monthName)
    remoteViews.setOnClickPendingIntent(R.id.widget_layout1x1, launchAppPendingIntent())
    manager.updateAppWidget(widget1x1, remoteViews)
}

private fun Context.update4x1Widget(
    manager: AppWidgetManager, jdn: Jdn, date: AbstractDate, widgetTitle: String, subtitle: String
) {
    val widget4x1 = ComponentName(this, Widget4x1::class.java)
    if (manager.getAppWidgetIds(widget4x1).isNullOrEmpty()) return
    val weekDayName = jdn.dayOfWeekName
    val enableClock =
        isWidgetClock && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
    val showOtherCalendars = OTHER_CALENDARS_KEY in whatToShowOnWidgets
    val mainDateString = formatDate(date, calendarNameInLinear = showOtherCalendars)
    val remoteViews = RemoteViews(
        packageName, if (enableClock) {
            if (isForcedIranTimeEnabled) {
                if (isCenterAlignWidgets) R.layout.widget4x1_clock_iran_center else R.layout.widget4x1_clock_iran
            } else {
                if (isCenterAlignWidgets) R.layout.widget4x1_clock_center else R.layout.widget4x1_clock
            }
        } else {
            if (isCenterAlignWidgets) R.layout.widget4x1_center else R.layout.widget4x1
        }
    )
    val color = Color.parseColor(selectedWidgetTextColor)
    remoteViews.setBackgroundColor(R.id.widget_layout4x1)
    remoteViews.setTextColor(R.id.textPlaceholder1_4x1, color)
    remoteViews.setTextColor(R.id.textPlaceholder2_4x1, color)
    remoteViews.setTextColor(R.id.textPlaceholder3_4x1, color)

    if (!enableClock) remoteViews.setTextViewText(R.id.textPlaceholder1_4x1, weekDayName)
    remoteViews.setTextViewText(R.id.textPlaceholder2_4x1, buildString {
        append(if (enableClock) widgetTitle else mainDateString)
        if (showOtherCalendars) append(spacedComma + subtitle)
    })
    remoteViews.setTextViewText(
        R.id.textPlaceholder3_4x1,
        if (enableClock && isForcedIranTimeEnabled) "(" + getString(R.string.iran_time) + ")" else ""
    )
    remoteViews.setOnClickPendingIntent(R.id.widget_layout4x1, launchAppPendingIntent())
    manager.updateAppWidget(widget4x1, remoteViews)
}

private fun Context.update2x2Widget(
    manager: AppWidgetManager, jdn: Jdn, date: AbstractDate, widgetTitle: String, subtitle: String,
    owghat: String
) {
    val widget2x2 = ComponentName(this, Widget2x2::class.java)
    if (manager.getAppWidgetIds(widget2x2).isNullOrEmpty()) return
    val weekDayName = jdn.dayOfWeekName
    val enableClock =
        isWidgetClock && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
    val showOtherCalendars = OTHER_CALENDARS_KEY in whatToShowOnWidgets
    val mainDateString = formatDate(date, calendarNameInLinear = showOtherCalendars)
    val remoteViews = RemoteViews(
        packageName, if (enableClock) {
            if (isForcedIranTimeEnabled) {
                if (isCenterAlignWidgets) R.layout.widget2x2_clock_iran_center else R.layout.widget2x2_clock_iran
            } else {
                if (isCenterAlignWidgets) R.layout.widget2x2_clock_center else R.layout.widget2x2_clock
            }
        } else {
            if (isCenterAlignWidgets) R.layout.widget2x2_center else R.layout.widget2x2
        }
    )
    val color = Color.parseColor(selectedWidgetTextColor)
    remoteViews.setBackgroundColor(R.id.widget_layout2x2)
    remoteViews.setTextColor(R.id.time_2x2, color)
    remoteViews.setTextColor(R.id.date_2x2, color)
    remoteViews.setTextColor(R.id.event_2x2, color)
    remoteViews.setTextColor(R.id.owghat_2x2, color)

    val events = getEvents(jdn, deviceCalendarEvents)
    val isRtl = resources.isRtl
    val holidays = getEventsTitle(
        events, holiday = true, compact = true, showDeviceCalendarEvents = true,
        insertRLM = isRtl, addIsHoliday = isHighTextContrastEnabled
    )
    if (holidays.isNotEmpty()) {
        remoteViews.setTextViewText(R.id.holiday_2x2, holidays)
        if (isTalkBackEnabled) remoteViews.setContentDescription(
            R.id.holiday_2x2, getString(R.string.holiday_reason) + " " + holidays
        )
        remoteViews.setViewVisibility(R.id.holiday_2x2, View.VISIBLE)
    } else {
        remoteViews.setViewVisibility(R.id.holiday_2x2, View.GONE)
    }

    val nonHolidays = getEventsTitle(
        events, holiday = false, compact = true, showDeviceCalendarEvents = true,
        insertRLM = isRtl, addIsHoliday = false
    )
    if (NON_HOLIDAYS_EVENTS_KEY in whatToShowOnWidgets && nonHolidays.isNotEmpty()) {
        remoteViews.setTextViewText(R.id.event_2x2, nonHolidays)
        remoteViews.setViewVisibility(R.id.event_2x2, View.VISIBLE)
    } else {
        remoteViews.setViewVisibility(R.id.event_2x2, View.GONE)
    }

    if (OWGHAT_KEY in whatToShowOnWidgets && owghat.isNotEmpty()) {
        remoteViews.setTextViewText(R.id.owghat_2x2, owghat)
        remoteViews.setViewVisibility(R.id.owghat_2x2, View.VISIBLE)
    } else {
        remoteViews.setViewVisibility(R.id.owghat_2x2, View.GONE)
    }

    if (!enableClock) remoteViews.setTextViewText(R.id.time_2x2, weekDayName)
    remoteViews.setTextViewText(R.id.date_2x2, buildString {
        append(if (enableClock) widgetTitle else mainDateString)
        if (showOtherCalendars) appendLine().append(subtitle)
    })

    remoteViews.setOnClickPendingIntent(R.id.widget_layout2x2, launchAppPendingIntent())
    manager.updateAppWidget(widget2x2, remoteViews)
}

private fun Context.update4x2Widget(
    manager: AppWidgetManager, jdn: Jdn, date: AbstractDate, nextOwghatId: Int, nowClock: Clock
) {
    val widget4x2 = ComponentName(this, Widget4x2::class.java)
    if (manager.getAppWidgetIds(widget4x2).isNullOrEmpty()) return

    val weekDayName = jdn.dayOfWeekName
    val showOtherCalendars = OTHER_CALENDARS_KEY in whatToShowOnWidgets
    val enableClock = isWidgetClock && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
    val remoteViews = RemoteViews(
        packageName, if (enableClock) {
            if (isForcedIranTimeEnabled) R.layout.widget4x2_clock_iran else R.layout.widget4x2_clock
        } else R.layout.widget4x2
    )

    remoteViews.setBackgroundColor(R.id.widget_layout4x2)

    val color = Color.parseColor(selectedWidgetTextColor)
    remoteViews.setTextColor(R.id.textPlaceholder0_4x2, color)
    remoteViews.setTextColor(R.id.textPlaceholder1_4x2, color)
    remoteViews.setTextColor(R.id.textPlaceholder2_4x2, color)
    remoteViews.setTextColor(R.id.textPlaceholder4owghat_3_4x2, color)
    remoteViews.setTextColor(R.id.textPlaceholder4owghat_1_4x2, color)
    remoteViews.setTextColor(R.id.textPlaceholder4owghat_4_4x2, color)
    remoteViews.setTextColor(R.id.textPlaceholder4owghat_2_4x2, color)
    remoteViews.setTextColor(R.id.textPlaceholder4owghat_5_4x2, color)

    if (!enableClock) remoteViews.setTextViewText(R.id.textPlaceholder0_4x2, weekDayName)
    remoteViews.setTextViewText(R.id.textPlaceholder1_4x2, buildString {
        if (enableClock) append(jdn.dayOfWeekName + "\n")
        append(formatDate(date, calendarNameInLinear = showOtherCalendars))
        if (showOtherCalendars) appendLine().append(dateStringOfOtherCalendars(jdn, "\n"))
    })

    if (nextOwghatId != 0) {
        // Set text of owghats
        listOf(
            R.id.textPlaceholder4owghat_1_4x2, R.id.textPlaceholder4owghat_2_4x2,
            R.id.textPlaceholder4owghat_3_4x2, R.id.textPlaceholder4owghat_4_4x2,
            R.id.textPlaceholder4owghat_5_4x2
        ).zip(
            when (calculationMethod) {
                CalculationMethod.Tehran, CalculationMethod.Jafari -> listOf(
                    R.string.fajr, R.string.sunrise,
                    R.string.dhuhr, R.string.maghrib,
                    R.string.midnight
                )
                else -> listOf(
                    R.string.fajr, R.string.dhuhr,
                    R.string.asr, R.string.maghrib,
                    R.string.isha
                )
            }
        ) { textHolderViewId, owghatStringId ->
            remoteViews.setTextViewText(
                textHolderViewId, getString(owghatStringId) + "\n" +
                        getOwghatTimeOfStringId(owghatStringId).toFormattedString()
            )
            remoteViews.setTextColor(
                textHolderViewId, if (owghatStringId == nextOwghatId) Color.RED else color
            )
        }

        val remaining = Clock.fromInt(
            (getOwghatTimeOfStringId(nextOwghatId).toInt() - nowClock.toInt())
                .let { if (it > 0) it else it + 60 * 24 })
        remoteViews.setTextViewText(
            R.id.textPlaceholder2_4x2,
            getString(R.string.n_till, remaining.asRemainingTime(this), getString(nextOwghatId))
        )
        remoteViews.setTextColor(R.id.textPlaceholder2_4x2, color)
    } else {
        remoteViews.setTextViewText(
            R.id.textPlaceholder2_4x2, getString(R.string.ask_user_to_set_location)
        )
        remoteViews.setTextColor(R.id.textPlaceholder2_4x2, color)
    }

    remoteViews.setOnClickPendingIntent(R.id.widget_layout4x2, launchAppPendingIntent())
    manager.updateAppWidget(widget4x2, remoteViews)
}

private fun Context.updateNotification(
    title: String, subtitle: String, jdn: Jdn, date: AbstractDate, owghat: String
) {
    if (!isNotifyDate) {
        if (enableWorkManager) getSystemService<NotificationManager>()?.cancel(NOTIFICATION_ID)
        return
    }

    val notificationManager = getSystemService<NotificationManager>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(
            NOTIFICATION_ID.toString(),
            getString(R.string.app_name), importance
        )
        channel.setShowBadge(false)
        notificationManager?.createNotificationChannel(channel)
    }

    // Prepend a right-to-left mark character to Android with sane text rendering stack
    // to resolve a bug seems some Samsung devices have with characters with weak direction,
    // digits being at the first of string on
    val toPrepend =
        if (resources.isRtl && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) RLM else ""

    val builder = NotificationCompat.Builder(this, NOTIFICATION_ID.toString())
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
        .setWhen(0)
        .setContentIntent(launchAppPendingIntent())
        .setVisibility(
            if (isNotifyDateOnLockScreen)
                NotificationCompat.VISIBILITY_PUBLIC
            else
                NotificationCompat.VISIBILITY_SECRET
        )
        .setColor(0xFF607D8B.toInt())
        .setColorized(true)
        .setContentTitle(toPrepend + title)
        .setContentText(
            when {
                isTalkBackEnabled -> getA11yDaySummary(
                    context = this, jdn = jdn,
                    isToday = false, // Don't set isToday, per a feedback
                    deviceCalendarEvents = deviceCalendarEvents, withZodiac = true,
                    withOtherCalendars = true, withTitle = false
                ) + if (owghat.isEmpty()) "" else spacedComma + owghat
                subtitle.isEmpty() -> subtitle
                else -> toPrepend + subtitle
            }
        )

    // Dynamic small icon generator, disabled as it needs API 23 and we need to have the other path anyway
    if ((false)) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val icon = IconCompat.createWithBitmap(createStatusIcon(this, date.dayOfMonth))
            builder.setSmallIcon(icon)
        }
    } else {
        builder.setSmallIcon(getDayIconResource(date.dayOfMonth))
    }

    // Night mode doesn't like our custom notification in Samsung and HTC One UI
    val shouldDisableCustomNotification = when (Build.BRAND) {
        "samsung", "htc" -> isNightModeEnabled(this)
        else -> false
    }

    if (!isTalkBackEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val events = getEvents(jdn, deviceCalendarEvents)
        val holidays = getEventsTitle(
            events, holiday = true,
            compact = true, showDeviceCalendarEvents = true, insertRLM = resources.isRtl,
            addIsHoliday = shouldDisableCustomNotification || isHighTextContrastEnabled
        )

        val nonHolidays = if (NON_HOLIDAYS_EVENTS_KEY in whatToShowOnWidgets) getEventsTitle(
            events, holiday = false,
            compact = true, showDeviceCalendarEvents = true, insertRLM = resources.isRtl,
            addIsHoliday = false
        ) else ""

        val notificationOwghat = if (OWGHAT_KEY in whatToShowOnWidgets) owghat else ""

        if (shouldDisableCustomNotification) {
            val content = listOf(subtitle, holidays.trim(), nonHolidays, notificationOwghat)
                .filter { it.isNotBlank() }.joinToString("\n")
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(content))
        } else {
            builder.setCustomContentView(RemoteViews(
                packageName,
                if (resources.isRtl) R.layout.custom_notification else R.layout.custom_notification_ltr
            ).also {
                it.setTextViewText(R.id.title, title)
                it.setTextViewText(R.id.body, subtitle)
            })

            if (listOf(holidays, nonHolidays, notificationOwghat).any { it.isNotBlank() })
                builder.setCustomBigContentView(RemoteViews(
                    packageName,
                    if (resources.isRtl) R.layout.custom_notification_big else R.layout.custom_notification_big_ltr
                ).also {
                    it.setTextViewText(R.id.title, title)
                    it.setTextViewTextOrHideIfEmpty(R.id.body, subtitle)
                    it.setTextViewTextOrHideIfEmpty(R.id.holidays, holidays)
                    it.setTextViewTextOrHideIfEmpty(R.id.nonholidays, nonHolidays)
                    it.setTextViewTextOrHideIfEmpty(R.id.owghat, notificationOwghat)
                })

            builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
        }
    }

    if (BuildConfig.DEBUG) builder.setWhen(System.currentTimeMillis())

    if (enableWorkManager) notificationManager?.notify(NOTIFICATION_ID, builder.build())
    else runCatching {
        ApplicationService.getInstance()?.startForeground(NOTIFICATION_ID, builder.build())
    }.onFailure(logException)
}

private fun RemoteViews.setBackgroundColor(
    @IdRes layoutId: Int, color: String = selectedWidgetBackgroundColor
) = setInt(layoutId, "setBackgroundColor", Color.parseColor(color))

private fun RemoteViews.setTextViewTextOrHideIfEmpty(viewId: Int, text: CharSequence) =
    if (text.isBlank()) setViewVisibility(viewId, View.GONE)
    else setTextViewText(viewId, text.trim())

private fun IntArray?.isNullOrEmpty() = this?.isEmpty() ?: true

private fun Context.launchAppPendingIntent(): PendingIntent? = PendingIntent.getActivity(
    this, 0,
    Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
)
