package com.byagowi.persiancalendar.ui.calendar

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContract
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.POST_NOTIFICATION_PERMISSION_REQUEST_CODE_ENABLE_CALENDAR_NOTIFICATION
import com.byagowi.persiancalendar.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.PREF_DISABLE_OWGHAT
import com.byagowi.persiancalendar.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.PREF_LAST_APP_VISIT_VERSION
import com.byagowi.persiancalendar.PREF_NOTIFY_IGNORED
import com.byagowi.persiancalendar.PREF_OTHER_CALENDARS_KEY
import com.byagowi.persiancalendar.PREF_SECONDARY_CALENDAR_IN_TABLE
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.CalendarScreenBinding
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.calculationMethod
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.enabledCalendars
import com.byagowi.persiancalendar.global.isShowDeviceCalendarEvents
import com.byagowi.persiancalendar.global.isTalkBackEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.secondaryCalendar
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.ui.MainActivity
import com.byagowi.persiancalendar.ui.calendar.dialogs.DayPickerDialog
import com.byagowi.persiancalendar.ui.calendar.dialogs.showMonthOverview
import com.byagowi.persiancalendar.ui.calendar.searchevent.SearchEventsStore.Companion.formattedTitle
import com.byagowi.persiancalendar.ui.calendar.shiftwork.ShiftWorkDialog
import com.byagowi.persiancalendar.ui.calendar.times.TimesTab
import com.byagowi.persiancalendar.ui.common.CalendarsOverview
import com.byagowi.persiancalendar.ui.common.ShrinkingFloatingActionButton
import com.byagowi.persiancalendar.ui.settings.INTERFACE_CALENDAR_TAB
import com.byagowi.persiancalendar.ui.settings.LOCATION_ATHAN_TAB
import com.byagowi.persiancalendar.ui.theme.AppTheme
import com.byagowi.persiancalendar.ui.utils.ExtraLargeShapeCornerSize
import com.byagowi.persiancalendar.ui.utils.MaterialCornerExtraLargeNoBottomEnd
import com.byagowi.persiancalendar.ui.utils.MaterialCornerExtraLargeTop
import com.byagowi.persiancalendar.ui.utils.askForCalendarPermission
import com.byagowi.persiancalendar.ui.utils.askForPostNotificationPermission
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.isRtl
import com.byagowi.persiancalendar.ui.utils.navigateSafe
import com.byagowi.persiancalendar.ui.utils.openHtmlInBrowser
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.utils.TWO_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import com.byagowi.persiancalendar.utils.calendarType
import com.byagowi.persiancalendar.utils.cityName
import com.byagowi.persiancalendar.utils.dayTitleSummary
import com.byagowi.persiancalendar.utils.enableDeviceCalendar
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getA11yDaySummary
import com.byagowi.persiancalendar.utils.getFromStringId
import com.byagowi.persiancalendar.utils.getTimeNames
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.monthFormatForSecondaryCalendar
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.utils.titleStringId
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

class CalendarFragment : Fragment(R.layout.calendar_screen) {

    private var mainBinding: CalendarScreenBinding? = null

    override fun onDestroyView() {
        super.onDestroyView()
        mainBinding = null
    }

    private val onBackPressedCloseSearchCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            viewModel.closeSearch()
            isEnabled = false
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.onBackPressedDispatcher?.addCallback(this, onBackPressedCloseSearchCallback)
    }

    private fun enableTimesTab(context: Context): Boolean {
        val appPrefs = context.appPrefs
        return coordinates.value != null || // if coordinates is set, should be shown
                (language.isPersian && // The placeholder isn't translated to other languages
                        // The user is already dismissed the third tab
                        !appPrefs.getBoolean(PREF_DISABLE_OWGHAT, false) &&
                        // Try to not show the placeholder to established users
                        PREF_APP_LANGUAGE !in appPrefs)
    }

    @Composable
    private fun EventsTab() {
        EventsTab(
            navigateToHolidaysSettings = {
                findNavController().navigateSafe(
                    CalendarFragmentDirections.navigateToSettings(
                        tab = INTERFACE_CALENDAR_TAB, preferenceKey = PREF_HOLIDAY_TYPES
                    )
                )
            },
            refreshCalendarPagerForEvents = {
                mainBinding?.calendarPager?.refresh(isEventsModified = true)
            },
            viewModel = viewModel,
        )
    }

    @Composable
    private fun TimesTab() {
        TimesTab(
            navigateToSelf = {
                findNavController().navigateSafe(
                    CalendarFragmentDirections.navigateToSelf()
                )
            },
            navigateToSettingsLocationTab = {
                findNavController().navigateSafe(
                    CalendarFragmentDirections.navigateToSettings(tab = LOCATION_ATHAN_TAB)
                )
            },
            navigateToAstronomy = { dayOffset ->
                findNavController().navigateSafe(
                    CalendarFragmentDirections.actionCalendarToAstronomy(dayOffset)
                )
            },
            viewModel,
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = CalendarScreenBinding.bind(view)
        mainBinding = binding

        binding.calendarPager.also {
            it.onDayClicked = { jdn -> bringDate(jdn, monthChange = false) }
            it.onDayLongClicked = ::addEventOnCalendar
            it.setSelectedDay(
                Jdn(viewModel.selectedMonth.value), highlight = false, smoothScroll = false
            )
            it.onMonthSelected = { viewModel.changeSelectedMonth(it.selectedMonth) }
        }

        val tabs = listOfNotNull<Pair<Int, @Composable () -> Unit>>(
            R.string.calendar to { CalendarsTab() },
            R.string.events to { EventsTab() },
            // The optional third tab
            if (enableTimesTab(view.context)) R.string.owghat to { TimesTab() } else null,
        )

        binding.content.setContent {
            AppTheme {
                CalendarScreen(tabs, viewModel) { addEventOnCalendar(viewModel.selectedDay.value) }
            }
        }

        binding.content.post {
            // Just to trick it for relayout, for now
            binding.content.minimumHeight = (220 * resources.dp).toInt()
        }

        binding.toolbar.setContent {
            AppTheme {
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
                ) { if (it) Search() else Toolbar() }
            }
        }

        binding.root.post {
            binding.root.context.appPrefs.edit {
                putInt(PREF_LAST_APP_VISIT_VERSION, BuildConfig.VERSION_CODE)
            }
        }

        if (viewModel.selectedDay.value != Jdn.today()) {
            bringDate(viewModel.selectedDay.value, monthChange = false, smoothScroll = false)
        } else {
            bringDate(Jdn.today(), monthChange = false, highlight = false)
        }
    }

    @Composable
    private fun CalendarsTab() {
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
                ButtonsBar(header = R.string.enable_notification,
                    acceptButton = R.string.notify_date,
                    discardAction = {
                        context.appPrefs.edit { putBoolean(PREF_NOTIFY_IGNORED, true) }
                    }) {
                    activity?.askForPostNotificationPermission(
                        POST_NOTIFICATION_PERMISSION_REQUEST_CODE_ENABLE_CALENDAR_NOTIFICATION
                    )
                }
            }
        }
    }

    private fun addEventOnCalendar(jdn: Jdn) {
        val activity = activity ?: return
        if (ActivityCompat.checkSelfPermission(
                activity, Manifest.permission.READ_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED
        ) activity.askForCalendarPermission() else {
            if (!isShowDeviceCalendarEvents) enableDeviceCalendar(activity, findNavController())
            else runCatching { addEvent.launch(jdn) }.onFailure(logException).onFailure {
                Toast.makeText(
                    view?.context ?: return, R.string.device_does_not_support, Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private val addEvent = registerForActivityResult(object : ActivityResultContract<Jdn, Void?>() {
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
    }) { mainBinding?.calendarPager?.refresh(isEventsModified = true) }

    override fun onResume() {
        super.onResume()
        // If events are enabled refresh the pager events on resumes anyway
        if (isShowDeviceCalendarEvents) mainBinding?.calendarPager?.refresh(isEventsModified = true)
    }

    private val viewModel by viewModels<CalendarViewModel>()

    private fun bringDate(
        jdn: Jdn,
        highlight: Boolean = true,
        monthChange: Boolean = true,
        smoothScroll: Boolean = true
    ) {
        mainBinding?.calendarPager?.setSelectedDay(jdn, highlight, monthChange, smoothScroll)

        val isToday = Jdn.today() == jdn
        viewModel.changeSelectedDay(jdn)

        // a11y
        if (isTalkBackEnabled && !isToday && monthChange) Toast.makeText(
            mainBinding?.root?.context ?: return, getA11yDaySummary(
                context ?: return,
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
    @OptIn(ExperimentalMaterial3Api::class)
    private fun Search() {
        viewLifecycleOwner.lifecycleScope.launch {
            // 2s timeout, give up if took too much time
            withTimeoutOrNull(TWO_SECONDS_IN_MILLIS) { viewModel.initializeEventsRepository() }
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
            events.take(10).forEach { event ->
                Box(
                    Modifier
                        .clickable {
                            viewModel.closeSearch()
                            bringEvent(event)
                        }
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 24.dp),
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

    private fun bringEvent(event: CalendarEvent<*>) {
        val date = event.date
        val type = date.calendarType
        val today = Jdn.today().toCalendar(type)
        bringDate(
            Jdn(
                type,
                if (date.year == -1) (today.year + if (date.month < today.month) 1 else 0)
                else date.year,
                date.month,
                date.dayOfMonth
            )
        )
    }

    @Composable
    private fun Toolbar() {
        val context = LocalContext.current
        // TODO: Ideally this should be onPrimary
        val colorOnAppBar = Color(context.resolveColor(R.attr.colorOnAppBar))
        @OptIn(ExperimentalMaterial3Api::class) TopAppBar(
            title = {
                val date by viewModel.selectedMonth.collectAsState()
                val secondaryCalendar = secondaryCalendar
                val title: String
                val subtitle: String
                if (secondaryCalendar == null) {
                    title = date.monthName
                    subtitle = formatNumber(date.year)
                } else {
                    title = language.my.format(date.monthName, formatNumber(date.year))
                    subtitle = monthFormatForSecondaryCalendar(date, secondaryCalendar)
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
                    ) { state -> Text(state, style = MaterialTheme.typography.titleMedium) }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                navigationIconContentColor = colorOnAppBar,
                actionIconContentColor = colorOnAppBar,
                titleContentColor = colorOnAppBar,
            ),
            navigationIcon = {
                IconButton(onClick = { (context as? MainActivity)?.openDrawer() }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = stringResource(R.string.open_drawer)
                    )
                }
            },
            actions = {
                run {
                    val selectedDay by viewModel.selectedDay.collectAsState()
                    val selectedMonth by viewModel.selectedMonth.collectAsState()
                    val todayJdn = Jdn.today()
                    val todayDate = todayJdn.toCalendar(mainCalendar)
                    AnimatedVisibility(
                        selectedMonth.year != todayDate.year || selectedMonth.month != todayDate.month ||
                                selectedDay != todayJdn
                    ) {
                        IconButton(onClick = { bringDate(Jdn.today(), highlight = false) }) {
                            Icon(
                                ImageVector.vectorResource(R.drawable.ic_restore_modified),
                                contentDescription = stringResource(R.string.return_to_today),
                            )
                        }
                    }
                }

                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip { Text(text = stringResource(R.string.search_in_events)) }
                    },
                    state = rememberTooltipState()
                ) {
                    IconButton(onClick = {
                        viewModel.openSearch()
                        onBackPressedCloseSearchCallback.isEnabled = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.search_in_events)
                        )
                    }
                }

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
                Menu(showMenu) { showMenu = false }
            },
        )
    }

    @Composable
    private fun Menu(showMenu: Boolean, closeMenu: () -> Unit) {
        var showDayPickerDialog by remember { mutableStateOf(false) }
        var showShiftWorkDialog by remember { mutableStateOf(false) }

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
                    addEventOnCalendar(viewModel.selectedDay.value)
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
                    activity?.let { showMonthOverview(it, viewModel.selectedMonth.value) }
                },
            )

            val coordinates by coordinates.collectAsState()
            if (coordinates != null) DropdownMenuItem(
                text = { Text(stringResource(R.string.month_pray_times)) },
                onClick = {
                    closeMenu()
                    context?.openHtmlInBrowser(createOwghatHtmlReport(viewModel.selectedMonth.value))
                },
            )

            // It doesn't have any effect in talkback ui, let's disable it there to avoid the confusion
            if (isTalkBackEnabled && enabledCalendars.size == 1) return@DropdownMenu

            var showSecondaryCalendarSubMenu by remember { mutableStateOf(false) }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.show_secondary_calendar)) },
                onClick = { showSecondaryCalendarSubMenu = !showSecondaryCalendarSubMenu },
            )

            val context = LocalContext.current
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
                    findNavController().navigateSafe(CalendarFragmentDirections.navigateToSelf())
                }
            }
        }

        if (showDayPickerDialog) DayPickerDialog(
            viewModel.selectedDay.value,
            R.string.go,
            { bringDate(it) }
        ) { showDayPickerDialog = false }

        if (showShiftWorkDialog) ShiftWorkDialog(
            viewModel.selectedDay.value,
            onDismissRequest = { showShiftWorkDialog = false },
        ) { findNavController().navigateSafe(CalendarFragmentDirections.navigateToSelf()) }
    }

    private fun createOwghatHtmlReport(date: AbstractDate): String = createHTML().html {
        val coordinates = coordinates.value ?: return@html
        attributes["lang"] = language.language
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
                    context?.appPrefs?.cityName,
                    language.my.format(date.monthName, formatNumber(date.year))
                ).joinToString(spacedComma)
            }
            table {
                thead {
                    tr {
                        th { +getString(R.string.day) }
                        getTimeNames().forEach { th { +getString(it) } }
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
                        tr { td { colSpan = "10"; +getString(calculationMethod.titleStringId) } }
                    }
                }
            }
            script { unsafe { +"print()" } }
        }
    }
}

//const val CALENDARS_TAB = 0
const val EVENTS_TAB = 1
const val TIMES_TAB = 2

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
private fun CalendarScreen(
    tabs: List<Pair<Int, @Composable () -> Unit>>,
    viewModel: CalendarViewModel,
    addEvent: () -> Unit,
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()
    Surface(
        shape = if (isLandscape) MaterialCornerExtraLargeNoBottomEnd() else MaterialCornerExtraLargeTop(),
        modifier = Modifier.fillMaxSize(),
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
                    modifier = Modifier.clip(MaterialCornerExtraLargeTop()),
                ) { index ->
                    Surface(modifier = Modifier.fillMaxSize()) {
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            tabs[index].second()
                            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
                        }
                    }
                }
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
                Text(text)
                Spacer(
                    Modifier
                        .weight(1f)
                        .width(16.dp),
                )
                RadioButton(
                    modifier = Modifier.size(24.dp),
                    selected = isSelected,
                    onClick = { setSelected(!isSelected) },
                )
            }
        },
        onClick = { setSelected(!isSelected) },
    )
}
