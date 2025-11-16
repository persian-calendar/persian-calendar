package com.byagowi.persiancalendar.service

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
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
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.nothingScheduledString
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.global.prayTimesTitles
import com.byagowi.persiancalendar.global.secondaryCalendar
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.ui.calendar.eventTextColor
import com.byagowi.persiancalendar.ui.calendar.sortEvents
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import com.byagowi.persiancalendar.utils.calendar
import com.byagowi.persiancalendar.utils.debugAssertNotNull
import com.byagowi.persiancalendar.utils.eventKey
import com.byagowi.persiancalendar.utils.getEnabledAlarms
import com.byagowi.persiancalendar.utils.getShiftWorkTitle
import com.byagowi.persiancalendar.utils.jdnActionKey
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.utils.readTwoWeekDeviceEvents
import io.github.persiancalendar.calendar.AbstractDate
import java.util.GregorianCalendar

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
        val eventsRepository = eventsRepository.value
        days.map {
            it to sortEvents(eventsRepository.getEvents(it, deviceEvents), language.value)
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
        row.setInt(R.id.event, "setTextColor", Color.WHITE)

        if (entry == Spacer) {
            row.setOnClickFillInIntent(R.id.widget_schedule_item_root, Intent())
            row.setViewVisibility(R.id.header, View.GONE)
            row.setViewVisibility(R.id.event_parent, View.GONE)
            return row
        }

        (entry as? Header)?.let { header ->
            val headerContent = run {
                val headerFirstPart = if (widthCells < 3) {
                    header.day.weekDay.shortTitle + spacedComma + language.value.dm.format(
                        numeral.value.format(header.date.dayOfMonth),
                        header.date.monthName
                    )
                } else header.date.monthName
                header.secondaryDate?.let {
                    language.value.inParentheses.format(
                        headerFirstPart,
                        if (widthCells < 3) it.calendar.preferredNumeral.format(it.dayOfMonth)
                        else it.monthName
                    )
                } ?: headerFirstPart
            }
            if (position == 0) {
                row.setViewVisibility(R.id.first_header, View.VISIBLE)
                row.setViewVisibility(R.id.other_header, View.GONE)
                row.setTextViewText(R.id.first_header, headerContent)
            } else if (widthCells < 3 || header.withMonth) {
                row.setViewVisibility(R.id.first_header, View.GONE)
                row.setViewVisibility(R.id.other_header, View.VISIBLE)
                row.setTextViewText(R.id.other_header, headerContent)
            } else {
                row.setViewVisibility(R.id.first_header, View.GONE)
                row.setViewVisibility(R.id.other_header, View.GONE)
            }
            row.setViewVisibility(R.id.header, View.VISIBLE)
            row.setViewVisibility(R.id.event_parent, View.GONE)
            val clickIntent = Intent().putExtra(jdnActionKey, header.day.value)
            row.setOnClickFillInIntent(R.id.widget_schedule_item_root, clickIntent)
            return row
        }

        val item = (entry as? Item).debugAssertNotNull ?: return row
        val event = item.value as? CalendarEvent<*>
        val backgroundColor = if (item.value == NextTime) {
            val (title, color) = getNextEnabledTime(enabledAlarms)
            row.setTextViewText(R.id.event, title)
            row.setViewVisibility(R.id.event_time, View.GONE)
            color
        } else {
            val title = when {
                event?.isHoliday == true -> "[$holidayString] ${event.title}"
                event is CalendarEvent -> event.title
                item.value is String -> item.value
                else -> ""
            }
            row.setTextViewText(R.id.event, title)
            (event as? CalendarEvent.DeviceCalendarEvent)?.time?.let {
                row.setTextViewText(R.id.event_time, it)
                row.setViewVisibility(R.id.event_time, View.VISIBLE)
            } ?: row.setViewVisibility(R.id.event_time, View.GONE)

            if (item.value is String) {
                ContextCompat.getColor(context, R.color.widget_nothing_scheduled)
            } else if (event is CalendarEvent<*>) {
                if (event is CalendarEvent.DeviceCalendarEvent) {
                    if (event.color.isEmpty()) Color.GRAY else event.color.toLong().toInt()
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ContextCompat.getColor(
                        context,
                        if (event.isHoliday) android.R.color.system_accent1_200
                        else android.R.color.system_accent1_100
                    )
                } else if (event.isHoliday) 0xFFB0C6FF.toInt() else 0xFFD9E2FF.toInt()
            } else null.debugAssertNotNull ?: Color.TRANSPARENT
        }
        row.setInt(R.id.event_background, "setColorFilter", backgroundColor)
        val textColor = eventTextColor(backgroundColor)
        row.setTextColor(R.id.event, textColor)
        row.setTextColor(R.id.event_time, textColor)

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
                    item.day.weekDay.shortTitle + (item.secondaryDate?.let {
                        "(${it.calendar.preferredNumeral.format(it.dayOfMonth)})"
                    }.orEmpty()),
                )
                row.setTextViewText(
                    if (item.today) {
                        if (item.secondaryDate == null) R.id.today_second_line
                        else R.id.today_with_secondary_second_line
                    } else R.id.day_second_line,
                    numeral.value.format(item.date.dayOfMonth)
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
            prayTimesTitles[next].orEmpty() + spacedColon + times[next].toFormattedString() to next.tint.toArgb()
        } ?: ("" to 0)
    }
}
