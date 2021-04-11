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
import android.view.*
import android.widget.ArrayAdapter
import androidx.activity.addCallback
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.SearchAutoComplete
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.byagowi.persiancalendar.CALENDAR_EVENT_ADD_MODIFY_REQUEST_CODE
import com.byagowi.persiancalendar.LAST_CHOSEN_TAB_KEY
import com.byagowi.persiancalendar.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.EventsTabContentBinding
import com.byagowi.persiancalendar.databinding.FragmentCalendarBinding
import com.byagowi.persiancalendar.databinding.OwghatTabContentBinding
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.DeviceCalendarEvent
import com.byagowi.persiancalendar.ui.calendar.dialogs.MonthOverviewDialog
import com.byagowi.persiancalendar.ui.calendar.dialogs.SelectDayDialog
import com.byagowi.persiancalendar.ui.calendar.dialogs.ShiftWorkDialog
import com.byagowi.persiancalendar.ui.shared.CalendarsView
import com.byagowi.persiancalendar.utils.*
import com.cepmuvakkit.times.posAlgo.SunMoonPosition
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import io.github.persiancalendar.calendar.CivilDate
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
    private val initialDate = getTodayOfCalendar(mainCalendar)
    private var navigation: NavigationInterface? = null

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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigation = activity as? NavigationInterface
        activity?.onBackPressedDispatcher?.addCallback(this) {
            if (closeSearchIfOpen().not()) {
                isEnabled = false
                activity?.onBackPressedDispatcher?.onBackPressed()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = FragmentCalendarBinding.inflate(inflater, container, false).apply {
        mainBinding = this

        val tabs = listOf(

            // First tab
            R.string.calendar to CalendarsView(inflater.context).apply {
                calendarsView = this
            },

            // Second tab
            R.string.events to EventsTabContentBinding.inflate(inflater, container, false).apply {
                eventsBinding = this

                // Apply some animation, don't do the same for others tabs, it is problematic
                eventsContent.layoutTransition = LayoutTransition().apply {
                    enableTransitionType(LayoutTransition.CHANGING)
                    setAnimateParentHierarchy(false)
                }
            }.root

        ) + (getCoordinate(inflater.context)?.run {
            coordinate = this

            listOf(
                // Optional third tab
                R.string.owghat to OwghatTabContentBinding.inflate(
                    inflater, container, false
                ).apply {
                    owghatBinding = this

                    root.setOnClickListener { onOwghatClick() }

                    cityName.run {
                        setOnClickListener { onOwghatClick() }
                        // Easter egg to test AthanActivity
                        setOnLongClickListener {
                            startAthan(context, "FAJR")
                            true
                        }
                        val cityName = getCityName(context, false)
                        if (cityName.isNotEmpty()) text = cityName
                    }

                    times.layoutTransition = LayoutTransition().apply {
                        enableTransitionType(LayoutTransition.APPEARING)
                        setAnimateParentHierarchy(false)
                    }
                    timesFlow.setup(times)
                }.root
            )
        } ?: emptyList())

        // tabs should fill their parent otherwise view pager can't handle it
        tabs.forEach {
            it.second.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        calendarPager.run {
            onDayClicked = fun(jdn: Long) { bringDate(jdn, monthChange = false) }
            onDayLongClicked = fun(jdn: Long) { addEventOnCalendar(jdn) }
            onMonthSelected = fun() {
                selectedMonth.let {
                    updateToolbar(getMonthName(it), formatNumber(it.year))
                    todayButton?.isVisible =
                        it.year != initialDate.year || it.month != initialDate.month
                }
            }
        }

        viewPager.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun getItemCount(): Int = tabs.size
            override fun getItemViewType(position: Int) = position // set viewtype equal to position
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                object : RecyclerView.ViewHolder(tabs[viewType].second) {}
        }
        TabLayoutMediator(tabLayout, viewPager) { tab, i -> tab.setText(tabs[i].first) }.attach()
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == OWGHAT_TAB) owghatBinding?.sunView?.startAnimate()
                else owghatBinding?.sunView?.clear()
                context?.appPrefs?.edit { putInt(LAST_CHOSEN_TAB_KEY, position) }
            }
        })

        var lastTab = inflater.context.appPrefs.getInt(LAST_CHOSEN_TAB_KEY, CALENDARS_TAB)
        if (lastTab >= tabs.size) lastTab = CALENDARS_TAB
        viewPager.setCurrentItem(lastTab, false)
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bringDate(getTodayJdn(), monthChange = false, highlight = false)

        mainBinding?.appBar?.let { appbar ->
            navigation?.setupToolbarWithDrawer(viewLifecycleOwner, appbar.toolbar)
            appbar.toolbar.inflateMenu(R.menu.calendar_menu_buttons)
            setupToolbarMenu(appbar.toolbar.menu)
            appbar.toolbar.setOnMenuItemClickListener { item ->
                when (item?.itemId) {
                    R.id.go_to -> openGoToDayDialog()
                    R.id.add_event -> addEventOnCalendar(selectedJdn)
                    R.id.shift_work -> openShiftWorkDialog()
                    R.id.month_overview -> openMonthOverView()
                }
                true
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) appbar.appbarLayout.outlineProvider =
                null
        }

        getTodayOfCalendar(mainCalendar).let { today ->
            updateToolbar(getMonthName(today), formatNumber(today.year))
        }
    }

    private fun addEventOnCalendar(jdn: Long) {
        val activity = activity ?: return
        val civil = CivilDate(jdn)
        val time = Calendar.getInstance()
        time.set(civil.year, civil.month - 1, civil.dayOfMonth)
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
                                getDateFromJdnOfCalendar(mainCalendar, jdn)
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
        with(mainBinding?.appBar?.toolbar ?: return) {
            this.title = title
            this.subtitle = subTitle
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
                    navigation?.restartActivity()
                }
            }
        }
    }

    private fun getDeviceEventsTitle(dayEvents: List<CalendarEvent<*>>) = dayEvents
        .filterIsInstance<DeviceCalendarEvent>()
        .map { event ->
            SpannableString(formatDeviceCalendarEventTitle(event)).apply {
                setSpan(object : ClickableSpan() {
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
                }, 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        .foldIndexed(SpannableStringBuilder()) { i, result, x ->
            if (i != 0) result.append("\n")
            result.append(x)
            result
        }

    private var selectedJdn = getTodayJdn()

    private fun bringDate(jdn: Long, highlight: Boolean = true, monthChange: Boolean = true) {
        selectedJdn = jdn

        mainBinding?.calendarPager?.setSelectedDay(jdn, highlight, monthChange)

        val isToday = getTodayJdn() == jdn

        // Show/Hide bring today menu button
        todayButton?.isVisible = !isToday

        // Update tabs
        calendarsView?.showCalendars(jdn, mainCalendar, getEnabledCalendarTypes())
        showEvent(jdn, isToday)
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

    private fun showEvent(jdn: Long, isToday: Boolean) {
        val activity = activity ?: return
        eventsBinding?.run {
            shiftWorkTitle.text = getShiftWorkTitle(jdn, false)
            val events = getEvents(
                jdn,
                readDayDeviceEvents(activity, jdn)
            )
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

            eventMessage.visibility = View.GONE
            noEvent.visibility = View.VISIBLE

            if (holidays.isNotEmpty()) {
                noEvent.visibility = View.GONE
                holidayTitle.text = holidays
                val holidayContent = getString(R.string.holiday_reason) + "\n" + holidays
                holidayTitle.contentDescription = holidayContent
                contentDescription.append(holidayContent)
                holidayTitle.visibility = View.VISIBLE
            } else {
                holidayTitle.visibility = View.GONE
            }

            if (deviceEvents.isNotEmpty()) {
                noEvent.visibility = View.GONE
                deviceEventTitle.text = deviceEvents
                contentDescription
                    .append("\n")
                    .append(getString(R.string.show_device_calendar_events))
                    .append("\n")
                    .append(deviceEvents)


                deviceEventTitle.run {
                    movementMethod = LinkMovementMethod.getInstance()
                    visibility = View.VISIBLE
                }

            } else {
                deviceEventTitle.visibility = View.GONE
            }

            if (nonHolidays.isNotEmpty()) {
                noEvent.visibility = View.GONE
                eventTitle.text = nonHolidays
                contentDescription
                    .append("\n")
                    .append(getString(R.string.events))
                    .append("\n")
                    .append(nonHolidays)


                eventTitle.visibility = View.VISIBLE
            } else {
                eventTitle.visibility = View.GONE
            }

            val messageToShow = SpannableStringBuilder()

            val enabledTypes = activity.appPrefs
                .getStringSet(PREF_HOLIDAY_TYPES, null) ?: emptySet()
            if (enabledTypes.isEmpty()) {
                noEvent.visibility = View.GONE
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
                eventMessage.run {
                    text = messageToShow
                    movementMethod = LinkMovementMethod.getInstance()
                    visibility = View.VISIBLE
                }
            }

            root.contentDescription = contentDescription
        }
    }

    private fun setOwghat(jdn: Long, isToday: Boolean) {
        if (coordinate == null) return

        val prayTimes = PrayTimesCalculator.calculate(
            calculationMethod, CivilDate(jdn).toCalendar().time, coordinate
        )
        owghatBinding?.timesFlow?.update(prayTimes)
        owghatBinding?.sunView?.run {
            setSunriseSunsetMoonPhase(prayTimes, runCatching {
                coordinate?.run {
                    SunMoonPosition(
                        getTodayJdn().toDouble(), latitude,
                        longitude, 0.0, 0.0
                    ).moonPhase
                }
            }.onFailure(logException).getOrNull() ?: 1.0)
            visibility = if (isToday) View.VISIBLE else View.GONE
            if (isToday && mainBinding?.viewPager?.currentItem == OWGHAT_TAB) startAnimate()
        }
    }

    var isExpanded = false
    private fun onOwghatClick() {
        isExpanded = !isExpanded
        owghatBinding?.let {
            it.timesFlow.toggle()
            it.moreOwghat.contentDescription = resources.getString(
                if (isExpanded) R.string.close else R.string.open)
            it.moreOwghat.animate()
                .rotation(if (isExpanded) 180f else 0f)
                .setDuration(resources.getInteger(android.R.integer.config_shortAnimTime).toLong())
                .start()
        }
    }

    private fun setupToolbarMenu(menu: Menu) {
        todayButton = menu.findItem(R.id.today_button).apply {
            isVisible = false
            setOnMenuItemClickListener {
                bringDate(getTodayJdn(), highlight = false)
                true
            }
        }
        searchView = (menu.findItem(R.id.search).actionView as? SearchView?)?.apply {
            setOnSearchClickListener {
                // Remove search edit view below bar
                findViewById<View?>(androidx.appcompat.R.id.search_plate)?.setBackgroundColor(
                    Color.TRANSPARENT
                )

                findViewById<SearchAutoComplete?>(androidx.appcompat.R.id.search_src_text)?.apply {
                    setHint(R.string.search_in_events)
                    setAdapter(
                        ArrayAdapter(
                            activity ?: return@apply, R.layout.suggestion, android.R.id.text1,
                            allEnabledEvents + getAllEnabledAppointments(context)
                        )
                    )
                    setOnItemClickListener { parent, _, position, _ ->
                        val date = (parent.getItemAtPosition(position) as CalendarEvent<*>).date
                        val type = getCalendarTypeFromDate(date)
                        val today = getTodayOfCalendar(type)
                        bringDate(
                            getDateOfCalendar(
                                type,
                                if (date.year == -1)
                                    (today.year + if (date.month < today.month) 1 else 0)
                                else date.year,
                                date.month,
                                date.dayOfMonth
                            ).toJdn()
                        )
                        onActionViewCollapsed()
                    }
                }
            }
        }
    }

    private fun openGoToDayDialog() {
        SelectDayDialog.newInstance(selectedJdn).apply {
            onSuccess = fun(jdn: Long) { bringDate(jdn) }
        }.show(
            childFragmentManager,
            SelectDayDialog::class.java.name
        )
    }

    private fun openShiftWorkDialog() {
        val activity = activity ?: return
        ShiftWorkDialog.newInstance(selectedJdn).apply {
            onSuccess = fun() {
                updateStoredPreference(activity)
                navigation?.restartActivity()
            }
        }.show(
            childFragmentManager,
            ShiftWorkDialog::class.java.name
        )
    }

    private fun openMonthOverView() {
        MonthOverviewDialog
            .newInstance(mainBinding?.calendarPager?.selectedMonth?.toJdn() ?: getTodayJdn())
            .show(childFragmentManager, MonthOverviewDialog::class.java.name)
    }

    /**
     * returns true of does sth, otherwise returns else
     */
    private fun closeSearchIfOpen(): Boolean {
        return searchView?.run {
            if (!isIconified) {
                onActionViewCollapsed()
                true
            } else false
        } ?: false
    }
}