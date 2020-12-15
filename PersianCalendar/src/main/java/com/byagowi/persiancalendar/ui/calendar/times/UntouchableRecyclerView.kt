package com.byagowi.persiancalendar.ui.calendar.times

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

import androidx.recyclerview.widget.RecyclerView

// https://stackoverflow.com/a/47671471
class UntouchableRecyclerView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null
) : RecyclerView(context, attrs) {
    override fun onTouchEvent(e: MotionEvent?): Boolean = false
}
