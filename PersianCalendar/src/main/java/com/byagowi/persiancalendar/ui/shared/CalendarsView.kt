package com.byagowi.persiancalendar.ui.shared

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.CalendarsViewBinding
import com.byagowi.persiancalendar.utils.*
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.praytimes.Clock
import java.util.*
import kotlin.math.abs

class CalendarsView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {

    private val calendarItemAdapter = CalendarItemAdapter(context)
    private val binding: CalendarsViewBinding =
        CalendarsViewBinding.inflate(LayoutInflater.from(context), this, true).apply {
            root.setOnClickListener { expand(!calendarItemAdapter.isExpanded) }
            extraInformationContainer.visibility = View.GONE
            calendarsRecyclerView.layoutManager = LinearLayoutManager(context).apply {
                orientation = RecyclerView.HORIZONTAL
            }
            calendarsRecyclerView.adapter = calendarItemAdapter
        }

    fun hideMoreIcon() {
        binding.moreCalendar.visibility = View.GONE
    }

    fun expand(expanded: Boolean) {
        calendarItemAdapter.isExpanded = expanded

        binding.moreCalendar.setImageResource(
            if (expanded)
                R.drawable.ic_keyboard_arrow_up
            else
                R.drawable.ic_keyboard_arrow_down
        )
        binding.extraInformationContainer.visibility = if (expanded) View.VISIBLE else View.GONE
    }

    fun showCalendars(
        jdn: Long, chosenCalendarType: CalendarType, calendarsToShow: List<CalendarType>
    ) {
        val context = context ?: return

        calendarItemAdapter.setDate(calendarsToShow, jdn)
        binding.weekDayName.text = getWeekDayName(CivilDate(jdn))

        binding.zodiac.text = getZodiacInfo(context, jdn, true)
        binding.zodiac.visibility = if (binding.zodiac.text.isEmpty()) View.GONE else View.VISIBLE

        val selectedDayAbsoluteDistance = abs(getTodayJdn() - jdn)

        if (selectedDayAbsoluteDistance == 0L) {
            if (isForcedIranTimeEnabled) binding.weekDayName.text = String.format(
                "%s (%s)",
                getWeekDayName(CivilDate(jdn)),
                context.getString(R.string.iran_time)
            )
            binding.diffDate.visibility = View.GONE
        } else {
            binding.diffDate.visibility = View.VISIBLE

            val civilBase = CivilDate(2000, 1, 1)
            val civilOffset = CivilDate(civilBase.toJdn() + selectedDayAbsoluteDistance)
            val yearDiff = civilOffset.year - 2000
            val monthDiff = civilOffset.month - 1
            val dayOfMonthDiff = civilOffset.dayOfMonth - 1
            var text = String.format(
                context.getString(R.string.date_diff_text),
                formatNumber(selectedDayAbsoluteDistance.toInt()),
                formatNumber(yearDiff),
                formatNumber(monthDiff),
                formatNumber(dayOfMonthDiff)
            )
            if (selectedDayAbsoluteDistance <= 31) text = text.split(" (")[0]
            binding.diffDate.text = text
        }

        val mainDate = getDateFromJdnOfCalendar(chosenCalendarType, jdn)
        val startOfYear = getDateOfCalendar(
            chosenCalendarType,
            mainDate.year, 1, 1
        )
        val startOfNextYear = getDateOfCalendar(
            chosenCalendarType, mainDate.year + 1, 1, 1
        )
        val startOfYearJdn = startOfYear.toJdn()
        val endOfYearJdn = startOfNextYear.toJdn() - 1
        val currentWeek = calculateWeekOfYear(jdn, startOfYearJdn)
        val weeksCount = calculateWeekOfYear(endOfYearJdn, startOfYearJdn)

        val startOfYearText = String.format(
            context.getString(R.string.start_of_year_diff),
            formatNumber((jdn - startOfYearJdn).toInt()),
            formatNumber(currentWeek),
            formatNumber(mainDate.month)
        )
        val endOfYearText = String.format(
            context.getString(R.string.end_of_year_diff),
            formatNumber((endOfYearJdn - jdn).toInt()),
            formatNumber(weeksCount - currentWeek),
            formatNumber(12 - mainDate.month)
        )
        binding.startAndEndOfYearDiff.text =
            String.format("%s\n%s", startOfYearText, endOfYearText)

        var equinox = ""
        if (mainCalendar == chosenCalendarType && chosenCalendarType == CalendarType.SHAMSI) {
            if (mainDate.month == 12 && mainDate.dayOfMonth >= 20 || mainDate.month == 1 && mainDate.dayOfMonth == 1) {
                val addition = if (mainDate.month == 12) 1 else 0
                val springEquinox = getSpringEquinox(mainDate.toJdn())
                equinox = String.format(
                    context.getString(R.string.spring_equinox),
                    formatNumber(mainDate.year + addition),
                    getFormattedClock(
                        Clock(
                            springEquinox[Calendar.HOUR_OF_DAY],
                            springEquinox[Calendar.MINUTE]
                        ), true
                    )
                )
            }
        }
        binding.equinox.text = equinox
        binding.equinox.visibility = if (equinox.isEmpty()) View.GONE else View.VISIBLE

        binding.root.contentDescription = getA11yDaySummary(
            context, jdn,
            selectedDayAbsoluteDistance == 0L, emptyEventsStore(),
            withZodiac = true, withOtherCalendars = true, withTitle = true
        )
    }
}
