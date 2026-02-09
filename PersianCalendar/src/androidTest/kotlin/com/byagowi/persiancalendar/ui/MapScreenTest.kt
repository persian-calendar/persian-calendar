package com.byagowi.persiancalendar.ui

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.ui.map.MapScreen
import org.junit.Rule
import org.junit.Test

class MapScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    private val today = Jdn.today()

    @Test
    fun mapScreenNavigateUp() {
        var navigateUpIsCalled = false
        var navigateUpString = ""
        val time = System.currentTimeMillis()
        composeTestRule.setContent {
            navigateUpString = stringResource(R.string.navigate_up)
            NavigationMock {
                MapScreen({ navigateUpIsCalled = true }, false, time, today)
            }
        }
        composeTestRule.onNodeWithContentDescription(navigateUpString)
            .assertHasClickAction()
            .performClick()
        assert(navigateUpIsCalled)
    }
}
