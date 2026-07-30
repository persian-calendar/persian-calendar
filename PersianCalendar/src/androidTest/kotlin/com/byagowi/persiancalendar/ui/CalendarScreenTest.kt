package com.byagowi.persiancalendar.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.byagowi.persiancalendar.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.PREF_ENGLISH_GREGORIAN_PERSIAN_MONTHS
import com.byagowi.persiancalendar.PREF_LOCAL_NUMERAL
import com.byagowi.persiancalendar.PREF_MAIN_CALENDAR_KEY
import com.byagowi.persiancalendar.PREF_OTHER_CALENDARS_KEY
import com.byagowi.persiancalendar.PREF_SECONDARY_CALENDAR_IN_TABLE
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.loadLanguageResources
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.ui.calendar.CalendarScreen
import com.byagowi.persiancalendar.utils.preferences
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalendarScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    private val today = Jdn.today()

    @Test
    fun calendarScreenSmokeTest() {
        composeTestRule.setContent {
            NavigationMock {
                val now = System.currentTimeMillis()
                CalendarScreen(0, {}, null, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, today, now)
            }
        }
    }

//    @Test
//    fun secondaryCalendarTitleDoesNotOverflowAtMaximumFontScale() {
//        val context = InstrumentationRegistry.getInstrumentation().targetContext
//        val preferences = context.preferences
//        val keys = listOf(
//            PREF_APP_LANGUAGE,
//            PREF_ENGLISH_GREGORIAN_PERSIAN_MONTHS,
//            PREF_LOCAL_NUMERAL,
//            PREF_MAIN_CALENDAR_KEY,
//            PREF_OTHER_CALENDARS_KEY,
//            PREF_SECONDARY_CALENDAR_IN_TABLE,
//        )
//        val originalPreferences = keys.associateWith { preferences.all[it] }
//
//        val testResult = runCatching {
//            preferences.edit(commit = true) {
//                putString(PREF_APP_LANGUAGE, "fa")
//                putBoolean(PREF_ENGLISH_GREGORIAN_PERSIAN_MONTHS, true)
//                putBoolean(PREF_LOCAL_NUMERAL, true)
//                putString(PREF_MAIN_CALENDAR_KEY, Calendar.SHAMSI.name)
//                putString(PREF_OTHER_CALENDARS_KEY, Calendar.GREGORIAN.name)
//                putBoolean(PREF_SECONDARY_CALENDAR_IN_TABLE, true)
//            }
//            composeTestRule.runOnUiThread {
//                updateStoredPreference(context)
//                loadLanguageResources(context.resources)
//            }
//
//            val fixedToday = Jdn(Calendar.SHAMSI, 1405, 5, 7)
//            composeTestRule.setContent {
//                val density = LocalDensity.current
//                CompositionLocalProvider(
//                    LocalDensity provides Density(density.density, fontScale = 2f),
//                    LocalLayoutDirection provides LayoutDirection.Rtl,
//                ) {
//                    Box(Modifier.width(412.dp).fillMaxHeight()) {
//                        NavigationMock {
//                            CalendarScreen(
//                                0, {}, null, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, fixedToday, 0,
//                            )
//                        }
//                    }
//                }
//            }
//
//            repeat(6) {
//                composeTestRule.onAllNodesWithContentDescription("ماه قبل")[0].performClick()
//                composeTestRule.waitForIdle()
//            }
//
//            val textLayoutResults = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
//            composeTestRule.onNodeWithText("جنوری–فبروری ۲۰۲۶", useUnmergedTree = true)
//                .performSemanticsAction(SemanticsActions.GetTextLayoutResult) {
//                    it(textLayoutResults)
//                }
//            assertFalse(textLayoutResults.single().hasVisualOverflow)
//        }
//
//        preferences.edit(commit = true) {
//            keys.forEach { key ->
//                when (val value = originalPreferences[key]) {
//                    null -> remove(key)
//                    is Boolean -> putBoolean(key, value)
//                    is String -> putString(key, value)
//                    else -> error("Unsupported preference type for $key")
//                }
//            }
//        }
//        composeTestRule.runOnUiThread {
//            updateStoredPreference(context)
//            loadLanguageResources(context.resources)
//        }
//        testResult.getOrThrow()
//    }
}
