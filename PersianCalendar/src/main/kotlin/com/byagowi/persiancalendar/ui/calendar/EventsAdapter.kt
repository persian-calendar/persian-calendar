package com.byagowi.persiancalendar.ui.calendar

import android.graphics.Color
import android.os.Build
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.EventItemBinding
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.global.holidayString
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.utils.formatTitle
import com.byagowi.persiancalendar.utils.isRtl
import com.byagowi.persiancalendar.utils.logException
import kotlin.math.min

class EventsAdapter(private val onEventClick: (Int) -> Unit) :
    RecyclerView.Adapter<EventsAdapter.EventViewHolder>() {
    fun showEvents(list: List<CalendarEvent<*>>) {
        val previousEventsCount = events.size
        events = list.sortedBy {
            when {
                it.isHoliday -> 0L
                it !is CalendarEvent.DeviceCalendarEvent -> 1L
                else -> it.start.time
            }
        }
        notifyItemRangeChanged(0, min(list.size, previousEventsCount))
        if (previousEventsCount > list.size) {
            notifyItemRangeRemoved(list.size, previousEventsCount - list.size)
        } else {
            notifyItemRangeInserted(list.size, list.size - previousEventsCount)
        }
    }

    private var events = emptyList<CalendarEvent<*>>()
    override fun getItemCount(): Int = events.size
    override fun onBindViewHolder(holder: EventViewHolder, position: Int) = holder.bind(position)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder =
        EventViewHolder(EventItemBinding.inflate(parent.context.layoutInflater, parent, false))

    inner class EventViewHolder(val binding: EventItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private fun TextView.putLineEndIcon(@DrawableRes icon: Int, @ColorInt color: Int) {
            val drawable = if (icon == 0) null else context.getCompatDrawable(icon)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) drawable?.setTint(color)
            if (resources.isRtl) setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
            else setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
        }

        fun bind(position: Int) {
            val event = events[position]

            val context = binding.root.context
            val backgroundColor = if (event is CalendarEvent.DeviceCalendarEvent) {
                runCatching {
                    // should be turned to long then int otherwise gets stupid alpha
                    if (event.color.isEmpty()) null else event.color.toLong().toInt()
                }.onFailure(logException).getOrNull()
                    ?: context.resolveColor(com.google.android.material.R.attr.colorAccent)
            } else null
            val attr =
                if (event.isHoliday) R.attr.colorTextHoliday
                else com.google.android.material.R.attr.colorButtonNormal
            val resourceId = TypedValue()
                .also { binding.root.context.theme.resolveAttribute(attr, it, true) }
                .resourceId

            val color = if (backgroundColor == null) {
                binding.backgroundView.setBackgroundResource(resourceId)
                ContextCompat.getColor(context, resourceId)
            } else {
                binding.backgroundView.setBackgroundColor(backgroundColor)
                backgroundColor
            }

            val textColor =
                if (ColorUtils.calculateLuminance(color) > .5) Color.BLACK else Color.WHITE
            binding.title.setTextColor(textColor)

            val text = when {
                event.isHoliday -> "${event.title} ($holidayString)"
                event is CalendarEvent.DeviceCalendarEvent -> event.formatTitle()
                else -> event.title
            }
            binding.root.contentDescription = if (event.isHoliday)
                binding.root.context.getString(R.string.holiday_reason, event.title) else text

            binding.title.text = text
            if (event is CalendarEvent.DeviceCalendarEvent) {
                binding.title.setTextIsSelectable(false)
                binding.root.setOnClickListener { onEventClick(event.id) }
                binding.title.putLineEndIcon(R.drawable.ic_open_in_new, textColor)
            } else {
                binding.title.setTextIsSelectable(true)
                binding.root.setOnClickListener(null)
                binding.title.putLineEndIcon(0, textColor)
            }
        }
    }
}
