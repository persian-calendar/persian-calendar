package com.byagowi.persiancalendar.ui.astronomy

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.util.lruCache
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_MAP
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_MOON
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_TIME_BAR
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Season
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.ui.common.AppDropdownMenuItem
import com.byagowi.persiancalendar.ui.common.BaseSlider
import com.byagowi.persiancalendar.ui.common.DatePickerDialog
import com.byagowi.persiancalendar.ui.common.NavigationNavigateUpIcon
import com.byagowi.persiancalendar.ui.common.NavigationOpenNavigationRailIcon
import com.byagowi.persiancalendar.ui.common.ScreenSurface
import com.byagowi.persiancalendar.ui.common.SolarDraw
import com.byagowi.persiancalendar.ui.common.SwitchWithLabel
import com.byagowi.persiancalendar.ui.common.ThreeDotsDropdownMenu
import com.byagowi.persiancalendar.ui.common.TimeArrow
import com.byagowi.persiancalendar.ui.common.TodayActionButton
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.theme.isDynamicGrayscale
import com.byagowi.persiancalendar.ui.theme.resolveAndroidCustomTypeface
import com.byagowi.persiancalendar.ui.utils.appBoundsTransform
import com.byagowi.persiancalendar.ui.utils.appContentSizeAnimationSpec
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey
import com.byagowi.persiancalendar.utils.formatDateAndTime
import com.byagowi.persiancalendar.utils.generateYearName
import com.byagowi.persiancalendar.utils.isSouthernHemisphere
import com.byagowi.persiancalendar.utils.toCivilDate
import com.byagowi.persiancalendar.utils.toGregorianCalendar
import io.github.cosinekitty.astronomy.seasons
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.PersianDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun SharedTransitionScope.AstronomyScreen(
    openNavigationRail: () -> Unit,
    navigateToMap: (time: Long) -> Unit,
    initialTime: Long,
    noBackStackAction: (() -> Unit)?,
) {
    var today by remember { mutableStateOf(Jdn.today()) }
    val timeInMillis = rememberSaveable { mutableLongStateOf(initialTime) }
    LaunchedEffect(Unit) {
        val interval = 10.seconds.inWholeMilliseconds
        while (true) {
            delay(interval)
            timeInMillis.longValue += interval
            today = Jdn.today()
        }
    }

    val mode = rememberSaveable { mutableStateOf(AstronomyMode.entries[0]) }
    var isTropical by rememberSaveable { mutableStateOf(false) }
    val isDatePickerDialogShown = rememberSaveable { mutableStateOf(false) }
    val jdn by remember {
        derivedStateOf {
            val date = Date(timeInMillis.longValue)
            Jdn(date.toGregorianCalendar(forceLocalTime = true).toCivilDate())
        }
    }
    val astronomyState = AstronomyState(timeInMillis.longValue)
    val sliderState = rememberLazyListState(initialFirstVisibleItemIndex = SLIDER_ITEMS / 2)

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class) TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.astronomy),
                        maxLines = 1,
                        autoSize = TextAutoSize.StepBased(
                            maxFontSize = LocalTextStyle.current.fontSize,
                            minFontSize = 10.sp,
                        ),
                    )
                },
                colors = appTopAppBarColors(),
                navigationIcon = {
                    if (noBackStackAction != null) NavigationNavigateUpIcon(noBackStackAction)
                    else NavigationOpenNavigationRailIcon(openNavigationRail)
                },
                actions = {
                    TodayActionButton(visible = jdn != today) {
                        timeInMillis.longValue = System.currentTimeMillis()
                    }
                    AnimatedVisibility(visible = mode.value == AstronomyMode.EARTH) {
                        SwitchWithLabel(
                            label = stringResource(R.string.tropical),
                            checked = isTropical,
                            labelBeforeSwitch = true,
                            onValueChange = { isTropical = it },
                        )
                    }

                    var showHoroscopeDialog by rememberSaveable { mutableStateOf(false) }
                    if (showHoroscopeDialog) HoroscopeDialog(timeInMillis.longValue) {
                        showHoroscopeDialog = false
                    }
                    var showYearHoroscopeDialog by rememberSaveable { mutableStateOf(false) }
                    if (showYearHoroscopeDialog) {
                        YearHoroscopeDialog(jdn.toPersianDate().year) {
                            showYearHoroscopeDialog = false
                        }
                    }

                    var showPlanetaryHoursDialog by rememberSaveable { mutableStateOf(false) }
                    if (showPlanetaryHoursDialog) coordinates?.also {
                        PlanetaryHoursDialog(it, timeInMillis.longValue) {
                            showPlanetaryHoursDialog = false
                        }
                    }

                    var showMoonInScorpioDialog by rememberSaveable { mutableStateOf(false) }
                    if (showMoonInScorpioDialog) MoonInScorpioDialog(
                        Date(timeInMillis.longValue).toGregorianCalendar(),
                    ) { showMoonInScorpioDialog = false }

                    ThreeDotsDropdownMenu { closeMenu ->
                        AppDropdownMenuItem({ Text(stringResource(R.string.select_date)) }) {
                            closeMenu()
                            isDatePickerDialogShown.value = true
                        }
                        AppDropdownMenuItem({ Text(stringResource(R.string.map)) }) {
                            closeMenu()
                            navigateToMap(timeInMillis.longValue)
                        }
                        AppDropdownMenuItem({ Text(stringResource(R.string.horoscope)) }) {
                            showHoroscopeDialog = true
                            closeMenu()
                        }
                        AppDropdownMenuItem(
                            {
                                val horoscopeString = stringResource(R.string.horoscope)
                                val yearString = stringResource(R.string.year)
                                Text(horoscopeString + spacedComma + yearString)
                            },
                        ) {
                            showYearHoroscopeDialog = true
                            closeMenu()
                        }
                        if (coordinates != null) AppDropdownMenuItem(
                            {
                                Text(stringResource(R.string.planetary_hours))
                            },
                        ) {
                            showPlanetaryHoursDialog = true
                            closeMenu()
                        }
                        AppDropdownMenuItem(
                            {
                                Text(stringResource(R.string.moon_in_scorpio))
                            },
                        ) {
                            showMoonInScorpioDialog = true
                            closeMenu()
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(Modifier.padding(top = paddingValues.calculateTopPadding())) {
            ScreenSurface {
                val bottomPadding = paddingValues.calculateBottomPadding()
                if (isLandscape) BoxWithConstraints(
                    Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)),
                ) {
                    val maxHeight = this.maxHeight
                    val maxWidth = this.maxWidth
                    Row(Modifier.fillMaxWidth()) {
                        Column(
                            Modifier
                                .width((maxWidth / 2).coerceAtMost(480.dp))
                                .fillMaxHeight()
                                .padding(
                                    top = 24.dp,
                                    start = 24.dp,
                                    bottom = bottomPadding + 16.dp,
                                ),
                        ) {
                            Header(
                                modifier = Modifier,
                                astronomyState = astronomyState,
                                jdn = jdn,
                                mode = mode.value,
                                isTropical = isTropical,
                                timeInMillis = timeInMillis,
                            )
                            Spacer(Modifier.weight(1f))
                            SliderBar(sliderState, timeInMillis, isDatePickerDialogShown)
                        }
                        SolarDisplay(
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 16.dp, bottom = bottomPadding + 16.dp)
                                .height(maxHeight - bottomPadding),
                            timeInMillis = timeInMillis,
                            astronomyState = astronomyState,
                            mode = mode,
                            isTropical = isTropical,
                            sliderState = sliderState,
                            navigateToMap = navigateToMap,
                        )
                    }
                } else Column {
                    BoxWithConstraints(Modifier.weight(1f, fill = false)) {
                        val maxHeight = this.maxHeight
                        val maxWidth = this.maxWidth
                        var needsScroll by remember { mutableStateOf(false) }
                        // Puts content in middle of available space after the measured header
                        Layout(
                            modifier = if (needsScroll) Modifier.verticalScroll(rememberScrollState()) else Modifier,
                            content = {
                                Header(
                                    modifier = Modifier.padding(
                                        start = 24.dp,
                                        end = 24.dp,
                                        top = 24.dp,
                                    ),
                                    astronomyState = astronomyState,
                                    jdn = jdn,
                                    mode = mode.value,
                                    isTropical = isTropical,
                                    timeInMillis = timeInMillis,
                                )
                                SolarDisplay(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(maxWidth - (56 * 2 + 8).dp),
                                    timeInMillis = timeInMillis,
                                    astronomyState = astronomyState,
                                    mode = mode,
                                    isTropical = isTropical,
                                    sliderState = sliderState,
                                    navigateToMap = navigateToMap,
                                )
                            },
                        ) { (header, content), constraints ->
                            val placeableHeader = header.measure(constraints)
                            val placeableContent = content.measure(constraints)
                            layout(
                                width = constraints.maxWidth,
                                height = max(
                                    placeableHeader.height + placeableContent.height,
                                    maxHeight.roundToPx(),
                                ),
                            ) {
                                // Put the header at top
                                placeableHeader.placeRelative(0, 0)

                                val availableHeight = maxHeight.roundToPx() - placeableHeader.height
                                val space = availableHeight / 2 - placeableContent.height / 2
                                needsScroll = space <= 0
                                placeableContent.placeRelative(
                                    0, placeableHeader.height + space.coerceAtLeast(0),
                                )
                            }
                        }
                    }
                    SliderBar(sliderState, timeInMillis, isDatePickerDialogShown)
                    Spacer(Modifier.height(16.dp + bottomPadding))
                }
            }
        }
    }

    if (isDatePickerDialogShown.value) DatePickerDialog(
        initialJdn = jdn,
        onDismissRequest = { isDatePickerDialogShown.value = false },
    ) { jdn ->
        timeInMillis.longValue =
            System.currentTimeMillis() + (jdn - Jdn.today()).days.inWholeMilliseconds
    }
}

private val oneMinute = 1.minutes.inWholeMilliseconds
private const val SLIDER_ITEMS = 1_000_000

@Composable
private fun SharedTransitionScope.SliderBar(
    sliderState: LazyListState,
    timeInMillis: MutableLongState,
    isDatePickerDialogShown: MutableState<Boolean>,
) {
    var lastButtonClickTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val coroutineScope = rememberCoroutineScope()
    fun buttonScrollSlider(days: Int) {
        lastButtonClickTimestamp = System.currentTimeMillis()
        coroutineScope.launch { sliderState.animateScrollBy(250f * days * if (isRtl) 1 else -1) }
        timeInMillis.longValue += days.days.inWholeMilliseconds
    }

    Column(Modifier.fillMaxWidth()) {
        Text(
            text = Date(timeInMillis.longValue).toGregorianCalendar().formatDateAndTime(),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { isDatePickerDialogShown.value = true },
                    onClickLabel = stringResource(R.string.select_date),
                    onLongClick = { timeInMillis.longValue = System.currentTimeMillis() },
                    onLongClickLabel = stringResource(R.string.today),
                )
                .sharedElement(
                    rememberSharedContentState(key = SHARED_CONTENT_KEY_TIME_BAR),
                    animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                    boundsTransform = appBoundsTransform,
                ),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            TimeArrow(::buttonScrollSlider, isPrevious = true)
            val primary = MaterialTheme.colorScheme.primary
            val space = 10.dp
            val spacePx = with(LocalDensity.current) { space.toPx() }
            LaunchedEffect(Unit) { sliderState.animateScrollBy(spacePx * 10) }
            Box(
                Modifier
                    .padding(horizontal = 16.dp)
                    .height(46.dp)
                    .weight(weight = 1f, fill = false),
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithContent {
                            val linesCount = this.size.width.toInt() / spacePx.roundToInt()
                            repeat(linesCount) {
                                val x =
                                    it * spacePx + sliderState.firstVisibleItemScrollOffset % spacePx
                                val deviation = 2 * (it - linesCount / 2f) / linesCount
                                drawLine(
                                    color = primary,
                                    start = Offset(x = x, y = 0f),
                                    end = Offset(x = x, y = this.size.height),
                                    strokeWidth = 2.dp.toPx(),
                                    alpha = cos(deviation * PI.toFloat() / 2),
                                )
                            }
                        },
                    state = sliderState,
                ) { items(SLIDER_ITEMS) { Box(Modifier.width(space)) } }
                // Above handles the painting below handles touch, they should be merged
                AndroidView(
                    factory = { context ->
                        val root = BaseSlider(context)
                        var latestVibration = 0L
                        root.onScrollListener = { dx, _ ->
                            if (dx != 0f) {
                                coroutineScope.launch { sliderState.scrollBy(dx) }
                                val current = System.currentTimeMillis()
                                if (current - lastButtonClickTimestamp > 2000) {
                                    if (current >= latestVibration + 25_000_000 / abs(dx)) {
                                        root.performHapticFeedbackVirtualKey()
                                        latestVibration = current
                                    }
                                    timeInMillis.longValue += (oneMinute * dx * if (isRtl) 1 else -1).toInt()
                                }
                            }
                        }
                        root
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            TimeArrow(::buttonScrollSlider, isPrevious = false)
        }
    }
}

@Composable
private fun SharedTransitionScope.TimeArrow(
    buttonScrollSlider: (Int) -> Unit,
    isPrevious: Boolean,
) {
    TimeArrow(
        onClick = { buttonScrollSlider(if (isPrevious) -1 else 1) },
        onClickLabel = stringResource(
            if (isPrevious) R.string.previous_x else R.string.next_x,
            stringResource(R.string.day),
        ),
        onLongClick = { buttonScrollSlider(if (isPrevious) -365 else 365) },
        onLongClickLabel = stringResource(
            if (isPrevious) R.string.previous_x else R.string.next_x,
            stringResource(R.string.year),
        ),
        isPrevious = isPrevious,
    )
}

@Composable
private fun SharedTransitionScope.SolarDisplay(
    modifier: Modifier,
    timeInMillis: MutableLongState,
    astronomyState: AstronomyState,
    mode: MutableState<AstronomyMode>,
    isTropical: Boolean,
    sliderState: LazyListState,
    navigateToMap: (time: Long) -> Unit,
) {
    Box(modifier) {
        Column(Modifier.align(Alignment.CenterStart)) {
            AstronomyMode.entries.forEach {
                NavigationRailItem(
                    modifier = Modifier.size(56.dp),
                    selected = mode.value == it,
                    onClick = { mode.value = it },
                    icon = {
                        if (it == AstronomyMode.MOON) MoonIcon(astronomyState) else Icon(
                            ImageVector.vectorResource(it.icon),
                            modifier = Modifier.size(24.dp),
                            contentDescription = null,
                            tint = Color.Unspecified,
                        )
                    },
                )
            }
        }
        val map = stringResource(R.string.map)
        NavigationRailItem(
            modifier = Modifier
                .size(56.dp)
                .align(Alignment.CenterEnd)
                .sharedBounds(
                    rememberSharedContentState(key = SHARED_CONTENT_KEY_MAP),
                    animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                    boundsTransform = appBoundsTransform,
                ),
            selected = false,
            onClick = { navigateToMap(timeInMillis.longValue) },
            icon = { Text("🗺", modifier = Modifier.semantics { this.contentDescription = map }) },
        )
        val surfaceColor = MaterialTheme.colorScheme.surface
        val contentColor = LocalContentColor.current
        val typeface = resolveAndroidCustomTypeface()
        val coroutineScope = rememberCoroutineScope()
        AndroidView(
            factory = {
                val solarView = SolarView(it)
                solarView.rotationalMinutesChange = { offset ->
                    timeInMillis.longValue += offset * oneMinute
                    coroutineScope.launch { sliderState.scrollBy(offset / 200f) }
                }
                solarView
            },
            modifier = Modifier
                .padding(horizontal = 56.dp)
                .aspectRatio(1f)
                .align(Alignment.Center),
            update = {
                it.setSurfaceColor(surfaceColor.toArgb())
                it.setContentColor(contentColor.toArgb())
                it.isTropicalDegree = isTropical
                it.setTime(astronomyState)
                it.setFont(typeface)
                it.mode = mode.value
            },
        )
    }
}

@Composable
private fun Header(
    modifier: Modifier,
    astronomyState: AstronomyState,
    jdn: Jdn,
    mode: AstronomyMode,
    isTropical: Boolean,
    timeInMillis: MutableLongState,
) {
    val sunZodiac = if (isTropical) Zodiac.fromTropical(astronomyState.sun.elon)
    else Zodiac.fromIau(astronomyState.sun.elon)
    val moonZodiac = if (isTropical) Zodiac.fromTropical(astronomyState.moon.lon)
    else Zodiac.fromIau(astronomyState.moon.lon)

    val resources = LocalResources.current
    val headerCache = remember(resources, language) {
        lruCache(
            1024,
            create = { jdn: Jdn ->
                astronomyState.generateHeader(resources, language) + generateYearName(
                    resources = resources,
                    jdn = jdn,
                    withOldEraName = language.isPersianOrDari,
                    withEmoji = true,
                    timeInMillis = timeInMillis.longValue,
                )
            },
        )
    }

    Column(modifier) {
        headerCache[jdn].fastForEach { line ->
            AnimatedContent(targetState = line, transitionSpec = appCrossfadeSpec) {
                SelectionContainer {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
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
        Seasons(jdn, timeInMillis)
        AnimatedVisibility(visible = mode == AstronomyMode.EARTH) {
            Row(Modifier.padding(top = 8.dp)) {
                listOf(
                    // ☉☀️
                    Triple(sunZodiac, R.string.sun, Color(0xcceaaa00)),
                    // ☽it.moonPhaseEmoji
                    Triple(moonZodiac, R.string.moon, Color(0xcc606060)),
                ).forEach { (zodiac, titleId, color) ->
                    Box(Modifier.weight(1f)) {
                        val title = stringResource(titleId)
                        val value = stringResource(zodiac.titleId)
                        Cell(
                            Modifier
                                .semantics(mergeDescendants = true) {
                                    this.contentDescription = title + spacedColon + value
                                }
                                .clearAndSetSemantics {}
                                .align(Alignment.Center),
                            color = color,
                            title = title,
                            value = zodiac.symbol + " " + value,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Seasons(jdn: Jdn, timeInMillis: MutableLongState) {
    val seasonsCache = remember { lruCache(1024, create = ::seasons) }
    val seasonsOrder = remember {
        if (coordinates?.isSouthernHemisphere == true) {
            listOf(Season.WINTER, Season.SPRING, Season.SUMMER, Season.AUTUMN)
        } else listOf(Season.SUMMER, Season.AUTUMN, Season.WINTER, Season.SPRING)
    }
    val equinoxes = (1..4).map { i ->
        Date(
            seasonsCache[
                CivilDate(
                    PersianDate(jdn.toPersianDate().year, i * 3, 29),
                ).year,
            ].let {
                when (i) {
                    1 -> it.juneSolstice
                    2 -> it.septemberEquinox
                    3 -> it.decemberSolstice
                    else -> it.marchEquinox
                }
            }.toMillisecondsSince1970(),
        ).let { it.time to it.toGregorianCalendar().formatDateAndTime() }
    }
    repeat(2) { row ->
        Row(Modifier.padding(top = 8.dp)) {
            repeat(2) { cell ->
                Box(Modifier.weight(1f)) {
                    val title = stringResource(seasonsOrder[cell + row * 2].nameStringId)
                    val (time, formattedTime) = equinoxes[cell + row * 2]
                    Cell(
                        Modifier
                            .clip(MaterialTheme.shapes.small)
                            .semantics(true) {
                                this.contentDescription = title + spacedComma + formattedTime
                            }
                            .clickable(onClickLabel = stringResource(R.string.select_date)) {
                                timeInMillis.longValue = time
                            }
                            .clearAndSetSemantics {},
                        color = seasonsOrder[cell + row * 2].color,
                        title = title,
                        value = formattedTime,
                    )
                }
            }
        }
    }
}

@Stable
@Composable
private fun SharedTransitionScope.MoonIcon(astronomyState: AstronomyState) {
    val resources = LocalResources.current
    val solarDraw = remember(resources) { SolarDraw(resources) }
    Box(
        modifier = Modifier
            .size(24.dp)
            .sharedBounds(
                rememberSharedContentState(key = SHARED_CONTENT_KEY_MOON),
                animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                boundsTransform = appBoundsTransform,
            )
            .drawBehind {
                drawIntoCanvas {
                    val radius = size.minDimension / 2f
                    val sun = astronomyState.sun
                    val moon = astronomyState.moon
                    solarDraw.moon(it.nativeCanvas, sun, moon, radius, radius, radius)
                }
            },
    )
}

@Stable
@Composable
private fun Cell(
    modifier: Modifier,
    color: Color,
    title: String,
    value: String,
) {
    Row(
        modifier
            .heightIn(max = 64.dp)
            .animateContentSize(appContentSizeAnimationSpec),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            modifier = Modifier
                .background(
                    if (isDynamicGrayscale()) Color(0xcc808080) else color,
                    MaterialTheme.shapes.small,
                )
                .align(alignment = Alignment.CenterVertically)
                .padding(vertical = 4.dp, horizontal = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            fontSize = with(LocalDensity.current) {
                MaterialTheme.typography.bodyMedium.fontSize.toDp().coerceAtMost(18.dp).toSp()
            },
        )
        SelectionContainer {
            Text(
                value,
                style = LocalTextStyle.current.copy(
                    lineHeight = with(LocalDensity.current) {
                        LocalTextStyle.current.lineHeight.toDp().coerceAtMost(28.dp).toSp()
                    },
                ),
                modifier = Modifier.padding(start = 8.dp, end = 4.dp),
                maxLines = if (LocalDensity.current.fontScale > 1) 2 else 1,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 9.sp,
                    maxFontSize = MaterialTheme.typography.bodyMedium.fontSize,
                ),
            )
        }
    }
}
