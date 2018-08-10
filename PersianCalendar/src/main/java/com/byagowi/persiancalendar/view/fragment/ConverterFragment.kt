package com.byagowi.persiancalendar.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast

import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentConverterBinding
import com.byagowi.persiancalendar.util.CalendarUtils
import com.byagowi.persiancalendar.util.UIUtils
import com.byagowi.persiancalendar.util.Utils

import androidx.annotation.Nullable
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import calendar.*

/**
 * Program activity for android
 *
 * @author ebraminio
 */
class ConverterFragment : Fragment(), AdapterView.OnItemSelectedListener, View.OnClickListener {

  private var startingYearOnYearSpinner = 0

  private lateinit var binding: FragmentConverterBinding

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {

    UIUtils.setActivityTitleAndSubtitle(activity, getString(R.string.date_converter), "")

    binding = DataBindingUtil.inflate(inflater, R.layout.fragment_converter, container,
        false)

    binding.calendarsCard.calendarsCardIcon.setImageResource(R.drawable.ic_swap_vertical_circle)

    binding.calendarsCard.today.visibility = View.GONE
    binding.calendarsCard.todayIcon.visibility = View.GONE
    binding.calendarsCard.today.setOnClickListener(this)
    binding.calendarsCard.todayIcon.setOnClickListener(this)

    // Hide the button, we don't need it here
    binding.calendarsCard.moreCalendar.visibility = View.GONE

    binding.calendarsCard.shamsiDateLinear.setOnClickListener(this)
    binding.calendarsCard.shamsiDateDay.setOnClickListener(this)
    binding.calendarsCard.shamsiDate.setOnClickListener(this)
    binding.calendarsCard.gregorianDateLinear.setOnClickListener(this)
    binding.calendarsCard.gregorianDateDay.setOnClickListener(this)
    binding.calendarsCard.gregorianDate.setOnClickListener(this)
    binding.calendarsCard.islamicDateLinear.setOnClickListener(this)
    binding.calendarsCard.islamicDateDay.setOnClickListener(this)
    binding.calendarsCard.islamicDate.setOnClickListener(this)

    // fill views
    binding.selectdayFragment.calendarTypeSpinner.adapter = ArrayAdapter(context,
        android.R.layout.simple_spinner_dropdown_item,
        resources.getStringArray(R.array.calendar_type))

    binding.selectdayFragment.calendarTypeSpinner.setSelection(CalendarUtils.positionFromCalendarType(Utils.getMainCalendar()))
    startingYearOnYearSpinner = UIUtils.fillSelectdaySpinners(context, binding.selectdayFragment)

    binding.selectdayFragment.calendarTypeSpinner.onItemSelectedListener = this

    binding.selectdayFragment.yearSpinner.onItemSelectedListener = this
    binding.selectdayFragment.monthSpinner.onItemSelectedListener = this
    binding.selectdayFragment.daySpinner.onItemSelectedListener = this

    return binding.root
  }

  private fun fillCalendarInfo() {
    val year = startingYearOnYearSpinner + binding.selectdayFragment.yearSpinner.selectedItemPosition
    val month = binding.selectdayFragment.monthSpinner.selectedItemPosition + 1
    val day = binding.selectdayFragment.daySpinner.selectedItemPosition + 1

    val jdn: Long

    try {
      binding.calendarsCard.shamsiContainer.visibility = View.VISIBLE
      binding.calendarsCard.gregorianContainer.visibility = View.VISIBLE
      binding.calendarsCard.islamicContainer.visibility = View.VISIBLE

      when (CalendarUtils.calendarTypeFromPosition(binding.selectdayFragment.calendarTypeSpinner.selectedItemPosition)) {
        CalendarType.GREGORIAN -> {
          jdn = DateConverter.civilToJdn(CivilDate(year, month, day))
          binding.calendarsCard.gregorianContainer.visibility = View.GONE
        }

        CalendarType.ISLAMIC -> {
          jdn = DateConverter.islamicToJdn(IslamicDate(year, month, day))
          binding.calendarsCard.islamicContainer.visibility = View.GONE
        }

        CalendarType.SHAMSI -> {
          jdn = DateConverter.persianToJdn(PersianDate(year, month, day))
          binding.calendarsCard.shamsiContainer.visibility = View.GONE
        }
        else -> {
          jdn = DateConverter.persianToJdn(PersianDate(year, month, day))
          binding.calendarsCard.shamsiContainer.visibility = View.GONE
        }
      }

      val isToday = CalendarUtils.getTodayJdn() == jdn
      UIUtils.fillCalendarsCard(context, jdn, binding.calendarsCard, isToday)

      binding.calendarsCard.calendarsCard.visibility = View.VISIBLE

      val diffDays = Math.abs(CalendarUtils.getTodayJdn() - jdn)
      val civilBase = CivilDate(2000, 1, 1)
      val civilOffset = DateConverter.jdnToCivil(diffDays + DateConverter.civilToJdn(civilBase))
      val yearDiff = civilOffset.year - 2000
      val monthDiff = civilOffset.month - 1
      val dayOfMonthDiff = civilOffset.dayOfMonth - 1
      binding.calendarsCard.diffDate.text = String.format(getString(R.string.date_diff_text),
          Utils.formatNumber(diffDays.toInt()),
          Utils.formatNumber(yearDiff),
          Utils.formatNumber(monthDiff),
          Utils.formatNumber(dayOfMonthDiff))
      binding.calendarsCard.diffDate.visibility = if (diffDays == 0L) View.GONE else View.VISIBLE
      binding.calendarsCard.today.visibility = if (diffDays == 0L) View.GONE else View.VISIBLE
      binding.calendarsCard.todayIcon.visibility = if (diffDays == 0L) View.GONE else View.VISIBLE

    } catch (e: RuntimeException) {
      binding.calendarsCard.calendarsCard.visibility = View.GONE
      Toast.makeText(context, getString(R.string.date_exception), Toast.LENGTH_SHORT).show()
    }

  }

  override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
    when (parent.id) {
      R.id.yearSpinner, R.id.monthSpinner, R.id.daySpinner -> fillCalendarInfo()

      R.id.calendarTypeSpinner -> startingYearOnYearSpinner = UIUtils.fillSelectdaySpinners(context, binding.selectdayFragment)
    }
  }

  override fun onNothingSelected(parent: AdapterView<*>) = Unit

  override fun onClick(view: View) {
    when (view.id) {

      R.id.shamsi_date, R.id.shamsi_date_day -> UIUtils.copyToClipboard(context, binding.calendarsCard.shamsiDateDay.text.toString() + " " +
          binding.calendarsCard.shamsiDate.text.toString().replace("\n", " "))

      R.id.shamsi_date_linear -> UIUtils.copyToClipboard(context, binding.calendarsCard.shamsiDateLinear.text)

      R.id.gregorian_date, R.id.gregorian_date_day -> UIUtils.copyToClipboard(context, binding.calendarsCard.gregorianDateDay.text.toString() + " " +
          binding.calendarsCard.gregorianDate.text.toString().replace("\n", " "))

      R.id.gregorian_date_linear -> UIUtils.copyToClipboard(context, binding.calendarsCard.gregorianDateLinear.text)

      R.id.islamic_date, R.id.islamic_date_day -> UIUtils.copyToClipboard(context, binding.calendarsCard.islamicDateDay.text.toString() + " " +
          binding.calendarsCard.islamicDate.text.toString().replace("\n", " "))

      R.id.islamic_date_linear -> UIUtils.copyToClipboard(context, binding.calendarsCard.islamicDateLinear.text)

      R.id.today, R.id.today_icon -> startingYearOnYearSpinner = UIUtils.fillSelectdaySpinners(context, binding.selectdayFragment)
    }
  }
}
