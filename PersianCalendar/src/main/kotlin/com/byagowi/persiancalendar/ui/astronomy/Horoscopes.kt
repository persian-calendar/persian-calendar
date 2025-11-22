package com.byagowi.persiancalendar.ui.astronomy

import androidx.annotation.VisibleForTesting
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.AFGHANISTAN_TIMEZONE_ID
import com.byagowi.persiancalendar.IRAN_TIMEZONE_ID
import com.byagowi.persiancalendar.LRM
import com.byagowi.persiancalendar.NBSP
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.RLM
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.cityName
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.isTalkBackEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.common.AppIconButton
import com.byagowi.persiancalendar.ui.common.NumberEdit
import com.byagowi.persiancalendar.ui.common.TodayActionButton
import com.byagowi.persiancalendar.ui.utils.SettingsHorizontalPaddingItem
import com.byagowi.persiancalendar.utils.dateStringOfOtherCalendars
import com.byagowi.persiancalendar.utils.formatDateAndTime
import com.byagowi.persiancalendar.utils.isMoonInScorpio
import com.byagowi.persiancalendar.utils.titleStringId
import com.byagowi.persiancalendar.utils.toCivilDate
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.roundToInt

private fun formatAngle(value: Double, isAbjad: Boolean = false): String {
    val degrees = value.toInt()
    val minutes = (value % 1 * 60).roundToInt()
    if (isAbjad) return toAbjad(degrees) + " " + toAbjad(minutes)
    return numeral.value.format("$LRM%02d°:%02d’$LRM".format(degrees, minutes))
}

private fun geocentricLongitudeAndDistanceOfBody(body: Body, time: Time): Pair<Double, Double> {
    return when (body) {
        Body.Sun -> sunPosition(time).let { it.elon to it.vec.length() }
        Body.Moon -> eclipticGeoMoon(time).let { it.lon to it.dist }
        else -> {
            val ecliptic = equatorialToEcliptic(geoVector(body, time, Aberration.Corrected))
            ecliptic.elon to ecliptic.vec.length()
        }
    }
}

@Composable
fun HoroscopeDialog(date: Date = Date(), onDismissRequest: () -> Unit) {
    val time = Time.fromMillisecondsSince1970(date.time)
    AppDialog(onDismissRequest = onDismissRequest) {
        Spacer(Modifier.height(SettingsHorizontalPaddingItem.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            var mode by rememberSaveable { mutableStateOf(AstronomyMode.EARTH) }
            val language by language.collectAsState()

            @Composable
            fun format(body: Body, longitude: Double, distance: Double): String {
                return stringResource(body.titleStringId) + ": %s %s %s%s".format(
                    formatAngle(longitude % 30), // Remaining angle
                    Zodiac.fromTropical(longitude).symbol,
                    if (language.isArabicScript) RLM else "",
                    language.formatAuAsKm(distance)
                )
            }

            val text = @Suppress("SimplifiableCallChain") when (mode) {
                AstronomyMode.EARTH -> geocentricDistanceBodies.map { body ->
                    val (longitude, distance) = geocentricLongitudeAndDistanceOfBody(body, time)
                    format(body, longitude, distance)
                }.joinToString("\n")

                AstronomyMode.SUN -> heliocentricDistanceBodies.map { body ->
                    val (longitude, distance) = helioVector(body, time).let {
                        // See also eclipticLongitude of the astronomy library
                        equatorialToEcliptic(it).elon to it.length()
                    }
                    format(body, longitude, distance)
                }.joinToString("\n")

                else -> ""
            }
            Text(
                text,
                maxLines = text.lines().size,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 9.sp,
                    maxFontSize = LocalTextStyle.current.fontSize,
                ),
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
                modifier = Modifier.align(Alignment.CenterHorizontally),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(
                    maxFontSize = LocalTextStyle.current.fontSize,
                    minFontSize = 9.sp,
                )
            )
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
            AscendantZodiac(time, it, abjad = false, isYearEquinox = false)
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
private fun EasternHoroscopePattern(
    modifier: Modifier = Modifier,
    cell: @Composable BoxScope.(Int) -> Unit,
) {
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
                        .semantics {
                            this.traversalIndex = i + 1f
                            this.isTraversalGroup = true
                        }
                        .size(this.maxWidth / 3),
                ) { CompositionLocalProvider(LocalLayoutDirection provides textDirection) { cell(i) } }
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
fun YearHoroscopeDialog(initialPersianYear: Int, onDismissRequest: () -> Unit) {
    AppDialog(onDismissRequest = onDismissRequest) {
        val state = rememberPagerState(yearPages / 2) { yearPages }
        val animationProgress = remember { Animatable(0f) }
        LaunchedEffect(Unit) { delay(700); animationProgress.animateTo(1f) }
        var abjad by remember { mutableStateOf(false) }
        val pendingConfirms = remember { mutableStateListOf<() -> Unit>() }
        if (state.currentPageOffsetFraction != 0f) pendingConfirms.forEach { it() }
        val coroutineScope = rememberCoroutineScope()
        HorizontalPager(
            state,
            pageSpacing = 8.dp,
            modifier = Modifier.animateContentSize(),
        ) { page ->
            Column {
                YearHoroscopeDialogContent(
                    persianYear = page - yearPages / 2 + initialPersianYear,
                    validRange = initialPersianYear - yearPages / 2..initialPersianYear + yearPages / 2,
                    animationProgress = animationProgress,
                    pendingConfirms = pendingConfirms,
                    abjad = abjad,
                    onPagerValueChange = {
                        coroutineScope.launch {
                            val targetPage = it + yearPages / 2 - initialPersianYear
                            if (abs(targetPage - state.currentPage) > 5) {
                                state.requestScrollToPage(targetPage)
                            } else state.animateScrollToPage(targetPage)
                        }
                    },
                )
            }
        }
        val action = when {
            pendingConfirms.isNotEmpty() -> FooterAction.Confirm
            state.currentPage != yearPages / 2 -> FooterAction.Reset
            language.collectAsState().value.isArabicScript -> FooterAction.Abjad
            else -> FooterAction.None
        }
        Crossfade(
            targetState = action,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .animateContentSize(),
        ) {
            when (it) {
                FooterAction.Confirm -> AppIconButton(
                    icon = Icons.Default.Done,
                    title = stringResource(R.string.accept),
                    onClick = { pendingConfirms.forEach { it() } },
                )

                FooterAction.Abjad -> Column(Modifier.align(Alignment.CenterHorizontally)) {
                    AnimatedVisibility(animationProgress.value != 0f) {
                        FilledTonalIconToggleButton(abjad, { abjad = it }) {
                            Text("ابجد")
                        }
                    }
                }

                FooterAction.Reset -> TodayActionButton {
                    coroutineScope.launch { state.animateScrollToPage(yearPages / 2) }
                }

                FooterAction.None -> {}
            }
        }
    }
}

private enum class FooterAction { Reset, Confirm, Abjad, None }

private const val yearPages = 5000

@Composable
private fun ColumnScope.YearHoroscopeDialogContent(
    persianYear: Int,
    animationProgress: Animatable<Float, AnimationVector1D>,
    pendingConfirms: SnapshotStateList<() -> Unit>,
    abjad: Boolean,
    onPagerValueChange: (Int) -> Unit,
    validRange: IntRange,
) {
    val coroutineScope = rememberCoroutineScope()
    val clickableModifier = Modifier.clickable(
        indication = null,
        interactionSource = null,
    ) {
        coroutineScope.launch {
            animationProgress.animateTo(if (animationProgress.value > .5f) 0f else 1f)
        }
    }
    val language by language.collectAsState()
    val resources = LocalResources.current
    val isTalkBackEnabled by isTalkBackEnabled.collectAsState()
    val horoscopeString = stringResource(R.string.horoscope)
    val yearNameString = stringResource(R.string.year_name)
    val yearNameModifier = if (isTalkBackEnabled) Modifier.semantics {
        this.contentDescription = yearNameString
        this.isTraversalGroup = true
    } else clickableModifier
    val planetaryModifier = if (isTalkBackEnabled) Modifier.semantics {
        this.contentDescription = horoscopeString
        this.isTraversalGroup = true
    } else clickableModifier
    EasternHoroscopePattern(yearNameModifier) { i ->
        val date = PersianDate(persianYear + i, 1, 1)
        val chineseZodiac = ChineseZodiac.fromPersianCalendar(date)
        Text(
            chineseZodiac.resolveEmoji(language.isPersianOrDari),
            fontSize = 40.sp,
            modifier = Modifier
                .semantics { this.hideFromAccessibility() }
                .alpha(1 - animationProgress.value * .8f),
        )
        Text(
            chineseZodiac.formatForHoroscope(resources, language.isPersianOrDari),
            textAlign = TextAlign.Center,
            modifier = Modifier.alpha(animationProgress.value * 1f),
        )
    }
    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
    val gregorianYear = CivilDate(PersianDate(persianYear, 1, 1)).year
    val isUserLocatedInIranOrAfghanistan = when (TimeZone.getDefault().id) {
        IRAN_TIMEZONE_ID, AFGHANISTAN_TIMEZONE_ID -> true
        else -> false
    }
    val settingsCoordinates = coordinates.collectAsState().value
    val settingsCityName = cityName.collectAsState().value
    val (coordinates, cityName) = when {
        !isUserLocatedInIranOrAfghanistan && settingsCoordinates != null && settingsCityName != null && !language.isIranExclusive -> {
            settingsCoordinates to settingsCityName
        }

        language.isAfghanistanExclusive -> {
            val kabulCoordinates = Coordinates(34.53, 69.16, 0.0)
            kabulCoordinates to if (language.isArabicScript) "کابل" else "Kabul"
        }

        // So the user would be able to verify it with the calendar book published
        else -> {
            val tehranCoordinates = Coordinates(35.68, 51.42, 0.0)
            tehranCoordinates to if (language.isArabicScript) "تهران" else "Tehran"
        }
    }

    val numeral by numeral.collectAsState()
    val time = seasons(gregorianYear).marchEquinox
    var showTextEdit by remember { mutableStateOf(false) }
    Crossfade(showTextEdit) { state ->
        val gregorianCalendar = Date(time.toMillisecondsSince1970()).toGregorianCalendar()
        Box(contentAlignment = Alignment.Center) {
            if (state) NumberEdit(
                dismissNumberEdit = { showTextEdit = false },
                pendingConfirms = pendingConfirms,
                modifier = Modifier.fillMaxWidth(),
                isValid = { it in validRange },
                initialValue = persianYear,
                onValueChange = onPagerValueChange,
            )
            val resources = LocalResources.current
            val lines = listOf(
                if (language.isPersianOrDari) {
                    "لحظهٔ تحویل سال " + numeral.format(persianYear) + spacedComma + ChineseZodiac.fromPersianCalendar(
                        PersianDate(persianYear, 1, 1)
                    ).format(
                        resources,
                        withEmoji = true,
                        isPersian = true,
                    ) + spacedComma + " شمسی در $cityName"
                } else "$cityName, March equinox of " + numeral.format(gregorianYear) + " CE",
                gregorianCalendar.formatDateAndTime(withWeekDay = true) + run {
                    if (isMoonInScorpio(time)) spacedComma + stringResource(R.string.moon_in_scorpio)
                    else ""
                },
                dateStringOfOtherCalendars(Jdn(gregorianCalendar.toCivilDate()), spacedComma),
            )
            Text(
                lines.joinToString("\n"),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClickLabel = stringResource(R.string.select_year)) {
                        showTextEdit = true
                    }
                    .then(if (state) Modifier.alpha(.0f) else Modifier),
                maxLines = lines.size,
                autoSize = TextAutoSize.StepBased(
                    maxFontSize = LocalTextStyle.current.fontSize,
                    minFontSize = 9.sp,
                )
            )
        }
    }
    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
    AscendantZodiac(
        time,
        coordinates,
        planetaryModifier,
        progress = animationProgress.value,
        abjad = abjad,
        isYearEquinox = true,
    )
}

private val geocentricDistanceBodies = listOf(
    Body.Sun, Body.Moon, Body.Mercury, Body.Venus, Body.Mars, Body.Jupiter,
    Body.Saturn, Body.Uranus, Body.Neptune, Body.Pluto,
)

private val heliocentricDistanceBodies = listOf(
    Body.Mercury, Body.Venus, Body.Earth, Body.Moon, Body.Mars, Body.Jupiter,
    Body.Saturn, Body.Uranus, Body.Neptune, Body.Pluto,
)

private val ascendantBodies = listOf(
    Body.Sun, Body.Moon, Body.Mercury, Body.Venus, Body.Mars, Body.Jupiter, Body.Saturn,
)

@Composable
private fun AscendantZodiac(
    time: Time,
    coordinates: Coordinates,
    modifier: Modifier = Modifier,
    progress: Float = 1f,
    isYearEquinox: Boolean,
    abjad: Boolean,
) {
    val bodiesZodiac = ascendantBodies.map { body ->
        if (body == Body.Sun && isYearEquinox) {
            // Sometimes 359.99 put it in a incorrect house so let's just hardcode it
            return@map body to .0
        }
        val (longitude, _) = geocentricLongitudeAndDistanceOfBody(body, time)
        body to longitude
    }.sortedBy { (_, longitude) -> longitude }
        .groupBy { (_, longitude) -> Zodiac.fromTropical(longitude) }
    val houses = houses(coordinates.latitude, coordinates.longitude, time)
    val ascendantZodiac = Zodiac.fromTropical(houses[0])
    val resources = LocalResources.current
    val numFontStyle = SpanStyle() // to be used later, hopefully
    val meanApogee = meanApogee(time)
    val meanApogeeZodiac = Zodiac.fromTropical(meanApogee)
    fun AnnotatedString.Builder.appendAngle(title: String, value: Double) {
        append(title + NBSP)
        withStyle(numFontStyle) { append(formatAngle(value % 30, abjad)) }
    }
    EasternHoroscopePattern(modifier) { i ->
        val zodiac = Zodiac.entries[(i + ascendantZodiac.ordinal) % 12]
        Text(
            zodiac.symbol,
            Modifier
                .semantics { this.hideFromAccessibility() }
                .alpha(1 - progress * .9f),
            fontSize = 40.sp,
        )
        val text = buildAnnotatedString {
            val house = setOf(zodiac, Zodiac.fromTropical(houses[i])).joinToString("/") {
                it.shortTitle(resources)
            }
            appendAngle(house, houses[i])
            val bodies = bodiesZodiac[zodiac] ?: emptyList()
            bodies.forEach { (body, longitude) ->
                appendLine()
                appendAngle(resources.getString(body.titleStringId), longitude)
            }
            if (zodiac == meanApogeeZodiac) {
                appendLine()
                appendAngle(/*"⚸" + */stringResource(R.string.black_moon), meanApogee)
            }
        }
        Text(
            text,
            textAlign = TextAlign.Center,
            modifier = Modifier.alpha(progress * 1f),
            style = LocalTextStyle.current.copy(
                lineHeight = with(LocalDensity.current) {
                    LocalTextStyle.current.lineHeight.toDp().coerceAtMost(20.dp).toSp()
                }
            ),
            softWrap = false,
            maxLines = text.split("\n").size,
            autoSize = TextAutoSize.StepBased(
                maxFontSize = LocalTextStyle.current.fontSize,
                minFontSize = 9.sp,
            )
        )
    }
}

@VisibleForTesting
fun meanApogee(time: Time): Double {
    val t = time.tt / 36525.0
    // https://ftp.space.dtu.dk/pub/DTU10/DTU10_TIDEMODEL/SOFTWARE/test_perth3.f#:~:text=lunar%20perigee
    val meanPerigee = ((-1.249172e-5 * t - 1.032e-2) * t + 4069.0137287) * t + 83.3532465
    return (meanPerigee + 180) % 360
}

// True apogee's time is calculatable like this though not related to our usecase
// eclipticGeoMoon(
//     lunarApsidesAfter(time)
//         .first { it.kind == ApsisKind.Apocenter }
//         .time
// ).lon

private val abjadMap = mapOf(
    1 to "ا", 2 to "ب", 3 to "ج", 4 to "د", 5 to "ه", 6 to "و", 7 to "ز", 8 to "ح", 9 to "ط",
    10 to "ی", 20 to "ک", 30 to "ل", 40 to "م", 50 to "ن", 60 to "س", 70 to "ع", 80 to "ف",
    90 to "ص", 100 to "ق", 200 to "ر", 300 to "ش", 400 to "ت", 500 to "ث", 600 to "خ", 700 to "ذ",
    800 to "ض", 900 to "ظ", 1000 to "غ", 1000_000 to "غ غ",
).toSortedMap { x, y -> y compareTo x }

@VisibleForTesting
fun toAbjad(number: Int): String {
    if (number == 0) return "ها" // It's like ها in Nastaliq https://imgur.com/a/0eMBO2c
    var n = number
    return buildString {
        for (value in abjadMap.keys) while (n >= value) {
            append(abjadMap[value])
            n -= value
        }
    }
}
