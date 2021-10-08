package com.byagowi.persiancalendar.ui.shared

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.CalendarsViewBinding
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.ui.utils.setupExpandableAccessibilityDescription
import com.byagowi.persiancalendar.utils.EventsStore
import com.byagowi.persiancalendar.utils.calculateDaysDifference
import com.byagowi.persiancalendar.utils.dayOfWeekName
import com.byagowi.persiancalendar.utils.formatDate
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getA11yDaySummary
import com.byagowi.persiancalendar.utils.getSpringEquinox
import com.byagowi.persiancalendar.utils.getWeekOfYear
import com.byagowi.persiancalendar.utils.getZodiacInfo
import com.byagowi.persiancalendar.utils.isForcedIranTimeEnabled
import com.byagowi.persiancalendar.utils.mainCalendar
import com.byagowi.persiancalendar.utils.spacedColon
import com.byagowi.persiancalendar.utils.toCivilDate
import java.util.*

class CalendarsView(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    private val binding = CalendarsViewBinding.inflate(context.layoutInflater, this, true).also {
        it.root.setOnClickListener { toggle() }
        it.root.setupExpandableAccessibilityDescription()
        it.extraInformationContainer.isVisible = false
    }
    private var isExpanded = false

    fun toggle() {
        isExpanded = !isExpanded

        binding.expansionArrow
            .animateTo(if (isExpanded) ArrowView.Direction.UP else ArrowView.Direction.DOWN)
        TransitionManager.beginDelayedTransition(binding.calendarsTabContent, ChangeBounds())

        binding.extraInformationContainer.isVisible = isExpanded
    }

    fun hideMoreIcon() {
        binding.expansionArrow.isVisible = false
    }

    fun showCalendars(
        jdn: Jdn, chosenCalendarType: CalendarType, calendarsToShow: List<CalendarType>
    ) {
        val context = context ?: return

        binding.calendarsFlow.update(calendarsToShow, jdn)
        binding.weekDayName.text = jdn.dayOfWeekName

        binding.zodiac.also {
            it.text = getZodiacInfo(context, jdn, withEmoji = true, short = false)
            it.isVisible = it.text.isNotEmpty()
        }

        val isToday = Jdn.today == jdn
        if (isToday) {
            if (isForcedIranTimeEnabled) binding.weekDayName.text = "%s (%s)".format(
                jdn.dayOfWeekName, context.getString(R.string.iran_time)
            )
            binding.diffDate.isVisible = false
        } else {
            binding.also {
                it.diffDate.isVisible = true
                it.diffDate.text = listOf(
                    context.getString(R.string.days_distance), spacedColon,
                    calculateDaysDifference(resources, jdn)
                ).joinToString("")
            }
        }

        val mainDate = jdn.toCalendar(chosenCalendarType)
        val startOfYearJdn = Jdn(chosenCalendarType, mainDate.year, 1, 1)
        val endOfYearJdn = Jdn(chosenCalendarType, mainDate.year + 1, 1, 1) - 1
        val currentWeek = jdn.getWeekOfYear(startOfYearJdn)
        val weeksCount = endOfYearJdn.getWeekOfYear(startOfYearJdn)

        val startOfYearText = context.getString(
            R.string.start_of_year_diff, formatNumber(jdn - startOfYearJdn + 1),
            formatNumber(currentWeek), formatNumber(mainDate.month)
        )
        val endOfYearText = context.getString(
            R.string.end_of_year_diff, formatNumber(endOfYearJdn - jdn),
            formatNumber(weeksCount - currentWeek), formatNumber(12 - mainDate.month)
        )
        binding.startAndEndOfYearDiff.text =
            listOf(startOfYearText, endOfYearText).joinToString("\n")

        var equinox = ""
        if (mainCalendar == chosenCalendarType && chosenCalendarType == CalendarType.SHAMSI) {
            if (mainDate.month == 12 && mainDate.dayOfMonth >= 20 || mainDate.month == 1 && mainDate.dayOfMonth == 1) {
                val addition = if (mainDate.month == 12) 1 else 0
                val springEquinox = jdn.toGregorianCalendar().getSpringEquinox()
                equinox = context.getString(
                    R.string.spring_equinox,
                    formatNumber(mainDate.year + addition),
                    Clock(springEquinox[Calendar.HOUR_OF_DAY], springEquinox[Calendar.MINUTE])
                        .toFormattedString(forcedIn12 = true) + " " +
                            formatDate(
                                Jdn(springEquinox.toCivilDate()).toCalendar(mainCalendar),
                                forceNonNumerical = true
                            )
                )
            }
        }
        binding.equinox.also {
            it.text = equinox
            it.isVisible = equinox.isNotEmpty()
        }

        binding.root.contentDescription = getA11yDaySummary(
            context, jdn, isToday, EventsStore.empty(),
            withZodiac = true, withOtherCalendars = true, withTitle = true
        )
    }
}
