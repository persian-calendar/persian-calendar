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
import com.byagowi.persiancalendar.global.prayTimesTitles
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.ui.calendar.eventTextColor
import com.byagowi.persiancalendar.ui.calendar.sortEvents
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import com.byagowi.persiancalendar.utils.eventKey
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getEnabledAlarms
import com.byagowi.persiancalendar.utils.jdnActionKey
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.utils.readWeekDeviceEvents
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import io.github.persiancalendar.calendar.AbstractDate
import java.util.GregorianCalendar

class ScheduleWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory =
        EventsViewFactory(this.applicationContext)
}

private fun getWeekEvents(context: Context, today: Jdn): List<Pair<Jdn, List<CalendarEvent<*>>>> {
    val deviceEvents = if (isShowDeviceCalendarEvents.value) {
        context.readWeekDeviceEvents(today)
    } else EventsStore.empty()
    return today.let { (it..it + 6) }.map {
        it to sortEvents(eventsRepository?.getEvents(it, deviceEvents) ?: emptyList())
    }.toList()
}

private fun getNextEnabledTime(enabledAlarms: Set<PrayTime>): Pair<String, Int> {
    if (enabledAlarms.isEmpty()) return "" to 0
    val time = GregorianCalendar()
    val now = Clock(time)
    return coordinates.value?.calculatePrayTimes(time)?.let { times ->
        val next = enabledAlarms.firstOrNull { times[it] > now }
            ?: enabledAlarms.first()
        (prayTimesTitles[next] ?: "") + spacedColon + times[next].toBasicFormatString() to
                next.tint.toArgb()
    } ?: ("" to 0)
}

private class EventsViewFactory(val context: Context) : RemoteViewsService.RemoteViewsFactory {
    private object Spacer
    private object NextTime
    private object NothingScheduled

    private val enabledAlarms = getEnabledAlarms(context)
    private val items = getWeekEvents(context, Jdn.today()).flatMapIndexed { i, (day, events) ->
        if (i != 0 && events.isEmpty()) return@flatMapIndexed emptyList()
        buildList {
            add(day to (day on mainCalendar))
            if (enabledAlarms.isNotEmpty() && i == 0) add(day to NextTime)
            addAll(events.map { day to it }.ifEmpty { listOf(day to NothingScheduled) })
        }
    } + listOf(Spacer)

    override fun onCreate() = Unit
    override fun onDestroy() = Unit
    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = items[position].hashCode().toLong()
    override fun hasStableIds(): Boolean = true
    override fun onDataSetChanged() = Unit
    override fun getCount(): Int = items.size
    override fun getViewAt(position: Int): RemoteViews {
        val row = RemoteViews(context.packageName, R.layout.widget_schedule_item)
        val item = items[position]
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            row.setBoolean(R.id.event, "setClipToOutline", true)
            row.setViewOutlinePreferredRadius(R.id.event, 12f, TypedValue.COMPLEX_UNIT_DIP)
        }
        row.setInt(R.id.event, "setTextColor", Color.WHITE)

        if (item !is Pair<*, *>) {
            row.setOnClickFillInIntent(R.id.widget_schedule_item_root, Intent())
            row.setViewVisibility(R.id.spacer, View.VISIBLE)
            row.setViewVisibility(R.id.header, View.GONE)
            row.setViewVisibility(R.id.event, View.GONE)
            return row
        }
        val day = (item.first as? Jdn).debugAssertNotNull ?: Jdn.today()

        (item.second as? AbstractDate)?.let { date ->
            if (position == 0) {
                row.setTextViewText(R.id.weekday_name_today, day.weekDayName)
                row.setViewVisibility(R.id.weekday_name_today, View.VISIBLE)
                row.setViewVisibility(R.id.weekday_name, View.GONE)
                row.setViewVisibility(R.id.top_space, View.VISIBLE)
            } else {
                row.setTextViewText(R.id.weekday_name, day.weekDayName)
                row.setViewVisibility(R.id.weekday_name_today, View.GONE)
                row.setViewVisibility(R.id.weekday_name, View.VISIBLE)
                row.setViewVisibility(R.id.top_space, View.GONE)
            }
            row.setTextViewText(R.id.day_of_month, formatNumber(date.dayOfMonth))
            val firstDayMonth = ((items[0] as? Pair<*, *>)?.second as? AbstractDate)?.month
            if (position != 0 && date.month != firstDayMonth) {
                row.setTextViewText(R.id.month_name, date.monthName)
                row.setViewVisibility(R.id.month_name, View.VISIBLE)
            } else row.setViewVisibility(R.id.month_name, View.GONE)
            row.setViewVisibility(R.id.spacer, View.GONE)
            row.setViewVisibility(R.id.header, View.VISIBLE)
            row.setViewVisibility(R.id.event, View.GONE)
            val clickIntent = Intent().putExtra(jdnActionKey, day.value)
            row.setOnClickFillInIntent(R.id.widget_schedule_item_root, clickIntent)
            return row
        }

        val event = item.second as? CalendarEvent<*>
        if (item.second == NextTime) {
            val (title, color) = getNextEnabledTime(enabledAlarms)
            row.setTextViewText(R.id.event, title)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                row.setInt(R.id.event, "setBackgroundColor", color)
                row.setInt(R.id.event, "setTextColor", eventTextColor(color))
                row.setBoolean(R.id.event, "setClipToOutline", true)
                row.setViewOutlinePreferredRadius(
                    R.id.event, 12f, TypedValue.COMPLEX_UNIT_DIP
                )
            } else {
                val background = R.drawable.widget_schedule_item_time
                row.setInt(R.id.event, "setBackgroundResource", background)
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (item.second == NothingScheduled) {
                    row.setColorAttr(
                        R.id.event,
                        "setTextColor",
                        android.R.attr.colorAccent,
                    )
                    row.setInt(
                        R.id.event,
                        "setBackgroundResource",
                        R.drawable.widget_nothing_scheduled,
                    )
                } else if (event is CalendarEvent<*>) {
                    if (event is CalendarEvent.DeviceCalendarEvent) {
                        val background =
                            if (event.color.isEmpty()) Color.GRAY else event.color.toLong()
                                .toInt()
                        row.setInt(R.id.event, "setBackgroundColor", background)
                        val foreground = eventTextColor(background)
                        row.setInt(R.id.event, "setTextColor", foreground)
                    } else {
                        row.setColorAttr(
                            R.id.event,
                            "setBackgroundColor",
                            if (event.isHoliday == true) android.R.attr.colorAccent
                            else android.R.attr.colorButtonNormal,
                        )
                        row.setColorAttr(
                            R.id.event,
                            "setTextColor",
                            if (event.isHoliday == true) android.R.attr.colorForegroundInverse
                            else android.R.attr.colorForeground,
                        )
                    }
                    row.setTextViewText(R.id.event, event.oneLinerTitleWithTime ?: "")
                }
            } else {
                val background = when {
                    event?.isHoliday == true -> R.drawable.widget_schedule_item_holiday
                    event is CalendarEvent.DeviceCalendarEvent ->
                        R.drawable.widget_schedule_item_event

                    else -> R.drawable.widget_schedule_item_default
                }
                row.setInt(R.id.event, "setBackgroundResource", background)
            }
            run {
                val title = when {
                    event?.isHoliday == true ->
                        language.value.inParentheses.format(event.title, holidayString)

                    event is CalendarEvent<*> -> event.oneLinerTitleWithTime
                    item.second == NothingScheduled -> nothingScheduledString

                    else -> ""
                }
                row.setTextViewText(R.id.event, title)
            }
        }

        row.setViewVisibility(R.id.spacer, View.GONE)
        row.setViewVisibility(R.id.header, View.GONE)
        row.setViewVisibility(R.id.event, View.VISIBLE)
        val clickIntent = if (event is CalendarEvent.DeviceCalendarEvent) {
            Intent().putExtra(eventKey, event.id)
        } else Intent().putExtra(jdnActionKey, day.value)
        row.setOnClickFillInIntent(R.id.widget_schedule_item_root, clickIntent)
        return row
    }
}
