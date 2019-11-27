package com.byagowi.persiancalendar.ui.converter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentConverterBinding
import com.byagowi.persiancalendar.ui.MainActivity
import com.byagowi.persiancalendar.utils.getOrderedCalendarTypes
import com.byagowi.persiancalendar.utils.getTodayJdn

class ConverterFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentConverterBinding.inflate(inflater, container, false).apply {
        (activity as? MainActivity)?.setTitleAndSubtitle(
            getString(R.string.date_converter), ""
        )

        calendarsView.expand(true)
        calendarsView.hideMoreIcon()
        calendarsView.showHideTodayButtonCallback = fun(show) {
            if (show) todayButton.show() else todayButton.hide()
        }

        todayButton.setOnClickListener { dayPickerView.setDayJdnOnView(getTodayJdn()) }

        dayPickerView.selectedDayListener = fun(jdn) {
            if (jdn == -1L) {
                calendarsView.visibility = View.GONE
            } else {
                calendarsView.visibility = View.VISIBLE
                val selectedCalendarType = dayPickerView.selectedCalendarType
                calendarsView.showCalendars(
                    jdn, selectedCalendarType,
                    getOrderedCalendarTypes() - selectedCalendarType
                )
            }
        }
        dayPickerView.setDayJdnOnView(getTodayJdn())
    }.root
}
