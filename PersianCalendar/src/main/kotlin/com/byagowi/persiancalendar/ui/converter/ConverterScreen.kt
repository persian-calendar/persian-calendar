package com.byagowi.persiancalendar.ui.converter

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
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
import com.byagowi.persiancalendar.ui.common.CalendarsOverview
import com.byagowi.persiancalendar.ui.common.DayPicker
import com.byagowi.persiancalendar.ui.theme.AppTheme
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey
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

class ConverterFragment : Fragment(R.layout.converter_screen) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModel by viewModels<ConverterViewModel>()

        val binding = ConverterScreenBinding.bind(view)
        val spinner = run {
            val toolbarContext = binding.appBar.toolbar.context
            val spinnerBinding = ConverterSpinnerBinding.inflate(toolbarContext.layoutInflater)
            binding.appBar.toolbar.addView(spinnerBinding.root)
            spinnerBinding.spinner
        }
        spinner.setPopupBackgroundResource(R.drawable.popup_background)
        val availableModes = ConverterScreenMode.entries.filter {
            // Converter doesn't work in Android 5, let's hide it there
            it != ConverterScreenMode.TimeZones || Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        }
        spinner.adapter = ArrayAdapter(
            spinner.context, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
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

        binding.calendarsView.setContent {
            AppTheme {
                var isExpanded by rememberSaveable { mutableStateOf(true) }
                val isLandscape =
                    LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
                val jdn by viewModel.selectedDate.collectAsState()
                val selectedCalendar by viewModel.calendar.collectAsState()

                @Composable
                fun Content() {
                    CalendarsOverview(
                        jdn = jdn,
                        selectedCalendar = selectedCalendar,
                        shownCalendars = enabledCalendars - selectedCalendar,
                        isExpanded = isExpanded
                    ) { isExpanded = !isExpanded }
                }
                if (isLandscape) Content() else {
                    @OptIn(ExperimentalMaterial3Api::class)
                    Card(
                        shape = MaterialTheme.shapes.extraLarge,
                        elevation = CardDefaults.cardElevation(8.dp),
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.padding(16.dp),
                    ) { Content() }
                }
            }
        }

        val todayJdn = Jdn.today()

        val todayButton = binding.appBar.toolbar.menu.add(R.string.return_to_today).also {
            it.icon =
                binding.appBar.toolbar.context.getCompatDrawable(R.drawable.ic_restore_modified)
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            it.onClick {
                viewModel.changeSelectedDate(todayJdn)
                viewModel.changeSecondSelectedDate(todayJdn)
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
                pickerBinding.timeZone.setOnValueChangedListener { picker, _, index ->
                    picker.performHapticFeedbackVirtualKey()
                    if (timeZoneFlow == viewModel.firstTimeZone)
                        viewModel.changeFirstTimeZone(zones[index])
                    else viewModel.changeSecondTimeZone(zones[index])
                }
                pickerBinding.clock.setOnTimeChangedListener { view, hourOfDay, minute ->
                    view.performHapticFeedbackVirtualKey()
                    viewModel.changeClock(hourOfDay, minute, timeZoneFlow.value)
                }
            }
        }

        binding.appBar.toolbar.menu.add(R.string.share).also { menu ->
            menu.icon =
                binding.appBar.toolbar.context.getCompatDrawable(R.drawable.ic_baseline_share)
            menu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }.onClick {
            val jdn = viewModel.selectedDate.value
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

        binding.dayPickerView.setContent {
            AppTheme {
                // Super ugly code
                var calendar by remember { mutableStateOf(viewModel.calendar.value) }
                var jdn by remember { mutableStateOf(viewModel.selectedDate.value) }
                val scope = rememberCoroutineScope()
                var changeToken by remember { mutableStateOf(0) }
                remember {
                    scope.launch {
                        viewModel.calendar.collect {
                            if (calendar != it) {
                                calendar = it
                                ++changeToken
                            }
                        }
                    }
                }
                remember {
                    scope.launch {
                        viewModel.selectedDate.collect {
                            if (jdn != it) {
                                jdn = it
                                ++changeToken
                            }
                        }
                    }
                }
                DayPicker(
                    calendarType = calendar,
                    changeToken = 0,
                    jdn = jdn,
                    setJdn = viewModel::changeSelectedDate
                )
            }
        }
        binding.secondDayPickerView.setContent {
            AppTheme {
                // Super ugly code
                var calendar by remember { mutableStateOf(viewModel.calendar.value) }
                var jdn by remember { mutableStateOf(viewModel.secondSelectedDate.value) }
                val scope = rememberCoroutineScope()
                var changeToken by remember { mutableStateOf(0) }
                remember {
                    scope.launch {
                        viewModel.calendar.collect {
                            if (calendar != it) {
                                calendar = it
                                ++changeToken
                            }
                        }
                    }
                }
                remember {
                    scope.launch {
                        viewModel.secondSelectedDate.collect {
                            if (jdn != it) {
                                jdn = it
                                ++changeToken
                            }
                        }
                    }
                }
                DayPicker(
                    calendarType = calendar,
                    changeToken = 0,
                    jdn = jdn,
                    setJdn = viewModel::changeSecondSelectedDate
                )
            }
        }

        binding.calendarsTypes.value = viewModel.calendar.value
        binding.calendarsTypes.onValueChangeListener = viewModel::changeCalendar
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
                            ConverterScreenMode.Converter -> {}

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
                                binding.inputText.isVisible = false
                                binding.secondDayPickerView.isVisible = false
                                binding.dayPickerView.isVisible = true
                                binding.calendarsTypes.isVisible = true
                                binding.resultText.isVisible = false
                                binding.qrView.isVisible = false
                                binding.calendarsView.isVisible = true
                            }

                            ConverterScreenMode.Distance -> {
                                binding.firstTimeZoneClockPicker.root.isVisible = false
                                binding.secondTimeZoneClockPicker.root.isVisible = false

                                binding.inputText.isVisible = false
                                binding.dayPickerView.isVisible = true
                                binding.calendarsTypes.isVisible = true
                                binding.secondDayPickerView.isVisible = true
                                binding.resultText.isVisible = true
                                binding.qrView.isVisible = false
                                binding.calendarsView.isVisible = false
                            }

                            ConverterScreenMode.Calculator -> {
                                binding.firstTimeZoneClockPicker.root.isVisible = false
                                binding.secondTimeZoneClockPicker.root.isVisible = false
                                binding.inputText.isVisible = true
                                binding.dayPickerView.isVisible = false
                                binding.calendarsTypes.isVisible = false
                                binding.secondDayPickerView.isVisible = false
                                binding.resultText.isVisible = true
                                binding.qrView.isVisible = false
                                binding.calendarsView.isVisible = false
                            }

                            ConverterScreenMode.QrCode -> {
                                binding.firstTimeZoneClockPicker.root.isVisible = false
                                binding.secondTimeZoneClockPicker.root.isVisible = false
                                binding.inputText.isVisible = true
                                binding.dayPickerView.isVisible = false
                                binding.calendarsTypes.isVisible = false
                                binding.secondDayPickerView.isVisible = false
                                binding.resultText.isVisible = false
                                binding.qrView.isVisible = true
                                binding.calendarsView.isVisible = false
                            }

                            ConverterScreenMode.TimeZones -> {
                                initializeTimeZones()
                                binding.firstTimeZoneClockPicker.root.isVisible = true
                                binding.secondTimeZoneClockPicker.root.isVisible = true
                                binding.inputText.isVisible = false
                                binding.dayPickerView.isVisible = false
                                binding.calendarsTypes.isVisible = false
                                binding.secondDayPickerView.isVisible = false
                                binding.resultText.isVisible = false
                                binding.qrView.isVisible = false
                                binding.calendarsView.isVisible = false
                            }
                        }
                    }
                }
            }
        }
    }
}
