package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.ui.map.magneticMapColor
import kotlin.test.Test
import kotlin.test.assertEquals

class MagneticMapColorTest {
    @Test
    fun `opposite angles have equal opacity and distinct colors`() {
        for (angle in listOf(2f, 45f, 90f, 180f)) {
            assertEquals(magneticMapColor(angle) ushr 24, magneticMapColor(-angle) ushr 24)
            assertEquals(0xFF0000, magneticMapColor(angle) and 0xFFFFFF)
            assertEquals(0xFF, magneticMapColor(-angle) and 0xFFFFFF)
        }
        assertEquals(127, magneticMapColor(-90f) ushr 24)
        assertEquals(255, magneticMapColor(-360f) ushr 24)
        assertEquals(0x1E00FF00, magneticMapColor(0f))
    }
}
