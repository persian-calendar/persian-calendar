package com.byagowi.persiancalendar.ui.astronomy

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.byagowi.persiancalendar.entities.Jdn
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
import io.github.cosinekitty.astronomy.sunPosition
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.PersianDate
import java.util.Date
import java.util.Locale
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.roundToLong

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
fun HoroscopesDialog(date: Date = Date(), onDismissRequest: () -> Unit) {
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
private fun EasternHoroscopePattern(textDirection: LayoutDirection, cellLabel: (Int) -> String) {
    val outline = MaterialTheme.colorScheme.outline
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

// See the following for more information:
// * https://github.com/user-attachments/assets/5f42c377-3f39-4000-b79c-08cbbf76fc07
// * https://github.com/user-attachments/assets/21eadf3f-c780-470d-91b0-a0e504689198
// See for example: https://w.wiki/E9uz
// See also: https://agnastrology.ir/بهینه-سازی-فروش/
@Composable
fun YearHoroscope(jdn: Jdn = Jdn.today(), onDismissRequest: () -> Unit) {
    val language by language.collectAsState()
    val resources = LocalContext.current.resources
    val baseDate = jdn.toPersianDate()
    val items = (0..<12).map {
        val date = PersianDate(baseDate.year + it, 1, 1)
        ChineseZodiac.fromPersianCalendar(date).format(
            resources = resources,
            withEmoji = true,
            persianDate = date,
            withOldEraName = language.isUserAbleToReadPersian,
            separator = "\n",
        )
    }
    val originalDirection = LocalLayoutDirection.current
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        AppDialog(onDismissRequest = onDismissRequest) {
            EasternHoroscopePattern(originalDirection) { items[it] }
            val time = seasons(CivilDate(PersianDate(baseDate.year, 1, 1)).year).marchEquinox
            val bodiesZodiac = bodies.filter {
                // Sun has fixed place, no point on showing that for year zodiac
                it != Body.Sun
            }.filter {
                it != Body.Neptune && it != Body.Pluto // Not visible to naked eye
            }.map { body ->
                val (longitude, _) = longitudeAndDistanceOfBody(body, time)
                body to longitude
            }.groupBy { (_, longitude) -> Zodiac.fromTropical(longitude) }
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(1.dp))
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
            EasternHoroscopePattern(originalDirection) {
                // We don't how these are calculated each year, let's hard code the far we can
                val offset = when (baseDate.year) {
                    1299 -> 9 // جدی
                    1300 -> 0 // implied حمل
                    1301 -> 3 // implied سرطان
                    1302 -> 6 // implied میزان
                    1303 -> 8 // قوس
                    1304 -> 0 // حمل
                    1305 -> 3 // سرطان
                    1306 -> 6 // implied میزان
                    // 1307 can be implied even
                    1308 -> 11 // implied حوت
                    1309 -> 3 // سرطان
                    1310 -> 5 // سنبله
                    1311 -> 8 // قوس
                    1312 -> 11 // حوت
                    1313 -> 2 // جوزا
                    1314 -> 5 // سنبله
                    1315 -> 7 // عقرب
                    1316 -> 10 // دلو
                    1317 -> 2 // جوزا
                    1318 -> 5 // سنبله
                    1319 -> 7 // عقرب
                    1320 -> 10 // دلو
                    1321 -> 2 // جوزا
                    1322 -> 4 // اسد
                    1323 -> 7 // عقرب
                    1324 -> 9 // جدی
                    1325 -> 1 // ثور
                    1326 -> 4 // اسد
                    1327 -> 6 // میزان
                    1328 -> 9 // implied جدی
                    1329 -> 1 // ثور
                    1330 -> 4 // اسد

                    1402 -> 11 // implied حوت
                    1403 -> 4 // implied اسد
                    1404 -> 3 // سرطان
                    else -> 0
                }
                val zodiac = Zodiac.entries[(it + offset) % 12]
                zodiac.format(
                    resources,
                    delim = "\n",
                    withEmoji = true,
                    short = true,
                ) + // " ${formatNumber(it)}\n" +
                        bodiesZodiac[zodiac]?.joinToString("\n") { (body, longitude) ->
                            val title = formatNumber(formatAngle(longitude % 30))
                            "${resources.getString(body.titleStringId)}: $LRM$title$LRM"
                        }?.let { "\n" + it }.orEmpty()
            }
        }
    }
}
