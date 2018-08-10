package com.byagowi.persiancalendar.view.dialog

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.databinding.DataBindingUtil
import calendar.CalendarType
import calendar.DateConverter
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.SelectdayFragmentBinding
import com.byagowi.persiancalendar.util.CalendarUtils
import com.byagowi.persiancalendar.util.UIUtils
import com.byagowi.persiancalendar.util.Utils
import com.byagowi.persiancalendar.view.fragment.CalendarFragment

/**
 * Created by ebrahim on 3/20/16.
 */
class SelectDayDialog : AppCompatDialogFragment() {
  private var startingYearOnYearSpinner = 0

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val localActivity = activity ?: return Dialog(null)

    val inflater = localActivity.getLayoutInflater()

    val binding = DataBindingUtil.inflate<SelectdayFragmentBinding>(inflater,
        R.layout.selectday_fragment, null, false)

    binding.calendarTypeSpinner.adapter = ArrayAdapter(context,
        android.R.layout.simple_spinner_dropdown_item,
        resources.getStringArray(R.array.calendar_type))

    binding.calendarTypeSpinner.setSelection(CalendarUtils.positionFromCalendarType(Utils.mainCalendar))
    val ctx = context
    if (ctx != null) {
      startingYearOnYearSpinner = UIUtils.fillSelectdaySpinners(ctx, binding)
    }

    binding.calendarTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
      override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
        if (ctx != null) {
          startingYearOnYearSpinner = UIUtils.fillSelectdaySpinners(ctx, binding)
        }
      }

      override fun onNothingSelected(adapterView: AdapterView<*>) {}
    }

    val builder = AlertDialog.Builder(localActivity)
    builder.setView(binding.root)
    builder.setCustomTitle(null)
    builder.setPositiveButton(R.string.go) { _, _ ->

      val year = startingYearOnYearSpinner + binding.yearSpinner.selectedItemPosition
      val month = binding.monthSpinner.selectedItemPosition + 1
      val day = binding.daySpinner.selectedItemPosition + 1

      val calendarFragment = localActivity
          .supportFragmentManager
          .findFragmentByTag(CalendarFragment::class.java.name) as CalendarFragment?

      try {
        when (CalendarUtils.calendarTypeFromPosition(binding.calendarTypeSpinner.selectedItemPosition)) {
          CalendarType.GREGORIAN -> calendarFragment?.bringDate(DateConverter.civilToJdn(year.toLong(), month.toLong(), day.toLong()))

          CalendarType.ISLAMIC -> calendarFragment?.bringDate(DateConverter.islamicToJdn(year, month, day))

          CalendarType.SHAMSI -> calendarFragment?.bringDate(DateConverter.persianToJdn(year, month, day))
        }
      } catch (e: RuntimeException) {
        if (ctx != null) {
          Toast.makeText(context, getString(R.string.date_exception), Toast.LENGTH_SHORT).show()
        }
        Log.e("SelectDayDialog", "", e)
      }
    }

    return builder.create()
  }
}
