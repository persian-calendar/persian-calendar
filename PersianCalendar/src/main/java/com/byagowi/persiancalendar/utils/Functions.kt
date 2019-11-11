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
import android.media.AudioManager
import android.os.Build
import android.util.Log
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.TextView
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
import com.byagowi.persiancalendar.entities.ShiftWorkRecord
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
import io.github.persiancalendar.praytimes.CalculationMethod
import io.github.persiancalendar.praytimes.Clock
import io.github.persiancalendar.praytimes.Coordinate
import io.github.persiancalendar.praytimes.PrayTimesCalculator
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.InputStream
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.math.sqrt

// This should be called before any use of Utils on the activity and services
fun initUtils(context: Context) {
    updateStoredPreference(context)
    applyAppLanguage(context)
    loadLanguageResource(context)
    loadAlarms(context)
    loadEvents(context)
}

fun getMaxSupportedYear(): Int = 1398

fun isShownOnWidgets(infoType: String): Boolean = whatToShowOnWidgets.contains(infoType)

fun isArabicDigitSelected(): Boolean = preferredDigits.contentEquals(ARABIC_DIGITS)

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

fun getCalculationMethod(): CalculationMethod = CalculationMethod.valueOf(calculationMethod)

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

fun formatNumber(number: String): String =
    if (preferredDigits.contentEquals(ARABIC_DIGITS)) number else number.toCharArray().map {
        if (Character.isDigit(it)) preferredDigits[Character.getNumericValue(it)] else it
    }.joinToString("")

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

    try {
        val messages = JSONObject(readRawResource(context, messagesFile))

        fun JSONArray.toStringList() = (0 until length()).map { getString(it) }

        persianMonths = messages.getJSONArray("PersianCalendarMonths").toStringList()
        islamicMonths = messages.getJSONArray("IslamicCalendarMonths").toStringList()
        gregorianMonths = messages.getJSONArray("GregorianCalendarMonths").toStringList()
        messages.getJSONArray("WeekDays").toStringList().run {
            weekDays = this
            weekDaysInitials = this.map {
                when (language) {
                    LANG_AR -> it.substring(2, 4)
                    LANG_AZB -> it.substring(0, 2)
                    else -> it.substring(0, 1)
                }
            }
        }
    } catch (ignore: JSONException) {
        persianMonths = monthNameEmptyList
        islamicMonths = monthNameEmptyList
        gregorianMonths = monthNameEmptyList
        weekDays = weekDaysEmptyList
        weekDaysInitials = weekDaysEmptyList
    }
}

fun createAndShowSnackbar(v: View?, message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(v ?: return, message, duration).apply {
        view.setOnClickListener { dismiss() }
        view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).maxLines = 5
    }.show()
}

fun createAndShowSnackbar(view: View?, @StringRes messageId: Int) =
    createAndShowSnackbar(view, view?.context?.getString(messageId) ?: "")

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

fun getOrderedCalendarTypes(): List<CalendarType> = getEnabledCalendarTypes().let {
    it + (CalendarType.values().toList() - it)
}

@StringRes
fun getNextOwghatTimeId(current: Clock, dateHasChanged: Boolean): Int {
    coordinate ?: return 0

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
            // TODO: this is today's, not tomorrow
            else -> R.string.fajr
        }
    }
    return 0
}

fun getClockFromStringId(@StringRes stringId: Int) = prayTimes?.run {
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
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    val prefString = (prefs.getString(PREF_ATHAN_ALARM, "") ?: "").trim()
    Log.d(TAG, "reading and loading all alarms from prefs: $prefString")
    val calculationMethod = getCalculationMethod()

    if (coordinate != null && prefString.isNotEmpty()) {
        val athanGap =
            (((prefs.getString(PREF_ATHAN_GAP, "0") ?: "0").toDoubleOrNull() ?: .0)
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

// http://stackoverflow.com/a/5445161
private fun readStream(inputStream: InputStream): String =
    Scanner(inputStream).useDelimiter("\\A").let { if (it.hasNext()) it.next() else "" }

fun readRawResource(context: Context, @RawRes res: Int) =
    readStream(context.resources.openRawResource(res))

fun formatCoordinate(context: Context, coordinate: Coordinate, separator: String) = String.format(
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

    val geocodedCityName = prefs.getString(PREF_GEOCODED_CITYNAME, "") ?: ""
    if (geocodedCityName.isNotEmpty()) return geocodedCityName

    if (fallbackToCoord)
        coordinate?.let {
            return formatCoordinate(context, it, spacedComma)
        }

    return ""
}

private fun getCityFromPreference(context: Context): CityItem? {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    val key = prefs.getString(PREF_SELECTED_LOCATION, "") ?: ""
    if (key.isEmpty() || key == DEFAULT_CITY) return null

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
    return cachedCity
}

fun getCoordinate(context: Context): Coordinate? {
    val cityEntity = getCityFromPreference(context)
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    if (cityEntity != null)
        return cityEntity.coordinate

    val coord = Coordinate(
        (prefs.getString(PREF_LATITUDE, "0") ?: "0").toDoubleOrNull() ?: .0,
        (prefs.getString(PREF_LONGITUDE, "0") ?: "0").toDoubleOrNull() ?: .0,
        (prefs.getString(PREF_ALTITUDE, "0") ?: "0").toDoubleOrNull() ?: .0
    )

    // If latitude or longitude is zero probably preference is not set yet
    return if (coord.latitude == 0.0 && coord.longitude == 0.0) null
    else coord
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

fun a11yAnnounceAndClick(view: View, @StringRes resId: Int) {
    if (!isTalkBackEnabled) return

    val context = view.context ?: return

    val now = System.currentTimeMillis()
    if (now - latestToastShowTime > twoSeconds) {
        createAndShowSnackbar(view, resId)
        // https://stackoverflow.com/a/29423018
        context.getSystemService<AudioManager>()?.playSoundEffect(AudioManager.FX_KEY_CLICK)
        latestToastShowTime = now
    }
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
    PreferenceManager.getDefaultSharedPreferences(context).edit {
        putBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, enable)
    }

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

fun isShiaPrayTimeCalculationSelected(): Boolean =
    getCalculationMethod().run { this == CalculationMethod.Tehran || this == CalculationMethod.Jafari }

fun copyToClipboard(view: View?, label: CharSequence?, text: CharSequence?) {
    view ?: return
    val clipboardService = view.context.getSystemService<ClipboardManager>()

    if (clipboardService == null || label == null || text == null) return

    clipboardService.setPrimaryClip(ClipData.newPlainText(label, text))
    createAndShowSnackbar(
        view,
        String.format(view.context.getString(R.string.date_copied_clipboard), text)
    )
}

fun dateStringOfOtherCalendars(jdn: Long, separator: String): String {
    val result = StringBuilder()
    var first = true
    for (type in otherCalendars) {
        if (!first) result.append(separator)
        result.append(formatDate(getDateFromJdnOfCalendar(type, jdn)))
        first = false
    }
    return result.toString()
}

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

fun updateStoredPreference(context: Context) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    language = prefs.getString(PREF_APP_LANGUAGE, DEFAULT_APP_LANGUAGE) ?: DEFAULT_APP_LANGUAGE

    preferredDigits = if (prefs.getBoolean(PREF_PERSIAN_DIGITS, DEFAULT_PERSIAN_DIGITS))
        PERSIAN_DIGITS
    else
        ARABIC_DIGITS
    if ((language == LANG_AR || language == LANG_CKB) && preferredDigits.contentEquals(
            PERSIAN_DIGITS
        )
    )
        preferredDigits = ARABIC_INDIC_DIGITS
    if (language == LANG_JA && preferredDigits.contentEquals(PERSIAN_DIGITS))
        preferredDigits = CJK_DIGITS

    clockIn24 = prefs.getBoolean(PREF_WIDGET_IN_24, DEFAULT_WIDGET_IN_24)
    iranTime = prefs.getBoolean(PREF_IRAN_TIME, DEFAULT_IRAN_TIME)
    isNotifyDateOnLockScreen = prefs.getBoolean(
        PREF_NOTIFY_DATE_LOCK_SCREEN,
        DEFAULT_NOTIFY_DATE_LOCK_SCREEN
    )
    isWidgetClock = prefs.getBoolean(PREF_WIDGET_CLOCK, DEFAULT_WIDGET_CLOCK)
    isNotifyDate = prefs.getBoolean(PREF_NOTIFY_DATE, DEFAULT_NOTIFY_DATE)
    notificationAthan = prefs.getBoolean(PREF_NOTIFICATION_ATHAN, DEFAULT_NOTIFICATION_ATHAN)
    isCenterAlignWidgets = prefs.getBoolean("CenterAlignWidgets", false)

    selectedWidgetTextColor =
        (prefs.getString(PREF_SELECTED_WIDGET_TEXT_COLOR, DEFAULT_SELECTED_WIDGET_TEXT_COLOR)
            ?: DEFAULT_SELECTED_WIDGET_TEXT_COLOR)

    selectedWidgetBackgroundColor =
        (prefs.getString(
            PREF_SELECTED_WIDGET_BACKGROUND_COLOR,
            DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
        )) ?: DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR

    // We were using "Jafari" method but later found out Tehran is nearer to time.ir and others
    // so switched to "Tehran" method as default calculation algorithm
    calculationMethod =
        prefs.getString(PREF_PRAY_TIME_METHOD, DEFAULT_PRAY_TIME_METHOD) ?: DEFAULT_PRAY_TIME_METHOD

    coordinate = getCoordinate(context)
    try {
        mainCalendar = CalendarType.valueOf(
            prefs.getString(PREF_MAIN_CALENDAR_KEY, "SHAMSI") ?: "SHAMSI"
        )

        otherCalendars =
            (prefs.getString(PREF_OTHER_CALENDARS_KEY, "GREGORIAN,ISLAMIC") ?: "GREGORIAN,ISLAMIC")
                .splitIgnoreEmpty(",").map(CalendarType::valueOf).toList()
    } catch (e: Exception) {
        Log.e(TAG, "Fail on parsing calendar preference", e)
        mainCalendar = CalendarType.SHAMSI
        otherCalendars = listOf(CalendarType.GREGORIAN, CalendarType.ISLAMIC)
    }

    spacedComma = if (isNonArabicScriptSelected()) ", " else "، "
    showWeekOfYear = prefs.getBoolean("showWeekOfYearNumber", false)
    weekStartOffset =
        (prefs.getString(PREF_WEEK_START, DEFAULT_WEEK_START) ?: DEFAULT_WEEK_START).toIntOrNull()
            ?: 0

    weekEnds = BooleanArray(7)
    (prefs.getStringSet(PREF_WEEK_ENDS, DEFAULT_WEEK_ENDS) ?: DEFAULT_WEEK_ENDS)
        .mapNotNull(String::toIntOrNull)
        .forEach { weekEnds[it] = true }

    showDeviceCalendarEvents = prefs.getBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, false)
    val resources = context.resources
    whatToShowOnWidgets = prefs.getStringSet("what_to_show", null)
        ?: resources.getStringArray(R.array.what_to_show_default).toSet()

    isAstronomicalFeaturesEnabled = prefs.getBoolean("astronomicalFeatures", false)
    numericalDatePreferred = prefs.getBoolean("numericalDatePreferred", false)

    if (getOnlyLanguage(language) != resources.getString(R.string.code))
        applyAppLanguage(context)

    calendarTypesTitleAbbr = context.resources.getStringArray(R.array.calendar_type_abbr).toList()

    try {
        sShiftWorks = (prefs.getString(PREF_SHIFT_WORK_SETTING, null) ?: "")
            .split(",")
            .map { it.split("=") }
            .filter { it.size == 2 }
            .map { ShiftWorkRecord(it[0], Integer.valueOf(it[1])) }

        sShiftWorkStartingJdn = prefs.getLong(PREF_SHIFT_WORK_STARTING_JDN, -1)

        sShiftWorkPeriod = 0
        for (shift in sShiftWorks) sShiftWorkPeriod += shift.length

        sShiftWorkRecurs = prefs.getBoolean(PREF_SHIFT_WORK_RECURS, true)

        val titles = resources.getStringArray(R.array.shift_work)
        val keys = resources.getStringArray(R.array.shift_work_keys)
        sShiftWorkTitles = keys.zip(titles).toMap()
    } catch (e: Exception) {
        e.printStackTrace()
        sShiftWorks = ArrayList()
        sShiftWorkStartingJdn = -1

        sShiftWorkPeriod = 0
        sShiftWorkRecurs = true
        sShiftWorkTitles = emptyMap()
    }

    when (language) {
        LANG_FA, LANG_FA_AF, LANG_EN_IR -> {
            sAM = DEFAULT_AM
            sPM = DEFAULT_PM
        }
        else -> {
            sAM = context.getString(R.string.am)
            sPM = context.getString(R.string.pm)
        }
    }

    appTheme = try {
        getThemeFromName(getThemeFromPreference(context, prefs))
    } catch (e: Exception) {
        e.printStackTrace()
        R.style.LightTheme
    }

    context.getSystemService<AccessibilityManager>()?.run {
        isTalkBackEnabled = isEnabled && isTouchExplorationEnabled
    }
}

private fun getOnlyLanguage(string: String): String = string.replace("-(IR|AF|US)".toRegex(), "")

// Context preferably should be activity context not application
fun applyAppLanguage(context: Context) {
    val localeCode = getOnlyLanguage(language)
    // To resolve this issue, https://issuetracker.google.com/issues/128908783 (marked as fixed now)
    // if ((language.equals(LANG_GLK) || language.equals(LANG_AZB)) && Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
    //    localeCode = LANG_FA;
    // }
    var locale = Locale(localeCode)
    Locale.setDefault(locale)
    val resources = context.resources
    val config = resources.configuration
    config.locale = locale
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        if (language == LANG_AZB) {
            locale = Locale(LANG_FA)
        }
        config.setLayoutDirection(locale)
    }
    resources.updateConfiguration(config, resources.displayMetrics)
}

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

        var isRunning = false
        context.getSystemService<ActivityManager>()?.let {
            try {
                for (service in it.getRunningServices(Integer.MAX_VALUE)) {
                    if (ApplicationService::class.java.name == service.service.className)
                        isRunning = true
                }
            } catch (e: Exception) {
                Log.e(TAG, "startEitherServiceOrWorker service's first part fail", e)
            }
        }

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
    if (sShiftWorkStartingJdn == -1L || jdn < sShiftWorkStartingJdn || sShiftWorkPeriod == 0)
        return ""

    val passedDays = jdn - sShiftWorkStartingJdn
    if (!sShiftWorkRecurs && passedDays >= sShiftWorkPeriod)
        return ""

    val dayInPeriod = (passedDays % sShiftWorkPeriod).toInt()
    var accumulation = 0
    for (shift in sShiftWorks) {
        accumulation += shift.length
        if (accumulation > dayInPeriod) {
            // Skip rests on abbreviated mode
            if (sShiftWorkRecurs && abbreviated &&
                (shift.type == "r" || shift.type == sShiftWorkTitles["r"])
            )
                return ""

            var title = sShiftWorkTitles[shift.type]
            if (title == null) title = shift.type
            return if (abbreviated) {
                title.substring(0, 1) +
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && language != LANG_AR)
                            ZWJ
                        else
                            ""
            } else title
        }
    }
    // Shouldn't be reached
    return ""
}

fun getAllCities(context: Context, needsSort: Boolean): List<CityItem> {
    val result = mutableListOf<CityItem>()
    try {
        fun JSONObject.forEach(f: (String, JSONObject) -> Unit) =
            this.keys().forEach { f(it, this.getJSONObject(it)) }

        JSONObject(readRawResource(context, R.raw.cities)).forEach { countryCode, country ->
            val countryEn = country.getString("en")
            val countryFa = country.getString("fa")
            val countryCkb = country.getString("ckb")
            val countryAr = country.getString("ar")

            country.getJSONObject("cities").forEach { key, city ->
                result.add(
                    CityItem(
                        key,
                        city.getString("en"), city.getString("fa"),
                        city.getString("ckb"), city.getString("ar"),
                        countryCode,
                        countryEn, countryFa, countryCkb, countryAr,
                        Coordinate(
                            city.getDouble("latitude"),
                            city.getDouble("longitude"),
                            // Don't Consider elevation for Iran
                            if (countryCode == "ir") 0.0 else city.getDouble("elevation")
                        )
                    )
                )
            }
        }
    } catch (ignore: JSONException) {
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
