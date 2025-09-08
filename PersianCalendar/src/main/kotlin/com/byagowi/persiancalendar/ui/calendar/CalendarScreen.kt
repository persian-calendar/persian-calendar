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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.lerp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.core.content.getSystemService
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.PREF_BATTERY_OPTIMIZATION_IGNORED_COUNT
import com.byagowi.persiancalendar.PREF_DISABLE_OWGHAT
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
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.enabledCalendars
import com.byagowi.persiancalendar.global.isAstronomicalExtraFeaturesEnabled
import com.byagowi.persiancalendar.global.isIranHolidaysEnabled
import com.byagowi.persiancalendar.global.isNotifyDate
import com.byagowi.persiancalendar.global.isShowWeekOfYearEnabled
import com.byagowi.persiancalendar.global.isTalkBackEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
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
import com.byagowi.persiancalendar.ui.common.AppIconButton
import com.byagowi.persiancalendar.ui.common.AskForCalendarPermissionDialog
import com.byagowi.persiancalendar.ui.common.CalendarsOverview
import com.byagowi.persiancalendar.ui.common.DatePickerDialog
import com.byagowi.persiancalendar.ui.common.NavigationOpenDrawerIcon
import com.byagowi.persiancalendar.ui.common.ScreenSurface
import com.byagowi.persiancalendar.ui.common.ScrollShadow
import com.byagowi.persiancalendar.ui.common.ThreeDotsDropdownMenu
import com.byagowi.persiancalendar.ui.common.TodayActionButton
import com.byagowi.persiancalendar.ui.settings.locationathan.location.LocationDialog
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.bringMarketPage
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeNoBottomEnd
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop
import com.byagowi.persiancalendar.ui.utils.openHtmlInBrowser
import com.byagowi.persiancalendar.utils.calendar
import com.byagowi.persiancalendar.utils.dayTitleSummary
import com.byagowi.persiancalendar.utils.debugAssertNotNull
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getA11yDaySummary
import com.byagowi.persiancalendar.utils.getEnabledAlarms
import com.byagowi.persiancalendar.utils.hasAnyWidgetUpdateRecently
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.monthFormatForSecondaryCalendar
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.supportedYearOfIranCalendar
import com.byagowi.persiancalendar.utils.update
import com.byagowi.persiancalendar.utils.yearViewYearFormat
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Date
import java.util.GregorianCalendar
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.CalendarScreen(
    openDrawer: () -> Unit,
    navigateToSchedule: () -> Unit,
    navigateToMonthView: () -> Unit,
    navigateToHolidaysSettings: () -> Unit,
    navigateToSettingsLocationTab: () -> Unit,
    navigateToSettingsLocationTabSetAthanAlarm: () -> Unit,
    navigateToAstronomy: (Jdn) -> Unit,
    navigateToDays: (Jdn, isWeek: Boolean) -> Unit,
    viewModel: CalendarViewModel,
    animatedContentScope: AnimatedContentScope,
    isCurrentDestination: Boolean,
) {
    LocationDialog {}
    val snackbarHostState = remember { SnackbarHostState() }
    val isYearView by viewModel.isYearView.collectAsState()
    val context = LocalContext.current
    val addEvent = addEvent(viewModel, snackbarHostState)
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val today by viewModel.today.collectAsState()

    val daysScreenSelectedDay by viewModel.daysScreenSelectedDay.collectAsState()
    LaunchedEffect(daysScreenSelectedDay) {
        daysScreenSelectedDay?.let { bringDate(viewModel, it, context, it != today) }
    }

    val swipeUpActions = mapOf(
        SwipeUpAction.Schedule to { navigateToSchedule() },
        SwipeUpAction.DayView to { navigateToDays(viewModel.selectedDay.value, false) },
        SwipeUpAction.WeekView to { navigateToDays(viewModel.selectedDay.value, true) },
    )

    val swipeDownActions = mapOf(
//            SwipeDownAction.MonthView to { navigateToMonthView() },
        SwipeDownAction.YearView to { viewModel.openYearView() },
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

            Crossfade(searchBoxIsOpen, label = "toolbar") {
                if (it) Search(viewModel) else Toolbar(
                    animatedContentScope = animatedContentScope,
                    openDrawer = openDrawer,
                    swipeUpActions = swipeUpActions,
                    swipeDownActions = swipeDownActions,
                    viewModel = viewModel,
                    isLandscape = isLandscape,
                    today = today,
                )
            }
        },
        floatingActionButton = {
            val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()
            AnimatedVisibility(
                visible = selectedTabIndex == EVENTS_TAB && !isYearView && isCurrentDestination,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .renderInSharedTransitionScopeOverlay(
                        renderInOverlay = { isCurrentDestination && isTransitionActive },
                    ),
                enter = scaleIn(),
                exit = scaleOut(),
            ) {
                FloatingActionButton(
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
        BoxWithConstraints(Modifier.padding(top = paddingValues.calculateTopPadding())) {
            val maxWidth = this.maxWidth
            val maxHeight = this.maxHeight
            val pagerSize = calendarPagerSize(isLandscape, maxWidth, maxHeight, bottomPadding)

            Column(Modifier.fillMaxSize()) {
                this.AnimatedVisibility(isYearView) {
                    YearView(viewModel, maxWidth, maxHeight, bottomPaddingWithMinimum)
                }

                // To preserve pager's state even in year view where calendar isn't in the tree
                val pagerState = calendarPagerState()

                val detailsTabs = detailsTabs(
                    viewModel = viewModel,
                    navigateToHolidaysSettings = navigateToHolidaysSettings,
                    navigateToSettingsLocationTab = navigateToSettingsLocationTab,
                    navigateToSettingsLocationTabSetAthanAlarm = navigateToSettingsLocationTabSetAthanAlarm,
                    navigateToAstronomy = navigateToAstronomy,
                    animatedContentScope = animatedContentScope,
                    bottomPadding = bottomPaddingWithMinimum,
                    today = today,
                )
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
                                animatedContentScope = animatedContentScope,
                            )
                        }
                        ScreenSurface(animatedContentScope, materialCornerExtraLargeNoBottomEnd()) {
                            Details(
                                viewModel = viewModel,
                                tabs = detailsTabs,
                                pagerState = detailsPagerState,
                                contentMinHeight = maxHeight,
                                scrollableTabs = true,
                                modifier = Modifier.fillMaxHeight(),
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
                                val density = LocalDensity.current
                                Box(
                                    Modifier
                                        .offset { IntOffset(0, scrollState.value * 3 / 4) }
                                        .onSizeChanged {
                                            calendarHeight = with(density) { it.height.toDp() }
                                        }
                                        .animateContentSize(),
                                ) {
                                    CalendarPager(
                                        viewModel = viewModel,
                                        today = today,
                                        pagerState = pagerState,
                                        addEvent = addEvent,
                                        suggestedPagerSize = pagerSize,
                                        navigateToDays = navigateToDays,
                                        animatedContentScope = animatedContentScope,
                                    )
                                }

                                val detailsMinHeight = maxHeight - calendarHeight
                                ScreenSurface(animatedContentScope, workaroundClipBug = true) {
                                    Details(
                                        viewModel = viewModel,
                                        tabs = detailsTabs,
                                        pagerState = detailsPagerState,
                                        contentMinHeight = detailsMinHeight,
                                        modifier = Modifier.defaultMinSize(minHeight = detailsMinHeight),
                                    )
                                }
                            }
                            ScrollShadow(scrollState, top = false)
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(today) {
        if (mainCalendar == Calendar.SHAMSI && isIranHolidaysEnabled && today
                .toPersianDate().year > supportedYearOfIranCalendar
        ) {
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

const val CALENDARS_TAB = 0
const val EVENTS_TAB = 1
const val TIMES_TAB = 2

private fun enableTimesTab(context: Context): Boolean {
    val preferences = context.preferences
    return coordinates.value != null || // if coordinates is set, should be shown
            (language.value.isPersian && // The placeholder isn't translated to other languages
                    // The user is already dismissed the third tab
                    !preferences.getBoolean(PREF_DISABLE_OWGHAT, false) &&
                    // Try to not show the placeholder to established users
                    PREF_APP_LANGUAGE !in preferences)
}

fun bringDate(viewModel: CalendarViewModel, jdn: Jdn, context: Context, highlight: Boolean = true) {
    viewModel.changeSelectedDay(jdn)
    if (!highlight) viewModel.clearHighlightedDay()
    val today = Jdn.today()
    viewModel.changeSelectedMonthOffsetCommand(mainCalendar.getMonthsDistance(today, jdn))

    // a11y
    if (isTalkBackEnabled && jdn != today) Toast.makeText(
        context, getA11yDaySummary(
            context.resources,
            jdn,
            false,
            EventsStore.empty(),
            withZodiac = true,
            withOtherCalendars = true,
            withTitle = true
        ), Toast.LENGTH_SHORT
    ).show()
}

private typealias DetailsTab = Pair<Int, @Composable (MutableInteractionSource, Dp) -> Unit>

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.detailsTabs(
    viewModel: CalendarViewModel,
    navigateToHolidaysSettings: () -> Unit,
    navigateToSettingsLocationTab: () -> Unit,
    navigateToSettingsLocationTabSetAthanAlarm: () -> Unit,
    navigateToAstronomy: (Jdn) -> Unit,
    animatedContentScope: AnimatedContentScope,
    bottomPadding: Dp,
    today: Jdn,
): List<DetailsTab> {
    val context = LocalContext.current
    val removeThirdTab by viewModel.removedThirdTab.collectAsState()
    val hasTimesTab = enableTimesTab(context) && !removeThirdTab
    val isOnlyEventsTab =
        !hasTimesTab && enabledCalendars.size == 1 && !isAstronomicalExtraFeaturesEnabled
    return listOfNotNull(
        if (!isOnlyEventsTab) R.string.calendar to { interactionSource, minHeight ->
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
        R.string.events to { _, _ ->
            EventsTab(
                navigateToHolidaysSettings = navigateToHolidaysSettings,
                viewModel = viewModel,
                animatedContentScope = animatedContentScope,
                bottomPadding = bottomPadding,
                isOnlyEventsTab = isOnlyEventsTab,
            )
        },
        // The optional third tab
        if (hasTimesTab) R.string.times to { interactionSource, minHeight ->
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
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()
    val pagerState = rememberPagerState(
        initialPage = selectedTabIndex.coerceAtMost(tabs.size - 1),
        pageCount = tabs::size,
    )
    LaunchedEffect(key1 = pagerState.currentPage) {
        viewModel.changeSelectedTabIndex(pagerState.currentPage)
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
    scrollableTabs: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(modifier.indication(interactionSource = interactionSource, indication = ripple())) {
        val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()
        val coroutineScope = rememberCoroutineScope()
        val isOnlyEventsTab = tabs.size == 1

        if (!isOnlyEventsTab) PrimaryTabRow(
            selectedTabIndex = selectedTabIndex,
            divider = {},
            containerColor = Color.Transparent,
            indicator = {
                val offset = selectedTabIndex.coerceAtMost(tabs.size - 1)
                TabRowDefaults.PrimaryIndicator(Modifier.tabIndicatorOffset(offset))
            },
        ) {
            tabs.forEachIndexed { index, (titlesResId, _) ->
                Tab(
                    text = { Text(stringResource(titlesResId)) },
                    selected = pagerState.currentPage == index,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurface,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                )
            }
        }

        HorizontalPager(state = pagerState, verticalAlignment = Alignment.Top) { index ->
            /** See [androidx.compose.material3.SmallTabHeight] for 48.dp */
            val tabMinHeight = contentMinHeight - (if (isOnlyEventsTab) 0 else 48).dp
            Box {
                // Currently scrollable tabs only happen on landscape layout
                val scrollState = if (scrollableTabs) rememberScrollState() else null
                Box(if (scrollState != null) Modifier.verticalScroll(scrollState) else Modifier) {
                    tabs[index].second(interactionSource, tabMinHeight)
                }
                if (scrollState != null) {
                    ScrollShadow(scrollState, top = true)
                    ScrollShadow(scrollState, top = false)
                }
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

@ChecksSdkIntAtLeast(Build.VERSION_CODES.M)
@Composable
private fun showEncourageToExemptFromBatteryOptimizations(): Boolean {
    val isNotifyDate by isNotifyDate.collectAsState()
    val context = LocalContext.current
    val isAnyAthanSet = getEnabledAlarms(context).isNotEmpty()
    if (!isNotifyDate && !isAnyAthanSet && !hasAnyWidgetUpdateRecently()) return false
    if (context.preferences.getInt(PREF_BATTERY_OPTIMIZATION_IGNORED_COUNT, 0) >= 2) return false
    val alarmManager = context.getSystemService<AlarmManager>()
    if (isAnyAthanSet && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && runCatching { alarmManager?.canScheduleExactAlarms() }.getOrNull().debugAssertNotNull == false) return true
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isIgnoringBatteryOptimizations(context)
}

@RequiresApi(Build.VERSION_CODES.M)
private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
    return runCatching {
        context.getSystemService<PowerManager>()?.isIgnoringBatteryOptimizations(
            context.applicationContext.packageName
        )
    }.onFailure(logException).getOrNull() == true
}

private val toolbarHeight = 56.dp

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun Search(viewModel: CalendarViewModel) {
    LaunchedEffect(Unit) {
        launch {
            // 2s timeout, give up if took too much time
            withTimeoutOrNull(2.seconds) { viewModel.initializeEventsRepository() }
        }
    }
    var query by rememberSaveable { mutableStateOf("") }
    viewModel.searchEvent(query)
    val events by viewModel.eventsFlow.collectAsState()
    val expanded = query.isNotEmpty()
    val padding by animateDpAsState(if (expanded) 0.dp else 32.dp, label = "padding")
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                modifier = Modifier.height(toolbarHeight),
                query = query,
                onQueryChange = { query = it },
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
        onExpandedChange = { if (!it) query = "" },
        modifier = Modifier
            .padding(horizontal = padding)
            .focusRequester(focusRequester),
    ) {
        if (padding.value != 0f) return@SearchBar
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            val context = LocalContext.current
            events.take(10).forEach { event ->
                Box(
                    Modifier
                        .clickable {
                            viewModel.closeSearch()
                            bringEvent(viewModel, event, context)
                        }
                        .fillMaxWidth()
                        .padding(vertical = 20.dp, horizontal = 24.dp),
                ) {
                    AnimatedContent(
                        targetState = event.formattedTitle,
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

private fun bringEvent(viewModel: CalendarViewModel, event: CalendarEvent<*>, context: Context) {
    val date = event.date
    val type = date.calendar
    val today = Jdn.today() on type
    bringDate(
        viewModel,
        Jdn(
            type, if (date.year == -1) (today.year + if (date.month < today.month) 1 else 0)
            else date.year, date.month, date.dayOfMonth
        ),
        context,
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.Toolbar(
    animatedContentScope: AnimatedContentScope,
    openDrawer: () -> Unit,
    swipeUpActions: Map<SwipeUpAction, () -> Unit>,
    swipeDownActions: Map<SwipeDownAction, () -> Unit>,
    viewModel: CalendarViewModel,
    isLandscape: Boolean,
    today: Jdn,
) {
    val context = LocalContext.current

    val selectedMonthOffset by viewModel.selectedMonthOffset.collectAsState()
    val todayDate = remember(today, mainCalendar) { today on mainCalendar }
    val selectedMonth = mainCalendar.getMonthStartFromMonthsDistance(today, selectedMonthOffset)
    val isYearView by viewModel.isYearView.collectAsState()
    val yearViewOffset by viewModel.yearViewOffset.collectAsState()
    val yearViewIsInYearSelection by viewModel.yearViewIsInYearSelection.collectAsState()

    BackHandler(enabled = isYearView, onBack = viewModel::onYearViewBackPressed)

    @OptIn(ExperimentalMaterial3Api::class) TopAppBar(
        title = {
            val refreshToken by viewModel.refreshToken.collectAsState()
            // just a noop to update title and subtitle when secondary calendar is toggled
            refreshToken.run {}

            val secondaryCalendar = secondaryCalendar
            val title: String
            val subtitle: String
            if (isYearView) {
                title = stringResource(
                    if (yearViewIsInYearSelection) R.string.select_year else R.string.year_view
                )
                subtitle = if (yearViewOffset == 0 || yearViewIsInYearSelection) "" else {
                    yearViewYearFormat(todayDate.year + yearViewOffset, secondaryCalendar)
                }
            } else if (secondaryCalendar == null) {
                title = selectedMonth.monthName
                subtitle = formatNumber(selectedMonth.year)
            } else {
                val language by language.collectAsState()
                title = language.my.format(
                    selectedMonth.monthName, formatNumber(selectedMonth.year)
                )
                subtitle = monthFormatForSecondaryCalendar(selectedMonth, secondaryCalendar)
            }
            Column(
                Modifier.clickable(
                    indication = ripple(bounded = false),
                    interactionSource = null,
                    onClickLabel = stringResource(
                        if (isYearView && !yearViewIsInYearSelection) R.string.select_year
                        else R.string.year_view
                    ),
                ) {
                    if (isYearView) viewModel.commandYearView(YearViewCommand.ToggleYearSelection)
                    else viewModel.openYearView()
                },
            ) {
                Crossfade(title, label = "title") { state ->
                    val fraction by animateFloatAsState(
                        targetValue = if (isYearView && subtitle.isNotEmpty()) 1f else 0f,
                        label = "font size"
                    )
                    Text(
                        state,
                        style = lerp(
                            MaterialTheme.typography.titleLarge,
                            MaterialTheme.typography.titleMedium,
                            fraction,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                this.AnimatedVisibility(visible = subtitle.isNotEmpty()) {
                    Crossfade(subtitle, label = "subtitle") { state ->
                        val fraction by animateFloatAsState(
                            targetValue = if (isYearView) 1f else 0f, label = "font size"
                        )
                        Text(
                            state,
                            style = lerp(
                                MaterialTheme.typography.titleMedium,
                                MaterialTheme.typography.titleLarge,
                                fraction,
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
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
                ) else NavigationOpenDrawerIcon(animatedContentScope, openDrawer)
            }
        },
        actions = {
            this.AnimatedVisibility(isYearView) {
                TodayActionButton(yearViewOffset != 0 && !yearViewIsInYearSelection) {
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
                    bringDate(viewModel, Jdn.today(), context, highlight = false)
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
) {
    val context = LocalContext.current
    val resources = LocalResources.current

    var showDatePickerDialog by rememberSaveable { mutableStateOf(false) }
    if (showDatePickerDialog) {
        val selectedDay by viewModel.selectedDay.collectAsState()
        DatePickerDialog(selectedDay, { showDatePickerDialog = false }) { jdn ->
            bringDate(viewModel, jdn, context)
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
            ActionItem(
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
            ActionItem(
                item,
                action,
                PREF_SWIPE_DOWN_ACTION,
                item.titleId,
                preferredSwipeDownAction,
                Icons.TwoTone.SwipeDown,
            ) { (if (preferredSwipeDownAction == item) SwipeDownAction.None else item).name }
        }

        // It doesn't have any effect in talkback ui, let's disable it there to avoid the confusion
        if (isTalkBackEnabled || enabledCalendars.size == 1) return@ThreeDotsDropdownMenu

        HorizontalDivider()

        val isShowWeekOfYearEnabled by isShowWeekOfYearEnabled.collectAsState()
        AppDropdownMenuCheckableItem(
            text = stringResource(R.string.week_number),
            isChecked = isShowWeekOfYearEnabled,
            setChecked = {
                context.preferences.edit { putBoolean(PREF_SHOW_WEEK_OF_YEAR_NUMBER, it) }
                closeMenu()
            },
        )

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
                ) { _ ->
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
                CalendarContract.Events.DESCRIPTION,
                description
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
                if (language.value.isPersian) coroutineScope.launch {
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
