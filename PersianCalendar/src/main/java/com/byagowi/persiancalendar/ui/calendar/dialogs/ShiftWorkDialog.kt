package com.byagowi.persiancalendar.ui.calendar.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_RECURS
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_SETTING
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_STARTING_JDN
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.ShiftWorkRecord
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.formatDate
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getDateFromJdnOfCalendar
import com.byagowi.persiancalendar.utils.mainCalendar
import com.byagowi.persiancalendar.utils.shiftWorkRecurs
import com.byagowi.persiancalendar.utils.shiftWorkStartingJdn
import com.byagowi.persiancalendar.utils.shiftWorkTitles
import com.byagowi.persiancalendar.utils.shiftWorks
import com.byagowi.persiancalendar.utils.spacedComma
import com.byagowi.persiancalendar.utils.updateStoredPreference

class ShiftWorkDialogState(jdn: Long) {
    val isDialogOpen = mutableStateOf(true)
    val selectedJdn = mutableStateOf(jdn)
    val body = shiftWorks.toMutableStateList()
    val recurs = mutableStateOf(shiftWorkRecurs)
    val selectedTypeDropdownIndex = mutableStateOf(-1)
    val selectedLengthDropdownIndex = mutableStateOf(-1)
    val isFirstSetup = mutableStateOf(shiftWorkStartingJdn == -1L)

    fun modifyTypeOfRecord(position: Int, newValue: String) {
        body[position] = ShiftWorkRecord(newValue, body[position].length)
    }

    fun modifyLengthOfRecord(position: Int, newValue: Int) {
        body[position] = ShiftWorkRecord(body[position].type, newValue)
    }

    fun ensureBodyIntegrity() {
        ensureBodyStateIntegrity(body)
    }

    fun closeDialog() {
        isDialogOpen.value = false
    }

    companion object {
        // Returns whether any change has occurred which is used only in testing
        fun ensureBodyStateIntegrity(state: MutableList<ShiftWorkRecord>): Boolean {
            if (state.isNotEmpty() && state.filterIndexed { index, it ->
                    (it.length == 0 && it.type.isBlank()) xor (index + 1 == state.size)
                }.isEmpty()) return false
            val newState =
                state.filterNot { it.length == 0 && it.type.isBlank() } + ShiftWorkRecord("", 0)
            state.clear()
            state.addAll(newState)
            return true
        }
    }
}


@Composable
fun ShiftWorkDialog(state: ShiftWorkDialogState, onSuccess: () -> Unit) {
    Surface(color = MaterialTheme.colors.background) {
        if (state.isDialogOpen.value) {
            val confirmText = stringResource(R.string.accept)
            val dismissText = stringResource(R.string.cancel)
            val context = LocalContext.current
            AlertDialog(
                onDismissRequest = { state.closeDialog() },
                shape = RoundedCornerShape(16.dp),
                text = { ShiftWorkDialogContent(state) },
                confirmButton = {
                    TextButton(onClick = {
                        state.closeDialog()
                        context.appPrefs.edit {
                            putLong(
                                PREF_SHIFT_WORK_STARTING_JDN,
                                if (state.body.none { it.length != 0 }) -1L else state.selectedJdn.value
                            )
                            putString(
                                PREF_SHIFT_WORK_SETTING,
                                state.body.filter { it.length != 0 }.joinToString(",") {
                                    "${it.type.replace("=", "").replace(",", "")}=${it.length}"
                                }
                            )
                            putBoolean(PREF_SHIFT_WORK_RECURS, state.recurs.value)
                        }
                        updateStoredPreference(context)
                        onSuccess()
                    }) { Text(confirmText) }
                },
                dismissButton = {
                    TextButton(onClick = { state.closeDialog() }) { Text(dismissText) }
                }
            )
        }
    }
}

@Preview(locale = "fa", showBackground = true)
@Composable
fun ShiftWorkDialogContent(state: ShiftWorkDialogState = ShiftWorkDialogState(-1)) {
    state.ensureBodyIntegrity()

    val shiftWorkLengthHeader = stringResource(R.string.shift_work_days_head)
    val resultTemplate = stringResource(R.string.shift_work_record_title)
    val shiftWorkTitles = stringArrayResource(R.array.shift_work)

    Column(
        Modifier
            .heightIn(0.dp, 500.dp)
            .fillMaxWidth()
    ) {
        Text(
            stringResource(
                if (state.isFirstSetup.value) R.string.shift_work_starting_date
                else R.string.shift_work_starting_date_edit
            ).format(formatDate(getDateFromJdnOfCalendar(mainCalendar, state.selectedJdn.value)))
        )

        Row(Modifier.padding(8.dp)) {
            Checkbox(state.recurs.value, onCheckedChange = { state.recurs.value = it })
            Text(stringResource(R.string.recurs))
        }

        Spacer(Modifier.height(4.dp))
        Button(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(50),
            onClick = { state.body.clear() }
        ) {
            Text(stringResource(R.string.shift_work_reset_button))
        }
        Spacer(Modifier.height(4.dp))

        val recordsToShow = state.body.filter { it.length != 0 }
        if (recordsToShow.isNotEmpty()) {
            Text(
                text = recordsToShow.joinToString(spacedComma) {
                    resultTemplate.format(formatNumber(it.length), shiftWorkKeyToString(it.type))
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(4.dp))
        }

        LazyColumn {
            items(state.body.size) { position ->
                val (type: String, length: Int) = state.body[position]
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "%s:".format(formatNumber(position + 1)),
                        modifier = Modifier.weight(5f)
                    )
                    TextField(
                        modifier = Modifier
                            .onFocusChanged {
                                if (it == FocusState.Active)
                                    state.selectedTypeDropdownIndex.value = position
                                else if (state.selectedTypeDropdownIndex.value == position)
                                    state.selectedTypeDropdownIndex.value = -1
                            }
                            .weight(75f),
                        value = type,
                        onValueChange = { state.modifyTypeOfRecord(position, it) }
                    )
                    DropdownMenu(
                        expanded = state.selectedTypeDropdownIndex.value == position,
                        onDismissRequest = { state.selectedTypeDropdownIndex.value = -1 },
                        offset = DpOffset(20.dp, 0.dp)
                    ) {
                        shiftWorkTitles.forEach { item ->
                            DropdownMenuItem(
                                onClick = {
                                    state.selectedTypeDropdownIndex.value = -1
                                    state.modifyTypeOfRecord(position, item)
                                }
                            ) { Text(item) }
                        }
                    }
                    Spacer(Modifier.width(4.dp))
                    TextField(
                        value = length.toString(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        onValueChange = {
                            state.modifyLengthOfRecord(position, it.toIntOrNull() ?: 0)
                        },
                        modifier = Modifier
                            .weight(20f)
                            .onFocusChanged {
                                if (it == FocusState.Active)
                                    state.selectedLengthDropdownIndex.value = position
                                else if (state.selectedLengthDropdownIndex.value == position)
                                    state.selectedLengthDropdownIndex.value = -1
                            }
                    )
                    DropdownMenu(
                        expanded = state.selectedLengthDropdownIndex.value == position,
                        onDismissRequest = { state.selectedLengthDropdownIndex.value = -1 },
                        offset = DpOffset(220.dp, 0.dp)
                    ) {
                        (0..7).map { length ->
                            DropdownMenuItem(
                                onClick = {
                                    state.selectedLengthDropdownIndex.value = -1
                                    state.modifyLengthOfRecord(position, length)
                                }
                            ) { Text(if (length == 0) shiftWorkLengthHeader else formatNumber(length)) }
                        }
                    }
                }
            }
        }
    }
}

// Don't inline it, utils.shiftWorkTitles is a title dictionary hold for legacy reasons
fun shiftWorkKeyToString(type: String): String = shiftWorkTitles[type] ?: type
