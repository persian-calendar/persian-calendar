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

//    public static List<Reminder> getReminderDetails() {
//        return sReminderDetails;
//    }

fun getShiftWorks(): ArrayList<ShiftWorkRecord> = ArrayList(sShiftWorks)

fun getAmString(): String = sAM

fun getPmString(): String = sPM

fun getShiftWorkStartingJdn(): Long = sShiftWorkStartingJdn

fun getShiftWorkRecurs(): Boolean = sShiftWorkRecurs

fun getShiftWorkTitles(): Map<String, String> = sShiftWorkTitles

fun getMaxSupportedYear(): Int = 1398

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

fun isNightModeEnabled(context: Context): Boolean = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

fun getThemeFromPreference(context: Context, prefs: SharedPreferences): String? {
    var result = prefs.getString(PREF_THEME, "")
    if (TextUtils.isEmpty(result))
        result = if (isNightModeEnabled(context)) DARK_THEME else LIGHT_THEME
    return result
}

@StyleRes
fun getAppTheme(): Int = appTheme

fun getIslamicOffset(context: Context): Int {
    return try {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val islamicOffset = prefs.getString(PREF_ISLAMIC_OFFSET, DEFAULT_ISLAMIC_OFFSET)
        Integer.parseInt(islamicOffset!!.replace("+", ""))
    } catch (ignore: Exception) {
        0
    }
}
fun isAstronomicalFeaturesEnabled(): Boolean = astronomicalFeaturesEnabled

fun getEnabledCalendarTypes(): List<CalendarType> {
    val result = ArrayList<CalendarType>()
    result.add(Utils.getMainCalendar())
    result.addAll(listOf(*otherCalendars))
    return result
}