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
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.byagowi.persiancalendar.ui.calendar.calendarpager.calendarPagerSize
import com.byagowi.persiancalendar.ui.calendar.calendarpager.calendarPagerState
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
import com.byagowi.persiancalendar.ui.common.AppScreenModesDropDown
import com.byagowi.persiancalendar.ui.common.AskForCalendarPermissionDialog
import com.byagowi.persiancalendar.ui.common.CalendarsOverview
import com.byagowi.persiancalendar.ui.common.DatePickerDialog
import com.byagowi.persiancalendar.ui.common.NavigationNavigateUpIcon
import com.byagowi.persiancalendar.ui.common.NavigationOpenNavigationRailIcon
import com.byagowi.persiancalendar.ui.common.ScreenSurface
import com.byagowi.persiancalendar.ui.common.ScrollShadow
import com.byagowi.persiancalendar.ui.common.ThreeDotsDropdownMenu
import com.byagowi.persiancalendar.ui.common.TodayActionButton
import com.byagowi.persiancalendar.ui.theme.animateColor
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.AnimatableFloatSaver
import com.byagowi.persiancalendar.ui.utils.appContentSizeAnimationSpec
import com.byagowi.persiancalendar.ui.utils.bringMarketPage
import com.byagowi.persiancalendar.ui.utils.isLight
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeNoBottomEnd
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop
import com.byagowi.persiancalendar.ui.utils.openHtmlInBrowser
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
import kotlinx.coroutines.launch
import java.util.Date
import java.util.GregorianCalendar
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

@Composable
fun SharedTransitionScope.CalendarScreen(
    openNavigationRail: () -> Unit,
    navigateToSchedule: () -> Unit,
    navigateToMonthView: () -> Unit,
    navigateToHolidaysSettings: (item: String?) -> Unit,
    navigateToSettingsLocationTab: () -> Unit,
    navigateToSettingsLocationTabSetAthanAlarm: () -> Unit,
    navigateToAstronomy: (Jdn) -> Unit,
    navigateToDays: (Jdn, isWeek: Boolean) -> Unit,
    viewModel: CalendarViewModel,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val addEvent = addEvent(viewModel, snackbarHostState)
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(viewModel.daysScreenSelectedDay) {
        viewModel.daysScreenSelectedDay?.let { viewModel.bringDay(it, it != viewModel.today) }
    }

    val density = LocalDensity.current
    var fabPlaceholderHeight by remember { mutableStateOf<Dp?>(null) }

    val detailsTabs = detailsTabs(
        viewModel = viewModel,
        navigateToHolidaysSettings = navigateToHolidaysSettings,
        navigateToSettingsLocationTab = navigateToSettingsLocationTab,
        navigateToSettingsLocationTabSetAthanAlarm = navigateToSettingsLocationTabSetAthanAlarm,
        navigateToAstronomy = navigateToAstronomy,
        today = viewModel.today,
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

    val swipeUpActions = mapOf(
        SwipeUpAction.Schedule to { navigateToSchedule() },
        SwipeUpAction.DayView to { navigateToDays(viewModel.selectedDay, false) },
        SwipeUpAction.WeekView to { navigateToDays(viewModel.selectedDay, true) },
        SwipeUpAction.None to {
            if (isOnlyEventsTab) viewModel.bringDay(viewModel.selectedDay - 7)
        },
    )

    val searchTerm = rememberSaveable { mutableStateOf<String?>(null) }
    val yearViewCalendar = rememberSaveable { mutableStateOf<Calendar?>(null) }
    val isYearView = rememberSaveable { mutableStateOf(false) }
    val yearViewLazyListState = if (isYearView.value) yearViewLazyListState(
        viewModel.today,
        viewModel.selectedMonthOffset,
        yearViewCalendar.value,
    ) else null
    val yearViewScale = if (isYearView.value) rememberSaveable(saver = AnimatableFloatSaver) {
        Animatable(1f)
    } else null

    val swipeDownActions = mapOf(
//            SwipeDownAction.MonthView to { navigateToMonthView() },
        SwipeDownAction.YearView to {
            searchTerm.value = null
            isYearView.value = true
        },
        SwipeDownAction.None to {
            if (isOnlyEventsTab) viewModel.bringDay(viewModel.selectedDay + 7)
        },
    )

    Scaffold(
        modifier = Modifier.onKeyEvent { keyEvent ->
            if (!isYearView.value && keyEvent.type == KeyEventType.KeyDown) {
                when (keyEvent.key) {
                    Key.D -> {
                        navigateToDays(viewModel.selectedDay, false)
                        true
                    }

                    Key.W -> {
                        navigateToDays(viewModel.selectedDay, true)
                        true
                    }

                    Key.Y -> {
                        isYearView.value = true
                        true
                    }

                    Key.A -> {
                        navigateToSchedule()
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
            val isSearchExpanded = !searchTerm.value.isNullOrEmpty()
            Crossfade(targetState = searchTerm.value != null) { isInSearch ->
                Box(
                    modifier = (if (isInSearch) {
                        if (isSearchExpanded || toolbarHeight <= 0.dp) Modifier
                        else Modifier.requiredHeight(toolbarHeight)
                    } else if (isYearView.value) {
                        if (toolbarHeight > 0.dp) {
                            Modifier.requiredHeight(toolbarHeight)
                        } else Modifier
                    } else Modifier.onSizeChanged {
                        toolbarHeight = with(density) { it.height.toDp() }
                    }).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isInSearch) Search(searchTerm, isSearchExpanded, viewModel::bringEvent) {
                        searchTerm.value = null
                    } else Toolbar(
                        openNavigationRail = openNavigationRail,
                        swipeUpActions = swipeUpActions,
                        swipeDownActions = swipeDownActions,
                        openSearch = { searchTerm.value = "" },
                        yearViewLazyListState = yearViewLazyListState,
                        yearViewScale = yearViewScale,
                        isYearView = isYearView,
                        yearViewCalendar = yearViewCalendar,
                        viewModel = viewModel,
                        isLandscape = isLandscape,
                        today = viewModel.today,
                        hasToolbarHeight = toolbarHeight > 0.dp,
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
                visible = (detailsPagerState.currentPage == CalendarScreenTab.EVENT.ordinal || isOnlyEventsTab) && !isYearView.value,
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
                    onClick = { addEvent(AddEventData.fromJdn(viewModel.selectedDay)) },
                ) { Icon(Icons.Default.Add, stringResource(R.string.add_event)) }
            }
        },
    ) { paddingValues ->
        // Refresh the calendar on resume
        LaunchedEffect(Unit) {
            viewModel.refreshCalendar()
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

            Column(Modifier.fillMaxSize()) {
                AnimatedVisibility(visible = isYearView.value) {
                    val yearViewLazyListState = yearViewLazyListState
                    val yearViewScale = yearViewScale
                    if (yearViewLazyListState != null && yearViewScale != null) YearView(
                        viewModel = viewModel,
                        closeYearView = { isYearView.value = false },
                        lazyListState = yearViewLazyListState,
                        scale = yearViewScale,
                        maxWidth = maxWidth,
                        yearViewCalendar = yearViewCalendar,
                        maxHeight = maxHeight,
                        bottomPadding = bottomPaddingWithMinimum,
                    )
                }

                // To preserve pager's state even in year view where calendar isn't in the tree
                val pagerState = calendarPagerState()

                AnimatedVisibility(
                    visible = !isYearView.value,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Top, clip = false),
                ) {
                    if (isLandscape) Row {
                        Box(Modifier.size(pagerSize)) {
                            CalendarPager(
                                viewModel = viewModel,
                                today = viewModel.today,
                                pagerState = pagerState,
                                yearViewCalendar = yearViewCalendar.value,
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
                                viewModel = viewModel,
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
                                        viewModel = viewModel,
                                        today = viewModel.today,
                                        pagerState = pagerState,
                                        yearViewCalendar = yearViewCalendar.value,
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
                                        viewModel = viewModel,
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
    LaunchedEffect(viewModel.today, eventsRepository) {
        if (mainCalendar == Calendar.SHAMSI && eventsRepository.iranHolidays && viewModel.today.toPersianDate().year > supportedYearOfIranCalendar) {
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

enum class CalendarScreenTab(@get:StringRes val titleId: Int) {
    CALENDAR(R.string.calendar),
    EVENT(R.string.events),
    TIMES(R.string.times),
}

@Composable
private fun enableTimesTab(): Boolean {
    val preferences = LocalContext.current.preferences
    return coordinates != null || // if coordinates is set, should be shown
            (language.isPersianOrDari && // The placeholder isn't translated to other languages
                    // The user is already dismissed the third tab
                    !preferences.getBoolean(PREF_DISMISSED_OWGHAT, false) &&
                    // Try to not show the placeholder to established users
                    PREF_APP_LANGUAGE !in preferences)
}

private typealias DetailsTab = Pair<CalendarScreenTab, @Composable (MutableInteractionSource, minHeight: Dp, bottomPadding: Dp) -> Unit>

@Composable
private fun SharedTransitionScope.detailsTabs(
    viewModel: CalendarViewModel,
    navigateToHolidaysSettings: (item: String?) -> Unit,
    navigateToSettingsLocationTab: () -> Unit,
    navigateToSettingsLocationTabSetAthanAlarm: () -> Unit,
    navigateToAstronomy: (Jdn) -> Unit,
    today: Jdn,
    fabPlaceholderHeight: Dp?,
): List<DetailsTab> {
    var removeThirdTab by rememberSaveable { mutableStateOf(false) }
    val hasTimesTab = enableTimesTab() && !removeThirdTab
    val isOnlyEventsTab =
        !hasTimesTab && enabledCalendars.size == 1 && !isAstronomicalExtraFeaturesEnabled
    return listOfNotNull(
        if (!isOnlyEventsTab) CalendarScreenTab.CALENDAR to { interactionSource, minHeight, bottomPadding ->
            CalendarsTab(
                viewModel = viewModel,
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
                viewModel = viewModel,
                // See the comment in floatingActionButton
                fabPlaceholderHeight = fabPlaceholderHeight ?: (bottomPadding + 76.dp),
            )
        },
        // The optional third tab
        if (hasTimesTab) CalendarScreenTab.TIMES to { interactionSource, minHeight, bottomPadding ->
            TimesTab(
                navigateToSettingsLocationTab = navigateToSettingsLocationTab,
                navigateToSettingsLocationTabSetAthanAlarm = navigateToSettingsLocationTabSetAthanAlarm,
                navigateToAstronomy = navigateToAstronomy,
                removeThirdTab = { removeThirdTab = true },
                viewModel = viewModel,
                interactionSource = interactionSource,
                minHeight = minHeight,
                bottomPadding = bottomPadding,
            )
        } else null,
    )
}

@Composable
private fun Details(
    viewModel: CalendarViewModel,
    tabs: List<DetailsTab>,
    pagerState: PagerState,
    contentMinHeight: Dp,
    modifier: Modifier,
    bottomPadding: Dp,
    isOnlyEventsTab: Boolean,
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
                        .detectHorizontalSwipe {
                            { isLeft ->
                                val newJdn = viewModel.selectedDay + if (isLeft xor isRtl) -1 else 1
                                viewModel.bringDay(newJdn)
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
    viewModel: CalendarViewModel,
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
            jdn = viewModel.selectedDay,
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
    searchTerm: MutableState<String?>,
    isSearchExpanded: Boolean,
    bringEvent: (CalendarEvent<*>) -> Unit,
    closeSearch: () -> Unit,
) {
    BackHandler { closeSearch() }
    var searchTerm by searchTerm
    val repository = eventsRepository
    val enabledEvents = remember { repository.getEnabledEvents(Jdn.today()) }
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
                onQueryChange = { searchTerm = it },
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
        onExpandedChange = { if (!it) searchTerm = "" },
        modifier = Modifier
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
    // mid search but who cares.
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
    swipeUpActions: Map<SwipeUpAction, () -> Unit>,
    swipeDownActions: Map<SwipeDownAction, () -> Unit>,
    openSearch: () -> Unit,
    yearViewCalendar: MutableState<Calendar?>,
    isYearView: MutableState<Boolean>,
    yearViewLazyListState: LazyListState?,
    yearViewScale: Animatable<Float, AnimationVector1D>?,
    viewModel: CalendarViewModel,
    isLandscape: Boolean,
    today: Jdn,
    hasToolbarHeight: Boolean,
) {
    val coroutineScope = rememberCoroutineScope()
    val selectedMonth = mainCalendar.getMonthStartFromMonthsDistance(
        baseJdn = today,
        monthsDistance = viewModel.selectedMonthOffset,
    )
    val yearViewOffset = yearViewOffset(yearViewLazyListState)
    val yearViewIsInYearSelection = yearViewIsInYearSelection(yearViewScale)

    var isYearView by isYearView
    var yearViewCalendar by yearViewCalendar
    val onYearViewBackPressed = {
        isYearView = false
        yearViewCalendar = null
    }
    if (isYearView) BackHandler(onBack = onYearViewBackPressed)

    @OptIn(ExperimentalMaterial3Api::class) TopAppBar(
        title = {
            val refreshToken = viewModel.refreshToken
            // just a noop to update title and subtitle when secondary calendar is toggled
            refreshToken.run {}

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
                    val selectedDay = viewModel.selectedDay
                    val selectedDate = selectedDay on mainCalendar
                    val isCurrentMonth =
                        selectedDate.year == selectedMonth.year && selectedDate.month == selectedMonth.month
                    if (viewModel.isHighlighted && isCurrentMonth) {
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
                            YearViewCommand.ToggleYearSelection(
                                yearViewLazyListState,
                                yearViewScale,
                            )
                        } else yearViewCalendar = mainCalendar
                    }
                    .then(
                        // Toolbar height might not exist if screen rotated while being in year view
                        if (isYearView && hasToolbarHeight) Modifier.fillMaxSize() else Modifier,
                    ),
                verticalArrangement = Arrangement.Center,
            ) {
                val yearViewCalendarValue = yearViewCalendar
                if (isYearView && yearViewCalendarValue != null) AppScreenModesDropDown(
                    value = yearViewCalendarValue,
                    onValueChange = { yearViewCalendar = it },
                    values = enabledCalendars.takeIf { it.size > 1 } ?: language.defaultCalendars,
                    small = subtitle.isNotEmpty(),
                ) { stringResource(it.title) } else Crossfade(targetState = title) { title ->
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
                                    ) { yearViewCalendar = null }
                                    .padding(horizontal = 8.dp)
                            },
                        )
                    }
                }
            }
        },
        colors = appTopAppBarColors(),
        navigationIcon = {
            Crossfade(targetState = isYearView) { state ->
                if (state) NavigationNavigateUpIcon(
                    navigateUp = onYearViewBackPressed,
                ) else NavigationOpenNavigationRailIcon(openNavigationRail)
            }
        },
        actions = {
            AnimatedVisibility(visible = isYearView) {
                TodayActionButton(visible = yearViewOffset != 0 && !yearViewIsInYearSelection) {
                    yearViewCalendar = mainCalendar
                    coroutineScope.launch {
                        YearViewCommand.TodayMonth(
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
                        YearViewCommand.NextMonth(
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
                        YearViewCommand.PreviousMonth(
                            yearViewLazyListState,
                            yearViewScale,
                        )
                    }
                }
            }

            AnimatedVisibility(!isYearView) {
                TodayActionButton(viewModel.selectedMonthOffset != 0 || viewModel.isHighlighted) {
                    yearViewCalendar = null
                    viewModel.bringDay(Jdn.today(), highlight = false)
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
                    viewModel = viewModel,
                    isLandscape = isLandscape,
                    swipeUpActions = swipeUpActions,
                    swipeDownActions = swipeDownActions,
                    isTalkBackEnabled = isTalkBackEnabled,
                )
            }
        },
    )
}

@Composable
private fun SharedTransitionScope.Menu(
    swipeUpActions: Map<SwipeUpAction, () -> Unit>,
    swipeDownActions: Map<SwipeDownAction, () -> Unit>,
    viewModel: CalendarViewModel,
    isLandscape: Boolean,
    isTalkBackEnabled: Boolean,
) {
    val context = LocalContext.current
    val resources = LocalResources.current

    var showDatePickerDialog by rememberSaveable { mutableStateOf(false) }
    if (showDatePickerDialog) {
        val selectedDay = viewModel.selectedDay
        DatePickerDialog(selectedDay, { showDatePickerDialog = false }) { jdn ->
            viewModel.bringDay(jdn)
        }
    }

    var showShiftWorkDialog by rememberSaveable { mutableStateOf(false) }
    if (showShiftWorkDialog) ShiftWorkDialog(
        selectedJdn = viewModel.selectedDay,
        onDismissRequest = { showShiftWorkDialog = false },
    )

    var showPlanetaryHoursDialog by rememberSaveable { mutableStateOf(false) }
    if (showPlanetaryHoursDialog) coordinates?.also {
        PlanetaryHoursDialog(
            coordinates = it,
            now = viewModel.now + (viewModel.selectedDay - viewModel.today).days.inWholeMilliseconds,
            isToday = viewModel.today == viewModel.selectedDay,
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
                baseJdn = Jdn.today(),
                monthsDistance = viewModel.selectedMonthOffset,
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
    viewModel: CalendarViewModel, snackbarHostState: SnackbarHostState,
): (AddEventData) -> Unit {
    val addEvent = rememberLauncherForActivityResult(AddEventContract()) {
        viewModel.refreshCalendar()
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var addEventData by remember { mutableStateOf<AddEventData?>(null) }

    addEventData?.let { data ->
        AskForCalendarPermissionDialog { isGranted ->
            viewModel.refreshCalendar()
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
