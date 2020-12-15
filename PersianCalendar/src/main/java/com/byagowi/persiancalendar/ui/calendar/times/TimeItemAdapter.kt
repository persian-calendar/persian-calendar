package com.byagowi.persiancalendar.ui.calendar.times

import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.TimeItemBinding
import com.byagowi.persiancalendar.utils.layoutInflater
import com.byagowi.persiancalendar.utils.toFormattedString
import com.google.android.flexbox.FlexboxLayoutManager
import io.github.persiancalendar.praytimes.PrayTimes

class TimeItemAdapter : RecyclerView.Adapter<TimeItemAdapter.ViewHolder>() {

    @StringRes
    private val timeNames = listOf(
            R.string.imsak, R.string.fajr, R.string.sunrise, R.string.dhuhr, R.string.asr,
            R.string.sunset, R.string.maghrib, R.string.isha, R.string.midnight
    )

    var prayTimes: PrayTimes? = null
        set(prayTimes) {
            field = prayTimes
            timeNames.indices.forEach(::notifyItemChanged)
        }
    var isExpanded = false
        set(expanded) {
            field = expanded
            timeNames.indices.forEach(::notifyItemChanged)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
            TimeItemBinding.inflate(parent.context.layoutInflater, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

    override fun getItemCount(): Int = timeNames.size

    inner class ViewHolder(private val binding: TimeItemBinding) :
            RecyclerView.ViewHolder(binding.root) {
        private val emptyLayout = FlexboxLayoutManager.LayoutParams(0, 0)
        private val wrapContent = FlexboxLayoutManager.LayoutParams(
                FlexboxLayoutManager.LayoutParams.WRAP_CONTENT,
                FlexboxLayoutManager.LayoutParams.WRAP_CONTENT
        )

        fun bind(position: Int) {
            val timeName = timeNames[position]

            binding.root.layoutParams = if (!isExpanded && timeName !in listOf(
                            R.string.fajr, R.string.dhuhr, R.string.maghrib
                    )
            ) emptyLayout else wrapContent
            binding.name.setText(timeName)
            binding.time.text = prayTimes?.run {
                (when (timeName) {
                    R.string.imsak -> imsakClock
                    R.string.fajr -> fajrClock
                    R.string.sunrise -> sunriseClock
                    R.string.dhuhr -> dhuhrClock
                    R.string.asr -> asrClock
                    R.string.sunset -> sunsetClock
                    R.string.maghrib -> maghribClock
                    R.string.isha -> ishaClock
                    R.string.midnight -> midnightClock
                    else -> midnightClock
                }).toFormattedString()
            } ?: ""
        }
    }
}
