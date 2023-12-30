package com.byagowi.persiancalendar.ui.calendar

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.provider.CalendarContract
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.PrimaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.PREF_DISABLE_OWGHAT
import com.byagowi.persiancalendar.PREF_LAST_APP_VISIT_VERSION
import com.byagowi.persiancalendar.PREF_NOTIFY_DATE
import com.byagowi.persiancalendar.PREF_NOTIFY_IGNORED
import com.byagowi.persiancalendar.PREF_OTHER_CALENDARS_KEY
import com.byagowi.persiancalendar.PREF_SECONDARY_CALENDAR_IN_TABLE
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.calculationMethod
import com.byagowi.persiancalendar.global.cityName
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.enabledCalendars
import com.byagowi.persiancalendar.global.isIranHolidaysEnabled
import com.byagowi.persiancalendar.global.isTalkBackEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.secondaryCalendar
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.ui.calendar.calendarpager.CalendarPager
import com.byagowi.persiancalendar.ui.calendar.calendarpager.Month
import com.byagowi.persiancalendar.ui.calendar.dialogs.DayPickerDialog
import com.byagowi.persiancalendar.ui.calendar.dialogs.MonthOverviewDialog
import com.byagowi.persiancalendar.ui.calendar.searchevent.SearchEventsStore.Companion.formattedTitle
import com.byagowi.persiancalendar.ui.calendar.shiftwork.ShiftWorkDialog
import com.byagowi.persiancalendar.ui.calendar.shiftwork.ShiftWorkViewModel
import com.byagowi.persiancalendar.ui.calendar.shiftwork.fillViewModelFromGlobalVariables
import com.byagowi.persiancalendar.ui.calendar.times.TimesTab
import com.byagowi.persiancalendar.ui.common.AppDropdownMenuExpandableItem
import com.byagowi.persiancalendar.ui.common.AppDropdownMenuItem
import com.byagowi.persiancalendar.ui.common.AppDropdownMenuRadioItem
import com.byagowi.persiancalendar.ui.common.AppIconButton
import com.byagowi.persiancalendar.ui.common.AskForCalendarPermissionDialog
import com.byagowi.persiancalendar.ui.common.CalendarsOverview
import com.byagowi.persiancalendar.ui.common.NavigationOpenDrawerIcon
import com.byagowi.persiancalendar.ui.common.ShrinkingFloatingActionButton
import com.byagowi.persiancalendar.ui.common.ThreeDotsDropdownMenu
import com.byagowi.persiancalendar.ui.common.TodayActionButton
import com.byagowi.persiancalendar.ui.theme.AppTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.MaterialCornerExtraLargeNoBottomEnd
import com.byagowi.persiancalendar.ui.utils.MaterialCornerExtraLargeTop
import com.byagowi.persiancalendar.ui.utils.bringMarketPage
import com.byagowi.persiancalendar.ui.utils.isRtl
import com.byagowi.persiancalendar.ui.utils.openHtmlInBrowser
import com.byagowi.persiancalendar.utils.TWO_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import com.byagowi.persiancalendar.utils.calendarType
import com.byagowi.persiancalendar.utils.dayTitleSummary
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getA11yDaySummary
import com.byagowi.persiancalendar.utils.getFromStringId
import com.byagowi.persiancalendar.utils.getTimeNames
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.monthFormatForSecondaryCalendar
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.utils.supportedYearOfIranCalendar
import com.byagowi.persiancalendar.utils.titleStringId
import com.byagowi.persiancalendar.utils.update
import io.github.persiancalendar.calendar.AbstractDate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.meta
import kotlinx.html.script
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.tfoot
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import kotlinx.html.unsafe

@Composable
fun CalendarScreen(
    openDrawer: () -> Unit,
    navigateToHolidaysSettings: () -> Unit,
    navigateToSettingsLocationTab: () -> Unit,
    navigateToAstronomy: (Int) -> Unit,
    viewModel: CalendarViewModel,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            val searchBoxIsOpen by viewModel.isSearchOpen.collectAsState()
            BackHandler(enabled = searchBoxIsOpen) { viewModel.closeSearch() }

            val animationTime = integerResource(android.R.integer.config_mediumAnimTime)
            AnimatedContent(
                searchBoxIsOpen,
                label = "toolbar",
                transitionSpec = {
                    fadeIn(tween(animationTime)).togetherWith(fadeOut(tween(animationTime)))
                },
            ) { if (it) Search(viewModel) else Toolbar(openDrawer, viewModel) }
        },
        floatingActionButton = {
            val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()
            val addEvent = AddEvent(viewModel)
            ShrinkingFloatingActionButton(
                modifier = Modifier.padding(end = 8.dp),
                isVisible = selectedTabIndex == EVENTS_TAB,
                action = addEvent,
                icon = Icons.Default.Add,
                title = stringResource(R.string.add_event),
            )
        },
    ) { paddingValues ->
        val context = LocalContext.current
        // Refresh the calendar on resume
        LaunchedEffect(Unit) {
            viewModel.refreshCalendar()
            context.appPrefs.edit { putInt(PREF_LAST_APP_VISIT_VERSION, BuildConfig.VERSION_CODE) }
        }
        val isLandscape =
            LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
        val bottomPadding = paddingValues.calculateBottomPadding()
        BoxWithConstraints(Modifier.padding(top = paddingValues.calculateTopPadding())) {
            val maxHeight = maxHeight
            val maxWidth = maxWidth
            val isYearView by viewModel.isYearView.collectAsState()
            BackHandler(enabled = isYearView) { viewModel.closeYearView() }
            if (isYearView) {
                YearView(viewModel, maxWidth, maxHeight - bottomPadding)
            } else if (isLandscape) Row {
                val width = (maxWidth * 45 / 100).coerceAtMost(400.dp)
                val height = 400.dp.coerceAtMost(maxHeight)
                Box(Modifier.width(width)) { CalendarPager(viewModel, width, height) }
                Surface(
                    shape = MaterialCornerExtraLargeNoBottomEnd(),
                    modifier = Modifier.fillMaxHeight(),
                ) {
                    Details(
                        viewModel,
                        navigateToHolidaysSettings,
                        navigateToSettingsLocationTab,
                        navigateToAstronomy,
                        bottomPadding,
                        maxHeight,
                        scrollableTabs = true,
                    )
                }
            } else {
                val scrollState = rememberScrollState()
                Column(modifier = Modifier.verticalScroll(scrollState)) {
                    val calendarHeight = (maxHeight / 2f).coerceIn(280.dp, 440.dp)
                    Box(Modifier.offset { IntOffset(0, scrollState.value * 3 / 4) }) {
                        CalendarPager(viewModel, maxWidth, calendarHeight - 4.dp)
                    }
                    Spacer(Modifier.height(4.dp))
                    val detailsMinHeight = maxHeight - calendarHeight
                    Surface(
                        modifier = Modifier.defaultMinSize(minHeight = detailsMinHeight),
                        shape = MaterialCornerExtraLargeTop(),
                    ) {
                        Details(
                            viewModel,
                            navigateToHolidaysSettings,
                            navigateToSettingsLocationTab,
                            navigateToAstronomy,
                            bottomPadding,
                            detailsMinHeight,
                        )
                    }
                }
            }
        }
    }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        if (mainCalendar == CalendarType.SHAMSI && isIranHolidaysEnabled && Jdn.today()
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

@Composable
private fun YearView(viewModel: CalendarViewModel, maxWidth: Dp, maxHeight: Dp) {
    Column(verticalArrangement = Arrangement.SpaceAround, modifier = Modifier.height(maxHeight)) {
        val today by viewModel.today.collectAsState()
        val date = today.toCalendar(mainCalendar)
        val monthNames = mainCalendar.monthsNames
        val yearOffsetInMonths = run {
            val selectedMonthOffset = viewModel.selectedMonthOffset.value
            val selectedMonth =
                mainCalendar.getMonthStartFromMonthsDistance(Jdn.today(), selectedMonthOffset)
            (selectedMonth.year - date.year) * 12
        }
        val isLandscape =
            LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

        val width = maxWidth / if (isLandscape) 4 else 3
        val height = maxHeight / if (isLandscape) 3 else 4

        val titleHeight = height / 10
        val titleHeightSp = with(LocalDensity.current) { titleHeight.toSp() / 1.6f }
        val padding = 4.dp

        repeat(if (isLandscape) 3 else 4) { row ->
            Row {
                repeat(if (isLandscape) 4 else 3) { column ->
                    val month = 1 + column + row * if (isLandscape) 4 else 3
                    val offset = yearOffsetInMonths + month - date.month
                    Column(
                        Modifier
                            .height(height - padding * 2)
                            .padding(start = padding, end = padding)
                            .clip(MaterialTheme.shapes.large)
                            .clickable { viewModel.closeYearView() }
                            .background(LocalContentColor.current.copy(alpha = .1f)),
                    ) {
                        Text(
                            monthNames[month - 1],
                            Modifier
                                .width(width - padding * 2)
                                .height(titleHeight),
                            fontSize = titleHeightSp,
                            textAlign = TextAlign.Center
                        )
                        Month(
                            viewModel = viewModel,
                            offset = offset,
                            isCurrentSelection = false,
                            width = width - padding * 2,
                            height = height - titleHeight - padding * 2,
                        )
                    }
                }
            }
        }
    }
}

const val CALENDARS_TAB = 0
const val EVENTS_TAB = 1
const val TIMES_TAB = 2

private fun enableTimesTab(context: Context): Boolean {
    val appPrefs = context.appPrefs
    return coordinates.value != null || // if coordinates is set, should be shown
            (language.value.isPersian && // The placeholder isn't translated to other languages
                    // The user is already dismissed the third tab
                    !appPrefs.getBoolean(PREF_DISABLE_OWGHAT, false) &&
                    // Try to not show the placeholder to established users
                    PREF_APP_LANGUAGE !in appPrefs)
}

private fun bringDate(
    viewModel: CalendarViewModel,
    jdn: Jdn,
    context: Context,
    highlight: Boolean = true,
) {
    viewModel.changeSelectedDay(jdn)
    if (!highlight) viewModel.clearHighlightedDay()
    viewModel.changeSelectedMonthOffsetCommand(mainCalendar.getMonthsDistance(Jdn.today(), jdn))

    // a11y
    if (isTalkBackEnabled && jdn != Jdn.today()) Toast.makeText(
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

@Composable
fun ButtonsBar(
    modifier: Modifier = Modifier,
    @StringRes header: Int,
    @StringRes acceptButton: Int = R.string.settings,
    discardAction: () -> Unit = {},
    acceptAction: () -> Unit,
) {
    var shown by remember { mutableStateOf(true) }
    AnimatedVisibility(modifier = modifier, visible = shown) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                stringResource(header),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                OutlinedButton(
                    onClick = {
                        discardAction()
                        shown = false
                    },
                    Modifier.weight(1f),
                ) { Text(stringResource(R.string.ignore)) }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        shown = false
                        acceptAction()
                    },
                    Modifier.weight(1f),
                ) { Text(stringResource(acceptButton)) }
            }
        }
    }
}

@Composable
fun Details(
    viewModel: CalendarViewModel,
    navigateToHolidaysSettings: () -> Unit,
    navigateToSettingsLocationTab: () -> Unit,
    navigateToAstronomy: (Int) -> Unit,
    bottomPadding: Dp,
    contentMinHeight: Dp,
    scrollableTabs: Boolean = false
) {
    val context = LocalContext.current
    val removeThirdTab by viewModel.removedThirdTab.collectAsState()
    val tabs = listOfNotNull<Pair<Int, @Composable () -> Unit>>(
        R.string.calendar to { CalendarsTab(viewModel) },
        R.string.events to { EventsTab(navigateToHolidaysSettings, viewModel) },
        // The optional third tab
        if (enableTimesTab(context) && !removeThirdTab) R.string.times to {
            TimesTab(navigateToSettingsLocationTab, navigateToAstronomy, viewModel)
        } else null,
    )

    val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()
    @OptIn(ExperimentalFoundationApi::class) Column(Modifier.fillMaxHeight()) {
        val pagerState = rememberPagerState(
            initialPage = selectedTabIndex.coerceAtMost(tabs.size - 1),
            pageCount = tabs::size,
        )
        val scope = rememberCoroutineScope()
        viewModel.changeSelectedTabIndex(pagerState.currentPage)

        TabRow(
            selectedTabIndex = selectedTabIndex,
            divider = {},
            indicator = @Composable { tabPositions ->
                if (selectedTabIndex < tabPositions.size) {
                    PrimaryIndicator(Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]))
                }
            },
        ) {
            tabs.forEachIndexed { index, (titlesResId, _) ->
                Tab(
                    text = { Text(stringResource(titlesResId)) },
                    selected = pagerState.currentPage == index,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurface,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            verticalAlignment = Alignment.Top,
        ) { index ->
            Column(
                Modifier
                    .defaultMinSize(minHeight = contentMinHeight * 3 / 4)
                    .then(
                        if (scrollableTabs) Modifier.verticalScroll(rememberScrollState())
                        else Modifier
                    )
            ) {
                tabs[index].second()
                Spacer(Modifier.height(bottomPadding))
            }
        }
    }
}

@Composable
private fun CalendarsTab(viewModel: CalendarViewModel) {
    Column {
        val jdn by viewModel.selectedDay.collectAsState()
        var isExpanded by remember { mutableStateOf(false) }
        CalendarsOverview(jdn, mainCalendar, enabledCalendars, isExpanded) {
            isExpanded = !isExpanded
        }

        val context = LocalContext.current
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED && PREF_NOTIFY_IGNORED !in context.appPrefs
        ) {
            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                context.appPrefs.edit { putBoolean(PREF_NOTIFY_DATE, isGranted) }
                updateStoredPreference(context)
                if (isGranted) update(context, updateDate = true)
            }
            ButtonsBar(
                header = R.string.enable_notification,
                acceptButton = R.string.yes,
                discardAction = {
                    context.appPrefs.edit { putBoolean(PREF_NOTIFY_IGNORED, true) }
                },
            ) { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun Search(viewModel: CalendarViewModel) {
    LaunchedEffect(Unit) {
        launch {
            // 2s timeout, give up if took too much time
            withTimeoutOrNull(TWO_SECONDS_IN_MILLIS) { viewModel.initializeEventsRepository() }
        }
    }
    var query by rememberSaveable { mutableStateOf("") }
    viewModel.searchEvent(query)
    val events by viewModel.eventsFlow.collectAsState(initial = emptyList())
    val isActive = query.isNotEmpty()
    val padding by animateDpAsState(if (isActive) 0.dp else 32.dp, label = "padding")
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    SearchBar(
        query = query,
        placeholder = { Text(stringResource(R.string.search_in_events)) },
        onQueryChange = { query = it },
        onSearch = {},
        active = isActive,
        onActiveChange = {},
        trailingIcon = {
            AppIconButton(icon = Icons.Default.Close, title = stringResource(R.string.close)) {
                viewModel.closeSearch()
            }
        },
        modifier = Modifier
            .padding(horizontal = padding)
            .focusRequester(focusRequester),
    ) {
        if (padding.value != 0f) return@SearchBar
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
                Text(
                    event.formattedTitle,
                    modifier = Modifier.align(Alignment.CenterStart),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        if (events.size > 10) Text("…", Modifier.padding(vertical = 12.dp, horizontal = 24.dp))
    }
}

private fun bringEvent(viewModel: CalendarViewModel, event: CalendarEvent<*>, context: Context) {
    val date = event.date
    val type = date.calendarType
    val today = Jdn.today().toCalendar(type)
    bringDate(
        viewModel,
        Jdn(
            type, if (date.year == -1) (today.year + if (date.month < today.month) 1 else 0)
            else date.year, date.month, date.dayOfMonth
        ),
        context,
    )
}

@Composable
private fun Toolbar(openDrawer: () -> Unit, viewModel: CalendarViewModel) {
    val context = LocalContext.current

    val selectedMonthOffset by viewModel.selectedMonthOffset.collectAsState()
    val today by viewModel.today.collectAsState()
    val selectedMonth = mainCalendar.getMonthStartFromMonthsDistance(today, selectedMonthOffset)
    val isYearView by viewModel.isYearView.collectAsState()

    @OptIn(ExperimentalMaterial3Api::class) TopAppBar(
        title = {
            val refreshToken by viewModel.refreshToken.collectAsState()
            // just a noop to update title and subtitle when secondary calendar is toggled
            refreshToken.run {}

            val secondaryCalendar = secondaryCalendar
            val title: String
            val subtitle: String
            if (isYearView) {
                title = stringResource(R.string.year_view)
                subtitle = formatNumber(selectedMonth.year)
            } else if (secondaryCalendar == null) {
                title = selectedMonth.monthName
                subtitle = formatNumber(selectedMonth.year)
            } else {
                title = language.value.my.format(
                    selectedMonth.monthName, formatNumber(selectedMonth.year)
                )
                subtitle = monthFormatForSecondaryCalendar(selectedMonth, secondaryCalendar)
            }
            val animationTime = integerResource(android.R.integer.config_mediumAnimTime)
            Column {
                AnimatedContent(
                    title,
                    label = "title",
                    transitionSpec = {
                        fadeIn(tween(animationTime)).togetherWith(fadeOut(tween(animationTime)))
                    },
                ) { state ->
                    Text(
                        state,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                AnimatedContent(
                    subtitle,
                    label = "subtitle",
                    transitionSpec = {
                        fadeIn(tween(animationTime)).togetherWith(fadeOut(tween(animationTime)))
                    },
                ) { state ->
                    Text(
                        state,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
        colors = AppTopAppBarColors(),
        navigationIcon = { NavigationOpenDrawerIcon(openDrawer) },
        actions = {
            AnimatedVisibility(isYearView) {
                AppIconButton(icon = Icons.Default.Close, title = stringResource(R.string.close)) {
                    viewModel.closeYearView()
                }
            }

            TodayActionButton(viewModel.todayButtonVisibility.collectAsState().value) {
                bringDate(viewModel, Jdn.today(), context, highlight = false)
            }

            AnimatedVisibility(!isYearView) {
                AppIconButton(
                    icon = Icons.Default.Search,
                    title = stringResource(R.string.search_in_events),
                ) { viewModel.openSearch() }
            }

            AnimatedVisibility(!isYearView) { Menu(viewModel) }
        },
    )
}

@Composable
private fun Menu(viewModel: CalendarViewModel) {
    val context = LocalContext.current
    val addEvent = AddEvent(viewModel)

    var showDayPickerDialog by rememberSaveable { mutableStateOf(false) }
    if (showDayPickerDialog) DayPickerDialog(
        viewModel.selectedDay.value,
        R.string.go,
        { bringDate(viewModel, it, context) }) { showDayPickerDialog = false }

    val shiftWorkViewModel by viewModel.shiftWorkViewModel.collectAsState()
    shiftWorkViewModel?.let {
        ShiftWorkDialog(
            it,
            viewModel.selectedDay.value,
            onDismissRequest = { viewModel.setShiftWorkViewModel(null) },
        ) { viewModel.refreshCalendar() }
    }

    var showMonthOverview by remember { mutableStateOf(false) }
    if (showMonthOverview) {
        val selectedMonthOffset = viewModel.selectedMonthOffset.value
        val selectedMonth =
            mainCalendar.getMonthStartFromMonthsDistance(Jdn.today(), selectedMonthOffset)
        MonthOverviewDialog(selectedMonth) { showMonthOverview = false }
    }

    ThreeDotsDropdownMenu { closeMenu ->
        AppDropdownMenuItem(
            text = { Text(stringResource(R.string.goto_date)) },
            onClick = {
                closeMenu()
                showDayPickerDialog = true
            },
        )

        AppDropdownMenuItem(
            text = { Text(stringResource(R.string.add_event)) },
            onClick = {
                closeMenu()
                addEvent()
            },
        )

        AppDropdownMenuItem(
            text = { Text(stringResource(R.string.shift_work_settings)) },
            onClick = {
                closeMenu()
                val dialogViewModel = ShiftWorkViewModel()
                // from already initialized global variable till a better solution
                fillViewModelFromGlobalVariables(dialogViewModel, viewModel.selectedDay.value)
                viewModel.setShiftWorkViewModel(dialogViewModel)
            },
        )

        AppDropdownMenuItem(
            text = { Text(stringResource(R.string.month_overview)) },
            onClick = {
                closeMenu()
                showMonthOverview = true
            },
        )

        val coordinates by coordinates.collectAsState()
        if (coordinates != null) AppDropdownMenuItem(
            text = { Text(stringResource(R.string.month_pray_times)) },
            onClick = {
                closeMenu()
                val selectedMonthOffset = viewModel.selectedMonthOffset.value
                val selectedMonth =
                    mainCalendar.getMonthStartFromMonthsDistance(Jdn.today(), selectedMonthOffset)
                context.openHtmlInBrowser(createOwghatHtmlReport(context.resources, selectedMonth))
            },
        )

        if (!isTalkBackEnabled) AppDropdownMenuItem(
            text = { Text(stringResource(R.string.year_view)) },
            onClick = {
                closeMenu()
                viewModel.openYearView()
            },
        )

        // It doesn't have any effect in talkback ui, let's disable it there to avoid the confusion
        if (isTalkBackEnabled && enabledCalendars.size == 1) return@ThreeDotsDropdownMenu

        var showSecondaryCalendarSubMenu by remember { mutableStateOf(false) }
        AppDropdownMenuExpandableItem(
            text = stringResource(R.string.show_secondary_calendar),
            isExpanded = showSecondaryCalendarSubMenu,
            onClick = { showSecondaryCalendarSubMenu = !showSecondaryCalendarSubMenu },
        )

        (listOf(null) + enabledCalendars.drop(1)).forEach {
            AnimatedVisibility(showSecondaryCalendarSubMenu) {
                AppDropdownMenuRadioItem(
                    stringResource(it?.title ?: R.string.none), it == secondaryCalendar
                ) { _ ->
                    context.appPrefs.edit {
                        if (it == null) remove(PREF_SECONDARY_CALENDAR_IN_TABLE)
                        else {
                            putBoolean(PREF_SECONDARY_CALENDAR_IN_TABLE, true)
                            putString(
                                PREF_OTHER_CALENDARS_KEY,
                                // Put the chosen calendars at the first of calendars priorities
                                (listOf(it) + (enabledCalendars.drop(1) - it)).joinToString(",")
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

private fun createOwghatHtmlReport(resources: Resources, date: AbstractDate): String {
    return createHTML().html {
        val coordinates = coordinates.value ?: return@html
        attributes["lang"] = language.value.language
        attributes["dir"] = if (resources.isRtl) "rtl" else "ltr"
        head {
            meta(charset = "utf8")
            style {
                unsafe {
                    +"""
                        body { font-family: system-ui }
                        th, td { padding: 0 .5em; text-align: center }
                        td { border-top: 1px solid lightgray; font-size: 95% }
                        h1 { text-align: center; font-size: 110% }
                        table { margin: 0 auto; }
                    """.trimIndent()
                }
            }
        }
        body {
            h1 {
                +listOfNotNull(
                    cityName.value,
                    language.value.my.format(date.monthName, formatNumber(date.year))
                ).joinToString(spacedComma)
            }
            table {
                thead {
                    tr {
                        th { +resources.getString(R.string.day) }
                        getTimeNames().forEach { th { +resources.getString(it) } }
                    }
                }
                tbody {
                    (0..<mainCalendar.getMonthLength(date.year, date.month)).forEach { day ->
                        tr {
                            val prayTimes = coordinates.calculatePrayTimes(
                                Jdn(
                                    mainCalendar.createDate(date.year, date.month, day)
                                ).toGregorianCalendar()
                            )
                            th { +formatNumber(day + 1) }
                            getTimeNames().forEach {
                                td { +prayTimes.getFromStringId(it).toBasicFormatString() }
                            }
                        }
                    }
                }
                if (calculationMethod != language.value.preferredCalculationMethod) {
                    tfoot {
                        tr {
                            td {
                                colSpan = "10"
                                +resources.getString(calculationMethod.titleStringId)
                            }
                        }
                    }
                }
            }
            script { unsafe { +"print()" } }
        }
    }
}

class AddEventContract : ActivityResultContract<Jdn, Void?>() {
    override fun parseResult(resultCode: Int, intent: Intent?): Void? = null
    override fun createIntent(context: Context, input: Jdn): Intent {
        val time = input.toGregorianCalendar().timeInMillis
        return Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI).putExtra(
            CalendarContract.Events.DESCRIPTION, dayTitleSummary(
                input, input.toCalendar(mainCalendar)
            )
        ).putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, time)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, time)
            .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
    }
}

@Composable
fun AddEvent(viewModel: CalendarViewModel): () -> Unit {
    val addEvent = rememberLauncherForActivityResult(AddEventContract()) {
        viewModel.refreshCalendar()
    }

    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) AskForCalendarPermissionDialog { isGranted ->
        viewModel.refreshCalendar()
        showDialog = false
        if (isGranted) runCatching {
            addEvent.launch(viewModel.selectedDay.value)
        }.onFailure(logException).onFailure {
            Toast.makeText(context, R.string.device_does_not_support, Toast.LENGTH_SHORT).show()
        }
    }

    return { showDialog = true }
}
