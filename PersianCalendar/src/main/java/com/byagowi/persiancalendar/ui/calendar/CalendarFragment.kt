package com.byagowi.persiancalendar.ui.calendar

import android.Manifest
import android.animation.LayoutTransition
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.SearchAutoComplete
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.byagowi.persiancalendar.CALENDAR_EVENT_ADD_MODIFY_REQUEST_CODE
import com.byagowi.persiancalendar.LANG_FA
import com.byagowi.persiancalendar.LAST_CHOSEN_TAB_KEY
import com.byagowi.persiancalendar.PREF_DISABLE_OWGHAT
import com.byagowi.persiancalendar.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.PREF_THEME
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.EventsTabContentBinding
import com.byagowi.persiancalendar.databinding.FragmentCalendarBinding
import com.byagowi.persiancalendar.databinding.OwghatTabContentBinding
import com.byagowi.persiancalendar.databinding.OwghatTabPlaceholderBinding
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.ui.DrawerHost
import com.byagowi.persiancalendar.ui.calendar.dialogs.MonthOverviewDialog
import com.byagowi.persiancalendar.ui.calendar.dialogs.ShiftWorkDialog
import com.byagowi.persiancalendar.ui.calendar.dialogs.showDayPickerDialog
import com.byagowi.persiancalendar.ui.calendar.searchevent.SearchEventsAdapter
import com.byagowi.persiancalendar.ui.preferences.LOCATION_ATHAN_TAB
import com.byagowi.persiancalendar.ui.shared.CalendarsView
import com.byagowi.persiancalendar.utils.Jdn
import com.byagowi.persiancalendar.utils.allEnabledEvents
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.askForCalendarPermission
import com.byagowi.persiancalendar.utils.calculationMethod
import com.byagowi.persiancalendar.utils.dayTitleSummary
import com.byagowi.persiancalendar.utils.emptyEventsStore
import com.byagowi.persiancalendar.utils.formatDeviceCalendarEventTitle
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getA11yDaySummary
import com.byagowi.persiancalendar.utils.getAllEnabledAppointments
import com.byagowi.persiancalendar.utils.getCalendarTypeFromDate
import com.byagowi.persiancalendar.utils.getCityName
import com.byagowi.persiancalendar.utils.getCoordinate
import com.byagowi.persiancalendar.utils.getEnabledCalendarTypes
import com.byagowi.persiancalendar.utils.getEvents
import com.byagowi.persiancalendar.utils.getEventsTitle
import com.byagowi.persiancalendar.utils.getMonthName
import com.byagowi.persiancalendar.utils.getShiftWorkTitle
import com.byagowi.persiancalendar.utils.isHighTextContrastEnabled
import com.byagowi.persiancalendar.utils.isShowDeviceCalendarEvents
import com.byagowi.persiancalendar.utils.isTalkBackEnabled
import com.byagowi.persiancalendar.utils.language
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.mainCalendar
import com.byagowi.persiancalendar.utils.readDayDeviceEvents
import com.byagowi.persiancalendar.utils.startAthan
import com.byagowi.persiancalendar.utils.toCalendar
import com.byagowi.persiancalendar.utils.toggleShowDeviceCalendarOnPreference
import com.cepmuvakkit.times.posAlgo.SunMoonPosition
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import io.github.persiancalendar.praytimes.Coordinate
import io.github.persiancalendar.praytimes.PrayTimesCalculator
import java.util.*

private const val CALENDARS_TAB = 0
private const val EVENTS_TAB = 1
private const val OWGHAT_TAB = 2

class CalendarFragment : Fragment() {

    private var coordinate: Coordinate? = null
    private var mainBinding: FragmentCalendarBinding? = null
    private var calendarsView: CalendarsView? = null
    private var owghatBinding: OwghatTabContentBinding? = null
    private var eventsBinding: EventsTabContentBinding? = null
    private var searchView: SearchView? = null
    private var todayButton: MenuItem? = null
    private val initialDate = Jdn.today.toCalendar(mainCalendar)

    override fun onDestroyView() {
        super.onDestroyView()
        coordinate = null
        mainBinding = null
        calendarsView = null
        owghatBinding = null
        eventsBinding = null
        searchView = null
        todayButton = null
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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = FragmentCalendarBinding.inflate(inflater, container, false).also { binding ->
        mainBinding = binding

        val coordinate = getCoordinate(inflater.context)
        this.coordinate = coordinate

        val appPrefs = inflater.context.appPrefs
        val shouldDisableOwghat = coordinate == null &&
                (appPrefs.getBoolean(PREF_DISABLE_OWGHAT, false) ||
                        // Just to check the isn't new to the app, the value will be not null when
                        // preferences is visited once.
                        appPrefs.getString(PREF_THEME, null) != null ||
                        // Really extra check as a user that has non default PREF_THEME sure went to
                        // preferences once but when we decide to remove the above one we should
                        // have this one at least as the placeholder isn't translated yet.
                        language != LANG_FA)

        val tabs = listOf(
            // First tab
            R.string.calendar to CalendarsView(inflater.context).also { this.calendarsView = it },

            // Second tab
            R.string.events to EventsTabContentBinding.inflate(
                inflater, container, false
            ).also { eventsBinding ->
                this.eventsBinding = eventsBinding
                eventsBinding.eventsContent.layoutTransition = LayoutTransition().also {
                    it.enableTransitionType(LayoutTransition.CHANGING)
                    it.setAnimateParentHierarchy(false)
                }
            }.root
        ) + if (shouldDisableOwghat) emptyList() else listOf(
            // The optional third tab
            R.string.owghat to if (coordinate == null) {
                OwghatTabPlaceholderBinding.inflate(
                    inflater, container, false
                ).also { owghatBindingPlaceholder ->
                    owghatBindingPlaceholder.activate.setOnClickListener {
                        findNavController().navigate(
                            CalendarFragmentDirections.navigateToSettings(LOCATION_ATHAN_TAB)
                        )
                    }
                    owghatBindingPlaceholder.discard.setOnClickListener {
                        context?.appPrefs?.edit { putBoolean(PREF_DISABLE_OWGHAT, true) }
                        findNavController().navigate(CalendarFragmentDirections.navigateToSelf())
                    }
                }.root
            } else {
                OwghatTabContentBinding.inflate(
                    inflater, container, false
                ).also { owghatBinding ->
                    this.owghatBinding = owghatBinding

                    var isExpanded = false
                    val onOwghatTabClick = fun(_: View) {
                        isExpanded = !isExpanded
                        owghatBinding.timesFlow.toggle()
                        owghatBinding.moreOwghat.contentDescription = resources.getString(
                            if (isExpanded) R.string.close else R.string.open
                        )
                        owghatBinding.moreOwghat.animate()
                            .rotation(if (isExpanded) 180f else 0f)
                            .setDuration(
                                resources.getInteger(android.R.integer.config_shortAnimTime)
                                    .toLong()
                            )
                            .start()
                    }

                    owghatBinding.root.setOnClickListener(onOwghatTabClick)
                    owghatBinding.cityName.also {
                        val cityName = getCityName(it.context, false)
                        if (cityName.isNotEmpty()) it.text = cityName

                        it.setOnClickListener(onOwghatTabClick)
                        // Easter egg to test AthanActivity
                        it.setOnLongClickListener { _ ->
                            startAthan(
                                it.context,
                                listOf("FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA").random()
                            )
                            true
                        }
                    }
                    owghatBinding.times.layoutTransition = LayoutTransition().also {
                        it.enableTransitionType(LayoutTransition.APPEARING)
                        it.setAnimateParentHierarchy(false)
                    }
                    owghatBinding.timesFlow.setup()
                }.root
            }
        )

        // tabs should fill their parent otherwise view pager can't handle it
        tabs.forEach { (_: Int, tabView: ViewGroup) ->
            tabView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        binding.calendarPager.also {
            it.onDayClicked = fun(jdn: Long) { bringDate(Jdn(jdn), monthChange = false) }
            it.onDayLongClicked = fun(jdn: Long) { addEventOnCalendar(Jdn(jdn)) }
            it.onMonthSelected = fun() {
                it.selectedMonth.let { date ->
                    updateToolbar(getMonthName(date), formatNumber(date.year))
                    todayButton?.isVisible =
                        date.year != initialDate.year || date.month != initialDate.month
                }
            }
        }

        binding.viewPager.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun getItemCount(): Int = tabs.size
            override fun getItemViewType(position: Int) = position // set viewtype equal to position
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                object : RecyclerView.ViewHolder(tabs[viewType].second) {}
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, i ->
            tab.setText(tabs[i].first)
        }.attach()
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == OWGHAT_TAB) owghatBinding?.sunView?.startAnimate()
                else owghatBinding?.sunView?.clear()
                context?.appPrefs?.edit { putInt(LAST_CHOSEN_TAB_KEY, position) }
            }
        })

        var lastTab = inflater.context.appPrefs.getInt(LAST_CHOSEN_TAB_KEY, CALENDARS_TAB)
        if (lastTab >= tabs.size) lastTab = CALENDARS_TAB
        binding.viewPager.setCurrentItem(lastTab, false)
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bringDate(Jdn.today, monthChange = false, highlight = false)

        mainBinding?.appBar?.let {
            (activity as? DrawerHost)
                ?.setupToolbarWithDrawer(viewLifecycleOwner, it.toolbar)
            it.toolbar.inflateMenu(R.menu.calendar_menu_buttons)
            setupToolbarMenu(it.toolbar.menu)
            it.toolbar.setOnMenuItemClickListener { clickedMenuItem ->
                when (clickedMenuItem?.itemId) {
                    R.id.go_to -> showDayPickerDialog(selectedJdn) { jdn -> bringDate(Jdn(jdn)) }
                    R.id.add_event -> addEventOnCalendar(Jdn(selectedJdn))
                    R.id.shift_work -> openShiftWorkDialog()
                    R.id.month_overview -> openMonthOverView()
                }
                true
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                it.appbarLayout.outlineProvider = null
        }

        Jdn.today.toCalendar(mainCalendar).let { today ->
            updateToolbar(getMonthName(today), formatNumber(today.year))
        }
    }

    private fun addEventOnCalendar(jdn: Jdn) {
        val activity = activity ?: return
        val gregorian = jdn.toGregorianCalendar()
        val time = Calendar.getInstance()
        time.set(gregorian.year, gregorian.month - 1, gregorian.dayOfMonth)
        if (ActivityCompat.checkSelfPermission(
                activity, Manifest.permission.READ_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED
        ) askForCalendarPermission(activity) else {
            runCatching {
                startActivityForResult(
                    Intent(Intent.ACTION_INSERT)
                        .setData(CalendarContract.Events.CONTENT_URI)
                        .putExtra(
                            CalendarContract.Events.DESCRIPTION, dayTitleSummary(
                                jdn.toCalendar(mainCalendar)
                            )
                        )
                        .putExtra(
                            CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                            time.timeInMillis
                        )
                        .putExtra(
                            CalendarContract.EXTRA_EVENT_END_TIME,
                            time.timeInMillis
                        )
                        .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true),
                    CALENDAR_EVENT_ADD_MODIFY_REQUEST_CODE
                )
            }.onFailure(logException).getOrElse {
                Snackbar.make(
                    mainBinding?.root ?: return,
                    R.string.device_calendar_does_not_support,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateToolbar(title: String, subTitle: String) {
        mainBinding?.appBar?.toolbar?.let {
            it.title = title
            it.subtitle = subTitle
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val activity = activity ?: return
        if (requestCode == CALENDAR_EVENT_ADD_MODIFY_REQUEST_CODE) {
            if (isShowDeviceCalendarEvents)
                mainBinding?.calendarPager?.refresh(isEventsModified = true)
            else {
                if (ActivityCompat.checkSelfPermission(
                        activity, Manifest.permission.READ_CALENDAR
                    ) != PackageManager.PERMISSION_GRANTED
                ) askForCalendarPermission(activity) else {
                    toggleShowDeviceCalendarOnPreference(activity, true)
                    findNavController().navigate(CalendarFragmentDirections.navigateToSelf())
                }
            }
        }
    }

    private fun getDeviceEventsTitle(dayEvents: List<CalendarEvent<*>>) = dayEvents
        .filterIsInstance<CalendarEvent.DeviceCalendarEvent>()
        .map { event ->
            val spannableString = SpannableString(formatDeviceCalendarEventTitle(event))
            spannableString.setSpan(object : ClickableSpan() {
                override fun onClick(textView: View) = runCatching {
                    startActivityForResult(
                        Intent(Intent.ACTION_VIEW)
                            .setData(
                                ContentUris.withAppendedId(
                                    CalendarContract.Events.CONTENT_URI, event.id.toLong()
                                )
                            ),
                        CALENDAR_EVENT_ADD_MODIFY_REQUEST_CODE
                    )
                }.onFailure(logException).getOrElse {
                    Snackbar.make(
                        textView,
                        R.string.device_calendar_does_not_support,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    runCatching {
                        // should be turned to long then int otherwise gets stupid alpha
                        if (event.color.isNotEmpty()) ds.color = event.color.toLong().toInt()
                    }.onFailure(logException)
                }
            }, 0, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableString
        }
        .foldIndexed(SpannableStringBuilder()) { i, result, x ->
            if (i != 0) result.append("\n")
            result.append(x)
            result
        }

    private var selectedJdn = Jdn.today.value

    private fun bringDate(jdn: Jdn, highlight: Boolean = true, monthChange: Boolean = true) {
        selectedJdn = jdn.value

        mainBinding?.calendarPager?.setSelectedDay(jdn.value, highlight, monthChange)

        val isToday = Jdn.today == jdn

        // Show/Hide bring today menu button
        todayButton?.isVisible = !isToday

        // Update tabs
        calendarsView?.showCalendars(jdn, mainCalendar, getEnabledCalendarTypes())
        showEvent(jdn)
        setOwghat(jdn, isToday)

        // a11y
        if (isTalkBackEnabled && !isToday && monthChange) Snackbar.make(
            mainBinding?.root ?: return,
            getA11yDaySummary(
                context ?: return, jdn, false, emptyEventsStore(),
                withZodiac = true, withOtherCalendars = true, withTitle = true
            ),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun showEvent(jdn: Jdn) {
        val activity = activity ?: return
        val eventsBinding = eventsBinding ?: return

        eventsBinding.shiftWorkTitle.text = getShiftWorkTitle(jdn, false)
        val events = getEvents(jdn, readDayDeviceEvents(activity, jdn))
        val holidays = getEventsTitle(
            events,
            holiday = true,
            compact = false,
            showDeviceCalendarEvents = false,
            insertRLM = false,
            addIsHoliday = isHighTextContrastEnabled
        )
        val nonHolidays = getEventsTitle(
            events,
            holiday = false,
            compact = false,
            showDeviceCalendarEvents = false,
            insertRLM = false,
            addIsHoliday = false
        )
        val deviceEvents = getDeviceEventsTitle(events)
        val contentDescription = StringBuilder()

        eventsBinding.eventMessage.visibility = View.GONE
        eventsBinding.noEvent.visibility = View.VISIBLE

        if (holidays.isNotEmpty()) {
            eventsBinding.noEvent.visibility = View.GONE
            eventsBinding.holidayTitle.text = holidays
            val holidayContent = getString(R.string.holiday_reason) + "\n" + holidays
            eventsBinding.holidayTitle.contentDescription = holidayContent
            contentDescription.append(holidayContent)
            eventsBinding.holidayTitle.visibility = View.VISIBLE
        } else {
            eventsBinding.holidayTitle.visibility = View.GONE
        }

        if (deviceEvents.isNotEmpty()) {
            eventsBinding.noEvent.visibility = View.GONE
            eventsBinding.deviceEventTitle.text = deviceEvents
            contentDescription
                .append("\n")
                .append(getString(R.string.show_device_calendar_events))
                .append("\n")
                .append(deviceEvents)

            eventsBinding.deviceEventTitle.let {
                it.movementMethod = LinkMovementMethod.getInstance()
                it.visibility = View.VISIBLE
            }
        } else {
            eventsBinding.deviceEventTitle.visibility = View.GONE
        }

        if (nonHolidays.isNotEmpty()) {
            eventsBinding.noEvent.visibility = View.GONE
            eventsBinding.eventTitle.text = nonHolidays
            contentDescription
                .append("\n")
                .append(getString(R.string.events))
                .append("\n")
                .append(nonHolidays)

            eventsBinding.eventTitle.visibility = View.VISIBLE
        } else {
            eventsBinding.eventTitle.visibility = View.GONE
        }

        val messageToShow = SpannableStringBuilder()

        val enabledTypes = activity.appPrefs
            .getStringSet(PREF_HOLIDAY_TYPES, null) ?: emptySet()
        if (enabledTypes.isEmpty()) {
            eventsBinding.noEvent.visibility = View.GONE
            if (messageToShow.isNotEmpty()) messageToShow.append("\n")

            val title = getString(R.string.warn_if_events_not_set)
            val ss = SpannableString(title)
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(textView: View) {
                    val direction = CalendarFragmentDirections.navigateToSettings()
                    findNavController().navigate(direction)
                }
            }
            ss.setSpan(clickableSpan, 0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            messageToShow.append(ss)

            contentDescription
                .append("\n")
                .append(title)
        }

        if (messageToShow.isNotEmpty()) {
            eventsBinding.eventMessage.let {
                it.text = messageToShow
                it.movementMethod = LinkMovementMethod.getInstance()
                it.visibility = View.VISIBLE
            }
        }

        eventsBinding.root.contentDescription = contentDescription
    }

    private fun setOwghat(jdn: Jdn, isToday: Boolean) {
        val coordinate = coordinate ?: return
        val owghatBinding = owghatBinding ?: return

        val prayTimes = PrayTimesCalculator.calculate(
            calculationMethod, jdn.toGregorianCalendar().toCalendar().time, coordinate
        )
        owghatBinding.timesFlow.update(prayTimes)
        owghatBinding.sunView.let { sunView ->
            sunView.visibility = if (isToday) {
                sunView.setSunriseSunsetMoonPhase(prayTimes, runCatching {
                    SunMoonPosition(
                        jdn.value.toDouble(), coordinate.latitude, coordinate.longitude,
                        coordinate.elevation, 0.0
                    ).moonPhase
                }.onFailure(logException).getOrNull() ?: 1.0)
                View.VISIBLE
            } else View.GONE
            if (isToday && mainBinding?.viewPager?.currentItem == OWGHAT_TAB) sunView.startAnimate()
        }
    }

    private fun setupToolbarMenu(menu: Menu) {
        todayButton = menu.findItem(R.id.today_button).also {
            it.isVisible = false
            it.setOnMenuItemClickListener {
                bringDate(Jdn.today, highlight = false)
                true
            }
        }
        searchView = (menu.findItem(R.id.search).actionView as? SearchView)?.also { searchView ->
            searchView.setOnCloseListener {
                onBackPressedCloseSearchCallback.isEnabled = false
                false // don't prevent the event cascade
            }
            searchView.setOnSearchClickListener {
                onBackPressedCloseSearchCallback.isEnabled = true
                // Remove search edit view below bar
                searchView.findViewById<View?>(
                    androidx.appcompat.R.id.search_plate
                )?.setBackgroundColor(Color.TRANSPARENT)

                searchView.findViewById<SearchAutoComplete?>(
                    androidx.appcompat.R.id.search_src_text
                )?.also { searchAutoComplete ->
                    val context = searchAutoComplete.context
                    searchAutoComplete.setHint(R.string.search_in_events)
                    val events = allEnabledEvents + getAllEnabledAppointments(context)
                    searchAutoComplete.setAdapter(SearchEventsAdapter(context, events))
                    searchAutoComplete.setOnItemClickListener { parent, _, position, _ ->
                        val date = (parent.getItemAtPosition(position) as CalendarEvent<*>).date
                        val type = getCalendarTypeFromDate(date)
                        val today = Jdn.today.toCalendar(type)
                        bringDate(
                            Jdn(
                                type, if (date.year == -1)
                                    (today.year + if (date.month < today.month) 1 else 0)
                                else date.year, date.month, date.dayOfMonth
                            )
                        )
                        searchView.onActionViewCollapsed()
                    }
                }
            }
        }
    }

    private fun openShiftWorkDialog() {
        ShiftWorkDialog.newInstance(selectedJdn)
            .show(childFragmentManager, ShiftWorkDialog::class.java.name)
    }

    private fun openMonthOverView() {
        MonthOverviewDialog
            .newInstance(mainBinding?.calendarPager?.selectedMonth?.toJdn() ?: Jdn.today.value)
            .show(childFragmentManager, MonthOverviewDialog::class.java.name)
    }
}
