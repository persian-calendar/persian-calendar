package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.DeviceCalendarEventsStore
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.eventsRepository
import com.byagowi.persiancalendar.global.isShowDeviceCalendarEvents
import com.byagowi.persiancalendar.global.isShowWeekOfYearEnabled
import com.byagowi.persiancalendar.global.isTalkBackEnabled
import com.byagowi.persiancalendar.utils.applyWeekStartOffsetToWeekDay
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getA11yDaySummary
import com.byagowi.persiancalendar.utils.getInitialOfWeekDay
import com.byagowi.persiancalendar.utils.getShiftWorkTitle
import com.byagowi.persiancalendar.utils.getWeekDayName
import com.byagowi.persiancalendar.utils.readMonthDeviceEvents
import com.byagowi.persiancalendar.utils.revertWeekStartOffsetFromWeekDay
import com.byagowi.persiancalendar.variants.debugAssertNotNull

class DaysAdapter(
    private val context: Context, val sharedDayViewData: SharedDayViewData,
    private val calendarPager: CalendarPager?
) : RecyclerView.Adapter<DaysAdapter.ViewHolder>() {

    var days = emptyList<Jdn>()
    var startingDayOfWeek: Int = 0
    var weekOfYearStart: Int = 0
    var weeksCount: Int = 0

    private var monthDeviceEvents: DeviceCalendarEventsStore = EventsStore.empty()
    private var selectedDayPosition: Int? = null

    fun initializeMonthEvents() {
        if (isShowDeviceCalendarEvents) monthDeviceEvents = context.readMonthDeviceEvents(days[0])
    }

    // Also returns recycler view position of the selected day view to use in the selection indicator
    internal fun selectDayInternal(dayOfMonth: Int?): Int? {
        selectedDayPosition?.let(::notifyItemChanged)
        if (dayOfMonth == null) {
            selectedDayPosition = null
            return null
        }

        var selectedDayPosition = dayOfMonth + 6 + applyWeekStartOffsetToWeekDay(startingDayOfWeek)
        if (isShowWeekOfYearEnabled) selectedDayPosition += selectedDayPosition / 7 + 1

        notifyItemChanged(selectedDayPosition)
        this.selectedDayPosition = selectedDayPosition
        return selectedDayPosition
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        DayView(parent.context).also {
            it.layoutParams = sharedDayViewData.layoutParams
            it.sharedDayViewData = sharedDayViewData
        }
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

    // days of week * month view rows
    override fun getItemCount(): Int = 7 * if (isShowWeekOfYearEnabled) 8 else 7

    private val todayJdn = Jdn.today()

    inner class ViewHolder(itemView: DayView) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, View.OnLongClickListener {

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(v: View) {
            val itemDayView = (v as? DayView).debugAssertNotNull ?: return
            val jdn = itemDayView.jdn ?: return
            calendarPager?.let { it.onDayClicked(jdn) }
        }

        override fun onLongClick(v: View): Boolean {
            onClick(v)
            val jdn = (v as? DayView).debugAssertNotNull?.jdn ?: return false
            calendarPager?.let { it.onDayLongClicked(jdn) }
            return false
        }

        fun bind(pos: Int) {
            var position = pos
            val dayView = (itemView as? DayView).debugAssertNotNull ?: return
            if (isShowWeekOfYearEnabled) {
                if (position % 8 == 0) {
                    val row = position / 8
                    if (row in 1..weeksCount) {
                        val weekNumber = formatNumber(weekOfYearStart + row - 1)
                        dayView.setWeekNumber(weekNumber)
                        dayView.contentDescription = if (isTalkBackEnabled)
                            context.getString(R.string.nth_week_of_year, weekNumber)
                        else weekNumber

                        dayView.isVisible = true
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
                val weekDayPosition = revertWeekStartOffsetFromWeekDay(position)
                dayView.setInitialOfWeekDay(getInitialOfWeekDay(weekDayPosition))
                dayView.contentDescription = context
                    .getString(R.string.week_days_name_column, getWeekDayName(weekDayPosition))

                dayView.isVisible = true
                dayView.setBackgroundResource(0)
            } else {
                if (position - 7 - fixedStartingDayOfWeek >= 0) {
                    val day = days[position - 7 - fixedStartingDayOfWeek]
                    val events = eventsRepository?.getEvents(day, monthDeviceEvents) ?: emptyList()

                    val isToday = day == todayJdn

                    val dayOfMonth = position - 6 - fixedStartingDayOfWeek
                    dayView.setDayOfMonthItem(
                        isToday, pos == selectedDayPosition,
                        events.any { it !is CalendarEvent.DeviceCalendarEvent },
                        events.any { it is CalendarEvent.DeviceCalendarEvent },
                        events.any { it.isHoliday }, day, dayOfMonth, getShiftWorkTitle(day, true)
                    )

                    dayView.contentDescription = if (isTalkBackEnabled) getA11yDaySummary(
                        context, day, isToday, EventsStore.empty(),
                        withZodiac = isToday, withOtherCalendars = false, withTitle = true
                    ) else dayOfMonth.toString()

                    dayView.isVisible = true
                } else {
                    setEmpty()
                }
            }
        }

        private fun setEmpty() {
            itemView.isVisible = false
        }
    }
}
