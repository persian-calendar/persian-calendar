package com.byagowi.persiancalendar.ui.shared

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.CalendarsViewBinding
import com.byagowi.persiancalendar.praytimes.Clock
import com.byagowi.persiancalendar.utils.AstronomicalUtils
import com.byagowi.persiancalendar.utils.CalendarType
import com.byagowi.persiancalendar.utils.Utils
import io.github.persiancalendar.calendar.CivilDate
import java.util.*
import kotlin.math.abs

class CalendarsView : FrameLayout {

    private lateinit var mBinding: CalendarsViewBinding
    private var mCalendarsViewExpandListener: OnCalendarsViewExpandListener? = null
    private var mOnShowHideTodayButton: OnShowHideTodayButton? = null
    private val mCalendarItemAdapter by lazy { CalendarItemAdapter(context) }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    fun init(context: Context) {
        mBinding = CalendarsViewBinding.inflate(LayoutInflater.from(context), this,
                true)

        mBinding.root.setOnClickListener { expand(!mCalendarItemAdapter.isExpanded) }
        mBinding.extraInformationContainer.visibility = View.GONE

        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = RecyclerView.HORIZONTAL
        mBinding.calendarsRecyclerView.layoutManager = linearLayoutManager
        mBinding.calendarsRecyclerView.adapter = mCalendarItemAdapter
    }

    fun hideMoreIcon() {
        mBinding.moreCalendar.visibility = View.GONE
    }

    fun setOnCalendarsViewExpandListener(listener: OnCalendarsViewExpandListener) {
        mCalendarsViewExpandListener = listener
    }

    fun setOnShowHideTodayButton(listener: OnShowHideTodayButton) {
        mOnShowHideTodayButton = listener
    }

    fun expand(expanded: Boolean) {
        mCalendarItemAdapter.isExpanded = expanded

        mBinding.moreCalendar.setImageResource(if (expanded)
            R.drawable.ic_keyboard_arrow_up
        else
            R.drawable.ic_keyboard_arrow_down)
        mBinding.extraInformationContainer.visibility = if (expanded) View.VISIBLE else View.GONE

        mCalendarsViewExpandListener?.onCalendarsViewExpand()
    }

    fun showCalendars(jdn: Long,
                      chosenCalendarType: CalendarType,
                      calendarsToShow: List<CalendarType>) {
        val context = context ?: return

        mCalendarItemAdapter.setDate(calendarsToShow, jdn)
        mBinding.weekDayName.text = Utils.getWeekDayName(CivilDate(jdn))

        mBinding.zodiac.text = AstronomicalUtils.getZodiacInfo(context, jdn, true)
        mBinding.zodiac.visibility = if (TextUtils.isEmpty(mBinding.zodiac.text)) View.GONE else View.VISIBLE

        val diffDays = abs(Utils.getTodayJdn() - jdn)

        if (diffDays == 0L) {
            if (Utils.isIranTime()) {
                mBinding.weekDayName.text = String.format("%s (%s)",
                        mBinding.weekDayName.text,
                        context.getString(R.string.iran_time))
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
            var text = String.format(context.getString(R.string.date_diff_text),
                    Utils.formatNumber(diffDays.toInt()),
                    Utils.formatNumber(yearDiff),
                    Utils.formatNumber(monthDiff),
                    Utils.formatNumber(dayOfMonthDiff))
            if (diffDays <= 30) {
                text = text.split("\\(".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            }
            mBinding.diffDate.text = text
        }

        run {
            val mainDate = Utils.getDateFromJdnOfCalendar(chosenCalendarType, jdn)
            val startOfYear = Utils.getDateOfCalendar(chosenCalendarType,
                    mainDate.year, 1, 1)
            val startOfNextYear = Utils.getDateOfCalendar(
                    chosenCalendarType, mainDate.year + 1, 1, 1)
            val startOfYearJdn = startOfYear.toJdn()
            val endOfYearJdn = startOfNextYear.toJdn() - 1
            val currentWeek = Utils.calculateWeekOfYear(jdn, startOfYearJdn)
            val weeksCount = Utils.calculateWeekOfYear(endOfYearJdn, startOfYearJdn)

            val startOfYearText = String.format(context.getString(R.string.start_of_year_diff),
                    Utils.formatNumber((jdn - startOfYearJdn).toInt()),
                    Utils.formatNumber(currentWeek),
                    Utils.formatNumber(mainDate.month))
            val endOfYearText = String.format(context.getString(R.string.end_of_year_diff),
                    Utils.formatNumber((endOfYearJdn - jdn).toInt()),
                    Utils.formatNumber(weeksCount - currentWeek),
                    Utils.formatNumber(12 - mainDate.month))
            mBinding.startAndEndOfYearDiff.text = String.format("%s\n%s", startOfYearText, endOfYearText)

            var equinox = ""
            if (Utils.getMainCalendar() == chosenCalendarType && chosenCalendarType == CalendarType.SHAMSI) {
                if (mainDate.month == 12 && mainDate.dayOfMonth >= 20 || mainDate.month == 1 && mainDate.dayOfMonth == 1) {
                    val addition = if (mainDate.month == 12) 1 else 0
                    val springEquinox = Utils.getSpringEquinox(mainDate.toJdn())
                    equinox = String.format(context.getString(R.string.spring_equinox),
                            Utils.formatNumber(mainDate.year + addition),
                            Utils.getFormattedClock(
                                    Clock(springEquinox.get(Calendar.HOUR_OF_DAY),
                                            springEquinox.get(Calendar.MINUTE)), true))
                }
            }
            mBinding.equinox.text = equinox
            mBinding.equinox.visibility = if (TextUtils.isEmpty(equinox)) View.GONE else View.VISIBLE
        }

        mBinding.root.contentDescription = Utils.getA11yDaySummary(context, jdn,
                diffDays == 0L, null, true, true, true)
    }

    interface OnShowHideTodayButton {
        fun onShowHideTodayButton(show: Boolean)
    }

    interface OnCalendarsViewExpandListener {
        fun onCalendarsViewExpand()
    }
}
