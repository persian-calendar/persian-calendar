package com.byagowi.persiancalendar.ui.calendar

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.provider.CalendarContract
import android.widget.Toast
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.calculationMethod
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.enabledCalendars
import com.byagowi.persiancalendar.global.isTalkBackEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.secondaryCalendar
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.ui.calendar.calendarpager.CalendarPager
import com.byagowi.persiancalendar.ui.calendar.dialogs.DayPickerDialog
import com.byagowi.persiancalendar.ui.calendar.searchevent.SearchEventsStore.Companion.formattedTitle
import com.byagowi.persiancalendar.ui.calendar.shiftwork.ShiftWorkDialog
import com.byagowi.persiancalendar.ui.calendar.times.TimesTab
import com.byagowi.persiancalendar.ui.common.CalendarsOverview
import com.byagowi.persiancalendar.ui.common.ShrinkingFloatingActionButton
import com.byagowi.persiancalendar.ui.utils.AskForCalendarPermissionDialog
import com.byagowi.persiancalendar.ui.utils.ExtraLargeShapeCornerSize
import com.byagowi.persiancalendar.ui.utils.MaterialCornerExtraLargeNoBottomEnd
import com.byagowi.persiancalendar.ui.utils.MaterialCornerExtraLargeTop
import com.byagowi.persiancalendar.ui.utils.isRtl
import com.byagowi.persiancalendar.ui.utils.openHtmlInBrowser
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.utils.TWO_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import com.byagowi.persiancalendar.utils.calendarType
import com.byagowi.persiancalendar.utils.cityName
import com.byagowi.persiancalendar.utils.dayTitleSummary
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getA11yDaySummary
import com.byagowi.persiancalendar.utils.getFromStringId
import com.byagowi.persiancalendar.utils.getTimeNames
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.monthFormatForSecondaryCalendar
import com.byagowi.persiancalendar.utils.monthName
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
    val context = LocalContext.current
    // Refresh the calendar on resume
    LaunchedEffect(null) {
        viewModel.refreshCalendar()
        context.appPrefs.edit { putInt(PREF_LAST_APP_VISIT_VERSION, BuildConfig.VERSION_CODE) }
    }

    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    Column {
        val searchBoxIsOpen by viewModel.isSearchOpen.collectAsState()
        val animationTime = integerResource(android.R.integer.config_mediumAnimTime)
        AnimatedContent(
            searchBoxIsOpen,
            label = "toolbar",
            transitionSpec = {
                fadeIn(animationSpec = tween(animationTime)).togetherWith(
                    fadeOut(animationSpec = tween(animationTime))
                )
            },
        ) { if (it) Search(viewModel) else Toolbar(openDrawer, viewModel) }

        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        val screenHeight = configuration.screenHeightDp.dp
        if (isLandscape) Row {
            CalendarPager(Modifier.width(screenWidth * 45 / 100), viewModel)
            Details(
                Modifier.fillMaxSize(),
                viewModel,
                navigateToHolidaysSettings,
                navigateToSettingsLocationTab,
                navigateToAstronomy,
                scrollableTabs = true,
            )
        } else Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            CalendarPager(Modifier.sizeIn(minHeight = 320.dp, maxHeight = 400.dp), viewModel)
            Details(
                Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = screenHeight - 400.dp - 64.dp),
                viewModel,
                navigateToHolidaysSettings,
                navigateToSettingsLocationTab,
                navigateToAstronomy,
            )
        }
    }
}

//const val CALENDARS_TAB = 0
const val EVENTS_TAB = 1
const val TIMES_TAB = 2

private fun enableTimesTab(context: Context): Boolean {
    val appPrefs = context.appPrefs
    return coordinates.value != null || // if coordinates is set, should be shown
            (language.isPersian && // The placeholder isn't translated to other languages
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
            context,
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
    modifier: Modifier,
    viewModel: CalendarViewModel,
    navigateToHolidaysSettings: () -> Unit,
    navigateToSettingsLocationTab: () -> Unit,
    navigateToAstronomy: (Int) -> Unit,
    scrollableTabs: Boolean = false
) {
    val context = LocalContext.current
    val removeThirdTab by viewModel.removedThirdTab.collectAsState()
    val tabs = listOfNotNull<Pair<Int, @Composable () -> Unit>>(
        R.string.calendar to { CalendarsTab(viewModel) },
        R.string.events to { EventsTab(navigateToHolidaysSettings, viewModel) },
        // The optional third tab
        if (enableTimesTab(context) && !removeThirdTab) R.string.owghat to {
            TimesTab(
                navigateToSettingsLocationTab,
                navigateToAstronomy,
                viewModel,
            )
        } else null,
    )

    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()
    Surface(
        shape = if (isLandscape) MaterialCornerExtraLargeNoBottomEnd() else MaterialCornerExtraLargeTop(),
    ) {
        @OptIn(ExperimentalFoundationApi::class) Column {
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
                        SecondaryIndicator(
                            Modifier
                                .tabIndicatorOffset(tabPositions[selectedTabIndex])
                                .padding(horizontal = ExtraLargeShapeCornerSize.dp),
                            height = 2.dp,
                        )
                    }
                },
            ) {
                tabs.forEachIndexed { index, (titlesResId, _) ->
                    Tab(
                        text = { Text(stringResource(titlesResId)) },
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    )
                }
            }

            Box {
                HorizontalPager(
                    state = pagerState,
                    modifier = modifier,
                    verticalAlignment = Alignment.Top,
                ) { index ->
                    Surface(modifier = Modifier.fillMaxSize()) {
                        Column(
                            if (scrollableTabs) Modifier.verticalScroll(rememberScrollState())
                            else Modifier
                        ) {
                            tabs[index].second()
                            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
                        }
                    }
                }
                val addEvent = AddEvent(viewModel)
                ShrinkingFloatingActionButton(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp)
                        .safeDrawingPadding(),
                    isVisible = selectedTabIndex == EVENTS_TAB,
                    action = addEvent,
                    icon = Icons.Default.Add,
                    title = stringResource(R.string.add_event),
                )
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
                acceptButton = R.string.notify_date,
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
    LaunchedEffect(null) {
        launch {
            // 2s timeout, give up if took too much time
            withTimeoutOrNull(TWO_SECONDS_IN_MILLIS) { viewModel.initializeEventsRepository() }
        }
    }
    var query by remember { mutableStateOf("") }
    viewModel.searchEvent(query)
    val events by viewModel.eventsFlow.collectAsState(initial = emptyList())
    val isActive by derivedStateOf { query.isNotEmpty() }
    val padding by animateDpAsState(if (isActive) 0.dp else 32.dp, label = "padding")
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(null) { focusRequester.requestFocus() }
    SearchBar(
        query = query,
        placeholder = { Text(stringResource(R.string.search_in_events)) },
        onQueryChange = { query = it },
        onSearch = {},
        active = isActive,
        onActiveChange = {},
        trailingIcon = {
            IconButton(onClick = { viewModel.closeSearch() }) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.close)
                )
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
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        if (events.size > 10)
            Text("…", Modifier.padding(vertical = 12.dp, horizontal = 24.dp))
    }
}

private fun bringEvent(viewModel: CalendarViewModel, event: CalendarEvent<*>, context: Context) {
    val date = event.date
    val type = date.calendarType
    val today = Jdn.today().toCalendar(type)
    bringDate(
        viewModel,
        Jdn(
            type,
            if (date.year == -1) (today.year + if (date.month < today.month) 1 else 0)
            else date.year,
            date.month,
            date.dayOfMonth
        ),
        context,
    )
}

@Composable
private fun Toolbar(openDrawer: () -> Unit, viewModel: CalendarViewModel) {
    val context = LocalContext.current
    // TODO: Ideally this should be onPrimary
    val colorOnAppBar = Color(context.resolveColor(R.attr.colorOnAppBar))

    val selectedDay by viewModel.selectedDay.collectAsState()
    val selectedMonthOffset by viewModel.selectedMonthOffset.collectAsState()
    val todayJdn = Jdn.today()
    val todayDate = todayJdn.toCalendar(mainCalendar)
    val selectedMonth =
        mainCalendar.getMonthStartFromMonthsDistance(todayJdn, selectedMonthOffset)

    @OptIn(ExperimentalMaterial3Api::class) TopAppBar(
        title = {
            val secondaryCalendar = secondaryCalendar
            val title: String
            val subtitle: String
            if (secondaryCalendar == null) {
                title = selectedMonth.monthName
                subtitle = formatNumber(selectedMonth.year)
            } else {
                title = language.my.format(
                    selectedMonth.monthName,
                    formatNumber(selectedMonth.year)
                )
                subtitle = monthFormatForSecondaryCalendar(selectedMonth, secondaryCalendar)
            }
            val animationTime = integerResource(android.R.integer.config_mediumAnimTime)
            Column {
                AnimatedContent(
                    title,
                    label = "title",
                    transitionSpec = {
                        fadeIn(animationSpec = tween(animationTime)).togetherWith(
                            fadeOut(animationSpec = tween(animationTime))
                        )
                    },
                ) { state -> Text(state, style = MaterialTheme.typography.titleLarge) }
                AnimatedContent(
                    subtitle,
                    label = "subtitle",
                    transitionSpec = {
                        fadeIn(animationSpec = tween(animationTime)).togetherWith(
                            fadeOut(animationSpec = tween(animationTime))
                        )
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
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            navigationIconContentColor = colorOnAppBar,
            actionIconContentColor = colorOnAppBar,
            titleContentColor = colorOnAppBar,
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
            AnimatedVisibility(
                selectedMonth.year != todayDate.year || selectedMonth.month != todayDate.month ||
                        selectedDay != todayJdn
            ) {
                IconButton(onClick = {
                    bringDate(viewModel, Jdn.today(), context, highlight = false)
                }) {
                    Icon(
                        ImageVector.vectorResource(R.drawable.ic_restore_modified),
                        contentDescription = stringResource(R.string.return_to_today),
                    )
                }
            }

            TooltipBox(
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                tooltip = {
                    PlainTooltip { Text(text = stringResource(R.string.search_in_events)) }
                },
                state = rememberTooltipState()
            ) {
                IconButton(onClick = { viewModel.openSearch() }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.search_in_events)
                    )
                }
            }

            val addEvent = AddEvent(viewModel)

            Box {
                var showMenu by rememberSaveable { mutableStateOf(false) }
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip { Text(text = stringResource(R.string.more_options)) }
                    },
                    state = rememberTooltipState()
                ) {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.more_options),
                        )
                    }
                }
                Menu(viewModel, showMenu, addEvent) { showMenu = false }
            }
        },
    )
}

@Composable
private fun Menu(
    viewModel: CalendarViewModel,
    showMenu: Boolean,
    addEvent: () -> Unit,
    closeMenu: () -> Unit
) {
    var showDayPickerDialog by remember { mutableStateOf(false) }
    var showShiftWorkDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    DropdownMenu(expanded = showMenu, onDismissRequest = closeMenu) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.goto_date)) },
            onClick = {
                closeMenu()
                showDayPickerDialog = true
            },
        )

        DropdownMenuItem(
            text = { Text(stringResource(R.string.add_event)) },
            onClick = {
                closeMenu()
                addEvent()
            },
        )

        DropdownMenuItem(
            text = { Text(stringResource(R.string.shift_work_settings)) },
            onClick = {
                closeMenu()
                showShiftWorkDialog = true
            },
        )

        DropdownMenuItem(
            text = { Text(stringResource(R.string.month_overview)) },
            onClick = {
                closeMenu()
//                val selectedMonthOffset = viewModel.selectedMonthOffset.value
//                val selectedMonth =
//                    mainCalendar.getMonthStartFromMonthsDistance(Jdn.today(), selectedMonthOffset)
//                activity?.let { showMonthOverview(it, viewModel.selectedMonth.value) }
            },
        )

        val coordinates by coordinates.collectAsState()
        if (coordinates != null) DropdownMenuItem(
            text = { Text(stringResource(R.string.month_pray_times)) },
            onClick = {
                closeMenu()
                val selectedMonthOffset = viewModel.selectedMonthOffset.value
                val selectedMonth =
                    mainCalendar.getMonthStartFromMonthsDistance(Jdn.today(), selectedMonthOffset)
                context.openHtmlInBrowser(createOwghatHtmlReport(context, selectedMonth))
            },
        )

        // It doesn't have any effect in talkback ui, let's disable it there to avoid the confusion
        if (isTalkBackEnabled && enabledCalendars.size == 1) return@DropdownMenu

        var showSecondaryCalendarSubMenu by remember { mutableStateOf(false) }
        DropdownMenuItem(
            text = { Text(stringResource(R.string.show_secondary_calendar)) },
            onClick = { showSecondaryCalendarSubMenu = !showSecondaryCalendarSubMenu },
        )

        if (showSecondaryCalendarSubMenu) (listOf(null) + enabledCalendars.drop(1)).forEach {
            DropdownMenuRadioItem(
                stringResource(it?.title ?: R.string.none),
                it == secondaryCalendar
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

    if (showDayPickerDialog) DayPickerDialog(
        viewModel.selectedDay.value,
        R.string.go,
        { bringDate(viewModel, it, context) }
    ) { showDayPickerDialog = false }

    if (showShiftWorkDialog) ShiftWorkDialog(
        viewModel.selectedDay.value,
        onDismissRequest = { showShiftWorkDialog = false },
    ) { viewModel.refreshCalendar() }
}

private fun createOwghatHtmlReport(context: Context, date: AbstractDate): String {
    return createHTML().html {
        val coordinates = coordinates.value ?: return@html
        attributes["lang"] = language.language
        attributes["dir"] = if (context.resources.isRtl) "rtl" else "ltr"
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
                    context.appPrefs?.cityName,
                    language.my.format(date.monthName, formatNumber(date.year))
                ).joinToString(spacedComma)
            }
            table {
                thead {
                    tr {
                        th { +context.getString(R.string.day) }
                        getTimeNames().forEach { th { +context.getString(it) } }
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
                if (calculationMethod != language.preferredCalculationMethod) {
                    tfoot {
                        tr {
                            td {
                                colSpan = "10"; +context.getString(calculationMethod.titleStringId)
                            }
                        }
                    }
                }
            }
            script { unsafe { +"print()" } }
        }
    }
}

@Composable
private fun DropdownMenuRadioItem(
    text: String,
    isSelected: Boolean,
    setSelected: (Boolean) -> Unit,
) {
    DropdownMenuItem(
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                Text(text, Modifier.weight(1f))
                RadioButton(selected = isSelected, onClick = null)
            }
        },
        onClick = { setSelected(!isSelected) },
    )
}

class AddEventContract : ActivityResultContract<Jdn, Void?>() {
    override fun parseResult(resultCode: Int, intent: Intent?): Void? = null
    override fun createIntent(context: Context, input: Jdn): Intent {
        val time = input.toGregorianCalendar().timeInMillis
        return Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(
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
        showDialog = false
        if (isGranted) runCatching {
            addEvent.launch(viewModel.selectedDay.value)
        }.onFailure(logException).onFailure {
            Toast.makeText(context, R.string.device_does_not_support, Toast.LENGTH_SHORT).show()
        }
    }

    return { showDialog = true }
}
