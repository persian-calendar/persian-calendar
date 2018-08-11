package com.byagowi.persiancalendar.util

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import android.text.Html
import android.text.TextUtils
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import calendar.DateConverter
import com.byagowi.persiancalendar.Constants
import com.byagowi.persiancalendar.Constants.AM_IN_CKB
import com.byagowi.persiancalendar.Constants.AM_IN_PERSIAN
import com.byagowi.persiancalendar.Constants.DARK_THEME
import com.byagowi.persiancalendar.Constants.LIGHT_THEME
import com.byagowi.persiancalendar.Constants.PM_IN_CKB
import com.byagowi.persiancalendar.Constants.PM_IN_PERSIAN
import com.byagowi.persiancalendar.Constants.PREF_THEME
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.CalendarsCardBinding
import com.byagowi.persiancalendar.databinding.SelectdayFragmentBinding
import com.byagowi.persiancalendar.entity.DeviceCalendarEvent
import com.github.praytimes.Clock
import java.util.*

object UIUtils {
  fun setActivityTitleAndSubtitle(activity: Activity, title: String, subtitle: String) {

    val supportActionBar = (activity as AppCompatActivity).supportActionBar
    if (supportActionBar != null) {
      supportActionBar.title = title
      supportActionBar.subtitle = subtitle
    }
  }

  fun fillCalendarsCard(context: Context, jdn: Long,
                        binding: CalendarsCardBinding, isToday: Boolean) {
    val persianDate = DateConverter.jdnToPersian(jdn)
    val civilDate = DateConverter.jdnToCivil(jdn)
    val hijriDate = DateConverter.civilToIslamic(civilDate, Utils.getIslamicOffset())

    binding.weekDayName.text = Utils.getWeekDayName(civilDate)
    binding.shamsiDateLinear.text = CalendarUtils.toLinearDate(persianDate)
    binding.shamsiDateDay.text = Utils.formatNumber(persianDate.dayOfMonth)
    binding.shamsiDate.text = CalendarUtils.getMonthName(persianDate) + "\n" + Utils.formatNumber(persianDate.year)

    binding.gregorianDateLinear.text = CalendarUtils.toLinearDate(civilDate)
    binding.gregorianDateDay.text = Utils.formatNumber(civilDate.dayOfMonth)
    binding.gregorianDate.text = CalendarUtils.getMonthName(civilDate) + "\n" + Utils.formatNumber(civilDate.year)

    binding.islamicDateLinear.text = CalendarUtils.toLinearDate(hijriDate)
    binding.islamicDateDay.text = Utils.formatNumber(hijriDate.dayOfMonth)
    binding.islamicDate.text = CalendarUtils.getMonthName(hijriDate) + "\n" + Utils.formatNumber(hijriDate.year)

    if (isToday) {
      binding.today.visibility = View.GONE
      binding.todayIcon.visibility = View.GONE
      if (Utils.isIranTime) {
        binding.weekDayName.text = binding.weekDayName.text.toString() + " (" + context.getString(R.string.iran_time) + ")"
      }
    } else {
      binding.today.visibility = View.VISIBLE
      binding.todayIcon.visibility = View.VISIBLE
    }
  }

  fun fillSelectdaySpinners(context: Context, binding: SelectdayFragmentBinding): Int {
    val date = CalendarUtils.getTodayOfCalendar(CalendarUtils.calendarTypeFromPosition(
        binding.calendarTypeSpinner.selectedItemPosition))

    // years spinner init.
    val years = arrayOfNulls<String>(200)
    val startingYearOnYearSpinner = date.year - years.size / 2
    for (i in years.indices) {
      years[i] = Utils.formatNumber(i + startingYearOnYearSpinner)
    }
    binding.yearSpinner.adapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, years)
    binding.yearSpinner.setSelection(years.size / 2)
    //

    // month spinner init.
    val months = Utils.monthsNamesOfCalendar(date).clone()
    for (i in months.indices) {
      months[i] = months[i] + " / " + Utils.formatNumber(i + 1)
    }
    binding.monthSpinner.adapter = ArrayAdapter(context,
        android.R.layout.simple_spinner_dropdown_item, months)
    binding.monthSpinner.setSelection(date.month - 1)
    //

    // days spinner init.
    val days = arrayOfNulls<String>(31)
    for (i in days.indices) {
      days[i] = Utils.formatNumber(i + 1)
    }
    binding.daySpinner.adapter = ArrayAdapter<String>(context,
        android.R.layout.simple_spinner_dropdown_item, days)
    binding.daySpinner.setSelection(date.dayOfMonth - 1)
    //

    return startingYearOnYearSpinner
  }

  fun askForCalendarPermission(activity: AppCompatActivity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      activity.requestPermissions(arrayOf(Manifest.permission.READ_CALENDAR),
          Constants.CALENDAR_READ_PERMISSION_REQUEST_CODE)
    }
  }

  fun formatDeviceCalendarEventTitle(event: DeviceCalendarEvent): String {
    val desc = event.description
    var title = event.title
    if (!TextUtils.isEmpty(desc))
      title += " (" + Html.fromHtml(event.description).toString().trim { it <= ' ' } + ")"

    return title.replace("\\n".toRegex(), " ").trim { it <= ' ' }
  }

  internal fun clockToString(hour: Int, minute: Int): String = Utils.formatNumber(String.format(Locale.ENGLISH, "%d:%02d", hour, minute))

  fun isRTL(context: Context): Boolean =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
      } else false

  fun getFormattedClock(clock: Clock): String {
    var timeText: String? = null

    var hour = clock.hour
    if (!Utils.isClockIn24) {
      if (hour >= 12) {
        timeText = if (Utils.appLanguage == "ckb")
          PM_IN_CKB
        else
          PM_IN_PERSIAN
        hour -= 12
      } else {
        timeText = if (Utils.appLanguage == "ckb")
          AM_IN_CKB
        else
          AM_IN_PERSIAN
      }
    }

    var result = clockToString(hour, clock.minute)
    if (!Utils.isClockIn24) {
      result = "$result $timeText"
    }
    return result
  }

  @StringRes
  fun getPrayTimeText(athanKey: String): Int = when (athanKey) {
    "FAJR" -> R.string.azan1

    "DHUHR" -> R.string.azan2

    "ASR" -> R.string.azan3

    "MAGHRIB" -> R.string.azan4

    "ISHA" -> R.string.azan5
    else -> R.string.azan5
  }

  @DrawableRes
  fun getPrayTimeImage(athanKey: String): Int = when (athanKey) {
    "FAJR" -> R.drawable.fajr

    "DHUHR" -> R.drawable.dhuhr

    "ASR" -> R.drawable.asr

    "MAGHRIB" -> R.drawable.maghrib

    "ISHA" -> R.drawable.isha

    else -> R.drawable.isha
  }

  fun copyToClipboard(context: Context, text: CharSequence) {
    val clipboardService =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?

    if (clipboardService != null) {
      clipboardService.primaryClip = ClipData.newPlainText("converted date", text)
      Toast.makeText(context, "«" + text + "»\n" + context.getString(R.string.date_copied_clipboard), Toast.LENGTH_SHORT).show()
    }
  }

  fun setTheme(activity: AppCompatActivity) {
    when (PreferenceManager.getDefaultSharedPreferences(activity).getString(PREF_THEME, LIGHT_THEME)) {
      DARK_THEME -> {
        activity.setTheme(R.style.DarkTheme)
        return
      }
      LIGHT_THEME -> activity.setTheme(R.style.LightTheme)
      //            case CLASSIC_THEME:
      //                setTheme(R.style.ClassicTheme);
      //                return;
      else -> activity.setTheme(R.style.LightTheme)
    }
  }

  // https://stackoverflow.com/a/27788209
  private fun resourceToUri(context: Context, resID: Int): Uri =
      Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
          context.resources.getResourcePackageName(resID) + '/'.toString() +
          context.resources.getResourceTypeName(resID) + '/'.toString() +
          context.resources.getResourceEntryName(resID))

  fun getDefaultAthanUri(context: Context): Uri = resourceToUri(context, R.raw.abdulbasit)

  internal fun getOnlyLanguage(string: String): String = string.replace("-(IR|AF|US)".toRegex(), "")
}
