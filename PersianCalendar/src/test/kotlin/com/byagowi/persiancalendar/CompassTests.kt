package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.ui.compass.isNearToDegree
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CompassTests {
    @Test
    fun `isNearToDegree correctness`() {
        assertTrue(isNearToDegree(360f, 1f))
        assertTrue(isNearToDegree(1f, 360f))

        assertTrue(isNearToDegree(2f, 360f))
        assertFalse(isNearToDegree(3f, 360f))

        assertTrue(isNearToDegree(360f, 2f))
        assertFalse(isNearToDegree(360f, 3f))

        assertTrue(isNearToDegree(180f, 181f))
        assertTrue(isNearToDegree(180f, 182f))
        assertFalse(isNearToDegree(180f, 183f))
        assertFalse(isNearToDegree(180f, 184f))

        assertTrue(isNearToDegree(181f, 180f))
        assertTrue(isNearToDegree(182f, 180f))
        assertFalse(isNearToDegree(183f, 180f))
        assertFalse(isNearToDegree(184f, 180f))
    }
}
