package com.byagowi.persiancalendar.ui.converter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentConverterBinding
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency
import com.byagowi.persiancalendar.ui.shared.DayPickerView
import com.byagowi.persiancalendar.utils.CalendarType
import com.byagowi.persiancalendar.utils.Utils

import javax.inject.Inject
import dagger.android.support.DaggerFragment

class ConverterFragment : DaggerFragment() {
    @Inject
    internal var mainActivityDependency: MainActivityDependency? = null

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        mainActivityDependency!!.mainActivity.setTitleAndSubtitle(getString(R.string.date_converter), "")

        val binding = FragmentConverterBinding.inflate(inflater,
                container, false)
        val dayPickerView = binding.dayPickerView

        binding.calendarsView.expand(true)
        binding.calendarsView.hideMoreIcon()
        binding.todayButton.setOnClickListener { v -> dayPickerView.setDayJdnOnView(Utils.getTodayJdn()) }
        binding.calendarsView.setOnShowHideTodayButton { show ->
            if (show)
                binding.todayButton.show()
            else
                binding.todayButton.hide()
        }

        dayPickerView.setOnSelectedDayChangedListener { jdn ->
            if (jdn == -1L) {
                binding.calendarsView.visibility = View.GONE
            } else {
                binding.calendarsView.visibility = View.VISIBLE
                val selectedCalendarType = dayPickerView.selectedCalendarType
                val orderedCalendarTypes = Utils.getOrderedCalendarTypes()
                orderedCalendarTypes.remove(selectedCalendarType)
                binding.calendarsView.showCalendars(jdn, selectedCalendarType, orderedCalendarTypes)
            }
        }
        dayPickerView.setDayJdnOnView(Utils.getTodayJdn())

        return binding.root
    }
}
