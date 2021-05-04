package com.byagowi.persiancalendar.ui.calendar.dialogs

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import com.byagowi.persiancalendar.utils.*

@Preview(locale = "fa", showBackground = true)
@Composable
fun ShiftWorkDialog(
    jdn: Long, isDialogOpen: MutableState<Boolean>, onSuccess: () -> Unit
) {
    val selectedJdn = remember { mutableStateOf(jdn) }
    val state = remember { shiftWorks.toMutableStateList() }
    val recurs = remember { mutableStateOf(shiftWorkRecurs) }

    Surface(color = MaterialTheme.colors.background) {
        if (isDialogOpen.value) {
            val confirmText = stringResource(R.string.accept)
            val dismissText = stringResource(R.string.cancel)
            val context = LocalContext.current
            AlertDialog(
                onDismissRequest = { isDialogOpen.value = false },
                shape = RoundedCornerShape(16.dp),
                text = {
                    ShiftWorkDialogList(state, selectedJdn, recurs, shiftWorkStartingJdn == -1L)
                },
                confirmButton = {
                    TextButton(onClick = {
                        isDialogOpen.value = false
                        context.appPrefs.edit {
                            putLong(
                                PREF_SHIFT_WORK_STARTING_JDN,
                                if (state.none { it.length != 0 }) -1L else selectedJdn.value
                            )
                            putString(
                                PREF_SHIFT_WORK_SETTING,
                                state.filter { it.length != 0 }.joinToString(",") {
                                    "${it.type.replace("=", "").replace(",", "")}=${it.length}"
                                }
                            )
                            putBoolean(PREF_SHIFT_WORK_RECURS, recurs.value)
                        }
                        updateStoredPreference(context)
                        onSuccess()
                    }) { Text(confirmText) }
                },
                dismissButton = {
                    TextButton(onClick = { isDialogOpen.value = false }) { Text(dismissText) }
                },
            )
        }
    }
}

// Returns whether any change has occurred which is used only in testing
fun ensureShiftWorkDialogMainStateIntegrity(state: MutableList<ShiftWorkRecord>): Boolean {
    if (state.isNotEmpty() && state.filterIndexed { index, it ->
            (it.length == 0 && it.type.isBlank()) xor (index + 1 == state.size)
        }.isEmpty()) return false
    val newState = state.filterNot { it.length == 0 && it.type.isBlank() } + ShiftWorkRecord("", 0)
    state.clear()
    state.addAll(newState)
    return true
}

fun SnapshotStateList<ShiftWorkRecord>.modifyTypeOfRecord(position: Int, newValue: String) {
    this[position] = ShiftWorkRecord(newValue, this[position].length)
}

fun SnapshotStateList<ShiftWorkRecord>.modifyLengthOfRecord(position: Int, newValue: Int) {
    this[position] = ShiftWorkRecord(this[position].type, newValue)
}

@Preview(locale = "fa", showBackground = true)
@Composable
fun ShiftWorkDialogList(
    state: SnapshotStateList<ShiftWorkRecord> = mutableStateListOf(),
    selectedJdn: MutableState<Long> = mutableStateOf(getTodayJdn()),
    shiftWorkRecurs: MutableState<Boolean> = mutableStateOf(true),
    isFirstSetupInitially: Boolean = true
) {
    ensureShiftWorkDialogMainStateIntegrity(state)

    val selectedTypeDropdownIndex = remember { mutableStateOf(-1) }
    val selectedLengthDropdownIndex = remember { mutableStateOf(-1) }
    val isFirstSetup = remember { mutableStateOf(isFirstSetupInitially) }
    val scrollState = remember { ScrollState(0) }

    val shiftWorkLengthHeader = stringResource(R.string.shift_work_days_head)
    val resultTemplate = stringResource(R.string.shift_work_record_title)
    val shiftWorkTitles = stringArrayResource(R.array.shift_work)

    Column(
        modifier = Modifier
            .heightIn(0.dp, 500.dp)
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {
        Text(
            stringResource(
                if (isFirstSetup.value) R.string.shift_work_starting_date
                else R.string.shift_work_starting_date_edit
            ).format(formatDate(getDateFromJdnOfCalendar(mainCalendar, selectedJdn.value)))
        )

        Row(modifier = Modifier.padding(8.dp)) {
            Checkbox(shiftWorkRecurs.value, onCheckedChange = { shiftWorkRecurs.value = it })
            Text(stringResource(R.string.recurs))
        }

        Spacer(modifier = Modifier.height(4.dp))
        Button(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(50),
            onClick = { state.clear() }
        ) {
            Text(stringResource(R.string.shift_work_reset_button))
            isFirstSetup.value = true
        }
        Spacer(modifier = Modifier.height(4.dp))

        state.forEachIndexed { position: Int, (type: String, length: Int) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "%s:".format(formatNumber(position + 1)),
                    modifier = Modifier.weight(5f)
                )
                TextField(
                    modifier = Modifier
                        .onFocusChanged {
                            if (it == FocusState.Active)
                                selectedTypeDropdownIndex.value = position
                            else if (selectedTypeDropdownIndex.value == position)
                                selectedTypeDropdownIndex.value = -1
                        }
                        .weight(75f),
                    value = type,
                    onValueChange = { state.modifyTypeOfRecord(position, it) }
                )
                DropdownMenu(
                    expanded = selectedTypeDropdownIndex.value == position,
                    onDismissRequest = { selectedTypeDropdownIndex.value = -1 },
                    offset = DpOffset(20.dp, 0.dp)
                ) {
                    shiftWorkTitles.forEach { item ->
                        DropdownMenuItem(
                            onClick = {
                                selectedTypeDropdownIndex.value = -1
                                state.modifyTypeOfRecord(position, item)
                            }
                        ) { Text(item) }
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
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
                                selectedLengthDropdownIndex.value = position
                            else if (selectedLengthDropdownIndex.value == position)
                                selectedLengthDropdownIndex.value = -1
                        }
                )
                DropdownMenu(
                    expanded = selectedLengthDropdownIndex.value == position,
                    onDismissRequest = { selectedLengthDropdownIndex.value = -1 },
                    offset = DpOffset(220.dp, 0.dp)
                ) {
                    (0..7).map { length ->
                        DropdownMenuItem(
                            onClick = {
                                selectedLengthDropdownIndex.value = -1
                                state.modifyLengthOfRecord(position, length)
                            }
                        ) { Text(if (length == 0) shiftWorkLengthHeader else formatNumber(length)) }
                    }
                }
            }
        }

        val recordsToShow = state.filter { it.length != 0 }
        if (recordsToShow.isNotEmpty()) {
            Text(
                text = recordsToShow.joinToString(spacedComma) {
                    resultTemplate.format(formatNumber(it.length), shiftWorkKeyToString(it.type))
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

// Don't inline it, utils.shiftWorkTitles is a title dictionary hold for legacy reasons
fun shiftWorkKeyToString(type: String): String = shiftWorkTitles[type] ?: type
