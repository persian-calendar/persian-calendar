package com.byagowi.persiancalendar.ui.calendar.times

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView

import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.TimeItemBinding
import com.byagowi.persiancalendar.praytimes.Clock
import com.byagowi.persiancalendar.praytimes.PrayTimes
import com.byagowi.persiancalendar.utils.Utils
import com.google.android.flexbox.FlexboxLayoutManager

class TimeItemAdapter : RecyclerView.Adapter<TimeItemAdapter.ViewHolder>() {
    private var mPrayTimes: PrayTimes? = null
    var isExpanded = false
        set(expanded) {
            field = expanded
            for (i in timeNames.indices) notifyItemChanged(i)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TimeItemBinding.inflate(LayoutInflater.from(parent.context),
                parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return timeNames.size
    }

    fun setTimes(prayTimes: PrayTimes) {
        mPrayTimes = prayTimes
        for (i in timeNames.indices) notifyItemChanged(i)
    }

    inner class ViewHolder(private val binding: TimeItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private val emptyLayout = FlexboxLayoutManager.LayoutParams(0, 0)
        private val wrapContent = FlexboxLayoutManager.LayoutParams(
                FlexboxLayoutManager.LayoutParams.WRAP_CONTENT,
                FlexboxLayoutManager.LayoutParams.WRAP_CONTENT)

        fun bind(position: Int) {
            val timeName = timeNames[position]
            binding.root.layoutParams = if (!isExpanded && !(timeName == R.string.fajr ||
                            timeName == R.string.dhuhr || timeName == R.string.maghrib))
                emptyLayout
            else
                wrapContent

            binding.name.setText(timeName)

            val clock: Clock? = when (timeName) {
                R.string.imsak -> mPrayTimes?.imsakClock
                R.string.fajr -> mPrayTimes?.fajrClock
                R.string.sunrise -> mPrayTimes?.sunriseClock
                R.string.dhuhr -> mPrayTimes?.dhuhrClock
                R.string.asr -> mPrayTimes?.asrClock
                R.string.sunset -> mPrayTimes?.sunsetClock
                R.string.maghrib -> mPrayTimes?.maghribClock
                R.string.isha -> mPrayTimes?.ishaClock
                R.string.midnight -> mPrayTimes?.midnightClock
                else -> mPrayTimes?.midnightClock
            }

            if (clock == null) {
                binding.time.text = ""
                return
            }

            binding.time.text = Utils.getFormattedClock(clock, false)
        }
    }

    companion object {
        @StringRes
        private val timeNames = intArrayOf(R.string.imsak, R.string.fajr, R.string.sunrise, R.string.dhuhr, R.string.asr, R.string.sunset, R.string.maghrib, R.string.isha, R.string.midnight)
    }
}
