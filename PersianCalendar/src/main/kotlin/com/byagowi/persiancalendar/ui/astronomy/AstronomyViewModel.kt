package com.byagowi.persiancalendar.ui.astronomy

import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byagowi.persiancalendar.utils.ONE_MINUTE_IN_MILLIS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*

class AstronomyViewModel : ViewModel() {
    private val _isTropical = MutableStateFlow(false)
    val isTropical: StateFlow<Boolean> = _isTropical

    private val _mode = MutableStateFlow(AstronomyMode.Earth)
    val mode: StateFlow<AstronomyMode> = _mode

    private val _minutesOffset = MutableStateFlow(DEFAULT_TIME)
    val minutesOffset: StateFlow<Int> = _minutesOffset

    private var _astronomyState = MutableStateFlow(AstronomyState(GregorianCalendar()))
    var astronomyState: StateFlow<AstronomyState> = _astronomyState

    init {
        viewModelScope.launch { // Fill astronomyState with states generated from minutes offset
            val date = GregorianCalendar()
            _minutesOffset.collectLatest {
                date.timeInMillis = System.currentTimeMillis() + it * ONE_MINUTE_IN_MILLIS
                _astronomyState.value = AstronomyState(date)
            }
        }
    }

    // Events
    val resetButtonVisibilityEvent = minutesOffset
        .map { it != 0 }
        .distinctUntilChanged()

    // Commands
    private var animator: ValueAnimator? = null
    fun animateToAbsoluteMinutesOffset(value: Int) {
        animator?.removeAllUpdateListeners()
        ValueAnimator.ofInt(_minutesOffset.value, value).also {
            animator = it
            it.duration = 400 // android.R.integer.config_mediumAnimTime
            it.interpolator = AccelerateDecelerateInterpolator()
            it.addUpdateListener { _ -> _minutesOffset.value = it.animatedValue as? Int ?: 0 }
        }.start()
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
