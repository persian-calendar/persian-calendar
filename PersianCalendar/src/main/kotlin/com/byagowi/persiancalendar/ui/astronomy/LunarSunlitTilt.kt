package com.byagowi.persiancalendar.ui.astronomy

import io.github.cosinekitty.astronomy.Aberration
import io.github.cosinekitty.astronomy.AstroTime
import io.github.cosinekitty.astronomy.AstroVector
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.EquatorEpoch
import io.github.cosinekitty.astronomy.Observer
import io.github.cosinekitty.astronomy.Refraction
import io.github.cosinekitty.astronomy.RotationMatrix
import io.github.cosinekitty.astronomy.equator
import io.github.cosinekitty.astronomy.horizon
import io.github.cosinekitty.astronomy.rotationEqdHor
import java.util.*
import kotlin.math.atan2

// Use astronomy provided one
fun rotateVector(rotation: RotationMatrix, vector: AstroVector) =
    AstroVector(
        rotation.rot[0][0] * vector.x + rotation.rot[1][0] * vector.y + rotation.rot[2][0] * vector.z,
        rotation.rot[0][1] * vector.x + rotation.rot[1][1] * vector.y + rotation.rot[2][1] * vector.z,
        rotation.rot[0][2] * vector.x + rotation.rot[1][2] * vector.y + rotation.rot[2][2] * vector.z,
        vector.t
    )

fun lunarSunlitTilt(time: GregorianCalendar, observer: Observer): Double {
    val astroTime = AstroTime(time)
    val moonEquator = equator(Body.Moon, astroTime, observer, EquatorEpoch.OfDate, Aberration.None)
    val sunEquator = equator(Body.Sun, astroTime, observer, EquatorEpoch.OfDate, Aberration.None)
    val moonHorizontal =
        horizon(astroTime, observer, moonEquator.ra, moonEquator.dec, Refraction.None);
    var rot = rotationEqdHor(astroTime, observer)
    rot = rot.pivot(2, moonHorizontal.azimuth)
    rot = rot.pivot(1, moonHorizontal.altitude)
    val vec = rotateVector(rot, sunEquator.vec)
    return Math.toDegrees(atan2(vec.z, vec.y))
}

