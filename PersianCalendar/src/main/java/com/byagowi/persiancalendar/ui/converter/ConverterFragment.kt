package com.byagowi.persiancalendar.ui.converter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentConverterBinding
import com.byagowi.persiancalendar.di.MainActivityDependency
import com.byagowi.persiancalendar.utils.getOrderedCalendarTypes
import com.byagowi.persiancalendar.utils.getTodayJdn
import dagger.android.support.DaggerFragment
import javax.inject.Inject


class ConverterFragment : DaggerFragment() {

    @Inject
    lateinit var mainActivityDependency: MainActivityDependency

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mainActivityDependency.mainActivity.setTitleAndSubtitle(
            getString(R.string.date_converter),
            ""
        )

        FragmentConverterBinding.inflate(inflater, container, false).apply {

            calendarsView.expand(true)
            calendarsView.hideMoreIcon()
            calendarsView.setOnShowHideTodayButton { show ->
                if (show) todayButton.show() else todayButton.hide()
            }

            todayButton.setOnClickListener { dayPickerView.setDayJdnOnView(getTodayJdn()) }
            swipeToRefresh.setOnRefreshListener {
                dayPickerView.setDayJdnOnView(getTodayJdn())
                swipeToRefresh.isRefreshing = false
            }

            dayPickerView.setOnSelectedDayChangedListener { jdn ->
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

            return root
        }
    }
}
