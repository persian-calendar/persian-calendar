package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.doOnAttach
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.MonthPageBinding
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.common.ArrowView
import com.byagowi.persiancalendar.ui.utils.isLandscape
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.ui.utils.sp
import io.github.persiancalendar.calendar.AbstractDate
import java.lang.ref.WeakReference

class CalendarPager(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    // Public API
    var onDayClicked = fun(_: Jdn) {}
    var onDayLongClicked = fun(_: Jdn) {}

    // Selected month is visible current month of the pager, maybe a day is not selected on it yet
    var onMonthSelected = fun() {}
    private val baseJdn = Jdn.today()
    val selectedMonth: AbstractDate
        get() = mainCalendar.getMonthStartFromMonthsDistance(
            baseJdn, -applyOffset(viewPager.currentItem)
        )

    fun setSelectedDay(
        jdn: Jdn,
        highlight: Boolean = true,
        monthChange: Boolean = true,
        smoothScroll: Boolean = true
    ) {
        selectedJdn = if (highlight) jdn else null

        if (monthChange) {
            viewPager.setCurrentItem(
                applyOffset(position = -mainCalendar.getMonthsDistance(baseJdn, jdn)), smoothScroll
            )
        }

        refresh()
    }

    // Public API, to be reviewed
    fun refresh(isEventsModified: Boolean = false) = pagesViewHolders.mapNotNull { it.get() }
        .forEach { it.pageRefresh(isEventsModified, selectedJdn) }

    private val pagesViewHolders = ArrayList<WeakReference<PagerAdapter.ViewHolder>>()

    // Package API, to be rewritten with viewPager.adapter.notifyItemChanged()
    private fun addViewHolder(vh: PagerAdapter.ViewHolder) = pagesViewHolders.add(WeakReference(vh))

    private val monthsLimit = 5000 // this should be an even number

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
            MonthPageBinding.inflate(parent.context.layoutInflater, parent, false)
        )

        override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

        override fun getItemCount() = monthsLimit

        private val monthViewInitializer by lazy(LazyThreadSafetyMode.NONE) {
            val suitableHeight = (resources.displayMetrics.heightPixels / 2.2f).coerceIn(
                resources.getDimension(R.dimen.grid_calendar_height),
                resources.getDimension(R.dimen.grid_calendar_height_max)
            )

            if (resources.isLandscape) layoutParams.width =
                (resources.displayMetrics.widthPixels / 2.05f)
                    .coerceAtMost(resources.getDimension(R.dimen.grid_calendar_land_width_max))
                    .toInt()
            else layoutParams.height = suitableHeight.toInt()

            val cellHeight = suitableHeight / 7 - resources.sp(4.5f)
            val sharedDayViewData = SharedDayViewData(context, cellHeight)
            fun(binding: MonthPageBinding) {
                binding.monthView.initialize(sharedDayViewData, this@CalendarPager)

                binding.previous.layoutParams.height = (cellHeight / 1.4).toInt()
                binding.next.layoutParams.height = (cellHeight / 1.4).toInt()
            }
        }

        inner class ViewHolder(val binding: MonthPageBinding) :
            RecyclerView.ViewHolder(binding.root) {

            var pageRefresh = fun(_: Boolean, _: Jdn?) {}

            init {
                doOnAttach { monthViewInitializer(binding) }

                binding.previous.let {
                    it.contentDescription = it.context.getString(
                        R.string.previous_x, it.context.getString(R.string.month)
                    )
                    it.rotateTo(ArrowView.Direction.START)
                    it.setOnClickListener {
                        viewPager.setCurrentItem(viewPager.currentItem - 1, true)
                    }
                    it.setOnLongClickListener {
                        viewPager.setCurrentItem(viewPager.currentItem - 12, false)
                        true
                    }
                }

                binding.next.let {
                    it.contentDescription = it.context.getString(
                        R.string.next_x, it.context.getString(R.string.month)
                    )
                    it.rotateTo(ArrowView.Direction.END)
                    it.setOnClickListener {
                        viewPager.setCurrentItem(viewPager.currentItem + 1, true)
                    }
                    it.setOnLongClickListener {
                        viewPager.setCurrentItem(viewPager.currentItem + 12, false)
                        true
                    }
                }

                addViewHolder(this)
            }

            fun bind(position: Int) {
                val monthStartDate = mainCalendar.getMonthStartFromMonthsDistance(
                    baseJdn, -applyOffset(position)
                )
                val monthStartJdn = Jdn(monthStartDate)
                val monthLength =
                    mainCalendar.getMonthLength(monthStartDate.year, monthStartDate.month)
                binding.monthView.bind(monthStartJdn, monthStartDate)

                pageRefresh = { isEventsModification: Boolean, jdn: Jdn? ->
                    if (viewPager.currentItem == position) {
                        if (isEventsModification && jdn != null) {
                            binding.monthView.initializeMonthEvents()
                            onDayClicked(jdn)
                        } else {
                            onMonthSelected()
                        }
                    }
                    binding.monthView.selectDay(
                        if (jdn != null && jdn >= monthStartJdn && jdn - monthStartJdn + 1 <= monthLength) jdn - monthStartJdn + 1
                        else null
                    )
                }

                pageRefresh(false, selectedJdn)
            }
        }
    }
}
