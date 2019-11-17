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
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.databinding.EventsTabContentBinding
import com.byagowi.persiancalendar.databinding.FragmentCalendarBinding
import com.byagowi.persiancalendar.databinding.OwghatTabContentBinding
import com.byagowi.persiancalendar.di.AppDependency
import com.byagowi.persiancalendar.di.MainActivityDependency
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.DeviceCalendarEvent
import com.byagowi.persiancalendar.ui.calendar.calendar.CalendarAdapter
import com.byagowi.persiancalendar.ui.calendar.dialogs.MonthOverviewDialog
import com.byagowi.persiancalendar.ui.calendar.dialogs.SelectDayDialog
import com.byagowi.persiancalendar.ui.calendar.dialogs.ShiftWorkDialog
import com.byagowi.persiancalendar.ui.calendar.month.MonthFragment
import com.byagowi.persiancalendar.ui.calendar.times.SunView
import com.byagowi.persiancalendar.ui.calendar.times.TimeItemAdapter
import com.byagowi.persiancalendar.ui.shared.CalendarsView
import com.byagowi.persiancalendar.utils.*
import com.cepmuvakkit.times.posAlgo.SunMoonPosition
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import dagger.android.support.DaggerFragment
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.praytimes.Coordinate
import io.github.persiancalendar.praytimes.PrayTimesCalculator
import java.util.*
import javax.inject.Inject

class CalendarFragment : DaggerFragment() {

    @Inject
    lateinit var appDependency: AppDependency // same object from App
    @Inject
    lateinit var mainActivityDependency: MainActivityDependency // same object from MainActivity

    private val mCalendarFragmentModel by viewModels<CalendarFragmentModel>()
    private val mCalendar = Calendar.getInstance()
    private var mCoordinate: Coordinate? = null
    var viewPagerPosition: Int = 0
        private set
    private lateinit var mMainBinding: FragmentCalendarBinding
    private lateinit var mCalendarsView: CalendarsView
    private lateinit var mOwghatBinding: OwghatTabContentBinding
    private lateinit var mEventsBinding: EventsTabContentBinding
    private var mLastSelectedJdn: Long = -1
    private var mSearchView: SearchView? = null

    fun onDaySelected(position: Int) {
        sendUpdateCommandToMonthFragments(position, false)
        if (position != 0) mMainBinding.todayButton.show()
    }

    class TabFragment : Fragment() {
        @get:JvmName("getView_")
        var view: View? = null // Don't turn it to lateinit till more investigations
        private var position = 0

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?, savedInstanceState: Bundle?
        ): View? = view

        override fun onResume() {
            super.onResume()
            view?.also {
                (it.findViewById<View?>(R.id.sunView) as? SunView?)?.startAnimate(immediate = false)

                PreferenceManager.getDefaultSharedPreferences(it.context)
                    .edit { putInt(LAST_CHOSEN_TAB_KEY, position) }
            }
        }

        companion object {
            internal fun newInstance(view: View, position: Int) = TabFragment().also {
                it.view = view
                it.position = position
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val context = mainActivityDependency.mainActivity

        setHasOptionsMenu(true)

        mMainBinding = FragmentCalendarBinding.inflate(inflater, container, false)
        viewPagerPosition = 0

        val titles = ArrayList<String>()
        val tabs = ArrayList<View>()

        titles.add(getString(R.string.calendar))
        mCalendarsView = CalendarsView(context).apply {
            showHideTodayButtonCallback = fun (show) {
                if (show) mMainBinding.todayButton.show()
                else mMainBinding.todayButton.hide()
            }
        }
        mMainBinding.todayButton.setOnClickListener { bringTodayYearMonth() }
        tabs.add(mCalendarsView)

        titles.add(getString(R.string.events))
        mEventsBinding = EventsTabContentBinding.inflate(inflater, container, false)
        tabs.add(mEventsBinding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mEventsBinding.eventsContent.layoutTransition = LayoutTransition().apply {
                enableTransitionType(LayoutTransition.CHANGING)
            }
            // Don't do the same for others tabs, it is problematic
        }

        mCoordinate = getCoordinate(context)?.apply {
            titles.add(getString(R.string.owghat))
            mOwghatBinding = OwghatTabContentBinding.inflate(inflater, container, false).apply {
                tabs.add(root)
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
            }
        }

        mMainBinding.run {
            tabsViewPager.adapter = object : FragmentStateAdapter(context) {
                override fun getItemCount() = tabs.size
                override fun createFragment(position: Int) =
                    TabFragment.newInstance(tabs[position], position)
            }
            TabLayoutMediator(tabLayout, tabsViewPager) { tab, position ->
                tab.text = titles[position]
            }.attach()
            calendarViewPager.adapter = CalendarAdapter(this@CalendarFragment)
            CalendarAdapter.gotoOffset(calendarViewPager, 0, false)

            var lastTab = appDependency.sharedPreferences
                .getInt(LAST_CHOSEN_TAB_KEY, CALENDARS_TAB)
            if (lastTab >= tabs.size)
                lastTab = CALENDARS_TAB

            tabsViewPager.setCurrentItem(lastTab, false)
        }

        val today = getTodayOfCalendar(mainCalendar)
        mainActivityDependency.mainActivity.setTitleAndSubtitle(
            getMonthName(today),
            formatNumber(today.year)
        )

        mCalendarFragmentModel.selectedDayLiveData.observe(this, Observer { jdn ->
            mLastSelectedJdn = jdn
            mCalendarsView.showCalendars(
                mLastSelectedJdn,
                mainCalendar,
                getEnabledCalendarTypes()
            )
            val isToday = getTodayJdn() == mLastSelectedJdn
            setOwghat(jdn, isToday)
            showEvent(jdn, isToday)
        })

        return mMainBinding.root
    }

    fun changeMonth(position: Int) = mMainBinding.calendarViewPager.setCurrentItem(
        mMainBinding.calendarViewPager.currentItem + position,
        true
    )

    fun addEventOnCalendar(jdn: Long) {
        val activity = mainActivityDependency.mainActivity

        val civil = CivilDate(jdn)
        val time = Calendar.getInstance()
        time.set(civil.year, civil.month - 1, civil.dayOfMonth)
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            askForCalendarPermission(activity)
        } else {
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
                    mMainBinding.root,
                    R.string.device_calendar_does_not_support,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val activity = mainActivityDependency.mainActivity

        if (requestCode == CALENDAR_EVENT_ADD_MODIFY_REQUEST_CODE) {
            if (isShowDeviceCalendarEvents) {
                sendUpdateCommandToMonthFragments(
                    calculateViewPagerPositionFromJdn(mLastSelectedJdn),
                    true
                )
            } else {
                if (ActivityCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.READ_CALENDAR
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    askForCalendarPermission(activity)
                } else {
                    toggleShowDeviceCalendarOnPreference(activity, true)
                    activity.restartActivity()
                }
            }
        }
    }

    private fun sendUpdateCommandToMonthFragments(toWhich: Int, addOrModify: Boolean) =
        ViewModelProviders.of(this)[CalendarFragmentModel::class.java].monthFragmentsUpdate(
            CalendarFragmentModel.MonthFragmentUpdateCommand(toWhich, addOrModify, mLastSelectedJdn)
        )

    private fun formatClickableEventTitle(event: DeviceCalendarEvent): SpannableString {
        val title = formatDeviceCalendarEventTitle(event)
        return SpannableString(title).apply {
            setSpan(object : ClickableSpan() {
                override fun onClick(textView: View) {
                    try {
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
            }, 0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun getDeviceEventsTitle(dayEvents: List<CalendarEvent<*>>): SpannableStringBuilder {
        val titles = SpannableStringBuilder()
        var first = true

        dayEvents.filterIsInstance<DeviceCalendarEvent>().forEach {
            if (first)
                first = false
            else
                titles.append("\n")

            titles.append(formatClickableEventTitle(it))
        }

        return titles
    }

    private fun showEvent(jdn: Long, isToday: Boolean) {
        mEventsBinding.run {
            shiftWorkTitle.text = getShiftWorkTitle(jdn, false)
            val events = getEvents(
                jdn,
                readDayDeviceEvents(mainActivityDependency.mainActivity, jdn)
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

            val enabledTypes = appDependency.sharedPreferences
                .getStringSet(PREF_HOLIDAY_TYPES, null) ?: emptySet()
            if (enabledTypes.size == 0) {
                noEvent.visibility = View.GONE
                if (messageToShow.isNotEmpty()) messageToShow.append("\n")

                val title = getString(R.string.warn_if_events_not_set)
                val ss = SpannableString(title)
                val clickableSpan = object : ClickableSpan() {
                    override fun onClick(textView: View) {
                        mainActivityDependency.mainActivity.navigateTo(R.id.settings)
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
        if (mCoordinate == null) return

        val civilDate = CivilDate(jdn)
        mCalendar.set(civilDate.year, civilDate.month - 1, civilDate.dayOfMonth)
        val date = mCalendar.time

        val prayTimes = PrayTimesCalculator.calculate(
            getCalculationMethod(),
            date, mCoordinate
        )
        (mOwghatBinding.timesRecyclerView.adapter as? TimeItemAdapter?)?.run {
            mPrayTimes = prayTimes
        }
        mOwghatBinding.sunView.run {
            setSunriseSunsetMoonPhase(prayTimes, try {
                mCoordinate?.run {
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
            if (isToday && mMainBinding.tabsViewPager.currentItem == OWGHAT_TAB)
                startAnimate(immediate = false)
        }
    }

    private fun onOwghatClick() {
        (mOwghatBinding.timesRecyclerView.adapter as? TimeItemAdapter?)?.run {
            isExpanded = !isExpanded
            mOwghatBinding.moreOwghat.setImageResource(
                if (isExpanded)
                    R.drawable.ic_keyboard_arrow_up
                else
                    R.drawable.ic_keyboard_arrow_down
            )
        }

        if (mLastSelectedJdn == -1L)
            mLastSelectedJdn = getTodayJdn()
    }

    private fun bringTodayYearMonth() {
        mLastSelectedJdn = -1
        sendUpdateCommandToMonthFragments(BROADCAST_TO_MONTH_FRAGMENT_RESET_DAY, false)

        CalendarAdapter.gotoOffset(mMainBinding.calendarViewPager, 0)

        mCalendarFragmentModel.selectDay(getTodayJdn())
    }

    fun afterShiftWorkChange() = context?.run {
        updateStoredPreference(this)
        sendUpdateCommandToMonthFragments(calculateViewPagerPositionFromJdn(mLastSelectedJdn), true)
    }

    fun bringDate(jdn: Long) {
        viewPagerPosition = calculateViewPagerPositionFromJdn(jdn)
        CalendarAdapter.gotoOffset(mMainBinding.calendarViewPager, viewPagerPosition)

        mCalendarFragmentModel.selectDay(jdn)
        mLastSelectedJdn = jdn
        sendUpdateCommandToMonthFragments(viewPagerPosition, false)

        if (isTalkBackEnabled) {
            val todayJdn = getTodayJdn()
            if (jdn != todayJdn) {
                Snackbar.make(
                    mMainBinding.root,
                    getA11yDaySummary(
                        mainActivityDependency.mainActivity, jdn,
                        false, emptyMap(), withZodiac = true,
                        withOtherCalendars = true, withTitle = true
                    ),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
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

        mSearchView = (menu.findItem(R.id.search).actionView as? SearchView?)?.apply {
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
                            mainActivityDependency.mainActivity,
                            R.layout.suggestion, android.R.id.text1,
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
            R.id.go_to -> SelectDayDialog.newInstance(mLastSelectedJdn).show(
                childFragmentManager,
                SelectDayDialog::class.java.name
            )
            R.id.add_event -> {
                if (mLastSelectedJdn == -1L)
                    mLastSelectedJdn = getTodayJdn()

                addEventOnCalendar(mLastSelectedJdn)
            }
            R.id.shift_work -> ShiftWorkDialog.newInstance(mLastSelectedJdn).show(
                childFragmentManager,
                ShiftWorkDialog::class.java.name
            )
            R.id.month_overview -> {
                val visibleMonthJdn = MonthFragment.getDateFromOffset(
                    mainCalendar,
                    CalendarAdapter.applyOffset(mMainBinding.calendarViewPager.currentItem)
                ).toJdn()
                MonthOverviewDialog.newInstance(visibleMonthJdn).show(
                    childFragmentManager,
                    MonthOverviewDialog::class.java.name
                )
            }
            else -> {
            }
        }
        return true
    }

    fun closeSearch(): Boolean {
        mSearchView?.run {
            if (!isIconified) {
                onActionViewCollapsed()
                return true
            }
        }
        return false
    }
}