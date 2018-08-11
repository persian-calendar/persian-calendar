package com.byagowi.persiancalendar.util

import android.Manifest
import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import android.provider.CalendarContract
import android.text.TextUtils
import android.util.Log
import android.util.SparseArray
import androidx.annotation.RawRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import calendar.*
import com.byagowi.persiancalendar.Constants
import com.byagowi.persiancalendar.Constants.ALARMS_BASE_ID
import com.byagowi.persiancalendar.Constants.ARABIC_DIGITS
import com.byagowi.persiancalendar.Constants.ARABIC_INDIC_DIGITS
import com.byagowi.persiancalendar.Constants.BROADCAST_ALARM
import com.byagowi.persiancalendar.Constants.BROADCAST_RESTART_APP
import com.byagowi.persiancalendar.Constants.BROADCAST_UPDATE_APP
import com.byagowi.persiancalendar.Constants.DAYS_ICONS
import com.byagowi.persiancalendar.Constants.DAYS_ICONS_AR
import com.byagowi.persiancalendar.Constants.DAYS_ICONS_CKB
import com.byagowi.persiancalendar.Constants.DEFAULT_ALTITUDE
import com.byagowi.persiancalendar.Constants.DEFAULT_APP_LANGUAGE
import com.byagowi.persiancalendar.Constants.DEFAULT_ATHAN_VOLUME
import com.byagowi.persiancalendar.Constants.DEFAULT_CITY
import com.byagowi.persiancalendar.Constants.DEFAULT_IRAN_TIME
import com.byagowi.persiancalendar.Constants.DEFAULT_ISLAMIC_OFFSET
import com.byagowi.persiancalendar.Constants.DEFAULT_LATITUDE
import com.byagowi.persiancalendar.Constants.DEFAULT_LONGITUDE
import com.byagowi.persiancalendar.Constants.DEFAULT_NOTIFICATION_ATHAN
import com.byagowi.persiancalendar.Constants.DEFAULT_NOTIFY_DATE
import com.byagowi.persiancalendar.Constants.DEFAULT_NOTIFY_DATE_LOCK_SCREEN
import com.byagowi.persiancalendar.Constants.DEFAULT_PERSIAN_DIGITS
import com.byagowi.persiancalendar.Constants.DEFAULT_PRAY_TIME_METHOD
import com.byagowi.persiancalendar.Constants.DEFAULT_SELECTED_WIDGET_TEXT_COLOR
import com.byagowi.persiancalendar.Constants.DEFAULT_WIDGET_CLOCK
import com.byagowi.persiancalendar.Constants.DEFAULT_WIDGET_IN_24
import com.byagowi.persiancalendar.Constants.KEY_EXTRA_PRAYER_KEY
import com.byagowi.persiancalendar.Constants.LANG_CKB
import com.byagowi.persiancalendar.Constants.LANG_EN
import com.byagowi.persiancalendar.Constants.LANG_EN_US
import com.byagowi.persiancalendar.Constants.LOAD_APP_ID
import com.byagowi.persiancalendar.Constants.PERSIAN_DIGITS
import com.byagowi.persiancalendar.Constants.PREF_ALTITUDE
import com.byagowi.persiancalendar.Constants.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.Constants.PREF_ATHAN_ALARM
import com.byagowi.persiancalendar.Constants.PREF_ATHAN_GAP
import com.byagowi.persiancalendar.Constants.PREF_ATHAN_URI
import com.byagowi.persiancalendar.Constants.PREF_ATHAN_VOLUME
import com.byagowi.persiancalendar.Constants.PREF_GEOCODED_CITYNAME
import com.byagowi.persiancalendar.Constants.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.Constants.PREF_IRAN_TIME
import com.byagowi.persiancalendar.Constants.PREF_ISLAMIC_OFFSET
import com.byagowi.persiancalendar.Constants.PREF_LATITUDE
import com.byagowi.persiancalendar.Constants.PREF_LONGITUDE
import com.byagowi.persiancalendar.Constants.PREF_NOTIFICATION_ATHAN
import com.byagowi.persiancalendar.Constants.PREF_NOTIFY_DATE
import com.byagowi.persiancalendar.Constants.PREF_NOTIFY_DATE_LOCK_SCREEN
import com.byagowi.persiancalendar.Constants.PREF_PERSIAN_DIGITS
import com.byagowi.persiancalendar.Constants.PREF_PRAY_TIME_METHOD
import com.byagowi.persiancalendar.Constants.PREF_SELECTED_LOCATION
import com.byagowi.persiancalendar.Constants.PREF_SELECTED_WIDGET_TEXT_COLOR
import com.byagowi.persiancalendar.Constants.PREF_SHOW_DEVICE_CALENDAR_EVENTS
import com.byagowi.persiancalendar.Constants.PREF_WIDGET_CLOCK
import com.byagowi.persiancalendar.Constants.PREF_WIDGET_IN_24
import com.byagowi.persiancalendar.Constants.THREE_HOURS_APP_ID
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entity.*
import com.byagowi.persiancalendar.service.ApplicationService
import com.byagowi.persiancalendar.service.AthanNotification
import com.byagowi.persiancalendar.service.BroadcastReceivers
import com.byagowi.persiancalendar.view.activity.AthanActivity
import com.github.praytimes.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.InputStream
import java.util.*
import java.util.concurrent.TimeUnit

//import com.byagowi.persiancalendar.service.UpdateWorker;
//import androidx.work.ExistingPeriodicWorkPolicy;
//import androidx.work.ExistingWorkPolicy;
//import androidx.work.OneTimeWorkRequest;
//import androidx.work.PeriodicWorkRequest;
//import androidx.work.WorkManager;

/**
 * Common utilities that needed for this calendar
 *
 * @author ebraminio
 */

object Utils {

  private val TAG = Utils::class.java.name

  val maxSupportedYear: Int
    get() = 1397

  private var persianMonths: Array<String> = emptyArray()
  private var islamicMonths: Array<String> = emptyArray()
  private var gregorianMonths: Array<String> = emptyArray()
  private var weekDays: Array<String> = emptyArray()
  private var weekDaysInitials: Array<String> = emptyArray()

  private var preferredDigits = PERSIAN_DIGITS
  var isClockIn24 = DEFAULT_WIDGET_IN_24
    private set
  var isIranTime = DEFAULT_IRAN_TIME
    private set
  var isNotifyDateOnLockScreen = DEFAULT_NOTIFY_DATE_LOCK_SCREEN
    private set
  var isWidgetClock = DEFAULT_WIDGET_CLOCK
    private set
  var isNotifyDate = DEFAULT_NOTIFY_DATE
    private set
  private var notificationAthan = DEFAULT_NOTIFICATION_ATHAN
  var selectedWidgetTextColor: String = DEFAULT_SELECTED_WIDGET_TEXT_COLOR
    private set
  private var islamicOffset: String = DEFAULT_ISLAMIC_OFFSET
  private var calculationMethod: String = DEFAULT_PRAY_TIME_METHOD
  private var language: String = DEFAULT_APP_LANGUAGE
  private var coordinate: Coordinate? = null
  lateinit var mainCalendar: CalendarType
    private set
  lateinit var comma: String
    private set
  var isWeekOfYearEnabled: Boolean = false
    private set
  private var weekStartOffset: Int = 0
  private lateinit var weekEnds: BooleanArray
  var isShowDeviceCalendarEvents: Boolean = false
    private set
  private lateinit var whatToShowOnWidgets: Set<String>

  val isArabicDigitSelected: Boolean
    get() = preferredDigits.contentEquals(ARABIC_DIGITS)

  // If is empty for whatever reason (pref dialog bug, etc), return Persian at least
  val appLanguage: String
    get() = if (TextUtils.isEmpty(language)) DEFAULT_APP_LANGUAGE else language

  // en-US is our only real LTR language for now
  val isLocaleRTL: Boolean
    get() = appLanguage != "en-US"

  private var prayTimes: Map<PrayTime, Clock>? = null

  private var cachedCityKey = ""
  private var cachedCity: CityEntity? = null

  private lateinit var persianCalendarEvents: SparseArray<MutableList<PersianCalendarEvent>>
  private lateinit var islamicCalendarEvents: SparseArray<MutableList<IslamicCalendarEvent>>
  private lateinit var gregorianCalendarEvents: SparseArray<MutableList<GregorianCalendarEvent>>
  private lateinit var deviceCalendarEvents: SparseArray<List<DeviceCalendarEvent>>
  lateinit var allEnabledEvents: MutableList<BaseEvent>
  lateinit var allEnabledEventsTitles: MutableList<String>

  //    public static boolean goForWorker() {
  //        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
  //    }

  private val UPDATE_TAG = "update"

  // This should be called before any use of Utils on the activity and services
  fun initUtils(context: Context) {
    updateStoredPreference(context)
    changeAppLanguage(context)
    loadLanguageResource(context)
    loadAlarms(context)
    loadEvents(context)
  }

  fun getCoordinate(context: Context): Coordinate? {
    val cityEntity = getCityFromPreference(context)
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    if (cityEntity != null) {
      return cityEntity.coordinate
    }

    try {
      val coord = Coordinate(
          java.lang.Double.parseDouble(prefs.getString(PREF_LATITUDE, DEFAULT_LATITUDE)),
          java.lang.Double.parseDouble(prefs.getString(PREF_LONGITUDE, DEFAULT_LONGITUDE)),
          java.lang.Double.parseDouble(prefs.getString(PREF_ALTITUDE, DEFAULT_ALTITUDE))
      )

      // If latitude or longitude is zero probably preference is not set yet
      return if (coord.latitude == 0.0 && coord.longitude == 0.0) {
        null
      } else coord

    } catch (e: NumberFormatException) {
      return null
    }

  }

  fun updateStoredPreference(context: Context) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    language = prefs.getString(PREF_APP_LANGUAGE, DEFAULT_APP_LANGUAGE)
    preferredDigits = if (prefs.getBoolean(PREF_PERSIAN_DIGITS, DEFAULT_PERSIAN_DIGITS))
      PERSIAN_DIGITS
    else
      ARABIC_DIGITS
    if (language == "ckb" && preferredDigits == PERSIAN_DIGITS)
      preferredDigits = ARABIC_INDIC_DIGITS

    isClockIn24 = prefs.getBoolean(PREF_WIDGET_IN_24, DEFAULT_WIDGET_IN_24)
    isIranTime = prefs.getBoolean(PREF_IRAN_TIME, DEFAULT_IRAN_TIME)
    isNotifyDateOnLockScreen = prefs.getBoolean(PREF_NOTIFY_DATE_LOCK_SCREEN,
        DEFAULT_NOTIFY_DATE_LOCK_SCREEN)
    isWidgetClock = prefs.getBoolean(PREF_WIDGET_CLOCK, DEFAULT_WIDGET_CLOCK)
    isNotifyDate = prefs.getBoolean(PREF_NOTIFY_DATE, DEFAULT_NOTIFY_DATE)
    notificationAthan = prefs.getBoolean(PREF_NOTIFICATION_ATHAN, DEFAULT_NOTIFICATION_ATHAN)
    selectedWidgetTextColor = prefs.getString(PREF_SELECTED_WIDGET_TEXT_COLOR,
        DEFAULT_SELECTED_WIDGET_TEXT_COLOR)
    islamicOffset = prefs.getString(PREF_ISLAMIC_OFFSET, DEFAULT_ISLAMIC_OFFSET)
    // We were using "Jafari" method but later found out Tehran is nearer to time.ir and others
    // so switched to "Tehran" method as default calculation algorithm
    calculationMethod = prefs.getString(PREF_PRAY_TIME_METHOD, DEFAULT_PRAY_TIME_METHOD)
    coordinate = getCoordinate(context)
    mainCalendar = CalendarType.valueOf(prefs.getString("mainCalendarType", "SHAMSI"))
    comma = if (language == LANG_EN_US) "," else "،"
    isWeekOfYearEnabled = prefs.getBoolean("showWeekOfYearNumber", false)

    weekStartOffset = Integer.parseInt(prefs.getString("WeekStart", "0"))
    // WeekEnds, 6 means Friday
    val weekEndz = BooleanArray(7)
    for (s in prefs.getStringSet("WeekEnds", hashSetOf("6")))
      weekEndz[Integer.parseInt(s)] = true
    weekEnds = weekEndz

    isShowDeviceCalendarEvents = prefs.getBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, false)
    whatToShowOnWidgets = prefs.getStringSet("what_to_show",
        hashSetOf(*context.resources.getStringArray(R.array.what_to_show_default)))
  }

  fun isShownOnWidgets(infoType: String): Boolean = whatToShowOnWidgets.contains(infoType)

  fun isWeekEnd(dayOfWeek: Int): Boolean = weekEnds[dayOfWeek]

  fun getAthanVolume(context: Context): Int {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    return prefs.getInt(PREF_ATHAN_VOLUME, DEFAULT_ATHAN_VOLUME)
  }

  fun getCalculationMethod(): CalculationMethod = CalculationMethod.valueOf(calculationMethod)

  fun getIslamicOffset(): Int = Integer.parseInt(islamicOffset.replace("+", ""))

  fun getNextOwghatTime(context: Context, clock: Clock, dateHasChanged: Boolean): String? {
    if (coordinate == null) return null

    val localPrayTimes = prayTimes
        ?: PrayTimesCalculator(getCalculationMethod()).calculate(Date(), coordinate)
    prayTimes = localPrayTimes

    return if (localPrayTimes[PrayTime.FAJR]!!.int > clock.int) {
      context.getString(R.string.azan1) + ": " + UIUtils.getFormattedClock(localPrayTimes[PrayTime.FAJR]!!)

    } else if (localPrayTimes[PrayTime.SUNRISE]!!.int > clock.int) {
      context.getString(R.string.aftab1) + ": " + UIUtils.getFormattedClock(localPrayTimes[PrayTime.SUNRISE]!!)

    } else if (localPrayTimes[PrayTime.DHUHR]!!.int > clock.int) {
      context.getString(R.string.azan2) + ": " + UIUtils.getFormattedClock(localPrayTimes[PrayTime.DHUHR]!!)

    } else if (localPrayTimes[PrayTime.ASR]!!.int > clock.int) {
      context.getString(R.string.azan3) + ": " + UIUtils.getFormattedClock(localPrayTimes[PrayTime.ASR]!!)

    } else if (localPrayTimes[PrayTime.SUNSET]!!.int > clock.int) {
      context.getString(R.string.aftab2) + ": " + UIUtils.getFormattedClock(localPrayTimes[PrayTime.SUNSET]!!)

    } else if (localPrayTimes[PrayTime.MAGHRIB]!!.int > clock.int) {
      context.getString(R.string.azan4) + ": " + UIUtils.getFormattedClock(localPrayTimes[PrayTime.MAGHRIB]!!)

    } else if (localPrayTimes[PrayTime.ISHA]!!.int > clock.int) {
      context.getString(R.string.azan5) + ": " + UIUtils.getFormattedClock(localPrayTimes[PrayTime.ISHA]!!)

    } else if (localPrayTimes[PrayTime.MIDNIGHT]!!.int > clock.int) {
      context.getString(R.string.aftab3) + ": " + UIUtils.getFormattedClock(localPrayTimes[PrayTime.MIDNIGHT]!!)

    } else {
      context.getString(R.string.azan1) + ": " + UIUtils.getFormattedClock(localPrayTimes[PrayTime.FAJR]!!) //this is today & not tomorrow
    }
  }

  fun formatNumber(number: Int): String = formatNumber(Integer.toString(number))

  fun formatNumber(number: String): String {
    if (preferredDigits == ARABIC_DIGITS)
      return number

    val result = number.toCharArray()
    for (i in result.indices) {
      val c = number[i]
      if (Character.isDigit(c))
        result[i] = preferredDigits[Character.getNumericValue(c)]
    }
    return String(result)
  }

  fun dateToString(date: AbstractDate): String =
      formatNumber(date.dayOfMonth) + ' '.toString() + CalendarUtils.getMonthName(date) + ' '.toString() +
          formatNumber(date.year)

  fun monthsNamesOfCalendar(date: AbstractDate): Array<String> = when (date) {
    is PersianDate -> persianMonths
    is IslamicDate -> islamicMonths
    else -> gregorianMonths
  }

  fun getWeekDayName(inputDate: AbstractDate): String {
    var date = inputDate
    if (date is IslamicDate)
      date = DateConverter.islamicToCivil(date)
    else if (date is PersianDate)
      date = DateConverter.persianToCivil(date)

    return weekDays[date.dayOfWeek % 7]
  }

  fun getDayIconResource(day: Int): Int {
    try {
      if (preferredDigits == ARABIC_DIGITS)
        return DAYS_ICONS_AR[day]
      else if (preferredDigits == ARABIC_INDIC_DIGITS)
        return DAYS_ICONS_CKB[day]
      return DAYS_ICONS[day]
    } catch (e: IndexOutOfBoundsException) {
      Log.e(TAG, "No such field is available")
      return 0
    }

  }

  private fun readStream(`is`: InputStream): String {
    // http://stackoverflow.com/a/5445161
    val s = Scanner(`is`).useDelimiter("\\A")
    return if (s.hasNext()) s.next() else ""
  }

  fun readRawResource(context: Context, @RawRes res: Int): String =
      readStream(context.resources.openRawResource(res))

  private fun prepareForArabicSort(text: String): String = text
      .replace("ی".toRegex(), "ي")
      .replace("ک".toRegex(), "ك")
      .replace("گ".toRegex(), "كی")
      .replace("ژ".toRegex(), "زی")
      .replace("چ".toRegex(), "جی")
      .replace("پ".toRegex(), "بی")
      .replace("ڕ".toRegex(), "ری")
      .replace("ڵ".toRegex(), "لی")
      .replace("ڤ".toRegex(), "فی")
      .replace("ۆ".toRegex(), "وی")
      .replace("ێ".toRegex(), "یی")
      .replace("ھ".toRegex(), "نی")
      .replace("ە".toRegex(), "هی")

  fun getAllCities(context: Context, needsSort: Boolean): List<CityEntity> {
    val result = ArrayList<CityEntity>()
    try {
      val countries = JSONObject(readRawResource(context, R.raw.cities))

      for (countryCode in countries.keys()) {
        val country = countries.getJSONObject(countryCode)

        val countryEn = country.getString("en")
        val countryFa = country.getString("fa")
        val countryCkb = country.getString("ckb")

        val cities = country.getJSONObject("cities")

        for (key in cities.keys()) {
          val city = cities.getJSONObject(key)

          val en = city.getString("en")
          val fa = city.getString("fa")
          val ckb = city.getString("ckb")

          val coordinate = Coordinate(
              city.getDouble("latitude"),
              city.getDouble("longitude"),
              0.0 // city.getDouble("elevation")
          )

          result.add(CityEntity(key, en, fa, ckb, countryCode,
              countryEn, countryFa, countryCkb, coordinate))
        }
      }
    } catch (e: JSONException) {
      Log.e(TAG, e.message)
    }

    if (!needsSort) {
      return result
    }

    val cities = result.toTypedArray()
    // Sort first by country code then city
    Arrays.sort(cities) { l, r ->
      if (l.key == "") {
        return@sort -1
      }
      if (r.key == DEFAULT_CITY) {
        return@sort 1
      }
      val compare = r.countryCode.compareTo(l.countryCode)
      if (compare == 0) compare
      return@sort when (language) {
        LANG_EN -> l.en.compareTo(r.en)
        LANG_CKB -> prepareForArabicSort(l.ckb).compareTo(prepareForArabicSort(r.ckb))
        else -> prepareForArabicSort(l.fa).compareTo(prepareForArabicSort(r.fa))
      }
    }

    return Arrays.asList(*cities)
  }

  private fun getCityFromPreference(context: Context): CityEntity? {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    val key = prefs.getString(PREF_SELECTED_LOCATION, "")

    if (TextUtils.isEmpty(key) || key == DEFAULT_CITY)
      return null

    if (key == cachedCityKey)
      return cachedCity

    // cache last query even if no city available under the key, useful in case invalid
    // value is somehow inserted on the preference
    cachedCityKey = key

    for (cityEntity in getAllCities(context, false))
      if (cityEntity.key == key) {
        cachedCity = cityEntity
        return cachedCity
      }

    cachedCity = null
    return null
  }

  fun formatCoordinate(context: Context, coordinate: Coordinate, separator: String): String {
    return String.format(Locale.getDefault(), "%s: %.4f%s%s: %.4f",
        context.getString(R.string.latitude), coordinate.latitude, separator,
        context.getString(R.string.longitude), coordinate.longitude)
  }

  fun getCityName(context: Context, fallbackToCoord: Boolean): String {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val cityEntity = getCityFromPreference(context)
    if (cityEntity != null) {
      if (language == LANG_EN)
        return cityEntity.en
      else if (language == LANG_CKB)
        return cityEntity.ckb
      return cityEntity.fa
    }

    val geocodedCityName = prefs.getString(PREF_GEOCODED_CITYNAME, "")
    if (!TextUtils.isEmpty(geocodedCityName))
      return geocodedCityName

    val coord = coordinate
    if (fallbackToCoord)
      if (coord != null)
        return formatCoordinate(context, coord, comma + " ")

    return ""
  }

  private fun loadEvents(context: Context) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    var enabledTypes = prefs.getStringSet(PREF_HOLIDAY_TYPES, hashSetOf())

    if (enabledTypes == null || enabledTypes.isEmpty())
      enabledTypes = hashSetOf("iran_holidays")

    val afghanistanHolidays = enabledTypes.contains("afghanistan_holidays")
    val afghanistanOthers = enabledTypes.contains("afghanistan_others")
    val iranHolidays = enabledTypes.contains("iran_holidays")
    val iranIslamic = enabledTypes.contains("iran_islamic")
    val iranAncient = enabledTypes.contains("iran_ancient")
    val iranOthers = enabledTypes.contains("iran_others")
    val international = enabledTypes.contains("international")

    val persianCalendarEvents = SparseArray<MutableList<PersianCalendarEvent>>()
    val islamicCalendarEvents = SparseArray<MutableList<IslamicCalendarEvent>>()
    val gregorianCalendarEvents = SparseArray<MutableList<GregorianCalendarEvent>>()
    val allEnabledEvents = ArrayList<BaseEvent>()
    val allEnabledEventsTitles = ArrayList<String>()

    try {
      var days: JSONArray
      var length: Int
      val allTheEvents = JSONObject(readRawResource(context, R.raw.events))

      days = allTheEvents.getJSONArray("Persian Calendar")
      length = days.length()
      for (i in 0 until length) {
        val event = days.getJSONObject(i)

        val month = event.getInt("month")
        val day = event.getInt("day")
        val year = if (event.has("year")) event.getInt("year") else -1
        var title = event.getString("title")
        var holiday = event.getBoolean("holiday")

        var addOrNot = false
        val type = event.getString("type")

        if (holiday && iranHolidays && (type == "Islamic Iran" ||
                type == "Iran" || type == "Ancient Iran"))
          addOrNot = true

        if (!iranHolidays && type == "Islamic Iran")
          holiday = false

        if (iranIslamic && type == "Islamic Iran")
          addOrNot = true

        if (iranAncient && type == "Ancient Iran")
          addOrNot = true

        if (iranOthers && type == "Iran")
          addOrNot = true

        if (afghanistanHolidays && type == "Afghanistan" && holiday)
          addOrNot = true

        if (!afghanistanHolidays && type == "Afghanistan")
          holiday = false

        if (afghanistanOthers && type == "Afghanistan")
          addOrNot = true

        if (addOrNot) {
          title += " ("
          if (holiday && afghanistanHolidays && iranHolidays) {
            if (type == "Islamic Iran" || type == "Iran")
              title += "ایران، "
            else if (type == "Afghanistan")
              title += "افغانستان، "
          }
          title += formatNumber(day) + " " + persianMonths[month - 1] + ")"

          var list: MutableList<PersianCalendarEvent>? =
              persianCalendarEvents.get(month * 100 + day)
          if (list == null) {
            list = ArrayList()
            persianCalendarEvents.put(month * 100 + day, list)
          }
          val ev = PersianCalendarEvent(PersianDate(year, month, day), title, holiday)
          list.add(ev)
          allEnabledEvents.add(ev)
          allEnabledEventsTitles.add(title)
        }
      }

      days = allTheEvents.getJSONArray("Hijri Calendar")
      length = days.length()
      for (i in 0 until length) {
        val event = days.getJSONObject(i)

        val month = event.getInt("month")
        val day = event.getInt("day")
        var title = event.getString("title")
        var holiday = event.getBoolean("holiday")

        var addOrNot = false
        val type = event.getString("type")

        if (afghanistanHolidays && holiday && type == "Islamic Afghanistan")
          addOrNot = true

        if (!afghanistanHolidays && type == "Islamic Afghanistan")
          holiday = false

        if (afghanistanOthers && type == "Islamic Afghanistan")
          addOrNot = true

        if (iranHolidays && holiday && type == "Islamic Iran")
          addOrNot = true

        if (!iranHolidays && type == "Islamic Iran")
          holiday = false

        if (iranIslamic && type == "Islamic Iran")
          addOrNot = true

        if (iranOthers && type == "Islamic Iran")
          addOrNot = true

        if (addOrNot) {
          title += " ("
          if (holiday && afghanistanHolidays && iranHolidays) {
            if (type == "Islamic Iran")
              title += "ایران، "
            else if (type == "Islamic Afghanistan")
              title += "افغانستان، "
          }
          title += formatNumber(day) + " " + islamicMonths[month - 1] + ")"
          var list: MutableList<IslamicCalendarEvent>? =
              islamicCalendarEvents.get(month * 100 + day)
          if (list == null) {
            list = ArrayList()
            islamicCalendarEvents.put(month * 100 + day, list)
          }
          val ev = IslamicCalendarEvent(IslamicDate(-1, month, day), title, holiday)
          list.add(ev)
          allEnabledEvents.add(ev)
          allEnabledEventsTitles.add(title)
        }
      }

      days = allTheEvents.getJSONArray("Gregorian Calendar")
      length = days.length()
      for (i in 0 until length) {
        val event = days.getJSONObject(i)

        val month = event.getInt("month")
        val day = event.getInt("day")
        var title = event.getString("title")

        if (international) {
          title += " (" + formatNumber(day) + " " + gregorianMonths[month - 1] + ")"
          var list: MutableList<GregorianCalendarEvent>? =
              gregorianCalendarEvents.get(month * 100 + day)
          if (list == null) {
            list = ArrayList()
            gregorianCalendarEvents.put(month * 100 + day, list)
          }
          val ev = GregorianCalendarEvent(CivilDate(-1, month, day), title, false)
          list.add(ev)
          allEnabledEvents.add(ev)
          allEnabledEventsTitles.add(title)
        }
      }

    } catch (e: JSONException) {
      Log.e(TAG, e.message)
    }

    Utils.persianCalendarEvents = persianCalendarEvents
    Utils.islamicCalendarEvents = islamicCalendarEvents
    Utils.gregorianCalendarEvents = gregorianCalendarEvents
    Utils.allEnabledEvents = allEnabledEvents
    Utils.allEnabledEventsTitles = allEnabledEventsTitles

    readDeviceCalendarEvents(context)
  }

  private fun readDeviceCalendarEvents(context: Context) {
    val deviceCalendarEvents = SparseArray<List<DeviceCalendarEvent>>()
    Utils.deviceCalendarEvents = deviceCalendarEvents

    if (!isShowDeviceCalendarEvents) {
      return
    }

    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
      if (context is AppCompatActivity) {
        UIUtils.askForCalendarPermission(context)
      }
      return
    }

    try {
      val cursor = context.contentResolver.query(CalendarContract.Events.CONTENT_URI,
          arrayOf(CalendarContract.Events._ID, // 0
              CalendarContract.Events.TITLE, // 1
              CalendarContract.Events.DESCRIPTION, // 2
              CalendarContract.Events.DTSTART, // 3
              CalendarContract.Events.DTEND, // 4
              CalendarContract.Events.EVENT_LOCATION, // 5
              CalendarContract.Events.RRULE, // 6
              CalendarContract.Events.VISIBLE, // 7
              CalendarContract.Events.ALL_DAY, // 8
              CalendarContract.Events.DELETED, // 9
              CalendarContract.Events.EVENT_COLOR     // 10
          ), null, null, null) ?: return

      while (cursor.moveToNext()) {
        if (cursor.getString(7) != "1" || cursor.getString(9) == "1")
          continue

        var allDay = false
        if (cursor.getString(8) == "1")
          allDay = true

        val startDate = Date(cursor.getLong(3))
        val endDate = Date(cursor.getLong(4))
        val startCalendar = CalendarUtils.makeCalendarFromDate(startDate)
        val endCalendar = CalendarUtils.makeCalendarFromDate(endDate)

        val civilDate = CivilDate(startCalendar)

        val month = civilDate.month
        val day = civilDate.dayOfMonth

        val repeatRule = cursor.getString(6)
        if (repeatRule != null && repeatRule.contains("FREQ=YEARLY"))
          civilDate.year = -1

        var list: MutableList<DeviceCalendarEvent>? = deviceCalendarEvents.get(month * 100 + day) as MutableList<DeviceCalendarEvent>?
        if (list == null) {
          list = ArrayList()
          deviceCalendarEvents.put(month * 100 + day, list)
        }

        var title = cursor.getString(1)
        if (allDay) {
          if (civilDate.year == -1) {
            title = "\uD83C\uDF89 $title"
          } else {
            title = "\uD83D\uDCC5 $title"
          }
        } else {
          title = "\uD83D\uDD53 $title"
          title += " (" + UIUtils.clockToString(startCalendar.get(Calendar.HOUR_OF_DAY),
              startCalendar.get(Calendar.MINUTE))

          if (cursor.getLong(3) != cursor.getLong(4) && cursor.getLong(4) != 0L) {
            title += "-" + UIUtils.clockToString(endCalendar.get(Calendar.HOUR_OF_DAY),
                endCalendar.get(Calendar.MINUTE))
          }

          title += ")"
        }
        val event = DeviceCalendarEvent(
            cursor.getInt(0),
            title,
            cursor.getString(2),
            startDate,
            endDate,
            cursor.getString(5),
            civilDate,
            cursor.getString(10),
            false
        )
        list.add(event)
        allEnabledEvents.add(event)
        allEnabledEventsTitles.add(title)
      }
      cursor.close()
    } catch (e: Exception) {
      // We don't like crash addition from here, just catch all of exceptions
      Log.e(TAG, "Error on device calendar events read", e)
    }

  }

  fun getEvents(jdn: Long): List<BaseEvent> {
    val day = DateConverter.jdnToPersian(jdn)
    val civil = DateConverter.jdnToCivil(jdn)
    val islamic = DateConverter.jdnToIslamic(jdn)

    val result = ArrayList<BaseEvent>()

    val persianList = persianCalendarEvents.get(day.month * 100 + day.dayOfMonth)
    if (persianList != null)
      for (persianCalendarEvent in persianList)
        if (persianCalendarEvent.date.equals(day))
          result.add(persianCalendarEvent)

    val islamicList = islamicCalendarEvents.get(islamic.month * 100 + islamic.dayOfMonth)
    if (islamicList != null)
      for (islamicCalendarEvent in islamicList)
        if (islamicCalendarEvent.date.equals(islamic))
          result.add(islamicCalendarEvent)

    val gregorianList = gregorianCalendarEvents.get(civil.month * 100 + civil.dayOfMonth)
    if (gregorianList != null)
      for (gregorianCalendarEvent in gregorianList)
        if (gregorianCalendarEvent.date.equals(civil))
          result.add(gregorianCalendarEvent)

    val deviceEventList = deviceCalendarEvents.get(civil.month * 100 + civil.dayOfMonth)
    if (deviceEventList != null)
      for (deviceCalendarEvent in deviceEventList)
        if (deviceCalendarEvent.civilDate.equals(civil))
          result.add(deviceCalendarEvent)

    return result
  }

  fun getEventsTitle(dayEvents: List<BaseEvent>, holiday: Boolean,
                     compact: Boolean, showDeviceCalendarEvents: Boolean,
                     insertRLM: Boolean): String {
    val titles = StringBuilder()
    var first = true

    for (event in dayEvents)
      if (event.isHoliday == holiday) {
        var title = event.title
        if (insertRLM) {
          title = Constants.RLM + title
        }
        if (event is DeviceCalendarEvent) {
          if (!showDeviceCalendarEvents)
            continue

          if (!compact) {
            title = UIUtils.formatDeviceCalendarEventTitle(event)
          }
        } else {
          if (compact)
            title = title.replace(" \\(.*$".toRegex(), "")
        }

        if (first)
          first = false
        else
          titles.append("\n")
        titles.append(title)
      }

    return titles.toString()
  }

  fun loadAlarms(context: Context) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    val prefString = prefs.getString(PREF_ATHAN_ALARM, "")
    Log.d(TAG, "reading and loading all alarms from prefs: " + prefString!!)
    val calculationMethod = getCalculationMethod()

    if (coordinate != null && !TextUtils.isEmpty(prefString)) {
      var athanGap: Long
      try {
        athanGap = (java.lang.Double.parseDouble(
            prefs.getString(PREF_ATHAN_GAP, "0")) * 60.0 * 1000.0).toLong()
      } catch (e: NumberFormatException) {
        athanGap = 0
      }

      val calculator = PrayTimesCalculator(calculationMethod)
      val prayTimes = calculator.calculate(Date(), coordinate)
      // convert comma separated string to a set
      val alarmTimesSet = hashSetOf(*TextUtils.split(prefString, ","))
      // in the past IMSAK was used but now we figured out FAJR was what we wanted
      if (alarmTimesSet.remove("IMSAK"))
        alarmTimesSet.add("FAJR")

      val alarmTimesNames = alarmTimesSet.toTypedArray()
      for (i in alarmTimesNames.indices) {
        val prayTime = PrayTime.valueOf(alarmTimesNames[i])

        val alarmTime = prayTimes[prayTime]

        if (alarmTime != null)
          setAlarm(context, prayTime, alarmTime, i, athanGap)
      }
    }
  }

  private fun setAlarm(context: Context, prayTime: PrayTime, clock: Clock, ord: Int,
                       athanGap: Long) {
    val triggerTime = Calendar.getInstance()
    triggerTime.set(Calendar.HOUR_OF_DAY, clock.hour)
    triggerTime.set(Calendar.MINUTE, clock.minute)
    setAlarm(context, prayTime, triggerTime.timeInMillis, ord, athanGap)
  }

  private fun setAlarm(context: Context, prayTime: PrayTime, timeInMillis: Long, ord: Int,
                       athanGap: Long) {
    val triggerTime = Calendar.getInstance()
    triggerTime.timeInMillis = timeInMillis - athanGap
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?

    // don't set an alarm in the past
    if (alarmManager != null && !triggerTime.before(Calendar.getInstance())) {
      Log.d(TAG, "setting alarm for: " + triggerTime.time)

      val pendingIntent = PendingIntent.getBroadcast(context,
          ALARMS_BASE_ID + ord,
          Intent(context, BroadcastReceivers::class.java)
              .putExtra(KEY_EXTRA_PRAYER_KEY, prayTime.name)
              .setAction(BROADCAST_ALARM),
          PendingIntent.FLAG_UPDATE_CURRENT)

      if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime.timeInMillis, pendingIntent)
      } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime.timeInMillis, pendingIntent)
      } else {
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime.timeInMillis, pendingIntent)
      }
    }
  }

  fun getCustomAthanUri(context: Context): Uri? {
    val uri = PreferenceManager.getDefaultSharedPreferences(context)
        .getString(PREF_ATHAN_URI, "")
    return if (TextUtils.isEmpty(uri)) null else Uri.parse(uri)
  }

  fun startAthan(context: Context, prayTimeKey: String) {
    if (notificationAthan) {
      context.startService(Intent(context, AthanNotification::class.java)
          .putExtra(Constants.KEY_EXTRA_PRAYER_KEY, prayTimeKey))
    } else {
      context.startActivity(Intent(context, AthanActivity::class.java)
          .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          .putExtra(Constants.KEY_EXTRA_PRAYER_KEY, prayTimeKey))
    }
  }

  fun fixDayOfWeek(dayOfWeek: Int): Int = (dayOfWeek + weekStartOffset) % 7

  fun fixDayOfWeekReverse(dayOfWeek: Int): Int = (dayOfWeek + 7 - weekStartOffset) % 7

  // Context preferably should be activity context not application
  fun changeAppLanguage(context: Context) {
    val localeCode = UIUtils.getOnlyLanguage(language)
    val locale = Locale(localeCode)
    Locale.setDefault(locale)
    val resources = context.resources
    val config = resources.configuration
    config.locale = locale
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      config.setLayoutDirection(config.locale)
    }
    resources.updateConfiguration(config, resources.displayMetrics)
  }

  private fun loadLanguageResource(context: Context) {
    @RawRes val messagesFile: Int
    when (language) {
      "fa-AF" -> messagesFile = R.raw.messages_fa_af
      "ps" -> messagesFile = R.raw.messages_ps
      "ckb" -> messagesFile = R.raw.messages_ckb
      "ur" -> messagesFile = R.raw.messages_ur
      "en-US" -> messagesFile = R.raw.messages_en
      "en", "fa" -> messagesFile = R.raw.messages_fa
      else -> messagesFile = R.raw.messages_fa
    }

    val persianMonthsLocal = mutableListOf<String>()
    val islamicMonthsLocal = mutableListOf<String>()
    val gregorianMonthsLocal = mutableListOf<String>()
    val weekDaysLocal = mutableListOf<String>()
    val weekDaysInitialsLocal = mutableListOf<String>()

    try {
      val messages = JSONObject(readRawResource(context, messagesFile))

      val persianMonthsArray = messages.getJSONArray("PersianCalendarMonths")
      for (i in 0..11)
        persianMonthsLocal.add(persianMonthsArray.getString(i))

      val islamicMonthsArray = messages.getJSONArray("IslamicCalendarMonths")
      for (i in 0..11)
        islamicMonthsLocal.add(islamicMonthsArray.getString(i))

      val gregorianMonthsArray = messages.getJSONArray("GregorianCalendarMonths")
      for (i in 0..11)
        gregorianMonthsLocal.add(gregorianMonthsArray.getString(i))

      val weekDaysArray = messages.getJSONArray("WeekDays")
      for (i in 0..6) {
        weekDaysLocal.add(weekDaysArray.getString(i))
        weekDaysInitialsLocal.add(weekDaysLocal[i].substring(0, 1))
      }

      persianMonths = persianMonthsLocal.toTypedArray()
      islamicMonths = islamicMonthsLocal.toTypedArray()
      gregorianMonths = gregorianMonthsLocal.toTypedArray()
      weekDays = weekDaysLocal.toTypedArray()
      weekDaysInitials = weekDaysInitialsLocal.toTypedArray()

    } catch (e: JSONException) {
      Log.e(TAG, e.message)
    }

  }

  fun getInitialOfWeekDay(position: Int): String = weekDaysInitials[position % 7]

  //
  //
  //
  // Service
  //
  //    private static final long DAY_IN_SECOND = 86400;

  //    private static long calculateDiffToChangeDate() {
  //        Date currentTime = Calendar.getInstance().getTime();
  //        long current = currentTime.getTime() / 1000;
  //
  //        Calendar startTime = Calendar.getInstance();
  //        startTime.set(Calendar.HOUR_OF_DAY, 0);
  //        startTime.set(Calendar.MINUTE, 0);
  //        startTime.set(Calendar.SECOND, 1);
  //
  //        long start = startTime.getTimeInMillis() / 1000 + DAY_IN_SECOND;
  //
  //        return start - current;
  //    }

  //    private static final String CHANGE_DATE_TAG = "changeDate";
  //    public static void setChangeDateWorker() {
  //        long remainedSeconds = calculateDiffToChangeDate();
  //        OneTimeWorkRequest changeDateWorker =
  //                new OneTimeWorkRequest.Builder(UpdateWorker.class)
  //                        .setInitialDelay(remainedSeconds, TimeUnit.SECONDS)// Use this when you want to add initial delay or schedule initial work to `OneTimeWorkRequest` e.g. setInitialDelay(2, TimeUnit.HOURS)
  //                        .build();
  //
  //        WorkManager.getInstance().beginUniqueWork(
  //                CHANGE_DATE_TAG,
  //                ExistingWorkPolicy.REPLACE,
  //                changeDateWorker).enqueue();
  //    }

  fun loadApp(context: Context) {
    //        if (!goForWorker()) {
    try {
      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager? ?: return

      val startTime = Calendar.getInstance()
      startTime.set(Calendar.HOUR_OF_DAY, 0)
      startTime.set(Calendar.MINUTE, 0)
      startTime.set(Calendar.SECOND, 1)
      startTime.add(Calendar.DATE, 1)

      val dailyPendingIntent = PendingIntent.getBroadcast(context, LOAD_APP_ID,
          Intent(context, BroadcastReceivers::class.java).setAction(BROADCAST_RESTART_APP),
          PendingIntent.FLAG_UPDATE_CURRENT)
      alarmManager.set(AlarmManager.RTC, startTime.timeInMillis, dailyPendingIntent)

      // There are simpler triggers on older Androids like SCREEN_ON but they
      // are not available anymore, lets register an hourly alarm for >= Oreo
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val threeHoursPendingIntent = PendingIntent.getBroadcast(context,
            THREE_HOURS_APP_ID,
            Intent(context, BroadcastReceivers::class.java)
                .setAction(BROADCAST_UPDATE_APP),
            PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager.setInexactRepeating(AlarmManager.RTC,
            // Start from one hour from now
            Calendar.getInstance().timeInMillis + TimeUnit.HOURS.toMillis(1),
            TimeUnit.HOURS.toMillis(3), threeHoursPendingIntent)
      }
    } catch (e: Exception) {
      Log.e(TAG, "loadApp fail", e)
    }

    //        }
  }

  fun startEitherServiceOrWorker(context: Context) {
    //        WorkManager workManager = WorkManager.getInstance();
    //        if (goForWorker()) {
    //            PeriodicWorkRequest.Builder updateBuilder = new PeriodicWorkRequest
    //                    .Builder(UpdateWorker.class, 1, TimeUnit.HOURS);
    //
    //            PeriodicWorkRequest updateWork = updateBuilder.build();
    //            workManager.enqueueUniquePeriodicWork(
    //                    UPDATE_TAG,
    //                    ExistingPeriodicWorkPolicy.REPLACE,
    //                    updateWork);
    //        } else {
    //            // Disable all the scheduled workers, just in case enabled before
    //            workManager.cancelAllWork();
    //            // Or,
    //            // workManager.cancelAllWorkByTag(UPDATE_TAG);
    //            // workManager.cancelUniqueWork(CHANGE_DATE_TAG);

    var alreadyRan = false
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
    if (manager != null) {
      for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
        if (ApplicationService::class.java.name == service.service.className) {
          alreadyRan = true
        }
      }
    }

    if (!alreadyRan) {
      try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
          context.startForegroundService(Intent(context, ApplicationService::class.java))

        context.startService(Intent(context, ApplicationService::class.java))
      } catch (e: Exception) {
        Log.e(TAG, "startEitherServiceOrWorker fail", e)
      }

    }
    //        }
  }
}
