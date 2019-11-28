package com.byagowi.persiancalendar.ui.calendar

import android.Manifest
import android.animation.LayoutTransition
import android.content.ContentUris
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
import android.widget.FrameLayout
import androidx.appcompat.widget.SearchView
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
import com.byagowi.persiancalendar.ui.calendar.calendarpager.CalendarPager
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
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.praytimes.Coordinate
import io.github.persiancalendar.praytimes.PrayTimesCalculator
import java.util.*

class CalendarFragment : Fragment() {

    private val calendar = Calendar.getInstance()
    private var coordinate: Coordinate? = null
    private lateinit var mainBinding: FragmentCalendarBinding
    private lateinit var calendarsView: CalendarsView
    private var owghatBinding: OwghatTabContentBinding? = null
    private lateinit var eventsBinding: EventsTabContentBinding
    private var searchView: SearchView? = null

    abstract class TabsAdapter : RecyclerView.Adapter<TabsAdapter.ViewHolder>() {
        inner class ViewHolder(private val frame: FrameLayout) : RecyclerView.ViewHolder(frame) {
            fun bind(view: View) = frame.run {
                removeAllViews()
                addView(view)
            }
        }
    }

    lateinit var mainActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        mainActivity = activity as MainActivity

        setHasOptionsMenu(true)

        getTodayOfCalendar(mainCalendar).also {
            mainActivity.setTitleAndSubtitle(
                getMonthName(it),
                formatNumber(it.year)
            )
        }

        val tabs = listOf(

            CALENDARS_TAB to CalendarsView(mainActivity).apply {
                calendarsView = this
                showHideTodayButtonCallback = fun(show) {
                    if (show) mainBinding.todayButton.show()
                    else mainBinding.todayButton.hide()
                }
            },

            EVENTS_TAB to EventsTabContentBinding.inflate(inflater, container, false).apply {
                eventsBinding = this

                // Apply some animation, don't do the same for others tabs, it is problematic
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    eventsContent.layoutTransition =
                        LayoutTransition().apply { enableTransitionType(LayoutTransition.CHANGING) }
                }
            }.root

        ) + (getCoordinate(mainActivity)?.run {
            coordinate = this

            listOf(
                OWGHAT_TAB to OwghatTabContentBinding.inflate(inflater, container, false).apply {
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

        return FragmentCalendarBinding.inflate(inflater, container, false).apply {
            mainBinding = this

            todayButton.setOnClickListener { bringTodayYearMonth() }

            calendarPager.onDayClicked = fun(jdn: Long) { selectDay(jdn) }
            calendarPager.onDayLongClicked = fun(jdn: Long) { addEventOnCalendar(jdn) }
            calendarPager.onNonDefaultPageSelected = fun() { todayButton.show() }
            calendarPager.onPageSelectedWithDate = fun(date: AbstractDate) {
                mainActivity.setTitleAndSubtitle(
                    getMonthName(date),
                    formatNumber(date.year)
                )
            }

            tabsViewPager.adapter = object : TabsAdapter() {
                override fun getItemCount(): Int = tabs.size
                override fun onBindViewHolder(holder: ViewHolder, position: Int) =
                    holder.bind(tabs[position].second)

                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
                    FrameLayout(mainActivity).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                )
            }

            tabsViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    if (position == OWGHAT_TAB) owghatBinding?.sunView?.startAnimate()
                    else owghatBinding?.sunView?.clear()
                    mainActivity.appPrefs.edit { putInt(LAST_CHOSEN_TAB_KEY, position) }
                }
            })

            TabLayoutMediator(tabLayout, tabsViewPager) { tab, position ->
                tab.setText(
                    listOf(
                        R.string.calendar, R.string.events, R.string.owghat
                    )[tabs[position].first]
                )
            }.attach()

            var lastTab = mainActivity.appPrefs.getInt(LAST_CHOSEN_TAB_KEY, CALENDARS_TAB)
            if (lastTab >= tabs.size) lastTab = CALENDARS_TAB
            tabsViewPager.setCurrentItem(lastTab, false)
        }.root
    }

    private fun selectDay(jdn: Long) {
        mainBinding.calendarPager.lastSelectedJdn = jdn
        calendarsView.showCalendars(
            mainBinding.calendarPager.lastSelectedJdn, mainCalendar, getEnabledCalendarTypes()
        )
        val isToday = getTodayJdn() == mainBinding.calendarPager.lastSelectedJdn
        setOwghat(jdn, isToday)
        showEvent(jdn, isToday)
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
            if (isShowDeviceCalendarEvents) {
                mainBinding.calendarPager.updateMonthFragments(
                    calculateViewPagerPositionFromJdn(mainBinding.calendarPager.lastSelectedJdn),
                    true
                )
            } else {
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
                                ds.color = event.color.toInt()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }, 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        .foldIndexed(SpannableStringBuilder()) { i, result, x ->
            result.apply {
                if (i != 0) append("\n")
                append(x)
            }
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
                insertRLM = false
            )
            val nonHolidays = getEventsTitle(
                events,
                holiday = false,
                compact = false,
                showDeviceCalendarEvents = false,
                insertRLM = false
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
                contentDescription.append("\n")
                contentDescription.append(getString(R.string.show_device_calendar_events))
                contentDescription.append("\n")
                contentDescription.append(deviceEvents)
                deviceEventTitle.movementMethod = LinkMovementMethod.getInstance()

                deviceEventTitle.visibility = View.VISIBLE
            } else {
                deviceEventTitle.visibility = View.GONE
            }

            if (nonHolidays.isNotEmpty()) {
                noEvent.visibility = View.GONE
                eventTitle.text = nonHolidays
                contentDescription.append("\n")
                contentDescription.append(getString(R.string.events))
                contentDescription.append("\n")
                contentDescription.append(nonHolidays)

                eventTitle.visibility = View.VISIBLE
            } else {
                eventTitle.visibility = View.GONE
            }

            val messageToShow = SpannableStringBuilder()

            val enabledTypes = mainActivity.appPrefs
                .getStringSet(PREF_HOLIDAY_TYPES, null) ?: emptySet()
            if (enabledTypes.size == 0) {
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

                contentDescription.append("\n")
                contentDescription.append(title)
            }

            if (messageToShow.isNotEmpty()) {
                eventMessage.text = messageToShow
                eventMessage.movementMethod = LinkMovementMethod.getInstance()
                eventMessage.visibility = View.VISIBLE
            }

            todayButtonSpace.visibility = if (isToday) View.GONE else View.VISIBLE

            root.contentDescription = contentDescription
        }
    }

    private fun setOwghat(jdn: Long, isToday: Boolean) {
        if (coordinate == null) return

        val civilDate = CivilDate(jdn)
        calendar.set(civilDate.year, civilDate.month - 1, civilDate.dayOfMonth)
        val date = calendar.time

        val prayTimes = PrayTimesCalculator.calculate(
            getCalculationMethod(), date, coordinate
        )
        (owghatBinding?.timesRecyclerView?.adapter as? TimeItemAdapter?)?.run {
            this.prayTimes = prayTimes
        }
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
            if (isToday && mainBinding.tabsViewPager.currentItem == OWGHAT_TAB) startAnimate()
        }
    }

    private fun onOwghatClick() {
        (owghatBinding?.timesRecyclerView?.adapter as? TimeItemAdapter?)?.run {
            isExpanded = !isExpanded
            owghatBinding?.moreOwghat?.setImageResource(
                if (isExpanded) R.drawable.ic_keyboard_arrow_up
                else R.drawable.ic_keyboard_arrow_down
            )
        }

        if (mainBinding.calendarPager.lastSelectedJdn == -1L)
            mainBinding.calendarPager.lastSelectedJdn = getTodayJdn()
    }

    private fun bringTodayYearMonth() {
        mainBinding.calendarPager.lastSelectedJdn = -1
        mainBinding.calendarPager.resetMonthFragments()
        mainBinding.calendarPager.gotoOffset(0)

        selectDay(getTodayJdn())
    }

    private fun bringDate(jdn: Long) {
        val viewPagerPosition = calculateViewPagerPositionFromJdn(jdn)
        mainBinding.calendarPager.gotoOffset(viewPagerPosition)

        selectDay(jdn)
        mainBinding.calendarPager.lastSelectedJdn = jdn
        mainBinding.calendarPager
            .updateMonthFragments(viewPagerPosition, false)

        if (isTalkBackEnabled && jdn != getTodayJdn()) Snackbar.make(
            mainBinding.root,
            getA11yDaySummary(
                mainActivity, jdn, false, emptyEventsStore(),
                withZodiac = true, withOtherCalendars = true, withTitle = true
            ),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun calculateViewPagerPositionFromJdn(jdn: Long): Int {
        val mainCalendar = mainCalendar
        val today = getTodayOfCalendar(mainCalendar)
        val date = getDateFromJdnOfCalendar(mainCalendar, jdn)
        return (today.year - date.year) * 12 + today.month - date.month
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.calendar_menu_buttons, menu)

        searchView = (menu.findItem(R.id.search).actionView as? SearchView?)?.apply {
            setOnSearchClickListener {
                // Remove search edit view below bar
                findViewById<View?>(androidx.appcompat.R.id.search_plate)?.setBackgroundColor(
                    Color.TRANSPARENT
                )

                (findViewById<View?>(
                    androidx.appcompat.R.id.search_src_text
                ) as? SearchView.SearchAutoComplete?)?.apply {
                    setHint(R.string.search_in_events)
                    setAdapter(
                        ArrayAdapter<CalendarEvent<*>>(
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
            R.id.go_to -> SelectDayDialog.newInstance(mainBinding.calendarPager.lastSelectedJdn).apply {
                onSuccess = fun(jdn: Long) { bringDate(jdn) }
            }.show(
                childFragmentManager,
                SelectDayDialog::class.java.name
            )
            R.id.add_event -> {
                if (mainBinding.calendarPager.lastSelectedJdn == -1L)
                    mainBinding.calendarPager.lastSelectedJdn = getTodayJdn()

                addEventOnCalendar(mainBinding.calendarPager.lastSelectedJdn)
            }
            R.id.shift_work -> ShiftWorkDialog.newInstance(mainBinding.calendarPager.lastSelectedJdn).apply {
                onSuccess = fun() {
                    updateStoredPreference(mainActivity)
                    mainBinding.calendarPager.updateMonthFragments(
                        calculateViewPagerPositionFromJdn(mainBinding.calendarPager.lastSelectedJdn),
                        true
                    )
                    mainActivity.restartActivity()
                }
            }.show(
                childFragmentManager,
                ShiftWorkDialog::class.java.name
            )
            R.id.month_overview -> {
                val visibleMonthJdn = CalendarPager.getDateFromOffset(
                    mainCalendar,
                    CalendarPager.applyOffset(mainBinding.calendarPager.getCurrentSelection())
                ).toJdn()
                MonthOverviewDialog.newInstance(visibleMonthJdn).show(
                    childFragmentManager, MonthOverviewDialog::class.java.name
                )
            }
            else -> {
            }
        }
        return true
    }

    fun closeSearch() = searchView?.run {
        if (!isIconified) {
            onActionViewCollapsed()
            return true
        } else false
    } ?: false

    companion object {
        private const val CALENDARS_TAB = 0
        private const val EVENTS_TAB = 1
        private const val OWGHAT_TAB = 2
    }
}