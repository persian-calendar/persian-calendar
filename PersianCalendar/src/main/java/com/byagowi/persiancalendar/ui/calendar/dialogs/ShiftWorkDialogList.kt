package com.byagowi.persiancalendar.ui.calendar.dialogs

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ReleaseDebugDifference.logDebug
import com.byagowi.persiancalendar.entities.ShiftWorkRecord
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.shiftWorkTitles
import com.byagowi.persiancalendar.utils.spacedComma

fun prepareNewState(state: SnapshotStateList<ShiftWorkRecord>) {
    val newState = state.filterNot { it.length == 0 && it.type.isBlank() } + ShiftWorkRecord("", 0)
    state.clear()
    state.addAll(newState)
    logDebug("ShiftWorkDialogList", "state rewritten")
}

fun SnapshotStateList<ShiftWorkRecord>.modifyTypeOfRecord(position: Int, newValue: String) {
    this[position] = ShiftWorkRecord(newValue, this[position].length)
    prepareNewState(this)
}

fun SnapshotStateList<ShiftWorkRecord>.modifyLengthOfRecord(position: Int, newValue: Int) {
    this[position] = ShiftWorkRecord(this[position].type, newValue)
    prepareNewState(this)
}

@Preview(locale = "fa")
@Composable
fun ShiftWorkDialogList(state: SnapshotStateList<ShiftWorkRecord> = mutableStateListOf()) {
    if (state.isEmpty()) prepareNewState(state)

    val selectedTypeDropdownIndex = remember { mutableStateOf(-1) }
    val selectedLengthDropdownIndex = remember { mutableStateOf(-1) }
    val scrollState = remember { ScrollState(0) }

    val shiftWorkLengthHeader = stringResource(R.string.shift_work_days_head)
    val resultTemplate = stringResource(R.string.shift_work_record_title)
    val shiftWorkTitles = stringArrayResource(R.array.shift_work)

    Column(
        modifier = Modifier
            .heightIn(0.dp, 300.dp)
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {
        state.forEachIndexed { position: Int, (type: String, length: Int) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "%s:".format(formatNumber(position + 1)),
                    modifier = Modifier.width(20.dp)
                )
                TextField(
                    modifier = Modifier
                        .onFocusChanged {
                            if (it == FocusState.Active)
                                selectedTypeDropdownIndex.value = position
                            else if (selectedTypeDropdownIndex.value == position)
                                selectedTypeDropdownIndex.value = -1
                        }
                        .width(180.dp),
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
                        .width(70.dp)
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
