package com.byagowi.persiancalendar.utils

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.provider.CalendarContract
import android.text.TextUtils
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import androidx.preference.PreferenceManager
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.entities.*
import com.byagowi.persiancalendar.praytimes.CalculationMethod
import com.byagowi.persiancalendar.praytimes.Clock
import com.byagowi.persiancalendar.praytimes.Coordinate
import com.byagowi.persiancalendar.praytimes.PrayTimesCalculator
import com.byagowi.persiancalendar.service.BroadcastReceivers
import com.byagowi.persiancalendar.utils.Utils.*
import com.google.android.material.circularreveal.CircularRevealCompat
import com.google.android.material.circularreveal.CircularRevealWidget
import com.google.android.material.snackbar.Snackbar
import io.github.persiancalendar.Equinox
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.InputStream
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.ceil
import kotlin.math.sqrt

private var sAllEnabledEvents: List<AbstractEvent<*>> = ArrayList()

// This should be called before any use of Utils on the activity and services
fun initUtils(context: Context) {
    updateStoredPreference(context)
    applyAppLanguage(context)
    loadLanguageResource(context)
    loadAlarms(context)
    loadEvents(context)
}

@StyleRes
fun getAppTheme(): Int = appTheme

fun getMaxSupportedYear(): Int = 1398

fun getAllEnabledEvents(): List<AbstractEvent<*>> = sAllEnabledEvents

fun getShiftWorks(): ArrayList<ShiftWorkRecord> = ArrayList(sShiftWorks)

fun getAmString(): String = sAM

fun getPmString(): String = sPM

fun getShiftWorkStartingJdn(): Long = sShiftWorkStartingJdn

fun getShiftWorkRecurs(): Boolean = sShiftWorkRecurs

fun getShiftWorkTitles(): Map<String, String> = sShiftWorkTitles

fun getMainCalendar(): CalendarType = mainCalendar

fun isShowDeviceCalendarEvents(): Boolean = showDeviceCalendarEvents

fun isShownOnWidgets(infoType: String): Boolean = whatToShowOnWidgets.contains(infoType)

fun isWeekEnd(dayOfWeek: Int): Boolean = weekEnds[dayOfWeek]

fun isIranTime(): Boolean = iranTime

fun isAstronomicalFeaturesEnabled(): Boolean = astronomicalFeaturesEnabled

fun isArabicDigitSelected(): Boolean = preferredDigits.contentEquals(ARABIC_DIGITS)

fun isWidgetClock(): Boolean = widgetClock

fun isNotifyDate(): Boolean = notifyDate

fun isWeekOfYearEnabled(): Boolean = showWeekOfYear

fun getSelectedWidgetTextColor(): String = selectedWidgetTextColor

fun getSelectedWidgetBackgroundColor(): String = selectedWidgetBackgroundColor

fun isIranHolidaysEnabled(): Boolean = sIsIranHolidaysEnabled

fun fixDayOfWeek(dayOfWeek: Int): Int = (dayOfWeek + weekStartOffset) % 7

fun goForWorker(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

fun getWeekDayName(position: Int): String? = weekDays?.let { it[position % 7] }

fun isTalkBackEnabled(): Boolean = talkBackEnabled

fun isCenterAlignWidgets(): Boolean = centerAlignWidgets

fun getSpacedComma(): String = spacedComma

fun isNotifyDateOnLockScreen(): Boolean = notifyInLockScreen

fun dayTitleSummary(date: AbstractDate): String =
    getWeekDayName(date) + getSpacedComma() + formatDate(date)

fun formatDayAndMonth(day: Int, month: String): String =
    String.format(if (language == LANG_CKB) "%sی %s" else "%s %s", formatNumber(day), month)

fun toLinearDate(date: AbstractDate): String = String.format(
    "%s/%s/%s", formatNumber(date.year),
    formatNumber(date.month), formatNumber(date.dayOfMonth)
)

fun isNightModeEnabled(context: Context): Boolean =
    context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

fun formatDate(date: AbstractDate): String =
    if (numericalDatePreferred)
        (toLinearDate(date) + " " + getCalendarNameAbbr(date)).trim { it <= ' ' }
    else
        String.format(
            if (getAppLanguage() == LANG_CKB) "%sی %sی %s" else "%s %s %s",
            formatNumber(date.dayOfMonth), getMonthName(date),
            formatNumber(date.year)
        )

fun getAppLanguage(): String = if (TextUtils.isEmpty(language)) DEFAULT_APP_LANGUAGE else language

fun getCalculationMethod(): CalculationMethod? = CalculationMethod.valueOf(calculationMethod)

fun isNonArabicScriptSelected(): Boolean = when (getAppLanguage()) {
    LANG_EN_US, LANG_JA -> true
    else -> false
}

// en-US and ja are our only real LTR locales for now
fun isLocaleRTL(): Boolean = when (getAppLanguage()) {
    LANG_EN_US, LANG_JA -> false
    else -> true
}

fun formatNumber(number: Int): String = formatNumber(number.toString())

fun formatNumber(number: String): String {
    if (preferredDigits.contentEquals(ARABIC_DIGITS))
        return number

    val result = number.toCharArray()
    for (i in result.indices) {
        val c = number[i]
        if (Character.isDigit(c))
            result[i] = preferredDigits[Character.getNumericValue(c)]
    }
    return String(result)
}

fun loadEvents(context: Context) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    var enabledTypes = prefs.getStringSet(PREF_HOLIDAY_TYPES, HashSet())

    if (enabledTypes == null) {
        enabledTypes = when (getAppLanguage()) {
            LANG_FA, LANG_GLK, LANG_AZB, LANG_EN_IR, LANG_CKB -> HashSet(listOf("iran_holidays"))
            else -> HashSet()
        }
    }

    val afghanistanHolidays = enabledTypes.contains("afghanistan_holidays")
    val afghanistanOthers = enabledTypes.contains("afghanistan_others")
    val iranHolidays = enabledTypes.contains("iran_holidays")
    val iranIslamic = enabledTypes.contains("iran_islamic")
    val iranAncient = enabledTypes.contains("iran_ancient")
    val iranOthers = enabledTypes.contains("iran_others")
    val international = enabledTypes.contains("international")

    sIsIranHolidaysEnabled = iranHolidays

    IslamicDate.useUmmAlQura = false
    if (!iranHolidays) {
        if (afghanistanHolidays) {
            IslamicDate.useUmmAlQura = true
        }
        when (getAppLanguage()) {
            LANG_FA_AF, LANG_PS, LANG_UR, LANG_AR, LANG_CKB, LANG_EN_US, LANG_JA -> IslamicDate.useUmmAlQura =
                true
        }
    }
    // Now that we are configuring converter's algorithm above, lets set the offset also
    IslamicDate.islamicOffset = getIslamicOffset(context)

    val persianCalendarEvents = SparseArray<ArrayList<PersianCalendarEvent>>()
    val islamicCalendarEvents = SparseArray<ArrayList<IslamicCalendarEvent>>()
    val gregorianCalendarEvents = SparseArray<ArrayList<GregorianCalendarEvent>>()
    val allEnabledEvents = ArrayList<AbstractEvent<*>>()

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
                        type == "Iran" || type == "Ancient Iran")
            )
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
                persianMonths?.let {
                    title += formatDayAndMonth(day, it[month - 1]) + ")"
                }

                var list: ArrayList<PersianCalendarEvent>? =
                    persianCalendarEvents.get(month * 100 + day)
                if (list == null) {
                    list = ArrayList()
                    persianCalendarEvents.put(month * 100 + day, list)
                }
                val ev = PersianCalendarEvent(PersianDate(year, month, day), title, holiday)
                list.add(ev)
                allEnabledEvents.add(ev)
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
                islamicMonths?.let {
                    title += formatDayAndMonth(day, it[month - 1]) + ")"
                }
                var list: ArrayList<IslamicCalendarEvent>? =
                    islamicCalendarEvents.get(month * 100 + day)
                if (list == null) {
                    list = ArrayList()
                    islamicCalendarEvents.put(month * 100 + day, list)
                }
                val ev = IslamicCalendarEvent(IslamicDate(-1, month, day), title, holiday)
                list.add(ev)
                allEnabledEvents.add(ev)
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
                gregorianMonths?.let {
                    title += " (" + formatDayAndMonth(day, it[month - 1]) + ")"
                }
                var list: ArrayList<GregorianCalendarEvent>? =
                    gregorianCalendarEvents.get(month * 100 + day)
                if (list == null) {
                    list = ArrayList()
                    gregorianCalendarEvents.put(month * 100 + day, list)
                }
                val ev = GregorianCalendarEvent(CivilDate(-1, month, day), title, false)
                list.add(ev)
                allEnabledEvents.add(ev)
            }
        }

    } catch (ignore: JSONException) {
    }

    sPersianCalendarEvents = persianCalendarEvents
    sIslamicCalendarEvents = islamicCalendarEvents
    sGregorianCalendarEvents = gregorianCalendarEvents
    sAllEnabledEvents = allEnabledEvents
}

// https://stackoverflow.com/a/52557989
fun <T> circularRevealFromMiddle(circularRevealWidget: T) where T : View, T : CircularRevealWidget {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        circularRevealWidget.post {
            val viewWidth = circularRevealWidget.width
            val viewHeight = circularRevealWidget.height

            val viewDiagonal =
                sqrt((viewWidth * viewWidth + viewHeight * viewHeight).toDouble()).toInt()

            AnimatorSet().apply {
                playTogether(
                    CircularRevealCompat.createCircularReveal(
                        circularRevealWidget,
                        (viewWidth / 2).toFloat(), (viewHeight / 2).toFloat(),
                        10f, (viewDiagonal / 2).toFloat()
                    ),
                    ObjectAnimator.ofArgb(
                        circularRevealWidget,
                        CircularRevealWidget.CircularRevealScrimColorProperty
                            .CIRCULAR_REVEAL_SCRIM_COLOR,
                        Color.GRAY, Color.TRANSPARENT
                    )
                )
                duration = 500
            }.start()
        }
    }
}

fun getCalendarNameAbbr(date: AbstractDate): String {
    if (calendarTypesTitleAbbr.size < 3) return ""
    // It should match with calendar_type array
    return when (date) {
        is PersianDate -> calendarTypesTitleAbbr[0]
        is IslamicDate -> calendarTypesTitleAbbr[1]
        is CivilDate -> calendarTypesTitleAbbr[2]
        else -> ""
    }
}

fun getThemeFromPreference(context: Context, prefs: SharedPreferences): String {
    var result = prefs.getString(PREF_THEME, "")
    if (result == null)
        result = if (isNightModeEnabled(context)) DARK_THEME else LIGHT_THEME
    return result
}

fun getIslamicOffset(context: Context): Int {
    try {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val islamicOffset = prefs.getString(PREF_ISLAMIC_OFFSET, DEFAULT_ISLAMIC_OFFSET)
        islamicOffset?.run {
            return Integer.parseInt(replace("+", ""))
        }
        return 0
    } catch (ignore: Exception) {
        return 0
    }
}

fun getEnabledCalendarTypes(): List<CalendarType> {
    val result = ArrayList<CalendarType>()
    result.add(getMainCalendar())
    result.addAll(listOf(*otherCalendars))
    return result
}

private fun loadLanguageResource(context: Context) {
    @RawRes val messagesFile: Int = when (language) {
        LANG_FA_AF -> R.raw.messages_fa_af
        LANG_PS -> R.raw.messages_ps
        LANG_GLK -> R.raw.messages_glk
        LANG_AR -> R.raw.messages_ar
        LANG_CKB -> R.raw.messages_ckb
        LANG_UR -> R.raw.messages_ur
        LANG_EN_US -> R.raw.messages_en
        LANG_JA -> R.raw.messages_ja
        LANG_AZB -> R.raw.messages_azb
        LANG_EN_IR, LANG_FA -> R.raw.messages_fa
        else -> R.raw.messages_fa
    }

    persianMonths = arrayOf("", "", "", "", "", "", "", "", "", "", "", "")
    islamicMonths = arrayOf("", "", "", "", "", "", "", "", "", "", "", "")
    gregorianMonths = arrayOf("", "", "", "", "", "", "", "", "", "", "", "")
    weekDays = arrayOf("", "", "", "", "", "", "")
    weekDaysInitials = arrayOf("", "", "", "", "", "", "")

    try {
        val messages = JSONObject(readRawResource(context, messagesFile))

        val persianMonthsArray = messages.getJSONArray("PersianCalendarMonths")
        for (i in 0..11)
            persianMonths?.let {
                it[i] = persianMonthsArray.getString(i)
            }

        val islamicMonthsArray = messages.getJSONArray("IslamicCalendarMonths")
        for (i in 0..11)
            islamicMonths?.let {
                it[i] = islamicMonthsArray.getString(i)
            }

        val gregorianMonthsArray = messages.getJSONArray("GregorianCalendarMonths")
        for (i in 0..11)
            gregorianMonths?.let {
                it[i] = gregorianMonthsArray.getString(i)
            }

        val weekDaysArray = messages.getJSONArray("WeekDays")
        for (i in 0..6) {
            weekDays?.let {
                it[i] = weekDaysArray.getString(i)
                weekDaysInitials?.run {
                    when (language) {
                        LANG_AR -> this[i] = it[i].substring(2, 4)
                        LANG_AZB -> this[i] = it[i].substring(0, 2)
                        else -> this[i] = it[i].substring(0, 1)
                    }
                }
            }
        }
    } catch (ignore: JSONException) {
    }
}

fun createAndShowSnackbar(view: View?, message: String, duration: Int) {
    view ?: return

    val snackbar = Snackbar.make(view, message, duration)

    val snackbarView = snackbar.view
    snackbarView.setOnClickListener { snackbar.dismiss() }

    val text = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
    text.setTextColor(Color.WHITE)
    text.maxLines = 5

    snackbar.show()
}

fun createAndShowShortSnackbar(view: View?, @StringRes messageId: Int) {
    view ?: return
    val context = view.context ?: return

    createAndShowSnackbar(view, context.getString(messageId), Snackbar.LENGTH_SHORT)
}

fun createAndShowShortSnackbar(view: View?, message: String) {
    view ?: return

    createAndShowSnackbar(view, message, Snackbar.LENGTH_SHORT)
}


fun civilDateToCalendar(civilDate: CivilDate): Calendar = Calendar.getInstance().apply {
    set(Calendar.YEAR, civilDate.year)
    set(Calendar.MONTH, civilDate.month - 1)
    set(Calendar.DAY_OF_MONTH, civilDate.dayOfMonth)
}

fun calendarToCivilDate(calendar: Calendar): CivilDate {
    return CivilDate(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH)
    )
}

fun hasAnyHolidays(dayEvents: List<AbstractEvent<*>>): Boolean {
    for (event in dayEvents) {
        if (event.isHoliday) {
            return true
        }
    }
    return false
}

fun loadApp(context: Context) {
    if (goForWorker()) return

    try {
        val alarmManager = context.getSystemService<AlarmManager>() ?: return

        val startTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 1)
            add(Calendar.DATE, 1)
        }


        val dailyPendingIntent = PendingIntent.getBroadcast(
            context, LOAD_APP_ID,
            Intent(context, BroadcastReceivers::class.java).setAction(BROADCAST_RESTART_APP),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.set(AlarmManager.RTC, startTime.timeInMillis, dailyPendingIntent)

        // There are simpler triggers on older Androids like SCREEN_ON but they
        // are not available anymore, lets register an hourly alarm for >= Oreo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val threeHoursPendingIntent = PendingIntent.getBroadcast(
                context,
                THREE_HOURS_APP_ID,
                Intent(context, BroadcastReceivers::class.java)
                    .setAction(BROADCAST_UPDATE_APP),
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            alarmManager.setInexactRepeating(
                AlarmManager.RTC,
                // Start from one hour from now
                Calendar.getInstance().timeInMillis + TimeUnit.HOURS.toMillis(1),
                TimeUnit.HOURS.toMillis(3), threeHoursPendingIntent
            )
        }
    } catch (e: Exception) {
        Log.e(TAG, "loadApp fail", e)
    }

}

fun getOrderedCalendarTypes(): ArrayList<CalendarType>? {
    val enabledCalendarTypes = getEnabledCalendarTypes()

    val result = ArrayList(enabledCalendarTypes)
    for (key in CalendarType.values())
        if (!enabledCalendarTypes.contains(key))
            result.add(key)

    return result
}

@StringRes
fun getNextOwghatTimeId(current: Clock, dateHasChanged: Boolean): Int {
    if (coordinate == null) return 0

    if (prayTimes == null || dateHasChanged)
        prayTimes = PrayTimesCalculator.calculate(getCalculationMethod(), Date(), coordinate)

    val clock = current.toInt()

    prayTimes?.run {
        //TODO We like to show Imsak only in Ramadan
        return when {
            fajrClock.toInt() > clock -> R.string.fajr
            sunriseClock.toInt() > clock -> R.string.sunrise
            dhuhrClock.toInt() > clock -> R.string.dhuhr
            asrClock.toInt() > clock -> R.string.asr
            sunsetClock.toInt() > clock -> R.string.sunset
            maghribClock.toInt() > clock -> R.string.maghrib
            ishaClock.toInt() > clock -> R.string.isha
            midnightClock.toInt() > clock -> R.string.midnight
            else
                // TODO: this is today's, not tomorrow
            -> R.string.fajr
        }
    }
    return 0
}

fun getClockFromStringId(@StringRes stringId: Int): Clock {
    prayTimes?.run {
        return when (stringId) {
            R.string.imsak -> imsakClock
            R.string.fajr -> fajrClock
            R.string.sunrise -> sunriseClock
            R.string.dhuhr -> dhuhrClock
            R.string.asr -> asrClock
            R.string.sunset -> sunsetClock
            R.string.maghrib -> maghribClock
            R.string.isha -> ishaClock
            R.string.midnight -> midnightClock
            else -> Clock.fromInt(0)
        }
    }
    return Clock.fromInt(0)
}

fun monthsNamesOfCalendar(date: AbstractDate): Array<String>? {
    return when (date) {
        is PersianDate -> persianMonths
        is IslamicDate -> islamicMonths
        else -> gregorianMonths
    }
}

fun getMonthName(date: AbstractDate): String {
    val months = monthsNamesOfCalendar(date) ?: return ""
    return months[date.month - 1]
}

fun getWeekDayName(date: AbstractDate): String {
    val civilDate = if (date is CivilDate)
        date
    else
        CivilDate(date)

    weekDays?.let {
        return it[civilDateToCalendar(civilDate).get(Calendar.DAY_OF_WEEK) % 7]
    }
    return ""
}

fun loadAlarms(context: Context) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    val prefString = prefs.getString(PREF_ATHAN_ALARM, "")
    Log.d(TAG, "reading and loading all alarms from prefs: $prefString")
    val calculationMethod = getCalculationMethod()

    if (calculationMethod != null && coordinate != null && !TextUtils.isEmpty(prefString)) {
        var athanGap: Long = 0
        try {
            val athanGapStr = prefs.getString(PREF_ATHAN_GAP, "0")
            athanGapStr?.let {
                athanGap = (java.lang.Double.parseDouble(it) * 60.0 * 1000.0).toLong()
            }
        } catch (e: NumberFormatException) {
        }

        val prayTimes = PrayTimesCalculator.calculate(
            calculationMethod,
            Date(), coordinate
        )
        // convert spacedComma separated string to a set
        val alarmTimesSet = HashSet(listOf(*TextUtils.split(prefString, ",")))

        val alarmTimesNames = alarmTimesSet.toTypedArray()
        for (i in alarmTimesNames.indices) {
            val alarmTime: Clock = when (alarmTimesNames[i]) {
                "DHUHR" -> prayTimes.dhuhrClock
                "ASR" -> prayTimes.asrClock
                "MAGHRIB" -> prayTimes.maghribClock
                "ISHA" -> prayTimes.ishaClock
                "FAJR" -> prayTimes.fajrClock
                // a better to have default
                else -> prayTimes.fajrClock
            }

            setAlarm(context, alarmTimesNames[i], alarmTime, i, athanGap)
        }
    }
    //        for (Reminder event : Utils.getReminderDetails()) {
    //            ReminderUtils.turnOn(context, event);
    //        }
}

private fun setAlarm(
    context: Context, alarmTimeName: String, clock: Clock, ord: Int,
    athanGap: Long
) {
    val triggerTime = Calendar.getInstance()
    triggerTime.set(Calendar.HOUR_OF_DAY, clock.hour)
    triggerTime.set(Calendar.MINUTE, clock.minute)
    setAlarm(context, alarmTimeName, triggerTime.timeInMillis, ord, athanGap)
}

private fun setAlarm(
    context: Context, alarmTimeName: String, timeInMillis: Long, ord: Int,
    athanGap: Long
) {
    val triggerTime = Calendar.getInstance()
    triggerTime.timeInMillis = timeInMillis - athanGap
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?

    // don't set an alarm in the past
    if (alarmManager != null && !triggerTime.before(Calendar.getInstance())) {
        Log.d(TAG, "setting alarm for: " + triggerTime.time)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARMS_BASE_ID + ord,
            Intent(context, BroadcastReceivers::class.java)
                .putExtra(KEY_EXTRA_PRAYER_KEY, alarmTimeName)
                .setAction(BROADCAST_ALARM),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        when {
            Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1 -> alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime.timeInMillis,
                pendingIntent
            )
            Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2 -> alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime.timeInMillis,
                pendingIntent
            )
            else -> alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime.timeInMillis,
                pendingIntent
            )
        }
    }
}

fun getOrderedCalendarEntities(context: Context): List<CalendarTypeItem> {
    applyAppLanguage(context)

    val values = context.resources.getStringArray(R.array.calendar_values)
    val titles = context.resources.getStringArray(R.array.calendar_type)

    // TODO: Can be done without Map
    val typeTitleMap = HashMap<CalendarType, String>()
    for (i in titles.indices) {
        typeTitleMap[CalendarType.valueOf(values[i])] = titles[i]
    }

    val result = ArrayList<CalendarTypeItem>()
    getOrderedCalendarTypes()?.run {
        for (type in this) {
            typeTitleMap[type]?.let {
                result.add(CalendarTypeItem(type, it))
            }
        }
    }

    return result
}

fun getDayIconResource(day: Int): Int {
    try {
        if (preferredDigits.contentEquals(ARABIC_DIGITS))
            return DAYS_ICONS_AR[day]
        else if (preferredDigits.contentEquals(ARABIC_INDIC_DIGITS))
            return DAYS_ICONS_CKB[day]
        return DAYS_ICONS[day]
    } catch (e: IndexOutOfBoundsException) {
        Log.e(TAG, "No such field is available")
        return 0
    }
}

private fun readStream(inputStream: InputStream): String {
    // http://stackoverflow.com/a/5445161
    val s = Scanner(inputStream).useDelimiter("\\A")
    return if (s.hasNext()) s.next() else ""
}

fun readRawResource(context: Context, @RawRes res: Int): String =
    readStream(context.resources.openRawResource(res))

fun formatCoordinate(context: Context, coordinate: Coordinate, separator: String): String =
    String.format(
        Locale.getDefault(), "%s: %.4f%s%s: %.4f",
        context.getString(R.string.latitude), coordinate.latitude, separator,
        context.getString(R.string.longitude), coordinate.longitude
    )

fun getCityName(context: Context, fallbackToCoord: Boolean): String {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val cityEntity = getCityFromPreference(context)
    if (cityEntity != null) {
        if (language == LANG_EN_IR || language == LANG_EN_US || language == LANG_JA)
            return cityEntity.en
        else if (language == LANG_CKB)
            return cityEntity.ckb
        return cityEntity.fa
    }

    val geocodedCityName = prefs.getString(PREF_GEOCODED_CITYNAME, "")
    geocodedCityName?.let {
        if (!TextUtils.isEmpty(it))
            return it
    }

    if (fallbackToCoord)
        coordinate?.let {
            return formatCoordinate(context, it, spacedComma)
        }

    return ""
}

private fun getCityFromPreference(context: Context): CityItem? {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    val key = prefs.getString(PREF_SELECTED_LOCATION, "")
    if (TextUtils.isEmpty(key) || key == DEFAULT_CITY)
        return null

    if (key == cachedCityKey)
        return cachedCity

    // cache last query even if no city available under the key, useful in case invalid
    // value is somehow inserted on the preference
    key?.let {
        cachedCityKey = key
    }

    for (cityEntity in getAllCities(context, false))
        if (cityEntity.key == key) {
            cachedCity = cityEntity
            return cachedCity
        }

    cachedCity = null
    return cachedCity
}

fun getCoordinate(context: Context): Coordinate? {
    val cityEntity = getCityFromPreference(context)
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    if (cityEntity != null)
        return cityEntity.coordinate

    try {
        val latitudeString = prefs.getString(PREF_LATITUDE, DEFAULT_LATITUDE)
        val longtitudeString = prefs.getString(PREF_LONGITUDE, DEFAULT_LONGITUDE)
        val altitudeString = prefs.getString(PREF_ALTITUDE, DEFAULT_ALTITUDE)

        var latitude = "0.0"
        var longtitude = "0.0"
        var altitude = "0.0"

        latitudeString?.let {
            latitude = it
        }

        longtitudeString?.let {
            longtitude = it
        }

        altitudeString?.let {
            altitude = it
        }

        val coord = Coordinate(
            java.lang.Double.parseDouble(latitude),
            java.lang.Double.parseDouble(longtitude),
            java.lang.Double.parseDouble(altitude)
        )

        // If latitude or longitude is zero probably preference is not set yet
        return if (coord.latitude == 0.0 && coord.longitude == 0.0) null
        else coord

    } catch (e: NumberFormatException) {
        return null
    }
}

fun fixDayOfWeekReverse(dayOfWeek: Int): Int = (dayOfWeek + 7 - weekStartOffset) % 7

fun getInitialOfWeekDay(position: Int): String {
    weekDaysInitials?.let {
        return it[position % 7]
    }
    return ""
}

// Extra helpers
fun getA11yDaySummary(
    context: Context, jdn: Long, isToday: Boolean,
    deviceCalendarEvents: SparseArray<ArrayList<DeviceCalendarEvent>>?,
    withZodiac: Boolean, withOtherCalendars: Boolean, withTitle: Boolean
): String {
    // It has some expensive calculations, lets not do that when not needed
    if (!isTalkBackEnabled()) return ""

    val result = StringBuilder()

    if (isToday) {
        result.append(context.getString(R.string.today))
        result.append("\n")
    }

    val mainDate = getDateFromJdnOfCalendar(getMainCalendar(), jdn)

    if (withTitle) {
        result.append("\n")
        result.append(dayTitleSummary(mainDate))
    }

    val shift = getShiftWorkTitle(jdn, false)
    if (!TextUtils.isEmpty(shift)) {
        result.append("\n")
        result.append(shift)
    }

    if (withOtherCalendars) {
        val otherCalendars = dateStringOfOtherCalendars(jdn, getSpacedComma())
        if (!TextUtils.isEmpty(otherCalendars)) {
            result.append("\n")
            result.append("\n")
            result.append(context.getString(R.string.equivalent_to))
            result.append(" ")
            result.append(otherCalendars)
        }
    }

    val events = getEvents(jdn, deviceCalendarEvents)
    val holidays = getEventsTitle(events, true, true, true, false)
    if (!TextUtils.isEmpty(holidays)) {
        result.append("\n")
        result.append("\n")
        result.append(context.getString(R.string.holiday_reason))
        result.append("\n")
        result.append(holidays)
    }

    val nonHolidays = getEventsTitle(events, false, true, true, false)
    if (!TextUtils.isEmpty(nonHolidays)) {
        result.append("\n")
        result.append("\n")
        result.append(context.getString(R.string.events))
        result.append("\n")
        result.append(nonHolidays)
    }

    if (isWeekOfYearEnabled()) {
        val startOfYearJdn = getDateOfCalendar(
            getMainCalendar(),
            mainDate.year, 1, 1
        ).toJdn()
        val weekOfYearStart = calculateWeekOfYear(jdn, startOfYearJdn)
        result.append("\n")
        result.append("\n")
        result.append(
            String.format(
                context.getString(R.string.nth_week_of_year),
                formatNumber(weekOfYearStart)
            )
        )
    }

    if (withZodiac) {
        val zodiac = getZodiacInfo(context, jdn, false)
        if (!TextUtils.isEmpty(zodiac)) {
            result.append("\n")
            result.append("\n")
            result.append(zodiac)
        }
    }

    return result.toString()
}


fun calculateWeekOfYear(jdn: Long, startOfYearJdn: Long): Int {
    val dayOfYear = jdn - startOfYearJdn
    return ceil(1 + (dayOfYear - fixDayOfWeekReverse(getDayOfWeekFromJdn(jdn))) / 7.0).toInt()
}

fun getTodayOfCalendar(calendar: CalendarType): AbstractDate =
    getDateFromJdnOfCalendar(calendar, getTodayJdn())

fun getTodayJdn(): Long = calendarToCivilDate(makeCalendarFromDate(Date())).toJdn()

fun getDayOfWeekFromJdn(jdn: Long): Int =
    civilDateToCalendar(CivilDate(jdn)).get(Calendar.DAY_OF_WEEK) % 7

fun getSpringEquinox(jdn: Long): Calendar =
    makeCalendarFromDate(Equinox.northwardEquinox(CivilDate(jdn).year))

fun makeCalendarFromDate(date: Date): Calendar {
    val calendar = Calendar.getInstance()
    if (isIranTime())
        calendar.timeZone = TimeZone.getTimeZone("Asia/Tehran")

    calendar.time = date
    return calendar
}

@StringRes
fun getPrayTimeText(athanKey: String?): Int = when (athanKey) {
    "FAJR" -> R.string.fajr
    "DHUHR" -> R.string.dhuhr
    "ASR" -> R.string.asr
    "MAGHRIB" -> R.string.maghrib
    "ISHA" -> R.string.isha
    else -> R.string.isha
}

@DrawableRes
fun getPrayTimeImage(athanKey: String?): Int = when (athanKey) {
    "FAJR" -> R.drawable.fajr
    "DHUHR" -> R.drawable.dhuhr
    "ASR" -> R.drawable.asr
    "MAGHRIB" -> R.drawable.maghrib
    "ISHA" -> R.drawable.isha
    else -> R.drawable.isha
}

fun getAllEnabledAppointments(context: Context): List<DeviceCalendarEvent> {
    val startingDate = Calendar.getInstance()
    startingDate.add(Calendar.YEAR, -1)
    val deviceCalendarEvent = SparseArray<ArrayList<DeviceCalendarEvent>>()
    val allEnabledAppointments = ArrayList<DeviceCalendarEvent>()
    readDeviceEvents(
        context, deviceCalendarEvent, allEnabledAppointments, startingDate,
        TimeUnit.DAYS.toMillis((365 * 2).toLong())
    )
    return allEnabledAppointments
}

fun readDayDeviceEvents(context: Context, jdn: Long): SparseArray<ArrayList<DeviceCalendarEvent>> {
    var jdn = jdn
    if (jdn == -1L)
        jdn = getTodayJdn()

    val startingDate = civilDateToCalendar(CivilDate(jdn))
    val deviceCalendarEvent = SparseArray<ArrayList<DeviceCalendarEvent>>()
    val allEnabledAppointments = ArrayList<DeviceCalendarEvent>()
    readDeviceEvents(
        context,
        deviceCalendarEvent,
        allEnabledAppointments,
        startingDate,
        DAY_IN_MILLIS
    )
    return deviceCalendarEvent
}

fun readMonthDeviceEvents(
    context: Context,
    jdn: Long
): SparseArray<ArrayList<DeviceCalendarEvent>> {
    val startingDate = civilDateToCalendar(CivilDate(jdn))
    val deviceCalendarEvent = SparseArray<ArrayList<DeviceCalendarEvent>>()
    val allEnabledAppointments = ArrayList<DeviceCalendarEvent>()
    readDeviceEvents(
        context,
        deviceCalendarEvent,
        allEnabledAppointments,
        startingDate,
        32L * DAY_IN_MILLIS
    )
    return deviceCalendarEvent
}

private fun readDeviceEvents(
    context: Context,
    deviceCalendarEvents: SparseArray<ArrayList<DeviceCalendarEvent>>,
    allEnabledAppointments: ArrayList<DeviceCalendarEvent>,
    startingDate: Calendar,
    rangeInMillis: Long
) {
    if (!isShowDeviceCalendarEvents()) return

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) != PackageManager.PERMISSION_GRANTED
    ) return

    try {
        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, startingDate.timeInMillis - DAY_IN_MILLIS)
        ContentUris.appendId(builder, startingDate.timeInMillis + rangeInMillis + DAY_IN_MILLIS)

        val cursor = context.contentResolver.query(
            builder.build(),
            arrayOf(
                CalendarContract.Instances.EVENT_ID, // 0
                CalendarContract.Instances.TITLE, // 1
                CalendarContract.Instances.DESCRIPTION, // 2
                CalendarContract.Instances.BEGIN, // 3
                CalendarContract.Instances.END, // 4
                CalendarContract.Instances.EVENT_LOCATION, // 5
                CalendarContract.Instances.RRULE, // 6
                CalendarContract.Instances.VISIBLE, // 7
                CalendarContract.Instances.ALL_DAY, // 8
                CalendarContract.Instances.EVENT_COLOR     // 9
            ), null, null, null
        ) ?: return

        var i = 0
        while (cursor.moveToNext()) {
            if (cursor.getString(7) != "1")
                continue

            var allDay = false
            if (cursor.getString(8) == "1")
                allDay = true

            val startDate = Date(cursor.getLong(3))
            val endDate = Date(cursor.getLong(4))
            val startCalendar = makeCalendarFromDate(startDate)
            val endCalendar = makeCalendarFromDate(endDate)

            val civilDate = calendarToCivilDate(startCalendar)

            val month = civilDate.month
            val day = civilDate.dayOfMonth

            var list: ArrayList<DeviceCalendarEvent>? =
                deviceCalendarEvents.get(month * 100 + day)
            if (list == null) {
                list = ArrayList()
                deviceCalendarEvents.put(month * 100 + day, list)
            }

            var title = cursor.getString(1)
            if (allDay)
                title = "\uD83D\uDCC5 $title"
            else {
                title = "\uD83D\uDD53 $title"
                title += " (" + baseFormatClock(
                    startCalendar.get(Calendar.HOUR_OF_DAY),
                    startCalendar.get(Calendar.MINUTE)
                )

                if (cursor.getLong(3) != cursor.getLong(4) && cursor.getLong(4) != 0L)
                    title += "-" + baseFormatClock(
                        endCalendar.get(Calendar.HOUR_OF_DAY),
                        endCalendar.get(Calendar.MINUTE)
                    )

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
                cursor.getString(9)
            )
            list.add(event)
            allEnabledAppointments.add(event)

            // Don't go more than 1k events on any case
            if (++i == 1000) break
        }
        cursor.close()
    } catch (e: Exception) {
        // We don't like crash addition from here, just catch all of exceptions
        Log.e("", "Error on device calendar events read", e)
    }

}

private fun baseFormatClock(hour: Int, minute: Int): String =
    formatNumber(String.format(Locale.ENGLISH, "%d:%02d", hour, minute))

fun getFormattedClock(clock: Clock, forceIn12: Boolean): String {
    val in12 = !clockIn24 || forceIn12
    if (!in12) return baseFormatClock(clock.hour, clock.minute)

    var hour = clock.hour
    val suffix: String
    if (hour >= 12) {
        suffix = getPmString()
        hour -= 12
    } else
        suffix = getAmString()

    return baseFormatClock(hour, clock.minute) + " " + suffix
}

fun getMonthLength(calendar: CalendarType, year: Int, month: Int): Int {
    val yearOfNextMonth = if (month == 12) year + 1 else year
    val nextMonth = if (month == 12) 1 else month + 1
    return (getDateOfCalendar(calendar, yearOfNextMonth, nextMonth, 1).toJdn() - getDateOfCalendar(
        calendar,
        year,
        month,
        1
    ).toJdn()).toInt()
}

fun getCalendarTypeFromDate(date: AbstractDate): CalendarType = when (date) {
    is IslamicDate -> CalendarType.ISLAMIC
    is CivilDate -> CalendarType.GREGORIAN
    else -> CalendarType.SHAMSI
}

fun getDateFromJdnOfCalendar(calendar: CalendarType, jdn: Long): AbstractDate =
    when (calendar) {
        CalendarType.ISLAMIC -> IslamicDate(jdn)
        CalendarType.GREGORIAN -> CivilDate(jdn)
        CalendarType.SHAMSI -> PersianDate(jdn)
        else -> PersianDate(jdn)
    }

fun getDateOfCalendar(calendar: CalendarType, year: Int, month: Int, day: Int): AbstractDate =
    when (calendar) {
        CalendarType.ISLAMIC -> IslamicDate(year, month, day)
        CalendarType.GREGORIAN -> CivilDate(year, month, day)
        CalendarType.SHAMSI -> PersianDate(year, month, day)
        else -> PersianDate(year, month, day)
    }


//    public static List<Reminder> getReminderDetails() {
//        return sReminderDetails;
//    }