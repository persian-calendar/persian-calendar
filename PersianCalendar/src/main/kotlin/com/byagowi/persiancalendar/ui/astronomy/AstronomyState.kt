package com.byagowi.persiancalendar.ui.astronomy

import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.utils.sunlitSideMoonTiltAngle
import com.byagowi.persiancalendar.utils.toObserver
import io.github.cosinekitty.astronomy.Aberration
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.EquatorEpoch
import io.github.cosinekitty.astronomy.Refraction
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.eclipticGeoMoon
import io.github.cosinekitty.astronomy.equator
import io.github.cosinekitty.astronomy.equatorialToEcliptic
import io.github.cosinekitty.astronomy.geoVector
import io.github.cosinekitty.astronomy.helioVector
import io.github.cosinekitty.astronomy.horizon
import io.github.cosinekitty.astronomy.sunPosition

class AstronomyState(timeInMillis: Long) {
    private val time = Time.fromMillisecondsSince1970(timeInMillis)
    val sun = sunPosition(time)
    val moon = eclipticGeoMoon(time)
    private val observer by lazy(LazyThreadSafetyMode.NONE) { coordinates?.toObserver() }
    val moonTilt by lazy(LazyThreadSafetyMode.NONE) {
        observer?.let { observer -> sunlitSideMoonTiltAngle(time, observer).toFloat() }
    }
    val sunAltitude by lazy(LazyThreadSafetyMode.NONE) {
        val observer = observer ?: return@lazy null
        val sunEquator =
            equator(Body.Sun, time, observer, EquatorEpoch.OfDate, Aberration.Corrected)
        horizon(time, observer, sunEquator.ra, sunEquator.dec, Refraction.Normal).altitude
    }
    val moonAltitude by lazy(LazyThreadSafetyMode.NONE) {
        val observer = observer ?: return@lazy null
        val moonEquator =
            equator(Body.Moon, time, observer, EquatorEpoch.OfDate, Aberration.Corrected)
        horizon(time, observer, moonEquator.ra, moonEquator.dec, Refraction.Normal).altitude
    }
    val heliocentricPlanets by lazy(LazyThreadSafetyMode.NONE) {
        heliocentricPlanetsList.map { equatorialToEcliptic(helioVector(it, time)) }
    }
    val geocentricPlanets by lazy(LazyThreadSafetyMode.NONE) {
        geocentricPlanetsList.map {
            equatorialToEcliptic(geoVector(it, time, Aberration.Corrected))
        }
    }

    companion object {
        val heliocentricPlanetsList = listOf(
            Body.Mercury,
            Body.Venus,
            Body.Earth,
            Body.Mars,
            Body.Jupiter,
            Body.Saturn,
            Body.Uranus,
            Body.Neptune,
        )
    }
}
