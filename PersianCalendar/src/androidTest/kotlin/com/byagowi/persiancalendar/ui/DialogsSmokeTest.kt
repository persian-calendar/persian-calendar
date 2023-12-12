package com.byagowi.persiancalendar.ui

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.byagowi.persiancalendar.ui.settings.common.showColorPickerDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.athan.showPrayerSelectDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.athan.showPrayerSelectPreviewDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.location.showDistrictsDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.location.showProvinceDialog
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class DialogsSmokeTest {
    @Test
    fun testDialog() {
        listOf<(MainActivity) -> Unit>(
            { showColorPickerDialog(it, true, "ABC") },
            { showColorPickerDialog(it, false, "ABC") },
            { showPrayerSelectDialog(it) },
            { showPrayerSelectPreviewDialog(it) },
            { showProvinceDialog(it) },
            { showDistrictsDialog(it, listOf("a", "b", "c")) },
        ).forEach { launcher ->
            ActivityScenario.launch<MainActivity>(
                Intent(
                    ApplicationProvider.getApplicationContext(), MainActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ).onActivity { activity -> launcher(activity) }
        }
    }
}
