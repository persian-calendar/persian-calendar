package com.byagowi.persiancalendar.ui.calendar.shiftwork

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.ShiftWorkRecord
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.shiftWorkTitles
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.ui.utils.SettingsHorizontalPaddingItem
import com.byagowi.persiancalendar.ui.utils.SettingsItemHeight
import com.byagowi.persiancalendar.utils.formatDate
import com.byagowi.persiancalendar.utils.formatNumber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftWorkDialog(
    viewModel: ShiftWorkViewModel,
    selectedJdn: Jdn,
    onDismissRequest: () -> Unit,
    refreshCalendar: () -> Unit,
) {
    BasicAlertDialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.bodyMedium
                ) {
                    ShiftWorkDialogContent(
                        viewModel,
                        selectedJdn,
                        onDismissRequest,
                        refreshCalendar
                    )
                }
            }
        }
    }
}

@Composable
fun ColumnScope.ShiftWorkDialogContent(
    viewModel: ShiftWorkViewModel,
    selectedJdn: Jdn,
    onDismissRequest: () -> Unit,
    navigateCalendarToItself: () -> Unit,
) {
    val context = LocalContext.current
    val isFirstSetup by viewModel.isFirstSetup.collectAsState()
    Text(
        stringResource(
            if (isFirstSetup) R.string.shift_work_starting_date
            else R.string.shift_work_starting_date_edit,
            formatDate(viewModel.startingDate.value.toCalendar(mainCalendar))
        ),
        modifier = Modifier.padding(horizontal = 24.dp),
    )
    val recurs by viewModel.recurs.collectAsState()
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { viewModel.changeRecurs(!recurs) }
            .padding(horizontal = SettingsHorizontalPaddingItem.dp)
            .height(SettingsItemHeight.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = recurs, onCheckedChange = null)
        Spacer(modifier = Modifier.width(SettingsHorizontalPaddingItem.dp))
        Text(stringResource(R.string.recurs))
    }
    TextButton(
        onClick = {
            viewModel.changeStartingDate(selectedJdn)
            viewModel.changeIsFirstSetup(true)
            viewModel.changeShiftWorks(emptyList())
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) { Text(stringResource(R.string.shift_work_reset_button)) }

    val shiftWorkRows by viewModel.shiftWorks.collectAsState()
    val lazyListState = rememberLazyListState()
    var selectedTypeDropdownIndex by remember { mutableIntStateOf(-1) }
    var selectedLengthDropdownIndex by remember { mutableIntStateOf(-1) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    LazyColumn(
        Modifier
            .weight(weight = 1f, fill = false)
            .fillMaxWidth(),
        state = lazyListState,
    ) {
        item {
            val summary = shiftWorkRows.filter { it.length != 0 }.map {
                pluralStringResource(
                    R.plurals.shift_work_record_title,
                    it.length,
                    formatNumber(it.length),
                    shiftWorkKeyToString(it.type)
                )
            }.joinToString(spacedComma)
            if (summary.isNotEmpty()) Text(
                summary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            )
        }
        itemsIndexed(shiftWorkRows) { position, (type, length) ->
            Row(
                modifier = Modifier
                    .height(48.dp)
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.width(16.dp))
                Text(text = formatNumber(position + 1) + spacedColon)
                Box(Modifier.weight(70f)) {
                    TextField(
                        shiftWorkKeyToString(type),
                        onValueChange = { value ->
                            selectedTypeDropdownIndex = -1
                            viewModel.changeShiftWorkTypeOfPosition(
                                position,
                                // Don't allow inserting '=' or ',' as they have special meaning
                                value.replace(Regex("[=,]"), "")
                            )
                        },
                    )
                    IconButton(
                        onClick = { selectedTypeDropdownIndex = position },
                        modifier = Modifier.align(Alignment.CenterEnd),
                    ) { Icon(Icons.Default.ExpandMore, contentDescription = null) }
                    DropdownMenu(
                        expanded = selectedTypeDropdownIndex == position,
                        onDismissRequest = { selectedTypeDropdownIndex = -1 },
                    ) {
                        (shiftWorkTitles.values + language.additionalShiftWorkTitles).forEach {
                            DropdownMenuItem(
                                onClick = {
                                    selectedTypeDropdownIndex = -1
                                    viewModel.changeShiftWorkTypeOfPosition(position, it)
                                },
                                text = { Text(it) }
                            )
                        }
                    }
                }
                Spacer(Modifier.width(4.dp))
                Box(Modifier.weight(30f)) {
                    TextField(
                        value = formatNumber(length),
                        readOnly = true,
                        onValueChange = {
                            selectedTypeDropdownIndex = -1
                            viewModel.changeShiftWorkLengthOfPosition(
                                position,
                                it.toIntOrNull() ?: 0
                            )
                        },
                        modifier = Modifier
                            .onFocusChanged {
                                if (it.hasFocus) selectedLengthDropdownIndex = position
                                else if (selectedLengthDropdownIndex == position)
                                    selectedLengthDropdownIndex = -1
                            }
                            .focusRequester(focusRequester),
                    )
                    DropdownMenu(
                        expanded = selectedLengthDropdownIndex == position,
                        onDismissRequest = {
                            focusManager.clearFocus()
                            selectedLengthDropdownIndex = -1
                        },
                    ) {
                        (0..14).map { length ->
                            DropdownMenuItem(
                                onClick = {
                                    focusManager.clearFocus()
                                    selectedLengthDropdownIndex = -1
                                    viewModel.changeShiftWorkLengthOfPosition(position, length)
                                },
                                text = { Text(formatNumber(length)) }
                            )
                        }
                    }
                }
                IconButton(onClick = { viewModel.removeShiftWorkPosition(position) }) {
                    Icon(
                        imageVector = Icons.Default.RemoveCircleOutline,
                        contentDescription = stringResource(R.string.remove),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(Modifier.width(12.dp))
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
    Row(Modifier.padding(bottom = 16.dp, start = 24.dp, end = 24.dp)) {
        TextButton(onClick = {
            viewModel.changeShiftWorks(
                viewModel.shiftWorks.value + ShiftWorkRecord(shiftWorkKeyToString("r"), 1)
            )
            // TODO: Make it scroll to end?
            // scope.launch {
            //     lazyListState.animateScrollBy(viewModel.shiftWorks.value.size + 1f)
            // }
        }) { Text(stringResource(R.string.add)) }
        Spacer(Modifier.weight(1f))
        TextButton(onClick = { onDismissRequest() }) {
            Text(stringResource(R.string.cancel))
        }
        Spacer(Modifier.width(8.dp))
        TextButton(onClick = {
            onDismissRequest()
            saveShiftWorkState(context, viewModel)
            navigateCalendarToItself()
        }) { Text(stringResource(R.string.accept)) }
    }
}

// Don't inline it, utils.shiftWorkTitles is a titles dictionary hold for legacy reasons
fun shiftWorkKeyToString(type: String): String = shiftWorkTitles[type] ?: type

// Returns whether any change has occurred which is used only in testing, the logic isn't used but
// was once supposed to by removal the remove button
@VisibleForTesting
fun trimEmptyRows(state: MutableList<ShiftWorkRecord>): Boolean {
    if (state.isNotEmpty() && state.filterIndexed { index, it ->
            (it.length == 0 && it.type.isBlank()) xor (index + 1 == state.size)
        }.isEmpty()) return false
    val newState = state.filterNot { it.length == 0 && it.type.isBlank() } + ShiftWorkRecord("", 0)
    state.clear()
    state.addAll(newState)
    return true
}
