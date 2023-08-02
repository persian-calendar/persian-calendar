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
        val viewModel by viewModels<ConverterViewModel>()

        val binding = ConverterScreenBinding.bind(view)
        val spinner = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val toolbarContext = binding.appBar.toolbar.context
            val spinnerBinding = ConverterSpinnerBinding.inflate(toolbarContext.layoutInflater)
            binding.appBar.toolbar.addView(spinnerBinding.root)
            spinnerBinding.spinner
        } else Spinner(binding.appBar.toolbar.context).also {
            binding.appBar.toolbar.addView(it)
        }
        val availableModes = ConverterScreenMode.entries.filter {
            // Converter doesn't work in Android 5, let's hide it there
            it != ConverterScreenMode.TimeZones || Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        }
        spinner.adapter = ArrayAdapter(
            spinner.context, R.layout.toolbar_dropdown_item,
            availableModes.map { it.title }.map(spinner.context::getString)
        )
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) = viewModel.changeScreenMode(availableModes[position])
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
                binding.dayPickerView.value = todayJdn
                binding.secondDayPickerView.value = todayJdn
                viewModel.resetTimeZoneClock()
            }
        }

        val zones by lazy(LazyThreadSafetyMode.NONE) {
            TimeZone.getAvailableIDs().map(TimeZone::getTimeZone).sortedBy { it.rawOffset }
        }
        val zoneNames by lazy(LazyThreadSafetyMode.NONE) {
            zones.map {
                val offset = Clock.fromMinutesCount(it.rawOffset / ONE_MINUTE_IN_MILLIS.toInt())
                    .toTimeZoneOffsetFormat()
                val id = it.id.replace("_", " ").replace(Regex(".*/"), "")
                "$id ($offset)"
            }.toTypedArray()
        }
        val timeZonePickerBindingFlowPairs = listOf(
            binding.firstTimeZoneClockPicker to viewModel.firstTimeZone,
            binding.secondTimeZoneClockPicker to viewModel.secondTimeZone
        )
        var timeZonesIsEverInitialized = false
        fun initializeTimeZones() {
            if (timeZonesIsEverInitialized) return
            timeZonesIsEverInitialized = true
            timeZonePickerBindingFlowPairs.forEach { (pickerBinding, timeZoneFlow) ->
                pickerBinding.timeZone.minValue = 0
                pickerBinding.timeZone.maxValue = zones.size - 1
                pickerBinding.timeZone.displayedValues = zoneNames
                pickerBinding.timeZone.value = zones.indexOf(timeZoneFlow.value)
                pickerBinding.timeZone.setOnValueChangedListener { _, _, index ->
                    if (timeZoneFlow == viewModel.firstTimeZone)
                        viewModel.changeFirstTimeZone(zones[index])
                    else viewModel.changeSecondTimeZone(zones[index])
                }
                pickerBinding.clock.setOnTimeChangedListener { _, hourOfDay, minute ->
                    viewModel.changeClock(hourOfDay, minute, timeZoneFlow.value)
                }
            }
        }

        binding.appBar.toolbar.menu.add(R.string.share).also { menu ->
            menu.icon =
                binding.appBar.toolbar.context.getCompatDrawable(R.drawable.ic_baseline_share)
            menu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }.onClick {
            val jdn = binding.dayPickerView.value
            if (viewModel.screenMode.value != ConverterScreenMode.QrCode) activity?.shareText(
                when (viewModel.screenMode.value) {
                    ConverterScreenMode.Converter -> listOf(
                        dayTitleSummary(jdn, jdn.toCalendar(mainCalendar)),
                        getString(R.string.equivalent_to),
                        dateStringOfOtherCalendars(jdn, spacedComma)
                    ).joinToString(" ")

                    ConverterScreenMode.Calculator, ConverterScreenMode.Distance ->
                        binding.resultText.text.toString()

                    ConverterScreenMode.TimeZones -> timeZonePickerBindingFlowPairs.joinToString("\n") { (pickerBinding) ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            zoneNames[pickerBinding.timeZone.value] + ": " +
                                    Clock(pickerBinding.clock.hour, pickerBinding.clock.minute)
                                        .toBasicFormatString()
                        } else ""
                    }

                    ConverterScreenMode.QrCode -> ""
                }
            ) else binding.qrView.share(activity)
        }

        binding.calendarsTypes.value = viewModel.calendar.value
        binding.calendarsTypes.onValueChangeListener = viewModel::changeCalendar
        binding.dayPickerView.value = viewModel.selectedDate.value
        binding.dayPickerView.onValueChangeListener = viewModel::changeSelectedDate
        binding.secondDayPickerView.value = viewModel.secondSelectedDate.value
        binding.secondDayPickerView.onValueChangeListener = viewModel::changeSecondSelectedDate
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

        var qrLongClickCount = 0
        binding.qrView.setOnLongClickListener {
            binding.inputText.setText(
                when (qrLongClickCount++ % 3) {
                    0 -> "https://example.com"
                    1 -> "WIFI:S:MySSID;T:WPA;P:MyPassWord;;"
                    else -> "MECARD:N:Smith,John;TEL:123123123;EMAIL:user@example.com;;"
                }
            )
            true
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
                                    viewModel.secondSelectedDate.value,
                                    calendarType = viewModel.calendar.value
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
                                    timeZonePickerBindingFlowPairs.forEach { (pickerBinding, timeZoneFlow) ->
                                        val clock = GregorianCalendar(timeZoneFlow.value)
                                        clock.timeInMillis = viewModel.clock.value.timeInMillis
                                        val hour = clock[GregorianCalendar.HOUR_OF_DAY]
                                        val clockView = pickerBinding.clock
                                        if (clockView.hour != hour) clockView.hour = hour
                                        val minute = clock[GregorianCalendar.MINUTE]
                                        if (clockView.minute != minute) clockView.minute = minute
                                        val zoneView = pickerBinding.timeZone
                                        val zoneIndex = zones.indexOf(timeZoneFlow.value)
                                        if (zoneView.value != zoneIndex) zoneView.value = zoneIndex
                                    }
                                }
                            }

                            ConverterScreenMode.QrCode ->
                                binding.qrView.update(binding.inputText.text?.toString() ?: "")
                        }
                    }
                }
                launch {
                    viewModel.calendar.collectLatest {
                        binding.dayPickerView.calendarType = it
                        if (viewModel.screenMode.value == ConverterScreenMode.Distance)
                            binding.secondDayPickerView.calendarType = it
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
                                binding.firstTimeZoneClockPicker.root.isVisible = false
                                binding.secondTimeZoneClockPicker.root.isVisible = false
                                binding.inputTextWrapper.isVisible = false
                                binding.secondDayPickerView.isVisible = false
                                binding.dayPickerView.isVisible = true
                                binding.calendarsTypes.isVisible = true
                                binding.resultText.isVisible = false
                                binding.qrView.isVisible = false
                                binding.calendarsView.isVisible = true
                                binding.resultCard?.isVisible = true
                            }

                            ConverterScreenMode.Distance -> {
                                binding.firstTimeZoneClockPicker.root.isVisible = false
                                binding.secondTimeZoneClockPicker.root.isVisible = false
                                binding.secondDayPickerView.calendarType = viewModel.calendar.value

                                binding.inputTextWrapper.isVisible = false
                                binding.dayPickerView.isVisible = true
                                binding.calendarsTypes.isVisible = true
                                binding.secondDayPickerView.isVisible = true
                                binding.resultText.isVisible = true
                                binding.qrView.isVisible = false
                                binding.calendarsView.isVisible = false
                                binding.resultCard?.isVisible = false
                            }

                            ConverterScreenMode.Calculator -> {
                                binding.firstTimeZoneClockPicker.root.isVisible = false
                                binding.secondTimeZoneClockPicker.root.isVisible = false
                                binding.inputTextWrapper.isVisible = true
                                binding.dayPickerView.isVisible = false
                                binding.calendarsTypes.isVisible = false
                                binding.secondDayPickerView.isVisible = false
                                binding.resultText.isVisible = true
                                binding.qrView.isVisible = false
                                binding.calendarsView.isVisible = false
                                binding.resultCard?.isVisible = false
                            }

                            ConverterScreenMode.QrCode -> {
                                binding.firstTimeZoneClockPicker.root.isVisible = false
                                binding.secondTimeZoneClockPicker.root.isVisible = false
                                binding.inputTextWrapper.isVisible = true
                                binding.dayPickerView.isVisible = false
                                binding.calendarsTypes.isVisible = false
                                binding.secondDayPickerView.isVisible = false
                                binding.resultText.isVisible = false
                                binding.qrView.isVisible = true
                                binding.calendarsView.isVisible = false
                                binding.resultCard?.isVisible = false
                            }

                            ConverterScreenMode.TimeZones -> {
                                initializeTimeZones()
                                binding.firstTimeZoneClockPicker.root.isVisible = true
                                binding.secondTimeZoneClockPicker.root.isVisible = true
                                binding.inputTextWrapper.isVisible = false
                                binding.dayPickerView.isVisible = false
                                binding.calendarsTypes.isVisible = false
                                binding.secondDayPickerView.isVisible = false
                                binding.resultText.isVisible = false
                                binding.qrView.isVisible = false
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
