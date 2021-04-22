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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = FragmentConverterBinding.inflate(inflater, container, false).also { binding ->
        binding.appBar.toolbar.let {
            it.setupUpNavigation()
            it.setTitle(R.string.date_converter)
        }

        binding.calendarsView.toggle()
        binding.calendarsView.hideMoreIcon()

        val todayJdn = getTodayJdn()

        binding.todayButton.setOnClickListener { binding.dayPickerView.jdn = todayJdn }

        binding.dayPickerView.also {
            it.selectedDayListener = fun(jdn) {
                if (jdn == -1L) {
                    binding.calendarsView.visibility = View.GONE
                } else {
                    if (jdn == todayJdn) binding.todayButton.hide() else binding.todayButton.show()

                    binding.calendarsView.visibility = View.VISIBLE
                    val selectedCalendarType = binding.dayPickerView.selectedCalendarType
                    binding.calendarsView.showCalendars(
                        jdn, selectedCalendarType,
                        getOrderedCalendarTypes() - selectedCalendarType
                    )
                }
            }
            it.jdn = getTodayJdn()
            it.anchorView = binding.todayButton
        }
    }.root
}
