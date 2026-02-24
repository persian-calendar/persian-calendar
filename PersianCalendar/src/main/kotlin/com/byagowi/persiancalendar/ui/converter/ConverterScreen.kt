package com.byagowi.persiancalendar.ui.converter

import android.content.res.Configuration
import android.icu.util.ChineseCalendar
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.byagowi.persiancalendar.PREF_CALCULATOR_INPUT
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.enabledCalendars
import com.byagowi.persiancalendar.global.isAstronomicalExtraFeaturesEnabled
import com.byagowi.persiancalendar.global.isGradient
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac
import com.byagowi.persiancalendar.ui.common.AppIconButton
import com.byagowi.persiancalendar.ui.common.AppModesDropDown
import com.byagowi.persiancalendar.ui.common.CalendarPicker
import com.byagowi.persiancalendar.ui.common.CalendarsOverview
import com.byagowi.persiancalendar.ui.common.DatePicker
import com.byagowi.persiancalendar.ui.common.NavigationNavigateUpIcon
import com.byagowi.persiancalendar.ui.common.NavigationOpenNavigationRailIcon
import com.byagowi.persiancalendar.ui.common.NumberPicker
import com.byagowi.persiancalendar.ui.common.ScreenSurface
import com.byagowi.persiancalendar.ui.common.ScrollShadow
import com.byagowi.persiancalendar.ui.common.ShareActionButton
import com.byagowi.persiancalendar.ui.common.TodayActionButton
import com.byagowi.persiancalendar.ui.common.calendarPickerHeight
import com.byagowi.persiancalendar.ui.theme.animateColor
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey
import com.byagowi.persiancalendar.ui.utils.shareText
import com.byagowi.persiancalendar.utils.calculateDaysDifference
import com.byagowi.persiancalendar.utils.dayTitleSummary
import com.byagowi.persiancalendar.utils.formatDate
import com.byagowi.persiancalendar.utils.preferences
import io.github.persiancalendar.calculator.eval
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@Composable
fun SharedTransitionScope.ConverterScreen(
    openNavigationRail: () -> Unit,
    navigateToAstronomy: (Jdn) -> Unit,
    noBackStackAction: (() -> Unit)?,
    today: Jdn,
    initialScreenMode: ConverterScreenMode = ConverterScreenMode.entries[0],
) {
    var screenMode by rememberSaveable { mutableStateOf(initialScreenMode) }
    var resetButtonVisibility by remember { mutableStateOf(false) }
    var shareAction by remember { mutableStateOf({}) }
    var resetAction by remember { mutableStateOf({}) }
    val pendingConfirms = remember { mutableStateListOf<() -> Unit>() }
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class) TopAppBar(
                title = {
                    AppModesDropDown(
                        value = screenMode,
                        onValueChange = { screenMode = it },
                        values = ConverterScreenMode.entries,
                    ) { stringResource(it.title) }
                },
                colors = appTopAppBarColors(),
                navigationIcon = {
                    if (noBackStackAction != null) NavigationNavigateUpIcon(noBackStackAction)
                    else NavigationOpenNavigationRailIcon(openNavigationRail)
                },
                actions = {
                    val anyPendingConfirm = pendingConfirms.isNotEmpty()
                    if (screenMode.backspaceReset) CompositionLocalProvider(
                        LocalLayoutDirection provides LayoutDirection.Ltr,
                    ) {
                        AnimatedVisibility(resetButtonVisibility) {
                            AppIconButton(
                                icon = Icons.AutoMirrored.Default.Backspace,
                                title = stringResource(R.string.return_to_today),
                                onClick = resetAction,
                            )
                        }
                    } else TodayActionButton(visible = resetButtonVisibility && !anyPendingConfirm) {
                        resetAction()
                    }
                    AnimatedVisibility(anyPendingConfirm) {
                        AppIconButton(
                            icon = Icons.Default.Done,
                            title = stringResource(R.string.accept),
                            onClick = { pendingConfirms.forEach { it() } },
                        )
                    }
                    AnimatedVisibility(visible = !anyPendingConfirm) {
                        ShareActionButton(shareAction)
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(Modifier.padding(top = paddingValues.calculateTopPadding())) {
            ScreenSurface {
                Box(
                    Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)),
                ) {
                    val scrollState = rememberScrollState()
                    Column(Modifier.verticalScroll(scrollState)) {
                        Spacer(Modifier.height(24.dp))

                        AnimatedVisibility(screenMode == ConverterScreenMode.TIME_ZONES) {
                            TimeZones(
                                pendingConfirms = pendingConfirms,
                                setShareAction = { shareAction = it },
                                setResetAction = { resetAction = it },
                                setResetButtonVisibility = { resetButtonVisibility = it },
                            )
                        }

                        AnimatedVisibility(
                            when (screenMode) {
                                ConverterScreenMode.CONVERTER, ConverterScreenMode.DISTANCE -> true
                                else -> false
                            },
                        ) {
                            Column(Modifier.padding(horizontal = 24.dp)) {
                                ConverterAndDistance(
                                    navigateToAstronomy = navigateToAstronomy,
                                    sharedTransitionScope = this@ConverterScreen,
                                    pendingConfirms = pendingConfirms,
                                    screenMode = screenMode,
                                    setShareAction = { shareAction = it },
                                    setResetAction = { resetAction = it },
                                    setResetButtonVisibility = { resetButtonVisibility = it },
                                    today = today,
                                )
                            }
                        }

                        AnimatedVisibility(screenMode == ConverterScreenMode.CALCULATOR) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Calculator(
                                    setShareAction = { shareAction = it },
                                    setResetAction = { resetAction = it },
                                    setResetButtonVisibility = { resetButtonVisibility = it },
                                )
                            }
                        }

                        AnimatedVisibility(screenMode == ConverterScreenMode.QR_CODE) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                QrCode(
                                    setShareAction = { shareAction = it },
                                    setResetAction = { resetAction = it },
                                    setResetButtonVisibility = { resetButtonVisibility = it },
                                )
                            }
                        }

                        Spacer(
                            Modifier.height(
                                paddingValues.calculateBottomPadding().coerceAtLeast(24.dp),
                            ),
                        )
                    }
                    ScrollShadow(scrollState)
                }
            }
        }
    }
}

private val oneMinutes = 1.minutes.inWholeMilliseconds

@Composable
private fun TimeZones(
    pendingConfirms: SnapshotStateList<() -> Unit>,
    setShareAction: (() -> Unit) -> Unit,
    setResetAction: (() -> Unit) -> Unit,
    setResetButtonVisibility: (Boolean) -> Unit,
) {
    var firstTimeZone by rememberSaveable { mutableStateOf(TimeZone.getDefault()) }
    val utc = TimeZone.getTimeZone("UTC")
    var secondTimeZone by rememberSaveable { mutableStateOf(utc) }
    val clock = remember { mutableLongStateOf(System.currentTimeMillis()) }
    val context = LocalContext.current
    val chooserTitle = stringResource(ConverterScreenMode.TIME_ZONES.title)
    setResetButtonVisibility(
        run {
            val sameClock = abs(clock.longValue - System.currentTimeMillis()) > oneMinutes
            sameClock || firstTimeZone != TimeZone.getDefault() || secondTimeZone != utc
        },
    )
    LaunchedEffect(key1 = Unit) {
        setShareAction {
            context.shareText(
                listOf(firstTimeZone, secondTimeZone).joinToString("\n") { timeZone ->
                    timeZone.displayName + ": " + Clock(
                        GregorianCalendar(timeZone).also {
                            it.timeInMillis = clock.longValue
                        },
                    ).toBasicFormatString()
                },
                chooserTitle,
            )
        }
        setResetAction {
            firstTimeZone = TimeZone.getDefault()
            secondTimeZone = utc
            clock.longValue = System.currentTimeMillis()
        }
    }
    val zones = remember {
        TimeZone.getAvailableIDs().map(TimeZone::getTimeZone).sortedBy { it.rawOffset }
            .toImmutableList()
    }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val difference = run {
        val distance = secondTimeZone.rawOffset.milliseconds - firstTimeZone.rawOffset.milliseconds
        Clock(abs(distance.inWholeMinutes / 60.0)).asRemainingTime(LocalResources.current)
    }
    if (isLandscape) Column {
        Row(Modifier.padding(horizontal = 24.dp)) {
            TimeZoneClock(
                timeZone = firstTimeZone,
                onTimeZoneChange = { firstTimeZone = it },
                clock = clock,
                zones = zones,
                pendingConfirms = pendingConfirms,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            TimeZoneClock(
                timeZone = secondTimeZone,
                onTimeZoneChange = { secondTimeZone = it },
                clock = clock,
                zones = zones,
                pendingConfirms = pendingConfirms,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(4.dp))
        TextWithSlideAnimation(difference)
    } else Column(Modifier.padding(horizontal = 24.dp)) {
        TimeZoneClock(
            timeZone = firstTimeZone,
            onTimeZoneChange = { firstTimeZone = it },
            clock = clock,
            zones = zones,
            pendingConfirms = pendingConfirms,
        )
        Spacer(Modifier.height(4.dp))
        TextWithSlideAnimation(difference)
        Spacer(Modifier.height(4.dp))
        TimeZoneClock(
            timeZone = secondTimeZone,
            onTimeZoneChange = { secondTimeZone = it },
            clock = clock,
            zones = zones,
            pendingConfirms = pendingConfirms,
        )
    }
}

@Composable
private fun Calculator(
    setShareAction: (() -> Unit) -> Unit,
    setResetAction: (() -> Unit) -> Unit,
    setResetButtonVisibility: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    var input by rememberSaveable { mutableStateOf("") }
    setResetButtonVisibility(input.isNotEmpty())
    DisposableEffect(Unit) {
        val preferences = context.preferences
        val currentInput = input
        if (currentInput.isEmpty()) {
            val storedInput = preferences.getString(PREF_CALCULATOR_INPUT, "").orEmpty()
            input = storedInput.ifEmpty { "1d 2h 3m 4s + 4h 5s - 2030s + 28h" }
        }
        onDispose {
            preferences.edit { putString(PREF_CALCULATOR_INPUT, input) }
        }
    }
    val result = runCatching {
        // running this inside a runCatching block is absolutely important
        eval(input)
    }.getOrElse { it.message }.orEmpty()
    val chooserTitle = stringResource(ConverterScreenMode.CALCULATOR.title)
    LaunchedEffect(key1 = Unit) {
        setShareAction { context.shareText(result, chooserTitle) }
        setResetAction { input = "" }
    }
    val defaultTextFieldColors = TextFieldDefaults.colors()
    val textFieldColors = defaultTextFieldColors.copy(
        focusedContainerColor = animateColor(defaultTextFieldColors.focusedContainerColor).value,
        unfocusedContainerColor = animateColor(defaultTextFieldColors.unfocusedContainerColor).value,
    )
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (isLandscape) Row(Modifier.padding(horizontal = 24.dp)) {
        TextField(
            value = input,
            onValueChange = { input = it },
            minLines = 6,
            modifier = Modifier.weight(1f),
            colors = textFieldColors,
        )
        Crossfade(targetState = result, modifier = Modifier.weight(1f)) {
            SelectionContainer {
                Text(
                    it,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                )
            }
        }
    } else Column(Modifier.padding(horizontal = 24.dp)) {
        TextField(
            value = input,
            onValueChange = { input = it },
            minLines = 10,
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors,
        )
        Spacer(Modifier.height(16.dp))
        Crossfade(targetState = result) {
            SelectionContainer {
                Text(
                    it,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun QrCode(
    setShareAction: (() -> Unit) -> Unit,
    setResetAction: (() -> Unit) -> Unit,
    setResetButtonVisibility: (Boolean) -> Unit,
) {
    var input by rememberSaveable { mutableStateOf("https://example.com") }
    LaunchedEffect(key1 = Unit) { setResetAction { input = "" } }
    setResetButtonVisibility(input.isNotEmpty())

    @Composable
    fun ColumnScope.SampleInputButton() {
        var clickCount by remember { mutableIntStateOf(1) }
        OutlinedButton(
            onClick = {
                input = when (clickCount++ % 4) {
                    0 -> "https://example.com"
                    1 -> "WIFI:S:MySSID;T:WPA;P:MyPassWord;;"
                    2 -> "MECARD:N:Smith,John;TEL:123123123;EMAIL:user@example.com;;"
                    else -> """BEGIN:VEVENT
                        |SUMMARY:Event title
                        |DTSTART:20201011T173000Z
                        |DTEND:20201011T173000Z
                        |LOCATION:Location name
                        |DESCRIPTION:Event description
                        |END:VEVENT""".trimMargin()
                }
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp),
        ) { Text(stringResource(R.string.sample_inputs)) }
    }

    @Composable
    fun Qr() = Crossfade(targetState = input) { text -> QrView(text, setShareAction) }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (isLandscape) Row(Modifier.padding(horizontal = 24.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            TextField(
                value = input,
                onValueChange = { input = it },
                minLines = 6,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            SampleInputButton()
        }
        Spacer(Modifier.width(24.dp))
        Box(Modifier.weight(1f)) { Qr() }
    } else Column(Modifier.padding(horizontal = 24.dp)) {
        TextField(
            value = input,
            onValueChange = { input = it },
            minLines = 10,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(24.dp))
        Qr()
        Spacer(Modifier.height(8.dp))
        SampleInputButton()
    }
}

@Composable
private fun ColumnScope.ConverterAndDistance(
    navigateToAstronomy: (Jdn) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    pendingConfirms: SnapshotStateList<() -> Unit>,
    screenMode: ConverterScreenMode,
    setShareAction: (() -> Unit) -> Unit,
    setResetAction: (() -> Unit) -> Unit,
    setResetButtonVisibility: (Boolean) -> Unit,
    today: Jdn,
) {
    var calendar by rememberSaveable { mutableStateOf(mainCalendar) }
    var selectedDate by rememberSaveable { mutableStateOf(today) }
    var secondSelectedDate by rememberSaveable { mutableStateOf(today) }

    setResetButtonVisibility(
        when (screenMode) {
            ConverterScreenMode.CONVERTER -> selectedDate != today
            ConverterScreenMode.DISTANCE -> selectedDate != today || secondSelectedDate != today
            else -> false
        },
    )

    val context = LocalContext.current
    val resources = LocalResources.current
    LaunchedEffect(key1 = screenMode) {
        setResetAction {
            when (screenMode) {
                ConverterScreenMode.CONVERTER -> selectedDate = today
                ConverterScreenMode.DISTANCE -> {
                    selectedDate = today
                    secondSelectedDate = today
                }

                else -> {}
            }
        }
        setShareAction {
            val chooserTitle = resources.getString(screenMode.title)
            if (screenMode == ConverterScreenMode.CONVERTER) {
                val calendarsList =
                    enabledCalendars.takeIf { it.size > 1 } ?: language.defaultCalendars
                val otherCalendars = calendarsList - calendar
                context.shareText(
                    text = listOf(
                        dayTitleSummary(
                            jdn = selectedDate,
                            date = selectedDate on calendar,
                        ),
                        resources.getString(R.string.equivalent_to),
                        otherCalendars.joinToString(spacedComma) {
                            formatDate(date = selectedDate on it)
                        },
                    ).joinToString(separator = " "),
                    chooserTitle = chooserTitle,
                )
            } else if (screenMode == ConverterScreenMode.DISTANCE) {
                context.shareText(
                    text = listOf(
                        calculateDaysDifference(
                            resources,
                            jdn = selectedDate,
                            baseJdn = secondSelectedDate,
                            calendar = calendar,
                        ),
                        formatDate(selectedDate on calendar),
                        formatDate(secondSelectedDate on calendar),
                    ).joinToString("\n"),
                    chooserTitle = chooserTitle,
                )
            }
        }
    }

    @Composable
    fun DaysDistanceSecondPart() {
        Column {
            TextWithSlideAnimation(
                text = calculateDaysDifference(
                    resources = LocalResources.current,
                    jdn = selectedDate,
                    baseJdn = secondSelectedDate,
                    calendar = calendar,
                ),
            )
            DatePicker(
                today = today,
                calendar = calendar,
                jdn = secondSelectedDate,
                pendingConfirms = pendingConfirms,
                setJdn = { secondSelectedDate = it },
            )
        }
    }

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val calendarsList = enabledCalendars.takeIf { it.size > 1 } ?: language.defaultCalendars
    if (calendar !in calendarsList) calendar = mainCalendar
    var isExpanded by rememberSaveable { mutableStateOf(true) }
    if (isLandscape) Row {
        Column(Modifier.weight(1f)) {
            CalendarPicker(
                value = calendar,
                items = calendarsList.toImmutableList(),
                backgroundColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            ) { calendar = it }
            DatePicker(
                today = today,
                calendar = calendar,
                jdn = selectedDate,
                pendingConfirms = pendingConfirms,
                setJdn = { selectedDate = it },
            )
        }
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            AnimatedVisibility(
                visible = screenMode == ConverterScreenMode.CONVERTER,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable { isExpanded = !isExpanded }
                    .padding(top = 12.dp, bottom = 12.dp),
            ) {
                sharedTransitionScope.apply {
                    CalendarsOverview(
                        jdn = selectedDate,
                        today = today,
                        selectedCalendar = calendar,
                        shownCalendars = (calendarsList - calendar).toImmutableList(),
                        isExpanded = isExpanded,
                        navigateToAstronomy = navigateToAstronomy,
                    )
                }
            }
            AnimatedVisibility(visible = screenMode == ConverterScreenMode.DISTANCE) {
                DaysDistanceSecondPart()
            }
        }
    } else {
        CalendarPicker(
            value = calendar,
            items = calendarsList.toImmutableList(),
            backgroundColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        ) { calendar = it }
        DatePicker(
            today = today,
            calendar = calendar,
            jdn = selectedDate,
            pendingConfirms = pendingConfirms,
            setJdn = { selectedDate = it },
        )
        AnimatedVisibility(visible = screenMode == ConverterScreenMode.CONVERTER) {
            val cardColors = CardDefaults.cardColors()
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                elevation = CardDefaults.cardElevation(if (isGradient) 8.dp else 0.dp),
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.padding(top = 16.dp),
                colors = cardColors.copy(
                    containerColor = animateColor(cardColors.containerColor).value,
                    contentColor = animateColor(cardColors.contentColor).value,
                    disabledContainerColor = animateColor(cardColors.disabledContainerColor).value,
                    disabledContentColor = animateColor(cardColors.disabledContentColor).value,
                ),
            ) {
                Spacer(Modifier.height(20.dp))
                Box(Modifier.fillMaxWidth()) {
                    sharedTransitionScope.apply {
                        CalendarsOverview(
                            jdn = selectedDate,
                            today = today,
                            selectedCalendar = calendar,
                            shownCalendars = (calendarsList - calendar).toImmutableList(),
                            isExpanded = isExpanded,
                            navigateToAstronomy = navigateToAstronomy,
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
        AnimatedVisibility(visible = screenMode == ConverterScreenMode.DISTANCE) {
            DaysDistanceSecondPart()
        }
    }

    AnimatedVisibility(
        isAstronomicalExtraFeaturesEnabled && screenMode == ConverterScreenMode.DISTANCE && !(secondSelectedDate == selectedDate && selectedDate == today),
    ) {
        val isPersian = calendar == Calendar.SHAMSI
        val zodiacs = listOf(selectedDate, secondSelectedDate).map {
            if (isPersian || Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                ChineseZodiac.fromPersianCalendar(it.toPersianDate())
            } else ChineseZodiac.fromChineseCalendar(ChineseCalendar(it.toGregorianCalendar().time))
        }
        val resources = LocalResources.current
        TextWithSlideAnimation(
            zodiacs.joinToString(spacedComma) {
                it.format(resources, true, isPersian)
            } + spacedColon + language.formatCompatibility(zodiacs[0] compatibilityWith zodiacs[1]),
        )
    }
}

@Composable
private fun TextWithSlideAnimation(text: String) {
    Box(Modifier.height(calendarPickerHeight() + 16.dp), contentAlignment = Alignment.Center) {
        AnimatedContent(
            targetState = text,
            transitionSpec = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(500),
                ) togetherWith slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(500),
                )
            },
        ) {
            SelectionContainer {
                Text(it, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

private val hoursRange = 0..23
private val minutesRange = 0..59

@Composable
private fun TimeZoneClock(
    timeZone: TimeZone,
    onTimeZoneChange: (TimeZone) -> Unit,
    clock: MutableLongState,
    zones: ImmutableList<TimeZone>,
    pendingConfirms: SnapshotStateList<() -> Unit>,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
            val view = LocalView.current
            NumberPicker(
                modifier = Modifier.weight(3f),
                range = zones.indices,
                value = zones.indexOf(timeZone).coerceAtLeast(0),
                onValueChange = {
                    view.performHapticFeedbackVirtualKey()
                    onTimeZoneChange(zones[it])
                },
                label = {
                    val hoursFraction = (zones[it].rawOffset).milliseconds / 1.hours
                    val (h, m) = Clock(abs(hoursFraction)).toHoursAndMinutesPair()
                    val sign = if (hoursFraction < 0) "-" else "+"
                    val offset = "%s%02d:%02d".format(Locale.ENGLISH, sign, h, m)
                    val id = zones[it].id.replace("_", " ").replace(Regex(".*/"), "")
                    "$id ($offset)"
                },
                disableEdit = true,
                pendingConfirms = pendingConfirms,
            )
            Spacer(Modifier.width(4.dp))
            val time = GregorianCalendar(timeZone).also { it.timeInMillis = clock.longValue }
            NumberPicker(
                modifier = Modifier.weight(1f),
                range = hoursRange,
                value = time[GregorianCalendar.HOUR_OF_DAY],
                onValueChange = { hours ->
                    view.performHapticFeedbackVirtualKey()
                    clock.longValue = GregorianCalendar(timeZone).also {
                        it.timeInMillis = clock.longValue
                        it[GregorianCalendar.HOUR_OF_DAY] = hours
                    }.timeInMillis
                },
                pendingConfirms = pendingConfirms,
            )
            Text(":")
            NumberPicker(
                modifier = Modifier.weight(1f),
                range = minutesRange,
                value = time[GregorianCalendar.MINUTE],
                onValueChange = { minutes ->
                    view.performHapticFeedbackVirtualKey()
                    clock.longValue = GregorianCalendar(timeZone).also {
                        it.timeInMillis = clock.longValue
                        it[GregorianCalendar.MINUTE] = minutes
                    }.timeInMillis
                },
                pendingConfirms = pendingConfirms,
            )
        }
    }
}
