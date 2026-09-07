package com.byagowi.persiancalendar.shared

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.ui.astronomy.LunarAge
import com.byagowi.persiancalendar.ui.astronomy.Tithi
import com.byagowi.persiancalendar.ui.astronomy.houses
import io.github.cosinekitty.astronomy.Aberration
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.EquatorEpoch
import io.github.cosinekitty.astronomy.Observer
import io.github.cosinekitty.astronomy.Refraction
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.degreesToRadians
import io.github.cosinekitty.astronomy.eclipticGeoMoon
import io.github.cosinekitty.astronomy.equator
import io.github.cosinekitty.astronomy.helioVector
import io.github.cosinekitty.astronomy.horizon
import io.github.cosinekitty.astronomy.illumination
import io.github.cosinekitty.astronomy.moonPhase
import io.github.cosinekitty.astronomy.searchGlobalSolarEclipse
import io.github.cosinekitty.astronomy.searchLunarEclipse
import io.github.cosinekitty.astronomy.searchMoonQuarter
import io.github.cosinekitty.astronomy.seasons
import io.github.cosinekitty.astronomy.sunPosition
import io.github.persiancalendar.calendar.CivilDate
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

internal fun astronomyTime(day: Long, hour: Double = 12.0) = Time.fromMillisecondsSince1970((((day - 2440588) * 86400 + hour * 3600) * 1000).toLong())
internal fun Time.epochMillis() = (ut + 10957.5) * 86400000
private val planets = listOf(Body.Sun, Body.Moon, Body.Mercury, Body.Venus, Body.Mars, Body.Jupiter, Body.Saturn, Body.Uranus, Body.Neptune, Body.Pluto)

@Composable
internal fun AstronomyScreen(state: CalendarAppState) {
    val s = state.strings
    var hour by remember { mutableFloatStateOf(12f) }
    DateInput(state, state.calendar, state.selected) { state.selected = it }
    Text("UTC ${clockText(hour.toDouble())}")
    Slider(hour, { hour = it }, valueRange = 0f..23.99f)
    val time = remember(state.selected, hour) { astronomyTime(state.selected, hour.toDouble()) }
    val observer = remember(state.prayer) { Observer(state.prayer.latitude, state.prayer.longitude, state.prayer.elevation) }
    val phase = remember(time) { moonPhase(time) }
    val moon = remember(time) { illumination(Body.Moon, time) }
    Section(s["moon"]) {
        MoonPhaseDrawing(phase, Modifier.size(140.dp))
        Text("${state.numeral.format((moon.phaseFraction * 1000).roundToInt() / 10.0)}%")
        Text("${state.numeral.format((LunarAge.fromDegrees(phase).days * 10).roundToInt() / 10.0)} ${s["day"]}")
        if (state.language == "ne") Text(Tithi.tithiName(time.epochMillis().toLong()))
        val quarter = remember(time) { searchMoonQuarter(time) }
        Text("${listOf("●", "◐", "○", "◑")[quarter.quarter]}  ${state.platform.timeInZone(quarter.time.epochMillis(), state.prayer.zone)}")
    }
    Section(s["astronomy"]) {
        val entries = remember(time, observer) { planets.map { body -> body to equator(body, time, observer, EquatorEpoch.OfDate, Aberration.Corrected) } }
        entries.forEach { (body, position) ->
            val horizontal = horizon(time, observer, position.ra, position.dec, Refraction.Normal)
            Row(Modifier.fillMaxWidth()) {
                Text(s[body.name.lowercase()], Modifier.weight(1f))
                Text("${state.numeral.format((horizontal.azimuth * 10).roundToInt() / 10.0)}° / ${state.numeral.format((horizontal.altitude * 10).roundToInt() / 10.0)}°")
            }
        }
        Text("${s["azimuth"]} / ${s["altitude"]}", style = MaterialTheme.typography.labelMedium)
        val positions = remember(time) { listOf(Body.Mercury, Body.Venus, Body.Earth, Body.Mars, Body.Jupiter, Body.Saturn).map { helioVector(it, time) } }
        val colors = listOf(Color.Gray, Color(0xFFFFC107), Color(0xFF42A5F5), Color(0xFFF44336), Color(0xFFA1887F), Color(0xFFFFB74D))
        val orbitColor = MaterialTheme.colorScheme.outlineVariant
        Canvas(Modifier.widthIn(max = 420.dp).fillMaxWidth().aspectRatio(1f)) {
            val radius = size.minDimension * .45f
            drawCircle(Color(0xFFFFC107), 7.dp.toPx())
            positions.forEachIndexed { index, position ->
                val orbit = radius * (index + 1) / positions.size
                drawCircle(orbitColor, orbit, style = Stroke(1.dp.toPx()))
                val angle = atan2(position.y, position.x)
                drawCircle(colors[index], 5.dp.toPx(), center + Offset((cos(angle) * orbit).toFloat(), (sin(angle) * orbit).toFloat()))
            }
        }
        Text(s["solar_system"], style = MaterialTheme.typography.labelMedium)
    }
    Section(s["seasons"]) {
        val season = remember(state.selected) { seasons(CivilDate(state.selected).year) }
        listOf("spring" to season.marchEquinox, "summer" to season.juneSolstice, "autumn" to season.septemberEquinox, "winter" to season.decemberSolstice).forEach { (key, value) -> Text("${s[key]}: ${state.platform.timeInZone(value.epochMillis(), state.prayer.zone)}") }
    }
    Section(s["zodiac"]) {
        val signs = "aries taurus gemini cancer leo virgo libra scorpio sagittarius capricorn aquarius pisces".split(' ')
        val sunLongitude = remember(time) { sunPosition(time).elon }
        val moonLongitude = remember(time) { eclipticGeoMoon(time).lon }
        Text("${s["sun"]}: ${s[signs[(sunLongitude / 30).toInt().coerceIn(0, 11)]]}")
        Text("${s["moon"]}: ${s[signs[(moonLongitude / 30).toInt().coerceIn(0, 11)]]}")
        val cusps = remember(time, observer) { houses(observer.latitude, observer.longitude, time) }
        cusps.forEachIndexed { index, longitude ->
            Text("${state.numeral.format(index + 1)}: ${if (longitude.isFinite()) state.numeral.format((longitude * 10).roundToInt() / 10.0) + "°" else "—"}")
        }
    }
    Section(s["planetary_hours"]) {
        val today = prayerTimes(state.selected, state.prayer, state.platform)
        val tomorrow = prayerTimes(state.selected + 1, state.prayer, state.platform)
        val sunrise = today.getValue("sunrise")
        val sunset = today.getValue("sunset")
        val nextSunrise = tomorrow.getValue("sunrise") + 24
        val order = listOf(Body.Saturn, Body.Jupiter, Body.Mars, Body.Sun, Body.Venus, Body.Mercury, Body.Moon)
        val starting = listOf(0, 3, 6, 2, 5, 1, 4)[weekday(state.selected)]
        if (sunrise.isFinite() && sunset.isFinite() && nextSunrise.isFinite()) repeat(24) { index ->
            val start = if (index < 12) sunrise else sunset
            val duration = (if (index < 12) sunset - sunrise else nextSunrise - sunset) / 12
            val from = start + index % 12 * duration
            Text("${s[order[(starting + index) % 7].name.lowercase()]}: ${state.numeral.format(clockText(from))}–${state.numeral.format(clockText(from + duration))}")
        } else Text("—")
    }
    Section("${s["solar_eclipse"]} / ${s["lunar_eclipse"]}") {
        val solar = remember(state.selected) { searchGlobalSolarEclipse(astronomyTime(state.selected)) }
        val lunar = remember(state.selected) { searchLunarEclipse(astronomyTime(state.selected)) }
        Text("${s["solar_eclipse"]}: ${solar.kind} · ${state.platform.timeInZone(solar.peak.epochMillis(), state.prayer.zone)}")
        Text("${s["lunar_eclipse"]}: ${lunar.kind} · ${state.platform.timeInZone(lunar.peak.epochMillis(), state.prayer.zone)}")
    }
}

@Composable
internal fun MoonPhaseDrawing(phase: Double, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val r = size.minDimension / 2
        drawCircle(Color(0xFFE0E0D8), r)
        val width = (cos(phase.degreesToRadians()) * r).toFloat()
        val path = Path().apply {
            arcTo(Rect(center.x - abs(width), center.y - r, center.x + abs(width), center.y + r), 90f, if (width > 0) 180f else -180f, true)
            arcTo(Rect(center.x - r, center.y - r, center.x + r, center.y + r), 270f, 180f, false)
            close()
        }
        drawContext.canvas.save()
        if (phase < 180) { drawContext.canvas.translate(center.x, center.y); drawContext.canvas.rotate(180f); drawContext.canvas.translate(-center.x, -center.y) }
        drawPath(path, Color(0xDD202830))
        drawContext.canvas.restore()
    }
}
