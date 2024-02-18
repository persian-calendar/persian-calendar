package com.byagowi.persiancalendar.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.settings.interfacecalendar.CountryEvents
import com.byagowi.persiancalendar.ui.settings.interfacecalendar.HolidaysTypesDialog
import com.byagowi.persiancalendar.ui.utils.stringResource
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test

class HolidaysTypesDialogTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun basicSmokeTest() {
        var eventsString = ""
        composeTestRule.setContent {
            eventsString = stringResource(R.string.events)
            HolidaysTypesDialog {}
        }
        composeTestRule.onNodeWithText(eventsString)
    }

    @Test
    fun noDuplicatedEntry() {
        val state = SnapshotStateList<String>()
        composeTestRule.setContent {
            Column {
                CountryEvents("a", "", "h", "n", state, "hk", "nk")
            }
        }
        composeTestRule.onNodeWithText("h").assertHasClickAction().performClick()
        assertEquals(state.toList(), listOf("hk"))
        composeTestRule.onNodeWithText("a").assertHasClickAction().performClick()
        assertEquals(state.toList().size, 2)
        composeTestRule.onNodeWithText("h").assertHasClickAction().performClick()
        assertEquals(state.toList(), listOf("nk"))
        composeTestRule.onNodeWithText("n").assertHasClickAction().performClick()
        assertEquals(state.toList().size, 0)
    }
}
