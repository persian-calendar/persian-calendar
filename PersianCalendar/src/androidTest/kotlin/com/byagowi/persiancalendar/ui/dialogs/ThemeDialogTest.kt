package com.byagowi.persiancalendar.ui.dialogs

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.settings.interfacecalendar.ThemeDialog
import org.junit.Rule
import org.junit.Test

class ThemeDialogTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun cancelButtonTest() {
        var showDialog = true
        var acceptString = ""
        composeTestRule.setContent {
            acceptString = stringResource(R.string.accept)
            if (showDialog) ThemeDialog { showDialog = false }
        }
        assert(showDialog)
        composeTestRule.onNodeWithText(acceptString)
            .assertHasClickAction()
            .performClick()
        assert(!showDialog)
    }
}
