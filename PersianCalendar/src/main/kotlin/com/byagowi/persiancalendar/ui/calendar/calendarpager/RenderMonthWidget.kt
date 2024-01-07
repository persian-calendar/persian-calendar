package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.graphics.Canvas
import androidx.core.graphics.withTranslation
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.eventsRepository
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.mainCalendarDigits
import com.byagowi.persiancalendar.utils.applyWeekStartOffsetToWeekDay
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getInitialOfWeekDay
import com.byagowi.persiancalendar.utils.getShiftWorkTitle
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.utils.revertWeekStartOffsetFromWeekDay
import io.github.persiancalendar.calendar.AbstractDate

fun renderMonthWidget(
    dayPainter: DayPainter,
    width: Int,
    canvas: Canvas,
    baseDate: AbstractDate,
    today: Jdn,
    deviceEvents: EventsStore<CalendarEvent.DeviceCalendarEvent>,
    isRtl: Boolean,
    isShowWeekOfYearEnabled: Boolean,
    selectedDay: Jdn?,
): String {
    val monthStartJdn = Jdn(baseDate)
    val startingDayOfWeek = monthStartJdn.dayOfWeek
    val monthLength = mainCalendar.getMonthLength(baseDate.year, baseDate.month)

    val cellWidth = dayPainter.width
    val cellHeight = dayPainter.height

    val footer = language.value.my.format(baseDate.monthName, formatNumber(baseDate.year))

    canvas.also {
        (0..<7).forEach { column ->
            val xStart = cellWidth * if (isShowWeekOfYearEnabled) 1 else 0
            it.withTranslation(
                if (isRtl) width - cellWidth * (column + 1) - xStart
                else cellWidth * column + xStart,
                0f
            ) {
                dayPainter.setInitialOfWeekDay(
                    getInitialOfWeekDay(revertWeekStartOffsetFromWeekDay(column))
                )
                dayPainter.drawDay(this)
            }
        }
        val monthRange = 0..<monthLength
        val rowsCount = 7
        (0..<rowsCount - 1).forEach { row ->
            (0..<7).forEach cell@{ column ->
                val dayOffset = (column + row * 7) -
                        applyWeekStartOffsetToWeekDay(startingDayOfWeek)
                if (dayOffset !in monthRange) return@cell
                val day = monthStartJdn + dayOffset
                val events = eventsRepository?.getEvents(day, deviceEvents) ?: emptyList()
                val isToday = day == today

                dayPainter.setDayOfMonthItem(
                    isToday = isToday,
                    isSelected = day == selectedDay,
                    hasEvent = events.any { it !is CalendarEvent.DeviceCalendarEvent },
                    hasAppointment = events.any { it is CalendarEvent.DeviceCalendarEvent },
                    isHoliday = events.any { it.isHoliday },
                    jdn = day,
                    dayOfMonth = formatNumber(dayOffset + 1, mainCalendarDigits),
                    header = getShiftWorkTitle(day, true)
                )

                val xStart = cellWidth * if (isShowWeekOfYearEnabled) 1 else 0
                it.withTranslation(
                    if (isRtl) width - cellWidth * (column + 1) - xStart
                    else cellWidth * column + xStart,
                    cellHeight * (row + 1),
                ) { dayPainter.drawDay(this) }
            }
        }
        if (isShowWeekOfYearEnabled) {
            val startOfYearJdn = Jdn(mainCalendar, baseDate.year, 1, 1)
            val weekOfYearStart = monthStartJdn.getWeekOfYear(startOfYearJdn)
            val weeksCount = (monthStartJdn + monthLength - 1).getWeekOfYear(startOfYearJdn) -
                    weekOfYearStart + 1
            (1..weeksCount).forEach { week ->
                val weekNumber = formatNumber(weekOfYearStart + week - 1)
                dayPainter.setWeekNumber(weekNumber)

                it.withTranslation(
                    if (isRtl) width - cellWidth else 0f, cellHeight * week,
                ) { dayPainter.drawDay(this) }
            }
        }
    }

    return footer
}

