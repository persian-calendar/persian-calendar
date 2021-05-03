package com.byagowi.persiancalendar.ui.calendar.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.ShiftWorkRecord
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.shiftWorkTitles
import com.byagowi.persiancalendar.utils.spacedComma

fun List<ShiftWorkRecord>.prepareNewState() =
    this.filterNot { it.length == 0 && it.type.isBlank() } + ShiftWorkRecord("", 0)

fun List<ShiftWorkRecord>.modifyTypeOfRecord(newValue: String, position: Int) =
    this.mapIndexed { i, x -> if (i == position) ShiftWorkRecord(newValue, x.length) else x }
        .prepareNewState()

fun List<ShiftWorkRecord>.modifyLengthOfRecord(newValue: Int, position: Int) =
    this.mapIndexed { i, x -> if (i == position) ShiftWorkRecord(x.type, newValue) else x }
        .prepareNewState()

@Preview(locale = "fa")
@Composable
private fun ShiftWorkDialogList(
    record: List<ShiftWorkRecord> = listOf(
        ShiftWorkRecord("روزکاری", 1),
        ShiftWorkRecord("شب‌کاری", 2),
        ShiftWorkRecord("استراحت", 3),
        ShiftWorkRecord("", 0)
    )
) {
    val state = remember { mutableStateOf(record) }
    val selectedTypeDropdownIndex = remember { mutableStateOf(-1) }
    val selectedLengthDropdownIndex = remember { mutableStateOf(-1) }

    val shiftWorkLengthHeader = stringResource(R.string.shift_work_days_head)
    val resultTemplate = stringResource(R.string.shift_work_record_title)
    val shiftWorkTitles = stringArrayResource(R.array.shift_work)

    Column(modifier = Modifier.fillMaxWidth()) {
        state.value.forEachIndexed { position: Int, (type: String, length: Int) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "%s:".format(formatNumber(position + 1)),
                    modifier = Modifier.width(10.dp)
                )
                TextField(
                    modifier = Modifier.onFocusChanged {
                        if (it == FocusState.Active)
                            selectedTypeDropdownIndex.value = position
                        else if (selectedTypeDropdownIndex.value == position)
                            selectedTypeDropdownIndex.value = -1
                    },
                    value = type,
                    onValueChange = { state.value = state.value.modifyTypeOfRecord(it, position) }
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
                                state.value = state.value.modifyTypeOfRecord(item, position)
                            }
                        ) { Text(item) }
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                TextField(
                    value = length.toString(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    onValueChange = {
                        state.value =
                            state.value.modifyLengthOfRecord(it.toIntOrNull() ?: 0, position)
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
                                state.value = state.value.modifyLengthOfRecord(length, position)
                            }
                        ) { Text(if (length == 0) shiftWorkLengthHeader else formatNumber(length)) }
                    }
                }
            }
        }

        val recordsToShow = state.value.filter { it.length != 0 }
        if (recordsToShow.isNotEmpty()) {
            Text(text = recordsToShow.joinToString(spacedComma) {
                resultTemplate.format(formatNumber(it.length), shiftWorkKeyToString(it.type))
            }, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

fun shiftWorkKeyToString(type: String): String = shiftWorkTitles[type] ?: type
