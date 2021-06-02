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
    var onDayClicked = fun(_: Jdn) {}
    var onDayLongClicked = fun(_: Jdn) {}

    // Selected month is visible current month of the pager, maybe a day is not selected on it yet
    var onMonthSelected = fun() {}
    val selectedMonth: AbstractDate
        get() = getDateFromOffset(mainCalendar, applyOffset(viewPager.currentItem))

    fun setSelectedDay(jdn: Jdn, highlight: Boolean = true, monthChange: Boolean = true) {
        selectedJdn = if (highlight) jdn else null

        if (monthChange) {
            val today = Jdn.today.toCalendar(mainCalendar)
            val date = jdn.toCalendar(mainCalendar)
            viewPager.setCurrentItem(
                applyOffset((today.year - date.year) * 12 + today.month - date.month), true
            )
        }

        refresh()
    }

    // Public API, to be reviewed
    fun refresh(isEventsModified: Boolean = false) = pagesViewHolders
        .mapNotNull { it.get() }.forEach { it.pageRefresh(isEventsModified, selectedJdn) }

    private val pagesViewHolders = ArrayList<WeakReference<PagerAdapter.ViewHolder>>()

    // Package API, to be rewritten with viewPager.adapter.notifyItemChanged()
    fun addViewHolder(vh: PagerAdapter.ViewHolder) = pagesViewHolders.add(WeakReference(vh))

    private val monthsLimit = 5000 // this should be an even number

    private fun getDateFromOffset(calendar: CalendarType, offset: Int): AbstractDate {
        val date = Jdn.today.toCalendar(calendar)
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
        return Jdn(calendar, year, month, 1).toCalendar(calendar)
    }

    private fun applyOffset(position: Int) = monthsLimit / 2 - position

    private val viewPager = ViewPager2(context)
    private var selectedJdn: Jdn? = null

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

            var pageRefresh = fun(_: Boolean, _: Jdn?) {}

            init {
                val isRTL = isRTL(binding.root.context)

                binding.next.let {
                    it.setImageResource(
                        if (isRTL) R.drawable.ic_keyboard_arrow_left
                        else R.drawable.ic_keyboard_arrow_right
                    )
                    it.setOnClickListener {
                        viewPager.setCurrentItem(viewPager.currentItem + 1, true)
                    }
                    it.setOnLongClickListener {
                        viewPager.setCurrentItem(viewPager.currentItem + 12, false)
                        true
                    }
                }

                binding.prev.let {
                    it.setImageResource(
                        if (isRTL) R.drawable.ic_keyboard_arrow_right
                        else R.drawable.ic_keyboard_arrow_left
                    )
                    it.setOnClickListener {
                        viewPager.setCurrentItem(viewPager.currentItem - 1, true)
                    }
                    it.setOnLongClickListener {
                        viewPager.setCurrentItem(viewPager.currentItem - 12, false)
                        true
                    }
                }

                binding.monthDays.let {
                    it.setHasFixedSize(true)
                    it.layoutManager = GridLayoutManager(
                        binding.root.context, if (isShowWeekOfYearEnabled) 8 else 7
                    )
                }

                addViewHolder(this)

                binding.monthDays.adapter = daysAdapter
            }

            fun bind(position: Int) {
                val offset = applyOffset(position)
                val date = getDateFromOffset(mainCalendar, offset)
                val baseJdn = Jdn(date)
                val monthLength = mainCalendar.getMonthLength(date.year, date.month)
                val startOfYearJdn = Jdn(mainCalendar, date.year, 1, 1)

                daysAdapter.let {
                    it.startingDayOfWeek = baseJdn.dayOfWeek
                    it.weekOfYearStart = baseJdn.getWeekOfYear(startOfYearJdn)
                    it.weeksCount = (baseJdn + monthLength - 1).getWeekOfYear(startOfYearJdn) -
                            it.weekOfYearStart + 1
                    it.days = baseJdn.createMonthDaysList(monthLength)
                    it.initializeMonthEvents()
                    it.notifyItemRangeChanged(0, daysAdapter.itemCount)
                }

                pageRefresh = fun(isEventsModification: Boolean, jdn: Jdn?) {
                    if (viewPager.currentItem == position) {
                        if (isEventsModification && jdn != null) {
                            daysAdapter.initializeMonthEvents()
                            onDayClicked(jdn)
                        } else {
                            onMonthSelected()
                        }

                        daysAdapter.selectDay(
                            if (jdn != null && jdn >= baseJdn && jdn - baseJdn + 1 <= monthLength)
                                jdn - baseJdn + 1
                            else -1
                        )
                    } else daysAdapter.selectDay(-1)
                }

                pageRefresh(false, null)
            }
        }
    }
}
