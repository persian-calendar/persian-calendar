package com.byagowi.persiancalendar.ui

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.fragment.app.viewModels
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.ui.converter.ConverterFragment
import com.byagowi.persiancalendar.ui.converter.ViewModel
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
            val model by it.viewModels<ViewModel>()

            // Converter
            assertEquals(model.jdn.value, Jdn.today())
            assertFalse(model.todayButtonVisibility.value)
            model.jdn.value = Jdn.today() + 1
            assertEquals(model.jdn.value, Jdn.today() + 1)
            assertTrue(model.todayButtonVisibility.value)
            model.jdn.value = Jdn.today()
            assertFalse(model.todayButtonVisibility.value)

            // Day distance
            assertFalse(model.isDayDistance.value)
            model.isDayDistance.value = true
            assertTrue(model.isDayDistance.value)
            model.distanceJdn.value = Jdn.today() + 1
            assertTrue(model.todayButtonVisibility.value)
        }
    }
}
