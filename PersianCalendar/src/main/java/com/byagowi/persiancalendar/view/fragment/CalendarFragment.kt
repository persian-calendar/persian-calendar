package com.byagowi.persiancalendar.view.fragment

import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.CalendarContract
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.ViewPager
import calendar.DateConverter
import com.byagowi.persiancalendar.Constants
import com.byagowi.persiancalendar.Constants.CALENDAR_EVENT_ADD_MODIFY_REQUEST_CODE
import com.byagowi.persiancalendar.Constants.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.adapter.CalendarAdapter
import com.byagowi.persiancalendar.databinding.FragmentCalendarBinding
import com.byagowi.persiancalendar.entity.*
import com.byagowi.persiancalendar.util.CalendarUtils
import com.byagowi.persiancalendar.util.UIUtils
import com.byagowi.persiancalendar.util.Utils
import com.byagowi.persiancalendar.view.activity.MainActivity
import com.byagowi.persiancalendar.view.dialog.SelectDayDialog
import com.github.praytimes.Clock
import com.github.praytimes.Coordinate
import com.github.praytimes.PrayTime
import com.github.praytimes.PrayTimesCalculator
import java.util.*

class CalendarFragment : Fragment(), View.OnClickListener {
  private val calendar = Calendar.getInstance()
  private var coordinate: Coordinate? = null
  var viewPagerPosition: Int = 0
    private set
  private lateinit var binding: FragmentCalendarBinding

  var firstTime = true

  internal var changeListener: ViewPager.OnPageChangeListener = object : ViewPager.SimpleOnPageChangeListener() {
    override fun onPageSelected(position: Int) {
      binding.calendarsCard.today.visibility = View.VISIBLE
      binding.calendarsCard.todayIcon.visibility = View.VISIBLE

      val ctx = context ?: return
      LocalBroadcastManager.getInstance(ctx).sendBroadcast(
          Intent(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT)
              .putExtra(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT,
                  CalendarAdapter.positionToOffset(position))
              .putExtra(Constants.BROADCAST_FIELD_SELECT_DAY_JDN, lastSelectedJdn))
    }

  }

  private var lastSelectedJdn: Long = -1

  private var isOwghatOpen = false

  private var mSearchView: SearchView? = null

  @Nullable
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    val ctx = context ?: return null
    setHasOptionsMenu(true)

    binding = DataBindingUtil.inflate(inflater, R.layout.fragment_calendar, container,
        false)
    viewPagerPosition = 0

    binding.calendarsCard.today.visibility = View.GONE
    binding.calendarsCard.todayIcon.visibility = View.GONE

    coordinate = Utils.getCoordinate(ctx)
    binding.calendarPager.adapter = CalendarAdapter(childFragmentManager, UIUtils.isRTL(ctx))
    CalendarAdapter.gotoOffset(binding.calendarPager, 0)

    binding.calendarPager.addOnPageChangeListener(changeListener)

    binding.owghat.setOnClickListener(this)
    binding.calendarsCard.today.setOnClickListener(this)
    binding.calendarsCard.todayIcon.setOnClickListener(this)
    binding.calendarsCard.gregorianDate.setOnClickListener(this)
    binding.calendarsCard.gregorianDateDay.setOnClickListener(this)
    binding.calendarsCard.gregorianDateLinear.setOnClickListener(this)
    binding.calendarsCard.islamicDate.setOnClickListener(this)
    binding.calendarsCard.islamicDateDay.setOnClickListener(this)
    binding.calendarsCard.islamicDateLinear.setOnClickListener(this)
    binding.calendarsCard.shamsiDate.setOnClickListener(this)
    binding.calendarsCard.shamsiDateDay.setOnClickListener(this)
    binding.calendarsCard.shamsiDateLinear.setOnClickListener(this)

    binding.calendarsCard.calendarsCard.setOnClickListener(this)

    binding.warnUserIcon.visibility = View.GONE
    binding.calendarsCard.gregorianDateLinear.visibility = View.GONE
    binding.calendarsCard.islamicDateLinear.visibility = View.GONE
    binding.calendarsCard.shamsiDateLinear.visibility = View.GONE

    val cityName = Utils.getCityName(ctx, false)
    if (!TextUtils.isEmpty(cityName)) {
      binding.owghatText.append(" ($cityName)")
    }

    // This will immediately be replaced by the same functionality on fragment but is here to
    // make sure enough space is dedicated to actionbar's title and subtitle, kinda hack anyway
    val today = CalendarUtils.getTodayOfCalendar(Utils.mainCalendar)
    val localActivity = activity
    if (localActivity != null) {
      UIUtils.setActivityTitleAndSubtitle(localActivity, CalendarUtils.getMonthName(today),
          Utils.formatNumber(today.year))
    }

    // Easter egg to test AthanActivity
    binding.owghatIcon.setOnLongClickListener {
      Utils.startAthan(ctx, "FAJR")
      true
    }

    return binding.root
  }

  fun changeMonth(position: Int) {
    binding.calendarPager.setCurrentItem(binding.calendarPager.currentItem + position, true)
  }

  fun selectDay(jdn: Long) {
    lastSelectedJdn = jdn
    val isToday = CalendarUtils.todayJdn == jdn
    val ctx = context
    if (ctx != null) {
      UIUtils.fillCalendarsCard(ctx, jdn, binding.calendarsCard, isToday)
    }
    setOwghat(jdn, isToday)
    showEvent(jdn)
  }

  fun addEventOnCalendar(jdn: Long) {
    val civil = DateConverter.jdnToCivil(jdn)
    val time = Calendar.getInstance()
    time.set(civil.year, civil.month - 1, civil.dayOfMonth)

    try {
      startActivityForResult(
          Intent(Intent.ACTION_INSERT)
              .setData(CalendarContract.Events.CONTENT_URI)
              .putExtra(CalendarContract.Events.DESCRIPTION, CalendarUtils.dayTitleSummary(
                  CalendarUtils.getDateFromJdnOfCalendar(Utils.mainCalendar, jdn)))
              .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                  time.timeInMillis)
              .putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                  time.timeInMillis)
              .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true),
          CALENDAR_EVENT_ADD_MODIFY_REQUEST_CODE)
    } catch (e: Exception) {
      Toast.makeText(context, R.string.device_calendar_does_not_support, Toast.LENGTH_SHORT).show()
    }

  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == CALENDAR_EVENT_ADD_MODIFY_REQUEST_CODE) {
      val ctx = context
      if (ctx != null) {
        if (Utils.isShowDeviceCalendarEvents) {
          Utils.initUtils(ctx)
        } else {
          Toast.makeText(ctx, R.string.enable_device_calendar, Toast.LENGTH_LONG).show()
        }
      }

      if (lastSelectedJdn == -1L)
        lastSelectedJdn = CalendarUtils.todayJdn
      selectDay(lastSelectedJdn)
    }
  }

  private fun formatClickableEventTitle(event: DeviceCalendarEvent): SpannableString {
    val title = UIUtils.formatDeviceCalendarEventTitle(event)
    val ss = SpannableString(title)
    val clickableSpan = object : ClickableSpan() {
      override fun onClick(textView: View) {
        try {
          startActivityForResult(Intent(Intent.ACTION_VIEW)
              .setData(ContentUris.withAppendedId(
                  CalendarContract.Events.CONTENT_URI, event.id.toLong())),
              CALENDAR_EVENT_ADD_MODIFY_REQUEST_CODE)
        } catch (e: Exception) { // Should be ActivityNotFoundException but we don't care really
          Toast.makeText(context, R.string.device_calendar_does_not_support, Toast.LENGTH_SHORT).show()
        }

      }

      override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        try {
          ds.color = Integer.parseInt(event.color)
        } catch (e: Exception) {
          e.printStackTrace()
        }

      }
    }
    ss.setSpan(clickableSpan, 0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    return ss
  }

  private fun getDeviceEventsTitle(dayEvents: List<AbstractEvent>): SpannableStringBuilder {
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

  private fun showEvent(jdn: Long) {
    val events = Utils.getEvents(jdn)
    val holidays = Utils.getEventsTitle(events, true, false, false, false)
    val nonHolidays = Utils.getEventsTitle(events, false, false, false, false)
    val deviceEvents = getDeviceEventsTitle(events)

    binding.cardEvent.visibility = View.GONE
    binding.holidayTitle.visibility = View.GONE
    binding.deviceEventTitle.visibility = View.GONE
    binding.eventTitle.visibility = View.GONE
    binding.eventMessage.visibility = View.GONE

    if (!TextUtils.isEmpty(holidays)) {
      binding.holidayTitle.text = holidays
      binding.holidayTitle.visibility = View.VISIBLE
      binding.cardEvent.visibility = View.VISIBLE
    }

    if (deviceEvents.length != 0) {
      binding.deviceEventTitle.text = deviceEvents
      binding.deviceEventTitle.movementMethod = LinkMovementMethod.getInstance()

      binding.deviceEventTitle.visibility = View.VISIBLE
      binding.cardEvent.visibility = View.VISIBLE
    }

    if (!TextUtils.isEmpty(nonHolidays)) {
      binding.eventTitle.text = nonHolidays

      binding.eventTitle.visibility = View.VISIBLE
      binding.cardEvent.visibility = View.VISIBLE
    }

    val messageToShow = SpannableStringBuilder()
    if (CalendarUtils.persianToday.year > Utils.maxSupportedYear) {
      val title = getString(R.string.shouldBeUpdated)
      val ss = SpannableString(title)
      val clickableSpan = object : ClickableSpan() {
        override fun onClick(textView: View) {
          try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.byagowi.persiancalendar")))
          } catch (e: ActivityNotFoundException) { // Should be ActivityNotFoundException but we don't care really
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.byagowi.p+ersiancalendar")))
          }

        }
      }
      ss.setSpan(clickableSpan, 0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
      messageToShow.append(ss)
    }

    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val enabledTypes = prefs.getStringSet(PREF_HOLIDAY_TYPES, HashSet())
    if (enabledTypes.size == 0) {
      if (!TextUtils.isEmpty(messageToShow))
        messageToShow.append("\n")

      val title = getString(R.string.warn_if_events_not_set)
      val ss = SpannableString(title)
      val clickableSpan = object : ClickableSpan() {
        override fun onClick(textView: View) {
          (activity as MainActivity).selectItem(MainActivity.PREFERENCE)
        }
      }
      ss.setSpan(clickableSpan, 0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
      messageToShow.append(ss)
    }

    if (!TextUtils.isEmpty(messageToShow)) {
      binding.warnUserIcon.visibility = View.VISIBLE
      binding.eventMessage.text = messageToShow
      binding.eventMessage.movementMethod = LinkMovementMethod.getInstance()

      binding.eventMessage.visibility = View.VISIBLE
      binding.cardEvent.visibility = View.VISIBLE
    }
  }

  private fun setOwghat(jdn: Long, isToday: Boolean) {
    if (coordinate == null) {
      binding.owghat.visibility = View.GONE
      return
    }
    val prayTimesCalculator = PrayTimesCalculator(Utils.getCalculationMethod())

    val civilDate = DateConverter.jdnToCivil(jdn)
    calendar.set(civilDate.year, civilDate.month - 1, civilDate.dayOfMonth)
    val date = calendar.time

    val prayTimes = prayTimesCalculator.calculate(date, coordinate)

    val nullClock = Clock(0, 0)
    binding.imsak.text = UIUtils.getFormattedClock(prayTimes[PrayTime.IMSAK] ?: nullClock)
    val sunriseClock = prayTimes[PrayTime.FAJR] ?: nullClock
    binding.fajr.text = UIUtils.getFormattedClock(sunriseClock)
    binding.sunrise.text = UIUtils.getFormattedClock(prayTimes[PrayTime.SUNRISE] ?: nullClock)
    val midddayClock = prayTimes[PrayTime.DHUHR] ?: nullClock
    binding.dhuhr.text = UIUtils.getFormattedClock(midddayClock)
    binding.asr.text = UIUtils.getFormattedClock(prayTimes[PrayTime.ASR] ?: nullClock)
    binding.sunset.text = UIUtils.getFormattedClock(prayTimes[PrayTime.SUNSET] ?: nullClock)
    val maghribClock = prayTimes[PrayTime.MAGHRIB] ?: nullClock
    binding.maghrib.text = UIUtils.getFormattedClock(maghribClock)
    binding.isgha.text = UIUtils.getFormattedClock(prayTimes[PrayTime.ISHA] ?: nullClock)
    binding.midnight.text = UIUtils.getFormattedClock(prayTimes[PrayTime.MIDNIGHT] ?: nullClock)

    binding.ssv.visibility = View.GONE
    if (isToday) {
      binding.ssv.setSunriseTime(sunriseClock)
      binding.ssv.setMiddayTime(midddayClock)
      binding.ssv.setSunsetTime(maghribClock)

      if (isOwghatOpen) {
        binding.ssv.visibility = View.VISIBLE
        binding.ssv.animate()
      }
    }
  }

  override fun onClick(v: View) {
    val ctx = context ?: return
    when (v.id) {

      R.id.calendars_card -> {
        val isOpenCalendarCommand = binding.calendarsCard.gregorianDateLinear.visibility != View.VISIBLE

        binding.calendarsCard.moreCalendar.setImageResource(if (isOpenCalendarCommand)
          R.drawable.ic_keyboard_arrow_up
        else
          R.drawable.ic_keyboard_arrow_down)
        binding.calendarsCard.gregorianDateLinear.visibility = if (isOpenCalendarCommand) View.VISIBLE else View.GONE
        binding.calendarsCard.islamicDateLinear.visibility = if (isOpenCalendarCommand) View.VISIBLE else View.GONE
        binding.calendarsCard.shamsiDateLinear.visibility = if (isOpenCalendarCommand) View.VISIBLE else View.GONE
      }

      R.id.owghat -> {

        val isOpenOwghatCommand = binding.sunriseLayout.visibility == View.GONE

        binding.moreOwghat.setImageResource(if (isOpenOwghatCommand)
          R.drawable.ic_keyboard_arrow_up
        else
          R.drawable.ic_keyboard_arrow_down)
        binding.imsakLayout.visibility = if (isOpenOwghatCommand) View.VISIBLE else View.GONE
        binding.sunriseLayout.visibility = if (isOpenOwghatCommand) View.VISIBLE else View.GONE
        binding.asrLayout.visibility = if (isOpenOwghatCommand) View.VISIBLE else View.GONE
        binding.sunsetLayout.visibility = if (isOpenOwghatCommand) View.VISIBLE else View.GONE
        binding.ishaLayout.visibility = if (isOpenOwghatCommand) View.VISIBLE else View.GONE
        binding.midnightLayout.visibility = if (isOpenOwghatCommand) View.VISIBLE else View.GONE
        isOwghatOpen = isOpenOwghatCommand

        if (lastSelectedJdn == -1L)
          lastSelectedJdn = CalendarUtils.todayJdn

        if (lastSelectedJdn == CalendarUtils.todayJdn && isOpenOwghatCommand) {
          binding.ssv.visibility = View.VISIBLE
          binding.ssv.startAnimate()
        } else {
          binding.ssv.visibility = View.GONE
        }
      }

      R.id.today, R.id.today_icon -> bringTodayYearMonth()

      R.id.shamsi_date, R.id.shamsi_date_day -> UIUtils.copyToClipboard(ctx, binding.calendarsCard.shamsiDateDay.text.toString() + " " +
          binding.calendarsCard.shamsiDate.text.toString().replace("\n", " "))

      R.id.shamsi_date_linear -> UIUtils.copyToClipboard(ctx, binding.calendarsCard.shamsiDateLinear.text)

      R.id.gregorian_date, R.id.gregorian_date_day -> UIUtils.copyToClipboard(ctx, binding.calendarsCard.gregorianDateDay.text.toString() + " " +
          binding.calendarsCard.gregorianDate.text.toString().replace("\n", " "))

      R.id.gregorian_date_linear -> UIUtils.copyToClipboard(ctx, binding.calendarsCard.gregorianDateLinear.text)

      R.id.islamic_date, R.id.islamic_date_day -> UIUtils.copyToClipboard(ctx, binding.calendarsCard.islamicDateDay.text.toString() + " " +
          binding.calendarsCard.islamicDate.text.toString().replace("\n", " "))

      R.id.islamic_date_linear -> UIUtils.copyToClipboard(ctx, binding.calendarsCard.islamicDateLinear.text)
    }
  }

  private fun bringTodayYearMonth() {
    lastSelectedJdn = -1
    val ctx = context
    if (ctx != null) {
      LocalBroadcastManager.getInstance(ctx).sendBroadcast(
          Intent(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT)
              .putExtra(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT,
                  Constants.BROADCAST_TO_MONTH_FRAGMENT_RESET_DAY)
              .putExtra(Constants.BROADCAST_FIELD_SELECT_DAY_JDN, -1))
    }

    CalendarAdapter.gotoOffset(binding.calendarPager, 0)

    selectDay(CalendarUtils.todayJdn)
  }

  fun bringDate(jdn: Long) {
    val mainCalendar = Utils.mainCalendar
    val today = CalendarUtils.getTodayOfCalendar(mainCalendar)
    val date = CalendarUtils.getDateFromJdnOfCalendar(mainCalendar, jdn)
    viewPagerPosition = (today.year - date.year) * 12 + today.month - date.month
    CalendarAdapter.gotoOffset(binding.calendarPager, viewPagerPosition)

    selectDay(jdn)

    val ctx = context
    if (ctx != null) {
      LocalBroadcastManager.getInstance(ctx).sendBroadcast(
          Intent(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT)
              .putExtra(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT, viewPagerPosition)
              .putExtra(Constants.BROADCAST_FIELD_SELECT_DAY_JDN, jdn))
    }
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    super.onCreateOptionsMenu(menu, inflater)
    menu.clear()
    inflater.inflate(R.menu.calendar_menu_button, menu)

    mSearchView = menu.findItem(R.id.search).actionView as SearchView?
    mSearchView?.setOnSearchClickListener {
      val searchView = mSearchView
      val searchAutoComplete = searchView?.findViewById<SearchView.SearchAutoComplete>(androidx.appcompat.R.id.search_src_text)
      val searchManager = context?.getSystemService(Context.SEARCH_SERVICE) as SearchManager?
      if (searchManager != null && searchView != null && searchAutoComplete != null) {
        searchAutoComplete.setHint(R.string.search_in_events)
        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
        searchAutoComplete.setAdapter(ArrayAdapter(context,
            R.layout.suggestion, android.R.id.text1, Utils.allEnabledEventsTitles))
        searchAutoComplete.setOnItemClickListener { parent, _, position, _ ->
          val ev = Utils.allEnabledEvents[Utils.allEnabledEventsTitles.indexOf(
              parent.getItemAtPosition(position) as String)]

          if (ev is PersianCalendarEvent) {
            val todayPersian = CalendarUtils.persianToday
            val date = ev.date
            var year = date.year
            if (year == -1) {
              year = todayPersian.year + if (date.month < todayPersian.month) 1 else 0
            }
            bringDate(DateConverter.persianToJdn(year, date.month, date.dayOfMonth))
          } else if (ev is IslamicCalendarEvent) {
            val todayIslamic = CalendarUtils.islamicToday
            val date = ev.date
            var year = date.year
            if (year == -1) {
              year = todayIslamic.year + if (date.month < todayIslamic.month) 1 else 0
            }
            bringDate(DateConverter.islamicToJdn(year, date.month, date.dayOfMonth))
          } else if (ev is GregorianCalendarEvent) {
            val todayCivil = CalendarUtils.gregorianToday
            val date = ev.date
            var year = date.year
            if (year == -1) {
              year = todayCivil.year + if (date.month < todayCivil.month) 1 else 0
            }
            bringDate(DateConverter.civilToJdn(year.toLong(), date.month.toLong(), date.dayOfMonth.toLong()))
          } else if (ev is DeviceCalendarEvent) {
            val todayCivil = CalendarUtils.gregorianToday
            val date = ev.civilDate
            var year = date.year
            if (year == -1) {
              year = todayCivil.year + if (date.month < todayCivil.month) 1 else 0
            }
            bringDate(DateConverter.civilToJdn(year.toLong(), date.month.toLong(), date.dayOfMonth.toLong()))
          }
          searchView.onActionViewCollapsed()
        }
      }
    }
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    when (item?.itemId) {
      R.id.go_to -> SelectDayDialog().show(childFragmentManager,
          SelectDayDialog::class.java.name)
      R.id.add_event -> {
        if (lastSelectedJdn == -1L)
          lastSelectedJdn = CalendarUtils.todayJdn

        addEventOnCalendar(lastSelectedJdn)
      }
      else -> {
      }
    }
    return true
  }

  fun closeSearch(): Boolean {
    val searchView = mSearchView
    if (searchView != null && !searchView.isIconified) {
      searchView.onActionViewCollapsed()
      return true
    }
    return false
  }
}
