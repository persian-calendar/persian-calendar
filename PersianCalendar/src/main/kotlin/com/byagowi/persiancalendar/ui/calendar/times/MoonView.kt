package com.byagowi.persiancalendar.ui.calendar.times

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.ui.shared.SolarDraw
import com.byagowi.persiancalendar.utils.calculateSunMoonPosition
import com.cepmuvakkit.times.posAlgo.SunMoonPosition
import java.util.*
import kotlin.math.roundToInt

class MoonView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    init {
        if (BuildConfig.DEVELOPMENT) {
            setOnLongClickListener {
                AlertDialog.Builder(context)
                    .setTitle("DEVELOPMENT ONLY")
                    .setView(SolarDemoView(context))
                    .show()
                true
            }
        }
    }

    private val solarDraw = SolarDraw(context)
    private var animator: ValueAnimator? = null
    private var sunMoonPosition: SunMoonPosition? = null
    var jdn = Jdn.today().value.toFloat()
        set(value) {
            animator?.removeAllUpdateListeners()
            if (!isVisible) {
                field = value
                update()
                return
            }
            ValueAnimator.ofFloat(field, value).also {
                animator = it
                it.duration = resources.getInteger(android.R.integer.config_longAnimTime).toLong()
                it.interpolator = AccelerateDecelerateInterpolator()
                it.addUpdateListener { _ ->
                    field = ((it.animatedValue as? Float) ?: return@addUpdateListener)
                    update()
                }
            }.start()
            postInvalidate()
        }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas ?: return)
        val cx = width / 2f
        solarDraw.moon(canvas, sunMoonPosition ?: return, cx, cx, cx)
    }

    fun update() {
        val date = Jdn(jdn.toLong()).toJavaCalendar()
        date[Calendar.HOUR_OF_DAY] = ((jdn % 1) * 24).roundToInt().coerceIn(0, 23)
        sunMoonPosition = date.calculateSunMoonPosition(coordinates)
        postInvalidate()
    }
}
