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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.twotone.SwipeDown
import androidx.compose.material.icons.twotone.SwipeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.lerp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
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
import com.byagowi.persiancalendar.LAST_CHOSEN_TAB_KEY
import com.byagowi.persiancalendar.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.PREF_BATTERY_OPTIMIZATION_IGNORED_COUNT
import com.byagowi.persiancalendar.PREF_DISMISSED_OWGHAT
import com.byagowi.persiancalendar.PREF_LAST_APP_VISIT_VERSION
import com.byagowi.persiancalendar.PREF_NOTIFY_DATE
import com.byagowi.persiancalendar.PREF_NOTIFY_IGNORED
import com.byagowi.persiancalendar.PREF_OTHER_CALENDARS_KEY
import com.byagowi.persiancalendar.PREF_SECONDARY_CALENDAR_IN_TABLE
import com.byagowi.persiancalendar.PREF_SHOW_WEEK_OF_YEAR_NUMBER
import com.byagowi.persiancalendar.PREF_SWIPE_DOWN_ACTION
import com.byagowi.persiancalendar.PREF_SWIPE_UP_ACTION
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.enabledCalendars
import com.byagowi.persiancalendar.global.eventsRepository
import com.byagowi.persiancalendar.global.isAstronomicalExtraFeaturesEnabled
import com.byagowi.persiancalendar.global.isNotifyDate
import com.byagowi.persiancalendar.global.isShowWeekOfYearEnabled
import com.byagowi.persiancalendar.global.isTalkBackEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.global.preferredSwipeDownAction
import com.byagowi.persiancalendar.global.preferredSwipeUpAction
import com.byagowi.persiancalendar.global.secondaryCalendar
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
import com.byagowi.persiancalendar.ui.common.DrawerArrowDrawable
import com.byagowi.persiancalendar.ui.common.NavigationNavigateUpIcon
import com.byagowi.persiancalendar.ui.common.NavigationOpenNavigationRailIcon
import com.byagowi.persiancalendar.ui.common.ScreenSurface
import com.byagowi.persiancalendar.ui.common.ScrollShadow
import com.byagowi.persiancalendar.ui.common.ThreeDotsDropdownMenu
import com.byagowi.persiancalendar.ui.common.TodayActionButton
import com.byagowi.persiancalendar.ui.theme.animateColor
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.appContentSizeAnimationSpec
import com.byagowi.persiancalendar.ui.utils.bringMarketPage
import com.byagowi.persiancalendar.ui.utils.enabledCalendarsWithDefaultInCompose
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
import com.byagowi.persiancalendar.utils.searchDeviceCalendarEvents
import com.byagowi.persiancalendar.utils.showUnsupportedActionToast
import com.byagowi.persiancalendar.utils.supportedYearOfIranCalendar
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
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
    var fabPlaceholderHeight by remember { mutableStateOf<Dp?>(null) }

    val detailsTabs = detailsTabs(
        refreshToken = refreshToken,
        refreshCalendar = refreshCalendar,
        selectedDay = selectedDay,
        navigateToHolidaysSettings = navigateToHolidaysSettings,
        navigateToSettingsLocationTab = navigateToSettingsLocationTab,
        navigateToSettingsLocationTabSetAthanAlarm = navigateToSettingsLocationTabSetAthanAlarm,
        navigateToAstronomy = navigateToAstronomy,
        today = today,
        now = now,
        fabPlaceholderHeight = fabPlaceholderHeight,
    )
    val detailsPagerState = rememberPagerState(
        initialPage = remember {
            CalendarScreenTab.entries.getOrNull(context.preferences.getInt(LAST_CHOSEN_TAB_KEY, 0))
                ?: CalendarScreenTab.entries[0]
        }.ordinal.coerceAtMost(detailsTabs.size - 1),
        pageCount = detailsTabs::size,
    )
    LaunchedEffect(detailsPagerState.currentPage) {
        context.preferences.edit { putInt(LAST_CHOSEN_TAB_KEY, detailsPagerState.currentPage) }
    }
    val isOnlyEventsTab = detailsTabs.size == 1

    val swipeUpActions = remember {
        persistentMapOf(
            SwipeUpAction.Schedule to { navigateToSchedule(selectedDay) },
            SwipeUpAction.DayView to { navigateToDays(selectedDay, false) },
            SwipeUpAction.WeekView to { navigateToDays(selectedDay, true) },
            SwipeUpAction.None to {
                if (isOnlyEventsTab) bringDay(selectedDay - 7, true, false)
            },
        )
    }

    var searchTerm by rememberSaveable { mutableStateOf<String?>(null) }
    var yearViewCalendar by rememberSaveable { mutableStateOf<Calendar?>(null) }
    var isYearView by rememberSaveable { mutableStateOf(false) }
    val isYearViewFraction = remember { Animatable(if (isYearView) 1f else 0f) }
    LaunchedEffect(isYearView) {
        isYearViewFraction.animateTo(
            targetValue = if (isYearView) 1f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        )
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
            SwipeDownAction.None to {
                if (isOnlyEventsTab) bringDay(selectedDay + 7, true, false)
            },
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
                        isYearViewFraction = isYearViewFraction,
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
            // Window height fallback for older device isn't consistent, let's just
            // use some hardcoded value in detailsTabs() instead
            val windowHeightPx = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                LocalActivity.current?.windowManager?.currentWindowMetrics?.bounds?.height()
            } else null

            val isCurrentDestination = run {
                val lifecycle by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
                lifecycle.isAtLeast(Lifecycle.State.RESUMED)
            }
            AnimatedVisibility(
                visible = (detailsPagerState.currentPage == CalendarScreenTab.EVENT.ordinal || isOnlyEventsTab) && !isYearView,
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
                    Box(Modifier.alpha(isYearViewFraction.value.coerceIn(0f, 1f))) {
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
                                tabs = detailsTabs,
                                pagerState = detailsPagerState,
                                contentMinHeight = maxHeight,
                                scrollableTabs = true,
                                bottomPadding = bottomPaddingWithMinimum,
                                isOnlyEventsTab = isOnlyEventsTab,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .windowInsetsPadding(
                                        WindowInsets.displayCutout.only(
                                            WindowInsetsSides.End,
                                        ),
                                    ),
                            )
                        }
                    } else {
                        val scrollState = rememberScrollState()
                        Box {
                            Column(
                                modifier = Modifier
                                    .clip(materialCornerExtraLargeTop())
                                    .verticalScroll(scrollState)
                                    .detectSwipe {
                                        val wasAtTop = scrollState.value == 0
                                        val wasAtEnd = scrollState.value == scrollState.maxValue
                                        { isUp: Boolean ->
                                            when {
                                                isUp && wasAtEnd -> {
                                                    swipeUpActions[preferredSwipeUpAction]
                                                }

                                                !isUp && wasAtTop -> {
                                                    swipeDownActions[preferredSwipeDownAction]
                                                }

                                                else -> null
                                            }?.invoke()
                                        }
                                    },
                            ) {
                                var calendarHeight by remember {
                                    mutableStateOf(pagerSize.height / 7 * 6)
                                }
                                Box(
                                    Modifier
                                        .offset { IntOffset(0, scrollState.value * 3 / 4) }
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
                                        tabs = detailsTabs,
                                        pagerState = detailsPagerState,
                                        contentMinHeight = detailsMinHeight,
                                        bottomPadding = bottomPaddingWithMinimum,
                                        isOnlyEventsTab = isOnlyEventsTab,
                                        modifier = Modifier.defaultMinSize(minHeight = detailsMinHeight),
                                    )
                                }
                            }
                            ScrollShadow(scrollState, skipTop = true)
                        }
                    }
                }
            }
        }
    }

    val eventsRepository = eventsRepository
    val resources = LocalResources.current
    LaunchedEffect(today, eventsRepository) {
        if (mainCalendar == Calendar.SHAMSI && eventsRepository.iranHolidays && today.toPersianDate().year > supportedYearOfIranCalendar) {
            if (snackbarHostState.showSnackbar(
                    resources.getString(R.string.outdated_app),
                    duration = SnackbarDuration.Long,
                    actionLabel = resources.getString(R.string.update),
                    withDismissAction = true,
                ) == SnackbarResult.ActionPerformed
            ) context.bringMarketPage()
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
    val preferences = LocalContext.current.preferences
    // The user is already dismissed the third tab
    return !preferences.getBoolean(PREF_DISMISSED_OWGHAT, false) &&
            // Try to not show the placeholder to established users
            PREF_APP_LANGUAGE !in preferences
}

private typealias DetailsTab = Pair<CalendarScreenTab, @Composable (MutableInteractionSource, minHeight: Dp, bottomPadding: Dp) -> Unit>

@Composable
private fun SharedTransitionScope.detailsTabs(
    selectedDay: Jdn,
    refreshToken: Int,
    refreshCalendar: () -> Unit,
    navigateToHolidaysSettings: (item: String?) -> Unit,
    navigateToSettingsLocationTab: () -> Unit,
    navigateToSettingsLocationTabSetAthanAlarm: () -> Unit,
    navigateToAstronomy: (Jdn) -> Unit,
    today: Jdn,
    now: Long,
    fabPlaceholderHeight: Dp?,
): ImmutableList<DetailsTab> {
    var removeThirdTab by rememberSaveable { mutableStateOf(false) }
    val hasTimesTab = enableTimesTab() && !removeThirdTab
    val isOnlyEventsTab =
        !hasTimesTab && enabledCalendars.size == 1 && !isAstronomicalExtraFeaturesEnabled
    return remember(hasTimesTab, isOnlyEventsTab) {
        listOfNotNull<DetailsTab>(
            if (!isOnlyEventsTab) CalendarScreenTab.CALENDAR to { interactionSource, minHeight, bottomPadding ->
                CalendarsTab(
                    selectedDay = selectedDay,
                    interactionSource = interactionSource,
                    minHeight = minHeight,
                    bottomPadding = bottomPadding,
                    today = today,
                    navigateToAstronomy = navigateToAstronomy,
                )
            } else null,
            CalendarScreenTab.EVENT to { _, _, bottomPadding ->
                EventsTab(
                    navigateToHolidaysSettings = navigateToHolidaysSettings,
                    // See the comment in floatingActionButton
                    fabPlaceholderHeight = fabPlaceholderHeight ?: (bottomPadding + 76.dp),
                    today = today,
                    now = now,
                    refreshToken = refreshToken,
                    refreshCalendar = refreshCalendar,
                    selectedDay = selectedDay,
                )
            },
            // The optional third tab
            if (hasTimesTab) CalendarScreenTab.TIMES to { interactionSource, minHeight, bottomPadding ->
                val coordinates = coordinates
                if (coordinates == null) Column(Modifier.fillMaxWidth()) {
                    val context = LocalContext.current
                    EncourageActionLayout(
                        modifier = Modifier.padding(top = 24.dp),
                        header = stringResource(R.string.ask_user_to_set_location),
                        discardAction = {
                            context.preferences.edit { putBoolean(PREF_DISMISSED_OWGHAT, true) }
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
                    interactionSource = interactionSource,
                    minHeight = minHeight,
                    bottomPadding = bottomPadding,
                    now = now,
                    today = today,
                )
            } else null,
        ).toImmutableList()
    }
}

@Composable
private fun Details(
    selectedDay: Jdn,
    bringDay: BringDay,
    tabs: ImmutableList<DetailsTab>,
    pagerState: PagerState,
    contentMinHeight: Dp,
    bottomPadding: Dp,
    isOnlyEventsTab: Boolean,
    modifier: Modifier = Modifier,
    scrollableTabs: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(modifier.indication(interactionSource = interactionSource, indication = ripple())) {
        val coroutineScope = rememberCoroutineScope()
        if (!isOnlyEventsTab) PrimaryTabRow(
            selectedTabIndex = pagerState.currentPage,
            divider = {},
            containerColor = Color.Transparent,
            indicator = {
                val offset = pagerState.currentPage.coerceAtMost(tabs.size - 1)
                val tabIndicatorColor by animateColor(MaterialTheme.colorScheme.primary)
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(selectedTabIndex = offset),
                    color = tabIndicatorColor,
                )
            },
        ) {
            tabs.forEachIndexed { index, (tab, _) ->
                Tab(
                    text = { Text(stringResource(tab.titleId)) },
                    modifier = Modifier.clip(MaterialTheme.shapes.large),
                    selected = pagerState.currentPage == index,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurface,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                )
            }
        }

        HorizontalPager(state = pagerState, verticalAlignment = Alignment.Top) { index ->
            /** See [androidx.compose.material3.SmallTabHeight] for 48.dp */
            val tabMinHeight = contentMinHeight - (if (isOnlyEventsTab) 0 else 48).dp
            Box(
                if (isOnlyEventsTab) run {
                    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
                    Modifier
                        .detectHorizontalSwipe(selectedDay) {
                            { isLeft ->
                                val newJdn = selectedDay + if (isLeft xor isRtl) -1 else 1
                                bringDay(newJdn, true, false)
                            }
                        }
                        .then(modifier)
                } else Modifier,
            ) {
                // Currently scrollable tabs only happen on landscape layout
                val scrollState = if (scrollableTabs) rememberScrollState() else null
                Box(if (scrollState != null) Modifier.verticalScroll(scrollState) else Modifier) {
                    tabs[index].second(interactionSource, tabMinHeight, bottomPadding)
                }
                if (scrollState != null) ScrollShadow(scrollState)
            }
        }
    }
}

@Composable
private fun SharedTransitionScope.CalendarsTab(
    selectedDay: Jdn,
    interactionSource: MutableInteractionSource,
    minHeight: Dp,
    bottomPadding: Dp,
    today: Jdn,
    navigateToAstronomy: (Jdn) -> Unit,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    Column(
        Modifier
            .defaultMinSize(minHeight = minHeight)
            .clickable(
                indication = null,
                interactionSource = interactionSource,
                onClickLabel = stringResource(R.string.more),
                onClick = { isExpanded = !isExpanded },
            ),
    ) {
        Spacer(Modifier.height(24.dp))
        CalendarsOverview(
            jdn = selectedDay,
            today = today,
            selectedCalendar = mainCalendar,
            shownCalendars = remember(enabledCalendars) { enabledCalendars.toImmutableList() },
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
        Spacer(Modifier.height(bottomPadding))
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
    val coroutineScope = rememberCoroutineScope()
    var alpha by rememberSaveable { mutableFloatStateOf(1f) }
    PredictiveBackHandler { flow ->
        runCatching {
            flow.collect { coroutineScope.launch { alpha = 1 - it.progress } }
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
    isYearViewFraction: Animatable<Float, AnimationVector1D>,
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

    val onYearViewBackPressed = {
        onIsYearViewChange(false)
        onYearViewCalendarChange(null)
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
                    values = enabledCalendarsWithDefaultInCompose(),
                    small = subtitle.isNotEmpty(),
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
                                MaterialTheme.typography.titleMedium,
                                MaterialTheme.typography.titleLarge,
                                fraction,
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
            if (isYearView) PredictiveBackHandler { flow ->
                runCatching {
                    flow.collect { isYearViewFraction.snapTo(1 - it.progress) }
                }.onSuccess { onYearViewBackPressed() }.onFailure {
                    isYearViewFraction.animateTo(1f)
                }
            }
            when (isYearViewFraction.value) {
                0f -> NavigationOpenNavigationRailIcon(openNavigationRail)
                1f -> NavigationNavigateUpIcon(navigateUp = onYearViewBackPressed)
                else -> DrawerArrowDrawable { isYearViewFraction.value }
            }
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
                item,
                action,
                PREF_SWIPE_UP_ACTION,
                item.titleId,
                preferredSwipeUpAction,
                Icons.TwoTone.SwipeUp,
            ) { (if (preferredSwipeUpAction == item) SwipeUpAction.None else item).name }
        }

        swipeDownActions.forEach { (item, action) ->
            if (item != SwipeDownAction.None) ActionItem(
                item,
                action,
                PREF_SWIPE_DOWN_ACTION,
                item.titleId,
                preferredSwipeDownAction,
                Icons.TwoTone.SwipeDown,
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
