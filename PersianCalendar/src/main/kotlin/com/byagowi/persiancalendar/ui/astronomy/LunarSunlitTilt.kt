package com.byagowi.persiancalendar.ui.astronomy

import io.github.cosinekitty.astronomy.Aberration
import io.github.cosinekitty.astronomy.AstroTime
import io.github.cosinekitty.astronomy.AstroVector
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.DEG2RAD
import io.github.cosinekitty.astronomy.EquatorEpoch
import io.github.cosinekitty.astronomy.KM_PER_AU
import io.github.cosinekitty.astronomy.Observer
import io.github.cosinekitty.astronomy.Refraction
import io.github.cosinekitty.astronomy.RotationMatrix
import io.github.cosinekitty.astronomy.StateVector
import io.github.cosinekitty.astronomy.equator
import io.github.cosinekitty.astronomy.horizon
import io.github.cosinekitty.astronomy.rotationEqdHor
import io.github.cosinekitty.astronomy.siderealTime
import java.util.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_FLATTENING = 0.996647180302104
private const val EARTH_EQUATORIAL_RADIUS_KM = 6378.1366
private const val ANGVEL = 7.2921150e-5

// Use astronomy provided once is ready
fun terra(observer: Observer, time: AstroTime): StateVector {
    val st: Double = siderealTime(time)
    val df2: Double = EARTH_FLATTENING * EARTH_FLATTENING
    val phi: Double = observer.latitude * DEG2RAD
    val sinphi: Double = sin(phi)
    val cosphi: Double = cos(phi)
    val c: Double = 1.0 / sqrt(cosphi * cosphi + df2 * sinphi * sinphi)
    val s = df2 * c
    val ht_km = observer.height / 1000.0
    val ach: Double = EARTH_EQUATORIAL_RADIUS_KM * c + ht_km
    val ash: Double = EARTH_EQUATORIAL_RADIUS_KM * s + ht_km
    val stlocl: Double = (15.0 * st + observer.longitude) * DEG2RAD
    val sinst: Double = sin(stlocl)
    val cosst: Double = cos(stlocl)
    return StateVector(
        ach * cosphi * cosst / KM_PER_AU,
        ach * cosphi * sinst / KM_PER_AU,
        ash * sinphi / KM_PER_AU,
        -(ANGVEL * 86400.0 / KM_PER_AU) * ach * cosphi * sinst,
        +(ANGVEL * 86400.0 / KM_PER_AU) * ach * cosphi * cosst,
        0.0,
        time
    )
}

// Use astronomy provided once is ready
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

