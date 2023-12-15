package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withTranslation
import androidx.core.text.layoutDirection
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.eventsRepository
import com.byagowi.persiancalendar.global.isShowDeviceCalendarEvents
import com.byagowi.persiancalendar.global.isShowWeekOfYearEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.utils.applyWeekStartOffsetToWeekDay
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getInitialOfWeekDay
import com.byagowi.persiancalendar.utils.getShiftWorkTitle
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.utils.readMonthDeviceEvents
import com.byagowi.persiancalendar.utils.revertWeekStartOffsetFromWeekDay
import kotlin.math.min

fun renderMonthWidget(
    context: Context,
    @ColorInt textColor: Int,
    width: Int,
    height: Int
): Pair<Bitmap, String> {
    val isShowWeekOfYearEnabled = isShowWeekOfYearEnabled
    val rowsCount = 7
    val cellHeight = height.toFloat() / rowsCount
    val columnsCount = if (isShowWeekOfYearEnabled) 8 else 7
    val cellWidth = width.toFloat() / columnsCount
    val diameter = min(cellWidth, cellHeight)
    val todayJdn = Jdn.today()
    val today = todayJdn.toCalendar(mainCalendar)
    val baseDate = mainCalendar.createDate(today.year, today.month, 1)
    val monthStartJdn = Jdn(baseDate)
    val startingDayOfWeek = monthStartJdn.dayOfWeek
    val monthLength = mainCalendar.getMonthLength(today.year, today.month)

    val isRtl =
        language.isLessKnownRtl || language.asSystemLocale().layoutDirection == View.LAYOUT_DIRECTION_RTL
    val dayPainter = DayPainter(context, cellWidth.toInt(), cellHeight.toInt(), isRtl, textColor)

    val monthDeviceEvents =
        if (isShowDeviceCalendarEvents) context.readMonthDeviceEvents(monthStartJdn)
        else EventsStore.empty()

    val footer = language.my.format(baseDate.monthName, formatNumber(baseDate.year))
    val bitmap = createBitmap(width, height)
    Canvas(bitmap).also {
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
        (0..<rowsCount - 1).forEach { row ->
            (0..<7).forEach cell@{ column ->
                val dayOffset = (column + row * 7) -
                        applyWeekStartOffsetToWeekDay(startingDayOfWeek)
                if (dayOffset !in monthRange) return@cell
                val day = monthStartJdn + dayOffset
                val events = eventsRepository?.getEvents(day, monthDeviceEvents) ?: emptyList()
                val isToday = day == todayJdn

                dayPainter.setDayOfMonthItem(
                    isToday, false,
                    events.any { it !is CalendarEvent.DeviceCalendarEvent },
                    events.any { it is CalendarEvent.DeviceCalendarEvent },
                    events.any { it.isHoliday }, day, dayOffset + 1,
                    getShiftWorkTitle(day, true)
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
            val startOfYearJdn = Jdn(mainCalendar, today.year, 1, 1)
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
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.textAlign = Paint.Align.CENTER
            it.textSize = diameter * 20 / 40
            it.color = textColor
            it.alpha = 90
        }
        it.drawText(footer, width / 2f, height * .95f, footerPaint)
    }
    return bitmap to footer
}

