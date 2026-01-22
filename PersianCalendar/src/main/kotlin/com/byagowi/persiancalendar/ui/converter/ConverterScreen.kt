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
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.byagowi.persiancalendar.ui.common.AppScreenModesDropDown
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
import com.byagowi.persiancalendar.ui.updatedToday
import com.byagowi.persiancalendar.ui.utils.JdnSaver
import com.byagowi.persiancalendar.ui.utils.TimeZoneSaver
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey
import com.byagowi.persiancalendar.ui.utils.shareText
import com.byagowi.persiancalendar.utils.calculateDaysDifference
import com.byagowi.persiancalendar.utils.dayTitleSummary
import com.byagowi.persiancalendar.utils.formatDate
import com.byagowi.persiancalendar.utils.preferences
import io.github.persiancalendar.calculator.eval
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
    initialScreenMode: ConverterScreenMode = ConverterScreenMode.entries[0],
) {
    val today = updatedToday()
    val calendar = rememberSaveable { mutableStateOf(mainCalendar) }
    val selectedDate = rememberSaveable(saver = JdnSaver) { mutableStateOf(today) }
    val secondSelectedDate = rememberSaveable(saver = JdnSaver) { mutableStateOf(today) }
    val screenMode = rememberSaveable { mutableStateOf(initialScreenMode) }
    val calculatorInputText = rememberSaveable { mutableStateOf("") }
    val qrCodeInputText = rememberSaveable { mutableStateOf("https://example.com") }
    val firstTimeZone = rememberSaveable(saver = TimeZoneSaver) {
        mutableStateOf(TimeZone.getDefault())
    }
    val utc = TimeZone.getTimeZone("UTC")
    val secondTimeZone = rememberSaveable(saver = TimeZoneSaver) {
        mutableStateOf(utc)
    }
    val clock = remember { mutableLongStateOf(System.currentTimeMillis()) }

    val todayButtonVisibility by remember {
        derivedStateOf {
            todayButtonVisibility(
                screenMode.value,
                calculatorInputText.value,
                qrCodeInputText.value,
                today,
                selectedDate.value,
                secondSelectedDate.value,
                clock.longValue,
                firstTimeZone.value,
                secondTimeZone.value,
                utc,
            )
        }
    }

    var qrShareAction by remember { mutableStateOf({}) }
    val pendingConfirms = remember { mutableStateListOf<() -> Unit>() }
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class) TopAppBar(
                title = {
                    AppScreenModesDropDown(
                        value = screenMode.value,
                        onValueChange = { screenMode.value = it },
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
                    TodayActionButton(visible = todayButtonVisibility && !anyPendingConfirm) {
                        when (screenMode.value) {
                            ConverterScreenMode.CALCULATOR -> {
                                calculatorInputText.value = ""
                            }

                            ConverterScreenMode.CONVERTER -> {
                                selectedDate.value = today
                            }

                            ConverterScreenMode.DISTANCE -> {
                                selectedDate.value = today
                                secondSelectedDate.value = today
                            }

                            ConverterScreenMode.TIME_ZONES -> {
                                firstTimeZone.value = TimeZone.getDefault()
                                secondTimeZone.value = utc
                                clock.longValue = System.currentTimeMillis()
                            }

                            ConverterScreenMode.QR_CODE -> {
                                qrCodeInputText.value = ""
                            }
                        }
                    }
                    AnimatedVisibility(anyPendingConfirm) {
                        AppIconButton(
                            icon = Icons.Default.Done,
                            title = stringResource(R.string.accept),
                            onClick = { pendingConfirms.forEach { it() } },
                        )
                    }
                    AnimatedVisibility(visible = !anyPendingConfirm) {
                        ConverterScreenShareActionButton(
                            screenMode.value,
                            calendar.value,
                            selectedDate.value,
                            secondSelectedDate.value,
                            calculatorInputText.value,
                            firstTimeZone.value,
                            secondTimeZone.value,
                            clock.longValue,
                            qrShareAction,
                        )
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

                        // Timezones
                        AnimatedVisibility(screenMode.value == ConverterScreenMode.TIME_ZONES) {
                            TimeZones(firstTimeZone, secondTimeZone, clock, pendingConfirms)
                        }

                        AnimatedVisibility(
                            when (screenMode.value) {
                                ConverterScreenMode.CONVERTER, ConverterScreenMode.DISTANCE -> true
                                else -> false
                            },
                        ) {
                            Column(Modifier.padding(horizontal = 24.dp)) {
                                ConverterAndDistance(
                                    navigateToAstronomy = navigateToAstronomy,
                                    sharedTransitionScope = this@ConverterScreen,
                                    pendingConfirms = pendingConfirms,
                                    today = today,
                                    calendar = calendar,
                                    selectedDate = selectedDate,
                                    secondSelectedDate = secondSelectedDate,
                                    screenMode = screenMode,
                                )
                            }
                        }

                        AnimatedVisibility(screenMode.value == ConverterScreenMode.CALCULATOR) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Calculator(calculatorInputText)
                            }
                        }

                        AnimatedVisibility(screenMode.value == ConverterScreenMode.QR_CODE) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                QrCode(qrCodeInputText) { qrShareAction = it }
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

private fun todayButtonVisibility(
    screenMode: ConverterScreenMode,
    calculatorInputText: String,
    qrCodeInputText: String,
    today: Jdn,
    selectedDate: Jdn,
    secondSelectedDate: Jdn,
    clock: Long,
    firstTimeZone: TimeZone,
    secondTimeZone: TimeZone,
    utc: TimeZone,
): Boolean {
    return when (screenMode) {
        ConverterScreenMode.CALCULATOR -> calculatorInputText.isNotEmpty()
        ConverterScreenMode.QR_CODE -> qrCodeInputText.isNotEmpty()
        ConverterScreenMode.CONVERTER -> selectedDate != today
        ConverterScreenMode.DISTANCE -> selectedDate != today || secondSelectedDate != today
        ConverterScreenMode.TIME_ZONES -> {
            val sameClock = abs(clock - System.currentTimeMillis()) > oneMinutes
            sameClock || firstTimeZone != TimeZone.getDefault() || secondTimeZone != utc
        }
    }
}

@Composable
private fun TimeZones(
    firstTimeZone: MutableState<TimeZone>,
    secondTimeZone: MutableState<TimeZone>,
    clock: MutableLongState,
    pendingConfirms: MutableCollection<() -> Unit>,
) {
    val zones = remember {
        TimeZone.getAvailableIDs().map(TimeZone::getTimeZone).sortedBy { it.rawOffset }
    }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val difference = run {
        val distance =
            secondTimeZone.value.rawOffset.milliseconds - firstTimeZone.value.rawOffset.milliseconds
        Clock(abs(distance.inWholeMinutes / 60.0)).asRemainingTime(LocalResources.current)
    }
    if (isLandscape) Column {
        Row(Modifier.padding(horizontal = 24.dp)) {
            TimezoneClock(
                firstTimeZone,
                secondTimeZone,
                clock,
                zones,
                pendingConfirms,
                Modifier.weight(1f),
                isFirst = true,
            )
            Spacer(Modifier.width(8.dp))
            TimezoneClock(
                firstTimeZone,
                secondTimeZone,
                clock,
                zones,
                pendingConfirms,
                Modifier.weight(1f),
                isFirst = false,
            )
        }
        Spacer(Modifier.height(4.dp))
        TextWithSlideAnimation(difference)
    } else Column(Modifier.padding(horizontal = 24.dp)) {
        TimezoneClock(firstTimeZone, secondTimeZone, clock, zones, pendingConfirms, isFirst = true)
        Spacer(Modifier.height(4.dp))
        TextWithSlideAnimation(difference)
        Spacer(Modifier.height(4.dp))
        TimezoneClock(firstTimeZone, secondTimeZone, clock, zones, pendingConfirms, isFirst = false)
    }
}

@Composable
private fun SharedTransitionScope.ConverterScreenShareActionButton(
    screenMode: ConverterScreenMode,
    calendar: Calendar,
    selectedDate: Jdn,
    secondSelectedDate: Jdn,
    calculatorInputText: String,
    firstTimeZone: TimeZone,
    secondTimeZone: TimeZone,
    clock: Long,
    qrShareAction: () -> Unit,
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    ShareActionButton {
        val chooserTitle = resources.getString(screenMode.title)
        when (screenMode) {
            ConverterScreenMode.CONVERTER -> {
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
            }

            ConverterScreenMode.DISTANCE -> {
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

            ConverterScreenMode.CALCULATOR -> {
                context.shareText(
                    runCatching {
                        // running this inside a runCatching block is absolutely important
                        eval(calculatorInputText)
                    }.getOrElse { it.message }.orEmpty(),
                    chooserTitle,
                )
            }

            ConverterScreenMode.TIME_ZONES -> {
                context.shareText(
                    listOf(
                        firstTimeZone,
                        secondTimeZone,
                    ).joinToString("\n") { timeZone ->
                        timeZone.displayName + ": " + Clock(
                            GregorianCalendar(timeZone).also {
                                it.timeInMillis = clock
                            },
                        ).toBasicFormatString()
                    },
                    chooserTitle,
                )
            }

            ConverterScreenMode.QR_CODE -> qrShareAction()
        }
    }
}

@Composable
private fun Calculator(
    calculatorInputText: MutableState<String>,
) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val preferences = context.preferences
        val currentInput = calculatorInputText.value
        if (currentInput.isEmpty()) {
            val storedInput = preferences.getString(PREF_CALCULATOR_INPUT, "").orEmpty()
            calculatorInputText.value = storedInput.ifEmpty {
                "1d 2h 3m 4s + 4h 5s - 2030s + 28h"
            }
        }
        onDispose {
            preferences.edit { putString(PREF_CALCULATOR_INPUT, calculatorInputText.value) }
        }
    }
    val result = runCatching {
        // running this inside a runCatching block is absolutely important
        eval(calculatorInputText.value)
    }.getOrElse { it.message }.orEmpty()
    val defaultTextFieldColors = TextFieldDefaults.colors()
    val textFieldColors = defaultTextFieldColors.copy(
        focusedContainerColor = animateColor(defaultTextFieldColors.focusedContainerColor).value,
        unfocusedContainerColor = animateColor(defaultTextFieldColors.unfocusedContainerColor).value,
    )
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (isLandscape) Row(Modifier.padding(horizontal = 24.dp)) {
        TextField(
            value = calculatorInputText.value,
            onValueChange = { calculatorInputText.value = it },
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
            value = calculatorInputText.value,
            onValueChange = { calculatorInputText.value = it },
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
    qrCodeInputText: MutableState<String>,
    setShareAction: (() -> Unit) -> Unit,
) {
    @Composable
    fun ColumnScope.SampleInputButton(
        qrCodeInputText: MutableState<String>,
    ) {
        var qrLongClickCount by remember { mutableIntStateOf(1) }
        OutlinedButton(
            onClick = {
                qrCodeInputText.value = when (qrLongClickCount++ % 4) {
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
    fun Qr() {
        Crossfade(targetState = qrCodeInputText.value) { text -> QrView(text, setShareAction) }
    }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (isLandscape) Row(Modifier.padding(horizontal = 24.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            TextField(
                value = qrCodeInputText.value,
                onValueChange = { qrCodeInputText.value = it },
                minLines = 6,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            SampleInputButton(qrCodeInputText)
        }
        Spacer(Modifier.width(24.dp))
        Box(Modifier.weight(1f)) { Qr() }
    } else Column(Modifier.padding(horizontal = 24.dp)) {
        TextField(
            value = qrCodeInputText.value,
            onValueChange = { qrCodeInputText.value = it },
            minLines = 10,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(24.dp))
        Qr()
        Spacer(Modifier.height(8.dp))
        SampleInputButton(qrCodeInputText)
    }
}

@Composable
private fun ColumnScope.ConverterAndDistance(
    navigateToAstronomy: (Jdn) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    pendingConfirms: MutableCollection<() -> Unit>,
    screenMode: MutableState<ConverterScreenMode>,
    selectedDate: MutableState<Jdn>,
    secondSelectedDate: MutableState<Jdn>,
    today: Jdn,
    calendar: MutableState<Calendar>,
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val calendarsList = enabledCalendars.takeIf { it.size > 1 } ?: language.defaultCalendars
    if (calendar.value !in calendarsList) calendar.value = mainCalendar
    var isExpanded by rememberSaveable { mutableStateOf(true) }
    if (isLandscape) Row {
        Column(Modifier.weight(1f)) {
            CalendarPicker(
                value = calendar.value,
                items = calendarsList,
                backgroundColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            ) { calendar.value = it }
            DatePicker(
                calendar = calendar.value,
                jdn = selectedDate.value,
                pendingConfirms = pendingConfirms,
                setJdn = { selectedDate.value = it },
            )
        }
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            AnimatedVisibility(
                visible = screenMode.value == ConverterScreenMode.CONVERTER,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable { isExpanded = !isExpanded }
                    .padding(top = 12.dp, bottom = 12.dp),
            ) {
                sharedTransitionScope.apply {
                    CalendarsOverview(
                        jdn = selectedDate.value,
                        today = today,
                        selectedCalendar = calendar.value,
                        shownCalendars = calendarsList - calendar.value,
                        isExpanded = isExpanded,
                        navigateToAstronomy = navigateToAstronomy,
                    )
                }
            }
            AnimatedVisibility(visible = screenMode.value == ConverterScreenMode.DISTANCE) {
                DaysDistanceSecondPart(
                    secondSelectedDate = secondSelectedDate,
                    jdn = selectedDate.value,
                    calendar = calendar.value,
                    pendingConfirms = pendingConfirms,
                )
            }
        }
    } else {
        CalendarPicker(
            value = calendar.value,
            items = calendarsList,
            backgroundColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        ) { calendar.value = it }
        DatePicker(
            calendar = calendar.value,
            jdn = selectedDate.value,
            pendingConfirms = pendingConfirms,
            setJdn = { selectedDate.value = it },
        )
        AnimatedVisibility(visible = screenMode.value == ConverterScreenMode.CONVERTER) {
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
                            jdn = selectedDate.value,
                            today = today,
                            selectedCalendar = calendar.value,
                            shownCalendars = calendarsList - calendar.value,
                            isExpanded = isExpanded,
                            navigateToAstronomy = navigateToAstronomy,
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
        AnimatedVisibility(visible = screenMode.value == ConverterScreenMode.DISTANCE) {
            DaysDistanceSecondPart(
                secondSelectedDate = secondSelectedDate,
                jdn = selectedDate.value,
                calendar = calendar.value,
                pendingConfirms = pendingConfirms,
            )
        }
    }

    AnimatedVisibility(
        isAstronomicalExtraFeaturesEnabled && screenMode.value == ConverterScreenMode.DISTANCE && !(secondSelectedDate == selectedDate && selectedDate.value == today),
    ) {
        val isPersian = calendar.value == Calendar.SHAMSI
        val zodiacs = listOf(selectedDate.value, secondSelectedDate.value).map {
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
private fun DaysDistanceSecondPart(
    secondSelectedDate: MutableState<Jdn>,
    jdn: Jdn,
    calendar: Calendar,
    pendingConfirms: MutableCollection<() -> Unit>,
) {
    Column {
        TextWithSlideAnimation(
            text = calculateDaysDifference(
                resources = LocalResources.current,
                jdn = jdn,
                baseJdn = secondSelectedDate.value,
                calendar = calendar,
            ),
        )
        DatePicker(
            calendar = calendar,
            jdn = secondSelectedDate.value,
            pendingConfirms = pendingConfirms,
            setJdn = { secondSelectedDate.value = it },
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
private fun TimezoneClock(
    firstTimeZone: MutableState<TimeZone>,
    secondTimeZone: MutableState<TimeZone>,
    clock: MutableLongState,
    zones: List<TimeZone>,
    pendingConfirms: MutableCollection<() -> Unit>,
    modifier: Modifier = Modifier,
    isFirst: Boolean,
) {
    val timeZone = if (isFirst) firstTimeZone else secondTimeZone
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
            val view = LocalView.current
            NumberPicker(
                modifier = Modifier.weight(3f),
                range = zones.indices,
                value = zones.indexOf(timeZone.value).coerceAtLeast(0),
                onValueChange = {
                    view.performHapticFeedbackVirtualKey()
                    timeZone.value = zones[it]
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
            val time = GregorianCalendar(timeZone.value).also { it.timeInMillis = clock.longValue }
            NumberPicker(
                modifier = Modifier.weight(1f),
                range = hoursRange,
                value = time[GregorianCalendar.HOUR_OF_DAY],
                onValueChange = { hours ->
                    view.performHapticFeedbackVirtualKey()
                    clock.longValue = GregorianCalendar(timeZone.value).also {
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
                    clock.longValue = GregorianCalendar(timeZone.value).also {
                        it.timeInMillis = clock.longValue
                        it[GregorianCalendar.MINUTE] = minutes
                    }.timeInMillis
                },
                pendingConfirms = pendingConfirms,
            )
        }
    }
}
