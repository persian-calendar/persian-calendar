package com.byagowi.persiancalendar.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.util.SparseArray
import android.view.View
import androidx.annotation.StyleRes
import androidx.preference.PreferenceManager
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.calendar.AbstractDate
import com.byagowi.persiancalendar.calendar.CivilDate
import com.byagowi.persiancalendar.calendar.IslamicDate
import com.byagowi.persiancalendar.calendar.PersianDate
import com.byagowi.persiancalendar.entities.*
import com.byagowi.persiancalendar.utils.Utils.*
import com.google.android.material.circularreveal.CircularRevealCompat
import com.google.android.material.circularreveal.CircularRevealWidget
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.math.sqrt

private var sAllEnabledEvents: List<AbstractEvent<*>> = ArrayList()

fun getAllEnabledEvents(): List<AbstractEvent<*>> = sAllEnabledEvents

@StyleRes
fun getAppTheme(): Int = appTheme

fun getMaxSupportedYear(): Int = 1398

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

fun formatNumber(number: Int): String = formatNumber(Integer.toString(number))

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

fun isIranHolidaysEnabled(): Boolean = sIsIranHolidaysEnabled

fun fixDayOfWeek(dayOfWeek: Int): Int = (dayOfWeek + weekStartOffset) % 7

fun goForWorker(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

fun getWeekDayName(position: Int): String = weekDays[position % 7]

fun isTalkBackEnabled(): Boolean = talkBackEnabled

fun isCenterAlignWidgets(): Boolean = centerAlignWidgets

fun getSpacedComma(): String = spacedComma

fun isNotifyDateOnLockScreen(): Boolean = notifyInLockScreen

fun formatDayAndMonth(day: Int, month: String): String = String.format(if (language == LANG_CKB) "%sی %s" else "%s %s", formatNumber(day), month)

fun toLinearDate(date: AbstractDate): String = String.format("%s/%s/%s", formatNumber(date.year),
        formatNumber(date.month), formatNumber(date.dayOfMonth))

fun isNightModeEnabled(context: Context): Boolean = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

fun formatDate(date: AbstractDate): String =
        if (numericalDatePreferred)
            (toLinearDate(date) + " " + getCalendarNameAbbr(date)).trim { it <= ' ' }
        else
            String.format(if (getAppLanguage() == LANG_CKB) "%sی %sی %s" else "%s %s %s",
                    formatNumber(date.dayOfMonth), getMonthName(date),
                    formatNumber(date.year))

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
            LANG_FA_AF, LANG_PS, LANG_UR, LANG_AR, LANG_CKB, LANG_EN_US, LANG_JA -> IslamicDate.useUmmAlQura = true
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
                title += formatDayAndMonth(day, persianMonths[month - 1]) + ")"

                var list: ArrayList<PersianCalendarEvent>? = persianCalendarEvents.get(month * 100 + day)
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
                title += formatDayAndMonth(day, islamicMonths[month - 1]) + ")"
                var list: ArrayList<IslamicCalendarEvent>? = islamicCalendarEvents.get(month * 100 + day)
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
                title += " (" + formatDayAndMonth(day, gregorianMonths[month - 1]) + ")"
                var list: ArrayList<GregorianCalendarEvent>? = gregorianCalendarEvents.get(month * 100 + day)
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

            val viewDiagonal = sqrt((viewWidth * viewWidth + viewHeight * viewHeight).toDouble()).toInt()

            AnimatorSet().apply {
                playTogether(
                        CircularRevealCompat.createCircularReveal(circularRevealWidget,
                                (viewWidth / 2).toFloat(), (viewHeight / 2).toFloat(),
                                10f, (viewDiagonal / 2).toFloat()),
                        ObjectAnimator.ofArgb(circularRevealWidget,
                                CircularRevealWidget.CircularRevealScrimColorProperty
                                        .CIRCULAR_REVEAL_SCRIM_COLOR,
                                Color.GRAY, Color.TRANSPARENT))
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


//    public static List<Reminder> getReminderDetails() {
//        return sReminderDetails;
//    }