package com.byagowi.persiancalendar.ui.calendar.month

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView

import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.di.CalendarFragmentDependency
import com.byagowi.persiancalendar.di.MainActivityDependency
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.DayItem
import com.byagowi.persiancalendar.entities.DeviceCalendarEvent
import com.byagowi.persiancalendar.ui.calendar.CalendarFragmentModel
import com.byagowi.persiancalendar.utils.*

class MonthAdapter internal constructor(
    mainActivityDependency: MainActivityDependency,
    private val calendarFragmentDependency: CalendarFragmentDependency,
    private val days: List<DayItem>,
    startingDayOfWeek: Int, private val weekOfYearStart: Int,
    private val weeksCount: Int
) : RecyclerView.Adapter<MonthAdapter.ViewHolder>() {

    private val startingDayOfWeek: Int = fixDayOfWeekReverse(startingDayOfWeek)
    private val totalDays: Int = days.size
    private val layoutParams: ViewGroup.LayoutParams
    private val daysPaintResources: DaysPaintResources
    private var monthEvents: DeviceCalendarEventsStore = emptyEventsStore()
    private val isArabicDigit: Boolean
    private val context: Context
    private var selectedDay = -1

    init {
        this.context = mainActivityDependency.mainActivity
        initializeMonthEvents(context)
        isArabicDigit = isArabicDigitSelected()

        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            context.resources.getDimensionPixelSize(R.dimen.day_item_size)
        )
        this.daysPaintResources = calendarFragmentDependency.daysPaintResources
    }

    internal fun initializeMonthEvents(context: Context?) {
        if (isShowDeviceCalendarEvents && context != null)
            monthEvents = readMonthDeviceEvents(context, days[0].jdn)
    }

    internal fun selectDay(dayOfMonth: Int) {
        val prevDay = selectedDay
        selectedDay = -1
        notifyItemChanged(prevDay)

        if (dayOfMonth == -1) return

        selectedDay = dayOfMonth + 6 + startingDayOfWeek

        if (isShowWeekOfYearEnabled) selectedDay += selectedDay / 7 + 1

        notifyItemChanged(selectedDay)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemDayView(parent.context, daysPaintResources).also { it.layoutParams = layoutParams }
    )

    private fun hasDeviceEvents(dayEvents: List<CalendarEvent<*>>): Boolean =
        dayEvents.any { it is DeviceCalendarEvent }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

    override fun getItemCount(): Int =
        7 * if (isShowWeekOfYearEnabled) 8 else 7 // days of week * month view rows

    inner class ViewHolder(itemView: ItemDayView) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, View.OnLongClickListener {
        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(v: View) {
            val itemDayView = v as ItemDayView
            val jdn = itemDayView.jdn
            if (jdn == -1L) return

            ViewModelProviders
                .of(calendarFragmentDependency.calendarFragment)[CalendarFragmentModel::class.java]
                .selectDay(jdn)
            this@MonthAdapter.selectDay(itemDayView.dayOfMonth)
        }

        override fun onLongClick(v: View): Boolean {
            onClick(v)

            val itemDayView = v as ItemDayView
            val jdn = itemDayView.jdn
            if (jdn == -1L) return false

            calendarFragmentDependency.calendarFragment.addEventOnCalendar(jdn)
            return false
        }

        fun bind(position: Int) {
            var position = position
            val originalPosition = position
            val itemDayView = itemView as ItemDayView
            if (isShowWeekOfYearEnabled) {
                if (position % 8 == 0) {
                    val row = position / 8
                    if (row in 1..weeksCount) {
                        val weekNumber = formatNumber(weekOfYearStart + row - 1)
                        itemDayView.setNonDayOfMonthItem(
                            weekNumber,
                            daysPaintResources.weekNumberTextSize
                        )
                        if (isTalkBackEnabled) {
                            itemDayView.contentDescription = String.format(
                                context.getString(R.string.nth_week_of_year),
                                weekNumber
                            )
                        }

                        itemDayView.visibility = View.VISIBLE
                    } else
                        setEmpty()
                    return
                }

                position = position - position / 8 - 1
            }

            if (totalDays < position - 6 - startingDayOfWeek) {
                setEmpty()
            } else if (position < 7) {
                itemDayView.setNonDayOfMonthItem(
                    getInitialOfWeekDay(fixDayOfWeek(position)),
                    daysPaintResources.weekDaysInitialTextSize
                )
                if (isTalkBackEnabled) {
                    itemDayView.contentDescription = String.format(
                        context.getString(R.string.week_days_name_column),
                        getWeekDayName(fixDayOfWeek(position))
                    )
                }

                itemDayView.visibility = View.VISIBLE
            } else {
                if (position - 7 - startingDayOfWeek >= 0) {
                    val day = days[position - 7 - startingDayOfWeek]
                    val events = getEvents(day.jdn, monthEvents)
                    val isHoliday = isWeekEnd(day.dayOfWeek) || events.any { it.isHoliday }

                    itemDayView.setDayOfMonthItem(
                        day.isToday, originalPosition == selectedDay,
                        events.isNotEmpty(), hasDeviceEvents(events), isHoliday,
                        if (isArabicDigit)
                            daysPaintResources.arabicDigitsTextSize
                        else
                            daysPaintResources.persianDigitsTextSize,
                        day.jdn, position - 6 - startingDayOfWeek,
                        getShiftWorkTitle(day.jdn, true)
                    )

                    itemDayView.contentDescription = getA11yDaySummary(
                        context,
                        day.jdn, day.isToday, emptyEventsStore(),
                        day.isToday, withOtherCalendars = false, withTitle = true
                    )

                    itemDayView.visibility = View.VISIBLE
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
