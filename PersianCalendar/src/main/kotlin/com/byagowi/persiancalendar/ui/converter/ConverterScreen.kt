package com.byagowi.persiancalendar.ui.converter

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.ConverterScreenBinding
import com.byagowi.persiancalendar.databinding.ConverterSpinnerBinding
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.enabledCalendars
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.setupLayoutTransition
import com.byagowi.persiancalendar.ui.utils.setupMenuNavigation
import com.byagowi.persiancalendar.ui.utils.shareText
import com.byagowi.persiancalendar.utils.ONE_MINUTE_IN_MILLIS
import com.byagowi.persiancalendar.utils.calculateDaysDifference
import com.byagowi.persiancalendar.utils.dateStringOfOtherCalendars
import com.byagowi.persiancalendar.utils.dayTitleSummary
import io.github.persiancalendar.calculator.eval
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.GregorianCalendar
import java.util.TimeZone

class ConverterScreen : Fragment(R.layout.converter_screen) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = ConverterScreenBinding.bind(view)

        val viewModel by viewModels<ConverterViewModel>()
        binding.dayPickerView.changeCalendarType(viewModel.calendar.value)

        val spinner = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val toolbarContext = binding.appBar.toolbar.context
            val spinnerBinding = ConverterSpinnerBinding.inflate(toolbarContext.layoutInflater)
            binding.appBar.toolbar.addView(spinnerBinding.root)
            spinnerBinding.spinner
        } else Spinner(binding.appBar.toolbar.context).also {
            binding.appBar.toolbar.addView(it)
        }
        spinner.adapter = ArrayAdapter(
            spinner.context, R.layout.toolbar_dropdown_item,
            enumValues<ConverterScreenMode>().filter {
                it != ConverterScreenMode.Converter || Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            }.map { it.title }.map(spinner.context::getString)
        )
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) = viewModel.changeScreenMode(ConverterScreenMode.fromPosition(position))
        }
        spinner.setSelection(viewModel.screenMode.value.ordinal)

        binding.appBar.toolbar.setupMenuNavigation()

        binding.calendarsView.post { // is in 'post' as otherwise will show ann empty circular indicator
            binding.calendarsView.toggle()
        }
        binding.calendarsView.hideMoreIcon()

        val todayJdn = Jdn.today()

        val todayButton = binding.appBar.toolbar.menu.add(R.string.return_to_today).also {
            it.icon =
                binding.appBar.toolbar.context.getCompatDrawable(R.drawable.ic_restore_modified)
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            it.onClick {
                binding.dayPickerView.jdn = todayJdn
                binding.secondDayPickerView.jdn = todayJdn
                viewModel.resetTimeZoneClock()
            }
        }

        val timeZones = TimeZone.getAvailableIDs()
            .map { TimeZone.getTimeZone(it) }.sortedBy { it.rawOffset }
        val timeZoneNames = timeZones.map {
            val offset = Clock.fromMinutesCount(it.rawOffset / ONE_MINUTE_IN_MILLIS.toInt())
                .toTimeZoneOffsetFormat()
            val id = it.id.replace("_", " ").let { id ->
                if ("/" in id) id.split("/")[1] else id
            }
            "$id ($offset)"
        }.toTypedArray()

        listOf(
            binding.firstTimezoneClockPicker to viewModel.firstTimeZone,
            binding.secondTimezoneClockPicker to viewModel.secondTimeZone
        ).forEach { (binding, timeZone) ->
            binding.timeZone.minValue = 0
            binding.timeZone.maxValue = timeZones.size - 1
            binding.timeZone.displayedValues = timeZoneNames
            binding.timeZone.value = timeZones.indexOf(timeZone.value)
            binding.timeZone.setOnValueChangedListener { _, _, index ->
                if (timeZone == viewModel.firstTimeZone)
                    viewModel.changeFirstTimeZone(timeZones[index])
                else viewModel.changeSecondTimeZone(timeZones[index])
            }
            binding.clock.setOnTimeChangedListener { _, hourOfDay, minute ->
                viewModel.changeClock(hourOfDay, minute, timeZone.value)
            }
        }

        binding.appBar.toolbar.menu.add(R.string.share).also { menu ->
            menu.icon =
                binding.appBar.toolbar.context.getCompatDrawable(R.drawable.ic_baseline_share)
            menu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }.onClick {
            val jdn = binding.dayPickerView.jdn
            activity?.shareText(
                when (viewModel.screenMode.value) {
                    ConverterScreenMode.Converter -> listOf(
                        dayTitleSummary(jdn, jdn.toCalendar(mainCalendar)),
                        getString(R.string.equivalent_to),
                        dateStringOfOtherCalendars(jdn, spacedComma)
                    ).joinToString(" ")

                    ConverterScreenMode.Calculator, ConverterScreenMode.Distance ->
                        binding.resultText.text.toString()

                    ConverterScreenMode.TimeZones -> listOf(
                        binding.firstTimezoneClockPicker,
                        binding.secondTimezoneClockPicker
                    ).joinToString("\n") {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            timeZoneNames[it.timeZone.value] + ": " +
                                    Clock(it.clock.hour, it.clock.minute).toBasicFormatString()
                        } else ""
                    }
                }
            )
        }

        binding.secondDayPickerView.jdn = viewModel.secondSelectedDate.value
        binding.secondDayPickerView.turnToSecondaryDatePicker()
        binding.dayPickerView.selectedDayListener = viewModel::changeSelectedDate
        binding.dayPickerView.selectedCalendarListener = viewModel::changeCalendar
        binding.dayPickerView.jdn = viewModel.selectedDate.value
        binding.secondDayPickerView.selectedDayListener = viewModel::changeSecondSelectedDate
        binding.inputText.setText(viewModel.inputText.value)
        binding.inputText.doOnTextChanged { text, _, _, _ ->
            viewModel.changeCalculatorInput(text?.toString() ?: "")
        }

        binding.contentRoot.setupLayoutTransition()
        binding.landscapeSecondPane?.setupLayoutTransition()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.contentRoot.updatePadding(bottom = insets.bottom)
            binding.appBar.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
            }
            WindowInsetsCompat.CONSUMED
        }

        // Setup view model change listeners
        // https://developer.android.com/topic/libraries/architecture/coroutines#lifecycle-aware
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.updateEvent.collectLatest {
                        when (viewModel.screenMode.value) {
                            ConverterScreenMode.Converter -> {
                                val selectedCalendarType = viewModel.calendar.value
                                binding.calendarsView.showCalendars(
                                    viewModel.selectedDate.value,
                                    selectedCalendarType,
                                    enabledCalendars - selectedCalendarType
                                )
                            }

                            ConverterScreenMode.Distance -> {
                                binding.resultText.textDirection = View.TEXT_DIRECTION_INHERIT
                                binding.resultText.text = calculateDaysDifference(
                                    resources, viewModel.selectedDate.value,
                                    viewModel.secondSelectedDate.value, viewModel.calendar.value
                                )
                            }

                            ConverterScreenMode.Calculator -> {
                                binding.resultText.textDirection = View.TEXT_DIRECTION_LTR
                                binding.resultText.text = runCatching {
                                    // running this inside a runCatching block is absolutely important
                                    eval(binding.inputText.text?.toString() ?: "")
                                }.getOrElse { it.message }
                            }

                            ConverterScreenMode.TimeZones -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    listOf(
                                        viewModel.firstTimeZone to binding.firstTimezoneClockPicker,
                                        viewModel.secondTimeZone to binding.secondTimezoneClockPicker
                                    ).forEach { (timeZone, timePicker) ->
                                        val clock = GregorianCalendar(timeZone.value)
                                        clock.timeInMillis = viewModel.clock.value.timeInMillis
                                        val hour = clock[GregorianCalendar.HOUR_OF_DAY]
                                        val clockView = timePicker.clock
                                        if (clockView.hour != hour) clockView.hour = hour
                                        val minute = clock[GregorianCalendar.MINUTE]
                                        if (clockView.minute != minute) clockView.minute = minute
                                        val timeZoneView = timePicker.timeZone
                                        val timeZoneIndex = timeZones.indexOf(timeZone.value)
                                        if (timeZoneView.value != timeZoneIndex)
                                            timeZoneView.value = timeZoneIndex
                                    }
                                }
                            }
                        }
                    }
                }
                launch {
                    viewModel.calendarChangeEvent.collectLatest {
                        if (viewModel.screenMode.value == ConverterScreenMode.Distance)
                            binding.secondDayPickerView.changeCalendarType(it)
                    }
                }
                launch {
                    viewModel.todayButtonVisibilityEvent
                        .distinctUntilChanged()
                        .collectLatest(todayButton::setVisible)
                }
                launch {
                    viewModel.screenModeChangeEvent.collectLatest {
                        when (viewModel.screenMode.value) {
                            ConverterScreenMode.Converter -> {
                                binding.firstTimezoneClockPicker.root.isVisible = false
                                binding.secondTimezoneClockPicker.root.isVisible = false
                                binding.inputTextWrapper.isVisible = false
                                binding.secondDayPickerView.isVisible = false
                                binding.dayPickerView.isVisible = true
                                binding.resultText.isVisible = false
                                binding.calendarsView.isVisible = true
                                binding.resultCard?.isVisible = true
                            }

                            ConverterScreenMode.Distance -> {
                                binding.firstTimezoneClockPicker.root.isVisible = false
                                binding.secondTimezoneClockPicker.root.isVisible = false
                                binding.secondDayPickerView.changeCalendarType(viewModel.calendar.value)

                                binding.inputTextWrapper.isVisible = false
                                binding.dayPickerView.isVisible = true
                                binding.secondDayPickerView.isVisible = true
                                binding.resultText.isVisible = true
                                binding.calendarsView.isVisible = false
                                binding.resultCard?.isVisible = false
                            }

                            ConverterScreenMode.Calculator -> {
                                binding.firstTimezoneClockPicker.root.isVisible = false
                                binding.secondTimezoneClockPicker.root.isVisible = false
                                binding.inputTextWrapper.isVisible = true
                                binding.dayPickerView.isVisible = false
                                binding.secondDayPickerView.isVisible = false
                                binding.resultText.isVisible = true
                                binding.calendarsView.isVisible = false
                                binding.resultCard?.isVisible = false
                            }

                            ConverterScreenMode.TimeZones -> {
                                binding.firstTimezoneClockPicker.root.isVisible = true
                                binding.secondTimezoneClockPicker.root.isVisible = true
                                binding.inputTextWrapper.isVisible = false
                                binding.dayPickerView.isVisible = false
                                binding.secondDayPickerView.isVisible = false
                                binding.resultText.isVisible = false
                                binding.calendarsView.isVisible = false
                                binding.resultCard?.isVisible = false
                            }
                        }
                    }
                }
            }
        }
    }
}
