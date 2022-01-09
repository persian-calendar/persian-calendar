package com.byagowi.persiancalendar.ui.athan

import androidx.activity.ComponentActivity
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.ActivityAthanBinding
import com.byagowi.persiancalendar.ui.utils.fadeIn
import com.byagowi.persiancalendar.utils.TWO_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.cityName
import com.byagowi.persiancalendar.utils.getPrayTimeName

fun setAthanActivityContent(activity: ComponentActivity, prayerKey: String, onClick: () -> Unit) {
    val cityName = activity.appPrefs.cityName
    activity.setContentView(ActivityAthanBinding.inflate(activity.layoutInflater).also { binding ->
        binding.athanName.setText(getPrayTimeName(prayerKey))
        binding.place.fadeIn(TWO_SECONDS_IN_MILLIS)
        binding.root.setOnClickListener { onClick() }
        binding.root.background = PatternDrawable(prayerKey)
        binding.place.text = cityName?.let { activity.getString(R.string.in_city_time, it) }
    }.root)
}
