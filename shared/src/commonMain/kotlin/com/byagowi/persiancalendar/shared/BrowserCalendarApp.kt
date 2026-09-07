package com.byagowi.persiancalendar.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.entities.Numeral
import com.byagowi.persiancalendar.ui.theme.DefaultDarkColorScheme
import com.byagowi.persiancalendar.ui.theme.DefaultLightColorScheme
import io.github.persiancalendar.calendar.IslamicDate
import kotlinx.coroutines.delay

internal class CalendarAppState(val platform: AppPlatform, val data: CalendarData) {
    var language by mutableStateOf(platform.readPreference("language")?.takeIf { it in data.translations } ?: "fa")
    var theme by mutableStateOf(platform.readPreference("theme") ?: "system")
    var numeral by mutableStateOf(Numeral.entries.firstOrNull { it.name == platform.readPreference("numeral") } ?: Numeral.PERSIAN)
    var calendar by mutableStateOf(CalendarKind.entries.firstOrNull { it.name == platform.readPreference("calendar") } ?: CalendarKind.PERSIAN)
    var weekStart by mutableIntStateOf(platform.readPreference("weekStart")?.toIntOrNull()?.takeIf { it in 0..6 } ?: 0)
    var islamicOffset by mutableIntStateOf(platform.readPreference("islamicOffset")?.toIntOrNull()?.coerceIn(-2, 2) ?: 0)
    var ummAlQura by mutableStateOf(platform.readPreference("ummAlQura") == "true")
    var sources by mutableStateOf(platform.readPreference("sources")?.split('|')?.toSet() ?: setOf("Iran", "International"))
    var today by mutableLongStateOf(platform.todayJdn())
    var selected by mutableLongStateOf(today)
    var month by mutableLongStateOf(calendar.monthStart(today))
    var route by mutableStateOf(platform.currentRoute())
    var prayer by mutableStateOf(PrayerPreferences.load(platform))
    init { IslamicDate.islamicOffset = islamicOffset; IslamicDate.useUmmAlQura = ummAlQura }
    val strings get() = AppStrings(data.translations, language)
    fun save(key: String, value: String) = platform.writePreference(key, value)
    fun select(day: Long) { selected = day; month = calendar.monthStart(day) }
    fun events(day: Long) = data.eventsOn(day, sources)
    fun date(day: Long, type: CalendarKind = calendar) = strings.date(day, type, numeral)
}

@Composable
fun BrowserCalendarApp(platform: AppPlatform) {
    var data by remember { mutableStateOf<CalendarData?>(null) }
    var failed by remember { mutableStateOf(false) }
    var attempt by remember { mutableIntStateOf(0) }
    LaunchedEffect(attempt) {
        failed = false
        try { data = CalendarData.load() } catch (_: Exception) { failed = true }
    }
    val loaded = data
    if (loaded == null) {
        MaterialTheme {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (failed) Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("دریافت اطلاعات تقویم ممکن نشد · Could not load calendar data")
                    Button(onClick = { attempt++ }) { Text("تلاش دوباره · Retry") }
                } else CircularProgressIndicator()
            }
        }
        return
    }
    val state = remember(platform, loaded) { CalendarAppState(platform, loaded) }
    val strings = state.strings
    LaunchedEffect(state.language) { platform.setLanguage(state.language, strings.isRtl) }
    val dark = state.theme == "dark" || state.theme == "black" || (state.theme == "system" && isSystemInDarkTheme())
    var scheme = if (dark) DefaultDarkColorScheme else DefaultLightColorScheme
    if (state.theme == "black") scheme = scheme.copy(background = Color.Black, surface = Color.Black)
    if (state.theme == "aqua") scheme = scheme.copy(primary = Color(0xFF00838F))
    MaterialTheme(colorScheme = scheme) {
        CompositionLocalProvider(LocalLayoutDirection provides if (strings.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
            LaunchedEffect(state) { while (true) { delay(30_000); state.today = platform.todayJdn() } }
            DisposableEffect(platform) {
                val remove = platform.observeNavigation { state.route = it }
                onDispose(remove)
            }
            Surface(Modifier.fillMaxSize()) {
                Column {
                    Row(Modifier.fillMaxWidth().background(scheme.primaryContainer).padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(strings["app_name"], style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                        TextButton(onClick = { state.select(state.today); platform.navigate("calendar") }) { Text(strings["today"]) }
                    }
                    val destinations = listOf("calendar" to "calendar", "converter" to "converter", "prayer" to "pray_times", "astronomy" to "astronomy", "map" to "map", "settings" to "settings", "about" to "about")
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        destinations.forEach { (route, key) ->
                            FilterChip(selected = state.route == route, onClick = { platform.navigate(route) }, label = { Text(strings[key]) })
                        }
                    }
                    HorizontalDivider()
                    key(state.route) {
                        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Column(Modifier.widthIn(max = 1080.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                when (state.route) {
                                    "calendar" -> CalendarScreen(state)
                                    "converter" -> ConverterScreen(state)
                                    "prayer" -> PrayerScreen(state)
                                    "astronomy" -> AstronomyScreen(state)
                                    "map" -> FlatMapScreen(state)
                                    "settings" -> SettingsScreen(state)
                                    "about" -> AboutScreen(state)
                                    else -> { Text(strings["not_supported_action"]); TextButton(onClick = { platform.navigate("calendar") }) { Text(strings["calendar"]) } }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
internal fun <T> Choice(label: String, value: T, options: List<T>, title: (T) -> String = { it.toString() }, onSelect: (T) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { open = true }) { Text("$label: ${title(value)} ▾") }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }, modifier = Modifier.heightIn(max = 320.dp)) {
            options.forEach { option -> DropdownMenuItem(text = { Text(title(option)) }, onClick = { onSelect(option); open = false }) }
        }
    }
}

@Composable
private fun CalendarScreen(state: CalendarAppState) {
    val s = state.strings
    var mode by remember { mutableStateOf("month") }
    var search by remember { mutableStateOf("") }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = { if (mode == "week") state.select(state.selected - 7) else state.month = state.calendar.monthStart(state.month, -1) }) { Text("‹") }
        Text(state.date(state.month).substringAfter(' '), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
        TextButton(onClick = { if (mode == "week") state.select(state.selected + 7) else state.month = state.calendar.monthStart(state.month, 1) }) { Text("›") }
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Choice(s["calendar"], state.calendar, CalendarKind.entries, { s[it.titleKey] }) { state.calendar = it; state.month = it.monthStart(state.selected); state.save("calendar", it.name) }
        Choice(s["view"], mode, listOf("month", "week", "schedule"), { s[if (it == "schedule") it else "${it}_view"] }) { mode = it }
        TextButton(onClick = { state.platform.download("calendar.html", "text/html;charset=utf-8", calendarReport(state)) }) { Text(s["print"]) }
    }
    DateInput(state, state.calendar, state.selected, onSelect = { state.select(it) })
    if (mode != "schedule") {
        val monthDate = state.calendar.date(state.month)
        val start = if (mode == "week") state.selected - (weekday(state.selected) - state.weekStart).mod(7)
            else state.month - (weekday(state.month) - state.weekStart).mod(7)
        val count = if (mode == "week") 7 else ((state.calendar.monthLength(monthDate.year, monthDate.month) + (state.month - start).toInt() + 6) / 7) * 7
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(8.dp)) {
                Row {
                    repeat(7) { column -> Box(Modifier.weight(1f).height(40.dp), contentAlignment = Alignment.Center) { Text(s[weekdayKeys[(state.weekStart + column) % 7] + "_short"]) } }
                }
                repeat(count / 7) { row ->
                    Row {
                        repeat(7) { column ->
                            val day = start + row * 7 + column
                            val date = state.calendar.date(day)
                            val events = state.events(day)
                            val holiday = weekday(day) == 6 || events.any { it.holiday }
                            val selected = day == state.selected
                            val color = if (selected) MaterialTheme.colorScheme.onPrimary else if (holiday) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            Surface(onClick = { state.selected = day }, shape = CircleShape,
                                color = if (selected) MaterialTheme.colorScheme.primary else if (day == state.today) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                                modifier = Modifier.weight(1f).heightIn(min = 56.dp)) {
                                Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(state.numeral.format(date.dayOfMonth), color = color.copy(alpha = if (mode == "month" && date.month != monthDate.month) .45f else 1f), style = MaterialTheme.typography.titleLarge)
                                    Text(if (events.isNotEmpty()) "•" else " ", color = color, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    Section(s["date"]) {
        CalendarKind.entries.forEach { type -> Text(state.date(state.selected, type), style = if (type == state.calendar) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge) }
        Text(s[weekdayKeys[weekday(state.selected)]])
        TextButton(onClick = { state.platform.share(CalendarKind.entries.joinToString("\n") { state.date(state.selected, it) }) }) { Text(s["share"]) }
    }
    OutlinedTextField(search, { search = it }, label = { Text(s["search_in_events"]) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
    Section(s["events"]) {
        val days = if (mode == "schedule" || search.isNotBlank()) state.month until state.calendar.monthStart(state.month, 1) else state.selected..state.selected
        var found = false
        days.forEach { day ->
            state.events(day).filter { it.title.contains(search, ignoreCase = true) }.forEach { event ->
            found = true
            TextButton(onClick = { state.selected = day }) {
                Text("${if (days.count() > 1) state.date(day) + " — " else ""}${event.title}", color = if (event.holiday) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
            }
        }
        }
        if (!found) Text(s["no_event"])
    }
}

@Composable
internal fun DateInput(state: CalendarAppState, type: CalendarKind, day: Long, onSelect: (Long) -> Unit) {
    val date = type.date(day)
    var year by remember(day, type) { mutableStateOf(date.year.toString()) }
    var month by remember(day, type) { mutableStateOf(date.month.toString()) }
    var dayText by remember(day, type) { mutableStateOf(date.dayOfMonth.toString()) }
    var error by remember { mutableStateOf(false) }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        listOf(Triple("year", year, { text: String -> year = text }), Triple("month", month, { text: String -> month = text }), Triple("day", dayText, { text: String -> dayText = text })).forEach { (key, value, change) ->
            OutlinedTextField(value, change, label = { Text(state.strings[key]) }, singleLine = true, isError = error, modifier = Modifier.weight(1f))
        }
        TextButton(onClick = {
            try {
                fun parse(text: String) = requireNotNull(state.numeral.parseDouble(text)?.takeIf { it % 1 == 0.0 }?.toInt()) { "Invalid number" }
                onSelect(type.validDate(parse(year), parse(month), parse(dayText)).toJdn()); error = false
            } catch (_: Exception) { error = true }
        }) { Text(state.strings["accept"]) }
    }
}

@Composable
private fun SettingsScreen(state: CalendarAppState) {
    val s = state.strings
    Section(s["interface"]) {
        Choice(s["language"], state.language, s.languages) { state.language = it; state.save("language", it) }
        Choice(s["theme"], state.theme, listOf("system", "light", "dark", "black", "aqua"), { s["theme_${if (it == "system") "default" else it}"] }) { state.theme = it; state.save("theme", it) }
        Choice(s["numeral"], state.numeral, Numeral.entries, { it.format("0123456789") }) { state.numeral = it; state.save("numeral", it.name) }
        Choice(s["week_start"], state.weekStart, (0..6).toList(), { s[weekdayKeys[it]] }) { state.weekStart = it; state.save("weekStart", "$it") }
    }
    Section(s["hijri_calendar"]) {
        Choice(s["islamic_offset"], state.islamicOffset, (-2..2).toList()) { IslamicDate.islamicOffset = it; state.islamicOffset = it; state.save("islamicOffset", "$it") }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(state.ummAlQura, { IslamicDate.useUmmAlQura = it; state.ummAlQura = it; state.save("ummAlQura", "$it") }); Text("Umm al-Qura")
        }
    }
    Section(s["events"]) {
        state.data.events.map { it.source }.distinct().forEach { source ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(source in state.sources, { enabled -> state.sources = if (enabled) state.sources + source else state.sources - source; state.save("sources", state.sources.joinToString("|")) })
                Text(source)
            }
        }
    }
    PrayerLocationSettings(state)
}

@Composable
private fun AboutScreen(state: CalendarAppState) {
    val s = state.strings
    Section(s["about"]) {
        Text(s["app_name"], style = MaterialTheme.typography.headlineSmall)
        Text("2012–2026 · Android Persian Calendar Developers")
        SelectionContainer { Text("https://github.com/persian-calendar/persian-calendar") }
    }
    var help by remember { mutableStateOf(false) }
    var licenses by remember { mutableStateOf(false) }
    TextButton(onClick = { help = !help }) { Text(s["help"]) }
    if (help) SelectionContainer { Text(state.data.help) }
    TextButton(onClick = { licenses = !licenses }) { Text(s["licenses"]) }
    if (licenses) SelectionContainer { Text(state.data.licenses) }
}
