package com.byagowi.persiancalendar.ui.astronomy

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.AU_IN_KM
import com.byagowi.persiancalendar.LRM
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.utils.SettingsHorizontalPaddingItem
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.titleStringId
import io.github.cosinekitty.astronomy.Aberration
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.eclipticGeoMoon
import io.github.cosinekitty.astronomy.equatorialToEcliptic
import io.github.cosinekitty.astronomy.geoVector
import io.github.cosinekitty.astronomy.seasons
import io.github.cosinekitty.astronomy.siderealTime
import io.github.cosinekitty.astronomy.sunPosition
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.praytimes.Coordinates
import java.util.Date
import java.util.Locale
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.tan

private fun formatAngle(value: Double): String {
    val degrees = floor(value)
    return "${degrees.toInt()}°${((value - degrees) * 60).roundToInt()}’"
}

private fun longitudeAndDistanceOfBody(body: Body, time: Time): Pair<Double, Double> {
    return when (body) {
        Body.Sun -> sunPosition(time).let { it.elon to it.vec.length() }
        Body.Moon -> eclipticGeoMoon(time).let { it.lon to it.dist }
        else -> equatorialToEcliptic(geoVector(body, time, Aberration.Corrected))
            .let { it.elon to it.vec.length() }
    }
}

private val bodies = listOf(
    Body.Sun, Body.Moon, Body.Mercury, Body.Venus, Body.Mars, Body.Jupiter,
    Body.Saturn, Body.Uranus, Body.Neptune, Body.Pluto
)

@Composable
fun HoroscopeDialog(date: Date = Date(), onDismissRequest: () -> Unit) {
    val time = Time.fromMillisecondsSince1970(date.time)
    AppDialog(onDismissRequest = onDismissRequest) {
        Text(
            bodies.map { body ->
                val (longitude, distance) = longitudeAndDistanceOfBody(body, time)
                stringResource(body.titleStringId) + ": %s%s %s %,d km".format(
                    Locale.ENGLISH,
                    LRM,
                    formatAngle(longitude % 30), // Remaining angle
                    Zodiac.fromTropical(longitude).emoji,
                    (distance * AU_IN_KM).roundToLong()
                )
            }.joinToString("\n"),
            modifier = Modifier.padding(SettingsHorizontalPaddingItem.dp),
        )
        val coordinates by coordinates.collectAsState()
        coordinates?.let { AscendantZodiac(time, it, isYearEquinox = false) }
    }
}

private val easternHoroscopePositions = listOf(
    /* 1*/1f / 2 - 1f / 6 to 1f / 2 - 1f / 3,
    /* 2*/1f / 2 - 1f / 6 - 1f / 6 to 1f / 2 - 1f / 3 - 1f / 6,
    /* 3*/1f / 2 - 1f / 3 - 1f / 6 to 1f / 2 - 1f / 3,
    /* 4*/1f / 2 - 1f / 3 to 1f / 2 - 1f / 6,
    /* 5*/1f / 2 - 1f / 3 - 1f / 6 to 1f / 2,
    /* 6*/1f / 2 - 1f / 3 to 1f / 2 + 1f / 6,
    /* 7*/1f / 2 - 1f / 6 to 1f / 2,
    /* 8*/1f / 2 to 1f / 2 + 1f / 6,
    /* 9*/1f / 2 - 1f / 6 + 1f / 3 to 1f / 2,
    /*10*/1f / 2 to 1f / 2 - 1f / 3 + 1f / 6,
    /*11*/1f / 2 - 1f / 6 + 1f / 3 to 1f / 2 - 1f / 3,
    /*12*/1f / 2 to 1f / 2 - 1f / 3 - 1f / 6,
)

@Composable
private fun EasternHoroscopePattern(cellLabel: (Int) -> String) {
    val outline = MaterialTheme.colorScheme.outline
    val textDirection = LocalLayoutDirection.current
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        ) {
            easternHoroscopePositions.forEachIndexed { i, (x, y) ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .absoluteOffset(this.maxWidth * x, this.maxHeight * y)
                        .size(this.maxWidth / 3),
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides textDirection) {
                        Text(cellLabel(i), textAlign = TextAlign.Center)
                    }
                }
            }
            Canvas(Modifier.fillMaxSize()) {
                val oneDp = 1.dp.toPx()
                val sizePx = size.width
                val c0 = Offset(0f, sizePx / 2)
                val c1 = Offset(sizePx / 6, sizePx / 2)
                val c2 = Offset(sizePx / 2, sizePx / 6)
                val c3 = Offset(sizePx / 6 + 3 * oneDp, sizePx / 2)
                val c4 = Offset(sizePx / 2, sizePx / 6 + 3 * oneDp)
                val c5 = Offset(sizePx / 2, sizePx / 2)
                (0..3).forEach {
                    rotate(it * 90f) {
                        drawLine(outline, Offset.Zero, c5, oneDp)
                        drawLine(outline, c0, c1, oneDp)
                        drawLine(outline, c1, c2, oneDp)
                        drawLine(outline, c3, c4, oneDp)
                    }
                }
            }
        }
    }
}

// See the following for more information:
// * https://github.com/user-attachments/assets/5f42c377-3f39-4000-b79c-08cbbf76fc07
// * https://github.com/user-attachments/assets/21eadf3f-c780-470d-91b0-a0e504689198
// See for example: https://w.wiki/E9uz
// See also: https://agnastrology.ir/بهینه-سازی-فروش/
@Composable
fun YearHoroscopeDialog(persianYear: Int, onDismissRequest: () -> Unit) {
    val language by language.collectAsState()
    val resources = LocalContext.current.resources
    val items = (0..<12).map {
        val date = PersianDate(persianYear + it, 1, 1)
        ChineseZodiac.fromPersianCalendar(date).format(
            resources = resources,
            withEmoji = true,
            persianDate = date,
            withOldEraName = language.isUserAbleToReadPersian,
            separator = "\n",
        )
    }
    AppDialog(onDismissRequest = onDismissRequest) appDialog@{
        EasternHoroscopePattern { items[it] }
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
        val gregorianYear = CivilDate(PersianDate(persianYear, 1, 1)).year
        Text(
            if (language.isUserAbleToReadPersian) {
                "لحظهٔ تحویل سال " + formatNumber(persianYear) + " در تهران"
            } else "Tehran, March equinox of " + formatNumber(gregorianYear),
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
        val time = seasons(gregorianYear).marchEquinox
        // So the user would be able to verify it with the calendar book published
        val tehran = Coordinates(35.68, 51.42, 0.0)
        AscendantZodiac(time, tehran, isYearEquinox = true)
    }
}

@Composable
private fun ColumnScope.AscendantZodiac(time: Time, coordinates: Coordinates, isYearEquinox: Boolean) {
    val bodiesZodiac = bodies.filter {
        // Sun has fixed place, no point on showing that for year zodiac
        it != Body.Sun || !isYearEquinox
    }.filter {
        it != Body.Neptune && it != Body.Pluto // Not visible to naked eye
    }.map { body ->
        val (longitude, _) = longitudeAndDistanceOfBody(body, time)
        body to longitude
    }.groupBy { (_, longitude) -> Zodiac.fromTropical(longitude) }
    val ascendant = calculateAscendant(coordinates.latitude, coordinates.longitude, time)
    val ascendantZodiac = Zodiac.fromTropical(ascendant)
    val resources = LocalContext.current.resources
    EasternHoroscopePattern {
        val zodiac = Zodiac.entries[(it + ascendantZodiac.ordinal) % 12]
        zodiac.format(
            resources,
            delim = "\n",
            withEmoji = true,
            short = true,
        ) + bodiesZodiac[zodiac]?.joinToString("\n") { (body, longitude) ->
            val title = formatNumber(formatAngle(longitude % 30))
            "${resources.getString(body.titleStringId)}: $LRM$title$LRM"
        }?.let { "\n" + it }.orEmpty()
    }
    Text(
        "$LRM${ascendantZodiac.emoji} ${formatNumber(formatAngle(ascendant % 30))}$LRM",
        modifier = Modifier.align(Alignment.CenterHorizontally),
    )
}

// Extracted from not exposed calculations of astronomy library
private fun eclipticObliquity(time: Time): Double {
    val t = time.tt / 36525.0
    val asec = ((((-0.0000000434 * t
            - 0.000000576) * t
            + 0.00200340) * t
            - 0.0001831) * t
            - 46.836769) * t + 84381.406
    val mobl = asec / 3600
    val tobl = mobl + (time.nutationEps() / 3600)
    return Math.toRadians(tobl)
}

// As https://github.com/cosinekitty/astronomy/discussions/340#discussioncomment-8966532
@VisibleForTesting
fun calculateAscendant(latitude: Double, longitude: Double, time: Time): Double {
    val localSiderealRadians = localSiderealTimeRadians(longitude, time)
    val eclipticObliquity = eclipticObliquity(time)
    val x = sin(localSiderealRadians) * cos(eclipticObliquity) +
            tan(Math.toRadians(latitude)) * sin(eclipticObliquity)
    val y = -1 * cos(localSiderealRadians)
    val celestialLongitudeRadians = atan(y / x)
    var ascendantDegrees = Math.toDegrees(celestialLongitudeRadians)

    // Correcting the quadrant
    ascendantDegrees += if (x < 0) 180.0 else 360.0
    if (ascendantDegrees < 180.0) ascendantDegrees += 180.0 else ascendantDegrees -= 180.0
    return ascendantDegrees
}

@VisibleForTesting
fun calculateMidheaven(longitude: Double, time: Time): Double {
    val localSiderealRadians = localSiderealTimeRadians(longitude, time);
    val eclipticObliquity = eclipticObliquity(time)
    val numerator = tan(localSiderealRadians)
    val denominator = cos(Math.toRadians(eclipticObliquity))
    var midheavenDegrees = Math.toDegrees(atan(numerator / denominator))

    // Correcting the quadrant
    if (midheavenDegrees < 0) midheavenDegrees += 360
    val localSiderealDegrees = Math.toDegrees(localSiderealRadians)
    if (midheavenDegrees > localSiderealDegrees) midheavenDegrees -= 180
    if (midheavenDegrees < 0) midheavenDegrees += 180
    if (midheavenDegrees < 180 && localSiderealDegrees >= 180) midheavenDegrees += 180
    midheavenDegrees = (midheavenDegrees + 360) % 360
    return midheavenDegrees
}

private fun hoursToDegrees(hours: Double) = hours * 15
private fun degreesToHours(degrees: Double) = degrees / 15

private fun localSiderealTimeRadians(longitude: Double, time: Time): Double {
    val greenwichSiderealTime = siderealTime(time)
    val localSiderealTime = greenwichSiderealTime + degreesToHours(longitude)
    var localSiderealDegrees = hoursToDegrees(localSiderealTime)
    localSiderealDegrees = (localSiderealDegrees + 360) % 360
    val localSiderealRadians = Math.toRadians(localSiderealDegrees)
    return localSiderealRadians
}
