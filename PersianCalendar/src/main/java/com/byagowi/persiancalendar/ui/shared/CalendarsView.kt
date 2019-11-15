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

    private var mCalendarsViewExpandListener: OnCalendarsViewExpandListener? = null
    private var mOnShowHideTodayButton: OnShowHideTodayButton? = null

    private val mCalendarItemAdapter = CalendarItemAdapter(context)
    private val mBinding: CalendarsViewBinding =
        CalendarsViewBinding.inflate(LayoutInflater.from(context), this, true).apply {
            root.setOnClickListener { expand(!mCalendarItemAdapter.isExpanded) }
            extraInformationContainer.visibility = View.GONE
            calendarsRecyclerView.layoutManager = LinearLayoutManager(context).apply {
                orientation = RecyclerView.HORIZONTAL
            }
            calendarsRecyclerView.adapter = mCalendarItemAdapter
        }

    fun hideMoreIcon() {
        mBinding.moreCalendar.visibility = View.GONE
    }

    fun setOnShowHideTodayButton(listener: (Boolean) -> Unit) {
        mOnShowHideTodayButton = object : OnShowHideTodayButton {
            override fun onShowHideTodayButton(show: Boolean) = listener(show)
        }
    }

    fun expand(expanded: Boolean) {
        mCalendarItemAdapter.isExpanded = expanded

        mBinding.moreCalendar.setImageResource(
            if (expanded)
                R.drawable.ic_keyboard_arrow_up
            else
                R.drawable.ic_keyboard_arrow_down
        )
        mBinding.extraInformationContainer.visibility = if (expanded) View.VISIBLE else View.GONE

        mCalendarsViewExpandListener?.onCalendarsViewExpand()
    }

    fun showCalendars(
        jdn: Long,
        chosenCalendarType: CalendarType,
        calendarsToShow: List<CalendarType>
    ) {
        val context = context ?: return

        mCalendarItemAdapter.setDate(calendarsToShow, jdn)
        mBinding.weekDayName.text = getWeekDayName(CivilDate(jdn))

        mBinding.zodiac.text = getZodiacInfo(context, jdn, true)
        mBinding.zodiac.visibility = if (mBinding.zodiac.text.isEmpty()) View.GONE else View.VISIBLE

        val diffDays = abs(getTodayJdn() - jdn)

        if (diffDays == 0L) {
            if (isIranTime) {
                mBinding.weekDayName.text = String.format(
                    "%s (%s)",
                    mBinding.weekDayName.text,
                    context.getString(R.string.iran_time)
                )
            }
            mOnShowHideTodayButton?.onShowHideTodayButton(false)
            mBinding.diffDate.visibility = View.GONE
        } else {
            mOnShowHideTodayButton?.onShowHideTodayButton(true)
            mBinding.diffDate.visibility = View.VISIBLE

            val civilBase = CivilDate(2000, 1, 1)
            val civilOffset = CivilDate(diffDays + civilBase.toJdn())
            val yearDiff = civilOffset.year - 2000
            val monthDiff = civilOffset.month - 1
            val dayOfMonthDiff = civilOffset.dayOfMonth - 1
            var text = String.format(
                context.getString(R.string.date_diff_text),
                formatNumber(diffDays.toInt()),
                formatNumber(yearDiff),
                formatNumber(monthDiff),
                formatNumber(dayOfMonthDiff)
            )
            if (diffDays <= 30) {
                text = text.split("(")[0]
            }
            mBinding.diffDate.text = text
        }

        run {
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
            mBinding.startAndEndOfYearDiff.text =
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
            mBinding.equinox.text = equinox
            mBinding.equinox.visibility = if (equinox.isEmpty()) View.GONE else View.VISIBLE
        }

        mBinding.root.contentDescription = getA11yDaySummary(
            context, jdn,
            diffDays == 0L, emptyMap(),
            withZodiac = true, withOtherCalendars = true, withTitle = true
        )
    }

    interface OnShowHideTodayButton {
        fun onShowHideTodayButton(show: Boolean)
    }

    interface OnCalendarsViewExpandListener {
        fun onCalendarsViewExpand()
    }
}
