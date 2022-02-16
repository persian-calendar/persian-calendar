package com.byagowi.persiancalendar.ui

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.byagowi.persiancalendar.ui.athan.AthanActivity
import com.byagowi.persiancalendar.ui.preferences.agewidget.AgeWidgetConfigureActivity
import com.byagowi.persiancalendar.ui.preferences.widgetnotification.WidgetConfigurationActivity
import org.junit.Test

class ActivitiesSmokeTest {
    @Test
    fun test() {
        ActivityScenario.launch<MainActivity>(
            Intent(
                ApplicationProvider.getApplicationContext(), MainActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        ActivityScenario.launch<AthanActivity>(
            Intent(
                ApplicationProvider.getApplicationContext(), AthanActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        ActivityScenario.launch<WidgetConfigurationActivity>(
            Intent(
                ApplicationProvider.getApplicationContext(), WidgetConfigurationActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        // Doesn't show anything yet just better than nothing for now
        ActivityScenario.launch<AgeWidgetConfigureActivity>(
            Intent(
                ApplicationProvider.getApplicationContext(), AgeWidgetConfigureActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
