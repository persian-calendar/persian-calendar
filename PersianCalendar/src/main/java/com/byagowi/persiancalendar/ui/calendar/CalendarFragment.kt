package com.byagowi.persiancalendar.ui.calendar

import android.Manifest
import android.animation.LayoutTransition
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
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
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.SearchAutoComplete
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
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
import com.byagowi.persiancalendar.ui.MainActivity
import com.byagowi.persiancalendar.ui.calendar.dialogs.MonthOverviewDialog
import com.byagowi.persiancalendar.ui.calendar.dialogs.SelectDayDialog
import com.byagowi.persiancalendar.ui.calendar.dialogs.ShiftWorkDialog
import com.byagowi.persiancalendar.ui.calendar.times.TimeItemAdapter
import com.byagowi.persiancalendar.ui.shared.CalendarsView
import com.byagowi.persiancalendar.utils.*
import com.cepmuvakkit.times.posAlgo.SunMoonPosition
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
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
    private lateinit var mainBinding: FragmentCalendarBinding
    private lateinit var calendarsView: CalendarsView
    private var owghatBinding: OwghatTabContentBinding? = null
    private lateinit var eventsBinding: EventsTabContentBinding
    private var searchView: SearchView? = null
    private var todayButton: MenuItem? = null
    lateinit var mainActivity: MainActivity
    val initialDate = getTodayOfCalendar(mainCalendar)

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = FragmentCalendarBinding.inflate(inflater, container, false).apply {
        mainBinding = this

        mainActivity = activity as MainActivity

        val tabs = listOf(

                // First tab
                R.string.calendar to CalendarsView(mainActivity).apply {
                    calendarsView = this
                },

                // Second tab
                R.string.events to EventsTabContentBinding.inflate(inflater, container, false).apply {
                    eventsBinding = this

                    // Apply some animation, don't do the same for others tabs, it is problematic
                    eventsContent.layoutTransition =
                            LayoutTransition().apply { enableTransitionType(LayoutTransition.CHANGING) }
                }.root

        ) + (getCoordinate(mainActivity)?.run {
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

                        timesRecyclerView.run {
                            layoutManager = FlexboxLayoutManager(context).apply {
                                flexWrap = FlexWrap.WRAP
                                justifyContent = JustifyContent.CENTER
                            }
                            adapter = TimeItemAdapter()
                        }
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
                    mainActivity.setTitleAndSubtitle(getMonthName(it), formatNumber(it.year))
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
                mainActivity.appPrefs.edit { putInt(LAST_CHOSEN_TAB_KEY, position) }
            }
        })

        var lastTab = mainActivity.appPrefs.getInt(LAST_CHOSEN_TAB_KEY, CALENDARS_TAB)
        if (lastTab >= tabs.size) lastTab = CALENDARS_TAB
        viewPager.setCurrentItem(lastTab, false)
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bringDate(getTodayJdn(), monthChange = false, highlight = false)

        setHasOptionsMenu(true)

        getTodayOfCalendar(mainCalendar).also {
            mainActivity.setTitleAndSubtitle(
                    getMonthName(it),
                    formatNumber(it.year)
            )
        }
    }

    private fun addEventOnCalendar(jdn: Long) {
        val civil = CivilDate(jdn)
        val time = Calendar.getInstance()
        time.set(civil.year, civil.month - 1, civil.dayOfMonth)
        if (ActivityCompat.checkSelfPermission(
                        mainActivity, Manifest.permission.READ_CALENDAR
                ) != PackageManager.PERMISSION_GRANTED
        ) askForCalendarPermission(activity) else {
            try {
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
            } catch (e: Exception) {
                e.printStackTrace()
                Snackbar.make(
                        mainBinding.root,
                        R.string.device_calendar_does_not_support,
                        Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CALENDAR_EVENT_ADD_MODIFY_REQUEST_CODE) {
            if (isShowDeviceCalendarEvents)
                mainBinding.calendarPager.refresh(isEventsModified = true)
            else {
                if (ActivityCompat.checkSelfPermission(
                                mainActivity, Manifest.permission.READ_CALENDAR
                        ) != PackageManager.PERMISSION_GRANTED
                ) askForCalendarPermission(activity) else {
                    toggleShowDeviceCalendarOnPreference(mainActivity, true)
                    mainActivity.restartActivity()
                }
            }
        }
    }

    private fun getDeviceEventsTitle(dayEvents: List<CalendarEvent<*>>) = dayEvents
            .filterIsInstance<DeviceCalendarEvent>()
            .map { event ->
                SpannableString(formatDeviceCalendarEventTitle(event)).apply {
                    setSpan(object : ClickableSpan() {
                        override fun onClick(textView: View) = try {
                            startActivityForResult(
                                    Intent(Intent.ACTION_VIEW)
                                            .setData(
                                                    ContentUris.withAppendedId(
                                                            CalendarContract.Events.CONTENT_URI, event.id.toLong()
                                                    )
                                            ),
                                    CALENDAR_EVENT_ADD_MODIFY_REQUEST_CODE
                            )
                        } catch (e: Exception) { // Should be ActivityNotFoundException but we don't care really
                            e.printStackTrace()
                            Snackbar.make(
                                    textView,
                                    R.string.device_calendar_does_not_support,
                                    Snackbar.LENGTH_SHORT
                            ).show()
                        }

                        override fun updateDrawState(ds: TextPaint) {
                            super.updateDrawState(ds)
                            if (event.color.isNotEmpty()) {
                                try {
                                    // should be turned to long then int otherwise gets stupid alpha
                                    ds.color = event.color.toLong().toInt()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
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

        mainBinding.calendarPager.setSelectedDay(jdn, highlight, monthChange)

        val isToday = getTodayJdn() == jdn

        // Show/Hide bring today menu button
        todayButton?.isVisible = !isToday

        // Update tabs
        calendarsView.showCalendars(jdn, mainCalendar, getEnabledCalendarTypes())
        showEvent(jdn, isToday)
        setOwghat(jdn, isToday)

        // a11y
        if (isTalkBackEnabled && !isToday && monthChange) Snackbar.make(
                mainBinding.root,
                getA11yDaySummary(
                        mainActivity, jdn, false, emptyEventsStore(),
                        withZodiac = true, withOtherCalendars = true, withTitle = true
                ),
                Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun showEvent(jdn: Long, isToday: Boolean) {
        eventsBinding.run {
            shiftWorkTitle.text = getShiftWorkTitle(jdn, false)
            val events = getEvents(
                    jdn,
                    readDayDeviceEvents(mainActivity, jdn)
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

            val enabledTypes = mainActivity.appPrefs
                    .getStringSet(PREF_HOLIDAY_TYPES, null) ?: emptySet()
            if (enabledTypes.isEmpty()) {
                noEvent.visibility = View.GONE
                if (messageToShow.isNotEmpty()) messageToShow.append("\n")

                val title = getString(R.string.warn_if_events_not_set)
                val ss = SpannableString(title)
                val clickableSpan = object : ClickableSpan() {
                    override fun onClick(textView: View) {
                        mainActivity.navigateTo(R.id.settings)
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
        (owghatBinding?.timesRecyclerView?.adapter as? TimeItemAdapter)?.prayTimes = prayTimes
        owghatBinding?.sunView?.run {
            setSunriseSunsetMoonPhase(prayTimes, try {
                coordinate?.run {
                    SunMoonPosition(
                            getTodayJdn().toDouble(), latitude,
                            longitude, 0.0, 0.0
                    ).moonPhase
                } ?: 1.0
            } catch (e: Exception) {
                e.printStackTrace()
                1.0
            })
            visibility = if (isToday) View.VISIBLE else View.GONE
            if (isToday && mainBinding.viewPager.currentItem == OWGHAT_TAB) startAnimate()
        }
    }

    private fun onOwghatClick() {
        (owghatBinding?.timesRecyclerView?.adapter as? TimeItemAdapter)?.apply {
            isExpanded = !isExpanded
            owghatBinding?.moreOwghat?.animate()
                    ?.rotation(if (isExpanded) 180f else 0f)
                    ?.setDuration(resources.getInteger(android.R.integer.config_shortAnimTime).toLong())
                    ?.start()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.calendar_menu_buttons, menu)

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
                                    mainActivity, R.layout.suggestion, android.R.id.text1,
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.go_to -> SelectDayDialog.newInstance(selectedJdn).apply {
                onSuccess = fun(jdn: Long) { bringDate(jdn) }
            }.show(
                    childFragmentManager,
                    SelectDayDialog::class.java.name
            )
            R.id.add_event -> addEventOnCalendar(selectedJdn)
            R.id.shift_work -> ShiftWorkDialog.newInstance(selectedJdn).apply {
                onSuccess = fun() {
                    updateStoredPreference(mainActivity)
                    mainActivity.restartActivity()
                }
            }.show(
                    childFragmentManager,
                    ShiftWorkDialog::class.java.name
            )
            R.id.month_overview -> MonthOverviewDialog
                    .newInstance(mainBinding.calendarPager.selectedMonth.toJdn())
                    .show(childFragmentManager, MonthOverviewDialog::class.java.name)
        }
        return true
    }

    fun closeSearch() = searchView?.run {
        if (!isIconified) {
            onActionViewCollapsed()
            return true
        } else false
    } ?: false
}