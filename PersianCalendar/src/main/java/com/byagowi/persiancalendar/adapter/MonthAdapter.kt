package com.byagowi.persiancalendar.adapter

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entity.AbstractEvent
import com.byagowi.persiancalendar.entity.DayEntity
import com.byagowi.persiancalendar.entity.DeviceCalendarEvent
import com.byagowi.persiancalendar.util.Utils
import com.byagowi.persiancalendar.view.fragment.MonthFragment

class MonthAdapter(private val context: Context, private val monthFragment: MonthFragment, private val days: List<DayEntity>,
                   startingDayOfWeek: Int, private val weekOfYearStart: Int, private val weeksCount: Int) : RecyclerView.Adapter<MonthAdapter.ViewHolder>() {
  private val isArabicDigit: Boolean
  private val startingDayOfWeek: Int
  private val totalDays: Int

  @ColorInt
  private val colorHoliday: Int
  @ColorInt
  private val colorTextHoliday: Int
  @ColorInt
  private val colorTextDay: Int
  @ColorInt
  private val colorPrimary: Int
  @ColorInt
  private val colorDayName: Int
  @DrawableRes
  private val shapeSelectDay: Int

  private var selectedDay = -1

  init {
    this.startingDayOfWeek = Utils.fixDayOfWeekReverse(startingDayOfWeek)
    totalDays = days.size
    isArabicDigit = Utils.isArabicDigitSelected

    val theme = context.theme

    val colorHolidayAttr = TypedValue()
    theme.resolveAttribute(R.attr.colorHoliday, colorHolidayAttr, true)
    colorHoliday = ContextCompat.getColor(context, colorHolidayAttr.resourceId)

    val colorTextHolidayAttr = TypedValue()
    theme.resolveAttribute(R.attr.colorTextHoliday, colorTextHolidayAttr, true)
    colorTextHoliday = ContextCompat.getColor(context, colorTextHolidayAttr.resourceId)

    val colorTextDayAttr = TypedValue()
    theme.resolveAttribute(R.attr.colorTextDay, colorTextDayAttr, true)
    colorTextDay = ContextCompat.getColor(context, colorTextDayAttr.resourceId)

    val colorPrimaryAttr = TypedValue()
    theme.resolveAttribute(R.attr.colorPrimary, colorPrimaryAttr, true)
    colorPrimary = ContextCompat.getColor(context, colorPrimaryAttr.resourceId)

    val colorDayNameAttr = TypedValue()
    theme.resolveAttribute(R.attr.colorTextDayName, colorDayNameAttr, true)
    colorDayName = ContextCompat.getColor(context, colorDayNameAttr.resourceId)

    val shapeSelectDayAttr = TypedValue()
    theme.resolveAttribute(R.attr.circleSelect, shapeSelectDayAttr, true)
    shapeSelectDay = shapeSelectDayAttr.resourceId
  }

  fun selectDay(dayOfMonth: Int) {
    val prevDay = selectedDay
    selectedDay = -1
    notifyItemChanged(prevDay)

    if (dayOfMonth == -1) {
      return
    }

    selectedDay = dayOfMonth + 6 + startingDayOfWeek
    if (Utils.isWeekOfYearEnabled) {
      selectedDay = selectedDay + selectedDay / 7 + 1
    }

    notifyItemChanged(selectedDay)
  }

  inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
    var num: TextView
    var today: View
    var event: View
    var deviceEvent: View

    init {

      num = itemView.findViewById(R.id.num)
      today = itemView.findViewById(R.id.today)
      event = itemView.findViewById(R.id.event)
      deviceEvent = itemView.findViewById(R.id.and_device_event)

      itemView.setOnClickListener(this)
      itemView.setOnLongClickListener(this)
    }

    override fun onClick(v: View) {
      var position = adapterPosition
      if (Utils.isWeekOfYearEnabled) {
        if (position % 8 == 0) {
          return
        }

        position = fixForWeekOfYearNumber(position)
      }

      if (totalDays < position - 6 - startingDayOfWeek || position - 7 - startingDayOfWeek < 0) {
        return
      }

      monthFragment.onClickItem(days[position - 7 - startingDayOfWeek].jdn)
      this@MonthAdapter.selectDay(1 + position - 7 - startingDayOfWeek)
    }

    override fun onLongClick(v: View): Boolean {
      var position = adapterPosition
      if (Utils.isWeekOfYearEnabled) {
        if (position % 8 == 0) {
          return false
        }

        position = fixForWeekOfYearNumber(position)
      }

      if (totalDays < position - 6 - startingDayOfWeek || position - 7 - startingDayOfWeek < 0) {
        return false
      }

      monthFragment.onLongClickItem(days[position - 7 - startingDayOfWeek].jdn)
      onClick(v)

      return false
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthAdapter.ViewHolder {
    val v = LayoutInflater.from(context).inflate(R.layout.item_day, parent, false)

    return ViewHolder(v)
  }

  private fun hasAnyHolidays(dayEvents: List<AbstractEvent>): Boolean {
    for (event in dayEvents)
      if (event.isHoliday)
        return true
    return false
  }

  private fun hasDeviceEvents(dayEvents: List<AbstractEvent>): Boolean {
    for (event in dayEvents)
      if (event is DeviceCalendarEvent)
        return true
    return false
  }

  override fun onBindViewHolder(holder: MonthAdapter.ViewHolder, originalPosition: Int) {
    var position = originalPosition
    if (Utils.isWeekOfYearEnabled) {
      if (position % 8 == 0) {
        val row = position / 8
        if (row > 0 && row <= weeksCount) {
          holder.num.text = Utils.formatNumber(weekOfYearStart + row - 1)
          holder.num.setTextColor(colorDayName)
          holder.num.textSize = 12f
          holder.num.setBackgroundResource(0)
          holder.num.visibility = View.VISIBLE
          holder.today.visibility = View.GONE
          holder.event.visibility = View.GONE
          holder.deviceEvent.visibility = View.GONE
        } else
          setEmpty(holder)
        return
      }

      position = fixForWeekOfYearNumber(position)
    }

    if (totalDays < position - 6 - startingDayOfWeek) {
      setEmpty(holder)
    } else if (isPositionHeader(position)) {
      holder.num.text = Utils.getInitialOfWeekDay(Utils.fixDayOfWeek(position))
      holder.num.setTextColor(colorDayName)
      holder.num.textSize = 20f
      holder.today.visibility = View.GONE
      holder.num.setBackgroundResource(0)
      holder.event.visibility = View.GONE
      holder.deviceEvent.visibility = View.GONE
      holder.num.visibility = View.VISIBLE
    } else {
      if (position - 7 - startingDayOfWeek >= 0) {
        holder.num.text = Utils.formatNumber(1 + position - 7 - startingDayOfWeek)
        holder.num.visibility = View.VISIBLE

        val day = days[position - 7 - startingDayOfWeek]

        holder.num.textSize = (if (isArabicDigit) 20 else 25).toFloat()

        val events = Utils.getEvents(day.jdn)
        var isEvent = false
        var isHoliday = false
        if (Utils.isWeekEnd(day.dayOfWeek) || hasAnyHolidays(events)) {
          isHoliday = true
        }
        if (events.size > 0) {
          isEvent = true
        }

        holder.event.visibility = if (isEvent) View.VISIBLE else View.GONE
        holder.deviceEvent.visibility = if (hasDeviceEvents(events)) View.VISIBLE else View.GONE
        holder.today.visibility = if (day.today) View.VISIBLE else View.GONE

        if (originalPosition == selectedDay) {
          holder.num.setBackgroundResource(shapeSelectDay)
          holder.num.setTextColor(if (isHoliday) colorTextHoliday else colorPrimary)

        } else {
          holder.num.setBackgroundResource(0)
          holder.num.setTextColor(if (isHoliday) colorHoliday else colorTextDay)
        }

      } else {
        setEmpty(holder)
      }

    }
  }

  private fun setEmpty(holder: MonthAdapter.ViewHolder) {
    holder.today.visibility = View.GONE
    holder.num.visibility = View.GONE
    holder.event.visibility = View.GONE
    holder.deviceEvent.visibility = View.GONE
  }

  override fun getItemCount(): Int =
      7 * if (Utils.isWeekOfYearEnabled) 8 else 7 // days of week * month view rows

  private fun isPositionHeader(position: Int): Boolean = position < 7

  private fun fixForWeekOfYearNumber(position: Int): Int = position - position / 8 - 1
}