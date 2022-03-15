package com.byagowi.persiancalendar.ui.converter

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
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
import io.github.persiancalendar.calculator.eval
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ConverterFragment : Fragment(R.layout.fragment_converter) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentConverterBinding.bind(view)

        val viewModel by viewModels<ConverterViewModel>()
        binding.dayPickerView.changeCalendarType(viewModel.calendar)

        val spinner = Spinner(binding.appBar.toolbar.context)
        spinner.adapter = ArrayAdapter(
            spinner.context, R.layout.toolbar_dropdown_item,
            ConverterScreenMode.values().map { it.title }.map(spinner.context::getString)
        )
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) = viewModel.changeScreenMode(ConverterScreenMode.fromPosition(position))
        }
        spinner.setSelection(viewModel.screenMode.ordinal)

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
                if (viewModel.screenMode == ConverterScreenMode.Converter) listOf(
                    dayTitleSummary(jdn, jdn.toCalendar(mainCalendar)),
                    getString(R.string.equivalent_to),
                    dateStringOfOtherCalendars(jdn, spacedComma)
                ).joinToString(" ") else binding.resultText.text.toString()
            )
        }

        binding.secondDayPickerView.jdn = viewModel.secondSelectedDate
        binding.secondDayPickerView.turnToSecondaryDatePicker()
        binding.dayPickerView.selectedDayListener = viewModel::changeSelectedDate
        binding.dayPickerView.selectedCalendarListener = viewModel::changeCalendar
        binding.dayPickerView.jdn = viewModel.selectedDate
        binding.secondDayPickerView.selectedDayListener = viewModel::changeSecondSelectedDate
        binding.inputText.setText(viewModel.inputText)
        binding.inputText.doOnTextChanged { text, _, _, _ ->
            viewModel.changeCalculatorInput(text?.toString() ?: "")
        }

        // Setup view model change listeners
        viewModel.updateEvent
            .onEach {
                when (viewModel.screenMode) {
                    ConverterScreenMode.Converter -> {
                        val selectedCalendarType = viewModel.calendar
                        binding.calendarsView.showCalendars(
                            viewModel.selectedDate,
                            selectedCalendarType, enabledCalendars - selectedCalendarType
                        )
                    }
                    ConverterScreenMode.Distance -> {
                        binding.resultText.textDirection = View.TEXT_DIRECTION_INHERIT
                        binding.resultText.text = calculateDaysDifference(
                            resources, viewModel.selectedDate, viewModel.secondSelectedDate,
                            viewModel.calendar
                        )
                    }
                    ConverterScreenMode.Calculator -> {
                        binding.resultText.textDirection = View.TEXT_DIRECTION_LTR
                        binding.resultText.text = runCatching {
                            // running this inside a runCatching block is absolutely important
                            eval(binding.inputText.text?.toString() ?: "")
                        }.getOrElse { it.message }
                    }
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
        viewModel.calendarChangeEvent
            .onEach {
                if (viewModel.screenMode == ConverterScreenMode.Distance)
                    binding.secondDayPickerView.changeCalendarType(it)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
        viewModel.todayButtonVisibilityEvent
            .distinctUntilChanged()
            .onEach(todayButton::setVisible)
            .launchIn(viewLifecycleOwner.lifecycleScope)
        viewModel.screenModeChangeEvent
            .onEach {
                when (viewModel.screenMode) {
                    ConverterScreenMode.Converter -> {
                        binding.inputTextWrapper.isVisible = false
                        binding.secondDayPickerView.isVisible = false
                        binding.dayPickerView.isVisible = true
                        binding.resultText.isVisible = false
                        binding.calendarsView.isVisible = true
                        binding.resultCard.isVisible = true
                    }
                    ConverterScreenMode.Distance -> {
                        binding.secondDayPickerView.changeCalendarType(viewModel.calendar)

                        binding.inputTextWrapper.isVisible = false
                        binding.dayPickerView.isVisible = true
                        binding.secondDayPickerView.isVisible = true
                        binding.resultText.isVisible = true
                        binding.calendarsView.isVisible = false
                        binding.resultCard.isVisible = false
                    }
                    ConverterScreenMode.Calculator -> {
                        binding.inputTextWrapper.isVisible = true
                        binding.dayPickerView.isVisible = false
                        binding.secondDayPickerView.isVisible = false
                        binding.resultText.isVisible = true
                        binding.calendarsView.isVisible = false
                        binding.resultCard.isVisible = false
                    }
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }
}
