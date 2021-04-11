package com.byagowi.persiancalendar.ui.shared

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.CalendarsViewBinding
import com.byagowi.persiancalendar.utils.*
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.praytimes.Clock
import java.util.*

class CalendarsView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {

    private val changeBoundTransition = ChangeBounds()
    private val arrowRotationAnimationDuration =
        resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
    private val binding = CalendarsViewBinding.inflate(context.layoutInflater, this, true).apply {
        root.setOnClickListener { toggle() }
        extraInformationContainer.visibility = View.GONE
    }
    private var isExpanded = false

    fun toggle() {
        isExpanded = !isExpanded

        binding.moreCalendar.contentDescription = context.getString(
            if (isExpanded) R.string.close else R.string.open)

        // Rotate expansion arrow
        binding.moreCalendar.animate()
            .rotation(if (isExpanded) 180f else 0f)
            .setDuration(arrowRotationAnimationDuration)
            .start()

        TransitionManager.beginDelayedTransition(binding.calendarsTabContent, changeBoundTransition)
        binding.extraInformationContainer.visibility = if (isExpanded) View.VISIBLE else View.GONE
    }

    fun hideMoreIcon() {
        binding.moreCalendar.visibility = View.GONE
    }

    fun showCalendars(
        jdn: Long, chosenCalendarType: CalendarType, calendarsToShow: List<CalendarType>
    ) {
        val context = context ?: return

        binding.calendarsFlow.update(binding.calendars, calendarsToShow, jdn)
        binding.weekDayName.text = getWeekDayName(CivilDate(jdn))

        binding.zodiac.also {
            it.text = getZodiacInfo(context, jdn, withEmoji = true, short = false)
            it.visibility = if (it.text.isEmpty()) View.GONE else View.VISIBLE
        }

        val isToday = getTodayJdn() == jdn
        if (isToday) {
            if (isForcedIranTimeEnabled) binding.weekDayName.text = "%s (%s)".format(
                getWeekDayName(CivilDate(jdn)),
                context.getString(R.string.iran_time)
            )
            binding.diffDate.visibility = View.GONE
        } else {
            binding.also {
                it.diffDate.visibility = View.VISIBLE
                it.diffDate.text =
                    calculateDaysDifference(jdn, context.getString(R.string.date_diff_text))
            }
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

        val startOfYearText = context.getString(R.string.start_of_year_diff).format(
            formatNumber((jdn - startOfYearJdn + 1).toInt()),
            formatNumber(currentWeek),
            formatNumber(mainDate.month)
        )
        val endOfYearText = context.getString(R.string.end_of_year_diff).format(
            formatNumber((endOfYearJdn - jdn).toInt()),
            formatNumber(weeksCount - currentWeek),
            formatNumber(12 - mainDate.month)
        )
        binding.startAndEndOfYearDiff.text =
            listOf(startOfYearText, endOfYearText).joinToString("\n")

        var equinox = ""
        if (mainCalendar == chosenCalendarType && chosenCalendarType == CalendarType.SHAMSI) {
            if (mainDate.month == 12 && mainDate.dayOfMonth >= 20 || mainDate.month == 1 && mainDate.dayOfMonth == 1) {
                val addition = if (mainDate.month == 12) 1 else 0
                val springEquinox = getSpringEquinox(mainDate.toJdn())
                equinox = context.getString(R.string.spring_equinox).format(
                    formatNumber(mainDate.year + addition),
                    Clock(springEquinox[Calendar.HOUR_OF_DAY], springEquinox[Calendar.MINUTE])
                        .toFormattedString(forcedIn12 = true) + " " +
                            formatDate(
                                getDateFromJdnOfCalendar(
                                    mainCalendar,
                                    calendarToCivilDate(springEquinox).toJdn()
                                ),
                                forceNonNumerical = true
                            )
                )
            }
        }
        binding.equinox.also {
            it.text = equinox
            it.visibility = if (equinox.isEmpty()) View.GONE else View.VISIBLE
        }

        binding.root.contentDescription = getA11yDaySummary(
            context, jdn, isToday, emptyEventsStore(),
            withZodiac = true, withOtherCalendars = true, withTitle = true
        )
    }
}
