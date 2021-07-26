package com.byagowi.persiancalendar.utils

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.*
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.ShapeDrawable
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.appcompat.widget.Toolbar
import androidx.browser.customtabs.CustomTabsIntent
import androidx.constraintlayout.helper.widget.Flow
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import androidx.work.*
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ReleaseDebugDifference.debugAssertNotNull
import com.byagowi.persiancalendar.ReleaseDebugDifference.logDebug
import com.byagowi.persiancalendar.entities.CalendarTypeItem
import com.byagowi.persiancalendar.entities.CityItem
import com.byagowi.persiancalendar.generated.citiesStore
import com.byagowi.persiancalendar.service.ApplicationService
import com.byagowi.persiancalendar.service.BroadcastReceivers
import com.byagowi.persiancalendar.service.UpdateWorker
import com.google.android.material.appbar.AppBarLayout
import io.github.persiancalendar.Equinox
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.calendar.islamic.IranianIslamicDateConverter
import io.github.persiancalendar.praytimes.Clock
import io.github.persiancalendar.praytimes.Coordinate
import io.github.persiancalendar.praytimes.PrayTimesCalculator
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

// This should be called before any use of Utils on the activity and services
fun initUtils(context: Context) {
    updateStoredPreference(context)
    applyAppLanguage(context)
    loadLanguageResource()
    loadAlarms(context)
    loadEvents(context)
}

val supportedYearOfIranCalendar: Int get() = IranianIslamicDateConverter.latestSupportedYearOfIran

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
    date: AbstractDate, calendarNameInLinear: Boolean = true, forceNonNumerical: Boolean = false
): String = if (numericalDatePreferred && !forceNonNumerical)
    (toLinearDate(date) + if (calendarNameInLinear) (" " + getCalendarNameAbbr(date)) else "").trim()
else when (language) {
    LANG_CKB -> "%sی %sی %s"
    else -> "%s %s %s"
}.format(formatNumber(date.dayOfMonth), date.monthName, formatNumber(date.year))

fun isNonArabicScriptSelected() = when (language) {
    LANG_EN_US, LANG_JA, LANG_FR, LANG_ES -> true
    else -> false
}

fun formatNumber(number: Double): String {
    if (isArabicDigitSelected()) return number.toString()
    return formatNumber(number.toString()).replace(".", "٫") // U+066B, Arabic Decimal Separator
}

fun formatNumber(number: Int): String = formatNumber(number.toString())

fun formatNumber(number: String): String {
    if (isArabicDigitSelected()) return number
    return number.map { preferredDigits.getOrNull(Character.getNumericValue(it)) ?: it }
        .joinToString("")
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

fun loadApp(context: Context) = runCatching {
    if (goForWorker()) return@runCatching
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
        PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
    )
    alarmManager.set(AlarmManager.RTC, startTime.timeInMillis, dailyPendingIntent)

    // There are simpler triggers on older Androids like SCREEN_ON but they
    // are not available anymore, lets register an hourly alarm for >= Oreo
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val threeHoursPendingIntent = PendingIntent.getBroadcast(
            context,
            THREE_HOURS_APP_ID,
            Intent(context, BroadcastReceivers::class.java).setAction(BROADCAST_UPDATE_APP),
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        alarmManager.setInexactRepeating(
            AlarmManager.RTC,
            // Start from one hour from now
            Calendar.getInstance().timeInMillis + TimeUnit.HOURS.toMillis(1),
            TimeUnit.HOURS.toMillis(3), threeHoursPendingIntent
        )
    }
}.onFailure(logException).let {}

fun getOrderedCalendarTypes(): List<CalendarType> =
    getEnabledCalendarTypes().let { it + (CalendarType.values().toList() - it) }

fun loadAlarms(context: Context) {
    val prefString = context.appPrefs.getString(PREF_ATHAN_ALARM, null)?.trim() ?: ""
    logDebug(TAG, "reading and loading all alarms from prefs: $prefString")

    if (coordinates != null && prefString.isNotEmpty()) {
        val athanGap =
            ((context.appPrefs.getString(PREF_ATHAN_GAP, null)?.toDoubleOrNull() ?: .0)
                    * 60.0 * 1000.0).toLong()

        val prayTimes = PrayTimesCalculator.calculate(calculationMethod, Date(), coordinates)
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
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
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
    return getOrderedCalendarTypes().mapNotNull { calendarType ->
        typeTitleMap[calendarType]?.let { CalendarTypeItem(calendarType, it) }
    }
}

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
).joinToString(" ") { (degree: Double, direction: String) ->
    val minutes = ((degree - degree.toInt()) * 60).toInt()
    val seconds = ((degree - degree.toInt()) * 3600 % 60).toInt()
    "%d°%02d′%02d″%s".format(Locale.US, degree.toInt(), minutes, seconds, direction)
} + (alt?.let { " %s%.1fm".format(Locale.US, if (alt < 0) "−" else "", abs(alt)) } ?: "")

fun SharedPreferences.getStoredCity(): CityItem? {
    return getString(PREF_SELECTED_LOCATION, null)
        ?.takeIf { it.isNotEmpty() && it != DEFAULT_CITY }?.let { citiesStore[it] }
}

val CityItem.localizedCountryName: String
    get() = when (language) {
        LANG_EN_IR, LANG_EN_US, LANG_JA, LANG_FR, LANG_ES -> this.countryEn
        LANG_AR -> this.countryAr
        LANG_CKB -> this.countryCkb
        else -> this.countryFa
    }

val CityItem.localizedCityName: String
    get() = when (language) {
        LANG_EN_IR, LANG_EN_US, LANG_JA, LANG_FR, LANG_ES -> this.en
        LANG_AR -> this.ar
        LANG_CKB -> this.ckb
        else -> this.fa
    }

fun getCityName(context: Context, fallbackToCoord: Boolean): String {
    val prefs = context.appPrefs
    return prefs.getStoredCity()?.localizedCityName
        ?: prefs.getString(PREF_GEOCODED_CITYNAME, null)?.takeIf { it.isNotEmpty() }
        ?: coordinates?.takeIf { fallbackToCoord }
            ?.let { formatCoordinate(context, it, spacedComma) }
        ?: ""
}

fun CivilDate.getSpringEquinox() = Equinox.northwardEquinox(this.year).toJavaCalendar()

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

@StyleRes
fun getThemeFromName(name: String): Int = when (name) {
    DARK_THEME -> R.style.DarkTheme
    MODERN_THEME -> R.style.ModernTheme
    BLUE_THEME -> R.style.BlueTheme
    LIGHT_THEME -> R.style.LightTheme
    else -> R.style.LightTheme
}

val Resources.isRtl get() = configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

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
                arrayOf(Manifest.permission.READ_CALENDAR), CALENDAR_READ_PERMISSION_REQUEST_CODE
            )
        }
        .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
        .show()
}

fun Context?.copyToClipboard(
    text: CharSequence?,
    onSuccess: ((String) -> Unit) = { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
) = runCatching {
    this?.getSystemService<ClipboardManager>()
        ?.setPrimaryClip(ClipData.newPlainText(null, text)) ?: return@runCatching null
    val message = (if (resources.isRtl) RLM else "") +
            getString(R.string.date_copied_clipboard).format(text)
    onSuccess(message)
}.onFailure(logException).getOrNull().debugAssertNotNull.let {}

fun dateStringOfOtherCalendars(jdn: Jdn, separator: String) =
    otherCalendars.joinToString(separator) { formatDate(jdn.toCalendar(it)) }

private fun calculateDiffToChangeDate(): Long = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 1)
}.timeInMillis / 1000 + DAY_IN_SECOND - Calendar.getInstance().time.time / 1000

fun setChangeDateWorker(context: Context) {
    val remainedSeconds = calculateDiffToChangeDate()
    val changeDateWorker = OneTimeWorkRequest.Builder(UpdateWorker::class.java)
        // Use this when you want to add initial delay or schedule initial work
        // to `OneTimeWorkRequest` e.g. setInitialDelay(2, TimeUnit.HOURS)
        .setInitialDelay(remainedSeconds, TimeUnit.SECONDS)
        .build()

    WorkManager.getInstance(context).beginUniqueWork(
        CHANGE_DATE_TAG, ExistingWorkPolicy.REPLACE, changeDateWorker
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
                    context, Intent(context, ApplicationService::class.java)
                )
            context.startService(Intent(context, ApplicationService::class.java))
        }.onFailure(logException)
    }
}

fun getShiftWorkTitle(jdn: Jdn, abbreviated: Boolean): String {
    val shiftWorkStartingJdn = shiftWorkStartingJdn ?: return ""
    if (jdn < shiftWorkStartingJdn || shiftWorkPeriod == 0)
        return ""

    val passedDays = jdn - shiftWorkStartingJdn
    if (!shiftWorkRecurs && passedDays >= shiftWorkPeriod) return ""

    val dayInPeriod = passedDays % shiftWorkPeriod

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

val Context.appPrefs: SharedPreferences get() = PreferenceManager.getDefaultSharedPreferences(this)

fun SharedPreferences.Editor.putJdn(key: String, jdn: Jdn?) {
    if (jdn == null) remove(jdn) else putLong(key, jdn.value)
}

fun SharedPreferences.getJdnOrNull(key: String): Jdn? =
    getLong(key, -1).takeIf { it != -1L }?.let(::Jdn)

val Context.layoutInflater: LayoutInflater get() = LayoutInflater.from(this)

fun bringMarketPage(activity: Activity): Unit = runCatching {
    activity.startActivity(
        Intent(Intent.ACTION_VIEW, "market://details?id=${activity.packageName}".toUri())
    )
}.onFailure(logException).onFailure {
    runCatching {
        val uri = "https://play.google.com/store/apps/details?id=${activity.packageName}".toUri()
        activity.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }.onFailure(logException)
}.let {}

val Number.dp: Float get() = this.toFloat() * Resources.getSystem().displayMetrics.density
val Number.sp: Float get() = this.toFloat() * Resources.getSystem().displayMetrics.scaledDensity

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

fun Context.showHtml(html: String) = runCatching {
    val uri = FileProvider.getUriForFile(
        applicationContext, "$packageName.provider",
        File(externalCacheDir, "temp.html").also { it.writeText(html) }
    )
    CustomTabsIntent.Builder().build()
        .also { it.intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        .launchUrl(this, uri)
}.onFailure(logException).let {}

fun Flow.addViewsToFlow(viewList: List<View>) {
    val parentView = (this.parent as? ViewGroup).debugAssertNotNull ?: return
    this.referencedIds = viewList.map {
        View.generateViewId().also { id ->
            it.id = id
            parentView.addView(it)
        }
    }.toIntArray()
}

fun NavController.navigateSafe(directions: NavDirections) = runCatching {
    navigate(directions)
}.onFailure(logException).getOrNull().debugAssertNotNull.let {}

fun Context.getCompatDrawable(@DrawableRes drawableRes: Int) =
    AppCompatResources.getDrawable(this, drawableRes).debugAssertNotNull ?: ShapeDrawable()

fun AppBarLayout.hideToolbarBottomShadow() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) outlineProvider = null
}

inline fun MenuItem.onClick(crossinline action: () -> Unit) =
    this.setOnMenuItemClickListener { action(); true /* it captures the click event */ }.let {}

fun View.setupExpandableAccessibilityDescription() {
    ViewCompat.setAccessibilityDelegate(this, object : AccessibilityDelegateCompat() {
        override fun onInitializeAccessibilityNodeInfo(
            host: View?,
            info: AccessibilityNodeInfoCompat?
        ) {
            super.onInitializeAccessibilityNodeInfo(host, info)
            info?.addAction(
                AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                    AccessibilityNodeInfoCompat.ACTION_CLICK, resources.getString(R.string.more)
                )
            )
        }
    })
}

fun <T> listOf31Items(
    x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T, x11: T, x12: T,
    x13: T, x14: T, x15: T, x16: T, x17: T, x18: T, x19: T, x20: T, x21: T, x22: T,
    x23: T, x24: T, x25: T, x26: T, x27: T, x28: T, x29: T, x30: T, x31: T
) = listOf(
    x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12,
    x13, x14, x15, x16, x17, x18, x19, x20, x21, x22,
    x23, x24, x25, x26, x27, x28, x29, x30, x31
)

fun <T> listOf12Items(
    x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T, x11: T, x12: T
) = listOf(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12)

fun <T> listOf7Items(
    x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T
) = listOf(x1, x2, x3, x4, x5, x6, x7)
