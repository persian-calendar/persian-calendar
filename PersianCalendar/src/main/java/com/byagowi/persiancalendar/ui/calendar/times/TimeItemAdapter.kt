package com.byagowi.persiancalendar.ui.calendar.times

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.TimeItemBinding
import com.byagowi.persiancalendar.utils.getFormattedClock
import com.google.android.flexbox.FlexboxLayoutManager
import io.github.persiancalendar.praytimes.Clock
import io.github.persiancalendar.praytimes.PrayTimes

@StringRes
private val timeNames = listOf(
    R.string.imsak, R.string.fajr, R.string.sunrise, R.string.dhuhr, R.string.asr,
    R.string.sunset, R.string.maghrib, R.string.isha, R.string.midnight
)

class TimeItemAdapter : RecyclerView.Adapter<TimeItemAdapter.ViewHolder>() {

    var mPrayTimes: PrayTimes? = null
        set(prayTimes) {
            field = prayTimes
            for (i in timeNames.indices) notifyItemChanged(i)
        }
    var isExpanded = false
        set(expanded) {
            field = expanded
            for (i in timeNames.indices) notifyItemChanged(i)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TimeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

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
            binding.root.layoutParams = if (!isExpanded && !(timeName == R.string.fajr ||
                        timeName == R.string.dhuhr || timeName == R.string.maghrib)
            )
                emptyLayout
            else
                wrapContent

            binding.name.setText(timeName)

            val prayTimes = mPrayTimes
            if (prayTimes == null) {
                binding.time.text = ""
                return
            }

            binding.time.text = getFormattedClock(when (timeName) {
                R.string.imsak -> prayTimes.imsakClock
                R.string.fajr -> prayTimes.fajrClock
                R.string.sunrise -> prayTimes.sunriseClock
                R.string.dhuhr -> prayTimes.dhuhrClock
                R.string.asr -> prayTimes.asrClock
                R.string.sunset -> prayTimes.sunsetClock
                R.string.maghrib -> prayTimes.maghribClock
                R.string.isha -> prayTimes.ishaClock
                R.string.midnight -> prayTimes.midnightClock
                else -> prayTimes.midnightClock
            }, false)
        }
    }
}
