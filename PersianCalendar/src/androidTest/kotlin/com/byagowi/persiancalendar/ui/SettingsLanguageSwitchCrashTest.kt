package com.byagowi.persiancalendar.ui

import android.content.Context
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.global.configureCalendarsAndLoadEvents
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.ui.settings.SettingsScreen
import com.byagowi.persiancalendar.ui.settings.SettingsTab
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.saveLanguage
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsLanguageSwitchCrashTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun switchingLanguageBackAndForthDoesNotCrash() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        composeTestRule.setContent {
            NavigationMock {
                SettingsScreen(
                    openNavigationRail = {},
                    navigateToMap = {},
                    initialTab = SettingsTab.InterfaceCalendar,
                    destination = null,
                    destinationItem = null,
                )
            }
        }

        fun setLanguage(language: Language) {
            composeTestRule.runOnUiThread {
                context.preferences.saveLanguage(language)
                configureCalendarsAndLoadEvents(context)
                updateStoredPreference(context)
            }
            composeTestRule.waitForIdle()
        }

        repeat(2) {
            setLanguage(Language.EN_US)
            setLanguage(Language.FA)
        }
    }
}
