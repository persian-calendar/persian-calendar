/*
 * Single class, no dependency, ColorPickerView.
 * Unlike the rest of the project is released under MIT license.
 * Feel free to copy and use it wherever you like or suggest improvements to it
 *
 * Copyright (c) 2018 Ebrahim Byagowi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.byagowi.persiancalendar.ui.preferences.widgetnotification

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView

import java.util.Locale

class ColorPickerView : LinearLayout {
    private var colorResultView: TextView? = null
    private var redSeekBar: SeekBar? = null
    private var greenSeekBar: SeekBar? = null
    private var blueSeekBar: SeekBar? = null
    private var colorsToPick: LinearLayout? = null
    private var colorCodeVisibility = false

    /*@ColorInt*/
    val pickerColor: Int
        get() = Color.argb(0xFF,
                redSeekBar!!.progress, greenSeekBar!!.progress, blueSeekBar!!.progress)

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        orientation = LinearLayout.VERTICAL

        val context = context ?: return

        colorResultView = TextView(context)
        colorResultView!!.setTextIsSelectable(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            colorResultView!!.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        }
        colorResultView!!.setOnClickListener { view ->
            colorCodeVisibility = !colorCodeVisibility
            showColor()
        }

        redSeekBar = SeekBar(context)
        greenSeekBar = SeekBar(context)
        blueSeekBar = SeekBar(context)

        val density = context.resources.displayMetrics.density
        val seekBarPadding = density.toInt() * 8
        val currentSidePad = redSeekBar!!.paddingLeft
        redSeekBar!!.setPadding(currentSidePad, seekBarPadding, currentSidePad, seekBarPadding)
        greenSeekBar!!.setPadding(currentSidePad, seekBarPadding, currentSidePad, seekBarPadding)
        blueSeekBar!!.setPadding(currentSidePad, seekBarPadding, currentSidePad, seekBarPadding)

        redSeekBar!!.max = 255
        greenSeekBar!!.max = 255
        blueSeekBar!!.max = 255

        redSeekBar!!.progressDrawable.setColorFilter(-0x400000, PorterDuff.Mode.SRC_IN)
        greenSeekBar!!.progressDrawable.setColorFilter(-0xff4000, PorterDuff.Mode.SRC_IN)
        blueSeekBar!!.progressDrawable.setColorFilter(-0xffff40, PorterDuff.Mode.SRC_IN)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            redSeekBar!!.thumb.setColorFilter(-0x400000, PorterDuff.Mode.SRC_IN)
            greenSeekBar!!.thumb.setColorFilter(-0xff4000, PorterDuff.Mode.SRC_IN)
            blueSeekBar!!.thumb.setColorFilter(-0xffff40, PorterDuff.Mode.SRC_IN)
        }

        val listener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                showColor()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        }

        redSeekBar!!.setOnSeekBarChangeListener(listener)
        greenSeekBar!!.setOnSeekBarChangeListener(listener)
        blueSeekBar!!.setOnSeekBarChangeListener(listener)

        val seekBars = LinearLayout(context)
        seekBars.orientation = LinearLayout.VERTICAL
        seekBars.addView(redSeekBar)
        seekBars.addView(greenSeekBar)
        seekBars.addView(blueSeekBar)
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.weight = 1f
        seekBars.layoutParams = params
        seekBars.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))

        val frameLayout = FrameLayout(context)
        frameLayout.addView(colorResultView)
        frameLayout.layoutParams = LinearLayout.LayoutParams(seekBars.measuredHeight,
                LinearLayout.LayoutParams.MATCH_PARENT)
        frameLayout.setBackgroundColor(Color.LTGRAY)
        val framePadding = density.toInt()
        frameLayout.setPadding(framePadding, framePadding, framePadding, framePadding)

        val widgetMain = LinearLayout(context)
        widgetMain.addView(seekBars)
        widgetMain.addView(frameLayout)

        colorsToPick = LinearLayout(context)
        colorsToPick!!.gravity = Gravity.CENTER
        colorsToPick!!.orientation = LinearLayout.HORIZONTAL

        addView(widgetMain)
        addView(colorsToPick)
    }

    fun setColorsToPick(/*@ColorInt*/colors: IntArray) {
        colorsToPick!!.removeAllViews()

        val context = context ?: return

        val density = context.resources.displayMetrics.density

        for (color in colors) {
            val view = View(context)
            view.setBackgroundColor(color)
            view.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT)

            val frameLayout = FrameLayout(context)
            val rectSize = (40 * density).toInt()
            val margin = (5 * density).toInt()
            val layoutParams = LinearLayout.LayoutParams(rectSize, rectSize)
            layoutParams.setMargins(margin, margin * 2, margin, margin)
            frameLayout.setBackgroundColor(Color.LTGRAY)
            frameLayout.layoutParams = layoutParams
            frameLayout.addView(view)
            val framePadding = density.toInt()
            frameLayout.setPadding(framePadding, framePadding, framePadding, framePadding)

            frameLayout.setOnClickListener { v -> setPickedColor(color) }

            colorsToPick!!.addView(frameLayout)
        }
    }

    private fun showColor() {
        val color = Color.argb(0xFF, redSeekBar!!.progress,
                greenSeekBar!!.progress, blueSeekBar!!.progress)
        colorResultView!!.setBackgroundColor(color)
        colorResultView!!.text = if (colorCodeVisibility)
            String.format(Locale.ENGLISH, "#%06X", 0xFFFFFF and color)
        else
            ""
        colorResultView!!.setTextColor(color xor 0xFFFFFF)
    }

    fun setPickedColor(/*@ColorInt*/color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            redSeekBar!!.setProgress(Color.red(color), true)
            greenSeekBar!!.setProgress(Color.green(color), true)
            blueSeekBar!!.setProgress(Color.blue(color), true)
        } else {
            redSeekBar!!.progress = Color.red(color)
            greenSeekBar!!.progress = Color.green(color)
            blueSeekBar!!.progress = Color.blue(color)
        }
        showColor()
    }
}
