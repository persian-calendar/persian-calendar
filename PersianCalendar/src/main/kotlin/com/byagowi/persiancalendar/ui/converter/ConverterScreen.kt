package com.byagowi.persiancalendar.ui.converter

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.enabledCalendars
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.ui.common.AppDropdownMenu
import com.byagowi.persiancalendar.ui.common.AppDropdownMenuItem
import com.byagowi.persiancalendar.ui.common.AppIconButton
import com.byagowi.persiancalendar.ui.common.CalendarsOverview
import com.byagowi.persiancalendar.ui.common.CalendarsTypesPicker
import com.byagowi.persiancalendar.ui.common.DatePicker
import com.byagowi.persiancalendar.ui.common.ExpandArrow
import com.byagowi.persiancalendar.ui.common.NavigationOpenDrawerIcon
import com.byagowi.persiancalendar.ui.common.NumberPicker
import com.byagowi.persiancalendar.ui.common.TodayActionButton
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey
import com.byagowi.persiancalendar.ui.utils.shareText
import com.byagowi.persiancalendar.utils.ONE_MINUTE_IN_MILLIS
import com.byagowi.persiancalendar.utils.calculateDaysDifference
import com.byagowi.persiancalendar.utils.dateStringOfOtherCalendars
import com.byagowi.persiancalendar.utils.dayTitleSummary
import com.byagowi.persiancalendar.utils.formatDate
import io.github.persiancalendar.calculator.eval
import java.util.GregorianCalendar
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(
    openDrawer: () -> Unit,
    viewModel: ConverterViewModel,
) {
    var qrShareAction by remember { mutableStateOf({}) }
    val screenMode by viewModel.screenMode.collectAsState()
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    var showMenu by rememberSaveable { mutableStateOf(false) }
                    val hapticFeedback = LocalHapticFeedback.current
                    Box(
                        Modifier
                            .clip(MaterialTheme.shapes.extraLarge)
                            .background(LocalContentColor.current.copy(alpha = .175f))
                            .clickable {
                                showMenu = !showMenu
                                if (showMenu) hapticFeedback.performHapticFeedback(
                                    HapticFeedbackType.LongPress
                                )
                            },
                    ) {
                        var dropDownWidth by remember { mutableIntStateOf(0) }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .onSizeChanged { dropDownWidth = it.width },
                        ) {
                            Spacer(Modifier.width(16.dp))
                            AnimatedContent(
                                targetState = stringResource(screenMode.title),
                                label = "title",
                                transitionSpec = appCrossfadeSpec,
                            ) { Text(it) }
                            ExpandArrow(isExpanded = showMenu)
                            Spacer(Modifier.width(8.dp))
                        }
                        AppDropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            minWidth = with(LocalDensity.current) { dropDownWidth.toDp() },
                        ) {
                            ConverterScreenMode.entries.forEach {
                                AppDropdownMenuItem(
                                    text = { Text(stringResource(it.title)) },
                                    onClick = {
                                        showMenu = false
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.changeScreenMode(it)
                                    },
                                )
                            }
                        }
                    }
                },
                colors = appTopAppBarColors(),
                navigationIcon = { NavigationOpenDrawerIcon(openDrawer) },
                actions = {
                    val todayButtonVisibility by viewModel.todayButtonVisibility.collectAsState()
                    TodayActionButton(visible = todayButtonVisibility) {
                        val todayJdn = Jdn.today()
                        viewModel.changeSelectedDate(todayJdn)
                        viewModel.changeSecondSelectedDate(todayJdn)
                        viewModel.resetTimeZoneClock()
                    }
                    ShareActionButton(viewModel, qrShareAction)
                },
            )
        },
    ) { paddingValues ->
        Surface(
            shape = materialCornerExtraLargeTop(),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
        ) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(24.dp))

                val isLandscape =
                    LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

                // Timezones
                AnimatedVisibility(screenMode == ConverterScreenMode.TimeZones) {
                    val zones = remember {
                        TimeZone.getAvailableIDs().map(TimeZone::getTimeZone)
                            .sortedBy { it.rawOffset }
                    }
                    if (isLandscape) Row(Modifier.padding(horizontal = 24.dp)) {
                        Box(Modifier.weight(1f)) {
                            TimezoneClock(viewModel, zones, isFirst = true)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(Modifier.weight(1f)) {
                            TimezoneClock(viewModel, zones, isFirst = false)
                        }
                    } else Column(Modifier.padding(horizontal = 24.dp)) {
                        TimezoneClock(viewModel, zones, isFirst = true)
                        Spacer(modifier = Modifier.height(8.dp))
                        TimezoneClock(viewModel, zones, isFirst = false)
                    }
                }

                AnimatedVisibility(
                    screenMode == ConverterScreenMode.Converter || screenMode == ConverterScreenMode.Distance
                ) {
                    Column(Modifier.padding(horizontal = 24.dp)) {
                        ConverterAndDistance(viewModel)
                    }
                }

                AnimatedVisibility(screenMode == ConverterScreenMode.Calculator) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Calculator(viewModel)
                    }
                }

                AnimatedVisibility(screenMode == ConverterScreenMode.QrCode) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        QrCode(viewModel) { qrShareAction = it }
                    }
                }

                Spacer(Modifier.height(paddingValues.calculateBottomPadding()))
            }
        }
    }
}

@Composable
private fun ShareActionButton(viewModel: ConverterViewModel, qrShareAction: () -> Unit) {
    val screenMode by viewModel.screenMode.collectAsState()
    val context = LocalContext.current
    AppIconButton(icon = Icons.Default.Share, title = stringResource(R.string.share)) {
        val chooserTitle = context.getString(screenMode.title)
        when (screenMode) {
            ConverterScreenMode.Converter -> {
                val jdn = viewModel.selectedDate.value
                context.shareText(
                    listOf(
                        dayTitleSummary(jdn, jdn.toCalendar(mainCalendar)),
                        context.getString(R.string.equivalent_to),
                        dateStringOfOtherCalendars(jdn, spacedComma)
                    ).joinToString(" "),
                    chooserTitle,
                )
            }

            ConverterScreenMode.Distance -> {
                val jdn = viewModel.selectedDate.value
                val secondJdn = viewModel.secondSelectedDate.value
                context.shareText(
                    listOf(
                        calculateDaysDifference(
                            context.resources,
                            jdn,
                            secondJdn,
                            calendarType = viewModel.calendar.value,
                        ),
                        formatDate(jdn.toCalendar(viewModel.calendar.value)),
                        formatDate(secondJdn.toCalendar(viewModel.calendar.value)),
                    ).joinToString("\n"),
                    chooserTitle,
                )
            }

            ConverterScreenMode.Calculator -> {
                context.shareText(
                    runCatching {
                        // running this inside a runCatching block is absolutely important
                        eval(viewModel.calculatorInputText.value)
                    }.getOrElse { it.message } ?: "",
                    chooserTitle,
                )
            }

            ConverterScreenMode.TimeZones -> {
                context.shareText(
                    listOf(
                        viewModel.firstTimeZone.value,
                        viewModel.secondTimeZone.value,
                    ).joinToString("\n") { timeZone ->
                        timeZone.displayName + ": " + Clock(GregorianCalendar(timeZone).also {
                            it.time = viewModel.clock.value.time
                        }).toBasicFormatString()
                    },
                    chooserTitle,
                )
            }

            ConverterScreenMode.QrCode -> qrShareAction()
        }
    }
}

@Composable
private fun Calculator(viewModel: ConverterViewModel) {
    val inputText = viewModel.calculatorInputText.collectAsState()
    val result = runCatching {
        // running this inside a runCatching block is absolutely important
        eval(inputText.value)
    }.getOrElse { it.message } ?: ""
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (isLandscape) Row(Modifier.padding(horizontal = 24.dp)) {
        TextField(
            value = inputText.value,
            onValueChange = viewModel::changeCalculatorInput,
            minLines = 6,
            modifier = Modifier.weight(1f),
        )
        AnimatedContent(
            result,
            label = "calculator result",
            modifier = Modifier.weight(1f),
        ) {
            Text(
                it,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
            )
        }
    } else Column(Modifier.padding(horizontal = 24.dp)) {
        TextField(
            value = inputText.value,
            onValueChange = viewModel::changeCalculatorInput,
            minLines = 10,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        AnimatedContent(result, label = "calculator result") {
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

@Composable
private fun QrCode(viewModel: ConverterViewModel, setShareAction: (() -> Unit) -> Unit) {
    val inputText = viewModel.qrCodeInputText.collectAsState()

    @Composable
    fun Qr() {
        val surfaceColor = MaterialTheme.colorScheme.surface
        val contentColor = LocalContentColor.current
        AndroidView(
            factory = {
                val root = QrView(it)
                root.setContentColor(contentColor.toArgb())
                setShareAction { root.share(backgroundColor = surfaceColor.toArgb()) }
                root
            },
            update = { it.update(inputText.value) },
        )
    }

    @Composable
    fun ColumnScope.SampleInputButton() {
        var qrLongClickCount by remember { mutableIntStateOf(1) }
        OutlinedButton(
            onClick = {
                viewModel.changeQrCodeInput(
                    when (qrLongClickCount++ % 4) {
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
                )
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp),
        ) { Text(stringResource(R.string.sample_inputs)) }
    }

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (isLandscape) Row(Modifier.padding(horizontal = 24.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            TextField(
                value = inputText.value,
                onValueChange = viewModel::changeQrCodeInput,
                minLines = 6,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            SampleInputButton()
        }
        Spacer(Modifier.width(24.dp))
        Qr()
    } else Column(Modifier.padding(horizontal = 24.dp)) {
        TextField(
            value = inputText.value,
            onValueChange = viewModel::changeQrCodeInput,
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
private fun ConverterAndDistance(viewModel: ConverterViewModel) {
    val screenMode by viewModel.screenMode.collectAsState()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val calendar by viewModel.calendar.collectAsState()
    if (calendar !in enabledCalendars) viewModel.changeCalendar(mainCalendar)
    val jdn by viewModel.selectedDate.collectAsState()
    val today by viewModel.today.collectAsState()
    var isExpanded by rememberSaveable { mutableStateOf(true) }
    if (isLandscape) Row {
        Column(Modifier.weight(1f)) {
            CalendarsTypesPicker(calendar, viewModel::changeCalendar)
            DatePicker(
                calendarType = calendar, jdn = jdn, setJdn = viewModel::changeSelectedDate
            )
        }
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            AnimatedVisibility(visible = screenMode == ConverterScreenMode.Converter) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    CalendarsOverview(
                        jdn = jdn,
                        today = today,
                        selectedCalendar = calendar,
                        shownCalendars = enabledCalendars - calendar,
                        isExpanded = isExpanded
                    ) { isExpanded = !isExpanded }
                }
            }
            AnimatedVisibility(visible = screenMode == ConverterScreenMode.Distance) {
                DaysDistanceSecondPart(viewModel, jdn, calendar)
            }
        }
    } else {
        CalendarsTypesPicker(calendar, viewModel::changeCalendar)
        DatePicker(
            calendarType = calendar, jdn = jdn, setJdn = viewModel::changeSelectedDate
        )
        AnimatedVisibility(
            visible = screenMode == ConverterScreenMode.Converter,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                elevation = CardDefaults.cardElevation(8.dp),
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.padding(top = 16.dp),
            ) {
                Spacer(Modifier.height(20.dp))
                CalendarsOverview(
                    jdn = jdn,
                    today = today,
                    selectedCalendar = calendar,
                    shownCalendars = enabledCalendars - calendar,
                    isExpanded = isExpanded
                ) { isExpanded = !isExpanded }
            }
        }
        AnimatedVisibility(
            visible = screenMode == ConverterScreenMode.Distance,
            modifier = Modifier.fillMaxWidth(),
        ) { DaysDistanceSecondPart(viewModel, jdn, calendar) }
    }
}

@Composable
private fun DaysDistanceSecondPart(
    viewModel: ConverterViewModel,
    jdn: Jdn,
    calendar: CalendarType,
) {
    Column {
        val secondJdn by viewModel.secondSelectedDate.collectAsState()
        DaysDistance(jdn, secondJdn, calendar)
        DatePicker(
            calendarType = calendar,
            jdn = secondJdn,
            setJdn = viewModel::changeSecondSelectedDate,
        )
    }
}

@Composable
private fun DaysDistance(jdn: Jdn, baseJdn: Jdn, calendar: CalendarType) {
    val context = LocalContext.current
    AnimatedContent(
        calculateDaysDifference(context.resources, jdn, baseJdn, calendar),
        modifier = Modifier.padding(vertical = 12.dp),
        transitionSpec = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(500)
            ) togetherWith slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(500)
            )
        },
        label = "day distance",
    ) {
        SelectionContainer {
            Text(it, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

private val hoursRange = 0..23
private val minutesRange = 0..59

@Composable
private fun TimezoneClock(viewModel: ConverterViewModel, zones: List<TimeZone>, isFirst: Boolean) {
    val timeZone by (if (isFirst) viewModel.firstTimeZone else viewModel.secondTimeZone).collectAsState()
    val clock by viewModel.clock.collectAsState()
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val view = LocalView.current
            NumberPicker(
                modifier = Modifier.weight(3f),
                range = zones.indices,
                value = zones.indexOf(timeZone).coerceAtLeast(0),
                onValueChange = {
                    view.performHapticFeedbackVirtualKey()
                    if (isFirst) viewModel.changeFirstTimeZone(zones[it])
                    else viewModel.changeSecondTimeZone(zones[it])
                },
                label = {
                    val offset = Clock.fromMinutesCount(
                        minutes = zones[it].rawOffset / ONE_MINUTE_IN_MILLIS.toInt()
                    ).toTimeZoneOffsetFormat()
                    val id = zones[it].id.replace("_", " ").replace(Regex(".*/"), "")
                    "$id ($offset)"
                },
                disableEdit = true,
            )
            Spacer(modifier = Modifier.width(4.dp))
            val time = GregorianCalendar(timeZone).also { it.time = clock.time }
            NumberPicker(
                modifier = Modifier.weight(1f),
                range = hoursRange,
                value = time[GregorianCalendar.HOUR_OF_DAY],
                onValueChange = { hours ->
                    view.performHapticFeedbackVirtualKey()
                    viewModel.changeClock(GregorianCalendar(timeZone).also {
                        it.time = clock.time
                        it[GregorianCalendar.HOUR_OF_DAY] = hours
                    })
                },
            )
            Text(":")
            NumberPicker(
                modifier = Modifier.weight(1f),
                range = minutesRange,
                value = time[GregorianCalendar.MINUTE],
                onValueChange = { minutes ->
                    view.performHapticFeedbackVirtualKey()
                    viewModel.changeClock(GregorianCalendar(timeZone).also {
                        it.time = clock.time
                        it[GregorianCalendar.MINUTE] = minutes
                    })
                },
            )
        }
    }
}
