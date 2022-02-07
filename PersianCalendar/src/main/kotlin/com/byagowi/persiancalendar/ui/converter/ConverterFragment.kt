package com.byagowi.persiancalendar.ui.converter

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentConverterBinding
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.enabledCalendars
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.setupMenuNavigation
import com.byagowi.persiancalendar.ui.utils.shareText
import com.byagowi.persiancalendar.utils.calculateDaysDifference
import com.byagowi.persiancalendar.utils.dateStringOfOtherCalendars
import com.byagowi.persiancalendar.utils.dayTitleSummary

class ConverterFragment : Fragment(R.layout.fragment_converter) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentConverterBinding.bind(view)

        val spinner = Spinner(binding.appBar.toolbar.context)
        spinner.adapter = ArrayAdapter(
            spinner.context, R.layout.toolbar_dropdown_item,
            listOf(R.string.date_converter, R.string.days_distance).map(spinner.context::getString)
        )
        var isDayDistance = false
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                isDayDistance = position == 1
                binding.secondDayPickerView.isVisible = isDayDistance
                binding.dayDistance.isVisible = isDayDistance
                binding.calendarsView.isVisible = !isDayDistance
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        binding.appBar.toolbar.let { toolbar ->
            toolbar.setupMenuNavigation()
            toolbar.addView(spinner)
        }

        binding.calendarsView.toggle()
        binding.calendarsView.hideMoreIcon()

        val todayJdn = Jdn.today()

        val todayButton = binding.appBar.toolbar.menu.add(R.string.return_to_today).also {
            it.icon =
                binding.appBar.toolbar.context.getCompatDrawable(R.drawable.ic_restore_modified)
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            it.isVisible = false
            it.onClick {
                binding.dayPickerView.jdn = todayJdn
                binding.secondDayPickerView.jdn = todayJdn
            }
        }

        binding.appBar.toolbar.menu.add(R.string.share).also { menu ->
            menu.icon =
                binding.appBar.toolbar.context.getCompatDrawable(R.drawable.ic_content_copy)
            menu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }.onClick {
            val jdn = binding.dayPickerView.jdn
            activity?.shareText(
                if (isDayDistance) binding.dayDistance.text.toString()
                else listOf(
                    dayTitleSummary(jdn, jdn.toCalendar(mainCalendar)),
                    getString(R.string.equivalent_to),
                    dateStringOfOtherCalendars(jdn, spacedComma)
                ).joinToString(" ")
            )
        }

        fun updateDifference() {
            if (binding.secondDayPickerView.selectedCalendarType !=
                binding.dayPickerView.selectedCalendarType
            ) {
                binding.secondDayPickerView.selectedCalendarType =
                    binding.dayPickerView.selectedCalendarType
                binding.secondDayPickerView.jdn = binding.dayPickerView.jdn
            }
            binding.dayDistance.text = calculateDaysDifference(
                resources,
                binding.dayPickerView.jdn,
                binding.secondDayPickerView.jdn,
                binding.dayPickerView.selectedCalendarType
            )
        }

        binding.secondDayPickerView.jdn = todayJdn
        binding.secondDayPickerView.turnToSecondaryDatePicker()
        binding.dayPickerView.selectedDayListener = { jdn ->
            todayButton.isVisible = jdn != todayJdn
            binding.resultCard.isVisible = true
            val selectedCalendarType = binding.dayPickerView.selectedCalendarType
            binding.calendarsView.showCalendars(
                jdn, selectedCalendarType, enabledCalendars - selectedCalendarType
            )
            updateDifference()
        }
        binding.dayPickerView.jdn = todayJdn
        binding.secondDayPickerView.selectedDayListener = { _ ->
            updateDifference()
        }
    }
}
