package com.byagowi.persiancalendar.ui.converter

import android.content.res.Configuration
import android.icu.util.ChineseCalendar
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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
import androidx.compose.ui.viewinterop.AndroidView
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
import com.byagowi.persiancalendar.ui.common.CalendarsOverview
import com.byagowi.persiancalendar.ui.common.CalendarsTypesPicker
import com.byagowi.persiancalendar.ui.common.DatePicker
import com.byagowi.persiancalendar.ui.common.NavigationNavigateUpIcon
import com.byagowi.persiancalendar.ui.common.NavigationOpenNavigationRailIcon
import com.byagowi.persiancalendar.ui.common.NumberPicker
import com.byagowi.persiancalendar.ui.common.ScreenSurface
import com.byagowi.persiancalendar.ui.common.ScrollShadow
import com.byagowi.persiancalendar.ui.common.ShareActionButton
import com.byagowi.persiancalendar.ui.common.TodayActionButton
import com.byagowi.persiancalendar.ui.common.calendarTypesHeight
import com.byagowi.persiancalendar.ui.theme.animateColor
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey
import com.byagowi.persiancalendar.ui.utils.shareText
import com.byagowi.persiancalendar.utils.calculateDaysDifference
import com.byagowi.persiancalendar.utils.dayTitleSummary
import com.byagowi.persiancalendar.utils.formatDate
import io.github.persiancalendar.calculator.eval
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedTransitionScope.ConverterScreen(
    openNavigationRail: () -> Unit,
    navigateToAstronomy: (Jdn) -> Unit,
    viewModel: ConverterViewModel,
    noBackStackAction: (() -> Unit)?,
) {
    var qrShareAction by remember { mutableStateOf({}) }
    val pendingConfirms = remember { mutableStateListOf<() -> Unit>() }
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    AppScreenModesDropDown(
                        value = viewModel.screenMode,
                        onValueChange = { viewModel.screenMode = it },
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
                    TodayActionButton(visible = viewModel.todayButtonVisibility && !anyPendingConfirm) {
                        val todayJdn = Jdn.today()
                        viewModel.selectedDate = todayJdn
                        viewModel.secondSelectedDate = todayJdn
                        viewModel.resetTimeZoneClock()
                    }
                    AnimatedVisibility(anyPendingConfirm) {
                        AppIconButton(
                            icon = Icons.Default.Done,
                            title = stringResource(R.string.accept),
                            onClick = { pendingConfirms.forEach { it() } },
                        )
                    }
                    AnimatedVisibility(!anyPendingConfirm) {
                        ConverterScreenShareActionButton(viewModel, qrShareAction)
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
                        .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
                ) {
                    val scrollState = rememberScrollState()
                    Column(Modifier.verticalScroll(scrollState)) {
                        Spacer(Modifier.height(24.dp))

                        val screenMode = viewModel.screenMode
                        // Timezones
                        this.AnimatedVisibility(screenMode == ConverterScreenMode.TIME_ZONES) {
                            TimeZones(viewModel, pendingConfirms)
                        }

                        this.AnimatedVisibility(
                            screenMode == ConverterScreenMode.CONVERTER || screenMode == ConverterScreenMode.DISTANCE
                        ) {
                            Column(Modifier.padding(horizontal = 24.dp)) {
                                ConverterAndDistance(
                                    navigateToAstronomy = navigateToAstronomy,
                                    viewModel = viewModel,
                                    sharedTransitionScope = this@ConverterScreen,
                                    pendingConfirms = pendingConfirms,
                                )
                            }
                        }

                        this.AnimatedVisibility(screenMode == ConverterScreenMode.CALCULATOR) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Calculator(viewModel)
                            }
                        }

                        this.AnimatedVisibility(screenMode == ConverterScreenMode.QR_CODE) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                QrCode(viewModel) { qrShareAction = it }
                            }
                        }

                        Spacer(
                            Modifier.height(
                                paddingValues.calculateBottomPadding().coerceAtLeast(24.dp)
                            )
                        )
                    }
                    ScrollShadow(scrollState)
                }
            }
        }
    }
}

@Composable
private fun TimeZones(
    viewModel: ConverterViewModel,
    pendingConfirms: MutableCollection<() -> Unit>,
) {
    val zones = remember {
        TimeZone.getAvailableIDs().map(TimeZone::getTimeZone).sortedBy { it.rawOffset }
    }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val difference = run {
        val distance =
            viewModel.secondTimeZone.rawOffset.milliseconds - viewModel.firstTimeZone.rawOffset.milliseconds
        Clock(abs(distance.inWholeMinutes / 60.0)).asRemainingTime(LocalResources.current)
    }
    if (isLandscape) Column {
        Row(Modifier.padding(horizontal = 24.dp)) {
            TimezoneClock(viewModel, zones, pendingConfirms, Modifier.weight(1f), isFirst = true)
            Spacer(Modifier.width(8.dp))
            TimezoneClock(viewModel, zones, pendingConfirms, Modifier.weight(1f), isFirst = false)
        }
        Spacer(Modifier.height(4.dp))
        TextWithSlideAnimation(difference)
    } else Column(Modifier.padding(horizontal = 24.dp)) {
        TimezoneClock(viewModel, zones, pendingConfirms, isFirst = true)
        Spacer(Modifier.height(4.dp))
        TextWithSlideAnimation(difference)
        Spacer(Modifier.height(4.dp))
        TimezoneClock(viewModel, zones, pendingConfirms, isFirst = false)
    }
}

@Composable
private fun SharedTransitionScope.ConverterScreenShareActionButton(
    viewModel: ConverterViewModel,
    qrShareAction: () -> Unit,
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    ShareActionButton {
        val chooserTitle = resources.getString(viewModel.screenMode.title)
        when (viewModel.screenMode) {
            ConverterScreenMode.CONVERTER -> {
                val calendarsList =
                    enabledCalendars.takeIf { it.size > 1 } ?: language.defaultCalendars
                val otherCalendars = calendarsList - viewModel.calendar
                context.shareText(
                    text = listOf(
                        dayTitleSummary(
                            jdn = viewModel.selectedDate,
                            date = viewModel.selectedDate on viewModel.calendar
                        ),
                        resources.getString(R.string.equivalent_to),
                        otherCalendars.joinToString(spacedComma) {
                            formatDate(date = viewModel.selectedDate on it)
                        }).joinToString(separator = " "),
                    chooserTitle = chooserTitle,
                )
            }

            ConverterScreenMode.DISTANCE -> {
                context.shareText(
                    text = listOf(
                        calculateDaysDifference(
                            resources,
                            jdn = viewModel.selectedDate,
                            baseJdn = viewModel.secondSelectedDate,
                            calendar = viewModel.calendar,
                        ),
                        formatDate(viewModel.selectedDate on viewModel.calendar),
                        formatDate(viewModel.secondSelectedDate on viewModel.calendar),
                    ).joinToString("\n"),
                    chooserTitle = chooserTitle,
                )
            }

            ConverterScreenMode.CALCULATOR -> {
                context.shareText(
                    runCatching {
                        // running this inside a runCatching block is absolutely important
                        eval(viewModel.calculatorInputText)
                    }.getOrElse { it.message }.orEmpty(),
                    chooserTitle,
                )
            }

            ConverterScreenMode.TIME_ZONES -> {
                context.shareText(
                    listOf(
                        viewModel.firstTimeZone,
                        viewModel.secondTimeZone,
                    ).joinToString("\n") { timeZone ->
                        timeZone.displayName + ": " + Clock(GregorianCalendar(timeZone).also {
                            it.time = viewModel.clock.time
                        }).toBasicFormatString()
                    },
                    chooserTitle,
                )
            }

            ConverterScreenMode.QR_CODE -> qrShareAction()
        }
    }
}

@Composable
private fun Calculator(viewModel: ConverterViewModel) {
    val result = runCatching {
        // running this inside a runCatching block is absolutely important
        eval(viewModel.calculatorInputText)
    }.getOrElse { it.message }.orEmpty()
    val defaultTextFieldColors = TextFieldDefaults.colors()
    val textFieldColors = defaultTextFieldColors.copy(
        focusedContainerColor = animateColor(defaultTextFieldColors.focusedContainerColor).value,
        unfocusedContainerColor = animateColor(defaultTextFieldColors.unfocusedContainerColor).value,
    )
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (isLandscape) Row(Modifier.padding(horizontal = 24.dp)) {
        TextField(
            value = viewModel.calculatorInputText,
            onValueChange = { viewModel.calculatorInputText = it },
            minLines = 6,
            modifier = Modifier.weight(1f),
            colors = textFieldColors,
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
            value = viewModel.calculatorInputText,
            onValueChange = { viewModel.calculatorInputText = it },
            minLines = 10,
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors,
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
            update = { it.update(viewModel.qrCodeInputText) },
        )
    }

    @Composable
    fun ColumnScope.SampleInputButton() {
        var qrLongClickCount by remember { mutableIntStateOf(1) }
        OutlinedButton(
            onClick = {
                viewModel.qrCodeInputText = when (qrLongClickCount++ % 4) {
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

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (isLandscape) Row(Modifier.padding(horizontal = 24.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            TextField(
                value = viewModel.qrCodeInputText,
                onValueChange = { viewModel.qrCodeInputText = it },
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
            value = viewModel.qrCodeInputText,
            onValueChange = { viewModel.qrCodeInputText = it },
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
    viewModel: ConverterViewModel,
    sharedTransitionScope: SharedTransitionScope,
    pendingConfirms: MutableCollection<() -> Unit>,
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val calendarsList = enabledCalendars.takeIf { it.size > 1 } ?: language.defaultCalendars
    if (viewModel.calendar !in calendarsList) viewModel.calendar = mainCalendar
    var isExpanded by rememberSaveable { mutableStateOf(true) }
    if (isLandscape) Row {
        Column(Modifier.weight(1f)) {
            CalendarsTypesPicker(
                value = viewModel.calendar,
                items = calendarsList,
                backgroundColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            ) { viewModel.calendar = it }
            DatePicker(
                calendar = viewModel.calendar,
                jdn = viewModel.selectedDate,
                pendingConfirms = pendingConfirms,
                setJdn = { viewModel.selectedDate = it },
            )
        }
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            this.AnimatedVisibility(
                visible = viewModel.screenMode == ConverterScreenMode.CONVERTER,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable { isExpanded = !isExpanded }
                    .padding(top = 12.dp, bottom = 12.dp),
            ) {
                sharedTransitionScope.apply {
                    CalendarsOverview(
                        jdn = viewModel.selectedDate,
                        today = viewModel.today,
                        selectedCalendar = viewModel.calendar,
                        shownCalendars = calendarsList - viewModel.calendar,
                        isExpanded = isExpanded,
                        navigateToAstronomy = navigateToAstronomy,
                    )
                }
            }
            this.AnimatedVisibility(visible = viewModel.screenMode == ConverterScreenMode.DISTANCE) {
                DaysDistanceSecondPart(
                    viewModel = viewModel,
                    jdn = viewModel.selectedDate,
                    calendar = viewModel.calendar,
                    pendingConfirms = pendingConfirms,
                )
            }
        }
    } else {
        CalendarsTypesPicker(
            value = viewModel.calendar,
            items = calendarsList,
            backgroundColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        ) { viewModel.calendar = it }
        DatePicker(
            calendar = viewModel.calendar,
            jdn = viewModel.selectedDate,
            pendingConfirms = pendingConfirms,
            setJdn = { viewModel.selectedDate = it },
        )
        this.AnimatedVisibility(visible = viewModel.screenMode == ConverterScreenMode.CONVERTER) {
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
                )
            ) {
                Spacer(Modifier.height(20.dp))
                Box(Modifier.fillMaxWidth()) {
                    sharedTransitionScope.apply {
                        CalendarsOverview(
                            jdn = viewModel.selectedDate,
                            today = viewModel.today,
                            selectedCalendar = viewModel.calendar,
                            shownCalendars = calendarsList - viewModel.calendar,
                            isExpanded = isExpanded,
                            navigateToAstronomy = navigateToAstronomy,
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
        this.AnimatedVisibility(visible = viewModel.screenMode == ConverterScreenMode.DISTANCE) {
            DaysDistanceSecondPart(
                viewModel = viewModel,
                jdn = viewModel.selectedDate,
                calendar = viewModel.calendar,
                pendingConfirms = pendingConfirms,
            )
        }
    }

    val isAstronomicalExtraFeaturesEnabled by isAstronomicalExtraFeaturesEnabled.collectAsState()
    this.AnimatedVisibility(
        isAstronomicalExtraFeaturesEnabled && viewModel.screenMode == ConverterScreenMode.DISTANCE && !(viewModel.secondSelectedDate == viewModel.selectedDate && viewModel.selectedDate == viewModel.today)
    ) {
        val isPersian = viewModel.calendar == Calendar.SHAMSI
        val zodiacs = listOf(viewModel.selectedDate, viewModel.secondSelectedDate).map {
            if (isPersian || Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                ChineseZodiac.fromPersianCalendar(it.toPersianDate())
            } else ChineseZodiac.fromChineseCalendar(ChineseCalendar(it.toGregorianCalendar().time))
        }
        val resources = LocalResources.current
        TextWithSlideAnimation(zodiacs.joinToString(spacedComma) {
            it.format(resources, true, isPersian)
        } + spacedColon + language.formatCompatibility(zodiacs[0] compatibilityWith zodiacs[1]))
    }
}

@Composable
private fun DaysDistanceSecondPart(
    viewModel: ConverterViewModel,
    jdn: Jdn,
    calendar: Calendar,
    pendingConfirms: MutableCollection<() -> Unit>,
) {
    Column {
        TextWithSlideAnimation(
            text = calculateDaysDifference(
                resources = LocalResources.current,
                jdn = jdn,
                baseJdn = viewModel.secondSelectedDate,
                calendar = calendar
            )
        )
        DatePicker(
            calendar = calendar,
            jdn = viewModel.secondSelectedDate,
            pendingConfirms = pendingConfirms,
            setJdn = { viewModel.secondSelectedDate = it },
        )
    }
}

@Composable
private fun TextWithSlideAnimation(text: String) {
    Box(Modifier.height(calendarTypesHeight() + 16.dp), contentAlignment = Alignment.Center) {
        AnimatedContent(
            text,
            transitionSpec = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(500)
                ) togetherWith slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(500)
                )
            },
            label = "slide text",
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
    viewModel: ConverterViewModel,
    zones: List<TimeZone>,
    pendingConfirms: MutableCollection<() -> Unit>,
    modifier: Modifier = Modifier,
    isFirst: Boolean,
) {
    val timeZone = if (isFirst) viewModel.firstTimeZone else viewModel.secondTimeZone
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
            val view = LocalView.current
            NumberPicker(
                modifier = Modifier.weight(3f),
                range = zones.indices,
                value = zones.indexOf(timeZone).coerceAtLeast(0),
                onValueChange = {
                    view.performHapticFeedbackVirtualKey()
                    if (isFirst) viewModel.firstTimeZone = zones[it]
                    else viewModel.secondTimeZone = zones[it]
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
            val time = GregorianCalendar(timeZone).also { it.time = viewModel.clock.time }
            NumberPicker(
                modifier = Modifier.weight(1f),
                range = hoursRange,
                value = time[GregorianCalendar.HOUR_OF_DAY],
                onValueChange = { hours ->
                    view.performHapticFeedbackVirtualKey()
                    viewModel.clock = GregorianCalendar(timeZone).also {
                        it.time = viewModel.clock.time
                        it[GregorianCalendar.HOUR_OF_DAY] = hours
                    }
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
                    viewModel.clock = GregorianCalendar(timeZone).also {
                        it.time = viewModel.clock.time
                        it[GregorianCalendar.MINUTE] = minutes
                    }
                },
                pendingConfirms = pendingConfirms,
            )
        }
    }
}
