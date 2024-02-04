package com.byagowi.persiancalendar.ui

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.lifecycle.viewmodel.compose.viewModel
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.map.MapScreen
import org.junit.Rule
import org.junit.Test

class MapScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun mapScreenNavigateUp() {
        var navigateUpIsCalled = false
        var navigateUpString = ""
        composeTestRule.setContent {
            navigateUpString = stringResource(R.string.navigate_up)
            MapScreen({ navigateUpIsCalled = true }, false, viewModel())
        }
        composeTestRule.onNodeWithContentDescription(navigateUpString)
            .assertHasClickAction()
            .performClick()
        assert(navigateUpIsCalled)
    }
}
