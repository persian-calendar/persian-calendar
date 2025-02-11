package com.byagowi.persiancalendar.service

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.compose.ui.graphics.toArgb
import androidx.core.text.buildSpannedString
import androidx.core.text.scale
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.PrayTime
import com.byagowi.persiancalendar.entities.PrayTime.Companion.get
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.eventsRepository
import com.byagowi.persiancalendar.global.holidayString
import com.byagowi.persiancalendar.global.isShowDeviceCalendarEvents
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.nothingScheduledString
import com.byagowi.persiancalendar.global.prayTimesTitles
import com.byagowi.persiancalendar.global.secondaryCalendar
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.ui.calendar.eventTextColor
import com.byagowi.persiancalendar.ui.calendar.sortEvents
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import com.byagowi.persiancalendar.utils.calendar
import com.byagowi.persiancalendar.utils.eventKey
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getEnabledAlarms
import com.byagowi.persiancalendar.utils.getShiftWorkTitle
import com.byagowi.persiancalendar.utils.jdnActionKey
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.utils.readTwoWeekDeviceEvents
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import io.github.persiancalendar.calendar.AbstractDate
import java.util.GregorianCalendar
import kotlin.math.roundToInt

class ScheduleWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory =
        EventsViewFactory(this.applicationContext, intent.getIntExtra(widgetWidthCellKey, -1))
}

const val widgetWidthCellKey = "width"

private class EventsViewFactory(
    val context: Context,
    val widthCells: Int,
) : RemoteViewsService.RemoteViewsFactory {
    private object Spacer
    private object NextTime
    private data class Header(
        val day: Jdn,
        val date: AbstractDate,
        val secondaryDate: AbstractDate?,
        val withMonth: Boolean
    )

    private data class Item(
        val value: Any,
        val day: Jdn,
        val date: AbstractDate,
        val secondaryDate: AbstractDate?,
        val today: Boolean,
        val first: Boolean
    )

    private val enabledAlarms = getEnabledAlarms(context)
    private val items = run {
        val today = Jdn.today()
        val deviceEvents = if (isShowDeviceCalendarEvents.value) {
            context.readTwoWeekDeviceEvents(today)
        } else EventsStore.empty()
        val days = (today..<today + 14).toList()
        val dates = days.map { it on mainCalendar }
        val secondaryDates = secondaryCalendar?.let { calendar -> days.map { it on calendar } }
        var monthChange = false
        var secondaryMonthChange = false
        days.map {
            it to sortEvents(eventsRepository?.getEvents(it, deviceEvents) ?: emptyList())
        }.flatMapIndexed { i, (day, events) ->
            val items = buildList {
                val shiftWorkTitle = getShiftWorkTitle(day)
                if (shiftWorkTitle != null) add(shiftWorkTitle)
                if (events.isEmpty() && shiftWorkTitle == null && i == 0) {
                    add(nothingScheduledString)
                } else addAll(events)
                if (enabledAlarms.isNotEmpty() && i == 0) add(NextTime)
            }
            val date = dates[i]
            val secondaryDate = secondaryDates?.let { it[i] }
            when {
                i != 0 && items.isEmpty() -> listOf()
                dates[0].month != date.month && !monthChange -> {
                    monthChange = true
                    if (secondaryDates?.get(0)?.month != secondaryDate?.month) {
                        secondaryMonthChange = true
                    }
                    listOf(Header(day, date, secondaryDate, true))
                }

                secondaryDates?.get(0)?.month != secondaryDate?.month && !secondaryMonthChange -> {
                    secondaryMonthChange = true
                    listOf(Header(day, date, secondaryDate, true))
                }

                else -> listOf(Header(day, date, secondaryDate, false))
            } + items.mapIndexed { j, item -> Item(item, day, date, secondaryDate, i == 0, j == 0) }
        }
    } + listOf(Spacer)

    override fun onCreate() = Unit
    override fun onDestroy() = Unit
    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = position.toLong()
    override fun hasStableIds(): Boolean = true
    override fun onDataSetChanged() = Unit
    override fun getCount(): Int = items.size
    override fun getViewAt(position: Int): RemoteViews {
        val row = RemoteViews(context.packageName, R.layout.widget_schedule_item)
        val entry = items[position]
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            row.setBoolean(R.id.event_background, "setClipToOutline", true)
            row.setViewOutlinePreferredRadius(
                R.id.event_background,
                12f,
                TypedValue.COMPLEX_UNIT_DIP
            )
        }
        row.setInt(R.id.event, "setTextColor", Color.WHITE)

        if (entry == Spacer) {
            row.setOnClickFillInIntent(R.id.widget_schedule_item_root, Intent())
            row.setViewVisibility(R.id.header, View.GONE)
            row.setViewVisibility(R.id.event_parent, View.GONE)
            return row
        }

        val dp = context.resources.dp
        fun Int.dp() = (this * dp).roundToInt()

        (entry as? Header)?.let { header ->
            val weekDayName = header.secondaryDate?.let {
                val secondaryDayOfMonth = formatNumber(it.dayOfMonth, it.calendar.preferredDigits)
                "${header.day.weekDayNameInitials}($secondaryDayOfMonth)"
            } ?: header.day.weekDayName
            if (position == 0 && widthCells < 3) {
                row.setTextViewText(R.id.day_of_month, formatNumber(header.date.dayOfMonth))
                row.setTextViewText(R.id.highlight, weekDayName)
                row.setViewVisibility(R.id.highlight, View.VISIBLE)
                row.setViewVisibility(R.id.weekday_name, View.GONE)
                row.setViewVisibility(R.id.day_of_month, View.VISIBLE)
                row.setViewVisibility(R.id.bigger_month_name, View.GONE)
            } else if (widthCells > 2) {
                row.setViewVisibility(R.id.weekday_name, View.GONE)
                row.setViewVisibility(R.id.highlight, View.GONE)
                row.setViewVisibility(R.id.day_of_month, View.GONE)
                if (header.withMonth || position == 0) {
                    val topSpace = when (position) {
                        0 -> if (header.secondaryDate == null) 12 else 2
                        else -> if (header.secondaryDate == null) 4 else 0
                    }.dp()
                    val bottomSpace = when (position) {
                        0 -> if (header.secondaryDate == null) 18 else 8
                        else -> if (header.secondaryDate == null) 12 else 4
                    }.dp()
                    val startSpace = (if (widthCells > 3) 8 else 4).dp()
                    val monthTitle = buildSpannedString {
                        append(header.date.monthName)
                        header.secondaryDate?.let { scale(.9f) { append("\n" + it.monthName) } }
                    }
                    row.setTextViewText(R.id.bigger_month_name, monthTitle)
                    row.setViewVisibility(R.id.bigger_month_name, View.VISIBLE)
                    row.setViewPadding(
                        R.id.bigger_month_name, startSpace, topSpace, startSpace, bottomSpace
                    )
                } else {
                    row.setViewVisibility(R.id.bigger_month_name, View.GONE)
                }
            } else {
                row.setTextViewText(R.id.day_of_month, formatNumber(header.date.dayOfMonth))
                row.setTextViewText(R.id.weekday_name, weekDayName)
                row.setViewVisibility(R.id.weekday_name, View.VISIBLE)
                row.setViewVisibility(R.id.day_of_month, View.VISIBLE)
                if (header.withMonth) {
                    val monthTitle = buildSpannedString {
                        append(header.date.monthName)
                        header.secondaryDate?.let { scale(.9f) { append(" (${it.monthName})") } }
                    }
                    row.setTextViewText(R.id.highlight, monthTitle)
                    row.setViewVisibility(R.id.highlight, View.VISIBLE)
                } else row.setViewVisibility(R.id.highlight, View.GONE)
                row.setViewVisibility(R.id.bigger_month_name, View.GONE)
            }
            row.setViewVisibility(R.id.header, View.VISIBLE)
            row.setViewVisibility(R.id.event_parent, View.GONE)
            val clickIntent = Intent().putExtra(jdnActionKey, header.day.value)
            row.setOnClickFillInIntent(R.id.widget_schedule_item_root, clickIntent)
            return row
        }

        run {
            val startPadding = when {
                widthCells > 3 -> 12
                widthCells == 3 -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) 4 else 6
                else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) 4 else 8
            }.dp()
            row.setViewPadding(R.id.event_start_padding, startPadding, 0, 0, 0)

            val betweenPadding = when {
                widthCells > 3 -> 8
                widthCells == 3 -> 4
                else -> 0
            }.dp()
            row.setViewPadding(R.id.event_middle_padding, betweenPadding, 0, 0, 0)

            val endPadding = when {
                widthCells > 3 -> 16
                widthCells == 3 -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) 4 else 8
                else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) 4 else 8
            }.dp()
            row.setViewPadding(R.id.event_end_padding, endPadding, 0, 0, 0)
        }

        val item = (entry as? Item).debugAssertNotNull ?: return row
        val event = item.value as? CalendarEvent<*>
        if (item.value == NextTime) {
            val (title, color) = getNextEnabledTime(enabledAlarms)
            row.setTextViewText(R.id.event, title)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                row.setInt(R.id.event_background, "setBackgroundColor", color)
                row.setInt(R.id.event, "setTextColor", eventTextColor(color))
                row.setInt(R.id.event_time, "setTextColor", eventTextColor(color))
                row.setBoolean(R.id.event_background, "setClipToOutline", true)
                row.setViewOutlinePreferredRadius(
                    R.id.event_background, 12f, TypedValue.COMPLEX_UNIT_DIP
                )
            } else {
                val background = R.drawable.widget_schedule_item_time
                row.setInt(R.id.event_background, "setBackgroundResource", background)
            }
            row.setViewVisibility(R.id.event_time, View.GONE)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (item.value is String) {
                    row.setColorAttr(
                        R.id.event,
                        "setTextColor",
                        android.R.attr.colorAccent,
                    )
                    row.setColorAttr(
                        R.id.event_time,
                        "setTextColor",
                        android.R.attr.colorAccent,
                    )
                    row.setInt(
                        R.id.event_background,
                        "setBackgroundResource",
                        R.drawable.widget_nothing_scheduled,
                    )
                } else if (event is CalendarEvent<*>) {
                    if (event is CalendarEvent.DeviceCalendarEvent) {
                        val background =
                            if (event.color.isEmpty()) Color.GRAY else event.color.toLong().toInt()
                        row.setInt(R.id.event_background, "setBackgroundColor", background)
                        row.setInt(R.id.event, "setTextColor", eventTextColor(background))
                        row.setInt(R.id.event_time, "setTextColor", eventTextColor(background))
                    } else {
                        if (event.isHoliday) row.setColorAttr(
                            R.id.event_background,
                            "setBackgroundColor",
                            android.R.attr.colorAccent,
                        ) else row.setInt(
                            R.id.event_background,
                            "setBackgroundResource",
                            R.drawable.widget_nothing_scheduled,
                        )
                        val textColor = if (event.isHoliday) android.R.attr.colorForegroundInverse
                        else android.R.attr.colorForeground
                        row.setColorAttr(R.id.event, "setTextColor", textColor)
                        row.setColorAttr(R.id.event_time, "setTextColor", textColor)
                    }
                }
            } else {
                val background = when {
                    event?.isHoliday == true -> R.drawable.widget_schedule_item_holiday
                    event is CalendarEvent.DeviceCalendarEvent -> R.drawable.widget_schedule_item_event

                    else -> R.drawable.widget_schedule_item_default
                }
                row.setInt(R.id.event_background, "setBackgroundResource", background)
            }
            val title = when {
                event?.isHoliday == true -> "[$holidayString] ${event.title}"
                event is CalendarEvent<*> -> event.title
                item.value is String -> item.value.toString()
                else -> ""
            }
            row.setTextViewText(R.id.event, title)
            (event as? CalendarEvent.DeviceCalendarEvent)?.time?.let {
                row.setTextViewText(R.id.event_time, it)
                row.setViewVisibility(R.id.event_time, View.VISIBLE)
            } ?: row.setViewVisibility(R.id.event_time, View.GONE)
        }

        row.setViewVisibility(R.id.header, View.GONE)
        row.setViewVisibility(R.id.event_parent, View.VISIBLE)
        if (widthCells > 2) {
            if (item.first) {
                if (item.today) {
                    if (item.secondaryDate == null) {
                        row.setViewVisibility(R.id.today_first_line, View.VISIBLE)
                        row.setViewVisibility(R.id.today_second_line, View.VISIBLE)
                        row.setViewVisibility(R.id.today_with_secondary_first_line, View.GONE)
                        row.setViewVisibility(R.id.today_with_secondary_second_line, View.GONE)
                        row.setViewVisibility(R.id.day_first_line, View.GONE)
                        row.setViewVisibility(R.id.day_second_line, View.GONE)
                        row.setInt(
                            R.id.day_wrapper, "setBackgroundResource",
                            R.drawable.widget_schedule_day_today
                        )
                    } else {
                        row.setViewVisibility(R.id.today_first_line, View.GONE)
                        row.setViewVisibility(R.id.today_second_line, View.GONE)
                        row.setViewVisibility(R.id.today_with_secondary_first_line, View.VISIBLE)
                        row.setViewVisibility(R.id.today_with_secondary_second_line, View.VISIBLE)
                        row.setViewVisibility(R.id.day_first_line, View.GONE)
                        row.setViewVisibility(R.id.day_second_line, View.GONE)
                        row.setInt(R.id.day_wrapper, "setBackgroundResource", 0)
                    }
                } else {
                    row.setViewVisibility(R.id.today_first_line, View.GONE)
                    row.setViewVisibility(R.id.today_second_line, View.GONE)
                    row.setViewVisibility(R.id.today_with_secondary_first_line, View.GONE)
                    row.setViewVisibility(R.id.today_with_secondary_second_line, View.GONE)
                    row.setViewVisibility(R.id.day_first_line, View.VISIBLE)
                    row.setViewVisibility(R.id.day_second_line, View.VISIBLE)
                    row.setInt(R.id.day_wrapper, "setBackgroundResource", 0)
                }
                row.setViewVisibility(R.id.day_wrapper, View.VISIBLE)
                row.setTextViewText(
                    if (item.today) {
                        if (item.secondaryDate == null) R.id.today_first_line
                        else R.id.today_with_secondary_first_line
                    } else R.id.day_first_line,
                    item.day.weekDayNameInitials + (item.secondaryDate?.let {
                        "(${formatNumber(it.dayOfMonth, it.calendar.preferredDigits)})"
                    } ?: ""),
                )
                row.setTextViewText(
                    if (item.today) {
                        if (item.secondaryDate == null) R.id.today_second_line
                        else R.id.today_with_secondary_second_line
                    } else R.id.day_second_line,
                    formatNumber(item.date.dayOfMonth)
                )
            } else row.setViewVisibility(R.id.day_wrapper, View.INVISIBLE)
        } else row.setViewVisibility(R.id.day_wrapper, View.GONE)
        val clickIntent = if (event is CalendarEvent.DeviceCalendarEvent) {
            Intent().putExtra(eventKey, event.id)
        } else Intent().putExtra(jdnActionKey, item.day.value)
        row.setOnClickFillInIntent(R.id.widget_schedule_item_root, clickIntent)
        return row
    }

    private fun getNextEnabledTime(enabledAlarms: Set<PrayTime>): Pair<String, Int> {
        if (enabledAlarms.isEmpty()) return "" to 0
        val time = GregorianCalendar()
        val now = Clock(time)
        return coordinates.value?.calculatePrayTimes(time)?.let { times ->
            val next = enabledAlarms.firstOrNull { times[it] > now } ?: enabledAlarms.first()
            (prayTimesTitles[next]
                ?: "") + spacedColon + times[next].toFormattedString() to next.tint.toArgb()
        } ?: ("" to 0)
    }
}
