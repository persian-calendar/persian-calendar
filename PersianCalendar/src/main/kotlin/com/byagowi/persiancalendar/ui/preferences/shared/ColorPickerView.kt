/*
 * Single class, no dependency, ColorPickerView.
 * Unlike the rest of the project is released under MIT license.
 * Feel free to copy and use it wherever you like or suggest improvements to it
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
package com.byagowi.persiancalendar.ui.preferences.shared

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.annotation.ColorInt
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import java.util.*

class ColorPickerView(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {

    private val colorResultView = EditText(context).apply {
        gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        setOnFocusChangeListener { _, hasFocus ->
            colorCodeVisibility = hasFocus
            showColor()
        }
        doOnTextChanged { text, _, _, _ ->
            runCatching { setPickedColor(Color.parseColor(text.toString()), fromEditor = true) }
        }
    }
    private val redSeekBar = SeekBar(context)
    private val greenSeekBar = SeekBar(context)
    private val blueSeekBar = SeekBar(context)
    private val alphaSeekBar = SeekBar(context)
    private val colorsToPick = LinearLayout(context).apply {
        gravity = Gravity.CENTER
        orientation = HORIZONTAL
    }
    private val seekBars = LinearLayout(context).apply {
        orientation = VERTICAL
        listOf(redSeekBar, greenSeekBar, blueSeekBar, alphaSeekBar).forEach(::addView)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).also {
            it.weight = 1f
        }
    }
    private val colorView = createColorView(createCheckerBoard(20))
    private var colorCodeVisibility = false

    val pickerColor: Int
        @ColorInt
        get() = Color.argb(
            alphaSeekBar.progress, redSeekBar.progress, greenSeekBar.progress, blueSeekBar.progress
        )

    private val Number.dp: Int
        get() = (toFloat() * Resources.getSystem().displayMetrics.density).toInt()

    private fun createColorView(paint: Paint) = object : FrameLayout(context) {
        val rect = Rect()
        override fun onDraw(canvas: Canvas) {
            getDrawingRect(rect)
            rect.inset(1.dp, 1.dp)
            canvas.drawRect(rect, paint)
        }
    }

    init {
        orientation = VERTICAL

        val listener = object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) =
                if (fromUser) showColor() else Unit
        }

        listOf(redSeekBar, greenSeekBar, blueSeekBar, alphaSeekBar).zip(
            listOf("#ff1744", "#00c853", "#448aff", "#a0a0a0").map(Color::parseColor)
        ) { seekBar, color ->
            seekBar.apply {
                updatePadding(top = 8.dp, bottom = 8.dp)
                max = 255
                progressDrawable.colorFilter =
                    BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                        color,
                        BlendModeCompat.SRC_IN
                    )
                setOnSeekBarChangeListener(listener)

                thumb.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    color,
                    BlendModeCompat.SRC_IN
                )
            }
        }

        addView(LinearLayout(context).apply {
            addView(seekBars.also {
                // this should happen here otherwise it won't get rectangular shape
                it.measure(
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                )
            })
            addView(colorView.apply {
                addView(colorResultView)
                layoutParams = LayoutParams(
                    seekBars.measuredHeight,
                    LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(Color.LTGRAY)
                setPadding(1.dp, 1.dp, 1.dp, 1.dp)
            })
        })
        addView(colorsToPick)
    }

    fun setColorsToPick(@ColorInt colors: List<Long>) {
        colorsToPick.removeAllViews()
        val checkerBoard = createCheckerBoard(12)
        colors.map(Long::toInt).forEach { color ->
            val view = View(context).apply {
                setBackgroundColor(color)
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
                )
            }
            val frameLayout = createColorView(checkerBoard).apply {
                setBackgroundColor(Color.LTGRAY)
                layoutParams = LayoutParams(40.dp, 40.dp).apply {
                    setMargins(5.dp, 10.dp, 5.dp, 5.dp)
                }
                addView(view)
                setPadding(1.dp)
                setOnClickListener { setPickedColor(color) }
            }
            colorsToPick.addView(frameLayout)
        }
    }

    private fun showColor(fromEditor: Boolean = false) {
        val color = Color.argb(
            alphaSeekBar.progress, redSeekBar.progress, greenSeekBar.progress, blueSeekBar.progress
        )
        colorResultView.apply {
            setBackgroundColor(color)
            if (!fromEditor) {
                setText(if (colorCodeVisibility) "#%08X".format(Locale.ENGLISH, color) else "")
            }
            setTextColor(ColorUtils.setAlphaComponent(color xor 0xFFFFFF, 0xFF))
        }
    }

    fun setPickedColor(@ColorInt color: Int, fromEditor: Boolean = false) {
        listOf(redSeekBar, greenSeekBar, blueSeekBar, alphaSeekBar).zip(
            listOf(Color.red(color), Color.green(color), Color.blue(color), Color.alpha(color))
        ) { seekBar, channelValue ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                seekBar.setProgress(channelValue, true)
            else
                seekBar.progress = channelValue
        }
        showColor(fromEditor)
    }

    fun hideAlphaSeekBar() {
        alphaSeekBar.isVisible = false
        seekBars.measure(
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        colorView.layoutParams = LayoutParams(seekBars.measuredHeight, LayoutParams.MATCH_PARENT)
    }
}

// https://stackoverflow.com/a/58471997
fun createCheckerBoard(tileSize: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    shader = BitmapShader(
        Bitmap.createBitmap(tileSize * 2, tileSize * 2, Bitmap.Config.ARGB_8888).apply {
            Canvas(this).apply {
                val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.FILL
                    color = 0x22000000
                }
                drawRect(0f, 0f, tileSize.toFloat(), tileSize.toFloat(), fill)
                drawRect(
                    tileSize.toFloat(), tileSize.toFloat(), tileSize * 2f, tileSize * 2f, fill
                )
            }
        }, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT
    )
}
