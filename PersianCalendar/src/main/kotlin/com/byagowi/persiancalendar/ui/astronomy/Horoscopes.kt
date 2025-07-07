package com.byagowi.persiancalendar.ui.astronomy

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.AU_IN_KM
import com.byagowi.persiancalendar.LRM
import com.byagowi.persiancalendar.global.cityName
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.utils.SettingsHorizontalPaddingItem
import com.byagowi.persiancalendar.utils.formatDateAndTime
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.titleStringId
import com.byagowi.persiancalendar.utils.toGregorianCalendar
import io.github.cosinekitty.astronomy.Aberration
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.eclipticGeoMoon
import io.github.cosinekitty.astronomy.equatorialToEcliptic
import io.github.cosinekitty.astronomy.geoVector
import io.github.cosinekitty.astronomy.helioVector
import io.github.cosinekitty.astronomy.seasons
import io.github.cosinekitty.astronomy.sunPosition
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.praytimes.Coordinates
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong

private fun formatAngle(value: Double): String {
    val degrees = value.toInt()
    val minutes = (value % 1 * 60).roundToInt()
    return formatNumber("$LRM%02d°:%02d’$LRM".format(degrees, minutes))
}

// This is geocentric
private fun longitudeAndDistanceOfBody(body: Body, time: Time): Pair<Double, Double> {
    return when (body) {
        Body.Sun -> sunPosition(time).let { it.elon to it.vec.length() }
        Body.Moon -> eclipticGeoMoon(time).let { it.lon to it.dist }
        else -> equatorialToEcliptic(geoVector(body, time, Aberration.Corrected))
            .let { it.elon to it.vec.length() }
    }
}

@Composable
fun HoroscopeDialog(date: Date = Date(), onDismissRequest: () -> Unit) {
    val time = Time.fromMillisecondsSince1970(date.time)
    AppDialog(onDismissRequest = onDismissRequest) {
        Spacer(Modifier.height(SettingsHorizontalPaddingItem.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            var mode by rememberSaveable { mutableStateOf(AstronomyMode.EARTH) }
            @Suppress("SimplifiableCallChain")
            Text(
                when (mode) {
                    AstronomyMode.EARTH -> listOf(
                        Body.Sun, Body.Moon, Body.Mercury, Body.Venus, Body.Mars, Body.Jupiter,
                        Body.Saturn, Body.Uranus, Body.Neptune, Body.Pluto
                    ).map { body ->
                        val (longitude, distance) = longitudeAndDistanceOfBody(body, time)
                        stringResource(body.titleStringId) + ": %s%s %s %,d km".format(
                            Locale.ENGLISH,
                            LRM,
                            formatAngle(longitude % 30), // Remaining angle
                            Zodiac.fromTropical(longitude).emoji,
                            (distance * AU_IN_KM).roundToLong()
                        )
                    }.joinToString("\n")

                    AstronomyMode.SUN -> listOf(
                        Body.Mercury, Body.Venus, Body.Earth, Body.Moon, Body.Mars, Body.Jupiter,
                        Body.Saturn, Body.Uranus, Body.Neptune, Body.Pluto
                    ).map { body ->
                        val (longitude, distance) = helioVector(body, time).let {
                            // See also eclipticLongitude of the astronomy library
                            equatorialToEcliptic(it).elon to it.length()
                        }
                        stringResource(body.titleStringId) + ": %s%s %s %,d km".format(
                            Locale.ENGLISH,
                            LRM,
                            formatAngle(longitude % 30), // Remaining angle
                            Zodiac.fromTropical(longitude).emoji,
                            (distance * AU_IN_KM).roundToLong()
                        )
                    }.joinToString("\n")

                    else -> ""
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = SettingsHorizontalPaddingItem.dp),
            )
            Column {
                listOf(AstronomyMode.EARTH, AstronomyMode.SUN).forEach {
                    NavigationRailItem(
                        modifier = Modifier.size(56.dp),
                        selected = mode == it,
                        onClick = { mode = it },
                        icon = {
                            Icon(
                                ImageVector.vectorResource(it.icon),
                                modifier = Modifier.size(24.dp),
                                contentDescription = null,
                                tint = Color.Unspecified,
                            )
                        },
                    )
                }
            }
        }
        val coordinates by coordinates.collectAsState()
        coordinates?.takeIf { abs(it.latitude) <= 66 /* not useful for higher latitudes */ }?.let {
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
            Text(
                text = buildString {
                    append(date.toGregorianCalendar().formatDateAndTime())
                    val cityName by cityName.collectAsState()
                    cityName?.let { name -> append(spacedComma); append(name) }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
            AscendantZodiac(time, it, isYearEquinox = false)
        } ?: Spacer(Modifier.height(SettingsHorizontalPaddingItem.dp))
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
private fun EasternHoroscopePattern(modifier: Modifier = Modifier, cellLabel: (Int) -> String) {
    val outline = MaterialTheme.colorScheme.outline
    val textDirection = LocalLayoutDirection.current
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        BoxWithConstraints(
            modifier
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
    AppDialog(onDismissRequest = onDismissRequest) appDialog@{
        EasternHoroscopePattern { i ->
            val date = PersianDate(persianYear + i, 1, 1)
            ChineseZodiac.fromPersianCalendar(date).format(
                resources = resources,
                withEmoji = true,
                persianDate = date,
                withOldEraName = language.isUserAbleToReadPersian,
                separator = "\n",
            )
        }
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
        val gregorianYear = CivilDate(PersianDate(persianYear, 1, 1)).year
        val (coordinates, cityName) = when {
            language.isAfghanistanExclusive -> Coordinates(34.53, 69.16, 0.0) to
                    if (language.isUserAbleToReadPersian) "کابل" else "Kabul"

            // So the user would be able to verify it with the calendar book published
            else -> Coordinates(35.68, 51.42, 0.0) to
                    if (language.isUserAbleToReadPersian) "تهران" else "Tehran"
        }

        Text(
            if (language.isUserAbleToReadPersian) {
                "لحظهٔ تحویل سال " + formatNumber(persianYear) + " شمسی در $cityName"
            } else "$cityName, March equinox of " + formatNumber(gregorianYear) + " CE",
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
        val time = seasons(gregorianYear).marchEquinox
        AscendantZodiac(time, coordinates, isYearEquinox = true)
    }
}

@Composable
private fun AscendantZodiac(time: Time, coordinates: Coordinates, isYearEquinox: Boolean) {
    val bodiesZodiac = listOf(
        Body.Sun, Body.Moon, Body.Mercury, Body.Venus, Body.Mars, Body.Jupiter, Body.Saturn
    ).map { body ->
        if (body == Body.Sun && isYearEquinox) {
            // Sometimes 359.99 put it in a incorrect house so let's just hardcode it
            return@map body to .0
        }
        val (longitude, _) = longitudeAndDistanceOfBody(body, time)
        body to longitude
    }.sortedBy { (_, longitude) -> longitude }
        .groupBy { (_, longitude) -> Zodiac.fromTropical(longitude) }
    val houses = houses(coordinates.latitude, coordinates.longitude, time)
    val ascendantZodiac = Zodiac.fromTropical(houses[0])
    val resources = LocalContext.current.resources
    EasternHoroscopePattern { i ->
        val zodiac = Zodiac.entries[(i + ascendantZodiac.ordinal) % 12]
        zodiac.emoji + "\n" + zodiac.format(
            resources,
            withEmoji = false,
            short = true,
        ) + " " + formatAngle(houses[i] % 30) + bodiesZodiac[zodiac]?.joinToString("\n") { (body, longitude) ->
            resources.getString(body.titleStringId) + " " + formatAngle(longitude % 30)
        }?.let { "\n" + it }.orEmpty()
    }
}
