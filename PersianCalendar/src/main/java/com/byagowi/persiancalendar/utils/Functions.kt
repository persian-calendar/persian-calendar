package com.byagowi.persiancalendar.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.text.TextUtils
import android.view.View
import androidx.annotation.StyleRes
import androidx.preference.PreferenceManager
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.calendar.AbstractDate
import com.byagowi.persiancalendar.calendar.CivilDate
import com.byagowi.persiancalendar.calendar.IslamicDate
import com.byagowi.persiancalendar.calendar.PersianDate
import com.byagowi.persiancalendar.entities.ShiftWorkRecord
import com.google.android.material.circularreveal.CircularRevealCompat
import com.google.android.material.circularreveal.CircularRevealWidget
import java.util.ArrayList
import kotlin.math.sqrt

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

fun isNightModeEnabled(context: Context): Boolean = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

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