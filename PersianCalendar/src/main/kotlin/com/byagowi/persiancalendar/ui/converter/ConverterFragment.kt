package com.byagowi.persiancalendar.ui.converter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentConverterBinding
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.ui.utils.setupMenuNavigation
import com.byagowi.persiancalendar.utils.getOrderedCalendarTypes

class ConverterFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentConverterBinding.inflate(inflater, container, false)
        binding.appBar.toolbar.let { toolbar ->
            toolbar.setupMenuNavigation()
            toolbar.setTitle(R.string.date_converter)
        }

        binding.calendarsView.toggle()
        binding.calendarsView.hideMoreIcon()

        val todayJdn = Jdn.today

        binding.todayButton.setOnClickListener { binding.dayPickerView.jdn = todayJdn }

        binding.dayPickerView.also {
            it.selectedDayListener = { jdn ->
                if (jdn == null) {
                    binding.resultCard.isVisible = false
                } else {
                    if (jdn == todayJdn) binding.todayButton.hide() else binding.todayButton.show()

                    binding.resultCard.isVisible = true
                    val selectedCalendarType = binding.dayPickerView.selectedCalendarType
                    binding.calendarsView.showCalendars(
                        jdn, selectedCalendarType, getOrderedCalendarTypes() - selectedCalendarType
                    )
                }
            }
            it.jdn = Jdn.today
            it.anchorView = binding.todayButton
        }
        return binding.root
    }
}
