package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.DeviceCalendarEvent
import com.byagowi.persiancalendar.utils.*

class DaysAdapter internal constructor(
        private val context: Context, private val calendarPager: CalendarPager,
        private val selectableItemBackground: Int
) : RecyclerView.Adapter<DaysAdapter.ViewHolder>() {

    var days: List<Long> = emptyList()
    var startingDayOfWeek: Int = 0
    var weekOfYearStart: Int = 0
    var weeksCount: Int = 0

    private val dayViewLayoutParams: ViewGroup.LayoutParams = ViewGroup.LayoutParams(
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

        selectedDay = dayOfMonth + 6 + applyWeekStartOffsetToWeekDay(startingDayOfWeek)

        if (isShowWeekOfYearEnabled) selectedDay += selectedDay / 7 + 1

        notifyItemChanged(selectedDay)
    }

    private val typeface = getCalendarFragmentFont(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
            DayView(parent.context).apply {
                layoutParams = dayViewLayoutParams
                setTextTypeface(typeface)
            }
    )

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

        private val weekNumberTextSize =
                context.resources.getDimensionPixelSize(R.dimen.day_item_week_number_text_size)
        private val weekDaysInitialTextSize =
                context.resources.getDimensionPixelSize(R.dimen.day_item_week_days_initial_text_size)
        private val arabicDigitsTextSize =
                context.resources.getDimensionPixelSize(R.dimen.day_item_arabic_digits_text_size)
        private val persianDigitsTextSize =
                context.resources.getDimensionPixelSize(R.dimen.day_item_persian_digits_text_size)

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
                                weekNumberTextSize
                        )
                        dayView.contentDescription = if (isTalkBackEnabled)
                            context.getString(R.string.nth_week_of_year).format(weekNumber)
                        else weekNumber

                        dayView.visibility = View.VISIBLE
                    } else
                        setEmpty()
                    return
                }

                position = position - position / 8 - 1
            }

            val fixedStartingDayOfWeek = applyWeekStartOffsetToWeekDay(startingDayOfWeek)
            if (days.size < position - 6 - fixedStartingDayOfWeek) {
                setEmpty()
            } else if (position < 7) {
                val weekDayInitial = getInitialOfWeekDay(revertWeekStartOffsetFromWeekDay(position))
                dayView.setNonDayOfMonthItem(
                        getInitialOfWeekDay(revertWeekStartOffsetFromWeekDay(position)),
                        weekDaysInitialTextSize
                )

                dayView.contentDescription = if (isTalkBackEnabled)
                    context.getString(R.string.week_days_name_column)
                            .format(getWeekDayName(revertWeekStartOffsetFromWeekDay(position)))
                else weekDayInitial

                dayView.visibility = View.VISIBLE
                dayView.setBackgroundResource(0)
            } else {
                if (position - 7 - fixedStartingDayOfWeek >= 0) {
                    val day = days[position - 7 - fixedStartingDayOfWeek]
                    val events = getEvents(day, monthEvents)

                    val isToday = day == todayJdn

                    val dayOfMonth = position - 6 - fixedStartingDayOfWeek

                    dayView.setDayOfMonthItem(
                            isToday,
                            originalPosition == selectedDay,
                            events.isNotEmpty(),
                            events.any { it is DeviceCalendarEvent },
                            isWeekEnd(((startingDayOfWeek + day - days[0]) % 7).toInt()) || events.any { it.isHoliday },
                            if (isArabicDigit) arabicDigitsTextSize else persianDigitsTextSize,
                            day, dayOfMonth, getShiftWorkTitle(day, true)
                    )

                    dayView.contentDescription = if (isTalkBackEnabled) getA11yDaySummary(
                            context, day, isToday, emptyEventsStore(),
                            withZodiac = isToday, withOtherCalendars = false, withTitle = true
                    ) else dayOfMonth.toString()

                    dayView.visibility = View.VISIBLE
                    dayView.setBackgroundResource(selectableItemBackground)
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
