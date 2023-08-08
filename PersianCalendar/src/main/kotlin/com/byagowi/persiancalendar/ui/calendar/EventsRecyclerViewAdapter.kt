package com.byagowi.persiancalendar.ui.calendar

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.util.lruCache
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.EventItemBinding
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.global.holidayString
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.ui.utils.resolveResourceIdFromTheme
import com.byagowi.persiancalendar.utils.formatTitle
import com.byagowi.persiancalendar.utils.logException
import com.google.android.material.color.MaterialColors
import kotlin.math.min
import kotlin.math.roundToInt

class EventsRecyclerViewAdapter(
    private val onEventClick: (Int) -> Unit, private val isRtl: Boolean, private val dp: Float,
    private val createEventIcon: () -> Drawable, private val applyGradient: Boolean,
) : RecyclerView.Adapter<EventsRecyclerViewAdapter.EventViewHolder>() {
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

    private val openInNewIconCache = lruCache(16, create = { color: Int ->
        val drawable = createEventIcon()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) drawable.setTint(color)
        val pad = (4 * dp).roundToInt()
        val result = InsetDrawable(drawable, if (isRtl) 0 else pad, 0, if (isRtl) pad else 0, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            result.layoutDirection =
                if (isRtl) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
            result.isAutoMirrored = true
        }
        result
    })

    inner class EventViewHolder(val binding: EventItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private fun TextView.putLineEndIcon(icon: Drawable?) {
            setCompoundDrawablesWithIntrinsicBounds(
                if (isRtl) icon else null, null, if (isRtl) null else icon, null
            )
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
            val backgroundColorAttribute =
                if (event.isHoliday) R.attr.colorTextHoliday
                else com.google.android.material.R.attr.colorButtonNormal

            val resolvedBackgroundColor = if (backgroundColor == null) {
                val resourceId = context.resolveResourceIdFromTheme(backgroundColorAttribute)
                binding.title.setBackgroundResource(resourceId)
                ContextCompat.getColor(context, resourceId)
            } else {
                binding.title.setBackgroundColor(backgroundColor)
                backgroundColor
            }

            val foregroundColor =
                if (MaterialColors.isColorLight(resolvedBackgroundColor)) Color.BLACK
                else Color.WHITE
            binding.title.setTextColor(foregroundColor)
            if (applyGradient && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                binding.title.foreground = GradientDrawable().also {
                    it.colors = intArrayOf(
                        Color.TRANSPARENT, ColorUtils.setAlphaComponent(foregroundColor, 40)
                    )
                    it.orientation =
                        if (isRtl) GradientDrawable.Orientation.TR_BL
                        else GradientDrawable.Orientation.TL_BR
                }
            }

            val text = when {
                event.isHoliday -> "${event.title} ($holidayString)"
                event is CalendarEvent.DeviceCalendarEvent -> event.formatTitle()
                else -> event.title
            }
            binding.root.contentDescription = if (event.isHoliday)
                context.getString(R.string.holiday_reason, event.title) else text

            binding.title.text = text
            if (event is CalendarEvent.DeviceCalendarEvent) {
                binding.root.setOnClickListener { onEventClick(event.id) }
                binding.title.setTextIsSelectable(false)
                binding.title.putLineEndIcon(openInNewIconCache[foregroundColor])
            } else {
                binding.root.setOnClickListener(null)
                binding.title.setTextIsSelectable(true)
                binding.title.putLineEndIcon(null)
            }
        }
    }
}
