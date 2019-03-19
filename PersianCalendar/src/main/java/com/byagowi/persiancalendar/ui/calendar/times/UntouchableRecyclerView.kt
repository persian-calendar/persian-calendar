package com.byagowi.persiancalendar.ui.calendar.times

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

import androidx.recyclerview.widget.RecyclerView

// https://stackoverflow.com/a/47671471
class UntouchableRecyclerView : RecyclerView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onTouchEvent(e: MotionEvent): Boolean = false
}
