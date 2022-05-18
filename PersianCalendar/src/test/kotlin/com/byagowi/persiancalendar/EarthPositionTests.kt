package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.utils.EarthPosition
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class EarthPositionTests {

    private val cambridge = EarthPosition(52.205, .119)
    private val paris = EarthPosition(48.857, 2.351)

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

    @Test
    fun rectangularBoundsOfRadius() {
        // https://gist.github.com/ebraminio/f20acf3bf605066d069bf313f52e2b68
        val (from, to) = EarthPosition(51.0217, 35.67719).rectangularBoundsOfRadius(.2)
        assertThat(from.latitude).isWithin(1.0e-7).of(51.01990333083999)
        assertThat(from.longitude).isWithin(1.0e-7).of(35.674333728359954)
        assertThat(to.latitude).isWithin(1.0e-7).of(51.023496669160025)
        assertThat(to.longitude).isWithin(1.0e-7).of(35.68004627164005)
    }
}
