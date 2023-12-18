package com.byagowi.persiancalendar.ui.converter

import android.content.res.Configuration
import android.view.View
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
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
import com.byagowi.persiancalendar.ui.common.CalendarsOverview
import com.byagowi.persiancalendar.ui.common.CalendarsTypesPicker
import com.byagowi.persiancalendar.ui.common.DayPicker
import com.byagowi.persiancalendar.ui.utils.MaterialCornerExtraLargeTop
import com.byagowi.persiancalendar.ui.utils.shareText
import com.byagowi.persiancalendar.utils.ONE_MINUTE_IN_MILLIS
import com.byagowi.persiancalendar.utils.calculateDaysDifference
import com.byagowi.persiancalendar.utils.dateStringOfOtherCalendars
import com.byagowi.persiancalendar.utils.dayTitleSummary
import io.github.persiancalendar.calculator.eval
import kotlinx.coroutines.flow.StateFlow
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(
    openDrawer: () -> Unit,
    viewModel: ConverterViewModel
) {
    val context = LocalContext.current
    var qrShareAction by remember { mutableStateOf({}) }
    val screenMode by viewModel.screenMode.collectAsState()
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    var showMenu by rememberSaveable { mutableStateOf(false) }
                    Box(
                        Modifier
                            .clip(MaterialTheme.shapes.extraLarge)
                            .background(Color.Gray.copy(alpha = .5f))
                            .clickable { showMenu = !showMenu },
                    ) {
                        var spinnerWidth by remember { mutableIntStateOf(0) }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .onSizeChanged { spinnerWidth = it.width },
                        ) {
                            Spacer(Modifier.width(16.dp))
                            Text(stringResource(viewModel.screenMode.value.title))
                            val angle by animateFloatAsState(
                                if (showMenu) 180f else 0f,
                                label = "angle",
                            )
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.rotate(angle),
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            Modifier.defaultMinSize(
                                minWidth = with(LocalDensity.current) { spinnerWidth.toDp() }
                            ),
                        ) {
                            ConverterScreenMode.entries.filter {
                                // Converter doesn't work in Android 5, let's hide it there
                                it != ConverterScreenMode.TimeZones
                                // Disable timezone for now
                                // || Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                            }.forEach {
                                DropdownMenuItem(
                                    text = { Text(stringResource(it.title)) },
                                    onClick = {
                                        showMenu = false
                                        viewModel.changeScreenMode(it)
                                    },
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = LocalContentColor.current,
                    actionIconContentColor = LocalContentColor.current,
                    titleContentColor = LocalContentColor.current,
                ),
                navigationIcon = {
                    IconButton(onClick = { openDrawer() }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(R.string.open_drawer)
                        )
                    }
                },
                actions = {
                    val todayButtonVisibility by viewModel.todayButtonVisibilityEvent.collectAsState(
                        initial = false
                    )
                    AnimatedVisibility(todayButtonVisibility) {
                        IconButton(onClick = {
                            val todayJdn = Jdn.today()
                            viewModel.changeSelectedDate(todayJdn)
                            viewModel.changeSecondSelectedDate(todayJdn)
                            viewModel.resetTimeZoneClock()
                        }) {
                            Icon(
                                ImageVector.vectorResource(R.drawable.ic_restore_modified),
                                contentDescription = stringResource(R.string.return_to_today),
                            )
                        }
                    }
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                        tooltip = { PlainTooltip { Text(text = stringResource(R.string.share)) } },
                        state = rememberTooltipState()
                    ) {
                        IconButton(onClick = {
                            when (screenMode) {
                                ConverterScreenMode.Converter -> {
                                    val jdn = viewModel.selectedDate.value
                                    context.shareText(
                                        listOf(
                                            dayTitleSummary(jdn, jdn.toCalendar(mainCalendar)),
                                            context.getString(R.string.equivalent_to),
                                            dateStringOfOtherCalendars(jdn, spacedComma)
                                        ).joinToString(" ")
                                    )
                                }

                                ConverterScreenMode.Distance -> {
                                    val jdn = viewModel.selectedDate.value
                                    val secondJdn = viewModel.secondSelectedDate.value
                                    context.shareText(
                                        calculateDaysDifference(
                                            context.resources,
                                            jdn,
                                            secondJdn,
                                            calendarType = viewModel.calendar.value
                                        )
                                    )
                                }

                                ConverterScreenMode.Calculator -> {
                                    context.shareText(runCatching {
                                        // running this inside a runCatching block is absolutely important
                                        eval(viewModel.calculatorInputText.value)
                                    }.getOrElse { it.message } ?: "")
                                }

                                ConverterScreenMode.TimeZones -> {
//                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                                    shareText(zoneNames[pickerBinding.timeZone.value] + ": " +
//                                            Clock(
//                                                pickerBinding.clock.hour,
//                                                pickerBinding.clock.minute
//                                            )
//                                                .toBasicFormatString())
//                                } else ""
                                }

                                ConverterScreenMode.QrCode -> qrShareAction()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = stringResource(R.string.share),
                            )
                        }
                    }
                },
            )
        }
    ) { paddingValues ->
        Surface(
            shape = MaterialCornerExtraLargeTop(),
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
                    end = paddingValues.calculateEndPadding(LocalLayoutDirection.current),
                ),
        ) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(24.dp))

                val isLandscape =
                    LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

                // Timezones
                AnimatedVisibility(screenMode == ConverterScreenMode.TimeZones) {
                    // TODO: Enable timezone
                    val zones = remember {
                        TimeZone.getAvailableIDs().map(TimeZone::getTimeZone)
                            .sortedBy { it.rawOffset }
                    }
                    val zoneNames = remember {
                        zones.map {
                            val offset =
                                Clock.fromMinutesCount(it.rawOffset / ONE_MINUTE_IN_MILLIS.toInt())
                                    .toTimeZoneOffsetFormat()
                            val id = it.id.replace("_", " ").replace(Regex(".*/"), "")
                            "$id ($offset)"
                        }.toTypedArray()
                    }
                    if (isLandscape) Row {
                        Box(Modifier.weight(1f)) {
                            TimezoneClock(
                                viewModel,
                                zones,
                                zoneNames,
                                viewModel.firstTimeZone,
                                0
                            )
                        }
                        Box(Modifier.weight(1f)) {
                            TimezoneClock(
                                viewModel,
                                zones,
                                zoneNames,
                                viewModel.secondTimeZone,
                                1
                            )
                        }
                    } else Column {
                        TimezoneClock(viewModel, zones, zoneNames, viewModel.firstTimeZone, 0)
                        TimezoneClock(viewModel, zones, zoneNames, viewModel.secondTimeZone, 1)
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
        ) { it ->
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
        AndroidView(
            factory = {
                val root = QrView(it)
                setShareAction { root.share() }
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
                        else -> "BEGIN:VEVENT\nSUMMARY:Event title\nDTSTART:20201011T173000Z\n" +
                                "DTEND:20201011T173000Z\nLOCATION:Location name\n" +
                                "DESCRIPTION:Event description\nEND:VEVENT"
                    }
                )
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp),
        ) { Text("Sample inputs") }
    }

    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
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
    val jdn by viewModel.selectedDate.collectAsState()
    var isExpanded by rememberSaveable { mutableStateOf(true) }
    if (isLandscape) Row {
        Column(Modifier.weight(1f)) {
            CalendarsTypesPicker(calendar, viewModel::changeCalendar)
            DayPicker(
                calendarType = calendar, jdn = jdn, setJdn = viewModel::changeSelectedDate
            )
        }
        Spacer(Modifier.width(8.dp))
        AnimatedContent(
            screenMode == ConverterScreenMode.Converter,
            Modifier.weight(1f),
            label = "converter/distance second pane",
        ) { state ->
            if (state) CalendarsOverview(
                jdn = jdn,
                selectedCalendar = calendar,
                shownCalendars = enabledCalendars - calendar,
                isExpanded = isExpanded
            ) { isExpanded = !isExpanded } else Column {
                val secondJdn by viewModel.secondSelectedDate.collectAsState()
                DaysDistance(jdn, secondJdn, calendar)
                DayPicker(
                    calendarType = calendar, jdn = jdn, setJdn = viewModel::changeSelectedDate
                )
            }
        }
    } else {
        CalendarsTypesPicker(calendar, viewModel::changeCalendar)
        DayPicker(
            calendarType = calendar, jdn = jdn, setJdn = viewModel::changeSelectedDate
        )
        AnimatedVisibility(screenMode == ConverterScreenMode.Converter) {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                elevation = CardDefaults.cardElevation(8.dp),
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.padding(top = 16.dp),
            ) {
                CalendarsOverview(
                    jdn = jdn,
                    selectedCalendar = calendar,
                    shownCalendars = enabledCalendars - calendar,
                    isExpanded = isExpanded
                ) { isExpanded = !isExpanded }
            }
        }
        AnimatedVisibility(screenMode == ConverterScreenMode.Distance) {
            Column {
                val secondJdn by viewModel.secondSelectedDate.collectAsState()
                DaysDistance(jdn, secondJdn, calendar)
                DayPicker(
                    calendarType = calendar, jdn = jdn, setJdn = viewModel::changeSelectedDate
                )
            }
        }
    }
}

@Composable
private fun DaysDistance(jdn: Jdn, baseJdn: Jdn, calendar: CalendarType) {
    val context = LocalContext.current
    val longAnimationTime = integerResource(android.R.integer.config_longAnimTime)
    AnimatedContent(
        calculateDaysDifference(context.resources, jdn, baseJdn, calendar),
        modifier = Modifier.padding(vertical = 12.dp),
        transitionSpec = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(durationMillis = longAnimationTime)
            ) togetherWith slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(durationMillis = longAnimationTime)
            )
        },
        label = "day distance",
    ) {
        SelectionContainer {
            Text(it, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun TimezoneClock(
    viewModel: ConverterViewModel,
    zones: List<TimeZone>,
    zoneNames: Array<String>,
    flow: StateFlow<TimeZone>,
    i: Int
) {
    val commonClock = viewModel.clock.collectAsState()
    AndroidView(
        factory = {
//            val binding = TimeZoneClockPickerBinding.inflate(it.layoutInflater)
//            binding.timeZone.minValue = 0
//            binding.timeZone.maxValue = zones.size - 1
//            binding.timeZone.displayedValues = zoneNames
//            binding.timeZone.value = zones.indexOf(flow.value)
//            binding.timeZone.setOnValueChangedListener { picker, _, index ->
//                picker.performHapticFeedbackVirtualKey()
//                if (i == 0) viewModel.changeFirstTimeZone(zones[index])
//                else viewModel.changeSecondTimeZone(zones[index])
//            }
//            binding.clock.setOnTimeChangedListener { view, hourOfDay, minute ->
//                view.performHapticFeedbackVirtualKey()
//                viewModel.changeClock(hourOfDay, minute, flow.value)
//            }
//            binding.root
            View(it)
        },
        update = {
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return@AndroidView
//            val binding = TimeZoneClockPickerBinding.bind(it)
//            val clock = GregorianCalendar(flow.value)
//            clock.timeInMillis = commonClock.value.timeInMillis
//            val hour = clock[GregorianCalendar.HOUR_OF_DAY]
//            val clockView = binding.clock
//            if (clockView.hour != hour) clockView.hour = hour
//            val minute = clock[GregorianCalendar.MINUTE]
//            if (clockView.minute != minute) clockView.minute = minute
//            val zoneView = binding.timeZone
//            val zoneIndex = zones.indexOf(flow.value)
//            if (zoneView.value != zoneIndex) zoneView.value = zoneIndex
        },
    )
}
