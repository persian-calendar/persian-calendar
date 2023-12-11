package com.byagowi.persiancalendar.ui.calendar

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.StringRes
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.SearchAutoComplete
import androidx.appcompat.widget.Toolbar
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
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
import com.byagowi.persiancalendar.ui.calendar.calendarpager.CalendarPager
import com.byagowi.persiancalendar.ui.calendar.dialogs.showDayPickerDialog
import com.byagowi.persiancalendar.ui.calendar.dialogs.showMonthOverviewDialog
import com.byagowi.persiancalendar.ui.calendar.searchevent.SearchEventsAdapter
import com.byagowi.persiancalendar.ui.calendar.shiftwork.showShiftWorkDialog
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
import com.byagowi.persiancalendar.ui.utils.getActivity
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.isRtl
import com.byagowi.persiancalendar.ui.utils.navigateSafe
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.openHtmlInBrowser
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
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import io.github.persiancalendar.calendar.AbstractDate
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
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
    private var searchView: SearchView? = null

    override fun onDestroyView() {
        super.onDestroyView()
        mainBinding = null
        searchView = null
    }

    private val onBackPressedCloseSearchCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            searchView?.takeIf { !it.isIconified }?.onActionViewCollapsed()
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedMonth.flowWithLifecycle(
                viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED
            ).collectLatest { updateToolbar(binding, it) }
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

        setupMenu(binding.toolbar, binding.calendarPager)

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

        // Ugly, to get rid of soon
        (context?.getActivity() as? MainActivity)?.setupToolbarWithDrawer(binding.toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // tabs.forEach { (_, view) -> view.updatePadding(bottom = systemBarsInsets.bottom) }
            binding.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBarsInsets.top
            }
            // val allInsets =
            //     insets.getInsets(WindowInsetsCompat.Type.ime() or WindowInsetsCompat.Type.systemBars())
            // binding.addEvent.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            //     bottomMargin = allInsets.bottom + (20 * resources.dp).toInt()
            // }
            // Content root is only available in portrait mode
            // binding.portraitContentRoot?.updatePadding(
            //     bottom = (allInsets.bottom - systemBarsInsets.bottom).coerceAtLeast(0)
            // )
            WindowInsetsCompat.CONSUMED
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

    private fun updateToolbar(binding: CalendarScreenBinding, date: AbstractDate) {
        val toolbar = binding.toolbar
        val secondaryCalendar = secondaryCalendar
        if (secondaryCalendar == null) {
            toolbar.title = date.monthName
            toolbar.subtitle = formatNumber(date.year)
        } else {
            toolbar.title = language.my.format(date.monthName, formatNumber(date.year))
            toolbar.subtitle = monthFormatForSecondaryCalendar(date, secondaryCalendar)
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

    private fun setupMenu(toolbar: Toolbar, calendarPager: CalendarPager) {
        val toolbarContext = toolbar.context // context wrapped with toolbar related theme
        val context = calendarPager.context // context usable for normal dialogs

        val searchView = SearchView(toolbarContext).also { searchView = it }
        searchView.setOnCloseListener {
            onBackPressedCloseSearchCallback.isEnabled = false
            false // don't prevent the event cascade
        }
        searchView.setOnSearchClickListener {
            onBackPressedCloseSearchCallback.isEnabled = true
            viewLifecycleOwner.lifecycleScope.launch {
                // 2s timeout, give up if took too much time
                withTimeoutOrNull(TWO_SECONDS_IN_MILLIS) { viewModel.initializeEventsRepository() }
            }
        }
        // Remove search edit view below bar
        searchView.findViewById<View?>(androidx.appcompat.R.id.search_plate).debugAssertNotNull?.setBackgroundColor(
            android.graphics.Color.TRANSPARENT
        )
        searchView.findViewById<SearchAutoComplete?>(
            androidx.appcompat.R.id.search_src_text
        ).debugAssertNotNull?.let {
            it.setHint(R.string.search_in_events)
            it.setOnItemClickListener { parent, _, position, _ ->
                val date = (parent.getItemAtPosition(position) as CalendarEvent<*>).date
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
                searchView.onActionViewCollapsed()
            }
            val eventsAdapter =
                SearchEventsAdapter(context, onQueryChanged = viewModel::searchEvent)
            it.setAdapter(eventsAdapter)
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.eventsFlow.flowWithLifecycle(
                    viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED
                ).collectLatest(eventsAdapter::setData)
            }
        }

        toolbar.menu.add(R.string.return_to_today).also {
            it.icon = toolbarContext.getCompatDrawable(R.drawable.ic_restore_modified)
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            it.onClick { bringDate(Jdn.today(), highlight = false) }
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.todayButtonVisibilityEvent.flowWithLifecycle(
                    viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED
                ).distinctUntilChanged().collectLatest(it::setVisible)
            }
        }
        toolbar.menu.add(R.string.search_in_events).also {
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            it.actionView = searchView
        }
        toolbar.menu.add(R.string.goto_date).also {
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            it.onClick {
                val activity = activity ?: return@onClick
                showDayPickerDialog(activity, viewModel.selectedDay.value, R.string.go, ::bringDate)
            }
        }
        toolbar.menu.add(R.string.add_event).also {
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            it.onClick { addEventOnCalendar(viewModel.selectedDay.value) }
        }
        toolbar.menu.add(R.string.shift_work_settings).also {
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            it.onClick {
                showShiftWorkDialog(activity ?: return@onClick, viewModel.selectedDay.value)
            }
        }
        toolbar.menu.add(R.string.month_overview).also {
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            it.onClick {
                showMonthOverviewDialog(activity ?: return@onClick, viewModel.selectedMonth.value)
            }
        }
        if (coordinates.value != null) {
            toolbar.menu.add(R.string.month_pray_times).also {
                it.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                it.onClick {
                    context.openHtmlInBrowser(createOwghatHtmlReport(viewModel.selectedMonth.value))
                }
            }
        }
        // It doesn't have any effect in talkback ui, let's disable it there to avoid the confusion
        if (!isTalkBackEnabled) {
            toolbar.menu.addSubMenu(R.string.show_secondary_calendar).also { menu ->
                val groupId = Menu.FIRST
                val prefs = context.appPrefs
                (listOf(null) + enabledCalendars.drop(1)).forEach {
                    val item = menu.add(groupId, Menu.NONE, Menu.NONE, it?.title ?: R.string.none)
                    item.isChecked = it == secondaryCalendar
                    item.onClick {
                        prefs.edit {
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
                        findNavController().navigateSafe(
                            CalendarFragmentDirections.navigateToSelf()
                        )
                    }
                }
                menu.setGroupCheckable(groupId, true, true)
            }
        }
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

const val CALENDARS_TAB = 0
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
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex])
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
                        .padding(24.dp),
                    isVisible = selectedTabIndex == EVENTS_TAB,
                    action = addEvent,
                    icon = Icons.Default.Add,
                    title = stringResource(R.string.add_event),
                )
            }
        }
    }
}
