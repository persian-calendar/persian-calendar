package com.byagowi.persiancalendar.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.ShiftWorkRecord
import com.byagowi.persiancalendar.ui.calendar.shiftwork.ShiftWorkDialog
import com.byagowi.persiancalendar.ui.calendar.shiftwork.ShiftWorkDialogContent
import org.junit.Rule
import org.junit.Test

class ShiftWorkDialogTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    private val today = Jdn.today()

    @Test
    fun basicSmokeTest() {
        var acceptString = ""
        composeTestRule.setContent {
            acceptString = stringResource(R.string.accept)
            ShiftWorkDialog(today) {}
        }
        composeTestRule.onNodeWithText(acceptString).assertExists()
    }

    @Test
    fun stateRestorationTest() {
        val restorationTester = StateRestorationTester(composeTestRule)

        restorationTester.setContent {
            ShiftWorkDialogContent(today) {}
            rememberSaveable {
                mutableStateListOf(ShiftWorkRecord("d", 12))
            }.let {}
            rememberSaveable { mutableStateListOf("a") }.let {}
            rememberSaveable { mutableStateListOf(today) }.let {}
            rememberSaveable { mutableStateOf("a") }.let {}
        }

        restorationTester.emulateSavedInstanceStateRestore()
    }
}
