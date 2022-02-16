package com.byagowi.persiancalendar.ui

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.ui.converter.ConverterFragment
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConverterFragmentTest {
    @Test
    fun testTodayButtonVisibility() {
        launchFragmentInContainer<ConverterFragment>(themeResId = R.style.LightTheme).onFragment {
            // Converter
            assertEquals(it.model.jdn.value, Jdn.today())
            assertFalse(it.model.todayButtonVisibility.value)
            it.model.jdn.value = Jdn.today() + 1
            assertEquals(it.model.jdn.value, Jdn.today() + 1)
            assertTrue(it.model.todayButtonVisibility.value)
            it.model.jdn.value = Jdn.today()
            assertFalse(it.model.todayButtonVisibility.value)

            // Day distance
            assertFalse(it.model.isDayDistance.value)
            it.model.isDayDistance.value = true
            assertTrue(it.model.isDayDistance.value)
            it.model.distanceJdn.value = Jdn.today() + 1
            assertTrue(it.model.todayButtonVisibility.value)
        }
    }
}
