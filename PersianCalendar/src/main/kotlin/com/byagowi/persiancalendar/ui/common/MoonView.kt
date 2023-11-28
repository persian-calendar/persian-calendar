package com.byagowi.persiancalendar.ui.common

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.view.isVisible
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.utils.lerp
import io.github.cosinekitty.astronomy.Ecliptic
import io.github.cosinekitty.astronomy.Spherical
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.eclipticGeoMoon
import io.github.cosinekitty.astronomy.sunPosition
import java.util.GregorianCalendar
import kotlin.math.roundToInt

class MoonView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val solarDraw = SolarDraw(context)
    private val animator = ValueAnimator.ofFloat(0f, 1f).also {
        it.duration = resources.getInteger(android.R.integer.config_longAnimTime).toLong()
        it.interpolator = AccelerateDecelerateInterpolator()
    }
    private var sun: Ecliptic? = null
    private var moon: Spherical? = null
    var jdn = Jdn.today().value.toFloat()
        set(value) {
            if (!isVisible) {
                field = value
                update()
                return
            }
            val from = if (field == value) value - 29f else field.coerceIn(value - 30f, value + 30f)
            animator.removeAllUpdateListeners()
            animator.addUpdateListener { _ ->
                field = lerp(from, value, animator.animatedFraction)
                update()
            }
            animator.start()
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        solarDraw.moon(canvas, sun ?: return, moon ?: return, cx, cx, cx)
    }

    fun update() {
        val date = Jdn(jdn.toLong()).toGregorianCalendar()
        val fractionOfDay = jdn % 1 // jdn is a float so it can do smooth transition
        date[GregorianCalendar.HOUR_OF_DAY] = (fractionOfDay * 24).roundToInt().coerceIn(0, 23)
        val time = Time.fromMillisecondsSince1970(date.time.time)
        sun = sunPosition(time)
        moon = eclipticGeoMoon(time)
        invalidate()
    }
}
