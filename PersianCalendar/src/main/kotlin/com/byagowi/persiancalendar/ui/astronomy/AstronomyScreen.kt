package com.byagowi.persiancalendar.ui.astronomy

import android.content.res.Configuration
import android.os.Build
import androidx.annotation.ColorInt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.util.lruCache
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Season
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.theme
import com.byagowi.persiancalendar.ui.calendar.dialogs.DayPickerDialog
import com.byagowi.persiancalendar.ui.common.AppDropdownMenuItem
import com.byagowi.persiancalendar.ui.common.NavigationOpenDrawerIcon
import com.byagowi.persiancalendar.ui.common.SolarDraw
import com.byagowi.persiancalendar.ui.common.ThreeDotsDropdownMenu
import com.byagowi.persiancalendar.ui.common.TodayActionButton
import com.byagowi.persiancalendar.ui.theme.AppTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.MaterialCornerExtraLargeTop
import com.byagowi.persiancalendar.ui.utils.isDynamicGrayscale
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey
import com.byagowi.persiancalendar.utils.TEN_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.formatDateAndTime
import com.byagowi.persiancalendar.utils.isSouthernHemisphere
import com.byagowi.persiancalendar.utils.toCivilDate
import com.byagowi.persiancalendar.utils.toGregorianCalendar
import io.github.cosinekitty.astronomy.seasons
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.PersianDate
import kotlinx.coroutines.delay
import java.util.Date
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AstronomyScreen(
    openDrawer: () -> Unit,
    navigateToMap: () -> Unit,
    viewModel: AstronomyViewModel,
) {
    LaunchedEffect(Unit) {
        // Default animation screen enter, only if minutes offset is at it's default
        if (viewModel.minutesOffset.value == AstronomyViewModel.DEFAULT_TIME)
            viewModel.animateToAbsoluteMinutesOffset(0)

        while (true) {
            delay(TEN_SECONDS_IN_MILLIS)
            // Ugly, just to make the offset
            viewModel.addMinutesOffset(1)
            viewModel.addMinutesOffset(-1)
        }
    }

    // Bad practice, for now
    var slider by remember { mutableStateOf<SliderView?>(null) }

    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.astronomy),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                colors = AppTopAppBarColors(),
                navigationIcon = { NavigationOpenDrawerIcon(openDrawer) },
                actions = {
                    val minutesOffset by viewModel.minutesOffset.collectAsState()
                    val isTropical by viewModel.isTropical.collectAsState()
                    val mode by viewModel.mode.collectAsState()
                    TodayActionButton(visible = minutesOffset != 0) {
                        viewModel.animateToAbsoluteMinutesOffset(0)
                    }
                    AnimatedVisibility(visible = mode == AstronomyMode.Earth) {
                        Row(
                            Modifier.clickable(
                                indication = rememberRipple(bounded = false),
                                interactionSource = remember { MutableInteractionSource() },
                            ) { viewModel.toggleIsTropical() },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.tropical))
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(isTropical, onCheckedChange = { viewModel.toggleIsTropical() })
                        }
                    }
                    ThreeDotsDropdownMenu { closeMenu ->
                        AppDropdownMenuItem(
                            text = { Text(stringResource(R.string.goto_date)) },
                            onClick = {
                                closeMenu()
                                viewModel.showDayPickerDialog()
                            },
                        )
                        AppDropdownMenuItem(
                            text = { Text(stringResource(R.string.map)) },
                            onClick = {
                                closeMenu()
                                navigateToMap()
                            },
                        )
                    }
                },
            )
        },
        bottomBar = {
            val modifier = Modifier
                .padding(bottom = 16.dp)
                .safeDrawingPadding()
            if (!isLandscape) SliderBar(modifier, slider, viewModel) { slider = it }
        }
    ) { paddingValues ->
        Surface(
            shape = MaterialCornerExtraLargeTop(),
            modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
        ) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val bottomPadding = paddingValues.calculateBottomPadding()
                val maxHeight = maxHeight
                val maxWidth = maxWidth
                if (isLandscape) Row(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier
                            .width((maxWidth / 2).coerceAtMost(480.dp))
                            .fillMaxHeight()
                            .padding(top = 24.dp, start = 24.dp, bottom = bottomPadding + 16.dp),
                    ) {
                        Header(Modifier, viewModel)
                        Spacer(Modifier.weight(1f))
                        SliderBar(Modifier, slider, viewModel) { slider = it }
                    }
                    SolarDisplay(
                        Modifier
                            .weight(1f)
                            .padding(top = 16.dp, bottom = bottomPadding + 16.dp)
                            .height(maxHeight - bottomPadding),
                        viewModel, slider, navigateToMap,
                    )
                } else Layout(
                    // Puts content in middle of available space after the measured header
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    content = {
                        Header(
                            Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp),
                            viewModel,
                        )
                        SolarDisplay(
                            Modifier
                                .fillMaxWidth()
                                .height(maxWidth - (56 * 2 + 8).dp),
                            viewModel, slider, navigateToMap,
                        )
                    },
                ) { measurables, constraints ->
                    val header = measurables[0].measure(constraints)
                    val content = measurables[1].measure(constraints)
                    layout(
                        width = constraints.maxWidth,
                        height = header.height + content.height +
                                // To make solar display can be scrolled above bottom padding in smaller screen
                                bottomPadding.roundToPx(),
                    ) {
                        // Put the header at top
                        header.placeRelative(0, 0)

                        val availableHeight =
                            (maxHeight - bottomPadding).roundToPx() - header.height
                        val space = availableHeight / 2 - content.height / 2
                        content.placeRelative(0, header.height + space.coerceAtLeast(0))
                    }
                }
            }
        }
    }

    val isDayPickerDialogShown by viewModel.isDayPickerDialogShown.collectAsState()
    if (isDayPickerDialogShown) DayPickerDialog(
        initialJdn = Jdn(viewModel.astronomyState.value.date.toCivilDate()),
        positiveButtonTitle = R.string.accept,
        onSuccess = { jdn -> viewModel.animateToAbsoluteDayOffset(jdn - Jdn.today()) },
        onDismissRequest = viewModel::dismissDayPickerDialog,
    )
}

@Composable
private fun SliderBar(
    modifier: Modifier,
    slider: SliderView?,
    viewModel: AstronomyViewModel,
    setSlider: (SliderView) -> Unit,
) {
    val state by viewModel.astronomyState.collectAsState()
    var lastButtonClickTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    fun buttonScrollSlider(days: Int) {
        lastButtonClickTimestamp = System.currentTimeMillis()
        slider?.smoothScrollBy(250f * days * if (isRtl) 1 else -1, 0f)
        viewModel.animateToRelativeDayOffset(days)
    }

    @OptIn(ExperimentalFoundationApi::class) Column(modifier.fillMaxWidth()) {
        Text(
            state.date.formatDateAndTime(),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { viewModel.showDayPickerDialog() },
                    onClickLabel = stringResource(R.string.goto_date),
                    onLongClick = { viewModel.animateToAbsoluteMinutesOffset(0) },
                    onLongClickLabel = stringResource(R.string.today),
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
            AndroidView(
                factory = { context ->
                    val root = SliderView(context)
                    root.setBarsColor(primary.toArgb())
                    setSlider(root)
                    var latestVibration = 0L
                    root.smoothScrollBy(250f * if (isRtl) 1 else -1, 0f)
                    root.onScrollListener = { dx, _ ->
                        if (dx != 0f) {
                            val current = System.currentTimeMillis()
                            if (current - lastButtonClickTimestamp > 2000) {
                                if (current >= latestVibration + 25_000_000 / abs(dx)) {
                                    root.performHapticFeedbackVirtualKey()
                                    latestVibration = current
                                }
                                viewModel.addMinutesOffset(
                                    (dx * if (isRtl) 1 else -1).toInt()
                                )
                            }
                        }
                    }
                    root
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .height(46.dp)
                    .weight(1f, fill = false),
            )
            TimeArrow(::buttonScrollSlider, isPrevious = false)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TimeArrow(buttonScrollSlider: (Int) -> Unit, isPrevious: Boolean) {
    val hapticFeedback = LocalHapticFeedback.current
    Icon(
        if (isPrevious) Icons.AutoMirrored.Default.KeyboardArrowLeft
        else Icons.AutoMirrored.Default.KeyboardArrowRight,
        contentDescription = null,
        Modifier.combinedClickable(
            indication = rememberRipple(bounded = false),
            interactionSource = remember { MutableInteractionSource() },
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                buttonScrollSlider(if (isPrevious) -1 else 1)
            },
            onClickLabel = stringResource(
                if (isPrevious) R.string.previous_x else R.string.next_x,
                stringResource(R.string.day),
            ),
            onLongClick = { buttonScrollSlider(if (isPrevious) -365 else 365) },
            onLongClickLabel = stringResource(
                if (isPrevious) R.string.previous_x else R.string.next_x,
                stringResource(R.string.year)
            ),
        ),
        tint = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun SolarDisplay(
    modifier: Modifier,
    viewModel: AstronomyViewModel,
    slider: SliderView?,
    navigateToMap: () -> Unit,
) {
    val state by viewModel.astronomyState.collectAsState()
    val isTropical by viewModel.isTropical.collectAsState()
    val mode by viewModel.mode.collectAsState()
    var showHoroscopeDialog by rememberSaveable { mutableStateOf(false) }
    if (showHoroscopeDialog) HoroscopesDialog(state.date.time) { showHoroscopeDialog = false }
    Box(modifier) {
        Column(Modifier.align(Alignment.CenterStart)) {
            AstronomyMode.entries.forEach {
                NavigationRailItem(
                    modifier = Modifier.size(56.dp),
                    selected = mode == it,
                    onClick = { viewModel.setMode(it) },
                    icon = {
                        if (it == AstronomyMode.Moon) MoonIcon(state) else Icon(
                            ImageVector.vectorResource(it.icon),
                            modifier = Modifier.size(24.dp),
                            contentDescription = null,
                            tint = Color.Unspecified,
                        )
                    },
                )
            }
        }
        val surfaceColor = MaterialTheme.colorScheme.surface
        val contentColor = LocalContentColor.current
        AndroidView(
            factory = {
                val solarView = SolarView(it)
                var clickCount = 0
                solarView.setOnClickListener {
                    if (++clickCount % 2 == 0) showHoroscopeDialog = true
                }
                solarView.rotationalMinutesChange = { offset ->
                    viewModel.addMinutesOffset(offset)
                    slider?.manualScrollBy(offset / 200f, 0f)
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
                it.setTime(state)
                it.mode = mode
            },
        )
        val map = stringResource(R.string.map)
        NavigationRailItem(
            modifier = Modifier
                .size(56.dp)
                .align(Alignment.CenterEnd),
            selected = false,
            onClick = navigateToMap,
            icon = {
                Text(
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) "m" else "🗺",
                    modifier = Modifier.semantics { this.contentDescription = map }
                )
            },
        )
    }
}

@Composable
private fun Header(modifier: Modifier, viewModel: AstronomyViewModel) {
    val isTropical by viewModel.isTropical.collectAsState()
    val mode by viewModel.mode.collectAsState()
    val state by viewModel.astronomyState.collectAsState()
    val sunZodiac = if (isTropical) Zodiac.fromTropical(state.sun.elon)
    else Zodiac.fromIau(state.sun.elon)
    val moonZodiac = if (isTropical) Zodiac.fromTropical(state.moon.lon)
    else Zodiac.fromIau(state.moon.lon)

    val context = LocalContext.current
    val headerCache = remember {
        lruCache(1024, create = { jdn: Jdn ->
            state.generateHeader(context.resources, jdn).joinToString("\n")
        })
    }

    Column(modifier) {
        val jdn by derivedStateOf { Jdn(state.date.toCivilDate()) }
        SelectionContainer {
            Text(
                headerCache[jdn],
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3
            )
        }
        Seasons(jdn)
        AnimatedVisibility(visible = mode == AstronomyMode.Earth) {
            Row(Modifier.padding(top = 8.dp)) {
                Box(Modifier.weight(1f)) {
                    Cell(
                        Modifier.align(Alignment.Center),
                        0xcceaaa00.toInt(),
                        stringResource(R.string.sun),
                        sunZodiac.format(context.resources, true) // ☉☀️
                    )
                }
                Box(Modifier.weight(1f)) {
                    Cell(
                        Modifier.align(Alignment.Center),
                        0xcc606060.toInt(),
                        stringResource(R.string.moon),
                        moonZodiac.format(context.resources, true) // ☽it.moonPhaseEmoji
                    )
                }
            }
        }
    }
}

@Composable
private fun Seasons(jdn: Jdn) {
    val seasonsCache = remember { lruCache(1024, create = ::seasons) }
    val seasonsOrder = remember {
        if (coordinates.value?.isSouthernHemisphere == true) {
            listOf(Season.WINTER, Season.SPRING, Season.SUMMER, Season.AUTUMN)
        } else listOf(Season.SUMMER, Season.AUTUMN, Season.WINTER, Season.SPRING)
    }
    val equinoxes = (1..4).map { i ->
        Date(
            seasonsCache[CivilDate(
                PersianDate(jdn.toPersianDate().year, i * 3, 29)
            ).year].let {
                when (i) {
                    1 -> it.juneSolstice
                    2 -> it.septemberEquinox
                    3 -> it.decemberSolstice
                    else -> it.marchEquinox
                }
            }.toMillisecondsSince1970()
        ).toGregorianCalendar().formatDateAndTime()
    }
    repeat(2) { row ->
        Row(Modifier.padding(top = 8.dp)) {
            repeat(2) { cell ->
                Box(Modifier.weight(1f)) {
                    Cell(
                        Modifier,
                        seasonsOrder[cell + row * 2].color,
                        stringResource(seasonsOrder[cell + row * 2].nameStringId),
                        equinoxes[cell + row * 2],
                    )
                }
            }
        }
    }
}

@Stable
@Composable
private fun MoonIcon(astronomyState: AstronomyState) {
    val context = LocalContext.current
    val solarDraw = remember { SolarDraw(context.resources) }
    Box(
        modifier = Modifier
            .size(24.dp)
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
private fun Cell(modifier: Modifier, @ColorInt color: Int, label: String, value: String) {
    Row(
        modifier.animateContentSize(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val context = LocalContext.current
        val isDynamicGrayscale = remember(LocalConfiguration.current) {
            theme.value.isDynamicColors() && context.isDynamicGrayscale
        }
        Text(
            label,
            modifier = Modifier
                .background(
                    Color(if (isDynamicGrayscale) 0xcc808080.toInt() else color),
                    MaterialTheme.shapes.small,
                )
                .align(alignment = Alignment.CenterVertically)
                .padding(vertical = 4.dp, horizontal = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
        )
        Spacer(Modifier.width(8.dp))
        SelectionContainer {
            Text(value, maxLines = 1, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
