package com.byagowi.persiancalendar.ui.calendar

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.PowerManager
import android.provider.CalendarContract
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.twotone.SwipeDown
import androidx.compose.material.icons.twotone.SwipeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.lerp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.PREF_BATTERY_OPTIMIZATION_IGNORED_COUNT
import com.byagowi.persiancalendar.PREF_DISMISSED_OWGHAT
import com.byagowi.persiancalendar.PREF_LAST_APP_VISIT_VERSION
import com.byagowi.persiancalendar.PREF_NOTIFY_DATE
import com.byagowi.persiancalendar.PREF_NOTIFY_IGNORED
import com.byagowi.persiancalendar.PREF_OTHER_CALENDARS_KEY
import com.byagowi.persiancalendar.PREF_OUTDATED_SHOWN
import com.byagowi.persiancalendar.PREF_SECONDARY_CALENDAR_IN_TABLE
import com.byagowi.persiancalendar.PREF_SHOW_DEVICE_CALENDAR_EVENTS
import com.byagowi.persiancalendar.PREF_SHOW_WEEK_OF_YEAR_NUMBER
import com.byagowi.persiancalendar.PREF_SWIPE_DOWN_ACTION
import com.byagowi.persiancalendar.PREF_SWIPE_UP_ACTION
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.enabledCalendars
import com.byagowi.persiancalendar.global.eventsRepository
import com.byagowi.persiancalendar.global.isAstronomicalExtraFeaturesEnabled
import com.byagowi.persiancalendar.global.isNotifyDate
import com.byagowi.persiancalendar.global.isShowDeviceCalendarEvents
import com.byagowi.persiancalendar.global.isShowWeekOfYearEnabled
import com.byagowi.persiancalendar.global.isTalkBackEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.global.preferredSwipeDownAction
import com.byagowi.persiancalendar.global.preferredSwipeUpAction
import com.byagowi.persiancalendar.global.secondaryCalendar
import com.byagowi.persiancalendar.global.shiftWorkSettings
import com.byagowi.persiancalendar.ui.astronomy.PlanetaryHoursDialog
import com.byagowi.persiancalendar.ui.calendar.calendarpager.CalendarPager
import com.byagowi.persiancalendar.ui.calendar.calendarpager.applyOffset
import com.byagowi.persiancalendar.ui.calendar.calendarpager.calendarPagerSize
import com.byagowi.persiancalendar.ui.calendar.calendarpager.calendarPagerState
import com.byagowi.persiancalendar.ui.calendar.calendarpager.clampPageNumber
import com.byagowi.persiancalendar.ui.calendar.reports.prayTimeHtmlReport
import com.byagowi.persiancalendar.ui.calendar.shiftwork.ShiftWorkDialog
import com.byagowi.persiancalendar.ui.calendar.times.TimesTab
import com.byagowi.persiancalendar.ui.calendar.yearview.YearView
import com.byagowi.persiancalendar.ui.calendar.yearview.YearViewCommand
import com.byagowi.persiancalendar.ui.calendar.yearview.yearViewIsInYearSelection
import com.byagowi.persiancalendar.ui.calendar.yearview.yearViewLazyListState
import com.byagowi.persiancalendar.ui.calendar.yearview.yearViewOffset
import com.byagowi.persiancalendar.ui.common.AppDropdownMenuCheckableItem
import com.byagowi.persiancalendar.ui.common.AppDropdownMenuExpandableItem
import com.byagowi.persiancalendar.ui.common.AppDropdownMenuItem
import com.byagowi.persiancalendar.ui.common.AppDropdownMenuRadioItem
import com.byagowi.persiancalendar.ui.common.AppFloatingActionButton
import com.byagowi.persiancalendar.ui.common.AppIconButton
import com.byagowi.persiancalendar.ui.common.AppModesDropDown
import com.byagowi.persiancalendar.ui.common.AskForCalendarPermissionDialog
import com.byagowi.persiancalendar.ui.common.CalendarsOverview
import com.byagowi.persiancalendar.ui.common.DatePickerDialog
import com.byagowi.persiancalendar.ui.common.NavigationMenuArrow
import com.byagowi.persiancalendar.ui.common.ScreenSurface
import com.byagowi.persiancalendar.ui.common.ScrollShadow
import com.byagowi.persiancalendar.ui.common.ThreeDotsDropdownMenu
import com.byagowi.persiancalendar.ui.common.TodayActionButton
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.appContentSizeAnimationSpec
import com.byagowi.persiancalendar.ui.utils.bringMarketPage
import com.byagowi.persiancalendar.ui.utils.enabledCalendarsWithDefault
import com.byagowi.persiancalendar.ui.utils.isLight
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeNoBottomEnd
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop
import com.byagowi.persiancalendar.ui.utils.openHtmlInBrowser
import com.byagowi.persiancalendar.utils.calendar
import com.byagowi.persiancalendar.utils.createSearchRegex
import com.byagowi.persiancalendar.utils.dayTitleSummary
import com.byagowi.persiancalendar.utils.debugAssertNotNull
import com.byagowi.persiancalendar.utils.getEnabledAlarms
import com.byagowi.persiancalendar.utils.hasAnyWidgetUpdateRecently
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.monthFormatForSecondaryCalendar
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.utils.otherCalendarFormat
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.readDayDeviceEvents
import com.byagowi.persiancalendar.utils.searchDeviceCalendarEvents
import com.byagowi.persiancalendar.utils.showUnsupportedActionToast
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.launch
import java.util.Date
import java.util.GregorianCalendar
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

@Composable
fun SharedTransitionScope.CalendarScreen(
    refreshToken: Int,
    refreshCalendar: () -> Unit,
    bringDayCommand: Jdn?,
    clearBringDayCommand: () -> Unit,
    openNavigationRail: () -> Unit,
    navigateToSchedule: (Jdn) -> Unit,
    navigateToMonthView: (Jdn) -> Unit,
    navigateToHolidaysSettings: (item: String?) -> Unit,
    navigateToSettingsLocationTab: () -> Unit,
    navigateToSettingsLocationTabSetAthanAlarm: () -> Unit,
    navigateToAstronomy: (Jdn) -> Unit,
    navigateToDays: (Jdn, isWeek: Boolean) -> Unit,
    today: Jdn,
    now: Long,
) {
    var selectedDay by rememberSaveable { mutableStateOf(today) }
    var isHighlighted by rememberSaveable { mutableStateOf(false) }
    fun Jdn.pagerMonthsDistance() = -mainCalendar.getMonthsDistance(baseJdn = today, toJdn = this)
    val calendarPagerState = calendarPagerState(bringDayCommand?.pagerMonthsDistance() ?: 0)
    val selectedMonthOffset = -applyOffset(calendarPagerState.currentPage)
    val coroutineScope = rememberCoroutineScope()
    val bringDay: BringDay = { jdn: Jdn, highlight: Boolean, immediate: Boolean ->
        isHighlighted = highlight
        selectedDay = jdn
        val page = clampPageNumber(applyOffset(jdn.pagerMonthsDistance()))
        if (calendarPagerState.currentPage != page) coroutineScope.launch {
            if (immediate) calendarPagerState.scrollToPage(page)
            else calendarPagerState.animateScrollToPage(page)
        }
    }
    bringDayCommand?.let { bringDay(it, it != today, true); clearBringDayCommand() }
    if (!isHighlighted && selectedDay != today) selectedDay = today

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val addEvent = addEvent(refreshCalendar, snackbarHostState)
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    val density = LocalDensity.current

    val swipeUpActions = remember {
        persistentMapOf(
            SwipeUpAction.Schedule to { navigateToSchedule(selectedDay) },
            SwipeUpAction.DayView to { navigateToDays(selectedDay, false) },
            SwipeUpAction.WeekView to { navigateToDays(selectedDay, true) },
            SwipeUpAction.None to { bringDay(selectedDay - 7, true, false) },
        )
    }

    var searchTerm by rememberSaveable { mutableStateOf<String?>(null) }
    var yearViewCalendar by rememberSaveable { mutableStateOf<Calendar?>(null) }
    var isYearView by rememberSaveable { mutableStateOf(false) }
    val backButtonFraction = remember { mutableFloatStateOf(if (isYearView) 1f else 0f) }
    LaunchedEffect(isYearView) {
        animate(
            initialValue = backButtonFraction.floatValue,
            targetValue = if (isYearView) 1f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        ) { value, _ -> backButtonFraction.floatValue = value }
    }
    var isAddEventBoxEnabled by remember { mutableStateOf(false) }
    LaunchedEffect(isAddEventBoxEnabled) {
        animate(
            initialValue = backButtonFraction.floatValue,
            targetValue = if (isAddEventBoxEnabled) 1f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        ) { value, _ -> backButtonFraction.floatValue = value }
    }
    val yearViewLazyListState = if (isYearView) yearViewLazyListState(
        today,
        selectedMonthOffset,
        yearViewCalendar,
    ) else null
    val yearViewScale = if (isYearView) rememberSaveable { mutableFloatStateOf(1f) } else null

    val swipeDownActions = remember {
        persistentMapOf(
//            SwipeDownAction.MonthView to { navigateToMonthView() },
            SwipeDownAction.YearView to {
                searchTerm = null
                isYearView = true
            },
            SwipeDownAction.None to { bringDay(selectedDay + 7, true, false) },
        )
    }

    Scaffold(
        modifier = Modifier.onKeyEvent { keyEvent ->
            if (!isYearView && keyEvent.type == KeyEventType.KeyDown) {
                when (keyEvent.key) {
                    Key.D -> {
                        navigateToDays(selectedDay, false)
                        true
                    }

                    Key.W -> {
                        navigateToDays(selectedDay, true)
                        true
                    }

                    Key.Y -> {
                        isYearView = true
                        true
                    }

                    Key.A -> {
                        navigateToSchedule(selectedDay)
                        true
                    }

                    else -> false
                }
            } else false
        },
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            var toolbarHeight by remember { mutableStateOf(0.dp) }
            val isSearchExpanded = !searchTerm.isNullOrEmpty()
            Crossfade(targetState = searchTerm != null) { isInSearch ->
                Box(
                    modifier = (if (isInSearch) {
                        if (isSearchExpanded || toolbarHeight <= 0.dp) Modifier
                        else Modifier.requiredHeight(toolbarHeight)
                    } else if (isYearView) {
                        if (toolbarHeight > 0.dp) {
                            Modifier.requiredHeight(toolbarHeight)
                        } else Modifier
                    } else Modifier.onSizeChanged {
                        toolbarHeight = with(density) { it.height.toDp() }
                    }).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isInSearch) Search(
                        searchTerm = searchTerm,
                        onSearchTermChange = { searchTerm = it },
                        closeSearch = { searchTerm = null },
                        isSearchExpanded = isSearchExpanded,
                        today = today,
                    ) { event ->
                        val date = event.date
                        val calendar = date.calendar
                        val jdn = Jdn(
                            calendar = calendar,
                            year = date.year.takeIf { it != -1 } ?: run {
                                val selectedMonth = calendar.getMonthStartFromMonthsDistance(
                                    baseJdn = today,
                                    monthsDistance = selectedMonthOffset,
                                )
                                selectedMonth.year + if (date.month < selectedMonth.month) 1 else 0
                            },
                            month = date.month,
                            day = date.dayOfMonth,
                        )
                        bringDay(jdn, true, true)
                    } else Toolbar(
                        openNavigationRail = openNavigationRail,
                        swipeUpActions = swipeUpActions,
                        swipeDownActions = swipeDownActions,
                        openSearch = { searchTerm = "" },
                        yearViewLazyListState = yearViewLazyListState,
                        yearViewScale = yearViewScale,
                        isYearView = isYearView,
                        onIsYearViewChange = { isYearView = it },
                        backButtonFraction = backButtonFraction,
                        isAddEventBoxEnabled = isAddEventBoxEnabled,
                        onAddEventBoxEnabledChange = { isAddEventBoxEnabled = it },
                        yearViewCalendar = yearViewCalendar,
                        onYearViewCalendarChange = { yearViewCalendar = it },
                        isLandscape = isLandscape,
                        today = today,
                        now = now,
                        hasToolbarHeight = toolbarHeight > 0.dp,
                        selectedMonthOffset = selectedMonthOffset,
                        isHighlighted = isHighlighted,
                        selectedDay = selectedDay,
                        bringDay = bringDay,
                    )
                }
            }
        },
        floatingActionButton = {
            val isCurrentDestination = run {
                val lifecycle by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
                lifecycle.isAtLeast(Lifecycle.State.RESUMED)
            }
            AnimatedVisibility(
                visible = !isYearView,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .renderInSharedTransitionScopeOverlay(
                        renderInOverlay = { isCurrentDestination && isTransitionActive },
                    ),
                enter = scaleIn(),
                exit = scaleOut(),
            ) {
                AppFloatingActionButton(
                    onClick = { addEvent(AddEventData.fromJdn(selectedDay)) },
                ) { Icon(Icons.Default.Add, stringResource(R.string.add_event)) }
            }
        },
    ) { paddingValues ->
        // Refresh the calendar on resume
        LaunchedEffect(Unit) {
            refreshCalendar()
            context.preferences.edit {
                putInt(PREF_LAST_APP_VISIT_VERSION, BuildConfig.VERSION_CODE)
            }
        }
        val bottomPadding = paddingValues.calculateBottomPadding()
        val bottomPaddingWithMinimum = bottomPadding
            // For screens without navigation bar, at least make sure it has some bottom padding
            .coerceAtLeast(24.dp)
        BoxWithConstraints(
            Modifier
                .padding(top = paddingValues.calculateTopPadding())
                .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Start)),
        ) {
            val maxWidth = this.maxWidth
            val maxHeight = this.maxHeight
            val pagerSize = calendarPagerSize(isLandscape, maxWidth, maxHeight, bottomPadding)

            AnimatedContent(
                targetState = isYearView,
                transitionSpec = {
                    val direction =
                        if (targetState) AnimatedContentTransitionScope.SlideDirection.Down
                        else AnimatedContentTransitionScope.SlideDirection.Up
                    slideIntoContainer(
                        towards = direction,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    ).togetherWith(slideOutOfContainer(direction))
                },
            ) { isYearViewState ->
                if (isYearViewState && yearViewLazyListState != null && yearViewScale != null) {
                    Box(Modifier.alpha(backButtonFraction.floatValue.coerceIn(0f, 1f))) {
                        YearView(
                            selectedDay = selectedDay,
                            selectedMonthOffset = selectedMonthOffset,
                            closeYearView = { isYearView = false },
                            lazyListState = yearViewLazyListState,
                            scale = yearViewScale,
                            maxWidth = maxWidth,
                            yearViewCalendar = yearViewCalendar,
                            onYearViewCalendarChange = { yearViewCalendar = it },
                            maxHeight = maxHeight,
                            bottomPadding = bottomPaddingWithMinimum,
                            today = today,
                        ) { calendar, monthsDistance ->
                            if (mainCalendar == calendar) coroutineScope.launch {
                                calendarPagerState.scrollToPage(
                                    clampPageNumber(applyOffset(-monthsDistance)),
                                )
                            } else {
                                val date = calendar.getMonthStartFromMonthsDistance(
                                    baseJdn = today,
                                    monthsDistance = monthsDistance,
                                )
                                bringDay(Jdn(date), true, true)
                            }
                        }
                    }
                }

                if (!isYearViewState) {
                    val scale = remember { mutableFloatStateOf(1f) }
                    val cellHeight by remember(scale.floatValue) {
                        mutableStateOf((64 * scale.floatValue).dp)
                    }
                    val initialScroll =
                        with(LocalDensity.current) { (cellHeight * 7 * scale.floatValue - 16.dp).roundToPx() }
                    val scrollState = rememberScrollState(initialScroll)
                    if (isLandscape) Row {
                        Box(Modifier.size(pagerSize)) {
                            CalendarPager(
                                selectedDay = selectedDay,
                                isHighlighted = isHighlighted,
                                refreshToken = refreshToken,
                                changeSelectedDay = { day: Jdn ->
                                    isHighlighted = true
                                    selectedDay = day
                                },
                                today = today,
                                calendarPagerState = calendarPagerState,
                                yearViewCalendar = yearViewCalendar,
                                addEvent = addEvent,
                                suggestedPagerSize = pagerSize,
                                navigateToDays = navigateToDays,
                            )
                        }
                        ScreenSurface(
                            materialCornerExtraLargeNoBottomEnd(),
                            drawBehindSurface = false,
                        ) {
                            Details(
                                selectedDay = selectedDay,
                                bringDay = bringDay,
                                bottomPadding = bottomPaddingWithMinimum,
                                today = today,
                                now = now,
                                addEvent = addEvent,
                                snackbarHostState = snackbarHostState,
                                isAddEventBoxEnabled = isAddEventBoxEnabled,
                                onAddEventBoxEnabledChange = { isAddEventBoxEnabled = true },
                                initialScroll = initialScroll,
                                scale = scale,
                                cellHeight = cellHeight,
                                refreshCalendar = refreshCalendar,
                                refreshToken = refreshToken,
                                navigateToHolidaysSettings = navigateToHolidaysSettings,
                                navigateToAstronomy = navigateToAstronomy,
                                navigateToSettingsLocationTab = navigateToSettingsLocationTab,
                                navigateToSettingsLocationTabSetAthanAlarm = navigateToSettingsLocationTabSetAthanAlarm,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .windowInsetsPadding(
                                        WindowInsets.displayCutout.only(
                                            WindowInsetsSides.End,
                                        ),
                                    ),
                                scrollState = scrollState,
                            )
                        }
                    } else Column(Modifier.clip(materialCornerExtraLargeTop())) {
                        var calendarHeight by remember {
                            mutableStateOf(pagerSize.height / 7 * 6)
                        }
                        Box(
                            Modifier
                                .detectSwipe {
                                    { isUp: Boolean ->
                                        when {
                                            isUp -> swipeUpActions[preferredSwipeUpAction]
                                            !isUp -> swipeDownActions[preferredSwipeDownAction]
                                            else -> null
                                        }?.invoke()
                                    }
                                }
                                .onSizeChanged {
                                    calendarHeight = with(density) { it.height.toDp() }
                                }
                                .animateContentSize(appContentSizeAnimationSpec),
                        ) {
                            CalendarPager(
                                selectedDay = selectedDay,
                                isHighlighted = isHighlighted,
                                refreshToken = refreshToken,
                                changeSelectedDay = { day: Jdn ->
                                    isHighlighted = true
                                    selectedDay = day
                                },
                                today = today,
                                calendarPagerState = calendarPagerState,
                                yearViewCalendar = yearViewCalendar,
                                addEvent = addEvent,
                                suggestedPagerSize = pagerSize,
                                navigateToDays = navigateToDays,
                            )
                        }

                        val detailsMinHeight = maxHeight - calendarHeight
                        ScreenSurface(
                            workaroundClipBug = true,
                            mayNeedDragHandleToDivide = true,
                        ) {
                            Details(
                                bringDay = bringDay,
                                selectedDay = selectedDay,
                                bottomPadding = bottomPaddingWithMinimum,
                                today = today,
                                addEvent = addEvent,
                                snackbarHostState = snackbarHostState,
                                isAddEventBoxEnabled = isAddEventBoxEnabled,
                                onAddEventBoxEnabledChange = { isAddEventBoxEnabled = true },
                                now = now,
                                scrollState = scrollState,
                                initialScroll = initialScroll,
                                scale = scale,
                                cellHeight = cellHeight,
                                refreshCalendar = refreshCalendar,
                                refreshToken = refreshToken,
                                navigateToAstronomy = navigateToAstronomy,
                                navigateToHolidaysSettings = navigateToHolidaysSettings,
                                navigateToSettingsLocationTab = navigateToSettingsLocationTab,
                                navigateToSettingsLocationTabSetAthanAlarm = navigateToSettingsLocationTabSetAthanAlarm,
                                modifier = Modifier.defaultMinSize(minHeight = detailsMinHeight),
                            )
                        }
                    }
                }
            }
        }
    }

    val eventsRepository = eventsRepository
    LaunchedEffect(today, eventsRepository) {
        if (mainCalendar == Calendar.SHAMSI && eventsRepository.iranOthers && !today.isYearSupportedOnApp && language.isIranExclusive) {
            val preferences = context.preferences
            if (PREF_OUTDATED_SHOWN !in preferences) {
                snackbarHostState.showSnackbar(
                    message = "تقویم بروز نیست و مناسبتی در این سال نمایش نمی‌دهد، اگر بروزرسانی وجود ندارد احتمالاً باید برنامه‌ای دیگر استفاده کنید",
                    duration = SnackbarDuration.Long,
                    withDismissAction = true,
                )
                preferences.edit { putBoolean(PREF_OUTDATED_SHOWN, true) }
            }
        }
    }
}

private typealias BringDay = (day: Jdn, highlight: Boolean, immediate: Boolean) -> Unit

private enum class CalendarScreenTab(@get:StringRes val titleId: Int) {
    CALENDAR(R.string.calendar), EVENT(R.string.events), TIMES(R.string.times),
}

@Composable
private fun enableTimesTab(): Boolean {
    if (coordinates != null) return true
    // The placeholder isn't translated to other languages
    if (!language.isPersianOrDari) return false
    val context = LocalContext.current
    val preferences = remember { context.preferences }
    // The user is already dismissed the third tab
    return !preferences.getBoolean(PREF_DISMISSED_OWGHAT, false) &&
            // Try to not show the placeholder to established users
            PREF_APP_LANGUAGE !in preferences
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Details(
    selectedDay: Jdn,
    bringDay: BringDay,
    bottomPadding: Dp,
    today: Jdn,
    now: Long,
    addEvent: (AddEventData) -> Unit,
    snackbarHostState: SnackbarHostState,
    initialScroll: Int,
    cellHeight: Dp,
    scale: MutableFloatState,
    isAddEventBoxEnabled: Boolean,
    onAddEventBoxEnabledChange: () -> Unit,
    refreshCalendar: () -> Unit,
    refreshToken: Int,
    navigateToHolidaysSettings: (item: String?) -> Unit,
    navigateToAstronomy: (Jdn) -> Unit,
    navigateToSettingsLocationTab: () -> Unit,
    navigateToSettingsLocationTabSetAthanAlarm: () -> Unit,
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(modifier.indication(interactionSource = interactionSource, indication = ripple())) {
        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        BoxWithConstraints(
            Modifier.detectHorizontalSwipe(selectedDay) {
                { isLeft ->
                    val newJdn = selectedDay + if (isLeft xor isRtl) -1 else 1
                    bringDay(newJdn, true, false)
                }
            },
        ) {
            val detailsWidth = this.maxWidth

            val context = LocalContext.current
            val dayDeviceEvents = remember(
                refreshToken, isShowDeviceCalendarEvents, selectedDay,
            ) {
                if (isShowDeviceCalendarEvents) {
                    context.readDayDeviceEvents(selectedDay)
                } else EventsStore.empty()
            }
            DaysView(
                bottomPadding = bottomPadding,
                setAddAction = {
                    // Better to keep add button to act as before here
                },
                startingDay = selectedDay,
                selectedDay = selectedDay,
                setSelectedDay = {
                    // Not needed for one day view
                },
                addEvent = addEvent,
                refreshCalendar = refreshCalendar,
                days = 1,
                now = now,
                isAddEventBoxEnabled = isAddEventBoxEnabled,
                onAddEventBoxEnabledChange = onAddEventBoxEnabledChange,
                snackbarHostState = snackbarHostState,
                navigateToHolidaysSettings = navigateToHolidaysSettings,
                hasWeekPager = true,
                deviceEvents = dayDeviceEvents,
                screenWidth = detailsWidth,
                scrollState = scrollState,
                scale = scale,
                initialScroll = initialScroll,
                cellHeight = cellHeight,
                scrollableModifier = Modifier,
                numeral = numeral,
            ) {
                val shiftWorkTitle = shiftWorkSettings.workTitle(selectedDay)
                AnimatedVisibility(visible = shiftWorkTitle != null) {
                    AnimatedContent(
                        targetState = shiftWorkTitle.orEmpty(),
                        transitionSpec = appCrossfadeSpec,
                    ) { state ->
                        SelectionContainer {
                            Text(
                                state,
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                            )
                        }
                    }
                }
                val shiftWorkInDaysDistance =
                    shiftWorkSettings.getShiftWorksInDaysDistance(today, selectedDay)
                AnimatedVisibility(visible = shiftWorkInDaysDistance != null) {
                    AnimatedContent(
                        targetState = shiftWorkInDaysDistance.orEmpty(),
                        transitionSpec = appCrossfadeSpec,
                    ) { state ->
                        SelectionContainer {
                            Text(
                                state,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                var removeThirdTab by rememberSaveable { mutableStateOf(false) }
                val hasTimesTab = enableTimesTab() && !removeThirdTab
                val buttons = listOfNotNull(
                    Pair(R.string.calendar) @Composable {
                        CalendarsTab(
                            selectedDay = selectedDay,
                            today = today,
                            navigateToAstronomy = navigateToAstronomy,
                        )
                    }.takeIf { enabledCalendars.size != 1 },
                    Pair(R.string.times) @Composable {
                        val coordinates = coordinates
                        if (coordinates == null) Column(Modifier.fillMaxWidth()) {
                            val context = LocalContext.current
                            EncourageActionLayout(
                                modifier = Modifier.padding(top = 24.dp),
                                header = stringResource(R.string.ask_user_to_set_location),
                                discardAction = {
                                    context.preferences.edit {
                                        putBoolean(PREF_DISMISSED_OWGHAT, true)
                                    }
                                    removeThirdTab = true
                                },
                                acceptAction = navigateToSettingsLocationTab,
                                hideOnAccept = false,
                            )
                            Spacer(Modifier.height(bottomPadding))
                        } else TimesTab(
                            navigateToSettingsLocationTab = navigateToSettingsLocationTab,
                            navigateToSettingsLocationTabSetAthanAlarm = navigateToSettingsLocationTabSetAthanAlarm,
                            navigateToAstronomy = navigateToAstronomy,
                            coordinates = coordinates,
                            selectedDay = selectedDay,
                            now = now,
                            today = today,
                        )
                    }.takeIf { hasTimesTab },
                )

                val coroutineScope = rememberCoroutineScope()
                var openedTab by remember { mutableIntStateOf(-1) }
                val tooltipStates = buttons.map { rememberTooltipState(isPersistent = true) }
                Box(contentAlignment = Alignment.Center) {
                    buttons.zip(tooltipStates) { (_, content), tooltipState ->
                        TooltipBox(
                            onDismissRequest = {
                                openedTab = -1
                                coroutineScope.launch { tooltipState.dismiss() }
                            },
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                positioning = TooltipAnchorPosition.Above,
                            ),
                            tooltip = {
                                RichTooltip(
                                    maxWidth = detailsWidth - 48.dp,
                                    tonalElevation = 12.dp,
                                    caretShape = TooltipDefaults.caretShape(),
                                ) { content() }
                            },
                            enableUserInput = false,
                            state = tooltipState,
                            modifier = Modifier.padding(horizontal = 4.dp),
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(1.dp),
                            )
                        }
                    }
                }
                Row(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    buttons.forEachIndexed { index, (stringId, _) ->
                        val tooltipState = tooltipStates[index]
                        DisposableEffect(Unit) {
                            onDispose { if (openedTab == index) openedTab = -1 }
                        }
                        AnimatedVisibility(
                            visible = openedTab == -1 || openedTab == index,
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    coroutineScope.launch {
                                        if (tooltipState.isVisible) {
                                            tooltipState.dismiss()
                                            openedTab = -1
                                        } else {
                                            openedTab = index
                                            tooltipState.show()
                                        }
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 4.dp),
                            ) {
                                Row(
                                    Modifier.alpha(.6f),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) { Text(stringResource(stringId)) }
                            }
                        }
                    }
                }

//        if (PREF_HOLIDAY_TYPES !in context.preferences && language.isIranExclusive) {
//            Spacer(Modifier.height(16.dp))
//            EncourageActionLayout(
//                header = stringResource(R.string.warn_if_events_not_set),
//                discardAction = {
//                    context.preferences.edit {
//                        putStringSet(PREF_HOLIDAY_TYPES, EventsRepository.iranDefault)
//                    }
//                },
//                acceptAction = { navigateToHolidaysSettings(null) },
//            )
//        } else
                val context = LocalContext.current
                if (PREF_SHOW_DEVICE_CALENDAR_EVENTS !in context.preferences) {
                    var showDialog by remember { mutableStateOf(false) }
                    if (showDialog) AskForCalendarPermissionDialog { showDialog = false }

                    Spacer(Modifier.height(16.dp))
                    EncourageActionLayout(
                        header = stringResource(R.string.ask_calendar_permission),
                        discardAction = {
                            context.preferences.edit { putBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, false) }
                        },
                        acceptButton = stringResource(R.string.yes),
                        acceptAction = { showDialog = true },
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarsTab(
    selectedDay: Jdn,
    today: Jdn,
    navigateToAstronomy: (Jdn) -> Unit,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    Column(
        Modifier.clickable(
            onClickLabel = stringResource(R.string.more),
            onClick = { isExpanded = !isExpanded },
        ),
    ) {
        Spacer(Modifier.height(24.dp))
        CalendarsOverview(
            jdn = selectedDay,
            today = today,
            selectedCalendar = mainCalendar,
            shownCalendars = enabledCalendars,
            isExpanded = isExpanded,
            navigateToAstronomy = navigateToAstronomy,
        )

        val context = LocalContext.current
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED && PREF_NOTIFY_IGNORED !in context.preferences
        ) {
            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission(),
            ) { isGranted -> context.preferences.edit { putBoolean(PREF_NOTIFY_DATE, isGranted) } }
            EncourageActionLayout(
                header = stringResource(R.string.enable_notification),
                acceptButton = stringResource(R.string.yes),
                discardAction = {
                    context.preferences.edit { putBoolean(PREF_NOTIFY_IGNORED, true) }
                },
            ) { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }
        } else if (showEncourageToExemptFromBatteryOptimizations()) {
            fun ignore() {
                val preferences = context.preferences
                preferences.edit {
                    val current = preferences.getInt(PREF_BATTERY_OPTIMIZATION_IGNORED_COUNT, 0)
                    putInt(PREF_BATTERY_OPTIMIZATION_IGNORED_COUNT, current + 1)
                }
            }

            fun requestExemption() {
                runCatching {
                    context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                }.onFailure(logException).onFailure { ignore() }.getOrNull().debugAssertNotNull
            }

            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission(),
            ) { requestExemption() }

            EncourageActionLayout(
                header = stringResource(R.string.exempt_app_battery_optimization),
                acceptButton = stringResource(R.string.yes),
                discardAction = ::ignore,
            ) {
                val alarmManager = context.getSystemService<AlarmManager>()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && runCatching { alarmManager?.canScheduleExactAlarms() }.getOrNull().debugAssertNotNull == false) launcher.launch(
                    Manifest.permission.SCHEDULE_EXACT_ALARM,
                ) else requestExemption()
            }
        }
    }
}

@Composable
private fun showEncourageToExemptFromBatteryOptimizations(): Boolean {
    val context = LocalContext.current
    val isAnyAthanSet = getEnabledAlarms(context).isNotEmpty()
    if (!isNotifyDate && !isAnyAthanSet && !hasAnyWidgetUpdateRecently()) return false
    if (context.preferences.getInt(PREF_BATTERY_OPTIMIZATION_IGNORED_COUNT, 0) >= 2) return false
    val alarmManager = context.getSystemService<AlarmManager>()
    if (isAnyAthanSet && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && runCatching { alarmManager?.canScheduleExactAlarms() }.getOrNull().debugAssertNotNull == false) return true
    return !isIgnoringBatteryOptimizations(context)
}

private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
    return runCatching {
        context.getSystemService<PowerManager>()?.isIgnoringBatteryOptimizations(
            context.applicationContext.packageName,
        )
    }.onFailure(logException).getOrNull() == true
}

@Composable
private fun Search(
    searchTerm: String?,
    onSearchTermChange: (String) -> Unit,
    isSearchExpanded: Boolean,
    closeSearch: () -> Unit,
    today: Jdn,
    bringEvent: (CalendarEvent<*>) -> Unit,
) {
    var alpha by rememberSaveable { mutableFloatStateOf(1f) }
    PredictiveBackHandler { flow ->
        runCatching {
            flow.collect { alpha = 1 - it.progress }
        }.onSuccess { closeSearch() }.onFailure { alpha = 1f }
    }
    val repository = eventsRepository
    val enabledEvents = remember(key1 = today) { repository.getEnabledEvents(today) }
    val context = LocalContext.current
    val items = remember(searchTerm) {
        val searchTerm = searchTerm
        if (searchTerm.isNullOrBlank()) return@remember emptyList()
        val regex = createSearchRegex(searchTerm)
        context.searchDeviceCalendarEvents(searchTerm) + enabledEvents.asSequence().filter {
            regex.containsMatchIn(it.title)
        }.take(50).toList()
    }
    val padding by animateDpAsState(targetValue = if (isSearchExpanded) 0.dp else 32.dp)
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    @OptIn(ExperimentalMaterial3Api::class) SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                modifier = if (isSearchExpanded) Modifier.padding(
                    paddingValues = WindowInsets.safeDrawing.only(
                        sides = WindowInsetsSides.Horizontal,
                    ).asPaddingValues(),
                ) else Modifier,
                query = searchTerm ?: "",
                onQueryChange = onSearchTermChange,
                onSearch = {},
                expanded = isSearchExpanded,
                onExpandedChange = {},
                placeholder = { Text(stringResource(R.string.search_in_events)) },
                trailingIcon = {
                    AppIconButton(
                        icon = Icons.Default.Close,
                        title = stringResource(R.string.close),
                    ) { closeSearch() }
                },
            )
        },
        expanded = isSearchExpanded,
        onExpandedChange = { if (!it) onSearchTermChange("") },
        modifier = Modifier
            .alpha(alpha)
            .padding(horizontal = padding)
            .focusRequester(focusRequester),
    ) {
        if (padding.value != 0f) return@SearchBar
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            Box {
                val lazyListState = rememberLazyListState()
                LazyColumn(
                    state = lazyListState,
                    contentPadding = WindowInsets.safeDrawing.only(
                        sides = WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
                    ).asPaddingValues(),
                ) {
                    items(items) {
                        Box(
                            Modifier
                                .clickable {
                                    closeSearch()
                                    bringEvent(it)
                                }
                                .fillMaxWidth()
                                .padding(vertical = 20.dp, horizontal = 24.dp),
                        ) {
                            AnimatedContent(
                                targetState = it.title,
                                transitionSpec = appCrossfadeSpec,
                            ) { title ->
                                Text(
                                    title,
                                    modifier = Modifier.align(Alignment.CenterStart),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
                ScrollShadow(lazyListState)
            }
        }
    }

    // Tweak status bar color when search is expanded, can break if system night mode is changed
    // mid-search but who cares.
    if (isSearchExpanded) LocalActivity.current?.window?.also { window ->
        val view = LocalView.current
        val colorScheme = MaterialTheme.colorScheme
        DisposableEffect(Unit) {
            val controller = WindowInsetsControllerCompat(window, view)
            controller.isAppearanceLightStatusBars = colorScheme.surface.isLight
            onDispose { controller.isAppearanceLightStatusBars = colorScheme.background.isLight }
        }
    }
}

@Composable
private fun SharedTransitionScope.Toolbar(
    openNavigationRail: () -> Unit,
    swipeUpActions: ImmutableMap<SwipeUpAction, () -> Unit>,
    swipeDownActions: ImmutableMap<SwipeDownAction, () -> Unit>,
    openSearch: () -> Unit,
    yearViewCalendar: Calendar?,
    onYearViewCalendarChange: (Calendar?) -> Unit,
    isYearView: Boolean,
    onIsYearViewChange: (Boolean) -> Unit,
    backButtonFraction: MutableFloatState,
    isAddEventBoxEnabled: Boolean,
    onAddEventBoxEnabledChange: (Boolean) -> Unit,
    yearViewLazyListState: LazyListState?,
    yearViewScale: MutableFloatState?,
    selectedMonthOffset: Int,
    selectedDay: Jdn,
    isHighlighted: Boolean,
    bringDay: BringDay,
    isLandscape: Boolean,
    today: Jdn,
    now: Long,
    hasToolbarHeight: Boolean,
) {
    val coroutineScope = rememberCoroutineScope()
    val selectedMonth = mainCalendar.getMonthStartFromMonthsDistance(
        baseJdn = today,
        monthsDistance = selectedMonthOffset,
    )
    val yearViewOffset = yearViewOffset(yearViewLazyListState)
    val yearViewIsInYearSelection = yearViewIsInYearSelection(yearViewScale)

    val onBackPressed = {
        when {
            isYearView -> {
                onIsYearViewChange(false)
                onYearViewCalendarChange(null)
            }
            isAddEventBoxEnabled -> onAddEventBoxEnabledChange(false)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class) TopAppBar(
        title = {
            val title: String
            val subtitle: String
            run {
                val yearViewCalendar = yearViewCalendar
                val secondaryCalendar =
                    yearViewCalendar.takeIf { it != mainCalendar } ?: secondaryCalendar
                if (isYearView && yearViewCalendar != null) {
                    title = stringResource(
                        if (yearViewIsInYearSelection) R.string.select_year else R.string.year_view,
                    )
                    subtitle = if (!isTalkBackEnabled && run {
                            yearViewOffset == 0 || yearViewIsInYearSelection
                        }) "" else {
                        val yearViewYear = (today on yearViewCalendar).year + yearViewOffset
                        val formattedYear = numeral.format(yearViewYear)
                        if (yearViewCalendar != mainCalendar) {
                            val mainCalendarTitle =
                                otherCalendarFormat(yearViewYear, yearViewCalendar, mainCalendar)
                            language.inParentheses.format(formattedYear, mainCalendarTitle)
                        } else if (secondaryCalendar == null) formattedYear else {
                            val secondaryTitle =
                                otherCalendarFormat(yearViewYear, mainCalendar, secondaryCalendar)
                            language.inParentheses.format(formattedYear, secondaryTitle)
                        }
                    }
                } else if (secondaryCalendar == null) {
                    title = selectedMonth.monthName
                    subtitle = numeral.format(selectedMonth.year)
                } else {
                    title = language.my.format(
                        selectedMonth.monthName, numeral.format(selectedMonth.year),
                    )
                    val selectedDate = selectedDay on mainCalendar
                    val isCurrentMonth =
                        selectedDate.year == selectedMonth.year && selectedDate.month == selectedMonth.month
                    if (isHighlighted && isCurrentMonth) {
                        val selectedSecondaryDate = selectedDay on secondaryCalendar
                        subtitle = language.my.format(
                            selectedSecondaryDate.monthName,
                            numeral.format(selectedSecondaryDate.year),
                        )
                    } else {
                        subtitle = monthFormatForSecondaryCalendar(selectedMonth, secondaryCalendar)
                    }
                }
            }
            Column(
                Modifier
                    .clickable(
                        indication = null,
                        interactionSource = null,
                        onClickLabel = stringResource(
                            if (isYearView && !yearViewIsInYearSelection) R.string.select_year
                            else R.string.year_view,
                        ),
                    ) {
                        if (isYearView) coroutineScope.launch {
                            YearViewCommand.ToggleYearSelection.execute(
                                yearViewLazyListState,
                                yearViewScale,
                            )
                        } else {
                            onYearViewCalendarChange(mainCalendar)
                            onIsYearViewChange(true)
                        }
                    }
                    .then(
                        // Toolbar height might not exist if screen rotated while being in year view
                        if (isYearView && hasToolbarHeight) Modifier.fillMaxSize() else Modifier,
                    ),
                verticalArrangement = Arrangement.Center,
            ) {
                if (isYearView) AppModesDropDown(
                    value = yearViewCalendar ?: mainCalendar,
                    onValueChange = onYearViewCalendarChange,
                    items = enabledCalendarsWithDefault,
                    small = subtitle.isNotEmpty(),
                    modifier = Modifier.alpha(backButtonFraction.floatValue.coerceIn(0f, 1f)),
                ) {
                    stringResource(
                        if (language.isArabicScript && LocalDensity.current.fontScale == 1f) {
                            it.title
                        } else it.shortTitle,
                    )
                } else Crossfade(targetState = title) { title ->
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                AnimatedVisibility(visible = subtitle.isNotEmpty()) {
                    Crossfade(targetState = subtitle) { subtitle ->
                        val fraction by animateFloatAsState(if (isYearView) 1f else 0f)
                        Text(
                            if (isTalkBackEnabled && isYearView) "$subtitle ${
                                stringResource(R.string.year_view)
                            }"
                            else subtitle,
                            style = lerp(
                                start = MaterialTheme.typography.titleMedium,
                                stop = MaterialTheme.typography.titleLarge,
                                fraction = fraction,
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = if (isYearView) Modifier else when (yearViewCalendar) {
                                null, mainCalendar, secondaryCalendar -> Modifier
                                else -> Modifier
                                    .clip(MaterialTheme.shapes.extraLarge)
                                    .background(LocalContentColor.current.copy(alpha = .175f))
                                    .clickable(
                                        onClickLabel = buildString {
                                            append(stringResource(R.string.cancel))
                                            append(" ")
                                            append(stringResource(R.string.year_view))
                                        },
                                    ) { onYearViewCalendarChange(null) }
                                    .padding(horizontal = 8.dp)
                            },
                        )
                    }
                }
            }
        },
        colors = appTopAppBarColors(),
        navigationIcon = {
            if (isYearView || isAddEventBoxEnabled) PredictiveBackHandler { flow ->
                runCatching {
                    flow.collect { backButtonFraction.floatValue = 1 - it.progress }
                }.onSuccess { onBackPressed() }.onFailure {
                    animate(
                        initialValue = backButtonFraction.floatValue,
                        targetValue = 1f,
                    ) { value, _ -> backButtonFraction.floatValue = value }
                }
            }
            NavigationMenuArrow(
                fraction = backButtonFraction.floatValue,
                action = if (backButtonFraction.floatValue == 0f) openNavigationRail else onBackPressed,
            )
        },
        actions = {
            AnimatedVisibility(visible = isYearView) {
                TodayActionButton(visible = yearViewOffset != 0 && !yearViewIsInYearSelection) {
                    onYearViewCalendarChange(mainCalendar)
                    coroutineScope.launch {
                        YearViewCommand.TodayMonth.execute(
                            yearViewLazyListState,
                            yearViewScale,
                        )
                    }
                }
            }
            AnimatedVisibility(visible = isYearView && !yearViewIsInYearSelection) {
                AppIconButton(
                    icon = Icons.Default.KeyboardArrowDown,
                    title = stringResource(R.string.next_x, stringResource(R.string.year)),
                ) {
                    coroutineScope.launch {
                        YearViewCommand.NextMonth.execute(
                            yearViewLazyListState,
                            yearViewScale,
                        )
                    }
                }
            }
            AnimatedVisibility(isYearView && !yearViewIsInYearSelection) {
                AppIconButton(
                    icon = Icons.Default.KeyboardArrowUp,
                    title = stringResource(R.string.previous_x, stringResource(R.string.year)),
                ) {
                    coroutineScope.launch {
                        YearViewCommand.PreviousMonth.execute(
                            yearViewLazyListState,
                            yearViewScale,
                        )
                    }
                }
            }

            AnimatedVisibility(!isYearView) {
                TodayActionButton(selectedMonthOffset != 0 || isHighlighted) {
                    onYearViewCalendarChange(null)
                    bringDay(today, false, false)
                }
            }
            AnimatedVisibility(!isYearView) {
                AppIconButton(
                    icon = Icons.Default.Search,
                    title = stringResource(R.string.search_in_events),
                    onClick = openSearch,
                )
            }
            AnimatedVisibility(!isYearView) {
                Menu(
                    selectedDay = selectedDay,
                    selectedMonthOffset = selectedMonthOffset,
                    bringDay = bringDay,
                    isLandscape = isLandscape,
                    swipeUpActions = swipeUpActions,
                    swipeDownActions = swipeDownActions,
                    isTalkBackEnabled = isTalkBackEnabled,
                    today = today,
                    now = now,
                )
            }
        },
    )
}

@Composable
private fun SharedTransitionScope.Menu(
    swipeUpActions: ImmutableMap<SwipeUpAction, () -> Unit>,
    swipeDownActions: ImmutableMap<SwipeDownAction, () -> Unit>,
    isLandscape: Boolean,
    bringDay: BringDay,
    selectedDay: Jdn,
    selectedMonthOffset: Int,
    isTalkBackEnabled: Boolean,
    today: Jdn,
    now: Long,
) {
    val context = LocalContext.current
    val resources = LocalResources.current

    var showDatePickerDialog by rememberSaveable { mutableStateOf(false) }
    if (showDatePickerDialog) DatePickerDialog(
        initialJdn = selectedDay,
        onDismissRequest = { showDatePickerDialog = false },
        today = today,
        onSuccess = { bringDay(it, true, false) },
    )

    var showShiftWorkDialog by rememberSaveable { mutableStateOf(false) }
    if (showShiftWorkDialog) ShiftWorkDialog(
        selectedJdn = selectedDay,
        onDismissRequest = { showShiftWorkDialog = false },
    )

    var showPlanetaryHoursDialog by rememberSaveable { mutableStateOf(false) }
    if (showPlanetaryHoursDialog) coordinates?.also {
        PlanetaryHoursDialog(
            coordinates = it,
            now = now + (selectedDay - today).days.inWholeMilliseconds,
            isToday = today == selectedDay,
        ) { showPlanetaryHoursDialog = false }
    }

    ThreeDotsDropdownMenu { closeMenu ->
        AppDropdownMenuItem({ Text(stringResource(R.string.select_date)) }) {
            closeMenu()
            showDatePickerDialog = true
        }

        AppDropdownMenuItem({ Text(stringResource(R.string.shift_work_settings)) }) {
            closeMenu()
            showShiftWorkDialog = true
        }

        if (coordinates != null) AppDropdownMenuItem(text = { Text(stringResource(R.string.month_pray_times)) }) {
            closeMenu()
            val selectedMonth = mainCalendar.getMonthStartFromMonthsDistance(
                baseJdn = today,
                monthsDistance = selectedMonthOffset,
            )
            context.openHtmlInBrowser(prayTimeHtmlReport(resources, selectedMonth))
        }
        if (coordinates != null && isAstronomicalExtraFeaturesEnabled) AppDropdownMenuItem(
            {
                Text(stringResource(R.string.planetary_hours))
            },
        ) {
            showPlanetaryHoursDialog = true
            closeMenu()
        }

        HorizontalDivider()

        @Composable
        fun <T> ActionItem(
            item: T,
            action: () -> Unit,
            prefKey: String,
            @StringRes title: Int,
            preferredAction: T,
            swipeIcon: ImageVector,
            valueToStoreOnClick: () -> String,
        ) {
            AppDropdownMenuItem(
                text = { Text(stringResource(title)) },
                trailingIcon = icon@{
                    if (isLandscape || isTalkBackEnabled) return@icon
                    Box(
                        Modifier.clickable(null, ripple(bounded = false)) {
                            context.preferences.edit { putString(prefKey, valueToStoreOnClick()) }
                        },
                    ) {
                        val alpha by animateFloatAsState(if (preferredAction == item) 1f else .2f)
                        val color = LocalContentColor.current.copy(alpha = alpha)
                        Icon(swipeIcon, null, tint = color)
                    }
                },
            ) { closeMenu(); action() }
        }

        swipeUpActions.forEach { (item, action) ->
            if (item != SwipeUpAction.None) ActionItem(
                item = item,
                action = action,
                prefKey = PREF_SWIPE_UP_ACTION,
                title = item.titleId,
                preferredAction = preferredSwipeUpAction,
                swipeIcon = Icons.TwoTone.SwipeUp,
            ) { (if (preferredSwipeUpAction == item) SwipeUpAction.None else item).name }
        }

        swipeDownActions.forEach { (item, action) ->
            if (item != SwipeDownAction.None) ActionItem(
                item = item,
                action = action,
                prefKey = PREF_SWIPE_DOWN_ACTION,
                title = item.titleId,
                preferredAction = preferredSwipeDownAction,
                swipeIcon = Icons.TwoTone.SwipeDown,
            ) { (if (preferredSwipeDownAction == item) SwipeDownAction.None else item).name }
        }

        HorizontalDivider()

        AppDropdownMenuCheckableItem(
            text = { Text(stringResource(R.string.week_number)) },
            isChecked = isShowWeekOfYearEnabled,
            onValueChange = {
                context.preferences.edit { putBoolean(PREF_SHOW_WEEK_OF_YEAR_NUMBER, it) }
                closeMenu()
            },
        )

        // It doesn't have any effect in talkback ui, let's disable it there to avoid the confusion
        if (isTalkBackEnabled || enabledCalendars.size == 1) return@ThreeDotsDropdownMenu

        var showSecondaryCalendarSubMenu by rememberSaveable { mutableStateOf(false) }
        AppDropdownMenuExpandableItem(
            text = { Text(stringResource(R.string.show_secondary_calendar)) },
            isExpanded = showSecondaryCalendarSubMenu,
            onClick = { showSecondaryCalendarSubMenu = !showSecondaryCalendarSubMenu },
        )

        (listOf(null) + enabledCalendars.drop(1)).forEach { calendar ->
            AnimatedVisibility(showSecondaryCalendarSubMenu) {
                AppDropdownMenuRadioItem(
                    text = { Text(stringResource(calendar?.title ?: R.string.none)) },
                    isSelected = calendar == secondaryCalendar,
                ) {
                    context.preferences.edit {
                        if (calendar == null) remove(PREF_SECONDARY_CALENDAR_IN_TABLE) else {
                            putBoolean(PREF_SECONDARY_CALENDAR_IN_TABLE, true)
                            val newOtherCalendars =
                                listOf(calendar) + (enabledCalendars.drop(1) - calendar)
                            putString(
                                PREF_OTHER_CALENDARS_KEY,
                                // Put the chosen calendars at the first of calendars priorities
                                newOtherCalendars.joinToString(","),
                            )
                        }
                    }
                    closeMenu()
                }
            }
        }
    }
}

data class AddEventData(
    val beginTime: Date,
    val endTime: Date,
    val allDay: Boolean,
    val description: String?,
) {
    fun asIntent(): Intent {
        return Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI).also {
            if (description != null) it.putExtra(
                CalendarContract.Events.DESCRIPTION, description,
            )
        }.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.time)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.time)
            .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, allDay)
    }

    companion object {
        fun fromJdn(jdn: Jdn): AddEventData {
            val time = jdn.toGregorianCalendar().time
            return AddEventData(
                beginTime = time,
                endTime = time,
                allDay = true,
                description = dayTitleSummary(jdn, jdn on mainCalendar),
            )
        }

        // Used in widget, turns 5:45 to 6:00-7:00 and 6:05 to 6:30-7:30
        fun upcoming(): AddEventData {
            val begin = GregorianCalendar()
            val wasAtFirstHalf = begin[GregorianCalendar.MINUTE] < 30
            begin[GregorianCalendar.MINUTE] = 0
            begin[GregorianCalendar.SECOND] = 0
            begin[GregorianCalendar.MILLISECOND] = 0
            begin.timeInMillis += (if (wasAtFirstHalf) .5 else 1.0).hours.inWholeMilliseconds
            val end = Date(begin.time.time)
            end.time += 1.hours.inWholeMilliseconds
            return AddEventData(
                beginTime = begin.time,
                endTime = end,
                allDay = false,
                description = null,
            )
        }
    }
}

private class AddEventContract : ActivityResultContract<AddEventData, Void?>() {
    override fun parseResult(resultCode: Int, intent: Intent?): Void? = null
    override fun createIntent(context: Context, input: AddEventData) = input.asIntent()
}

@Composable
fun addEvent(
    refreshCalendar: () -> Unit,
    snackbarHostState: SnackbarHostState,
): (AddEventData) -> Unit {
    val addEvent = rememberLauncherForActivityResult(AddEventContract()) {
        refreshCalendar()
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var addEventData by remember { mutableStateOf<AddEventData?>(null) }

    addEventData?.let { data ->
        AskForCalendarPermissionDialog { isGranted ->
            refreshCalendar()
            if (isGranted) runCatching { addEvent.launch(data) }.onFailure(logException).onFailure {
                if (language.isPersianOrDari) coroutineScope.launch {
                    if (snackbarHostState.showSnackbar(
                            "جهت افزودن رویداد نیاز است از نصب و فعال بودن تقویم گوگل اطمینان حاصل کنید",
                            duration = SnackbarDuration.Long,
                            actionLabel = "نصب",
                            withDismissAction = true,
                        ) == SnackbarResult.ActionPerformed
                    ) context.bringMarketPage("com.google.android.calendar")
                } else showUnsupportedActionToast(context)
            }
            addEventData = null
        }
    }

    return { addEventData = it }
}
