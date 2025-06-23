package com.byagowi.persiancalendar.ui.astronomy

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import com.byagowi.persiancalendar.utils.titleStringId
import com.byagowi.persiancalendar.utils.toCivilDate
import io.github.cosinekitty.astronomy.Body
import io.github.persiancalendar.praytimes.Coordinates
import java.util.GregorianCalendar
import kotlin.time.Duration.Companion.days

class Chaldean(val body: Body, val en: String, val fa: String)

// https://en.wikipedia.org/wiki/Planetary_hours
@VisibleForTesting
private val chaldeanOrder = listOf(
    Chaldean(Body.Saturn, en = "Greatest Inauspicious", fa = "نحس اکبر"),
    Chaldean(Body.Jupiter, en = "Greatest Auspicious", fa = "سعد اکبر"),
    Chaldean(Body.Mars, en = "Lesser Inauspicious", fa = "نحس اصغر"),
    Chaldean(Body.Sun, en = "Auspicious", fa = "سعد"),
    Chaldean(Body.Venus, en = "Lesser Auspicious", fa = "سعد اصغر"),
    Chaldean(Body.Mercury, en = "Mixed", fa = "ممترج"),
    Chaldean(Body.Moon, en = "Ascendant", fa = "طالع")
)

private val ruledBy = listOf(
    Body.Saturn, // Saturday
    Body.Sun, // Sunday
    Body.Moon, // Monday
    Body.Mars, // Tuesday
    Body.Mercury, // Wednesday
    Body.Jupiter, // Thursday
    Body.Venus, // Friday
)

private fun chaldeanIndexFromJdn(jdn: Jdn): Int {
    val ruledBy = ruledBy[jdn.weekDay]
    return chaldeanOrder.indexOfFirst { it.body == ruledBy }
}

private class PlanetaryHourRow(
    val chaldean: Chaldean,
    val isDay: Boolean,
    val from: Clock,
    val to: Clock,
    val highlighted: Boolean,
)

private fun getDaySplits(
    now: Long,
    coordinates: Coordinates,
): List<PlanetaryHourRow> {
    val nowClock = Clock(GregorianCalendar().also { it.timeInMillis = now }) + Clock(24.0)
    return buildList {
        val days = (-1..1).map { day ->
            GregorianCalendar().also { it.timeInMillis = now + (day.days.inWholeMilliseconds) }
        }
        val daysChaldeanIndices = days.map { chaldeanIndexFromJdn(Jdn(it.toCivilDate())) }
        val times = days.mapIndexed { i, day ->
            val prayTimes = coordinates.calculatePrayTimes(day)
            listOf(prayTimes.sunrise + i * 24, prayTimes.sunset + i * 24)
        }
        val currentDayIndex = times.indexOfFirst { (sunrise) -> nowClock.value < sunrise } - 1
        times.flatten().reduceIndexed { i, previous, clock ->
            val dayIndex = (i - 1) / 2
            val isDay = i % 2 == 1
            val groupOffset = ((i - 1) % 2) * 12
            val distance = (clock - previous) / 12
            val chaldeanIndex = daysChaldeanIndices[dayIndex]
            if (dayIndex == currentDayIndex) addAll((0..<12).mapNotNull {
                val from = previous + distance * it
                val to = previous + distance * (it + 1)
                val chaldean =
                    chaldeanOrder[(chaldeanIndex + it + groupOffset) % chaldeanOrder.size]
                PlanetaryHourRow(
                    chaldean = chaldean,
                    isDay = isDay,
                    from = Clock(from % 24),
                    to = Clock(to % 24),
                    highlighted = nowClock.value in from..to,
                )
            })
            clock
        }
    }
}

@Composable
fun PlanetaryHoursDialog(
    coordinates: Coordinates,
    now: Long = System.currentTimeMillis(),
    onDismissRequest: () -> Unit,
) {
    val language by language.collectAsState()
    AppDialog(onDismissRequest = onDismissRequest) {
        getDaySplits(now, coordinates).forEach { row ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .then(
                        if (row.highlighted) Modifier.background(
                            MaterialTheme.colorScheme.surfaceContainerLowest,
                            MaterialTheme.shapes.medium,
                        ) else Modifier
                    ),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    row.from.toFormattedString() + " - " + row.to.toFormattedString(),
                    Modifier.weight(2f),
                    textAlign = TextAlign.Center,
                )
                Icon(
                    if (row.isDay) Icons.Default.Brightness7 else Icons.Default.NightlightRound,
                    null
                )
                Text(
                    stringResource(row.chaldean.body.titleStringId),
                    Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    if (language.isArabicScript) row.chaldean.fa else row.chaldean.en,
                    Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
