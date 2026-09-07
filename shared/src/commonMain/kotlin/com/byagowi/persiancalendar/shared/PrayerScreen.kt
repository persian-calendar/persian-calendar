package com.byagowi.persiancalendar.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.praytimes.AsrMethod
import io.github.persiancalendar.praytimes.CalculationMethod
import io.github.persiancalendar.praytimes.Coordinates
import io.github.persiancalendar.praytimes.HighLatitudesMethod
import io.github.persiancalendar.praytimes.MidnightMethod
import io.github.persiancalendar.praytimes.PrayTimes
import kotlin.math.roundToInt

internal data class PrayerPreferences(
    val latitude: Double = 35.6892,
    val longitude: Double = 51.3890,
    val elevation: Double = 1200.0,
    val zone: String = "Asia/Tehran",
    val method: CalculationMethod = CalculationMethod.Tehran,
    val asr: AsrMethod = AsrMethod.Standard,
    val highLatitude: HighLatitudesMethod = HighLatitudesMethod.NightMiddle,
    val midnight: MidnightMethod? = null,
) {
    fun save(platform: AppPlatform) {
        mapOf("latitude" to "$latitude", "longitude" to "$longitude", "elevation" to "$elevation", "zone" to zone, "method" to method.name, "asr" to asr.name, "highLatitude" to highLatitude.name, "midnight" to (midnight?.name ?: "default")).forEach { (key, value) -> platform.writePreference(key, value) }
    }
    companion object {
        fun load(platform: AppPlatform) = PrayerPreferences(
            latitude = platform.readPreference("latitude")?.toDoubleOrNull()?.takeIf { it.isFinite() && it in -90.0..90.0 } ?: 35.6892,
            longitude = platform.readPreference("longitude")?.toDoubleOrNull()?.takeIf { it.isFinite() && it in -180.0..180.0 } ?: 51.3890,
            elevation = platform.readPreference("elevation")?.toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0 } ?: 1200.0,
            zone = platform.readPreference("zone")?.takeIf { it in platform.timeZoneIds() } ?: "Asia/Tehran",
            method = CalculationMethod.entries.firstOrNull { it.name == platform.readPreference("method") } ?: CalculationMethod.Tehran,
            asr = AsrMethod.entries.firstOrNull { it.name == platform.readPreference("asr") } ?: AsrMethod.Standard,
            highLatitude = HighLatitudesMethod.entries.firstOrNull { it.name == platform.readPreference("highLatitude") } ?: HighLatitudesMethod.NightMiddle,
            midnight = MidnightMethod.entries.firstOrNull { it.name == platform.readPreference("midnight") },
        )
    }
}

internal fun prayerTimes(day: Long, preferences: PrayerPreferences, platform: AppPlatform): Map<String, Double> {
    val date = CivilDate(day)
    val times = PrayTimes(preferences.method, date.year, date.month, date.dayOfMonth, platform.offsetHours(day, preferences.zone), Coordinates(preferences.latitude, preferences.longitude, preferences.elevation), preferences.asr, preferences.highLatitude, preferences.midnight)
    return linkedMapOf("fajr" to times.fajr, "sunrise" to times.sunrise, "dhuhr" to times.dhuhr, "asr" to times.asr, "sunset" to times.sunset, "maghrib" to times.maghrib, "isha" to times.isha, "midnight" to times.midnight)
}

internal fun clockText(hours: Double): String {
    if (!hours.isFinite()) return "—"
    // Match Android's Clock: truncate seconds rather than round into the next minute.
    val minutes = (hours * 60).toInt().mod(1440)
    return "${(minutes / 60).toString().padStart(2, '0')}:${(minutes % 60).toString().padStart(2, '0')}"
}

@Composable
internal fun PrayerLocationSettings(state: CalendarAppState) {
    val s = state.strings
    fun update(value: PrayerPreferences) { state.prayer = value; value.save(state.platform) }
    Section(s["location"]) {
        var query by remember { mutableStateOf("") }
        var latitude by remember(state.prayer) { mutableStateOf("${state.prayer.latitude}") }
        var longitude by remember(state.prayer) { mutableStateOf("${state.prayer.longitude}") }
        var elevation by remember(state.prayer) { mutableStateOf("${state.prayer.elevation}") }
        var error by remember { mutableStateOf(false) }
        OutlinedTextField(query, { query = it }, label = { Text(s["city_name"]) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        if (query.isNotBlank()) {
            state.data.cities.filter { city -> city.names.values.any { it.contains(query, ignoreCase = true) } }.take(12).forEach { city ->
                TextButton(onClick = { update(state.prayer.copy(latitude = city.coordinates.latitude, longitude = city.coordinates.longitude, elevation = city.coordinates.elevation)); query = "" }) { Text(city.name(state.language)) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(latitude, { latitude = it }, label = { Text(s["latitude"]) }, isError = error, singleLine = true, modifier = Modifier.weight(1f))
            OutlinedTextField(longitude, { longitude = it }, label = { Text(s["longitude"]) }, isError = error, singleLine = true, modifier = Modifier.weight(1f))
        }
        OutlinedTextField(elevation, { elevation = it }, label = { Text(s["elevation"]) }, isError = error, singleLine = true)
        TextButton(onClick = {
            val lat = state.numeral.parseDouble(latitude); val lon = state.numeral.parseDouble(longitude); val height = state.numeral.parseDouble(elevation)
            error = lat == null || lon == null || height == null || !lat.isFinite() || !lon.isFinite() || !height.isFinite() || lat !in -90.0..90.0 || lon !in -180.0..180.0 || height < 0
            if (!error) update(state.prayer.copy(latitude = requireNotNull(lat), longitude = requireNotNull(lon), elevation = requireNotNull(height)))
        }) { Text(s["accept"]) }
        Choice(s["time_zones"], state.prayer.zone, state.platform.timeZoneIds()) { update(state.prayer.copy(zone = it)) }
        Choice(s["pray_methods"], state.prayer.method, CalculationMethod.entries) { update(state.prayer.copy(method = it)) }
        Choice(s["asr"], state.prayer.asr, AsrMethod.entries) { update(state.prayer.copy(asr = it)) }
        Choice(s["high_latitudes"], state.prayer.highLatitude, HighLatitudesMethod.entries) { update(state.prayer.copy(highLatitude = it)) }
        Choice(s["midnight"], state.prayer.midnight, listOf(null) + MidnightMethod.entries, { it?.name ?: s["default"] }) { update(state.prayer.copy(midnight = it)) }
    }
}

@Composable
internal fun PrayerScreen(state: CalendarAppState) {
    val s = state.strings
    DateInput(state, state.calendar, state.selected) { state.selected = it }
    val times = remember(state.selected, state.prayer) { prayerTimes(state.selected, state.prayer, state.platform) }
    Section(state.date(state.selected)) {
        Text("${state.prayer.latitude}, ${state.prayer.longitude} · ${state.prayer.zone}")
        times.forEach { (key, time) -> Row(Modifier.fillMaxWidth()) { Text(s[key], Modifier.weight(1f)); Text(state.numeral.format(clockText(time)), style = MaterialTheme.typography.titleLarge) } }
        TextButton(onClick = { state.platform.copy(times.entries.joinToString("\n") { "${s[it.key]}: ${state.numeral.format(clockText(it.value))}" }) }) { Text(s["copy"]) }
        TextButton(onClick = {
            val first = state.calendar.monthStart(state.selected)
            val until = state.calendar.monthStart(first, 1)
            val csv = buildString {
                appendLine("Date," + times.keys.joinToString(","))
                for (day in first until until) appendLine(state.date(day) + "," + prayerTimes(day, state.prayer, state.platform).values.joinToString(",", transform = ::clockText))
            }
            state.platform.download("prayer-times.csv", "text/csv;charset=utf-8", "\uFEFF$csv")
        }) { Text(s["month_pray_times"]) }
    }
    PrayerLocationSettings(state)
}
