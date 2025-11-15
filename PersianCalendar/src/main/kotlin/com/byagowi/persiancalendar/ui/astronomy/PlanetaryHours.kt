package com.byagowi.persiancalendar.ui.astronomy

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.WeekDay
import com.byagowi.persiancalendar.global.cityName
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import com.byagowi.persiancalendar.utils.formatDate
import com.byagowi.persiancalendar.utils.symbol
import com.byagowi.persiancalendar.utils.titleStringId
import com.byagowi.persiancalendar.utils.toCivilDate
import io.github.cosinekitty.astronomy.Body
import io.github.persiancalendar.praytimes.Coordinates
import java.util.GregorianCalendar
import kotlin.time.Duration.Companion.days

@VisibleForTesting
data class Planet(val body: Body, val en: String, val fa: String)

// https://en.wikipedia.org/wiki/Planetary_hours
private val chaldeanOrder = listOf(
    Planet(Body.Saturn, en = "Greatest Inauspicious", fa = "نحس اکبر"),
    Planet(Body.Jupiter, en = "Greatest Auspicious", fa = "سعد اکبر"),
    Planet(Body.Mars, en = "Lesser Inauspicious", fa = "نحس اصغر"),
    Planet(Body.Sun, en = "Auspicious", fa = "سعد"),
    Planet(Body.Venus, en = "Lesser Auspicious", fa = "سعد اصغر"),
    Planet(Body.Mercury, en = "Mixed", fa = "ممترج"),
    Planet(Body.Moon, en = "Ascendant", fa = "طالع")
)

// This is different level of sky that is used in Astronomy screen in geocentric mode,
// a bit unrelated here but actually this is the same as chaldeanOrder but reversed, who knew
// سنایی، فی صفة الافلاک:
// فلک تاسع است بر ز افلاک - کین فلکها بود درو چو مغاک
// فلک ثامن است جای بروج - واندر آن هفت را دخول و خروج
// فلک سابع است آنِ کیوانست - که مر او را بسان ایوانست
// فلک سادس است زاوش را - که دهنده است دانش و هُش را
// فلک خامس آنِ بهرامست - آنکه در فعل و رای خودکامست
// فلک رابع آنِ خورشیدست - که به ملک اندر آن چو جمشیدست
// فلک ثالث آنِ ناهیدست - زهره کز نور او جهان شیدست
// فلک ثانی آنِ تیر آمد - آن عُطارد که وی دبیر آمد
// فلک اوّل آنِ ماه آمد - که اثیر اندر آن پناه آمد
val geocentricPlanetsList = chaldeanOrder
    .asReversed()
    .map { it.body }
    // Already drawn
    .filter { it != Body.Moon && it != Body.Sun }

private fun chaldeanIndexFromJdn(jdn: Jdn): Int {
    // Just note how similar they are…
    val ruledBy = when (jdn.weekDay) {
        WeekDay.SATURDAY -> Body.Saturn
        WeekDay.SUNDAY -> Body.Sun
        WeekDay.MONDAY -> Body.Moon
        WeekDay.TUESDAY -> Body.Mars
        WeekDay.WEDNESDAY -> Body.Mercury
        WeekDay.THURSDAY -> Body.Jupiter
        WeekDay.FRIDAY -> Body.Venus
    }
    return chaldeanOrder.indexOfFirst { it.body == ruledBy }
}

@VisibleForTesting
data class PlanetaryHourRow(
    val planet: Planet,
    val isDay: Boolean,
    val from: Clock,
    val to: Clock,
    val upcoming: Boolean,
)

@VisibleForTesting
fun getDaySplits(
    now: Long,
    coordinates: Coordinates,
    isToday: Boolean,
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
                val planet = chaldeanOrder[(chaldeanIndex + it + groupOffset) % chaldeanOrder.size]
                PlanetaryHourRow(
                    planet = planet,
                    isDay = isDay,
                    from = Clock(from % 24),
                    to = Clock(to % 24),
                    upcoming = isToday && nowClock.value in from..to,
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
    isToday: Boolean = true,
    onDismissRequest: () -> Unit,
) {
    val language by language.collectAsState()
    AppDialog(onDismissRequest = onDismissRequest) {
        val cityName by cityName.collectAsState()
        Text(
            formatDate(
                Jdn(GregorianCalendar().also { it.timeInMillis = now }
                    .toCivilDate()) on mainCalendar,
            ) + cityName?.let { spacedComma + it }.orEmpty(),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
        )
        getDaySplits(now, coordinates, isToday).forEach { row ->
            HorizontalDivider()
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .then(
                        if (row.upcoming) Modifier.background(
                            MaterialTheme.colorScheme.surfaceContainerLowest,
                            MaterialTheme.shapes.medium,
                        ) else Modifier
                    )
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                AutoSizedText(
                    row.from.toFormattedString() + " - " + row.to.toFormattedString(),
                    2f,
                )
                Icon(
                    if (row.isDay) Icons.Default.Brightness7 else Icons.Default.NightlightRound,
                    null
                )
                AutoSizedText(
                    stringResource(row.planet.body.titleStringId) + " " + row.planet.body.symbol,
                    1f,
                )
                AutoSizedText(if (language.isArabicScript) row.planet.fa else row.planet.en, 1f)
            }
        }
    }
}

@Composable
private fun RowScope.AutoSizedText(text: String, weight: Float) {
    Box(Modifier.weight(weight), contentAlignment = Alignment.Center) {
        SelectionContainer {
            Text(
                text = text,
                style = LocalTextStyle.current,
                maxLines = 1,
                softWrap = false,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 9.sp,
                    maxFontSize = MaterialTheme.typography.bodyMedium.fontSize,
                ),
            )
        }
    }
}
