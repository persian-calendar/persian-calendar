package com.byagowi.persiancalendar.ui.astronomy

import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.util.GregorianCalendar
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

class AstronomyViewModel : ViewModel() {
    var mode by mutableStateOf(AstronomyMode.entries[0])
    var isTropical by mutableStateOf(false)
    var isDatePickerDialogShown by mutableStateOf(false)

    private var _minutesOffset = mutableIntStateOf(DEFAULT_TIME)
    val minutesOffset by _minutesOffset

    private val dateSink = GregorianCalendar() // Just to avoid recreating it everytime
    private val _astronomyState = mutableStateOf(AstronomyState(dateSink))
    val astronomyState by _astronomyState

    // Both minutesOffset and astronomyState keep some sort of time state, astronomyState however
    // is meant to be used in animation thus is the visible one and the other is to keep final
    // animation value so we should keep the two in sync manually with a slight difference.
    //
    // The separation has the benefit of not making reset button visible on initial animation
    // of the screen entrance and makes one day button to exactly jump 24h regardless of current
    // animation of the screen.

    // Commands

    private fun setAstronomyState(value: Int) {
        dateSink.timeInMillis =
            (System.currentTimeMillis().milliseconds + value.minutes).inWholeMilliseconds
        _astronomyState.value = AstronomyState(dateSink)
    }

    private val animator = ValueAnimator().also {
        it.duration = 400 // android.R.integer.config_mediumAnimTime
        it.interpolator = AccelerateDecelerateInterpolator()
        it.addUpdateListener { _ -> setAstronomyState(it.animatedValue as? Int ?: 0) }
    }

    fun animateToAbsoluteMinutesOffset(value: Int) {
        animator.setIntValues(
            // If the animation is still going on use its current value to not have jumps
            if (animator.isRunning) animator.animatedValue as? Int ?: 0
            else _minutesOffset.intValue,
            value,
        )
        animator.start()
        _minutesOffset.intValue = value
    }

    fun animateToAbsoluteDayOffset(dayOffset: Int) {
        animateToAbsoluteMinutesOffset(dayOffset * MINUTES_IN_DAY)
    }

    fun animateToRelativeDayOffset(dayOffset: Int) {
        animateToAbsoluteMinutesOffset(_minutesOffset.intValue + dayOffset * MINUTES_IN_DAY)
    }

    fun animateToTime(time: Long) {
        animateToAbsoluteMinutesOffset(
            ((time - System.currentTimeMillis()).milliseconds / 1.minutes).roundToInt(),
        )
    }

    // This is provided to bypass view model provided animation for the screen's slider
    // which changes the values smoothly and doesn't need another filter in between.
    fun addMinutesOffset(offset: Int) {
        _minutesOffset.intValue += offset
        setAstronomyState(_minutesOffset.intValue)
    }

    // Command to be issued from MapScreen when astronomy screen is in its backstack so we like to
    // have them in sync
    fun changeToTime(time: Long) {
        _minutesOffset.intValue =
            ((time - System.currentTimeMillis()).milliseconds / 1.minutes).roundToInt()
        setAstronomyState(_minutesOffset.intValue)
    }

    companion object {
        private const val MINUTES_IN_DAY = 60 * 24
        const val DEFAULT_TIME = -MINUTES_IN_DAY // Initial animation, comes from yesterday
    }
}
