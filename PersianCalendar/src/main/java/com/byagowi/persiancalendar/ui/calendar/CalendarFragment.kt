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
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.*
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.byagowi.persiancalendar.*
import io.github.persiancalendar.calendar.CivilDate
import com.byagowi.persiancalendar.databinding.EventsTabContentBinding
import com.byagowi.persiancalendar.databinding.FragmentCalendarBinding
import com.byagowi.persiancalendar.databinding.OwghatTabContentBinding
import com.byagowi.persiancalendar.di.AppDependency
import com.byagowi.persiancalendar.di.MainActivityDependency
import com.byagowi.persiancalendar.entities.AbstractEvent
import com.byagowi.persiancalendar.entities.DeviceCalendarEvent
import com.byagowi.persiancalendar.praytimes.Coordinate
import com.byagowi.persiancalendar.praytimes.PrayTimesCalculator
import com.byagowi.persiancalendar.ui.calendar.calendar.CalendarAdapter
import com.byagowi.persiancalendar.ui.calendar.dialogs.MonthOverviewDialog
import com.byagowi.persiancalendar.ui.calendar.dialogs.SelectDayDialog
import com.byagowi.persiancalendar.ui.calendar.dialogs.ShiftWorkDialog
import com.byagowi.persiancalendar.ui.calendar.month.MonthFragment
import com.byagowi.persiancalendar.ui.calendar.times.TimeItemAdapter
import com.byagowi.persiancalendar.ui.shared.CalendarsView
import com.byagowi.persiancalendar.utils.*
import com.cepmuvakkit.times.posAlgo.SunMoonPosition
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.android.support.DaggerFragment
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
    private var mSearchAutoComplete: SearchView.SearchAutoComplete? = null
    private lateinit var mCalendarAdapterHelper: CalendarAdapter.CalendarAdapterHelper
    private val mChangeListener = object : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            sendUpdateCommandToMonthFragments(mCalendarAdapterHelper.positionToOffset(position), false)
            mMainBinding.todayButton.show()
            //            mMainBinding.swipeRefresh.setEnabled(true);
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val context = mainActivityDependency.mainActivity

        setHasOptionsMenu(true)

        mMainBinding = FragmentCalendarBinding.inflate(inflater, container, false)
        viewPagerPosition = 0

        val titles = ArrayList<String>()
        val tabs = ArrayList<View>()

        titles.add(getString(R.string.calendar))
        mCalendarsView = CalendarsView(context).apply {
            setOnCalendarsViewExpandListener {
                mMainBinding.tabsViewPager.measureCurrentView(this@apply)
            }
            setOnShowHideTodayButton { show ->
                if (show) {
                    mMainBinding.todayButton.show()
                    //                mMainBinding.swipeRefresh.setEnabled(true);
                } else {
                    mMainBinding.todayButton.hide()
                    //                mMainBinding.swipeRefresh.setEnabled(false);
                }
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
                root.setOnClickListener { onOwghatClick(it) }
                cityName.run {
                    setOnClickListener { onOwghatClick(it) }
                    // Easter egg to test AthanActivity
                    setOnLongClickListener {
                        startAthan(context, "FAJR")
                        true
                    }
                    val cityName = getCityName(context, false)
                    if (!TextUtils.isEmpty(cityName)) {
                        text = cityName
                    }
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
            tabsViewPager.adapter = TabsViewPager.TabsAdapter(childFragmentManager,
                    appDependency, tabs, titles)
            tabLayout.setupWithViewPager(tabsViewPager)
            mCalendarAdapterHelper = CalendarAdapter.CalendarAdapterHelper(isRTL(context))
            calendarViewPager.adapter = CalendarAdapter(childFragmentManager,
                    mCalendarAdapterHelper)
            mCalendarAdapterHelper.gotoOffset(calendarViewPager, 0)

            calendarViewPager.addOnPageChangeListener(mChangeListener)

            var lastTab = appDependency.sharedPreferences
                    .getInt(LAST_CHOSEN_TAB_KEY, CALENDARS_TAB)
            if (lastTab >= tabs.size)
                lastTab = CALENDARS_TAB

            tabsViewPager.setCurrentItem(lastTab, false)
        }

        val today = getTodayOfCalendar(getMainCalendar())
        mainActivityDependency.mainActivity.setTitleAndSubtitle(getMonthName(today),
                formatNumber(today.year))

        mCalendarFragmentModel.selectedDayLiveData.observe(this, Observer { jdn ->
            mLastSelectedJdn = jdn
            mCalendarsView.showCalendars(mLastSelectedJdn, getMainCalendar(), getEnabledCalendarTypes())
            val isToday = getTodayJdn() == mLastSelectedJdn
            setOwghat(jdn, isToday)
            showEvent(jdn, isToday)
        })

        //        mMainBinding.swipeRefresh.setEnabled(false);
        //        mMainBinding.swipeRefresh.setOnRefreshListener(() -> {
        //            bringTodayYearMonth();
        //            mMainBinding.swipeRefresh.setRefreshing(false);
        //        });

        return mMainBinding.root
    }

    fun changeMonth(position: Int) = mMainBinding.calendarViewPager.setCurrentItem(mMainBinding.calendarViewPager.currentItem + position, true)

    fun addEventOnCalendar(jdn: Long) {
        val activity = mainActivityDependency.mainActivity

        val civil = CivilDate(jdn)
        val time = Calendar.getInstance()
        time.set(civil.year, civil.month - 1, civil.dayOfMonth)
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            askForCalendarPermission(activity)
        } else {
            try {
                startActivityForResult(
                        Intent(Intent.ACTION_INSERT)
                                .setData(CalendarContract.Events.CONTENT_URI)
                                .putExtra(CalendarContract.Events.DESCRIPTION, dayTitleSummary(
                                        getDateFromJdnOfCalendar(getMainCalendar(), jdn)))
                                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                                        time.timeInMillis)
                                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                                        time.timeInMillis)
                                .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true),
                        CALENDAR_EVENT_ADD_MODIFY_REQUEST_CODE)
            } catch (e: Exception) {
                createAndShowShortSnackbar(view, R.string.device_calendar_does_not_support)
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val activity = mainActivityDependency.mainActivity

        if (requestCode == CALENDAR_EVENT_ADD_MODIFY_REQUEST_CODE) {
            if (isShowDeviceCalendarEvents()) {
                sendUpdateCommandToMonthFragments(calculateViewPagerPositionFromJdn(mLastSelectedJdn), true)
            } else {
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                    askForCalendarPermission(activity)
                } else {
                    toggleShowDeviceCalendarOnPreference(activity, true)
                    activity.restartActivity()
                }
            }
        }
    }

    private fun sendUpdateCommandToMonthFragments(toWhich: Int, addOrModify: Boolean) {
        ViewModelProviders.of(this).get(CalendarFragmentModel::class.java).monthFragmentsUpdate(
                CalendarFragmentModel.MonthFragmentUpdateCommand(toWhich, addOrModify, mLastSelectedJdn))
    }

    private fun formatClickableEventTitle(event: DeviceCalendarEvent): SpannableString {
        val title = Utils.formatDeviceCalendarEventTitle(event)
        val ss = SpannableString(title)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                try {
                    startActivityForResult(Intent(Intent.ACTION_VIEW)
                            .setData(ContentUris.withAppendedId(
                                    CalendarContract.Events.CONTENT_URI, event.id.toLong())),
                            CALENDAR_EVENT_ADD_MODIFY_REQUEST_CODE)
                } catch (e: Exception) { // Should be ActivityNotFoundException but we don't care really
                    createAndShowShortSnackbar(textView, R.string.device_calendar_does_not_support)
                }

            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                val color = event.color
                if (!TextUtils.isEmpty(color)) {
                    try {
                        ds.color = Integer.parseInt(color)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        ss.setSpan(clickableSpan, 0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return ss
    }

    private fun getDeviceEventsTitle(dayEvents: List<AbstractEvent<*>>): SpannableStringBuilder {
        val titles = SpannableStringBuilder()
        var first = true

        for (event in dayEvents)
            if (event is DeviceCalendarEvent) {
                if (first)
                    first = false
                else
                    titles.append("\n")

                titles.append(formatClickableEventTitle(event))
            }

        return titles
    }

    private fun showEvent(jdn: Long, isToday: Boolean) {
        mEventsBinding.run {
            shiftWorkTitle.text = Utils.getShiftWorkTitle(jdn, false)
            val events = getEvents(jdn,
                    readDayDeviceEvents(mainActivityDependency.mainActivity, jdn))
            val holidays = Utils.getEventsTitle(events, true, false, false, false)
            val nonHolidays = Utils.getEventsTitle(events, false, false, false, false)
            val deviceEvents = getDeviceEventsTitle(events)
            val contentDescription = StringBuilder()

            eventMessage.visibility = View.GONE
            noEvent.visibility = View.VISIBLE

            if (!TextUtils.isEmpty(holidays)) {
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

            if (!TextUtils.isEmpty(nonHolidays)) {
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
                    .getStringSet(PREF_HOLIDAY_TYPES, HashSet())
            if (enabledTypes == null || enabledTypes.size == 0) {
                noEvent.visibility = View.GONE
                if (!TextUtils.isEmpty(messageToShow))
                    messageToShow.append("\n")

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

            if (!TextUtils.isEmpty(messageToShow)) {
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

        val prayTimes = PrayTimesCalculator.calculate(getCalculationMethod(),
                date, mCoordinate)
        val adapter = mOwghatBinding.timesRecyclerView.adapter
        if (adapter is TimeItemAdapter)
            adapter.setTimes(prayTimes)

        var moonPhase = 1.0
        try {
            mCoordinate?.run {
                moonPhase = SunMoonPosition(getTodayJdn().toDouble(), latitude,
                        longitude, 0.0, 0.0).moonPhase
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mOwghatBinding.sunView.run {
            setSunriseSunsetMoonPhase(prayTimes, moonPhase)
            if (isToday) {
                visibility = View.VISIBLE
                if (mMainBinding.tabsViewPager.currentItem == OWGHAT_TAB)
                    startAnimate(true)
            } else
                visibility = View.GONE
        }
    }

    private fun onOwghatClick(v: View) {
        val adapter = mOwghatBinding.timesRecyclerView.adapter
        if (adapter is TimeItemAdapter) {
            val expanded = !adapter.isExpanded
            adapter.isExpanded = expanded
            mOwghatBinding.moreOwghat.setImageResource(if (expanded)
                R.drawable.ic_keyboard_arrow_up
            else
                R.drawable.ic_keyboard_arrow_down)
        }
        mMainBinding.tabsViewPager.measureCurrentView(mOwghatBinding.root)

        if (mLastSelectedJdn == -1L)
            mLastSelectedJdn = getTodayJdn()
    }

    private fun bringTodayYearMonth() {
        mLastSelectedJdn = -1
        sendUpdateCommandToMonthFragments(BROADCAST_TO_MONTH_FRAGMENT_RESET_DAY, false)

        mCalendarAdapterHelper.gotoOffset(mMainBinding.calendarViewPager, 0)

        mCalendarFragmentModel.selectDay(getTodayJdn())
    }

    fun afterShiftWorkChange() = context?.run {
        Utils.updateStoredPreference(this)
        sendUpdateCommandToMonthFragments(calculateViewPagerPositionFromJdn(mLastSelectedJdn), true)
    }

    fun bringDate(jdn: Long) {
        viewPagerPosition = calculateViewPagerPositionFromJdn(jdn)
        mCalendarAdapterHelper.gotoOffset(mMainBinding.calendarViewPager, viewPagerPosition)

        mCalendarFragmentModel.selectDay(jdn)
        mLastSelectedJdn = jdn
        sendUpdateCommandToMonthFragments(viewPagerPosition, false)

        if (isTalkBackEnabled()) {
            val todayJdn = getTodayJdn()
            if (jdn != todayJdn) {
                createAndShowShortSnackbar(view,
                        getA11yDaySummary(mainActivityDependency.mainActivity, jdn,
                                false, null, withZodiac = true,
                            withOtherCalendars = true, withTitle = true
                        ))
            }
        }
    }

    private fun calculateViewPagerPositionFromJdn(jdn: Long): Int {
        val mainCalendar = getMainCalendar()
        val today = getTodayOfCalendar(mainCalendar)
        val date = getDateFromJdnOfCalendar(mainCalendar, jdn)
        return (today.year - date.year) * 12 + today.month - date.month
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.calendar_menu_buttons, menu)

        mSearchView = menu.findItem(R.id.search).actionView as SearchView
        mSearchView?.run {
            setOnSearchClickListener {
                mSearchAutoComplete?.onItemClickListener = null

                findViewById<View>(androidx.appcompat.R.id.search_plate)
                setBackgroundColor(Color.TRANSPARENT)

                val context = context ?: return@setOnSearchClickListener

                mSearchAutoComplete = findViewById(androidx.appcompat.R.id.search_src_text)
                mSearchAutoComplete?.setHint(R.string.search_in_events)

                val eventsAdapter = ArrayAdapter<AbstractEvent<*>>(context,
                        R.layout.suggestion, android.R.id.text1)
                eventsAdapter.addAll(getAllEnabledEvents())
                eventsAdapter.addAll(getAllEnabledAppointments(context))
                mSearchAutoComplete?.setAdapter(eventsAdapter)
                mSearchAutoComplete?.setOnItemClickListener { parent, _, position, _ ->
                    val ev = parent.getItemAtPosition(position) as AbstractEvent<*>
                    val date = ev.date
                    val type = getCalendarTypeFromDate(date)
                    val today = getTodayOfCalendar(type)
                    var year = date.year
                    if (year == -1) {
                        year = today.year + if (date.month < today.month) 1 else 0
                    }
                    bringDate(getDateOfCalendar(type, year, date.month, date.dayOfMonth).toJdn())
                    onActionViewCollapsed()
                }
            }
        }
    }

    private fun destroySearchView() {
        mSearchView = mSearchView?.run {
            setOnSearchClickListener(null)
            null
        }

        mSearchAutoComplete = mSearchAutoComplete?.run {
            setAdapter(null)
            onItemClickListener = null
            null
        }
    }

    override fun onDestroyOptionsMenu() {
        destroySearchView()
        super.onDestroyOptionsMenu()
    }

    override fun onDestroy() {
        destroySearchView()
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.go_to -> SelectDayDialog.newInstance(mLastSelectedJdn).show(childFragmentManager,
                    SelectDayDialog::class.java.name)
            R.id.add_event -> {
                if (mLastSelectedJdn == -1L)
                    mLastSelectedJdn = getTodayJdn()

                addEventOnCalendar(mLastSelectedJdn)
            }
            R.id.shift_work -> ShiftWorkDialog.newInstance(mLastSelectedJdn).show(childFragmentManager,
                    ShiftWorkDialog::class.java.name)
            R.id.month_overview -> {
                val visibleMonthJdn = MonthFragment.getDateFromOffset(getMainCalendar(),
                        mCalendarAdapterHelper.positionToOffset(mMainBinding.calendarViewPager.currentItem)).toJdn()
                MonthOverviewDialog.newInstance(visibleMonthJdn).show(childFragmentManager,
                        MonthOverviewDialog::class.java.name)
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