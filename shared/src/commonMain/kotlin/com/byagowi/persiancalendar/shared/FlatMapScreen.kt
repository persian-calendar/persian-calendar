package com.byagowi.persiancalendar.shared

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.vector.toPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import io.github.cosinekitty.astronomy.Aberration
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.Direction
import io.github.cosinekitty.astronomy.EquatorEpoch
import io.github.cosinekitty.astronomy.Observer
import io.github.cosinekitty.astronomy.Refraction
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.degreesToRadians
import io.github.cosinekitty.astronomy.elongation
import io.github.cosinekitty.astronomy.equator
import io.github.cosinekitty.astronomy.horizon
import io.github.cosinekitty.astronomy.libration
import io.github.cosinekitty.astronomy.radiansToDegrees
import io.github.cosinekitty.astronomy.searchRiseSet
import io.github.cosinekitty.astronomy.siderealTime
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.yield
import kotlin.coroutines.coroutineContext
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

@Composable
internal fun FlatMapScreen(state: CalendarAppState) {
    val s = state.strings
    var type by remember { mutableStateOf("none") }
    var zoom by remember { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    var grid by remember { mutableStateOf(true) }
    var hour by remember { mutableFloatStateOf(12f) }
    val world = remember { addPathNodes(state.data.worldMap).toPath() }
    val zones = remember { addPathNodes(state.data.timeZonesMap).toPath() }
    val plates = remember { addPathNodes(state.data.tectonicPlates).toPath() }
    var mask by remember { mutableStateOf<List<Pair<Offset, Color>>>(emptyList()) }
    var busy by remember { mutableStateOf(false) }
    val modes = listOf("none", "show_night_mask_label", "moon_visibility", "magnetic_field_strength", "magnetic_declination", "magnetic_inclination", "time_zones", "tectonic_plates", "crescent_evening_visibility_yallop", "crescent_evening_visibility_odeh", "crescent_morning_visibility_yallop", "crescent_morning_visibility_odeh")
    Choice(s["map"], type, modes, { s[it] }) { type = it }
    DateInput(state, state.calendar, state.selected) { state.selected = it }
    Text("UTC ${clockText(hour.toDouble())}")
    Slider(hour, { hour = it }, valueRange = 0f..23.99f)
    LaunchedEffect(type, state.selected, hour) {
        mask = emptyList()
        if (type == "none" || type == "time_zones" || type == "tectonic_plates") return@LaunchedEffect
        busy = true
        try {
            val time = astronomyTime(state.selected, hour.toDouble())
            val result = mutableListOf<Pair<Offset, Color>>()
            val step = if (type.startsWith("crescent")) 5 else 3
            val body = if (type == "moon_visibility") Body.Moon else Body.Sun
            val equatorial = equator(body, time, Observer(0.0, 0.0, 0.0), EquatorEpoch.OfDate, Aberration.Corrected)
            val subLongitude = (equatorial.ra - siderealTime(time)) * 15
            for (x in 0 until 360 step step) {
                coroutineContext.ensureActive()
                for (y in 0 until 180 step step) {
                    val latitude = 90.0 - y - step / 2.0
                    val longitude = x - 180.0 + step / 2.0
                    val color = if (type.startsWith("crescent")) crescentColor(astronomyTime(state.selected, 0.0), latitude, longitude, type.endsWith("yallop"), "evening" in type)
                    else if (type.startsWith("magnetic")) {
                        val field = magneticField(latitude, longitude, 0.0, time.epochMillis())
                        val value = when (type) { "magnetic_declination" -> field.declination / 180; "magnetic_inclination" -> field.inclination / 90; else -> field.strength / 60000 }
                        if (value < 0) Color(0f, .2f, 1f, abs(value).toFloat().coerceIn(0f, .75f)) else Color(1f, .1f, 0f, value.toFloat().coerceIn(0f, .75f))
                    } else {
                        val altitude = asin(sin(latitude.degreesToRadians()) * sin(equatorial.dec.degreesToRadians()) + cos(latitude.degreesToRadians()) * cos(equatorial.dec.degreesToRadians()) * cos((longitude - subLongitude).degreesToRadians())).radiansToDegrees()
                        if (type == "moon_visibility") if (altitude > 0) Color(0x5542A5F5) else Color.Transparent
                        else if (altitude < -18) Color(0x99000022) else if (altitude < 0) Color(0x55000022) else Color.Transparent
                    }
                    if (color != Color.Transparent) result += Offset(x.toFloat(), y.toFloat()) to color
                }
                yield()
            }
            mask = result
        } finally { busy = false }
    }
    if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
    if (type.startsWith("magnetic")) Text(s["magnetic_model"])
    Canvas(Modifier.fillMaxWidth().aspectRatio(2f).clipToBounds()
        .pointerInput(Unit) { detectTransformGestures { _, drag, scale, _ -> zoom = (zoom * scale).coerceIn(1f, 8f); pan += drag } }
        .pointerInput(zoom, pan) {
            detectTapGestures { point ->
            val x = ((point.x - size.width / 2f - pan.x) / zoom + size.width / 2f) / size.width
            val y = ((point.y - size.height / 2f - pan.y) / zoom + size.height / 2f) / size.height
            if (x in 0f..1f && y in 0f..1f) {
                state.prayer = state.prayer.copy(latitude = (90 - y * 180).toDouble(), longitude = (x * 360 - 180).toDouble())
                state.prayer.save(state.platform)
            }
        }
        }) {
        drawRect(Color(0xFF809DB5))
        translate(pan.x, pan.y) {
            scale(zoom, pivot = center) {
            scale(size.width / 5760f, size.height / 2880f, pivot = Offset.Zero) {
                drawPath(world, Color(0xFFFBF8E5))
                if (type == "time_zones") drawPath(zones, Color(0x80393CC4), style = Stroke(4f))
                if (type == "tectonic_plates") drawPath(plates, Color(0x80393CC4), style = Stroke(4f))
                val step = if (type.startsWith("crescent")) 5 else 3
                mask.forEach { (point, color) -> drawRect(color, point * 16f, Size(step * 16f, step * 16f)) }
                if (grid) {
                    for (x in 0..360 step 30) drawLine(Color(0x55808080), Offset(x * 16f, 0f), Offset(x * 16f, 2880f), 2f)
                    for (y in 0..180 step 30) drawLine(Color(0x55808080), Offset(0f, y * 16f), Offset(5760f, y * 16f), 2f)
                }
                drawCircle(Color.Red, 24f / zoom, Offset(((state.prayer.longitude + 180) * 16).toFloat(), ((90 - state.prayer.latitude) * 16).toFloat()))
            }
        }
        }
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = { zoom = (zoom * 1.5f).coerceAtMost(8f) }) { Text("+") }
        TextButton(onClick = { zoom = (zoom / 1.5f).coerceAtLeast(1f); if (zoom == 1f) pan = Offset.Zero }) { Text("−") }
        TextButton(onClick = { zoom = 1f; pan = Offset.Zero }) { Text(s["reset"]) }
        FilterChip(grid, { grid = !grid }, label = { Text(s["grid"]) })
    }
    Text("${s["latitude"]}: ${state.prayer.latitude} · ${s["longitude"]}: ${state.prayer.longitude}")
}

/** Same Yallop/Odeh calculations as Android's MapDraw, independent of Android Canvas. */
internal fun crescentColor(base: Time, latitude: Double, longitude: Double, yallop: Boolean, evening: Boolean): Color {
    val observer = Observer(latitude, longitude, 0.0)
    val time = base.addDays(-longitude / 360)
    val direction = if (evening) Direction.Set else Direction.Rise
    val multiplier = if (evening) 1 else -1
    val sun = searchRiseSet(Body.Sun, observer, direction, time, 1.0) ?: return Color.Transparent
    val moon = searchRiseSet(Body.Moon, observer, direction, time, 1.0) ?: return Color.Transparent
    val lag = (moon.ut - sun.ut) * multiplier
    if (lag < 0) return Color(0x70FF0000)
    val best = sun.addDays(lag * 4 / 9 * multiplier)
    val sunEq = equator(Body.Sun, best, observer, EquatorEpoch.OfDate, Aberration.Corrected)
    val moonEq = equator(Body.Moon, best, observer, EquatorEpoch.OfDate, Aberration.Corrected)
    val sunHor = horizon(best, observer, sunEq.ra, sunEq.dec, Refraction.None)
    val moonHor = horizon(best, observer, moonEq.ra, moonEq.dec, Refraction.None)
    val sd = libration(best).diamDeg * 30
    val sdTopo = sd * (1 + sin(moonHor.altitude.degreesToRadians()) * sin((sd / .27245 / 60).degreesToRadians()))
    val arc = if (yallop) elongation(Body.Moon, best).elongation else sunEq.vec.angleWith(moonEq.vec)
    val arcv = acos((cos(arc.degreesToRadians()) / cos((sunHor.azimuth - moonHor.azimuth).degreesToRadians())).coerceIn(-1.0, 1.0)).radiansToDegrees()
    val width = sdTopo * (1 - cos(arc.degreesToRadians()))
    val correction = 6.3226 * width - .7319 * width.pow(2) + .1018 * width.pow(3)
    return if (yallop) {
        val q = (arcv - 11.8371 + correction) / 10
        when { q > .216 -> Color(0x7F3EFF00); q > -.014 -> Color(0x7F3EFF6D); q > -.160 -> Color(0x7F00FF9E); q > -.232 -> Color(0x7F00FFFA); q > -.293 -> Color(0x7F3C78FF); else -> Color.Transparent }
    } else {
        val v = arcv - 7.1651 + correction
        when { v >= 5.65 -> Color(0x7F3EFF00); v >= 2 -> Color(0x7F00FF9E); v >= -.96 -> Color(0x7F3C78FF); else -> Color.Transparent }
    }
}
