package com.byagowi.persiancalendar.ui.athan

import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.AthanActivityBinding
import com.byagowi.persiancalendar.entities.Theme
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.fadeIn
import com.byagowi.persiancalendar.utils.TWO_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.cityName
import com.byagowi.persiancalendar.utils.getPrayTimeName

fun setAthanActivityContent(activity: ComponentActivity, prayerKey: String, onClick: () -> Unit) {
    val cityName = activity.appPrefs.cityName
    activity.setContentView(AthanActivityBinding.inflate(activity.layoutInflater).also { binding ->
        binding.athanName.setText(getPrayTimeName(prayerKey))
        binding.place.fadeIn(TWO_SECONDS_IN_MILLIS)
        binding.root.setOnClickListener { onClick() }
        val pattern = PatternDrawable(
            prayerKey, darkBaseColor = Theme.isNightMode(activity), dp = activity.resources.dp
        )
        val valueAnimator = ValueAnimator.ofFloat(0f, 360f).also {
            it.duration = 180000L
            it.interpolator = LinearInterpolator()
            it.repeatMode = ValueAnimator.RESTART
            it.repeatCount = ValueAnimator.INFINITE
            it.addUpdateListener { valueAnimator ->
                pattern.rotationDegree = valueAnimator.animatedValue as? Float ?: 0f
                pattern.invalidateSelf()
            }
            listOf(it::start, it::reverse).random()()
        }
        valueAnimator.start()
        activity.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                valueAnimator.removeAllUpdateListeners()
                valueAnimator.cancel()
            }
        })
        binding.root.background = pattern
        binding.place.text = cityName?.let { activity.getString(R.string.in_city_time, it) }
    }.root)
}
