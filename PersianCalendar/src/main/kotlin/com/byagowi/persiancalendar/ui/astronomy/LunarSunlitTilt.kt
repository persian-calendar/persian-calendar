package com.byagowi.persiancalendar.ui.astronomy

import io.github.cosinekitty.astronomy.Aberration
import io.github.cosinekitty.astronomy.AstroTime
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.EquatorEpoch
import io.github.cosinekitty.astronomy.Observer
import io.github.cosinekitty.astronomy.Refraction
import io.github.cosinekitty.astronomy.equator
import io.github.cosinekitty.astronomy.horizon
import io.github.cosinekitty.astronomy.rotationEqdHor
import java.util.*
import kotlin.math.atan2

fun lunarSunlitTilt(time: GregorianCalendar, observer: Observer): Double {
    val astroTime = AstroTime(time)
    val moonEquator = equator(Body.Moon, astroTime, observer, EquatorEpoch.OfDate, Aberration.None)
    val sunEquator = equator(Body.Sun, astroTime, observer, EquatorEpoch.OfDate, Aberration.None)
    val moonHorizontal =
        horizon(astroTime, observer, moonEquator.ra, moonEquator.dec, Refraction.None);
    var rot = rotationEqdHor(astroTime, observer)
    rot = rot.pivot(2, moonHorizontal.azimuth)
    rot = rot.pivot(1, moonHorizontal.altitude)
    val vec = rot.rotate(sunEquator.vec)
    return Math.toDegrees(atan2(vec.z, vec.y))
}

