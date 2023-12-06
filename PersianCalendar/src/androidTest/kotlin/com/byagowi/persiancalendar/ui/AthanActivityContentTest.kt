package com.byagowi.persiancalendar.ui

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.byagowi.persiancalendar.FAJR_KEY
import com.byagowi.persiancalendar.ui.athan.AthanActivityContent
import com.byagowi.persiancalendar.utils.getPrayTimeName
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AthanActivityContentTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun clickToExit() {
        var athanName = ""
        var isClicked = false
        composeTestRule.setContent {
            athanName = stringResource(getPrayTimeName(FAJR_KEY))
            AthanActivityContent(prayerKey = FAJR_KEY, cityName = "City") {
                isClicked = true
            }
        }
        assert(!isClicked)
        composeTestRule.onNodeWithText(athanName).performClick()
        assert(isClicked)
    }
}
