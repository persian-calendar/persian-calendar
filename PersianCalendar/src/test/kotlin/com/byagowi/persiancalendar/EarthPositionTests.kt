package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.entities.EarthPosition
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class EarthPositionTests {

    private val cambridge = EarthPosition(52.205, .119)
    private val paris = EarthPosition(48.857, 2.351)

    @Test
    fun intermediatePoints() {
        // https://www.movable-type.co.uk/scripts/latlong.html
        val points = cambridge.intermediatePoints(paris, 4).toList()
        // include start and end and three points, not the best API yet
        assertEquals(5, points.size)
        assertEquals(51.3721, points[1].latitude, 1.0e-4)
        assertEquals(.7073, points[1].longitude, 1.0e-4)
        assertEquals(50.5363, points[2].latitude, 1.0e-4)
        assertEquals(1.2746, points[2].longitude, 1.0e-4)
    }

    @Test
    fun rectangularBoundsOfRadius() {
        // https://gist.github.com/ebraminio/f20acf3bf605066d069bf313f52e2b68
        val (from, to) = EarthPosition(51.0217, 35.67719).rectangularBoundsOfRadius(.2)
        assertEquals(51.01990333083999, from.latitude, 1.0e-7)
        assertEquals(35.674333728359954, from.longitude, 1.0e-7)
        assertEquals(51.023496669160025, to.latitude, 1.0e-7)
        assertEquals(35.68004627164005, to.longitude, 1.0e-7)
    }
}
