package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.graphics.Canvas
import androidx.core.graphics.withTranslation
import com.byagowi.persiancalendar.OTHER_CALENDARS_KEY
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.eventsRepository
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendarNumeral
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.global.secondaryCalendar
import com.byagowi.persiancalendar.global.weekEnds
import com.byagowi.persiancalendar.global.weekStart
import com.byagowi.persiancalendar.global.whatToShowOnWidgets
import com.byagowi.persiancalendar.utils.getShiftWorkTitle
import com.byagowi.persiancalendar.utils.monthName
import io.github.persiancalendar.calendar.AbstractDate

fun renderMonthWidget(
    dayPainter: DayPainter,
    width: Float,
    canvas: Canvas,
    baseDate: AbstractDate,
    today: Jdn,
    deviceEvents: EventsStore<CalendarEvent.DeviceCalendarEvent>,
    isRtl: Boolean,
    isShowWeekOfYearEnabled: Boolean,
    selectedDay: Jdn?,
    calendar: Calendar,
    setWeekNumberText: ((i: Int, text: String) -> Unit)? = null,
    setText: ((i: Int, text: String, isHoliday: Boolean) -> Unit)? = null,
): String {
    val monthStartJdn = Jdn(baseDate)
    val startingWeekDay = monthStartJdn.weekDay
    val monthLength = calendar.getMonthLength(baseDate.year, baseDate.month)

    val cellWidth = dayPainter.width
    val cellHeight = dayPainter.height

    val footer =
        language.value.my.format(baseDate.monthName, numeral.value.format(baseDate.year))

    val weekStart = weekStart.value
    val weekEnds = weekEnds.value
    canvas.also {
        (0..<7).forEach { column ->
            val xStart = cellWidth * if (isShowWeekOfYearEnabled) 1 else 0
            it.withTranslation(
                if (isRtl) width - cellWidth * (column + 1) - xStart
                else cellWidth * column + xStart,
                0f
            ) {
                val text = (weekStart + column).shortTitle
                setText?.invoke(column, text, false) ?: dayPainter.setInitialOfWeekDay(text)
                dayPainter.drawDay(this)
            }
        }
        val monthRange = 0..<monthLength
        val rowsCount = 7
        val eventsRepository = eventsRepository.value
        val secondaryCalendar =
            if (OTHER_CALENDARS_KEY in whatToShowOnWidgets.value) secondaryCalendar else null
        (0..<rowsCount - 1).forEach { row ->
            (0..<7).forEach cell@{ column ->
                val dayOffset = (column + row * 7) - (startingWeekDay - weekStart)
                if (dayOffset !in monthRange) return@cell
                val day = monthStartJdn + dayOffset
                val events = eventsRepository.getEvents(day, deviceEvents)
                val isToday = day == today

                val text = mainCalendarNumeral.format(dayOffset + 1)
                val isHoliday = events.any { it.isHoliday } || day.weekDay in weekEnds
                dayPainter.setDayOfMonthItem(
                    isToday = isToday,
                    isSelected = day == selectedDay,
                    hasEvent = events.any { it !is CalendarEvent.DeviceCalendarEvent },
                    hasAppointment = events.any { it is CalendarEvent.DeviceCalendarEvent },
                    isHoliday = isHoliday,
                    jdn = day,
                    dayOfMonth = if (setText == null) text else "",
                    header = getShiftWorkTitle(day, true),
                    secondaryCalendar = secondaryCalendar,
                )
                if (setText != null) setText((row + 1) * 7 + column, text, isHoliday)

                val xStart = cellWidth * if (isShowWeekOfYearEnabled) 1 else 0
                it.withTranslation(
                    if (isRtl) width - cellWidth * (column + 1) - xStart
                    else cellWidth * column + xStart,
                    cellHeight * (row + 1),
                ) { dayPainter.drawDay(this) }
            }
        }
        if (isShowWeekOfYearEnabled) {
            val startOfYearJdn = Jdn(calendar, baseDate.year, 1, 1)
            val weekOfYearStart = monthStartJdn.getWeekOfYear(startOfYearJdn, weekStart)
            val weeksCount =
                (monthStartJdn + monthLength - 1).getWeekOfYear(startOfYearJdn, weekStart) -
                        weekOfYearStart + 1
            (1..6).forEach { week ->
                val weekNumber = if (week > weeksCount) ""
                else numeral.value.format(weekOfYearStart + week - 1)
                if (setWeekNumberText != null) {
                    setWeekNumberText(week, weekNumber)
                } else if (weekNumber.isNotEmpty()) {
                    dayPainter.setWeekNumber(weekNumber)

                    it.withTranslation(
                        if (isRtl) width - cellWidth else 0f, cellHeight * week,
                    ) { dayPainter.drawDay(this) }
                }
            }
        }
    }

    return footer
}

