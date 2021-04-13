package com.byagowi.persiancalendar.ui.converter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentConverterBinding
import com.byagowi.persiancalendar.utils.getOrderedCalendarTypes
import com.byagowi.persiancalendar.utils.getTodayJdn
import com.byagowi.persiancalendar.utils.setupUpNavigation

class ConverterFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentConverterBinding.inflate(inflater, container, false).apply {
        appBar.toolbar.let {
            it.setupUpNavigation()
            it.setTitle(R.string.date_converter)
        }

        calendarsView.toggle()
        calendarsView.hideMoreIcon()

        val todayJdn = getTodayJdn()

        todayButton.setOnClickListener { dayPickerView.jdn = todayJdn }

        dayPickerView.also {
            it.selectedDayListener = fun(jdn) {
                if (jdn == -1L) {
                    calendarsView.visibility = View.GONE
                } else {
                    if (jdn == todayJdn) todayButton.hide() else todayButton.show()

                    calendarsView.visibility = View.VISIBLE
                    val selectedCalendarType = dayPickerView.selectedCalendarType
                    calendarsView.showCalendars(
                        jdn, selectedCalendarType,
                        getOrderedCalendarTypes() - selectedCalendarType
                    )
                }
            }
            it.jdn = getTodayJdn()
            it.anchorView = todayButton
        }
    }.root
}
