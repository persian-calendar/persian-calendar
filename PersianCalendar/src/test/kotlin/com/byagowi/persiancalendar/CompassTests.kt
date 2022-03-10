package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.ui.compass.CompassFragment
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CompassTests {
    @Test
    fun `isNearToDegree correctness`() {
        assertTrue(CompassFragment.isNearToDegree(360f, 1f))
        assertTrue(CompassFragment.isNearToDegree(1f, 360f))

        assertTrue(CompassFragment.isNearToDegree(2f, 360f))
        assertFalse(CompassFragment.isNearToDegree(3f, 360f))

        assertTrue(CompassFragment.isNearToDegree(360f, 2f))
        assertFalse(CompassFragment.isNearToDegree(360f, 3f))

        assertTrue(CompassFragment.isNearToDegree(180f, 181f))
        assertTrue(CompassFragment.isNearToDegree(180f, 182f))
        assertFalse(CompassFragment.isNearToDegree(180f, 183f))
        assertFalse(CompassFragment.isNearToDegree(180f, 184f))

        assertTrue(CompassFragment.isNearToDegree(181f, 180f))
        assertTrue(CompassFragment.isNearToDegree(182f, 180f))
        assertFalse(CompassFragment.isNearToDegree(183f, 180f))
        assertFalse(CompassFragment.isNearToDegree(184f, 180f))
    }
}
