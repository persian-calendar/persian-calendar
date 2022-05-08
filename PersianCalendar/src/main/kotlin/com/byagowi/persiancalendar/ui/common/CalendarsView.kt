package com.byagowi.persiancalendar.ui.common

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.CalendarsViewBinding
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.isAstronomicalExtraFeaturesEnabled
import com.byagowi.persiancalendar.global.isForcedIranTimeEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.ui.utils.setupDefaultLayoutTransition
import com.byagowi.persiancalendar.ui.utils.setupExpandableAccessibilityDescription
import com.byagowi.persiancalendar.utils.EventsStore
import com.byagowi.persiancalendar.utils.calculateDaysDifference
import com.byagowi.persiancalendar.utils.formatDateAndTime
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getA11yDaySummary
import com.byagowi.persiancalendar.utils.getZodiacInfo
import com.byagowi.persiancalendar.utils.toJavaCalendar
import io.github.cosinekitty.astronomy.seasons
import java.util.*

class CalendarsView(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    private val binding = CalendarsViewBinding.inflate(context.layoutInflater, this, true).also {
        it.root.setOnClickListener { toggle() }
        it.root.setupDefaultLayoutTransition()
        it.root.setupExpandableAccessibilityDescription()
        it.extraInformationContainer.isVisible = false
        it.extraInformationContainer.setupDefaultLayoutTransition()
    }
    private var isExpanded = false

    fun toggle() {
        isExpanded = !isExpanded

        binding.expansionArrow
            .animateTo(if (isExpanded) ArrowView.Direction.UP else ArrowView.Direction.DOWN)
        TransitionManager.beginDelayedTransition(binding.root, ChangeBounds())

        binding.extraInformationContainer.isVisible = isExpanded
        binding.moonPhaseView.isVisible = isExpanded && isAstronomicalExtraFeaturesEnabled
        binding.seasonProgress.enableAnimation = isExpanded
        binding.monthProgress.enableAnimation = isExpanded
        binding.yearProgress.enableAnimation = isExpanded
    }

    fun hideMoreIcon() {
        binding.moonPhaseView.isVisible = false
    }

    fun showCalendars(
        jdn: Jdn, chosenCalendarType: CalendarType, calendarsToShow: List<CalendarType>
    ) {
        val context = context ?: return

        binding.calendarsFlow.update(calendarsToShow, jdn)
        binding.weekDayName.text = jdn.dayOfWeekName
        binding.moonPhaseView.jdn = jdn.value.toFloat()

        binding.zodiac.also {
            it.text = getZodiacInfo(context, jdn, withEmoji = true, short = false)
            it.isVisible = it.text.isNotEmpty()
        }

        val isToday = Jdn.today() == jdn
        if (isToday) {
            if (isForcedIranTimeEnabled) binding.weekDayName.text = language.inParentheses.format(
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

        val date = jdn.toCalendar(chosenCalendarType)
        val startOfYearJdn = Jdn(chosenCalendarType, date.year, 1, 1)
        val endOfYearJdn = Jdn(chosenCalendarType, date.year + 1, 1, 1) - 1
        val currentWeek = jdn.getWeekOfYear(startOfYearJdn)
        val weeksCount = endOfYearJdn.getWeekOfYear(startOfYearJdn)

        val startOfYearText = context.getString(
            R.string.start_of_year_diff, formatNumber(jdn - startOfYearJdn + 1),
            formatNumber(currentWeek), formatNumber(date.month)
        )
        val endOfYearText = context.getString(
            R.string.end_of_year_diff, formatNumber(endOfYearJdn - jdn),
            formatNumber(weeksCount - currentWeek), formatNumber(12 - date.month)
        )
        binding.startAndEndOfYearDiff.text =
            listOf(startOfYearText, endOfYearText).joinToString("\n")

        var equinox = ""
        if (mainCalendar == chosenCalendarType && chosenCalendarType == CalendarType.SHAMSI) {
            if (date.month == 12 && date.dayOfMonth >= 20 || date.month == 1 && date.dayOfMonth == 1) {
                val addition = if (date.month == 12) 1 else 0
                equinox = context.getString(
                    R.string.spring_equinox,
                    formatNumber(date.year + addition),
                    Date(
                        seasons(jdn.toGregorianCalendar().year).marchEquinox
                            .toMillisecondsSince1970()
                    ).toJavaCalendar().formatDateAndTime()
                )
            }
        }
        binding.equinox.also {
            it.text = equinox
            it.isVisible = equinox.isNotEmpty()
        }

        val (seasonPassedDays, seasonDaysCount) = jdn.calculatePersianSeasonPassedDaysAndCount()
        binding.seasonProgress.max = seasonDaysCount
        binding.seasonProgress.progress = seasonPassedDays
        binding.monthProgress.max = mainCalendar.getMonthLength(date.year, date.month)
        binding.monthProgress.progress = date.dayOfMonth
        binding.yearProgress.max = endOfYearJdn - startOfYearJdn
        binding.yearProgress.progress = jdn - startOfYearJdn

        // a11y
        binding.root.contentDescription = getA11yDaySummary(
            context, jdn, isToday, EventsStore.empty(),
            withZodiac = true, withOtherCalendars = true, withTitle = true
        )
    }
}
