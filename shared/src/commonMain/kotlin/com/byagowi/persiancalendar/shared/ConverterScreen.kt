package com.byagowi.persiancalendar.shared

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.persiancalendar.calculator.eval
import io.github.persiancalendar.qr.qr
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
internal fun ConverterScreen(state: CalendarAppState) {
    val s = state.strings
    var mode by remember { mutableStateOf("date_converter") }
    Choice(s["converter"], mode, listOf("date_converter", "days_distance", "calculator", "time_zones", "qr_code"), { s[it] }) { mode = it }
    when (mode) {
        "date_converter", "days_distance" -> {
            var calendar by remember { mutableStateOf(state.calendar) }
            var start by remember { mutableLongStateOf(state.selected) }
            var end by remember { mutableLongStateOf(state.today) }
            Choice(s["calendar"], calendar, CalendarKind.entries, { s[it.titleKey] }) { calendar = it }
            DateInput(state, calendar, start) { start = it }
            if (mode == "days_distance") {
                DateInput(state, calendar, end) { end = it }
                val days = abs(end - start)
                Section(s[mode]) { Text("${state.numeral.formatLongNumber(days)} ${s["day"]}", style = MaterialTheme.typography.headlineMedium); Text("${state.numeral.formatLongNumber(days / 7)} ${s["week"]} + ${state.numeral.formatLongNumber(days % 7)} ${s["day"]}") }
            } else Section(s[mode]) {
                CalendarKind.entries.forEach { type -> SelectionContainer { Text(state.date(start, type)) } }
                TextButton(onClick = { state.platform.copy(CalendarKind.entries.joinToString("\n") { state.date(start, it) }) }) { Text(s["copy"]) }
            }
        }
        "calculator" -> {
            var input by remember { mutableStateOf("") }
            var result by remember { mutableStateOf("") }
            var invalid by remember { mutableStateOf(false) }
            LaunchedEffect(input) {
                delay(180)
                if (input.isBlank()) { result = ""; invalid = false } else try { result = eval(input); invalid = false } catch (_: Exception) { result = s["invalid_input"]; invalid = true }
            }
            OutlinedTextField(input, { if (it.length <= 10_000) input = it }, modifier = Modifier.fillMaxWidth(), minLines = 3, label = { Text(s["calculator"]) }, isError = invalid)
            Section(s["result"]) { SelectionContainer { Text(result, style = MaterialTheme.typography.titleLarge) }; TextButton(onClick = { state.platform.copy(result) }, enabled = !invalid && result.isNotBlank()) { Text(s["copy"]) } }
        }
        "time_zones" -> {
            var time by remember { mutableDoubleStateOf(state.platform.nowMillis()) }
            var zone by remember { mutableStateOf(state.prayer.zone) }
            LaunchedEffect(Unit) { while (true) { delay(30_000); time = state.platform.nowMillis() } }
            Choice(s["time_zones"], zone, state.platform.timeZoneIds()) { zone = it }
            Section(s["time_zones"]) { SelectionContainer { Text(state.numeral.format(state.platform.timeInZone(time, zone)), style = MaterialTheme.typography.titleLarge) }; Text("UTC: ${state.platform.timeInZone(time, "UTC")}") }
        }
        "qr_code" -> {
            var text by remember { mutableStateOf("") }
            var rounded by remember { mutableStateOf(true) }
            val matrix = remember(text) { qr(text) }
            OutlinedTextField(text, { if (it.encodeToByteArray().size <= 2500) text = it }, label = { Text(s["qr_code"]) }, modifier = Modifier.fillMaxWidth())
            if (text.isNotEmpty()) {
                Canvas(Modifier.widthIn(max = 360.dp).fillMaxWidth().aspectRatio(1f).background(Color.White).padding(24.dp)) { drawQr(drawContext.canvas, size.width, matrix, Color.Black, if (rounded) 1f else 0f) }
                Row { Checkbox(rounded, { rounded = it }); Text(s["rounded"]) }
                TextButton(onClick = { state.platform.download("qr.svg", "image/svg+xml", qrSvg(matrix)) }) { Text(s["save"]) }
            }
        }
    }
}

internal fun qrSvg(matrix: List<List<Boolean>>): String = buildString {
    val size = matrix.size + 8
    append("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 $size $size\" shape-rendering=\"crispEdges\"><rect width=\"$size\" height=\"$size\" fill=\"white\"/><g fill=\"black\">")
    matrix.forEachIndexed { x, column -> column.forEachIndexed { y, filled -> if (filled) append("<rect x=\"${x + 4}\" y=\"${y + 4}\" width=\"1\" height=\"1\"/>") } }
    append("</g></svg>")
}
