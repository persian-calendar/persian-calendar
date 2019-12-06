package com.byagowi.persiancalendar.utils

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.*
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.preference.PreferenceManager
import androidx.work.*
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.CalendarTypeItem
import com.byagowi.persiancalendar.entities.CityItem
import com.byagowi.persiancalendar.service.ApplicationService
import com.byagowi.persiancalendar.service.BroadcastReceivers
import com.byagowi.persiancalendar.service.UpdateWorker
import com.google.android.material.circularreveal.CircularRevealCompat
import com.google.android.material.circularreveal.CircularRevealWidget
import com.google.android.material.snackbar.Snackbar
import io.github.persiancalendar.Equinox
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.praytimes.Clock
import io.github.persiancalendar.praytimes.Coordinate
import io.github.persiancalendar.praytimes.PrayTimesCalculator
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.sqrt

// This should be called before any use of Utils on the activity and services
fun initUtils(context: Context) {
    updateStoredPreference(context)
    applyAppLanguage(context)
    loadLanguageResource(context)
    loadAlarms(context)
    loadEvents(context)
}

val supportedYearOfIranCalendar: Int
    get() = 1399

fun isArabicDigitSelected(): Boolean = when (preferredDigits) {
    ARABIC_DIGITS -> true
    else -> false
}

fun goForWorker(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

fun toLinearDate(date: AbstractDate): String = String.format(
    "%s/%s/%s", formatNumber(date.year),
    formatNumber(date.month), formatNumber(date.dayOfMonth)
)

fun isNightModeEnabled(context: Context): Boolean =
    context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

fun formatDate(date: AbstractDate): String = if (numericalDatePreferred)
    (toLinearDate(date) + " " + getCalendarNameAbbr(date)).trim()
else
    String.format(
        if (language == LANG_CKB) "%sی %sی %s" else "%s %s %s",
        formatNumber(date.dayOfMonth), getMonthName(date),
        formatNumber(date.year)
    )

fun isNonArabicScriptSelected() = when (language) {
    LANG_EN_US, LANG_JA -> true
    else -> false
}

// en-US and ja are our only real LTR locales for now
fun isLocaleRTL(): Boolean = when (language) {
    LANG_EN_US, LANG_JA -> false
    else -> true
}

fun formatNumber(number: Int): String = formatNumber(number.toString())

fun formatNumber(number: String): String = when (preferredDigits) {
    ARABIC_DIGITS -> number
    else -> number.toCharArray().map {
        if (Character.isDigit(it)) preferredDigits[Character.getNumericValue(it)] else it
    }.joinToString("")
}

// https://stackoverflow.com/a/52557989
fun <T> circularRevealFromMiddle(circularRevealWidget: T) where T : View?, T : CircularRevealWidget {
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

fun getThemeFromPreference(context: Context, prefs: SharedPreferences): String =
    prefs.getString(PREF_THEME, null)
        ?: if (isNightModeEnabled(context)) DARK_THEME else LIGHT_THEME

fun getEnabledCalendarTypes(): List<CalendarType> = listOf(mainCalendar) + otherCalendars

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
                Intent(context, BroadcastReceivers::class.java).setAction(BROADCAST_UPDATE_APP),
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

fun getOrderedCalendarTypes(): List<CalendarType> = getEnabledCalendarTypes().let {
    it + (CalendarType.values().toList() - it)
}

fun getClockFromStringId(@StringRes stringId: Int): Clock = prayTimes?.run {
    when (stringId) {
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
} ?: Clock.fromInt(0)

fun loadAlarms(context: Context) {
    val prefString = context.appPrefs.getString(PREF_ATHAN_ALARM, null)?.trim() ?: ""
    Log.d(TAG, "reading and loading all alarms from prefs: $prefString")

    if (coordinate != null && prefString.isNotEmpty()) {
        val athanGap =
            ((context.appPrefs.getString(PREF_ATHAN_GAP, null)?.toDoubleOrNull() ?: .0)
                    * 60.0 * 1000.0).toLong()

        val prayTimes = PrayTimesCalculator.calculate(
            calculationMethod,
            Date(), coordinate
        )
        // convert spacedComma separated string to a set
        prefString.split(",").toSet().forEachIndexed { i, name ->
            val alarmTime: Clock = when (name) {
                "DHUHR" -> prayTimes.dhuhrClock
                "ASR" -> prayTimes.asrClock
                "MAGHRIB" -> prayTimes.maghribClock
                "ISHA" -> prayTimes.ishaClock
                "FAJR" -> prayTimes.fajrClock
                // a better to have default
                else -> prayTimes.fajrClock
            }

            setAlarm(context, name, Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, alarmTime.hour)
                set(Calendar.MINUTE, alarmTime.minute)
            }.timeInMillis, i, athanGap)
        }
    }
}

private fun setAlarm(
    context: Context, alarmTimeName: String, timeInMillis: Long, ord: Int, athanGap: Long
) {
    val triggerTime = Calendar.getInstance()
    triggerTime.timeInMillis = timeInMillis - athanGap
    val alarmManager = context.getSystemService<AlarmManager>()

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
    val typeTitleMap = context.resources.getStringArray(R.array.calendar_values)
        .map(CalendarType::valueOf)
        .zip(context.resources.getStringArray(R.array.calendar_type))
        .toMap()
    return getOrderedCalendarTypes().mapNotNull {
        typeTitleMap[it]?.run { CalendarTypeItem(it, this) }
    }
}

fun getDayIconResource(day: Int): Int = try {
    when (preferredDigits) {
        ARABIC_DIGITS -> DAYS_ICONS_ARABIC[day]
        ARABIC_INDIC_DIGITS -> DAYS_ICONS_ARABIC_INDIC[day]
        else -> DAYS_ICONS_PERSIAN[day]
    }
} catch (e: IndexOutOfBoundsException) {
    Log.e(TAG, "No such field is available", e)
    0
}

fun readRawResource(context: Context, @RawRes res: Int) =
    context.resources.openRawResource(res).use { String(it.readBytes()) }

fun formatCoordinate(context: Context, coordinate: Coordinate, separator: String) = String.format(
    Locale.getDefault(), "%s: %.4f%s%s: %.4f",
    context.getString(R.string.latitude), coordinate.latitude, separator,
    context.getString(R.string.longitude), coordinate.longitude
)

fun getCityName(context: Context, fallbackToCoord: Boolean): String =
    getCityFromPreference(context)?.let {
        when (language) {
            LANG_EN_IR, LANG_EN_US, LANG_JA -> it.en
            LANG_CKB -> it.ckb
            else -> it.fa
        }
    } ?: context.appPrefs.getString(PREF_GEOCODED_CITYNAME, null)?.takeUnless { it.isEmpty() }
    ?: coordinate?.takeIf { fallbackToCoord }?.let { formatCoordinate(context, it, spacedComma) }
    ?: ""

fun getCoordinate(context: Context): Coordinate? =
    getCityFromPreference(context)?.coordinate ?: context.appPrefs.run {
        Coordinate(
            getString(PREF_LATITUDE, null)?.toDoubleOrNull() ?: .0,
            getString(PREF_LONGITUDE, null)?.toDoubleOrNull() ?: .0,
            getString(PREF_ALTITUDE, null)?.toDoubleOrNull() ?: .0
        ).takeUnless { it.latitude == 0.0 && it.longitude == 0.0 }
        // If latitude or longitude is zero probably preference is not set yet
    }

fun getTodayOfCalendar(calendar: CalendarType) = getDateFromJdnOfCalendar(calendar, getTodayJdn())

fun getTodayJdn(): Long = calendarToCivilDate(makeCalendarFromDate(Date())).toJdn()

fun getSpringEquinox(jdn: Long) =
    makeCalendarFromDate(Equinox.northwardEquinox(CivilDate(jdn).year))

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

fun getDateFromJdnOfCalendar(calendar: CalendarType, jdn: Long): AbstractDate = when (calendar) {
    CalendarType.ISLAMIC -> IslamicDate(jdn)
    CalendarType.GREGORIAN -> CivilDate(jdn)
    CalendarType.SHAMSI -> PersianDate(jdn)
}

@StyleRes
fun getThemeFromName(name: String): Int = when (name) {
    DARK_THEME -> R.style.DarkTheme
    MODERN_THEME -> R.style.ModernTheme
    BLUE_THEME -> R.style.BlueTheme
    LIGHT_THEME -> R.style.LightTheme
    else -> R.style.LightTheme
}

fun isRTL(context: Context): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
        context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
    else false

fun toggleShowDeviceCalendarOnPreference(context: Context, enable: Boolean) =
    context.appPrefs.edit { putBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, enable) }

fun askForLocationPermission(activity: Activity?) {
    if (activity == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return

    AlertDialog.Builder(activity)
        .setTitle(R.string.location_access)
        .setMessage(R.string.phone_location_required)
        .setPositiveButton(R.string.continue_button) { _, _ ->
            activity.requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
        .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }.show()
}

fun askForCalendarPermission(activity: Activity?) {
    if (activity == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return

    AlertDialog.Builder(activity)
        .setTitle(R.string.calendar_access)
        .setMessage(R.string.phone_calendar_required)
        .setPositiveButton(R.string.continue_button) { _, _ ->
            activity.requestPermissions(
                arrayOf(Manifest.permission.READ_CALENDAR),
                CALENDAR_READ_PERMISSION_REQUEST_CODE
            )
        }
        .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }.show()
}

fun copyToClipboard(view: View?, label: CharSequence?, text: CharSequence?) {
    view ?: return
    val clipboardService = view.context.getSystemService<ClipboardManager>()

    if (clipboardService == null || label == null || text == null) return

    clipboardService.setPrimaryClip(ClipData.newPlainText(label, text))
    Snackbar.make(
        view,
        String.format(view.context.getString(R.string.date_copied_clipboard), text),
        Snackbar.LENGTH_SHORT
    ).show()
}

fun dateStringOfOtherCalendars(jdn: Long, separator: String) =
    otherCalendars.joinToString(separator) { formatDate(getDateFromJdnOfCalendar(it, jdn)) }

private fun calculateDiffToChangeDate(): Long = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 1)
}.timeInMillis / 1000 + DAY_IN_SECOND - Calendar.getInstance().time.time / 1000

fun setChangeDateWorker(context: Context) {
    val remainedSeconds = calculateDiffToChangeDate()
    val changeDateWorker = OneTimeWorkRequest.Builder(UpdateWorker::class.java)
        .setInitialDelay(
            remainedSeconds,
            TimeUnit.SECONDS
        )// Use this when you want to add initial delay or schedule initial work to `OneTimeWorkRequest` e.g. setInitialDelay(2, TimeUnit.HOURS)
        .build()

    WorkManager.getInstance(context).beginUniqueWork(
        CHANGE_DATE_TAG,
        ExistingWorkPolicy.REPLACE,
        changeDateWorker
    ).enqueue()
}

fun String.splitIgnoreEmpty(delim: String) = this.split(delim).filter { it.isNotEmpty() }

fun startEitherServiceOrWorker(context: Context) {
    val workManager = WorkManager.getInstance(context)
    if (goForWorker()) {
        val updateBuilder = PeriodicWorkRequest.Builder(UpdateWorker::class.java, 1, TimeUnit.HOURS)

        val updateWork = updateBuilder.build()
        workManager.enqueueUniquePeriodicWork(
            UPDATE_TAG,
            ExistingPeriodicWorkPolicy.REPLACE,
            updateWork
        )
    } else {
        // Disable all the scheduled workers, just in case enabled before
        workManager.cancelAllWork()
        // Or,
        // workManager.cancelAllWorkByTag(UPDATE_TAG);
        // workManager.cancelUniqueWork(CHANGE_DATE_TAG);

        val isRunning = context.getSystemService<ActivityManager>()?.let { am ->
            try {
                am.getRunningServices(Integer.MAX_VALUE).any {
                    ApplicationService::class.java.name == it.service.className
                }
            } catch (e: Exception) {
                Log.e(TAG, "startEitherServiceOrWorker service's first part fail", e)
                false
            }
        } ?: false

        if (!isRunning) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    ContextCompat.startForegroundService(
                        context,
                        Intent(context, ApplicationService::class.java)
                    )

                context.startService(Intent(context, ApplicationService::class.java))
            } catch (e: Exception) {
                Log.e(TAG, "startEitherServiceOrWorker service's second part fail", e)
            }
        }
    }
}

fun getShiftWorkTitle(jdn: Long, abbreviated: Boolean): String {
    if (shiftWorkStartingJdn == -1L || jdn < shiftWorkStartingJdn || shiftWorkPeriod == 0)
        return ""

    val passedDays = jdn - shiftWorkStartingJdn
    if (!shiftWorkRecurs && passedDays >= shiftWorkPeriod) return ""

    val dayInPeriod = (passedDays % shiftWorkPeriod).toInt()

    var accumulation = 0
    val type = shiftWorks.firstOrNull {
        accumulation += it.length
        accumulation > dayInPeriod
    }?.type ?: return ""

    // Skip rests on abbreviated mode
    if (shiftWorkRecurs && abbreviated && (type == "r" || type == shiftWorkTitles["r"])) return ""

    val title = shiftWorkTitles[type] ?: type
    return if (abbreviated && title.isNotEmpty()) title.substring(0, 1) + when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && language != LANG_AR -> ZWJ
        else -> ""
    } else title
}

fun getAllCities(context: Context, needsSort: Boolean): List<CityItem> {
    val result = mutableListOf<CityItem>()
    try {
        fun JSONObject.forEach(f: (String, JSONObject) -> Unit) =
            this.keys().asSequence().forEach { f(it, this.getJSONObject(it)) }

        JSONObject(readRawResource(context, R.raw.cities)).forEach { countryCode, country ->
            val countryEn = country.getString("en")
            val countryFa = country.getString("fa")
            val countryCkb = country.getString("ckb")
            val countryAr = country.getString("ar")

            country.getJSONObject("cities").forEach { key, city ->
                result.add(
                    CityItem(
                        key = key,
                        en = city.getString("en"), fa = city.getString("fa"),
                        ckb = city.getString("ckb"), ar = city.getString("ar"),
                        countryCode = countryCode,
                        countryEn = countryEn, countryFa = countryFa,
                        countryCkb = countryCkb, countryAr = countryAr,
                        coordinate = Coordinate(
                            city.getDouble("latitude"),
                            city.getDouble("longitude"),
                            // Don't Consider elevation for Iran
                            if (countryCode == "ir") 0.0 else city.getDouble("elevation")
                        )
                    )
                )
            }
        }
    } catch (e: JSONException) {
        e.printStackTrace()
    }

    if (!needsSort) return result

    val irCodeOrder = listOf("zz", "ir", "af", "iq")
    val afCodeOrder = listOf("zz", "af", "ir", "iq")
    val arCodeOrder = listOf("zz", "iq", "ir", "af")

    fun getCountryCodeOrder(countryCode: String): Int =
        when (language) {
            LANG_FA_AF, LANG_PS -> afCodeOrder.indexOf(countryCode)
            LANG_AR -> arCodeOrder.indexOf(countryCode)
            LANG_FA, LANG_GLK, LANG_AZB -> irCodeOrder.indexOf(countryCode)
            else -> irCodeOrder.indexOf(countryCode)
        }

    fun prepareForArabicSort(text: String): String =
        text
            .replace("ی", "ي")
            .replace("ک", "ك")
            .replace("گ", "كی")
            .replace("ژ", "زی")
            .replace("چ", "جی")
            .replace("پ", "بی")
            .replace("ڕ", "ری")
            .replace("ڵ", "لی")
            .replace("ڤ", "فی")
            .replace("ۆ", "وی")
            .replace("ێ", "یی")
            .replace("ھ", "نی")
            .replace("ە", "هی")

    return result.sortedWith(kotlin.Comparator { l, r ->
        if (l.key == "") return@Comparator -1

        if (r.key == DEFAULT_CITY) return@Comparator 1

        val compare = getCountryCodeOrder(l.countryCode) - getCountryCodeOrder(r.countryCode)
        if (compare != 0) return@Comparator compare

        when (language) {
            LANG_EN_US, LANG_JA, LANG_EN_IR -> l.en.compareTo(r.en)
            LANG_AR -> l.ar.compareTo(r.ar)
            LANG_CKB -> prepareForArabicSort(l.ckb).compareTo(prepareForArabicSort(r.ckb))
            else -> prepareForArabicSort(l.fa).compareTo(prepareForArabicSort(r.fa))
        }
    })
}

val Context.appPrefs: SharedPreferences
    get() = PreferenceManager.getDefaultSharedPreferences(this)

val Context.layoutInflater: LayoutInflater
    get() = LayoutInflater.from(this)
