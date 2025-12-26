package com.byagowi.persiancalendar.ui.calendar.shiftwork

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_RECURS
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_SETTING
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_STARTING_JDN
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.ShiftWorkRecord
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.putJdn

class ShiftWorkViewModel() : ViewModel() {
    var startingDate by mutableStateOf(Jdn.today())
    var recurs by mutableStateOf(true)
    var isFirstSetup by mutableStateOf(true)
    var shiftWorks = mutableStateListOf<ShiftWorkRecord>()

    fun updateItem(position: Int, function: (ShiftWorkRecord) -> ShiftWorkRecord) {
        shiftWorks[position] = function(shiftWorks[position])
    }

    fun persist(context: Context) {
        val result = shiftWorks.filter { it.length != 0 }.joinToString(",") {
            "${it.type.replace("=", "").replace(",", "")}=${it.length}"
        }

        context.preferences.edit {
            if (result.isEmpty()) remove(PREF_SHIFT_WORK_STARTING_JDN)
            else putJdn(PREF_SHIFT_WORK_STARTING_JDN, startingDate)
            putString(PREF_SHIFT_WORK_SETTING, result)
            putBoolean(PREF_SHIFT_WORK_RECURS, recurs)
        }
    }

    companion object {
        fun initiateFromGlobalVariables(selectedJdn: Jdn): ShiftWorkViewModel {
            val viewModel = ShiftWorkViewModel()
            viewModel.shiftWorks.addAll(
                com.byagowi.persiancalendar.global.shiftWorks.takeIf { it.isNotEmpty() } ?: listOf(
                    ShiftWorkRecord(shiftWorkKeyToString("d"), 1)
                ),
            )
            viewModel.isFirstSetup = false
            viewModel.startingDate =
                com.byagowi.persiancalendar.global.shiftWorkStartingJdn ?: viewModel.run {
                    viewModel.isFirstSetup = true
                    selectedJdn
                }
            viewModel.recurs = com.byagowi.persiancalendar.global.shiftWorkRecurs
            return viewModel
        }
    }
}
