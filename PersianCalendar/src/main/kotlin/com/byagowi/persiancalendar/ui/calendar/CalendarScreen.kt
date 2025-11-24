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
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.byagowi.persiancalendar.BuildConfig
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
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.ui.astronomy.PlanetaryHoursDialog
import com.byagowi.persiancalendar.ui.calendar.calendarpager.CalendarPager
import com.byagowi.persiancalendar.ui.calendar.calendarpager.calendarPagerSize
import com.byagowi.persiancalendar.ui.calendar.calendarpager.calendarPagerState
import com.byagowi.persiancalendar.ui.calendar.reports.prayTimeHtmlReport
import com.byagowi.persiancalendar.ui.calendar.shiftwork.ShiftWorkDialog
import com.byagowi.persiancalendar.ui.calendar.shiftwork.ShiftWorkViewModel
import com.byagowi.persiancalendar.ui.calendar.shiftwork.fillViewModelFromGlobalVariables
import com.byagowi.persiancalendar.ui.calendar.times.TimesTab
import com.byagowi.persiancalendar.ui.calendar.yearview.YearView
import com.byagowi.persiancalendar.ui.calendar.yearview.YearViewCommand
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
import com.byagowi.persiancalendar.ui.common.NavigationOpenNavigationRailIcon
import com.byagowi.persiancalendar.ui.common.ScreenSurface
import com.byagowi.persiancalendar.ui.common.ScrollShadow
import com.byagowi.persiancalendar.ui.common.ThreeDotsDropdownMenu
import com.byagowi.persiancalendar.ui.common.TodayActionButton
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.appContentSizeAnimationSpec
import com.byagowi.persiancalendar.ui.utils.bringMarketPage
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeNoBottomEnd
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop
import com.byagowi.persiancalendar.ui.utils.openHtmlInBrowser
import com.byagowi.persiancalendar.utils.dayTitleSummary
import com.byagowi.persiancalendar.utils.debugAssertNotNull
import com.byagowi.persiancalendar.utils.getEnabledAlarms
import com.byagowi.persiancalendar.utils.hasAnyWidgetUpdateRecently
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.monthFormatForSecondaryCalendar
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.utils.otherCalendarFormat
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.supportedYearOfIranCalendar
import com.byagowi.persiancalendar.utils.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.GregorianCalendar
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalSharedTransitionApi::class)
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
    animatedContentScope: AnimatedContentScope,
    isCurrentDestination: Boolean,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val isYearView by viewModel.isYearView.collectAsState()
    val context = LocalContext.current
    val addEvent = addEvent(viewModel, snackbarHostState)
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val today by viewModel.today.collectAsState()

    val daysScreenSelectedDay by viewModel.daysScreenSelectedDay.collectAsState()
    LaunchedEffect(daysScreenSelectedDay) {
        daysScreenSelectedDay?.let { viewModel.bringDay(it, it != today) }
    }

    val density = LocalDensity.current
    var fabPlaceholderHeight by remember { mutableStateOf<Dp?>(null) }

    val detailsTabs = detailsTabs(
        viewModel = viewModel,
        navigateToHolidaysSettings = navigateToHolidaysSettings,
        navigateToSettingsLocationTab = navigateToSettingsLocationTab,
        navigateToSettingsLocationTabSetAthanAlarm = navigateToSettingsLocationTabSetAthanAlarm,
        navigateToAstronomy = navigateToAstronomy,
        animatedContentScope = animatedContentScope,
        today = today,
        fabPlaceholderHeight = fabPlaceholderHeight,
    )
    val isOnlyEventsTab = detailsTabs.size == 1

    val swipeUpActions = mapOf(
        SwipeUpAction.Schedule to { navigateToSchedule() },
        SwipeUpAction.DayView to { navigateToDays(viewModel.selectedDay.value, false) },
        SwipeUpAction.WeekView to { navigateToDays(viewModel.selectedDay.value, true) },
        SwipeUpAction.None to {
            if (isOnlyEventsTab) viewModel.bringDay(viewModel.selectedDay.value - 7)
        },
    )

    val swipeDownActions = mapOf(
//            SwipeDownAction.MonthView to { navigateToMonthView() },
        SwipeDownAction.YearView to {
            viewModel.closeSearch()
            viewModel.openYearView()
        },
        SwipeDownAction.None to {
            if (isOnlyEventsTab) viewModel.bringDay(viewModel.selectedDay.value + 7)
        },
    )

    Scaffold(
        modifier = Modifier.onKeyEvent { keyEvent ->
            if (!viewModel.isYearView.value && keyEvent.type == KeyEventType.KeyDown) {
                when (keyEvent.key) {
                    Key.D -> {
                        navigateToDays(viewModel.selectedDay.value, false)
                        true
                    }

                    Key.W -> {
                        navigateToDays(viewModel.selectedDay.value, true)
                        true
                    }

                    Key.Y -> {
                        viewModel.openYearView()
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
            val searchBoxIsOpen by viewModel.isSearchOpen.collectAsState()
            BackHandler(enabled = searchBoxIsOpen, onBack = viewModel::closeSearch)
            var toolbarHeight by remember { mutableStateOf(0.dp) }
            Crossfade(searchBoxIsOpen, label = "toolbar") { searchBoxIsOpenState ->
                Box(
                    (if (searchBoxIsOpenState) {
                        val query by viewModel.query.collectAsState()
                        if (query.isEmpty() && toolbarHeight > 0.dp) Modifier.requiredHeight(
                            toolbarHeight
                        ) else Modifier
                    } else if (isYearView) {
                        if (toolbarHeight > 0.dp) {
                            Modifier.requiredHeight(toolbarHeight)
                        } else Modifier
                    } else Modifier.onSizeChanged {
                        toolbarHeight = with(density) { it.height.toDp() }
                    }).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    if (searchBoxIsOpenState) Search(viewModel) else Toolbar(
                        animatedContentScope = animatedContentScope,
                        openNavigationRail = openNavigationRail,
                        swipeUpActions = swipeUpActions,
                        swipeDownActions = swipeDownActions,
                        viewModel = viewModel,
                        isLandscape = isLandscape,
                        today = today,
                        hasToolbarHeight = toolbarHeight > 0.dp,
                    )
                }
            }
        },
        floatingActionButton = {
            val selectedTab by viewModel.selectedTab.collectAsState()

            // Window height fallback for older device isn't consistent, let's just
            // use some hardcoded value in detailsTabs() instead
            val windowHeightPx = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                LocalActivity.current?.windowManager?.currentWindowMetrics?.bounds?.height()
            } else null

            AnimatedVisibility(
                visible = (selectedTab == CalendarScreenTab.EVENT || isOnlyEventsTab) && !isYearView && isCurrentDestination,
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
                    onClick = { addEvent(AddEventData.fromJdn(viewModel.selectedDay.value)) },
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
                .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Start))
        ) {
            val maxWidth = this.maxWidth
            val maxHeight = this.maxHeight
            val pagerSize = calendarPagerSize(isLandscape, maxWidth, maxHeight, bottomPadding)

            Column(Modifier.fillMaxSize()) {
                this.AnimatedVisibility(isYearView) {
                    YearView(viewModel, maxWidth, maxHeight, bottomPaddingWithMinimum)
                }

                // To preserve pager's state even in year view where calendar isn't in the tree
                val pagerState = calendarPagerState()
                val detailsPagerState = detailsPagerState(viewModel = viewModel, tabs = detailsTabs)

                this.AnimatedVisibility(
                    !isYearView,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Top, clip = false),
                ) {
                    if (isLandscape) Row {
                        Box(Modifier.size(pagerSize)) {
                            CalendarPager(
                                viewModel = viewModel,
                                today = today,
                                pagerState = pagerState,
                                addEvent = addEvent,
                                suggestedPagerSize = pagerSize,
                                navigateToDays = navigateToDays,
                            )
                        }
                        ScreenSurface(
                            animatedContentScope,
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
                                            WindowInsetsSides.End
                                        )
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
                                                    swipeUpActions[preferredSwipeUpAction.value]
                                                }

                                                !isUp && wasAtTop -> {
                                                    swipeDownActions[preferredSwipeDownAction.value]
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
                                        today = today,
                                        pagerState = pagerState,
                                        addEvent = addEvent,
                                        suggestedPagerSize = pagerSize,
                                        navigateToDays = navigateToDays,
                                    )
                                }

                                val detailsMinHeight = maxHeight - calendarHeight
                                ScreenSurface(
                                    animatedContentScope,
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

    val eventsRepository by eventsRepository.collectAsState()
    LaunchedEffect(today, eventsRepository) {
        if (mainCalendar == Calendar.SHAMSI && eventsRepository.iranHolidays && today.toPersianDate().year > supportedYearOfIranCalendar) {
            if (snackbarHostState.showSnackbar(
                    context.getString(R.string.outdated_app),
                    duration = SnackbarDuration.Long,
                    actionLabel = context.getString(R.string.update),
                    withDismissAction = true,
                ) == SnackbarResult.ActionPerformed
            ) context.bringMarketPage()
        }
    }
}

enum class CalendarScreenTab(@get:StringRes val titleId: Int) {
    CALENDAR(R.string.calendar), EVENT(R.string.events), TIMES(R.string.times)
}

@Composable
private fun enableTimesTab(): Boolean {
    val coordinates by coordinates.collectAsState()
    val language by language.collectAsState()
    val preferences = LocalContext.current.preferences
    return coordinates != null || // if coordinates is set, should be shown
            (language.isPersianOrDari && // The placeholder isn't translated to other languages
                    // The user is already dismissed the third tab
                    !preferences.getBoolean(PREF_DISMISSED_OWGHAT, false) &&
                    // Try to not show the placeholder to established users
                    PREF_APP_LANGUAGE !in preferences)
}

private typealias DetailsTab = Pair<CalendarScreenTab, @Composable (MutableInteractionSource, minHeight: Dp, bottomPadding: Dp) -> Unit>

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.detailsTabs(
    viewModel: CalendarViewModel,
    navigateToHolidaysSettings: (item: String?) -> Unit,
    navigateToSettingsLocationTab: () -> Unit,
    navigateToSettingsLocationTabSetAthanAlarm: () -> Unit,
    navigateToAstronomy: (Jdn) -> Unit,
    animatedContentScope: AnimatedContentScope,
    today: Jdn,
    fabPlaceholderHeight: Dp?,
): List<DetailsTab> {
    val removeThirdTab by viewModel.removedThirdTab.collectAsState()
    val hasTimesTab = enableTimesTab() && !removeThirdTab
    val isAstronomicalExtraFeaturesEnabled by isAstronomicalExtraFeaturesEnabled.collectAsState()
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
                animatedContentScope = animatedContentScope,
            )
        } else null,
        CalendarScreenTab.EVENT to { _, _, bottomPadding ->
            EventsTab(
                navigateToHolidaysSettings = navigateToHolidaysSettings,
                viewModel = viewModel,
                animatedContentScope = animatedContentScope,
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
                animatedContentScope = animatedContentScope,
                viewModel = viewModel,
                interactionSource = interactionSource,
                minHeight = minHeight,
                bottomPadding = bottomPadding,
            )
        } else null,
    )
}

@Composable
private fun detailsPagerState(
    viewModel: CalendarViewModel,
    tabs: List<DetailsTab>,
): PagerState {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val pagerState = rememberPagerState(
        initialPage = selectedTab.ordinal.coerceAtMost(tabs.size - 1),
        pageCount = tabs::size,
    )
    LaunchedEffect(key1 = pagerState.currentPage) {
        viewModel.changeSelectedTab(
            CalendarScreenTab.entries.getOrNull(pagerState.currentPage)
                ?: CalendarScreenTab.entries[0]
        )
    }
    return pagerState
}

@OptIn(ExperimentalMaterial3Api::class)
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
        val selectedTab by viewModel.selectedTab.collectAsState()
        val coroutineScope = rememberCoroutineScope()

        if (!isOnlyEventsTab) PrimaryTabRow(
            selectedTabIndex = selectedTab.ordinal,
            divider = {},
            containerColor = Color.Transparent,
            indicator = {
                val offset = selectedTab.ordinal.coerceAtMost(tabs.size - 1)
                TabRowDefaults.PrimaryIndicator(Modifier.tabIndicatorOffset(offset))
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
                    val jdn by viewModel.selectedDay.collectAsState()
                    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
                    Modifier
                        .detectHorizontalSwipe {
                            { isLeft ->
                                val newJdn = jdn + if (isLeft xor isRtl) -1 else 1
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.CalendarsTab(
    viewModel: CalendarViewModel,
    interactionSource: MutableInteractionSource,
    minHeight: Dp,
    bottomPadding: Dp,
    today: Jdn,
    navigateToAstronomy: (Jdn) -> Unit,
    animatedContentScope: AnimatedContentScope,
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
            )
    ) {
        val jdn by viewModel.selectedDay.collectAsState()
        Spacer(Modifier.height(24.dp))
        CalendarsOverview(
            jdn = jdn,
            today = today,
            selectedCalendar = mainCalendar,
            shownCalendars = enabledCalendars,
            isExpanded = isExpanded,
            navigateToAstronomy = navigateToAstronomy,
            animatedContentScope = animatedContentScope,
        )

        val context = LocalContext.current
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED && PREF_NOTIFY_IGNORED !in context.preferences
        ) {
            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                context.preferences.edit { putBoolean(PREF_NOTIFY_DATE, isGranted) }
                updateStoredPreference(context)
                if (isGranted) update(context, updateDate = true)
            }
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
                ActivityResultContracts.RequestPermission()
            ) { requestExemption() }

            EncourageActionLayout(
                header = stringResource(R.string.exempt_app_battery_optimization),
                acceptButton = stringResource(R.string.yes),
                discardAction = ::ignore,
            ) {
                val alarmManager = context.getSystemService<AlarmManager>()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && runCatching { alarmManager?.canScheduleExactAlarms() }.getOrNull().debugAssertNotNull == false) launcher.launch(
                    Manifest.permission.SCHEDULE_EXACT_ALARM
                ) else requestExemption()
            }
        }
        Spacer(Modifier.height(bottomPadding))
    }
}

@Composable
private fun showEncourageToExemptFromBatteryOptimizations(): Boolean {
    val isNotifyDate by isNotifyDate.collectAsState()
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
            context.applicationContext.packageName
        )
    }.onFailure(logException).getOrNull() == true
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun Search(viewModel: CalendarViewModel) {
    val repository by eventsRepository.collectAsState()
    LaunchedEffect(repository) { viewModel.initializeEventsStore(repository) }
    val query by viewModel.query.collectAsState()
    val expanded = query.isNotEmpty()
    val events by viewModel.foundItems.collectAsState()
    val padding by animateDpAsState(if (expanded) 0.dp else 32.dp, label = "padding")
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = { viewModel.changeQuery(it) },
                onSearch = {},
                expanded = expanded,
                onExpandedChange = {},
                placeholder = { Text(stringResource(R.string.search_in_events)) },
                trailingIcon = {
                    AppIconButton(
                        icon = Icons.Default.Close,
                        title = stringResource(R.string.close),
                    ) { viewModel.closeSearch() }
                },
            )
        },
        expanded = expanded,
        onExpandedChange = { if (!it) viewModel.changeQuery("") },
        modifier = Modifier
            .padding(horizontal = padding)
            .focusRequester(focusRequester),
    ) {
        if (padding.value != 0f) return@SearchBar
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            events.take(10).forEach { event ->
                Box(
                    Modifier
                        .clickable {
                            viewModel.closeSearch()
                            viewModel.bringEvent(event)
                        }
                        .fillMaxWidth()
                        .padding(vertical = 20.dp, horizontal = 24.dp),
                ) {
                    AnimatedContent(
                        targetState = event.title,
                        label = "title",
                        transitionSpec = appCrossfadeSpec,
                    ) { state ->
                        Text(
                            state,
                            modifier = Modifier.align(Alignment.CenterStart),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
            if (events.size > 10) Text("…", Modifier.padding(vertical = 12.dp, horizontal = 24.dp))
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.Toolbar(
    animatedContentScope: AnimatedContentScope,
    openNavigationRail: () -> Unit,
    swipeUpActions: Map<SwipeUpAction, () -> Unit>,
    swipeDownActions: Map<SwipeDownAction, () -> Unit>,
    viewModel: CalendarViewModel,
    isLandscape: Boolean,
    today: Jdn,
    hasToolbarHeight: Boolean,
) {
    val selectedMonthOffset by viewModel.selectedMonthOffset.collectAsState()
    val selectedMonth = mainCalendar.getMonthStartFromMonthsDistance(today, selectedMonthOffset)
    val isYearView by viewModel.isYearView.collectAsState()
    val yearViewOffset by viewModel.yearViewOffset.collectAsState()
    val yearViewIsInYearSelection by viewModel.yearViewIsInYearSelection.collectAsState()
    val isTalkBackEnabled by isTalkBackEnabled.collectAsState()

    BackHandler(enabled = isYearView, onBack = viewModel::onYearViewBackPressed)

    @OptIn(ExperimentalMaterial3Api::class) TopAppBar(
        title = {
            val refreshToken by viewModel.refreshToken.collectAsState()
            // just a noop to update title and subtitle when secondary calendar is toggled
            refreshToken.run {}

            val yearViewCalendar = viewModel.yearViewCalendar.collectAsState().value
            val language by language.collectAsState()
            val title: String
            val subtitle: String
            run {
                val numeral by numeral.collectAsState()
                val secondaryCalendar =
                    yearViewCalendar.takeIf { it != mainCalendar } ?: secondaryCalendar
                if (isYearView && yearViewCalendar != null) {
                    title = stringResource(
                        if (yearViewIsInYearSelection) R.string.select_year else R.string.year_view
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
                        selectedMonth.monthName, numeral.format(selectedMonth.year)
                    )
                    val selectedDay by viewModel.selectedDay.collectAsState()
                    val selectedDate = selectedDay on mainCalendar
                    val isCurrentMonth =
                        selectedDate.year == selectedMonth.year && selectedDate.month == selectedMonth.month
                    val isHighlighted by viewModel.isHighlighted.collectAsState()
                    if (isHighlighted && isCurrentMonth) {
                        val selectedSecondaryDate = selectedDay on secondaryCalendar
                        subtitle = language.my.format(
                            selectedSecondaryDate.monthName,
                            numeral.format(selectedSecondaryDate.year)
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
                            else R.string.year_view
                        ),
                    ) {
                        if (isYearView) viewModel.commandYearView(YearViewCommand.ToggleYearSelection)
                        else viewModel.openYearView()
                    }
                    .then(
                        // Toolbar height might not exist if screen rotated while being in year view
                        if (isYearView && hasToolbarHeight) Modifier.fillMaxSize() else Modifier
                    ),
                verticalArrangement = Arrangement.Center,
            ) {
                if (isYearView && yearViewCalendar != null) AppScreenModesDropDown(
                    yearViewCalendar,
                    onValueChange = viewModel::changeYearViewCalendar,
                    label = { stringResource(it.title) },
                    values = enabledCalendars.takeIf { it.size > 1 } ?: language.defaultCalendars,
                    small = subtitle.isNotEmpty(),
                ) else Crossfade(title, label = "title") { title ->
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                this.AnimatedVisibility(visible = subtitle.isNotEmpty()) {
                    Crossfade(subtitle, label = "subtitle") { subtitle ->
                        val fraction by animateFloatAsState(
                            targetValue = if (isYearView) 1f else 0f, label = "font size"
                        )
                        Text(
                            if (isTalkBackEnabled && isYearView) "$subtitle ${stringResource(R.string.year_view)}"
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
                                    ) { viewModel.changeYearViewCalendar(null) }
                                    .padding(horizontal = 8.dp)
                            },
                        )
                    }
                }
            }
        },
        colors = appTopAppBarColors(),
        navigationIcon = {
            Crossfade(targetState = isYearView, label = "nav icon") { state ->
                if (state) AppIconButton(
                    icon = Icons.AutoMirrored.Default.ArrowBack,
                    title = stringResource(R.string.close),
                    onClick = viewModel::onYearViewBackPressed,
                ) else NavigationOpenNavigationRailIcon(animatedContentScope, openNavigationRail)
            }
        },
        actions = {
            this.AnimatedVisibility(isYearView) {
                TodayActionButton(yearViewOffset != 0 && !yearViewIsInYearSelection) {
                    viewModel.changeYearViewCalendar(mainCalendar)
                    viewModel.commandYearView(YearViewCommand.TodayMonth)
                }
            }
            this.AnimatedVisibility(isYearView && !yearViewIsInYearSelection) {
                AppIconButton(
                    icon = Icons.Default.KeyboardArrowDown,
                    title = stringResource(R.string.next_x, stringResource(R.string.year)),
                ) { viewModel.commandYearView(YearViewCommand.NextMonth) }
            }
            this.AnimatedVisibility(isYearView && !yearViewIsInYearSelection) {
                AppIconButton(
                    icon = Icons.Default.KeyboardArrowUp,
                    title = stringResource(R.string.previous_x, stringResource(R.string.year)),
                ) { viewModel.commandYearView(YearViewCommand.PreviousMonth) }
            }

            this.AnimatedVisibility(!isYearView) {
                val todayButtonVisibility by viewModel.todayButtonVisibility.collectAsState()
                TodayActionButton(todayButtonVisibility) {
                    viewModel.changeYearViewCalendar(null)
                    viewModel.bringDay(Jdn.today(), highlight = false)
                }
            }
            this.AnimatedVisibility(!isYearView) {
                AppIconButton(
                    icon = Icons.Default.Search,
                    title = stringResource(R.string.search_in_events),
                ) { viewModel.openSearch() }
            }
            this.AnimatedVisibility(!isYearView) {
                Menu(
                    animatedContentScope = animatedContentScope,
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.Menu(
    animatedContentScope: AnimatedContentScope,
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
        val selectedDay by viewModel.selectedDay.collectAsState()
        DatePickerDialog(selectedDay, { showDatePickerDialog = false }) { jdn ->
            viewModel.bringDay(jdn)
        }
    }

    val shiftWorkViewModel by viewModel.shiftWorkViewModel.collectAsState()
    shiftWorkViewModel?.let {
        val selectedDay by viewModel.selectedDay.collectAsState()
        ShiftWorkDialog(
            it,
            selectedDay,
            onDismissRequest = { viewModel.setShiftWorkViewModel(null) },
        ) { viewModel.refreshCalendar() }
    }

    val coordinates by coordinates.collectAsState()

    var showPlanetaryHoursDialog by rememberSaveable { mutableStateOf(false) }
    if (showPlanetaryHoursDialog) coordinates?.also {
        val now by viewModel.now.collectAsState()
        val today by viewModel.today.collectAsState()
        val selectedDay by viewModel.selectedDay.collectAsState()
        PlanetaryHoursDialog(
            coordinates = it,
            now = now + (selectedDay - today).days.inWholeMilliseconds,
            isToday = today == selectedDay,
        ) { showPlanetaryHoursDialog = false }
    }

    ThreeDotsDropdownMenu(animatedContentScope) { closeMenu ->
        AppDropdownMenuItem({ Text(stringResource(R.string.select_date)) }) {
            closeMenu()
            showDatePickerDialog = true
        }

        AppDropdownMenuItem({ Text(stringResource(R.string.shift_work_settings)) }) {
            closeMenu()
            val dialogViewModel = ShiftWorkViewModel()
            // from already initialized global variable till a better solution
            fillViewModelFromGlobalVariables(dialogViewModel, viewModel.selectedDay.value)
            viewModel.setShiftWorkViewModel(dialogViewModel)
        }

        if (coordinates != null) AppDropdownMenuItem(text = { Text(stringResource(R.string.month_pray_times)) }) {
            closeMenu()
            val selectedMonthOffset = viewModel.selectedMonthOffset.value
            val selectedMonth =
                mainCalendar.getMonthStartFromMonthsDistance(Jdn.today(), selectedMonthOffset)
            context.openHtmlInBrowser(prayTimeHtmlReport(resources, selectedMonth))
        }
        val isAstronomicalExtraFeaturesEnabled by isAstronomicalExtraFeaturesEnabled.collectAsState()
        if (coordinates != null && isAstronomicalExtraFeaturesEnabled) AppDropdownMenuItem({
            Text(stringResource(R.string.planetary_hours))
        }) {
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
                    Box(Modifier.clickable(null, ripple(bounded = false)) {
                        context.preferences.edit { putString(prefKey, valueToStoreOnClick()) }
                    }) {
                        val alpha by animateFloatAsState(
                            targetValue = if (preferredAction == item) 1f else .2f,
                            label = "alpha",
                        )
                        val color = LocalContentColor.current.copy(alpha = alpha)
                        Icon(swipeIcon, null, tint = color)
                    }
                },
            ) { closeMenu(); action() }
        }

        val preferredSwipeUpAction by preferredSwipeUpAction.collectAsState()
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

        val preferredSwipeDownAction by preferredSwipeDownAction.collectAsState()
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

        val isShowWeekOfYearEnabled by isShowWeekOfYearEnabled.collectAsState()
        AppDropdownMenuCheckableItem(
            text = stringResource(R.string.week_number),
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
            text = stringResource(R.string.show_secondary_calendar),
            isExpanded = showSecondaryCalendarSubMenu,
            onClick = { showSecondaryCalendarSubMenu = !showSecondaryCalendarSubMenu },
        )

        (listOf(null) + enabledCalendars.drop(1)).forEach { calendar ->
            this.AnimatedVisibility(showSecondaryCalendarSubMenu) {
                AppDropdownMenuRadioItem(
                    stringResource(calendar?.title ?: R.string.none), calendar == secondaryCalendar
                ) {
                    context.preferences.edit {
                        if (calendar == null) remove(PREF_SECONDARY_CALENDAR_IN_TABLE)
                        else {
                            putBoolean(PREF_SECONDARY_CALENDAR_IN_TABLE, true)
                            val newOtherCalendars =
                                listOf(calendar) + (enabledCalendars.drop(1) - calendar)
                            putString(
                                PREF_OTHER_CALENDARS_KEY,
                                // Put the chosen calendars at the first of calendars priorities
                                newOtherCalendars.joinToString(",")
                            )
                        }
                    }
                    updateStoredPreference(context)
                    viewModel.refreshCalendar()
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
                CalendarContract.Events.DESCRIPTION, description
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
    viewModel: CalendarViewModel, snackbarHostState: SnackbarHostState
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
                if (language.value.isPersianOrDari) coroutineScope.launch {
                    if (snackbarHostState.showSnackbar(
                            "جهت افزودن رویداد نیاز است از نصب و فعال بودن تقویم گوگل اطمینان حاصل کنید",
                            duration = SnackbarDuration.Long,
                            actionLabel = "نصب",
                            withDismissAction = true,
                        ) == SnackbarResult.ActionPerformed
                    ) context.bringMarketPage("com.google.android.calendar")
                } else Toast.makeText(context, R.string.device_does_not_support, Toast.LENGTH_SHORT)
                    .show()
            }
            addEventData = null
        }
    }

    return { addEventData = it }
}
