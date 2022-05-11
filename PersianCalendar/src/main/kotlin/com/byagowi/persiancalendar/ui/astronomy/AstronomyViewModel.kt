package com.byagowi.persiancalendar.ui.astronomy

import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.lifecycle.ViewModel
import com.byagowi.persiancalendar.utils.ONE_MINUTE_IN_MILLIS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.util.*
import kotlin.math.roundToInt

class AstronomyViewModel : ViewModel() {
    private val _isTropical = MutableStateFlow(false)
    val isTropical: StateFlow<Boolean> = _isTropical

    private val _mode = MutableStateFlow(AstronomyMode.Earth)
    val mode: StateFlow<AstronomyMode> = _mode

    private val _minutesOffset = MutableStateFlow(DEFAULT_TIME)
    val minutesOffset: StateFlow<Int> = _minutesOffset

    private val dateSink = GregorianCalendar() // Just to avoid recreating it everytime
    private var _astronomyState = MutableStateFlow(AstronomyState(dateSink))
    var astronomyState: StateFlow<AstronomyState> = _astronomyState

    // Both minutesOffset and astronomyState keep some sort of time state, astronomyState however
    // is meant to be used in animation thus is the visible one and the other is to keep final
    // animation value so we should keep the two in sync manually with a slight difference.
    //
    // The separation has the benefit of not making reset button visible on initial animation
    // of the screen entrance and makes one day button to exactly jump 24h regardless of current
    // animation of the screen.

    // Events
    val resetButtonVisibilityEvent = minutesOffset
        .map { it != 0 }
        .distinctUntilChanged()

    // Commands
    private fun setAstronomyState(value: Int) {
        dateSink.timeInMillis = System.currentTimeMillis() + value * ONE_MINUTE_IN_MILLIS
        _astronomyState.value = AstronomyState(dateSink)
    }

    private var animator: ValueAnimator? = null
    private val interpolator = AccelerateDecelerateInterpolator()
    fun animateToAbsoluteMinutesOffset(value: Int) {
        animator?.removeAllUpdateListeners()
        ValueAnimator.ofInt(
            // If the animation is still going on use its current value to not have jumps
            if (animator?.isRunning == true) animator?.animatedValue as? Int ?: 0
            else _minutesOffset.value,
            value
        ).also {
            animator = it
            it.duration = 400 // android.R.integer.config_mediumAnimTime
            it.interpolator = interpolator
            it.addUpdateListener { _ -> setAstronomyState(it.animatedValue as? Int ?: 0) }
        }.start()
        _minutesOffset.value = value
    }

    fun animateToAbsoluteDayOffset(dayOffset: Int) {
        animateToAbsoluteMinutesOffset(dayOffset * MINUTES_IN_DAY)
    }

    fun animateToRelativeDayOffset(dayOffset: Int) {
        animateToAbsoluteMinutesOffset(_minutesOffset.value + dayOffset * MINUTES_IN_DAY)
    }

    // This is provided to bypass view model provided animation for the screen's slider
    // which changes the values smoothly and doesn't need another filter in between.
    fun addMinutesOffset(offset: Int) {
        _minutesOffset.value += offset
        setAstronomyState(_minutesOffset.value)
    }

    // Command to be issued from MapScreen when astronomy screen is in its backstack so we like to
    // have them in sync
    fun changeToTime(time: Long) {
        _minutesOffset.value =
            ((time - System.currentTimeMillis()) / ONE_MINUTE_IN_MILLIS).toFloat().roundToInt()
        setAstronomyState(_minutesOffset.value)
    }

    fun changeTropicalStatus(value: Boolean) {
        _isTropical.value = value
    }

    fun changeScreenMode(value: AstronomyMode) {
        _mode.value = value
    }

    companion object {
        private const val MINUTES_IN_DAY = 60 * 24
        const val DEFAULT_TIME = -MINUTES_IN_DAY // Initial animation, comes from yesterday
    }
}
