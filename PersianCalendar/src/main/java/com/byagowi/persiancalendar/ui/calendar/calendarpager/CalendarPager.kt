package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentMonthBinding
import com.byagowi.persiancalendar.utils.*
import io.github.persiancalendar.calendar.AbstractDate
import java.lang.ref.WeakReference
import java.util.*

class CalendarPager @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    // Public API
    var onDayClicked = fun(jdn: Long) {}
    var onDayLongClicked = fun(jdn: Long) {}

    // Selected month is visible current month of the pager, maybe a day is not selected on it yet
    var onMonthSelected = fun() {}
    val selectedMonth: AbstractDate
        get() = getDateFromOffset(mainCalendar, applyOffset(viewPager.currentItem))

    fun setSelectedDay(jdn: Long, highlight: Boolean = true, monthChange: Boolean = true) {
        selectedJdn = if (highlight) jdn else -1

        if (monthChange) {
            val today = getTodayOfCalendar(mainCalendar)
            val date = getDateFromJdnOfCalendar(mainCalendar, jdn)
            viewPager.setCurrentItem(
                    applyOffset((today.year - date.year) * 12 + today.month - date.month), true
            )
        }

        refresh()
    }

    // Public API, to be reviewed
    fun refresh(isEventsModified: Boolean = false) = pagesViewHolders.forEach {
        it.get()?.apply { refresh(isEventsModified, selectedJdn) }
    }

    private val pagesViewHolders = ArrayList<WeakReference<PagerAdapter.ViewHolder>>()

    // Package API, to be rewritten with viewPager.adapter.notifyItemChanged()
    fun addViewHolder(vh: PagerAdapter.ViewHolder) = pagesViewHolders.add(WeakReference(vh))

    private val monthsLimit = 5000 // this should be an even number

    private fun getDateFromOffset(calendar: CalendarType, offset: Int): AbstractDate {
        val date = getTodayOfCalendar(calendar)
        var month = date.month - offset
        month -= 1
        var year = date.year

        year += month / 12
        month %= 12
        if (month < 0) {
            year -= 1
            month += 12
        }
        month += 1
        return getDateOfCalendar(calendar, year, month, 1)
    }

    private fun applyOffset(position: Int) = monthsLimit / 2 - position

    private val viewPager = ViewPager2(context)
    private var selectedJdn: Long = -1

    init {
        viewPager.adapter = PagerAdapter()
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) = refresh()
        })
        addView(viewPager)
        viewPager.setCurrentItem(applyOffset(0), false)
    }

    inner class PagerAdapter : RecyclerView.Adapter<PagerAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
                FragmentMonthBinding.inflate(parent.context.layoutInflater, parent, false)
        )

        override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

        override fun getItemCount() = monthsLimit

        inner class ViewHolder(val binding: FragmentMonthBinding) :
                RecyclerView.ViewHolder(binding.root) {

            private val selectableItemBackground = TypedValue().also {
                context.theme.resolveAttribute(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                            android.R.attr.selectableItemBackgroundBorderless
                        else android.R.attr.selectableItemBackground,
                        it, true
                )
            }.resourceId

            private val daysAdapter = DaysAdapter(
                    binding.root.context, this@CalendarPager, selectableItemBackground
            )

            var refresh = fun(_: Boolean, _: Long) {}

            init {
                val isRTL = isRTL(binding.root.context)

                binding.next.apply {
                    setImageResource(
                            if (isRTL) R.drawable.ic_keyboard_arrow_left
                            else R.drawable.ic_keyboard_arrow_right
                    )
                    setOnClickListener { viewPager.setCurrentItem(viewPager.currentItem + 1, true) }
                    setBackgroundResource(selectableItemBackground)
                }

                binding.prev.apply {
                    setImageResource(
                            if (isRTL) R.drawable.ic_keyboard_arrow_right
                            else R.drawable.ic_keyboard_arrow_left
                    )
                    setOnClickListener { viewPager.setCurrentItem(viewPager.currentItem - 1, true) }
                    setBackgroundResource(selectableItemBackground)
                }

                binding.monthDays.apply {
                    setHasFixedSize(true)
                    layoutManager = GridLayoutManager(
                            binding.root.context, if (isShowWeekOfYearEnabled) 8 else 7
                    )
                }

                addViewHolder(this)

                binding.monthDays.adapter = daysAdapter
            }

            fun bind(position: Int) {
                val offset = applyOffset(position)
                val date = getDateFromOffset(mainCalendar, offset)
                val baseJdn = date.toJdn()
                val monthLength = getMonthLength(mainCalendar, date.year, date.month)
                val startOfYearJdn = getDateOfCalendar(mainCalendar, date.year, 1, 1).toJdn()

                daysAdapter.apply {
                    startingDayOfWeek = getDayOfWeekFromJdn(baseJdn)
                    weekOfYearStart = calculateWeekOfYear(baseJdn, startOfYearJdn)
                    weeksCount = calculateWeekOfYear(baseJdn + monthLength - 1, startOfYearJdn) -
                            weekOfYearStart + 1
                    days = (baseJdn until baseJdn + monthLength).toList()
                    initializeMonthEvents()
                    notifyItemRangeChanged(0, daysAdapter.itemCount)
                }

                refresh = fun(isEventsModification: Boolean, jdn: Long) {
                    if (viewPager.currentItem == position) {
                        if (isEventsModification) {
                            daysAdapter.initializeMonthEvents()
                            onDayClicked(jdn)
                        } else {
                            onMonthSelected()
                        }

                        val selectedDay = 1 + jdn - baseJdn
                        if (jdn != -1L && jdn >= baseJdn && selectedDay <= monthLength)
                            daysAdapter.selectDay(selectedDay.toInt())
                        else daysAdapter.selectDay(-1)
                    } else daysAdapter.selectDay(-1)
                }

                refresh()
            }
        }
    }
}