package com.byagowi.persiancalendar.ui.converter

import android.content.res.Configuration
import android.icu.util.ChineseCalendar
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ConverterScreen(
    animatedContentScope: AnimatedContentScope,
    openNavigationRail: () -> Unit,
    navigateToAstronomy: (Jdn) -> Unit,
    viewModel: ConverterViewModel,
    noBackStackAction: (() -> Unit)?,
) {
    var qrShareAction by remember { mutableStateOf({}) }
    val screenMode by viewModel.screenMode.collectAsState()
    val pendingConfirms = remember { mutableStateListOf<() -> Unit>() }
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    AppScreenModesDropDown(
                        value = screenMode,
                        onValueChange = { viewModel.changeScreenMode(it) },
                        label = { stringResource(it.title) },
                        values = ConverterScreenMode.entries,
                    )
                },
                colors = appTopAppBarColors(),
                navigationIcon = {
                    if (noBackStackAction != null) NavigationNavigateUpIcon(noBackStackAction)
                    else NavigationOpenNavigationRailIcon(animatedContentScope, openNavigationRail)
                },
                actions = {
                    val anyPendingConfirm = pendingConfirms.isNotEmpty()
                    val todayButtonVisibility by viewModel.todayButtonVisibility.collectAsState()
                    TodayActionButton(visible = todayButtonVisibility && !anyPendingConfirm) {
                        val todayJdn = Jdn.today()
                        viewModel.changeSelectedDate(todayJdn)
                        viewModel.changeSecondSelectedDate(todayJdn)
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
                        ConverterScreenShareActionButton(
                            animatedContentScope,
                            viewModel,
                            qrShareAction,
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(Modifier.padding(top = paddingValues.calculateTopPadding())) {
            ScreenSurface(animatedContentScope) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
                ) {
                    val scrollState = rememberScrollState()
                    Column(Modifier.verticalScroll(scrollState)) {
                        Spacer(Modifier.height(24.dp))

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
                                    animatedContentScope = animatedContentScope,
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
        TimeZone.getAvailableIDs().map(TimeZone::getTimeZone)
            .sortedBy { it.rawOffset }
    }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val difference = run {
        val firstTimeZone by viewModel.firstTimeZone.collectAsState()
        val secondTimeZone by viewModel.secondTimeZone.collectAsState()
        val distance = secondTimeZone.rawOffset.milliseconds - firstTimeZone.rawOffset.milliseconds
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.ConverterScreenShareActionButton(
    animatedContentScope: AnimatedContentScope,
    viewModel: ConverterViewModel,
    qrShareAction: () -> Unit,
) {
    val screenMode by viewModel.screenMode.collectAsState()
    val context = LocalContext.current
    val resources = LocalResources.current
    ShareActionButton(animatedContentScope) {
        val chooserTitle = context.getString(screenMode.title)
        when (screenMode) {
            ConverterScreenMode.CONVERTER -> {
                val jdn = viewModel.selectedDate.value
                val selectedCalendar = viewModel.calendar.value
                val calendarsList =
                    enabledCalendars.takeIf { it.size > 1 } ?: language.value.defaultCalendars
                val otherCalendars = calendarsList - selectedCalendar
                context.shareText(
                    listOf(
                        dayTitleSummary(jdn, jdn on selectedCalendar),
                        context.getString(R.string.equivalent_to),
                        otherCalendars.joinToString(spacedComma) { formatDate(jdn on it) }
                    ).joinToString(" "),
                    chooserTitle,
                )
            }

            ConverterScreenMode.DISTANCE -> {
                val jdn = viewModel.selectedDate.value
                val secondJdn = viewModel.secondSelectedDate.value
                context.shareText(
                    listOf(
                        calculateDaysDifference(
                            resources,
                            jdn,
                            secondJdn,
                            calendar = viewModel.calendar.value,
                        ),
                        formatDate(jdn on viewModel.calendar.value),
                        formatDate(secondJdn on viewModel.calendar.value),
                    ).joinToString("\n"),
                    chooserTitle,
                )
            }

            ConverterScreenMode.CALCULATOR -> {
                context.shareText(
                    runCatching {
                        // running this inside a runCatching block is absolutely important
                        eval(viewModel.calculatorInputText.value)
                    }.getOrElse { it.message }.orEmpty(),
                    chooserTitle,
                )
            }

            ConverterScreenMode.TIME_ZONES -> {
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

            ConverterScreenMode.QR_CODE -> qrShareAction()
        }
    }
}

@Composable
private fun Calculator(viewModel: ConverterViewModel) {
    val inputText = viewModel.calculatorInputText.collectAsState()
    val result = runCatching {
        // running this inside a runCatching block is absolutely important
        eval(inputText.value)
    }.getOrElse { it.message }.orEmpty()
    val defaultTextFieldColors = TextFieldDefaults.colors()
    val textFieldColors = defaultTextFieldColors.copy(
        focusedContainerColor = animateColor(defaultTextFieldColors.focusedContainerColor).value,
        unfocusedContainerColor = animateColor(defaultTextFieldColors.unfocusedContainerColor).value,
    )
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (isLandscape) Row(Modifier.padding(horizontal = 24.dp)) {
        TextField(
            value = inputText.value,
            onValueChange = viewModel::changeCalculatorInput,
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
            value = inputText.value,
            onValueChange = viewModel::changeCalculatorInput,
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ColumnScope.ConverterAndDistance(
    navigateToAstronomy: (Jdn) -> Unit,
    viewModel: ConverterViewModel,
    animatedContentScope: AnimatedContentScope,
    sharedTransitionScope: SharedTransitionScope,
    pendingConfirms: MutableCollection<() -> Unit>,
) {
    val screenMode by viewModel.screenMode.collectAsState()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val calendar by viewModel.calendar.collectAsState()
    val language by language.collectAsState()
    val calendarsList = enabledCalendars.takeIf { it.size > 1 } ?: language.defaultCalendars
    if (calendar !in calendarsList) viewModel.changeCalendar(mainCalendar)
    val jdn by viewModel.selectedDate.collectAsState()
    val today by viewModel.today.collectAsState()
    var isExpanded by rememberSaveable { mutableStateOf(true) }
    if (isLandscape) Row {
        Column(Modifier.weight(1f)) {
            CalendarsTypesPicker(calendar, calendarsList, viewModel::changeCalendar)
            DatePicker(
                calendar = calendar,
                jdn = jdn,
                pendingConfirms = pendingConfirms,
                setJdn = viewModel::changeSelectedDate,
            )
        }
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            this.AnimatedVisibility(
                visible = screenMode == ConverterScreenMode.CONVERTER,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable { isExpanded = !isExpanded }
                    .padding(top = 12.dp, bottom = 12.dp)
            ) {
                sharedTransitionScope.apply {
                    CalendarsOverview(
                        jdn = jdn,
                        today = today,
                        selectedCalendar = calendar,
                        shownCalendars = calendarsList - calendar,
                        isExpanded = isExpanded,
                        navigateToAstronomy = navigateToAstronomy,
                        animatedContentScope = animatedContentScope,
                    )
                }
            }
            this.AnimatedVisibility(visible = screenMode == ConverterScreenMode.DISTANCE) {
                DaysDistanceSecondPart(viewModel, jdn, calendar, pendingConfirms)
            }
        }
    } else {
        CalendarsTypesPicker(calendar, calendarsList, viewModel::changeCalendar)
        DatePicker(
            calendar = calendar,
            jdn = jdn,
            pendingConfirms = pendingConfirms,
            setJdn = viewModel::changeSelectedDate
        )
        this.AnimatedVisibility(visible = screenMode == ConverterScreenMode.CONVERTER) {
            val cardColors = CardDefaults.cardColors()
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                elevation = CardDefaults.cardElevation(8.dp),
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
                            jdn = jdn,
                            today = today,
                            selectedCalendar = calendar,
                            shownCalendars = calendarsList - calendar,
                            isExpanded = isExpanded,
                            navigateToAstronomy = navigateToAstronomy,
                            animatedContentScope = animatedContentScope,
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
        this.AnimatedVisibility(visible = screenMode == ConverterScreenMode.DISTANCE) {
            DaysDistanceSecondPart(viewModel, jdn, calendar, pendingConfirms)
        }
    }

    val secondJdn by viewModel.secondSelectedDate.collectAsState()
    val isAstronomicalExtraFeaturesEnabled by isAstronomicalExtraFeaturesEnabled.collectAsState()
    this.AnimatedVisibility(
        isAstronomicalExtraFeaturesEnabled && screenMode == ConverterScreenMode.DISTANCE &&
                !(secondJdn == jdn && jdn == today)
    ) {
        val isPersian = calendar == Calendar.SHAMSI
        val zodiacs = listOf(jdn, secondJdn).map {
            if (isPersian || Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                ChineseZodiac.fromPersianCalendar(it.toPersianDate())
            } else ChineseZodiac.fromChineseCalendar(ChineseCalendar(it.toGregorianCalendar().time))
        }
        val resources = LocalResources.current
        TextWithSlideAnimation(
            zodiacs.joinToString(spacedComma) { it.format(resources, true, isPersian) } +
                    spacedColon + language.formatCompatibility(zodiacs[0] compatibilityWith zodiacs[1])
        )
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
        val secondJdn by viewModel.secondSelectedDate.collectAsState()
        val resources = LocalResources.current
        TextWithSlideAnimation(
            calculateDaysDifference(resources, jdn, secondJdn, calendar)
        )
        DatePicker(
            calendar = calendar,
            jdn = secondJdn,
            pendingConfirms = pendingConfirms,
            setJdn = viewModel::changeSecondSelectedDate,
        )
    }
}

@Composable
private fun TextWithSlideAnimation(text: String) {
    AnimatedContent(
        text,
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
        label = "slide text",
    ) {
        SelectionContainer {
            Text(it, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
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
    val timeZone by (if (isFirst) viewModel.firstTimeZone else viewModel.secondTimeZone).collectAsState()
    val clock by viewModel.clock.collectAsState()
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
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
                pendingConfirms = pendingConfirms,
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
                pendingConfirms = pendingConfirms,
            )
        }
    }
}
