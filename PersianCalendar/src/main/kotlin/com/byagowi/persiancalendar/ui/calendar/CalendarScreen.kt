package com.byagowi.persiancalendar.ui.calendar

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.twotone.SwipeDown
import androidx.compose.material.icons.twotone.SwipeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
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
import com.byagowi.persiancalendar.LAST_CHOSEN_BUTTON_KEY
import com.byagowi.persiancalendar.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.PREF_BATTERY_OPTIMIZATION_IGNORED_COUNT
import com.byagowi.persiancalendar.PREF_DISMISSED_TIMES
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
import com.byagowi.persiancalendar.ui.theme.animateColor
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.ui.utils.appContentSizeAnimationSpec
import com.byagowi.persiancalendar.ui.utils.enabledCalendarsWithDefault
import com.byagowi.persiancalendar.ui.utils.isLandscape
import com.byagowi.persiancalendar.ui.utils.isLight
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeNoBottomEnd
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop
import com.byagowi.persiancalendar.ui.utils.openHtmlInBrowser
import com.byagowi.persiancalendar.utils.AddEventData
import com.byagowi.persiancalendar.utils.addEvent
import com.byagowi.persiancalendar.utils.calendar
import com.byagowi.persiancalendar.utils.createSearchRegex
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
import com.byagowi.persiancalendar.utils.viewEvent
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.days

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
    navigateToCalendarsPrioritySettings: () -> Unit,
    navigateToAstronomy: (Jdn) -> Unit,
    navigateToWeek: (Jdn) -> Unit,
    today: Jdn,
    now: Long,
    modifier: Modifier = Modifier,
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
    var addAction by remember { mutableStateOf({}) }
    val addEvent = addEvent(refreshCalendar, snackbarHostState)
    val viewEvent = viewEvent(refreshCalendar)
    val isLandscape = isLandscape()

    val density = LocalDensity.current

    var fabPlaceholderHeight by remember { mutableStateOf<Dp?>(null) }

    val swipeUpActions = remember {
        persistentMapOf(
            SwipeUpAction.Schedule to { navigateToSchedule(selectedDay) },
            SwipeUpAction.WeekView to { navigateToWeek(selectedDay) },
            SwipeUpAction.None to { bringDay(selectedDay - 7, true, false) },
        )
    }

    var searchTerm by rememberSaveable { mutableStateOf<String?>(null) }
    var yearViewCalendar by rememberSaveable { mutableStateOf<Calendar?>(null) }
    var isYearView by rememberSaveable { mutableStateOf(false) }
    val backButtonFraction = remember { mutableFloatStateOf(if (isYearView) 1f else 0f) }
    var isAddEventBoxEnabled by remember { mutableStateOf(false) }
    LaunchedEffect(isYearView, isAddEventBoxEnabled) {
        animate(
            initialValue = backButtonFraction.floatValue,
            targetValue = if (isYearView || isAddEventBoxEnabled) 1f else 0f,
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
            SwipeDownAction.MonthView to { navigateToMonthView(selectedDay) },
            SwipeDownAction.YearView to {
                searchTerm = null
                isYearView = true
            },
            SwipeDownAction.None to { bringDay(selectedDay + 7, true, false) },
        )
    }

    Scaffold(
        modifier = modifier.onKeyEvent { keyEvent ->
            if (!isYearView && keyEvent.type == KeyEventType.KeyDown) {
                when (keyEvent.key) {
                    Key.W -> {
                        navigateToWeek(selectedDay)
                        true
                    }

                    Key.M -> {
                        navigateToMonthView(selectedDay)
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
            val windowHeightPx = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                LocalActivity.current?.windowManager?.currentWindowMetrics?.bounds?.height()
            } else null

            val isCurrentDestination = run {
                val lifecycle by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
                lifecycle.isAtLeast(Lifecycle.State.RESUMED)
            }
            AnimatedVisibility(
                visible = !isYearView,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .onGloballyPositioned {
                        if (windowHeightPx != null) fabPlaceholderHeight = with(density) {
                            (windowHeightPx - it.positionInWindow().y).toDp()
                        } + 4.dp
                    }
                    .renderInSharedTransitionScopeOverlay(
                        renderInOverlay = { isCurrentDestination && isTransitionActive },
                    ),
                enter = scaleIn(),
                exit = scaleOut(),
            ) {
                AppFloatingActionButton(
                    onClick = {
                        if ((isTalkBackEnabled || !isShowDeviceCalendarEvents) && !isAddEventBoxEnabled) {
                            addEvent(AddEventData.fromJdn(selectedDay))
                        } else addAction()
                    },
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
        BoxWithConstraints(
            Modifier
                .padding(top = paddingValues.calculateTopPadding())
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Start)),
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
                if (isYearViewState && yearViewLazyListState != null && yearViewScale != null) YearView(
                    selectedDay = selectedDay,
                    selectedMonthOffset = selectedMonthOffset,
                    closeYearView = { isYearView = false },
                    lazyListState = yearViewLazyListState,
                    scale = yearViewScale,
                    maxWidth = maxWidth,
                    yearViewCalendar = yearViewCalendar,
                    onYearViewCalendarChange = { yearViewCalendar = it },
                    maxHeight = maxHeight,
                    bottomPadding = bottomPadding
                        // For screens without navigation bar, at least make sure it has some bottom padding
                        .coerceAtLeast(24.dp),
                    today = today,
                    modifier = Modifier.alpha(backButtonFraction.floatValue.coerceIn(0f, 1f)),
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

                if (!isYearViewState) {
                    @Composable
                    fun Details(detailsModifier: Modifier) = Details(
                        selectedDay = selectedDay,
                        onAddActionChange = { addAction = it },
                        bringDay = bringDay,
                        today = today,
                        now = now,
                        viewEvent = viewEvent,
                        navigateToCalendarsPrioritySettings = navigateToCalendarsPrioritySettings,
                        addEvent = addEvent,
                        snackbarHostState = snackbarHostState,
                        isAddEventBoxEnabled = isAddEventBoxEnabled,
                        onAddEventBoxEnabledChange = { isAddEventBoxEnabled = it },
                        fabPlaceholderHeight = fabPlaceholderHeight,
                        refreshToken = refreshToken,
                        navigateToHolidaysSettings = navigateToHolidaysSettings,
                        navigateToAstronomy = navigateToAstronomy,
                        navigateToSettingsLocationTab = navigateToSettingsLocationTab,
                        navigateToSettingsLocationTabSetAthanAlarm = navigateToSettingsLocationTabSetAthanAlarm,
                        swipeUpActions = swipeUpActions,
                        swipeDownActions = swipeDownActions,
                        modifier = detailsModifier,
                    )

                    @Composable
                    fun CalendarPager(pagerModifier: Modifier) = CalendarPager(
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
                        navigateToWeek = navigateToWeek,
                        modifier = pagerModifier.detectSwipe {
                            { isUp: Boolean ->
                                when {
                                    isUp -> swipeUpActions[preferredSwipeUpAction]
                                    !isUp -> swipeDownActions[preferredSwipeDownAction]
                                    else -> null
                                }?.invoke()
                            }
                        },
                    )

                    if (isLandscape) Row {
                        CalendarPager(pagerModifier = Modifier.size(pagerSize))
                        ScreenSurface(
                            shape = materialCornerExtraLargeNoBottomEnd(),
                            drawBehindSurface = false,
                        ) {
                            Details(
                                detailsModifier = Modifier
                                    .fillMaxHeight()
                                    .windowInsetsPadding(
                                        WindowInsets.safeDrawing.only(
                                            WindowInsetsSides.End,
                                        ),
                                    ),
                            )
                        }
                    } else Column(Modifier.clip(materialCornerExtraLargeTop())) {
                        var calendarHeight by remember { mutableStateOf(pagerSize.height / 7 * 6) }
                        CalendarPager(
                            pagerModifier = Modifier
                                .onSizeChanged {
                                    calendarHeight = with(density) { it.height.toDp() }
                                }
                                .animateContentSize(appContentSizeAnimationSpec),
                        )
                        val detailsMinHeight = maxHeight - calendarHeight
                        ScreenSurface(
                            workaroundClipBug = true,
                            mayNeedDragHandleToDivide = true,
                        ) { Details(detailsModifier = Modifier.defaultMinSize(minHeight = detailsMinHeight)) }
                    }
                }
            }
        }
    }

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

@Composable
private fun enableTimesTab(): Boolean {
    if (coordinates != null) return true
    // The placeholder isn't translated to other languages
    if (!language.isPersianOrDari) return false
    val context = LocalContext.current
    val preferences = remember { context.preferences }
    // The user is already dismissed the third tab
    return !preferences.getBoolean(PREF_DISMISSED_TIMES, false) &&
            // Try to not show the placeholder to established users
            PREF_APP_LANGUAGE !in preferences
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SharedTransitionScope.Details(
    selectedDay: Jdn,
    onAddActionChange: (() -> Unit) -> Unit,
    bringDay: BringDay,
    today: Jdn,
    now: Long,
    addEvent: (AddEventData) -> Unit,
    viewEvent: (CalendarEvent.DeviceCalendarEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    fabPlaceholderHeight: Dp?,
    isAddEventBoxEnabled: Boolean,
    onAddEventBoxEnabledChange: (Boolean) -> Unit,
    refreshToken: Int,
    navigateToCalendarsPrioritySettings: () -> Unit,
    navigateToHolidaysSettings: (item: String?) -> Unit,
    navigateToAstronomy: (Jdn) -> Unit,
    navigateToSettingsLocationTab: () -> Unit,
    navigateToSettingsLocationTabSetAthanAlarm: () -> Unit,
    swipeUpActions: ImmutableMap<SwipeUpAction, () -> Unit>,
    swipeDownActions: ImmutableMap<SwipeDownAction, () -> Unit>,
    modifier: Modifier = Modifier,
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    BoxWithConstraints(
        modifier.detectHorizontalSwipe(selectedDay) {
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
        val scale = remember {
            mutableFloatStateOf(
                (maxHeight.value / (25f * defaultCellHeight)).coerceIn(.5f, 1.5f),
            )
        }
        val cellHeight = (defaultCellHeight * scale.floatValue).dp
        val initialScroll =
            with(LocalDensity.current) { (cellHeight * initialHour - 16.dp).roundToPx() }
        val scrollState = rememberScrollState(initialScroll)
        LaunchedEffect(selectedDay) {
            onAddEventBoxEnabledChange(false)
            scrollState.animateScrollTo(initialScroll)
        }
        DaysView(
            bottomPadding = fabPlaceholderHeight ?: 0.dp,
            onAddActionChange = onAddActionChange,
            startingDay = selectedDay,
            selectedDay = selectedDay,
            onSelectedDayChange = {
                // Not needed for one day view
            },
            addEvent = addEvent,
            viewEvent = viewEvent,
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
            scrollableModifier = Modifier.detectSwipe {
                val wasAtTop = scrollState.value == 0
                val wasAtEnd = scrollState.value == scrollState.maxValue
                { isUp: Boolean ->
                    when {
                        isUp && wasAtEnd -> swipeUpActions[preferredSwipeUpAction]
                        !isUp && wasAtTop -> swipeDownActions[preferredSwipeDownAction]
                        else -> null
                    }?.invoke()
                }
            },
            numeral = numeral,
        ) { appointments, headerScrollState, onHasContentChange ->
            val shiftWorkTitle = shiftWorkSettings.workTitle(selectedDay)
            Column {
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
                                modifier = Modifier.fillMaxWidth(),
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
                AnimatedVisibility(visible = shiftWorkTitle != null) {
                    Spacer(Modifier.height(12.dp))
                }
            }

            var selectedButton by remember {
                val lastChosenIndex = context.preferences.getInt(LAST_CHOSEN_BUTTON_KEY, 0)
                mutableStateOf(Button.entries.getOrNull(lastChosenIndex))
            }

            val buttons = listOfNotNull(
                Pair(Button.Calendar) @Composable {
                    CalendarsTab(
                        modifier = Modifier.padding(top = 4.dp),
                        selectedDay = selectedDay,
                        today = today,
                        navigateToAstronomy = navigateToAstronomy,
                        navigateToCalendarsPrioritySettings = navigateToCalendarsPrioritySettings,
                    )
                }.takeIf { enabledCalendars.size > 1 },
                Pair(Button.Events) @Composable {
                    AnimatedContent(
                        targetState = appointments.isEmpty(),
                        transitionSpec = {
                            (fadeIn() + expandVertically()).togetherWith(fadeOut() + shrinkVertically())
                        },
                    ) { appointmentsIsEmpty ->
                        if (appointmentsIsEmpty) Box {
                            Text(
                                text = stringResource(R.string.no_event),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(vertical = 12.dp, horizontal = 24.dp)
                                    .fillMaxWidth(),
                            )
//                            TabEditButton(
//                                action = { navigateToHolidaysSettings(null) },
//                                title = stringResource(R.string.settings),
//                                visible = remember { PREF_HOLIDAY_TYPES !in context.preferences },
//                            )
                        } else DayEvents(
                            events = appointments,
                            navigateToHolidaysSettings = navigateToHolidaysSettings,
                            viewEvent = viewEvent,
                            modifier = Modifier.padding(
                                top = 6.dp,
                                bottom = 8.dp,
                                start = 24.dp,
                                end = 24.dp,
                            ),
                        )
                    }
//                        if (when {
//                                eventsRepository.iranOthers -> true
//                                eventsRepository.iranHolidays && appointments.isNotEmpty() -> true
//                                else -> false
//                            } && !isTalkBackEnabled
//                        ) {
//                            var showDialog by rememberSaveable { mutableStateOf(false) }
//                            if (showDialog) NoteOnAppointments(
//                                onDismissRequest = { showDialog = false },
//                            )
//                            Icon(
//                                imageVector = Icons.AutoMirrored.Default.Help,
//                                contentDescription = null,
//                                modifier = Modifier
//                                    .size(36.dp)
//                                    .padding(start = 8.dp)
//                                    .clickable { showDialog = true },
//                                tint = MaterialTheme.colorScheme.primary,
//                            )
//                        }
                }.takeIf { !eventsRepository.isEmpty && today.isYearSupportedOnApp },
                Pair(Button.Times) @Composable {
                    val coordinates = coordinates
                    if (coordinates != null) TimesTab(
                        modifier = Modifier.padding(top = 4.dp),
                        navigateToSettingsLocationTab = navigateToSettingsLocationTab,
                        navigateToSettingsLocationTabSetAthanAlarm = navigateToSettingsLocationTabSetAthanAlarm,
                        navigateToAstronomy = navigateToAstronomy,
                        coordinates = coordinates,
                        selectedDay = selectedDay,
                        now = now,
                        today = today,
                    )
//                    else EncourageActionLayout(
//                        modifier = Modifier.padding(vertical = 8.dp),
//                        header = stringResource(R.string.ask_user_to_set_location),
//                        discardAction = {
//                            context.preferences.edit { putBoolean(PREF_DISMISSED_TIMES, true) }
//                            removeThirdTab = true
//                        },
//                        acceptAction = navigateToSettingsLocationTab,
//                        hideOnAccept = false,
//                    )
                }.takeIf {
                    coordinates != null
//                    !removeThirdTab && enableTimesTab()
                },
            ).toMap().toPersistentMap()
            onHasContentChange(buttons.isNotEmpty() || !shiftWorkTitle.isNullOrEmpty())

            val swipeModifier = Modifier
                .detectHorizontalSwipe {
                    { isLeft ->
                        selectedButton = selectedButton?.let {
                            buttons.keys.toList().getOrNull(
                                (it.ordinal + if (isLeft xor isRtl) 1 else -1).mod(buttons.size),
                            )
                        }
                    }
                }
                .detectSwipe {
                    val wasAtTop = headerScrollState.value == 0
                    val wasAtEnd = headerScrollState.value == headerScrollState.maxValue
                    { isUp: Boolean ->
                        when {
                            isUp && wasAtEnd -> swipeUpActions[preferredSwipeUpAction]
                            !isUp && wasAtTop -> swipeDownActions[preferredSwipeDownAction]
                            else -> null
                        }?.invoke()
                    }
                }

            if (buttons.isNotEmpty()) CompositionLocalProvider(
                LocalMinimumInteractiveComponentSize provides 0.dp,
            ) {
                SingleChoiceSegmentedButtonRow(
                    modifier = swipeModifier.align(Alignment.CenterHorizontally),
                ) {
                    val defaultColors = SegmentedButtonDefaults.colors()
                    val colors = defaultColors.copy(
                        inactiveContainerColor = animateColor(MaterialTheme.colorScheme.surfaceContainer).value,
                        inactiveContentColor = animateColor(defaultColors.inactiveContentColor).value,
                        activeContainerColor = animateColor(defaultColors.activeContainerColor).value,
                        activeContentColor = animateColor(defaultColors.activeContentColor).value,
                    )
                    buttons.entries.forEachIndexed { index, (button, _) ->
                        SegmentedButton(
                            modifier = Modifier.defaultMinSize(minHeight = 38.dp),
                            onClick = {
                                selectedButton = if (selectedButton == button) null else button
                                context.preferences.edit {
                                    putInt(LAST_CHOSEN_BUTTON_KEY, selectedButton?.ordinal ?: -1)
                                }
                            },
                            contentPadding = PaddingValues.Zero,
                            colors = colors,
                            border = BorderStroke(
                                width = 1.dp,
                                color = animateColor(MaterialTheme.colorScheme.outlineVariant).value,
                            ),
                            selected = selectedButton == button,
                            icon = {},
                            shape = SegmentedButtonDefaults.itemShape(index, buttons.size),
                            label = {
                                Text(
                                    text = stringResource(button.title),
                                    modifier = Modifier.alpha(AppBlendAlpha),
                                )
                            },
                        )
                    }
                }
            }
            AnimatedContent(
                targetState = selectedButton,
                transitionSpec = {
                    (fadeIn() + expandVertically()).togetherWith(fadeOut() + shrinkVertically())
                },
                modifier = swipeModifier.fillMaxWidth(),
            ) {
                val content = buttons[it]
                if (content != null) content() else Spacer(Modifier.height(10.dp))
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
            if (false) {
                val context = LocalContext.current
                if (PREF_SHOW_DEVICE_CALENDAR_EVENTS !in context.preferences) {
                    var showDialog by remember { mutableStateOf(false) }
                    if (showDialog) AskForCalendarPermissionDialog { showDialog = false }

                    EncourageActionLayout(
                        header = stringResource(R.string.ask_calendar_permission),
                        discardAction = {
                            context.preferences.edit {
                                putBoolean(
                                    PREF_SHOW_DEVICE_CALENDAR_EVENTS,
                                    false,
                                )
                            }
                        },
                        acceptButton = stringResource(R.string.yes),
                        acceptAction = { showDialog = true },
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

private enum class Button(@get:StringRes val title: Int) {
    Calendar(R.string.calendar), Events(R.string.events), Times(R.string.times),
}

@Composable
private fun SharedTransitionScope.CalendarsTab(
    selectedDay: Jdn,
    today: Jdn,
    navigateToCalendarsPrioritySettings: () -> Unit,
    navigateToAstronomy: (Jdn) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier
            .clickable(
                onClickLabel = stringResource(R.string.more),
                onClick = { isExpanded = !isExpanded },
            )
            .padding(bottom = 12.dp),
    ) {
        CalendarsOverview(
            jdn = selectedDay,
            today = today,
            navigateToCalendarsPrioritySettings = navigateToCalendarsPrioritySettings,
            selectedCalendar = mainCalendar,
            shownCalendars = enabledCalendars,
            isExpanded = isExpanded,
            navigateToAstronomy = navigateToAstronomy,
        )

        if (false) {
            val context = LocalContext.current
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED && PREF_NOTIFY_IGNORED !in context.preferences && language.isUserAbleToReadPersian && today.isYearSupportedOnApp
            ) {
                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission(),
                ) { isGranted ->
                    context.preferences.edit { putBoolean(PREF_NOTIFY_DATE, isGranted) }
                }
                EncourageActionLayout(
                    header = stringResource(R.string.enable_notification),
                    acceptButton = stringResource(R.string.yes),
                    discardAction = {
                        context.preferences.edit { putBoolean(PREF_NOTIFY_IGNORED, true) }
                    },
                ) { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }
            }
        }
//        else if (showEncourageToExemptFromBatteryOptimizations()) {
//            fun ignore() {
//                val preferences = context.preferences
//                preferences.edit {
//                    val current = preferences.getInt(PREF_BATTERY_OPTIMIZATION_IGNORED_COUNT, 0)
//                    putInt(PREF_BATTERY_OPTIMIZATION_IGNORED_COUNT, current + 1)
//                }
//            }
//
//            fun requestExemption() {
//                runCatching {
//                    context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
//                }.onFailure(logException).onFailure { ignore() }.getOrNull().debugAssertNotNull
//            }
//
//            val launcher = rememberLauncherForActivityResult(
//                ActivityResultContracts.RequestPermission(),
//            ) { requestExemption() }
//
//            EncourageActionLayout(
//                header = stringResource(R.string.exempt_app_battery_optimization),
//                acceptButton = stringResource(R.string.yes),
//                discardAction = ::ignore,
//            ) {
//                val alarmManager = context.getSystemService<AlarmManager>()
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && runCatching { alarmManager?.canScheduleExactAlarms() }.getOrNull().debugAssertNotNull == false) launcher.launch(
//                    Manifest.permission.SCHEDULE_EXACT_ALARM,
//                ) else requestExemption()
//            }
//        }
    }
}

@Composable
private fun showEncourageToExemptFromBatteryOptimizations(): Boolean {
    val context = LocalContext.current
    val isAnyAthanSet = getEnabledAlarms(context).isNotEmpty()
    if (!isNotifyDate && !isAnyAthanSet && !hasAnyWidgetUpdateRecently()) return false
    if (context.preferences.getInt(PREF_BATTERY_OPTIMIZATION_IGNORED_COUNT, 0) >= 1) return false
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
                TodayActionButton(selectedMonthOffset != 0 || isHighlighted || isAddEventBoxEnabled) {
                    if (isAddEventBoxEnabled) onAddEventBoxEnabledChange(false) else {
                        onYearViewCalendarChange(null)
                        bringDay(today, false, false)
                    }
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
                    if (isTalkBackEnabled) return@icon
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoxScope.TabEditButton(
    action: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
) {
    Row(
        modifier = modifier
            .padding(end = 12.dp)
            .align(Alignment.TopEnd)
            .height(48.dp),
    ) {
        AnimatedVisibility(visible = visible) {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    TooltipAnchorPosition.Above,
                ),
                tooltip = { PlainTooltip { Text(title) } },
                state = rememberTooltipState(),
            ) {
                IconButton(onClick = action, modifier = Modifier.alpha(.5f)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = title,
                    )
                }
            }
        }
    }
}
