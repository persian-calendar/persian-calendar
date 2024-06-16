package com.byagowi.persiancalendar.ui.calendar.shiftwork

import android.content.Context
import androidx.core.content.edit
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_RECURS
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_SETTING
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_STARTING_JDN
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.ShiftWorkRecord
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.putJdn

fun saveShiftWorkState(context: Context, viewModel: ShiftWorkViewModel) {
    val result = viewModel.shiftWorks.value.filter { it.length != 0 }.joinToString(",") {
        "${it.type.replace("=", "").replace(",", "")}=${it.length}"
    }

    context.preferences.edit {
        if (result.isEmpty()) remove(PREF_SHIFT_WORK_STARTING_JDN)
        else putJdn(PREF_SHIFT_WORK_STARTING_JDN, viewModel.startingDate.value)
        putString(PREF_SHIFT_WORK_SETTING, result)
        putBoolean(PREF_SHIFT_WORK_RECURS, viewModel.recurs.value)
    }

    updateStoredPreference(context)
}

fun fillViewModelFromGlobalVariables(shiftWorkViewModel: ShiftWorkViewModel, selectedJdn: Jdn) {
    shiftWorkViewModel.changeShiftWorks(
        com.byagowi.persiancalendar.global.shiftWorks
            .takeIf { it.isNotEmpty() } ?: listOf(ShiftWorkRecord(shiftWorkKeyToString("d"), 1))
    )
    shiftWorkViewModel.changeIsFirstSetup(false)
    shiftWorkViewModel.changeStartingDate(
        com.byagowi.persiancalendar.global.shiftWorkStartingJdn ?: shiftWorkViewModel.run {
            shiftWorkViewModel.changeIsFirstSetup(true)
            selectedJdn
        }
    )
    shiftWorkViewModel.changeRecurs(com.byagowi.persiancalendar.global.shiftWorkRecurs)
}
