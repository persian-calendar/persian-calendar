package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.DeviceCalendarEvent
import com.byagowi.persiancalendar.utils.*

class DaysAdapter internal constructor(
    private val context: Context, private val daysPaintResources: DaysPaintResources,
    private val calendarPager: CalendarPager
) : RecyclerView.Adapter<DaysAdapter.ViewHolder>() {

    var days: List<Long> = emptyList()
    var startingDayOfWeek: Int = 0
    var fixedStartingDayOfWeek: Int = 0
    var weekOfYearStart: Int = 0
    var weeksCount: Int = 0

    private val layoutParams: ViewGroup.LayoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        context.resources.getDimensionPixelSize(R.dimen.day_item_size)
    )
    private var monthEvents: DeviceCalendarEventsStore = emptyEventsStore()
    private val isArabicDigit: Boolean = isArabicDigitSelected()
    private var selectedDay = -1

    fun initializeMonthEvents() {
        if (isShowDeviceCalendarEvents) monthEvents = readMonthDeviceEvents(context, days[0])
    }

    internal fun selectDay(dayOfMonth: Int) {
        val prevDay = selectedDay
        selectedDay = -1
        notifyItemChanged(prevDay)

        if (dayOfMonth == -1) return

        selectedDay = dayOfMonth + 6 + fixedStartingDayOfWeek

        if (isShowWeekOfYearEnabled) selectedDay += selectedDay / 7 + 1

        notifyItemChanged(selectedDay)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        DayView(parent.context, daysPaintResources).also { it.layoutParams = layoutParams }
    )

    private fun hasDeviceEvents(dayEvents: List<CalendarEvent<*>>): Boolean =
        dayEvents.any { it is DeviceCalendarEvent }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

    // days of week * month view rows
    override fun getItemCount(): Int = 7 * if (isShowWeekOfYearEnabled) 8 else 7

    val todayJdn = getTodayJdn()

    inner class ViewHolder(itemView: DayView) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, View.OnLongClickListener {

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(v: View) {
            val itemDayView = v as DayView
            val jdn = itemDayView.jdn
            if (jdn == -1L) return

            calendarPager.onDayClicked(jdn)
            this@DaysAdapter.selectDay(itemDayView.dayOfMonth)
        }

        override fun onLongClick(v: View): Boolean {
            onClick(v)

            val itemDayView = v as DayView
            val jdn = itemDayView.jdn
            if (jdn == -1L) return false

            calendarPager.onDayLongClicked(jdn)
            return false
        }

        fun bind(position: Int) {
            var position = position
            val originalPosition = position
            val dayView = itemView as DayView
            if (isShowWeekOfYearEnabled) {
                if (position % 8 == 0) {
                    val row = position / 8
                    if (row in 1..weeksCount) {
                        val weekNumber = formatNumber(weekOfYearStart + row - 1)
                        dayView.setNonDayOfMonthItem(
                            weekNumber,
                            daysPaintResources.weekNumberTextSize
                        )
                        if (isTalkBackEnabled) {
                            dayView.contentDescription = String.format(
                                context.getString(R.string.nth_week_of_year),
                                weekNumber
                            )
                        }

                        dayView.visibility = View.VISIBLE
                    } else
                        setEmpty()
                    return
                }

                position = position - position / 8 - 1
            }

            if (days.size < position - 6 - fixedStartingDayOfWeek) {
                setEmpty()
            } else if (position < 7) {
                dayView.setNonDayOfMonthItem(
                    getInitialOfWeekDay(fixDayOfWeek(position)),
                    daysPaintResources.weekDaysInitialTextSize
                )
                if (isTalkBackEnabled) {
                    dayView.contentDescription = String.format(
                        context.getString(R.string.week_days_name_column),
                        getWeekDayName(fixDayOfWeek(position))
                    )
                }

                dayView.visibility = View.VISIBLE
            } else {
                if (position - 7 - fixedStartingDayOfWeek >= 0) {
                    val day = days[position - 7 - fixedStartingDayOfWeek]
                    val events = getEvents(day, monthEvents)
                    val isHoliday = isWeekEnd(
                        ((startingDayOfWeek + day - days[0]) % 7).toInt()
                    ) || events.any { it.isHoliday }

                    val isToday = day == todayJdn

                    dayView.setDayOfMonthItem(
                        isToday, originalPosition == selectedDay,
                        events.isNotEmpty(), hasDeviceEvents(events), isHoliday,
                        if (isArabicDigit)
                            daysPaintResources.arabicDigitsTextSize
                        else
                            daysPaintResources.persianDigitsTextSize,
                        day, position - 6 - fixedStartingDayOfWeek,
                        getShiftWorkTitle(day, true)
                    )

                    dayView.contentDescription = getA11yDaySummary(
                        context, day, isToday, emptyEventsStore(),
                        withZodiac = isToday, withOtherCalendars = false, withTitle = true
                    )

                    dayView.visibility = View.VISIBLE
                } else {
                    setEmpty()
                }
            }
        }

        private fun setEmpty() {
            itemView.visibility = View.GONE
        }
    }
}
