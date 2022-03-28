package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.ui.compass.CompassScreen
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CompassTests {
    @Test
    fun `isNearToDegree correctness`() {
        assertTrue(CompassScreen.isNearToDegree(360f, 1f))
        assertTrue(CompassScreen.isNearToDegree(1f, 360f))

        assertTrue(CompassScreen.isNearToDegree(2f, 360f))
        assertFalse(CompassScreen.isNearToDegree(3f, 360f))

        assertTrue(CompassScreen.isNearToDegree(360f, 2f))
        assertFalse(CompassScreen.isNearToDegree(360f, 3f))

        assertTrue(CompassScreen.isNearToDegree(180f, 181f))
        assertTrue(CompassScreen.isNearToDegree(180f, 182f))
        assertFalse(CompassScreen.isNearToDegree(180f, 183f))
        assertFalse(CompassScreen.isNearToDegree(180f, 184f))

        assertTrue(CompassScreen.isNearToDegree(181f, 180f))
        assertTrue(CompassScreen.isNearToDegree(182f, 180f))
        assertFalse(CompassScreen.isNearToDegree(183f, 180f))
        assertFalse(CompassScreen.isNearToDegree(184f, 180f))
    }
}
