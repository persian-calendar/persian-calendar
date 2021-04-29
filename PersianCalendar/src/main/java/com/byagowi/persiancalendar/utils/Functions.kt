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
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import androidx.work.*
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ReleaseDebugDifference.debugAssertNotNull
import com.byagowi.persiancalendar.ReleaseDebugDifference.logDebug
import com.byagowi.persiancalendar.entities.CalendarTypeItem
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
import io.github.persiancalendar.calendar.islamic.IranianIslamicDateConverter
import io.github.persiancalendar.praytimes.Clock
import io.github.persiancalendar.praytimes.Coordinate
import io.github.persiancalendar.praytimes.PrayTimesCalculator
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.sqrt

// This should be called before any use of Utils on the activity and services
fun initUtils(context: Context) {
    updateStoredPreference(context)
    applyAppLanguage(context)
    loadLanguageResource()
    loadAlarms(context)
    loadEvents(context)
}

val supportedYearOfIranCalendar: Int
    get() = IranianIslamicDateConverter.latestSupportedYearOfIran

fun isArabicDigitSelected(): Boolean = when (preferredDigits) {
    ARABIC_DIGITS -> true
    else -> false
}

fun goForWorker(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

fun toLinearDate(date: AbstractDate): String = "%s/%s/%s".format(
    formatNumber(date.year), formatNumber(date.month), formatNumber(date.dayOfMonth)
)

fun isNightModeEnabled(context: Context): Boolean =
    context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

fun formatDate(
    date: AbstractDate,
    calendarNameInLinear: Boolean = true,
    forceNonNumerical: Boolean = false
): String = if (numericalDatePreferred && !forceNonNumerical)
    (toLinearDate(date) + if (calendarNameInLinear) (" " + getCalendarNameAbbr(date)) else "").trim()
else when (language) {
    LANG_CKB -> "%sی %sی %s"
    else -> "%s %s %s"
}.format(formatNumber(date.dayOfMonth), getMonthName(date), formatNumber(date.year))

fun isNonArabicScriptSelected() = when (language) {
    LANG_EN_US, LANG_JA -> true
    else -> false
}

// en-US and ja are our only real LTR locales for now
fun isLocaleRTL(): Boolean = when (language) {
    LANG_EN_US, LANG_JA -> false
    else -> true
}

fun formatNumber(number: Double): String = when (preferredDigits) {
    ARABIC_DIGITS -> number.toString()
    else -> formatNumber(number.toString())
        .replace(".", "٫" /* U+066B, Arabic Decimal Separator */)
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

// It should match with calendar_type_abbr array
fun getCalendarNameAbbr(date: AbstractDate) = calendarTypesTitleAbbr.getOrNull(
    when (date) {
        is PersianDate -> 0
        is IslamicDate -> 1
        is CivilDate -> 2
        else -> -1
    }
) ?: ""

fun getThemeFromPreference(context: Context, prefs: SharedPreferences): String =
    prefs.getString(PREF_THEME, null)?.takeIf { it != SYSTEM_DEFAULT_THEME }
        ?: if (isNightModeEnabled(context)) DARK_THEME else LIGHT_THEME

fun getEnabledCalendarTypes() = listOf(mainCalendar) + otherCalendars

fun loadApp(context: Context): Unit = if (!goForWorker()) runCatching {
    val alarmManager = context.getSystemService<AlarmManager>() ?: return@runCatching

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
}.getOrElse(logException) else Unit

fun getOrderedCalendarTypes(): List<CalendarType> = getEnabledCalendarTypes().let {
    it + (CalendarType.values().toList() - it)
}

fun loadAlarms(context: Context) {
    val prefString = context.appPrefs.getString(PREF_ATHAN_ALARM, null)?.trim() ?: ""
    logDebug(TAG, "reading and loading all alarms from prefs: $prefString")

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
        logDebug(TAG, "setting alarm for: " + triggerTime.time)

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

fun getOrderedCalendarEntities(
    context: Context, abbreviation: Boolean = false
): List<CalendarTypeItem> {
    applyAppLanguage(context)
    val typeTitleMap =
        context.resources.getStringArray(R.array.calendar_values)
            .map(CalendarType::valueOf)
            .zip(context.resources.getStringArray(if (abbreviation) R.array.calendar_type_abbr else R.array.calendar_type))
            .toMap()
    return getOrderedCalendarTypes().mapNotNull {
        typeTitleMap[it]?.run { CalendarTypeItem(it, this) }
    }
}

fun getDayIconResource(day: Int): Int = runCatching {
    when (preferredDigits) {
        ARABIC_DIGITS -> DAYS_ICONS_ARABIC[day]
        ARABIC_INDIC_DIGITS -> DAYS_ICONS_ARABIC_INDIC[day]
        else -> DAYS_ICONS_PERSIAN[day]
    }
}.onFailure(logException).getOrDefault(0)

fun formatCoordinate(context: Context, coordinate: Coordinate, separator: String) =
    "%s: %.7f%s%s: %.7f".format(
        Locale.getDefault(),
        context.getString(R.string.latitude), coordinate.latitude, separator,
        context.getString(R.string.longitude), coordinate.longitude
    )

// https://stackoverflow.com/a/62499553
// https://en.wikipedia.org/wiki/ISO_6709#Representation_at_the_human_interface_(Annex_D)
fun formatCoordinateISO6709(lat: Double, long: Double, alt: Double? = null) = listOf(
    abs(lat) to if (lat >= 0) "N" else "S", abs(long) to if (long >= 0) "E" else "W"
).joinToString(" ") {
    val degrees = it.first.toInt()
    val minutes = ((it.first - degrees) * 60).toInt()
    val seconds = ((it.first - degrees) * 3600 % 60).toInt()
    "%d°%02d′%02d″%s".format(Locale.US, degrees, minutes, seconds, it.second)
} + (alt?.let { " %s%.1fm".format(Locale.US, if (alt < 0) "−" else "", abs(alt)) } ?: "")

fun getCityName(context: Context, fallbackToCoord: Boolean): String =
    getCityFromPreference(context)?.let {
        when (language) {
            LANG_EN_IR, LANG_EN_US, LANG_JA -> it.en
            LANG_CKB -> it.ckb
            else -> it.fa
        }
    } ?: context.appPrefs.getString(PREF_GEOCODED_CITYNAME, null)?.takeIf { it.isNotEmpty() }
    ?: coordinate?.takeIf { fallbackToCoord }?.let { formatCoordinate(context, it, spacedComma) }
    ?: ""

fun getCoordinate(context: Context): Coordinate? =
    getCityFromPreference(context)?.coordinate ?: context.appPrefs.run {
        Coordinate(
            getString(PREF_LATITUDE, null)?.toDoubleOrNull() ?: .0,
            getString(PREF_LONGITUDE, null)?.toDoubleOrNull() ?: .0,
            getString(PREF_ALTITUDE, null)?.toDoubleOrNull() ?: .0
        ).takeIf { it.latitude != 0.0 || it.longitude != 0.0 }
        // If latitude and longitude both are zero probably preference is not set yet
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
    context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

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
        .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
        .show()
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
        .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
        .show()
}

fun copyToClipboard(
    view: View?, label: CharSequence?, text: CharSequence?, showToastInstead: Boolean = false
) {
    view ?: return
    val clipboardService = view.context.getSystemService<ClipboardManager>()

    if (clipboardService == null || label == null || text == null) return

    clipboardService.setPrimaryClip(ClipData.newPlainText(label, text))
    val textToShow = view.context.getString(R.string.date_copied_clipboard).format(text)
    if (showToastInstead)
        Toast.makeText(view.context, textToShow, Toast.LENGTH_SHORT).show()
    else
        Snackbar.make(view, textToShow, Snackbar.LENGTH_SHORT).show()
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
    if (goForWorker()) {
        runCatching {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UPDATE_TAG, ExistingPeriodicWorkPolicy.REPLACE,
                // An hourly task to call UpdateWorker.doWork
                PeriodicWorkRequest.Builder(UpdateWorker::class.java, 1L, TimeUnit.HOURS).build()
            )
        }.onFailure(logException).getOrNull().debugAssertNotNull
    } else {
        val isRunning = context.getSystemService<ActivityManager>()?.let { am ->
            runCatching {
                am.getRunningServices(Integer.MAX_VALUE).any {
                    ApplicationService::class.java.name == it.service.className
                }
            }.onFailure(logException).getOrNull()
        } ?: false

        if (!isRunning) runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                ContextCompat.startForegroundService(
                    context,
                    Intent(context, ApplicationService::class.java)
                )
            context.startService(Intent(context, ApplicationService::class.java))
        }.onFailure(logException)
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
    return if (abbreviated && title.isNotEmpty()) title.substring(0, 1) +
            (if (language != LANG_AR) ZWJ else "")
    else title
}

val Context.appPrefs: SharedPreferences
    get() = PreferenceManager.getDefaultSharedPreferences(this)

val Context.layoutInflater: LayoutInflater
    get() = LayoutInflater.from(this)

fun bringMarketPage(activity: Activity): Unit = runCatching {
    activity.startActivity(
        Intent(Intent.ACTION_VIEW, "market://details?id=${activity.packageName}".toUri())
    )
}.onFailure(logException).getOrElse {
    runCatching {
        activity.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                "https://play.google.com/store/apps/details?id=${activity.packageName}".toUri()
            )
        )
    }.onFailure(logException)
}

val Number.dp: Int
    get() = (toFloat() * Resources.getSystem().displayMetrics.density).toInt()

val logException = fun(e: Throwable) { Log.e("Persian Calendar", e.message, e) }

fun Toolbar.setupUpNavigation() {
    navigationIcon = DrawerArrowDrawable(context).apply { progress = 1f }
    setNavigationContentDescription(androidx.navigation.ui.R.string.nav_app_bar_navigate_up_description)
    setNavigationOnClickListener { findNavController().navigateUp() }
}

@ColorInt
fun Context.resolveColor(attr: Int) = TypedValue().let {
    theme.resolveAttribute(attr, it, true)
    ContextCompat.getColor(this, it.resourceId)
}
