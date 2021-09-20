package com.byagowi.persiancalendar.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.drawToBitmap
import com.byagowi.persiancalendar.AgeWidget
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
import com.byagowi.persiancalendar.Variants
import com.byagowi.persiancalendar.Variants.debugLog
import com.byagowi.persiancalendar.Widget1x1
import com.byagowi.persiancalendar.Widget2x2
import com.byagowi.persiancalendar.Widget4x1
import com.byagowi.persiancalendar.Widget4x2
import com.byagowi.persiancalendar.WidgetMonthView
import com.byagowi.persiancalendar.WidgetSunView
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.service.ApplicationService
import com.byagowi.persiancalendar.ui.MainActivity
import com.byagowi.persiancalendar.ui.calendar.calendarpager.MonthView
import com.byagowi.persiancalendar.ui.calendar.times.SunView
import com.byagowi.persiancalendar.ui.utils.dp
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.praytimes.CalculationMethod
import io.github.persiancalendar.praytimes.Clock
import io.github.persiancalendar.praytimes.PrayTimes
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
        debugLog("UpdateUtils: skip update")
        return
    }
    latestFiredUpdate = now

    debugLog("UpdateUtils: update")
    applyAppLanguage(context)

    val jdn = Jdn.today
    val date = jdn.toCalendar(mainCalendar)

    if (pastDate == null || pastDate != date || updateDate) {
        debugLog("UpdateUtils: date has changed")
        scheduleAlarms(context)
        pastDate = date
        readAndStoreDeviceCalendarEventsOfTheDay(context)
    }

    val shiftWorkTitle = getShiftWorkTitle(jdn, false)
    val title = dayTitleSummary(jdn, date) +
            if (shiftWorkTitle.isEmpty()) "" else " ($shiftWorkTitle)"
    val widgetTitle = dayTitleSummary(
        jdn, date, calendarNameInLinear = OTHER_CALENDARS_KEY in whatToShowOnWidgets
    ) + if (shiftWorkTitle.isEmpty()) "" else " ($shiftWorkTitle)"
    val subtitle = dateStringOfOtherCalendars(jdn, spacedComma)

    // region owghat calculations
    val nowClock = Clock(Date().toJavaCalendar(forceLocalTime = true))
    val prayTimes = coordinates?.calculatePrayTimes()

    @StringRes
    val nextOwghatId = prayTimes?.getNextOwghatTimeId(nowClock)
    val owghat = if (nextOwghatId == null) "" else buildString {
        append(context.getString(nextOwghatId))
        append(": ")
        append(prayTimes.getFromStringId(nextOwghatId)?.toFormattedString() ?: "")
        if (OWGHAT_LOCATION_KEY in whatToShowOnWidgets)
            context.appPrefs.cityName?.also { append(" ($it)") }
    }
    // endregion

    // Widgets
    AppWidgetManager.getInstance(context).run {
        updateFromRemoteViews<AgeWidget>(context) { width, height, widgetId ->
            createAgeRemoteViews(context, width, height, widgetId)
        }
        updateFromRemoteViews<Widget1x1>(context) { width, height, _ ->
            create1x1RemoteViews(context, width, height, date)
        }
        updateFromRemoteViews<Widget4x1>(context) { width, height, _ ->
            create4x1RemoteViews(context, width, height, jdn, date, widgetTitle, subtitle)
        }
        updateFromRemoteViews<Widget2x2>(context) { width, height, _ ->
            create2x2RemoteViews(context, width, height, jdn, date, widgetTitle, subtitle, owghat)
        }
        updateFromRemoteViews<Widget4x2>(context) { width, height, _ ->
            create4x2RemoteViews(context, width, height, jdn, date, nowClock, prayTimes)
        }
        updateFromRemoteViews<WidgetSunView>(context) { width, height, _ ->
            createSunViewRemoteViews(context, width, height, jdn, prayTimes)
        }
        updateFromRemoteViews<WidgetMonthView>(context) { width, height, _ ->
            createMonthViewRemoteViews(context, width, height, date)
        }
    }

    // Notification
    updateNotification(context, title, subtitle, jdn, date, owghat)
}

@StringRes
private fun PrayTimes.getNextOwghatTimeId(current: Clock): Int {
    val clock = current.toInt()
    return when {
        fajrClock.toInt() > clock -> R.string.fajr
        sunriseClock.toInt() > clock -> R.string.sunrise
        dhuhrClock.toInt() > clock -> R.string.dhuhr
        asrClock.toInt() > clock -> R.string.asr
        sunsetClock.toInt() > clock -> R.string.sunset
        maghribClock.toInt() > clock -> R.string.maghrib
        ishaClock.toInt() > clock -> R.string.isha
        midnightClock.toInt() > clock -> R.string.midnight
        // TODO: this is today's, not tomorrow
        else -> R.string.fajr
    }
}

fun AppWidgetManager.getWidgetSize(context: Context, widgetId: Int): Pair<Int, Int> {
    // https://stackoverflow.com/a/69080699
    val isPortrait = context.resources.configuration.orientation == ORIENTATION_PORTRAIT
    val (width, height) = listOf(
        if (isPortrait) AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH
        else AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,
        if (isPortrait) AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT
        else AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT
    ).map { getAppWidgetOptions(widgetId).getInt(it, 0).dp.toInt() }
    // Crashes terribly if is below zero, let's make sure that won't happen till we understand it better
    return width.coerceAtLeast(10) to height.coerceAtLeast(10)
}

private inline fun <reified T> AppWidgetManager.updateFromRemoteViews(
    context: Context, widgetUpdateAction: (width: Int, height: Int, widgetId: Int) -> RemoteViews
) {
    runCatching {
        getAppWidgetIds(ComponentName(context, T::class.java))?.forEach { widgetId ->
            val (width, height) = getWidgetSize(context, widgetId)
            updateAppWidget(widgetId, widgetUpdateAction(width, height, widgetId))
        }
    }.onFailure(logException).onFailure {
        if (Variants.enableDevelopmentFeatures) {
            Toast.makeText(
                context,
                "An error has happened, see the in-app log and post it to me",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

fun createAgeRemoteViews(context: Context, width: Int, height: Int, widgetId: Int): RemoteViews {
    val appPrefs = context.appPrefs
    val baseJdn = appPrefs.getJdnOrNull(PREF_SELECTED_DATE_AGE_WIDGET + widgetId) ?: Jdn.today
    val title = appPrefs.getString(PREF_TITLE_AGE_WIDGET + widgetId, null) ?: ""
    val subtitle = calculateDaysDifference(context.resources, baseJdn)
    val textColor = appPrefs.getString(PREF_SELECTED_WIDGET_TEXT_COLOR + widgetId, null)
        ?: DEFAULT_SELECTED_WIDGET_TEXT_COLOR
    val backgroundColor = appPrefs.getString(PREF_SELECTED_WIDGET_BACKGROUND_COLOR + widgetId, null)
        ?: DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
    val remoteViews = RemoteViews(context.packageName, R.layout.widget_age)
    remoteViews.setRoundBackground(R.id.age_widget_background, width, height, backgroundColor)
    remoteViews.setDirection(R.id.age_widget_root, context)
    remoteViews.setTextViewTextOrHideIfEmpty(R.id.textview_age_widget_title, title)
    remoteViews.setTextViewText(R.id.textview_age_widget, subtitle)
    val color = Color.parseColor(textColor)
    remoteViews.setTextColor(R.id.textview_age_widget_title, color)
    remoteViews.setTextColor(R.id.textview_age_widget, color)
    return remoteViews
}

private fun prepareViewForWidget(view: View, width: Int, height: Int) {
    if (selectedWidgetBackgroundColor != DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR) {
        view.background = createRoundDrawable(selectedWidgetBackgroundColor)
    }
    view.layoutDirection = view.context.resources.configuration.layoutDirection
    // https://stackoverflow.com/a/69080742
    view.measure(
        View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.AT_MOST),
        View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.AT_MOST)
    )
    view.layout(0, 0, width, height)
}

private fun createSunViewRemoteViews(
    context: Context, width: Int, height: Int, jdn: Jdn, prayTimes: PrayTimes?
): RemoteViews {
    val remoteViews = RemoteViews(context.packageName, R.layout.widget_sun_view)
    val sunView = SunView(context, textColor = Color.parseColor(selectedWidgetTextColor))
    prepareViewForWidget(sunView, width, height)
    prayTimes?.let { sunView.setPrayTimesAndMoonPhase(it, coordinates.calculateMoonPhase(jdn)) }
    sunView.initiate()
    remoteViews.setTextViewTextOrHideIfEmpty(
        R.id.message,
        if (coordinates == null) context.getString(R.string.ask_user_to_set_location) else ""
    )

    // These are used to generate preview,
    // view.setBackgroundColor(Color.parseColor("#80A0A0A0"))
    // val outStream = ByteArrayOutputStream()
    // view.drawToBitmap().compress(Bitmap.CompressFormat.PNG, 100, outStream)
    // copyToClipboard(Base64.encodeToString(outStream.toByteArray(), Base64.DEFAULT)) {}
    // $ convert -scale 50% a.png b.png
    // $ zopflipng --iterations=15 --filters=01234mepb --lossy_8bit --lossy_transparent b.png c.png
    remoteViews.setImageViewBitmap(R.id.image, sunView.drawToBitmap())
    remoteViews.setContentDescription(R.id.image, sunView.contentDescription)
    remoteViews.setOnClickPendingIntent(
        R.id.widget_layout_sun_view,
        context.launchAppPendingIntent()
    )
    return remoteViews
}

private fun createMonthViewRemoteViews(
    context: Context, width: Int, height: Int, date: AbstractDate
): RemoteViews {
    val remoteViews = RemoteViews(context.packageName, R.layout.widget_image_view)
    val monthView = MonthView(ContextThemeWrapper(context, R.style.ModernTheme))
    monthView.initializeForWidget(Color.parseColor(selectedWidgetTextColor), height, date)
    prepareViewForWidget(monthView, width, height)
    remoteViews.setImageViewBitmap(R.id.image, monthView.drawToBitmap())
    remoteViews.setContentDescription(R.id.image, monthView.contentDescription)
    remoteViews.setOnClickPendingIntent(R.id.image, context.launchAppPendingIntent())
    return remoteViews
}

fun createSampleRemoteViews(context: Context, width: Int, height: Int): RemoteViews {
    val remoteViews = RemoteViews(context.packageName, R.layout.widget_sample)
    remoteViews.setRoundBackground(R.id.widget_sample_background, width, height)
    remoteViews.setDirection(R.id.widget_sample, context)
    val color = Color.parseColor(selectedWidgetTextColor)
    remoteViews.setTextColor(R.id.sample_text, color)
    remoteViews.setTextColor(R.id.sample_clock, color)
    remoteViews.setTextColor(R.id.sample_clock_replacement, color)
    if (isWidgetClock) {
        remoteViews.setViewVisibility(R.id.sample_clock, View.VISIBLE)
        remoteViews.configureClock(R.id.sample_clock)
        remoteViews.setTextViewTextOrHideIfEmpty(R.id.sample_clock_replacement, "")
    } else {
        remoteViews.setViewVisibility(R.id.sample_clock, View.GONE)
        remoteViews.setTextViewTextOrHideIfEmpty(R.id.sample_clock_replacement, getWeekDayName(0))
    }
    remoteViews.setTextViewText(R.id.sample_text, context.getString(R.string.widget_text_color))
    return remoteViews
}

private fun create1x1RemoteViews(
    context: Context, width: Int, height: Int, date: AbstractDate
): RemoteViews {
    val color = Color.parseColor(selectedWidgetTextColor)
    val remoteViews = RemoteViews(context.packageName, R.layout.widget1x1)
    remoteViews.setRoundBackground(R.id.widget_layout1x1_background, width, height)
    remoteViews.setDirection(R.id.widget_layout1x1, context)
    remoteViews.setTextColor(R.id.textPlaceholder1_1x1, color)
    remoteViews.setTextColor(R.id.textPlaceholder2_1x1, color)
    remoteViews.setTextViewText(R.id.textPlaceholder1_1x1, formatNumber(date.dayOfMonth))
    remoteViews.setTextViewText(R.id.textPlaceholder2_1x1, date.monthName)
    remoteViews.setOnClickPendingIntent(R.id.widget_layout1x1, context.launchAppPendingIntent())
    return remoteViews
}

private fun create4x1RemoteViews(
    context: Context, width: Int, height: Int, jdn: Jdn, date: AbstractDate, widgetTitle: String,
    subtitle: String
): RemoteViews {
    val weekDayName = jdn.dayOfWeekName
    val enableClock =
        isWidgetClock && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
    val showOtherCalendars = OTHER_CALENDARS_KEY in whatToShowOnWidgets
    val mainDateString = formatDate(date, calendarNameInLinear = showOtherCalendars)
    val remoteViews = RemoteViews(
        context.packageName, if (enableClock) {
            if (isCenterAlignWidgets) R.layout.widget4x1_clock_center else R.layout.widget4x1_clock
        } else {
            if (isCenterAlignWidgets) R.layout.widget4x1_center else R.layout.widget4x1
        }
    )
    if (enableClock) remoteViews.configureClock(R.id.textPlaceholder1_4x1)
    val color = Color.parseColor(selectedWidgetTextColor)
    remoteViews.setRoundBackground(R.id.widget_layout4x1_background, width, height)
    remoteViews.setDirection(R.id.widget_layout4x1, context)
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
        if (enableClock && isForcedIranTimeEnabled) "(" + context.getString(R.string.iran_time) + ")" else ""
    )
    remoteViews.setOnClickPendingIntent(R.id.widget_layout4x1, context.launchAppPendingIntent())
    return remoteViews
}

private fun create2x2RemoteViews(
    context: Context, width: Int, height: Int, jdn: Jdn, date: AbstractDate, widgetTitle: String,
    subtitle: String, owghat: String
): RemoteViews {
    val weekDayName = jdn.dayOfWeekName
    val enableClock =
        isWidgetClock && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
    val showOtherCalendars = OTHER_CALENDARS_KEY in whatToShowOnWidgets
    val mainDateString = formatDate(date, calendarNameInLinear = showOtherCalendars)
    val remoteViews = RemoteViews(
        context.packageName, if (enableClock) {
            if (isCenterAlignWidgets) R.layout.widget2x2_clock_center else R.layout.widget2x2_clock
        } else {
            if (isCenterAlignWidgets) R.layout.widget2x2_center else R.layout.widget2x2
        }
    )
    if (enableClock) remoteViews.configureClock(R.id.time_2x2)
    val color = Color.parseColor(selectedWidgetTextColor)
    remoteViews.setRoundBackground(R.id.widget_layout2x2_background, width, height)
    remoteViews.setDirection(R.id.widget_layout2x2, context)
    remoteViews.setTextColor(R.id.time_2x2, color)
    remoteViews.setTextColor(R.id.date_2x2, color)
    remoteViews.setTextColor(R.id.event_2x2, color)
    remoteViews.setTextColor(R.id.owghat_2x2, color)

    setEventsInWidget(context, jdn, remoteViews, R.id.holiday_2x2, R.id.event_2x2)

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

    remoteViews.setOnClickPendingIntent(R.id.widget_layout2x2, context.launchAppPendingIntent())
    return remoteViews
}

private fun create4x2RemoteViews(
    context: Context, width: Int, height: Int, jdn: Jdn, date: AbstractDate, nowClock: Clock,
    prayTimes: PrayTimes?
): RemoteViews {
    val weekDayName = jdn.dayOfWeekName
    val showOtherCalendars = OTHER_CALENDARS_KEY in whatToShowOnWidgets
    val enableClock =
        isWidgetClock && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
    val remoteViews = RemoteViews(
        context.packageName, if (enableClock) R.layout.widget4x2_clock else R.layout.widget4x2
    )

    if (enableClock) remoteViews.configureClock(R.id.textPlaceholder0_4x2)
    remoteViews.setRoundBackground(R.id.widget_layout4x2_background, width, height)
    remoteViews.setDirection(R.id.widget_layout4x2, context)

    val color = Color.parseColor(selectedWidgetTextColor)
    remoteViews.setTextColor(R.id.textPlaceholder0_4x2, color)
    remoteViews.setTextColor(R.id.textPlaceholder1_4x2, color)
    remoteViews.setTextColor(R.id.textPlaceholder4owghat_3_4x2, color)
    remoteViews.setTextColor(R.id.textPlaceholder4owghat_1_4x2, color)
    remoteViews.setTextColor(R.id.textPlaceholder4owghat_4_4x2, color)
    remoteViews.setTextColor(R.id.textPlaceholder4owghat_2_4x2, color)
    remoteViews.setTextColor(R.id.textPlaceholder4owghat_5_4x2, color)
    remoteViews.setTextColor(R.id.event_4x2, color)

    if (!enableClock) remoteViews.setTextViewText(R.id.textPlaceholder0_4x2, weekDayName)
    remoteViews.setTextViewText(R.id.textPlaceholder1_4x2, buildString {
        if (enableClock) append(jdn.dayOfWeekName + "\n")
        append(formatDate(date, calendarNameInLinear = showOtherCalendars))
        if (showOtherCalendars) appendLine().append(dateStringOfOtherCalendars(jdn, "\n"))
    })

    if (prayTimes != null) {
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
            val timeClock = prayTimes.getFromStringId(owghatStringId) ?: Clock.fromInt(0)
            remoteViews.setTextViewText(
                textHolderViewId, context.getString(owghatStringId) + "\n" +
                        timeClock?.toFormattedString(printAmPm = false)
            )
            remoteViews.setTextColor(textHolderViewId, color)
            textHolderViewId to timeClock
        }.firstOrNull { (_, timeClock) -> timeClock.toInt() > nowClock.toInt() }
            ?.let { (viewId, _) -> remoteViews.setTextColor(viewId, Color.RED) }
    } else remoteViews.setViewVisibility(R.id.widget4x2_owghat, View.GONE)

    setEventsInWidget(context, jdn, remoteViews, R.id.holiday_4x2, R.id.event_4x2)

    remoteViews.setOnClickPendingIntent(R.id.widget_layout4x2, context.launchAppPendingIntent())
    return remoteViews
}

private fun setEventsInWidget(
    context: Context, jdn: Jdn, remoteViews: RemoteViews, holidaysId: Int, eventsId: Int
) {
    val events = getEvents(jdn, deviceCalendarEvents)
    val holidays = getEventsTitle(
        events, holiday = true, compact = true, showDeviceCalendarEvents = true,
        insertRLM = context.resources.isRtl, addIsHoliday = isHighTextContrastEnabled
    )
    remoteViews.setTextViewTextOrHideIfEmpty(holidaysId, holidays)
    if (isTalkBackEnabled)
        remoteViews.setContentDescription(
            holidaysId,
            context.getString(R.string.holiday_reason, holidays)
        )

    val nonHolidays = if (NON_HOLIDAYS_EVENTS_KEY in whatToShowOnWidgets) getEventsTitle(
        events, holiday = false, compact = true, showDeviceCalendarEvents = true,
        insertRLM = context.resources.isRtl, addIsHoliday = false
    ) else ""
    remoteViews.setTextViewTextOrHideIfEmpty(eventsId, nonHolidays)
}

private fun updateNotification(
    context: Context, title: String, subtitle: String, jdn: Jdn, date: AbstractDate, owghat: String
) {
    if (!isNotifyDate) {
        if (enableWorkManager)
            context.getSystemService<NotificationManager>()?.cancel(NOTIFICATION_ID)
        return
    }

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

    // Prepend a right-to-left mark character to Android with sane text rendering stack
    // to resolve a bug seems some Samsung devices have with characters with weak direction,
    // digits being at the first of string on
    val toPrepend =
        if (context.resources.isRtl && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) RLM else ""

    val builder = NotificationCompat.Builder(context, NOTIFICATION_ID.toString())
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
        .setWhen(0)
        .setContentIntent(context.launchAppPendingIntent())
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
                    context = context, jdn = jdn,
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
            val icon = IconCompat.createWithBitmap(createStatusIcon(context, date.dayOfMonth))
            builder.setSmallIcon(icon)
        }
    } else {
        builder.setSmallIcon(getDayIconResource(date.dayOfMonth))
    }

    // Night mode doesn't like our custom notification in Samsung and HTC One UI
    val shouldDisableCustomNotification = when (Build.BRAND) {
        "samsung", "htc" -> isNightModeEnabled(context)
        else -> false
    }

    if (!isTalkBackEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val events = getEvents(jdn, deviceCalendarEvents)
        val holidays = getEventsTitle(
            events, holiday = true,
            compact = true, showDeviceCalendarEvents = true, insertRLM = context.resources.isRtl,
            addIsHoliday = shouldDisableCustomNotification || isHighTextContrastEnabled
        )

        val nonHolidays = if (NON_HOLIDAYS_EVENTS_KEY in whatToShowOnWidgets) getEventsTitle(
            events, holiday = false,
            compact = true, showDeviceCalendarEvents = true, insertRLM = context.resources.isRtl,
            addIsHoliday = false
        ) else ""

        val notificationOwghat = if (OWGHAT_KEY in whatToShowOnWidgets) owghat else ""

        if (shouldDisableCustomNotification) {
            val content = listOf(subtitle, holidays.trim(), nonHolidays, notificationOwghat)
                .filter { it.isNotBlank() }.joinToString("\n")
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(content))
        } else {
            builder.setCustomContentView(RemoteViews(
                context.packageName,
                if (context.resources.isRtl) R.layout.custom_notification else R.layout.custom_notification_ltr
            ).also {
                it.setTextViewText(R.id.title, title)
                it.setTextViewText(R.id.body, subtitle)
            })

            if (listOf(holidays, nonHolidays, notificationOwghat).any { it.isNotBlank() })
                builder.setCustomBigContentView(RemoteViews(
                    context.packageName,
                    if (context.resources.isRtl) R.layout.custom_notification_big else R.layout.custom_notification_big_ltr
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

    if (Variants.enableDevelopmentFeatures) builder.setWhen(System.currentTimeMillis())

    if (enableWorkManager) notificationManager?.notify(NOTIFICATION_ID, builder.build())
    else context.runCatching {
        ApplicationService.getInstance()?.startForeground(NOTIFICATION_ID, builder.build())
    }.onFailure(logException)
}

private fun RemoteViews.setRoundBackground(
    @IdRes viewId: Int, width: Int, height: Int, color: String = selectedWidgetBackgroundColor
) {
    if (color == DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR) setImageViewResource(viewId, 0)
    else setImageViewBitmap(viewId, createRoundDrawable(color).toBitmap(width, height))
}

private fun createRoundDrawable(color: String): Drawable {
    return MaterialShapeDrawable().also {
        it.fillColor = ColorStateList.valueOf(Color.parseColor(color))
        // https://developer.android.com/about/versions/12/features/widgets#ensure-compatibility
        // Apply a 16dp round corner which is the default in Android 12 apparently
        it.shapeAppearanceModel = ShapeAppearanceModel().withCornerSize(16.dp)
    }
}

private fun RemoteViews.setDirection(@IdRes viewId: Int, context: Context) {
    val direction =
        if (language.isArabicScript) View.LAYOUT_DIRECTION_RTL // just in case something went wrong
        else context.resources.configuration.layoutDirection
    setInt(viewId, "setLayoutDirection", direction)
}

private fun RemoteViews.configureClock(@IdRes viewId: Int) {
    if (isForcedIranTimeEnabled) setString(viewId, "setTimeZone", "Asia/Tehran")
    val clockFormat = if (clockIn24) "H:mm" else "h:mm"
    setCharSequence(viewId, "setFormat12Hour", clockFormat)
    setCharSequence(viewId, "setFormat24Hour", clockFormat)
}

private fun RemoteViews.setTextViewTextOrHideIfEmpty(viewId: Int, text: CharSequence) {
    if (text.isBlank()) setViewVisibility(viewId, View.GONE)
    else {
        setViewVisibility(viewId, View.VISIBLE)
        setTextViewText(viewId, text.trim())
    }
}

private fun Context.launchAppPendingIntent(): PendingIntent? = PendingIntent.getActivity(
    this, 0,
    Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
)
