package com.byagowi.persiancalendar.ui.calendar

import android.Manifest
import android.animation.LayoutTransition
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.provider.CalendarContract
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.SearchAutoComplete
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import androidx.viewpager2.widget.ViewPager2
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.LAST_CHOSEN_TAB_KEY
import com.byagowi.persiancalendar.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.PREF_DISABLE_OWGHAT
import com.byagowi.persiancalendar.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.PREF_LAST_APP_VISIT_VERSION
import com.byagowi.persiancalendar.PREF_OTHER_CALENDARS_KEY
import com.byagowi.persiancalendar.PREF_SECONDARY_CALENDAR_IN_TABLE
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.TIME_NAMES
import com.byagowi.persiancalendar.databinding.EventsTabContentBinding
import com.byagowi.persiancalendar.databinding.FragmentCalendarBinding
import com.byagowi.persiancalendar.databinding.OwghatTabContentBinding
import com.byagowi.persiancalendar.databinding.OwghatTabPlaceholderBinding
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.calculationMethod
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.enabledCalendars
import com.byagowi.persiancalendar.global.isHighTextContrastEnabled
import com.byagowi.persiancalendar.global.isShowDeviceCalendarEvents
import com.byagowi.persiancalendar.global.isTalkBackEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.secondaryCalendar
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.ui.calendar.calendarpager.CalendarPager
import com.byagowi.persiancalendar.ui.calendar.dialogs.showDayPickerDialog
import com.byagowi.persiancalendar.ui.calendar.dialogs.showMonthOverviewDialog
import com.byagowi.persiancalendar.ui.calendar.dialogs.showShiftWorkDialog
import com.byagowi.persiancalendar.ui.calendar.searchevent.SearchEventsAdapter
import com.byagowi.persiancalendar.ui.common.ArrowView
import com.byagowi.persiancalendar.ui.common.CalendarsView
import com.byagowi.persiancalendar.ui.preferences.PreferencesFragment
import com.byagowi.persiancalendar.ui.utils.askForCalendarPermission
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.hideToolbarBottomShadow
import com.byagowi.persiancalendar.ui.utils.navigateSafe
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.openHtmlInBrowser
import com.byagowi.persiancalendar.ui.utils.setupExpandableAccessibilityDescription
import com.byagowi.persiancalendar.ui.utils.setupMenuNavigation
import com.byagowi.persiancalendar.utils.EnabledHolidays
import com.byagowi.persiancalendar.utils.EventsStore
import com.byagowi.persiancalendar.utils.TWO_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import com.byagowi.persiancalendar.utils.calculateSunMoonPosition
import com.byagowi.persiancalendar.utils.calendarType
import com.byagowi.persiancalendar.utils.cityName
import com.byagowi.persiancalendar.utils.dayTitleSummary
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.formatTitle
import com.byagowi.persiancalendar.utils.getA11yDaySummary
import com.byagowi.persiancalendar.utils.getEvents
import com.byagowi.persiancalendar.utils.getEventsTitle
import com.byagowi.persiancalendar.utils.getFromStringId
import com.byagowi.persiancalendar.utils.getShiftWorkTitle
import com.byagowi.persiancalendar.utils.isRtl
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.monthFormatForSecondaryCalendar
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.utils.readDayDeviceEvents
import com.byagowi.persiancalendar.utils.titleStringId
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import io.github.persiancalendar.calendar.AbstractDate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

class CalendarFragment : Fragment(R.layout.fragment_calendar) {

    private var mainBinding: FragmentCalendarBinding? = null
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

    private fun enableOwghatTab(context: Context): Boolean {
        val appPrefs = context.appPrefs
        return coordinates != null || // if coordinates is set, should be shown
                (language.isPersian && // The placeholder isn't translated to other languages
                        // The user is already dismissed the third tab
                        !appPrefs.getBoolean(PREF_DISABLE_OWGHAT, false) &&
                        // Try to not show the placeholder to established users
                        PREF_APP_LANGUAGE !in appPrefs)
    }

    private fun createEventsTab(inflater: LayoutInflater, container: ViewGroup?): View {
        val binding = EventsTabContentBinding.inflate(inflater, container, false)
        binding.eventsContent.layoutTransition = LayoutTransition().also {
            it.enableTransitionType(LayoutTransition.CHANGING)
            it.setAnimateParentHierarchy(false)
        }
        viewModel.selectedDayChangeEvent
            .onEach { showEvent(binding, it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
        return binding.root
    }

    private fun createOwghatTab(inflater: LayoutInflater, container: ViewGroup?): View {
        val coordinates = coordinates ?: return createOwghatTabPlaceholder(inflater, container)
        val binding = OwghatTabContentBinding.inflate(inflater, container, false)

        var isExpanded = false
        binding.root.setOnClickListener {
            isExpanded = !isExpanded
            binding.timesFlow.toggle()
            binding.expansionArrow.animateTo(
                if (isExpanded) ArrowView.Direction.UP else ArrowView.Direction.DOWN
            )
            TransitionManager.beginDelayedTransition(binding.root, ChangeBounds())
        }
        binding.root.setupExpandableAccessibilityDescription()
        binding.cityName.text = binding.root.context.appPrefs.cityName
        binding.times.layoutTransition = LayoutTransition().also {
            it.enableTransitionType(LayoutTransition.APPEARING)
            it.setAnimateParentHierarchy(false)
        }
        binding.timesFlow.setup()

        viewModel.selectedDayChangeEvent
            .onEach { jdn -> setOwghat(binding, jdn, jdn == Jdn.today()) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.selectedTabIndex
            .onEach {
                if (it == OWGHAT_TAB) binding.sunView.startAnimate()
                else binding.sunView.clear()
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        return binding.root
    }

    private fun createOwghatTabPlaceholder(inflater: LayoutInflater, container: ViewGroup?): View {
        val binding = OwghatTabPlaceholderBinding.inflate(inflater, container, false)
        binding.buttonsBar.header.setText(R.string.ask_user_to_set_location)
        binding.buttonsBar.settings.setOnClickListener {
            findNavController().navigateSafe(
                CalendarFragmentDirections.navigateToSettings(PreferencesFragment.LOCATION_ATHAN_TAB)
            )
        }
        binding.buttonsBar.discard.setOnClickListener {
            context?.appPrefs?.edit { putBoolean(PREF_DISABLE_OWGHAT, true) }
            findNavController().navigateSafe(
                CalendarFragmentDirections.navigateToSelf()
            )
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentCalendarBinding.bind(view)
        mainBinding = binding

        val tabs = listOfNotNull(
            R.string.calendar to CalendarsView(view.context).also { calendarsView ->
                viewModel.selectedDayChangeEvent
                    .onEach { jdn ->
                        calendarsView.showCalendars(jdn, mainCalendar, enabledCalendars)
                    }
                    .launchIn(viewLifecycleOwner.lifecycleScope)
            },
            R.string.events to createEventsTab(layoutInflater, view.parent as ViewGroup),
            if (enableOwghatTab(view.context)) // The optional third tab
                R.string.owghat to createOwghatTab(layoutInflater, view.parent as ViewGroup)
            else null
        )

        // tabs should fill their parent otherwise view pager can't handle it
        tabs.forEach { (_: Int, tabView: View) ->
            tabView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        binding.calendarPager.also {
            it.onDayClicked = { jdn -> bringDate(jdn, monthChange = false) }
            it.onDayLongClicked = ::addEventOnCalendar
            it.setSelectedDay(
                Jdn(viewModel.selectedMonth.value), highlight = false, smoothScroll = false
            )
            it.onMonthSelected = { viewModel.changeSelectedMonth(it.selectedMonth) }
        }

        viewModel.selectedMonth
            .onEach { updateToolbar(binding, it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        val tabsViewPager = binding.viewPager
        tabsViewPager.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun getItemCount(): Int = tabs.size
            override fun getItemViewType(position: Int) = position // set viewtype equal to position
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                object : RecyclerView.ViewHolder(tabs[viewType].second) {}
        }
        TabLayoutMediator(binding.tabLayout, tabsViewPager) { tab, i ->
            tab.setText(tabs[i].first)
        }.attach()
        tabsViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.changeSelectedTabIndex(position)
                context?.appPrefs?.edit { putInt(LAST_CHOSEN_TAB_KEY, position) }

                // Make sure view pager's height at least matches with the shown tab
                binding.viewPager.width.takeIf { it != 0 }?.let { width ->
                    tabs[position].second.measure(
                        View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    )
                    binding.viewPager.minimumHeight = tabs[position].second.measuredHeight
                }
            }
        })

        var lastTab = view.context.appPrefs.getInt(LAST_CHOSEN_TAB_KEY, CALENDARS_TAB)
        if (lastTab >= tabs.size) lastTab = CALENDARS_TAB
        viewModel.changeSelectedTabIndex(lastTab)
        tabsViewPager.setCurrentItem(viewModel.selectedTabIndex.value, false)
        setupMenu(binding.appBar.toolbar, binding.calendarPager)

        binding.root.post {
            binding.root.context.appPrefs.edit {
                putInt(PREF_LAST_APP_VISIT_VERSION, BuildConfig.VERSION_CODE)
            }
        }

        if (viewModel.selectedDay != Jdn.today()) {
            bringDate(viewModel.selectedDay, monthChange = false, smoothScroll = false)
        } else {
            bringDate(Jdn.today(), monthChange = false, highlight = false)
        }

        binding.appBar.let { appBar ->
            appBar.toolbar.setupMenuNavigation()
            appBar.root.hideToolbarBottomShadow()
        }
    }

    private fun addEventOnCalendar(jdn: Jdn) {
        val activity = activity ?: return
        if (ActivityCompat.checkSelfPermission(
                activity, Manifest.permission.READ_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED
        ) activity.askForCalendarPermission() else {
            runCatching { addEvent.launch(jdn) }.onFailure(logException).onFailure {
                Snackbar.make(
                    view ?: return, R.string.device_calendar_does_not_support,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateToolbar(binding: FragmentCalendarBinding, date: AbstractDate) {
        val toolbar = binding.appBar.toolbar
        val secondaryCalendar = secondaryCalendar
        if (secondaryCalendar == null) {
            toolbar.title = date.monthName
            toolbar.subtitle = formatNumber(date.year)
        } else {
            toolbar.title = language.my.format(date.monthName, formatNumber(date.year))
            toolbar.subtitle = monthFormatForSecondaryCalendar(date, secondaryCalendar)
        }
    }

    private val addEvent =
        registerForActivityResult(object : ActivityResultContract<Jdn, Void?>() {
            override fun parseResult(resultCode: Int, intent: Intent?): Void? = null
            override fun createIntent(context: Context, input: Jdn): Intent {
                val time = input.toJavaCalendar().timeInMillis
                return Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(
                        CalendarContract.Events.DESCRIPTION, dayTitleSummary(
                            input, input.toCalendar(mainCalendar)
                        )
                    )
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, time)
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, time)
                    .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
            }
        }) { mainBinding?.calendarPager?.refresh(isEventsModified = true) }

    private val viewEvent =
        registerForActivityResult(object : ActivityResultContract<Long, Void?>() {
            override fun parseResult(resultCode: Int, intent: Intent?): Void? = null
            override fun createIntent(context: Context, input: Long): Intent =
                Intent(Intent.ACTION_VIEW).setData(
                    ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, input)
                )
        }) { mainBinding?.calendarPager?.refresh(isEventsModified = true) }

    override fun onResume() {
        super.onResume()
        // If events are enabled refresh the pager events on resumes anyway
        if (isShowDeviceCalendarEvents) mainBinding?.calendarPager?.refresh(isEventsModified = true)
    }

    private fun getDeviceEventsTitle(dayEvents: List<CalendarEvent<*>>) = buildSpannedString {
        dayEvents.filterIsInstance<CalendarEvent.DeviceCalendarEvent>().forEachIndexed { i, event ->
            if (i != 0) appendLine()
            inSpans(object : ClickableSpan() {
                override fun onClick(textView: View) = runCatching {
                    viewEvent.launch(event.id.toLong())
                }.onFailure(logException).onFailure {
                    Snackbar.make(
                        textView,
                        R.string.device_calendar_does_not_support,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }.let {}

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    runCatching {
                        // should be turned to long then int otherwise gets stupid alpha
                        if (event.color.isNotEmpty()) ds.color = event.color.toLong().toInt()
                    }.onFailure(logException)
                }
            }) { append(event.formatTitle()) }
        }
    }

    private val viewModel by viewModels<CalendarViewModel>()
    private fun bringDate(
        jdn: Jdn, highlight: Boolean = true, monthChange: Boolean = true,
        smoothScroll: Boolean = true
    ) {
        mainBinding?.calendarPager?.setSelectedDay(jdn, highlight, monthChange, smoothScroll)

        val isToday = Jdn.today() == jdn
        viewModel.changeSelectedDay(jdn)

        // a11y
        if (isTalkBackEnabled && !isToday && monthChange) Snackbar.make(
            mainBinding?.root ?: return,
            getA11yDaySummary(
                context ?: return, jdn, false, EventsStore.empty(),
                withZodiac = true, withOtherCalendars = true, withTitle = true
            ),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun showEvent(eventsBinding: EventsTabContentBinding, jdn: Jdn) {
        val activity = activity ?: return

        eventsBinding.shiftWorkTitle.text = getShiftWorkTitle(jdn, false)
        val events = getEvents(jdn, activity.readDayDeviceEvents(jdn))
        val holidays = getEventsTitle(
            events,
            holiday = true, compact = false, showDeviceCalendarEvents = false, insertRLM = false,
            addIsHoliday = isHighTextContrastEnabled
        )
        val nonHolidays = getEventsTitle(
            events,
            holiday = false, compact = false, showDeviceCalendarEvents = false, insertRLM = false,
            addIsHoliday = false
        )
        val deviceEvents = getDeviceEventsTitle(events)
        val contentDescription = StringBuilder()

        eventsBinding.noEvent.isVisible =
            listOf(holidays, deviceEvents, nonHolidays).all { it.isEmpty() }

        if (holidays.isNotEmpty()) {
            eventsBinding.holidayTitle.text = holidays
            val holidayContent = getString(R.string.holiday_reason, holidays)
            eventsBinding.holidayTitle.contentDescription = holidayContent
            contentDescription.append(holidayContent)
            eventsBinding.holidayTitle.isVisible = true
        } else {
            eventsBinding.holidayTitle.isVisible = false
        }

        if (deviceEvents.isNotEmpty()) {
            eventsBinding.deviceEventTitle.text = deviceEvents
            contentDescription
                .appendLine()
                .appendLine(getString(R.string.show_device_calendar_events))
                .append(deviceEvents)

            eventsBinding.deviceEventTitle.let {
                it.movementMethod = LinkMovementMethod.getInstance()
                it.isVisible = true
            }
        } else {
            eventsBinding.deviceEventTitle.isVisible = false
        }

        if (nonHolidays.isNotEmpty()) {
            eventsBinding.eventTitle.text = nonHolidays
            contentDescription
                .appendLine()
                .appendLine(getString(R.string.events))
                .append(nonHolidays)

            eventsBinding.eventTitle.isVisible = true
        } else {
            eventsBinding.eventTitle.isVisible = false
        }

        if (EnabledHolidays(activity.appPrefs, emptySet()).isEmpty && language.isIranExclusive) {
            eventsBinding.buttonsBar.header.setText(R.string.warn_if_events_not_set)
            eventsBinding.buttonsBar.settings.setOnClickListener {
                findNavController().navigateSafe(
                    CalendarFragmentDirections.navigateToSettings(
                        PreferencesFragment.INTERFACE_CALENDAR_TAB, PREF_HOLIDAY_TYPES
                    )
                )
            }
            eventsBinding.buttonsBar.discard.setOnClickListener {
                activity.appPrefs.edit {
                    putStringSet(PREF_HOLIDAY_TYPES, EnabledHolidays.iranDefault)
                }
                eventsBinding.buttonsBar.root.isVisible = false
            }
        } else eventsBinding.buttonsBar.root.isVisible = false

        eventsBinding.root.contentDescription = contentDescription
    }

    private fun setOwghat(owghatBinding: OwghatTabContentBinding, jdn: Jdn, isToday: Boolean) {
        val coordinates = coordinates ?: return

        val date = jdn.toJavaCalendar()
        val prayTimes = coordinates.calculatePrayTimes(date)
        owghatBinding.timesFlow.update(prayTimes)
        owghatBinding.moonView.isVisible = !isToday
        owghatBinding.moonView.setOnClickListener {
            findNavController().navigateSafe(
                CalendarFragmentDirections.actionCalendarToAstronomy(jdn - Jdn.today())
            )
        }
        if (!isToday) owghatBinding.moonView.jdn = jdn.value.toFloat()
        owghatBinding.sunView.let { sunView ->
            sunView.isVisible = if (isToday) {
                sunView.prayTimes = prayTimes
                sunView.sunMoonPosition = date.calculateSunMoonPosition(coordinates)
                true
            } else false
            if (isToday && mainBinding?.viewPager?.currentItem == OWGHAT_TAB) sunView.startAnimate()
        }
    }

    private fun setupMenu(toolbar: MaterialToolbar, calendarPager: CalendarPager) {
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
        searchView.findViewById<View?>(androidx.appcompat.R.id.search_plate).debugAssertNotNull
            ?.setBackgroundColor(Color.TRANSPARENT)
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
                        type, if (date.year == -1)
                            (today.year + if (date.month < today.month) 1 else 0)
                        else date.year, date.month, date.dayOfMonth
                    )
                )
                searchView.onActionViewCollapsed()
            }
            val eventsAdapter =
                SearchEventsAdapter(context, onQueryChanged = viewModel::searchEvent)
            it.setAdapter(eventsAdapter)
            viewModel.eventsFlow
                .onEach(eventsAdapter::setData)
                .launchIn(viewLifecycleOwner.lifecycleScope)
        }

        toolbar.menu.add(R.string.return_to_today).also {
            it.icon = toolbarContext.getCompatDrawable(R.drawable.ic_restore_modified)
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            it.onClick { bringDate(Jdn.today(), highlight = false) }

            viewModel.todayButtonVisibilityEvent
                .distinctUntilChanged()
                .onEach { visibility -> it.isVisible = visibility }
                .launchIn(viewLifecycleOwner.lifecycleScope)
        }
        toolbar.menu.add(R.string.search_in_events).also {
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            it.actionView = searchView
        }
        toolbar.menu.add(R.string.goto_date).also {
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            it.onClick {
                showDayPickerDialog(
                    activity ?: return@onClick, viewModel.selectedDay, R.string.go
                ) { jdn -> bringDate(jdn) }
            }
        }
        toolbar.menu.add(R.string.add_event).also {
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            it.onClick { addEventOnCalendar(viewModel.selectedDay) }
        }
        toolbar.menu.add(R.string.shift_work_settings).also {
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            it.onClick {
                showShiftWorkDialog(activity ?: return@onClick, viewModel.selectedDay) {
                    findNavController().navigateSafe(CalendarFragmentDirections.navigateToSelf())
                }
            }
        }
        toolbar.menu.add(R.string.month_overview).also {
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            it.onClick {
                showMonthOverviewDialog(activity ?: return@onClick, viewModel.selectedMonth.value)
            }
        }
        if (coordinates != null) {
            toolbar.menu.add(R.string.month_pray_times).also {
                it.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                it.onClick {
                    context.openHtmlInBrowser(createOwghatHtmlReport(viewModel.selectedMonth.value))
                }
            }
        }
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
                    findNavController().navigateSafe(CalendarFragmentDirections.navigateToSelf())
                }
            }
            menu.setGroupCheckable(groupId, true, true)
        }
    }

    private fun createOwghatHtmlReport(date: AbstractDate): String = createHTML().html {
        val coordinates = coordinates ?: return@html
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
                        TIME_NAMES.forEach { th { +getString(it) } }
                    }
                }
                tbody {
                    (0 until mainCalendar.getMonthLength(date.year, date.month)).forEach { day ->
                        tr {
                            val prayTimes = coordinates.calculatePrayTimes(
                                Jdn(mainCalendar.createDate(date.year, date.month, day))
                                    .toJavaCalendar()
                            )
                            th { +formatNumber(day + 1) }
                            TIME_NAMES.forEach {
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

    companion object {
        private const val CALENDARS_TAB = 0
        private const val EVENTS_TAB = 1
        private const val OWGHAT_TAB = 2
    }
}
