package com.byagowi.persiancalendar.view.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.byagowi.persiancalendar.Constants
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.adapter.MonthAdapter
import com.byagowi.persiancalendar.entity.DayEntity
import com.byagowi.persiancalendar.util.CalendarUtils
import com.byagowi.persiancalendar.util.UIUtils
import com.byagowi.persiancalendar.util.Utils

import java.util.ArrayList

import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import calendar.AbstractDate

class MonthFragment : Fragment(), View.OnClickListener {
  private var calendarFragment: CalendarFragment? = null
  private lateinit var typedDate: AbstractDate
  private var offset: Int = 0

  private var adapter: MonthAdapter? = null
  private var baseJdn: Long = 0
  private var monthLength: Int = 0

  private val setCurrentMonthReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      val extras = intent.extras ?: return

      adapter?.selectDay(-1)

      val value = extras.getInt(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT)
      if (value == offset) {
        updateTitle()

        val jdn = extras.getLong(Constants.BROADCAST_FIELD_SELECT_DAY_JDN)
        val selectedDay = 1 + jdn - baseJdn
        if (jdn != -1L && jdn >= baseJdn && selectedDay <= monthLength) {
          adapter?.selectDay((1 + jdn - baseJdn).toInt())
        }
      }
    }
  }

  private fun calculateWeekOfYear(jdn: Long, startOfYear: Long): Int {
    val dayOfYear = jdn - startOfYear
    return Math.ceil(1 + (dayOfYear - Utils.fixDayOfWeekReverse(CalendarUtils.getDayOfWeekFromJdn(jdn))) / 7.0).toInt()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    val ctx = context ?: return null
    val view = inflater.inflate(R.layout.fragment_month, container, false)
    isRTL = UIUtils.isRTL(ctx)

    offset = arguments?.getInt(Constants.OFFSET_ARGUMENT) ?: 0

    val prev = view.findViewById<AppCompatImageView>(R.id.prev)
    val next = view.findViewById<AppCompatImageView>(R.id.next)
    if (isRTL) {
      prev.setImageResource(R.drawable.ic_keyboard_arrow_right)
      next.setImageResource(R.drawable.ic_keyboard_arrow_left)
    } else {
      prev.setImageResource(R.drawable.ic_keyboard_arrow_left)
      next.setImageResource(R.drawable.ic_keyboard_arrow_right)
    }
    prev.setOnClickListener(this)
    next.setOnClickListener(this)

    val recyclerView = view.findViewById<RecyclerView>(R.id.RecyclerView)
    recyclerView.setHasFixedSize(true)

    recyclerView.layoutManager =
        GridLayoutManager(context, if (Utils.isWeekOfYearEnabled) 8 else 7)
    //////////////////
    //////////////////
    val mainCalendar = Utils.mainCalendar
    val days = ArrayList<DayEntity>()
    typedDate = CalendarUtils.getTodayOfCalendar(mainCalendar)
    var month = typedDate.month - offset
    month -= 1
    var year = typedDate.year

    year = year + month / 12
    month = month % 12
    if (month < 0) {
      year -= 1
      month += 12
    }
    month += 1
    typedDate = CalendarUtils.getDateOfCalendar(mainCalendar, year, month, 1)

    baseJdn = CalendarUtils.getJdnDate(typedDate)
    monthLength = (CalendarUtils.getJdnOfCalendar(mainCalendar, if (month == 12) year + 1 else year,
        if (month == 12) 1 else month + 1, 1) - baseJdn).toInt()

    var dayOfWeek = CalendarUtils.getDayOfWeekFromJdn(baseJdn)

    val todayJdn = CalendarUtils.todayJdn
    for (i in 0 until monthLength) {
      val jdn = baseJdn + i
      days.add(DayEntity(jdn, jdn == todayJdn, dayOfWeek))
      dayOfWeek++
      if (dayOfWeek == 7) {
        dayOfWeek = 0
      }
    }

    val startOfYearJdn = CalendarUtils.getJdnOfCalendar(mainCalendar, year, 1, 1)
    val weekOfYearStart = calculateWeekOfYear(baseJdn, startOfYearJdn)
    val weeksCount = 1 + calculateWeekOfYear(baseJdn + monthLength - 1, startOfYearJdn) - weekOfYearStart

    val startingDayOfWeek = CalendarUtils.getDayOfWeekFromJdn(baseJdn)
    //////////////////
    //////////////////
    adapter = MonthAdapter(ctx, this, days, startingDayOfWeek, weekOfYearStart, weeksCount)
    recyclerView.adapter = adapter
    recyclerView.itemAnimator = null

    calendarFragment = activity?.supportFragmentManager
        ?.findFragmentByTag(CalendarFragment::class.java.name) as CalendarFragment?

    val calendar = calendarFragment
    if (calendar != null) {
      if (calendar.firstTime && offset == 0 &&
          calendar.viewPagerPosition == offset) {
        calendar.firstTime = false
        calendar.selectDay(CalendarUtils.todayJdn)
        updateTitle()
      }
    }

    LocalBroadcastManager.getInstance(ctx).registerReceiver(setCurrentMonthReceiver,
        IntentFilter(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT))

    return view
  }

  override fun onDestroy() {
    val ctx = context
    if (ctx != null) {
      LocalBroadcastManager.getInstance(ctx).unregisterReceiver(setCurrentMonthReceiver)
    }
    super.onDestroy()
  }

  fun onClickItem(jdn: Long) = calendarFragment?.selectDay(jdn)

  fun onLongClickItem(jdn: Long) = calendarFragment?.addEventOnCalendar(jdn)

  override fun onClick(v: View) {
    when (v.id) {
      R.id.next -> calendarFragment?.changeMonth(if (isRTL) -1 else 1)

      R.id.prev -> calendarFragment?.changeMonth(if (isRTL) 1 else -1)
    }
  }

  private fun updateTitle() {
    val localActivity = activity ?: return
    UIUtils.setActivityTitleAndSubtitle(localActivity, CalendarUtils.getMonthName(typedDate),
        Utils.formatNumber(typedDate.year))
  }

  companion object {
    internal var isRTL = false
  }
}
