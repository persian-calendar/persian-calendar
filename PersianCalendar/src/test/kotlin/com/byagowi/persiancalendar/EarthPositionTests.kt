package com.byagowi.persiancalendar

import com.cepmuvakkit.times.posAlgo.EarthPosition
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EarthPositionTests {

    private val cambridge = EarthPosition(52.205, .119)
    private val paris = EarthPosition(48.857, 2.351)

    @Test
    fun toEarthHeading() {
        // https://www.movable-type.co.uk/scripts/latlong.html
        val heading = cambridge.toEarthHeading(paris)
        assertThat(heading.metres.toDouble()).isWithin(1.0e2).of(404.3e3)
        assertThat(heading.heading).isWithin(1.0e-1).of(156.2)
    }

    @Test
    fun intermediatePoints() {
        // https://www.movable-type.co.uk/scripts/latlong.html
        val points = cambridge.intermediatePoints(paris, 4)
        // include start and end and three points, not the best API yet
        assertThat(points.size).isEqualTo(5)
        assertThat(points[1].latitude).isWithin(1.0e-4).of(51.3721)
        assertThat(points[1].longitude).isWithin(1.0e-4).of(.7073)
        assertThat(points[2].latitude).isWithin(1.0e-4).of(50.5363)
        assertThat(points[2].longitude).isWithin(1.0e-4).of(1.2746)
    }
}
