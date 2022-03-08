package com.byagowi.persiancalendar.ui

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.about.showEmailDialog
import com.byagowi.persiancalendar.ui.calendar.dialogs.showDayPickerDialog
import com.byagowi.persiancalendar.ui.calendar.dialogs.showMonthOverviewDialog
import com.byagowi.persiancalendar.ui.calendar.shiftwork.showShiftWorkDialog
import com.byagowi.persiancalendar.ui.common.showQrCode
import com.byagowi.persiancalendar.ui.preferences.common.showColorPickerDialog
import com.byagowi.persiancalendar.ui.preferences.interfacecalendar.calendarsorder.showCalendarPreferenceDialog
import com.byagowi.persiancalendar.ui.preferences.interfacecalendar.showHolidaysTypesDialog
import com.byagowi.persiancalendar.ui.preferences.interfacecalendar.showLanguagePreferenceDialog
import com.byagowi.persiancalendar.ui.preferences.locationathan.athan.showAthanGapDialog
import com.byagowi.persiancalendar.ui.preferences.locationathan.athan.showAthanSelectDialog
import com.byagowi.persiancalendar.ui.preferences.locationathan.athan.showAthanVolumeDialog
import com.byagowi.persiancalendar.ui.preferences.locationathan.athan.showPrayerSelectDialog
import com.byagowi.persiancalendar.ui.preferences.locationathan.athan.showPrayerSelectPreviewDialog
import com.byagowi.persiancalendar.ui.preferences.locationathan.location.showCoordinatesDialog
import com.byagowi.persiancalendar.ui.preferences.locationathan.location.showDistrictsDialog
import com.byagowi.persiancalendar.ui.preferences.locationathan.location.showGPSLocationDialog
import com.byagowi.persiancalendar.ui.preferences.locationathan.location.showLocationPreferenceDialog
import com.byagowi.persiancalendar.ui.preferences.locationathan.location.showProvinceDialog
import com.byagowi.persiancalendar.ui.preferences.showIconsDemoDialog
import com.byagowi.persiancalendar.ui.preferences.showTypographyDemoDialog
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DialogsSmokeTest {
    @Test
    fun testDialog() {
        listOf<(MainActivity) -> Unit>(
            { it.showChangeLanguageSnackbar() },
            { it.showAppIsOutDatedSnackbar() },
            { showDayPickerDialog(it, Jdn.today(), R.string.accept) {} },
            { showMonthOverviewDialog(it, Jdn.today().toCalendar(mainCalendar)) },
            { showShiftWorkDialog(it, Jdn.today()) {} },
            { showQrCode(it, "http://example.com") },
            { showIconsDemoDialog(it) },
            { showTypographyDemoDialog(it) },
            { showColorPickerDialog(it, true, "ABC") },
            { showColorPickerDialog(it, false, "ABC") },
            { showHolidaysTypesDialog(it) },
            { showCalendarPreferenceDialog(it) {} },
            { showAthanGapDialog(it) },
            {
                showAthanSelectDialog(it, object : ActivityResultLauncher<Unit>() {
                    override fun launch(input: Unit?, options: ActivityOptionsCompat?) = Unit
                    override fun unregister() = Unit
                    override fun getContract(): ActivityResultContract<Unit, *> = TODO()
                })
            },
            { showAthanVolumeDialog(it) },
            { showPrayerSelectDialog(it) },
            { showPrayerSelectPreviewDialog(it) },
            { showCoordinatesDialog(it, it) },
            { showProvinceDialog(it) },
            { showDistrictsDialog(it, listOf("a", "b", "c")) },
            { showGPSLocationDialog(it, it) },
            { showGPSLocationDialog(it, it) },
            { showEmailDialog(it) },
            { showLanguagePreferenceDialog(it) },
            { showLocationPreferenceDialog(it) },
        ).forEach { launcher ->
            ActivityScenario.launch<MainActivity>(
                Intent(
                    ApplicationProvider.getApplicationContext(), MainActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ).onActivity { activity -> launcher(activity) }
        }
    }
}
