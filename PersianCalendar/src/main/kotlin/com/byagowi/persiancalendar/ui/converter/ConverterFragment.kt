package com.byagowi.persiancalendar.ui.converter

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ConverterFragment : Fragment(R.layout.fragment_converter) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentConverterBinding.bind(view)

        val model by viewModels<ViewModel>()

        val spinner = Spinner(binding.appBar.toolbar.context)
        spinner.adapter = ArrayAdapter(
            spinner.context, R.layout.toolbar_dropdown_item,
            listOf(R.string.date_converter, R.string.days_distance).map(spinner.context::getString)
        )
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                model.isDayDistance.value = position == 1
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
                if (model.isDayDistance.value) binding.dayDistance.text.toString()
                else listOf(
                    dayTitleSummary(jdn, jdn.toCalendar(mainCalendar)),
                    getString(R.string.equivalent_to),
                    dateStringOfOtherCalendars(jdn, spacedComma)
                ).joinToString(" ")
            )
        }

        binding.secondDayPickerView.jdn = todayJdn
        binding.secondDayPickerView.turnToSecondaryDatePicker()
        binding.dayPickerView.selectedDayListener = { model.jdn.value = it }
        binding.dayPickerView.selectedCalendarListener = { model.calendarType.value = it }
        binding.dayPickerView.jdn = todayJdn
        binding.secondDayPickerView.selectedDayListener = { model.distanceJdn.value = it }

        // Setup flow listeners
        fun updateResult() {
            model.todayButtonVisibility.value = model.jdn.value != todayJdn ||
                    (model.isDayDistance.value && model.distanceJdn.value != todayJdn)
            if (model.isDayDistance.value) {
                binding.dayDistance.text = calculateDaysDifference(
                    resources, model.jdn.value, model.distanceJdn.value,
                    model.calendarType.value
                )
            } else {
                val selectedCalendarType = model.calendarType.value
                binding.calendarsView.showCalendars(
                    model.jdn.value,
                    selectedCalendarType, enabledCalendars - selectedCalendarType
                )
            }
        }

        val viewLifecycleScope = viewLifecycleOwner.lifecycleScope
        model.calendarType.onEach {
            if (model.isDayDistance.value) binding.secondDayPickerView.changeCalendarType(it)
            updateResult()
        }.launchIn(viewLifecycleScope)
        model.jdn.onEach { updateResult() }.launchIn(viewLifecycleScope)
        model.distanceJdn.onEach { updateResult() }.launchIn(viewLifecycleScope)
        model.todayButtonVisibility.onEach(todayButton::setVisible).launchIn(viewLifecycleScope)
        model.isDayDistance.onEach {
            if (it) binding.secondDayPickerView.changeCalendarType(model.calendarType.value)
            binding.secondDayPickerView.isVisible = it
            binding.dayDistance.isVisible = it
            binding.calendarsView.isVisible = !it
            binding.resultCard.isVisible = !it
            updateResult()
        }.launchIn(viewLifecycleScope)
    }
}
