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
package com.byagowi.persiancalendar.ui.preferences.widgetnotification

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.view.setPadding
import androidx.core.view.updatePadding
import java.util.*

class ColorPickerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
        LinearLayout(context, attrs) {

    private var colorResultView: TextView
    private var redSeekBar: SeekBar
    private var greenSeekBar: SeekBar
    private var blueSeekBar: SeekBar
    private var alphaSeekBar: SeekBar
    private var colorsToPick: LinearLayout
    private var seekBars: LinearLayout
    private var colorFrame: FrameLayout
    private var colorCodeVisibility = false

    val pickerColor: Int
        @ColorInt
        get() = Color.argb(
                alphaSeekBar.progress, redSeekBar.progress, greenSeekBar.progress, blueSeekBar.progress
        )

    private val Number.dp: Int
        get() = (toFloat() * Resources.getSystem().displayMetrics.density).toInt()

    init {
        orientation = VERTICAL

        colorResultView = TextView(context).apply {
            setTextIsSelectable(true)
            gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
            setOnClickListener {
                colorCodeVisibility = !colorCodeVisibility
                showColor()
            }
        }

        val listener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) =
                    showColor()

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        }

        redSeekBar = SeekBar(context)
        greenSeekBar = SeekBar(context)
        blueSeekBar = SeekBar(context)
        alphaSeekBar = SeekBar(context)

        listOf(redSeekBar, greenSeekBar, blueSeekBar, alphaSeekBar).zip(
                listOf("#ff1744", "#00c853", "#448aff", "#a0a0a0").map { Color.parseColor(it) }
        ) { seekBar, color ->
            seekBar.apply {
                updatePadding(top = 8.dp, bottom = 8.dp)
                max = 255
                progressDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
                setOnSeekBarChangeListener(listener)

                thumb.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            }
        }

        seekBars = LinearLayout(context).apply {
            orientation = VERTICAL

            listOf(redSeekBar, greenSeekBar, blueSeekBar, alphaSeekBar).forEach(::addView)

            layoutParams =
                    LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                        weight = 1f
                    }
            measure(
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            )
        }

        colorFrame = object : FrameLayout(context) {
            val checker = createCheckerBoard(20)
            val rect: Rect = Rect()
            override fun onDraw(canvas: Canvas?) {
                super.onDraw(canvas)
                getDrawingRect(rect)
                canvas?.drawRect(rect, checker)
            }
        }.apply {
            addView(colorResultView)
            layoutParams = LayoutParams(
                    seekBars.measuredHeight,
                    LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.LTGRAY)
            setPadding(1.dp, 1.dp, 1.dp, 1.dp)
        }

        val widgetMain = LinearLayout(context).apply {
            addView(seekBars)
            addView(colorFrame)
        }

        colorsToPick = LinearLayout(context).apply {
            gravity = Gravity.CENTER
            orientation = HORIZONTAL
        }

        addView(widgetMain)
        addView(colorsToPick)
    }

    fun setColorsToPick(@ColorInt colors: List<Long>) {
        colorsToPick.removeAllViews()

        val context = context ?: return

        colors.map(Long::toInt).forEach { color ->
            val view = View(context).apply {
                setBackgroundColor(color)
                layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
                )
            }

            val checkerBoard = createCheckerBoard(12)
            val frameLayout = object : FrameLayout(context) {
                val rect: Rect = Rect()
                override fun onDraw(canvas: Canvas?) {
                    super.onDraw(canvas)
                    getDrawingRect(rect)
                    canvas?.drawRect(rect, checkerBoard)
                }
            }.apply {
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

    private fun showColor() {
        val color = Color.argb(
                alphaSeekBar.progress, redSeekBar.progress, greenSeekBar.progress, blueSeekBar.progress
        )
        colorResultView.apply {
            setBackgroundColor(color)
            text =
                    if (colorCodeVisibility) "#%08X".format(Locale.ENGLISH, color) else ""
            setTextColor(color xor 0xFFFFFF)
        }
    }

    fun setPickedColor(@ColorInt color: Int) {
        listOf(redSeekBar, greenSeekBar, blueSeekBar, alphaSeekBar).zip(
                listOf(Color.red(color), Color.green(color), Color.blue(color), Color.alpha(color))
        ) { seekBar, channelValue ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                seekBar.setProgress(channelValue, true)
            else
                seekBar.progress = channelValue
        }
        showColor()
    }

    fun hideAlphaSeekBar() {
        alphaSeekBar.visibility = GONE
        seekBars.measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        colorFrame.layoutParams = LayoutParams(
                seekBars.measuredHeight, LayoutParams.MATCH_PARENT
        )
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