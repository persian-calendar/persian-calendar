package com.byagowi.persiancalendar.ui.converter

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.ConverterScreenBinding
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.enabledCalendars
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.ui.MainActivity
import com.byagowi.persiancalendar.ui.common.CalendarsOverview
import com.byagowi.persiancalendar.ui.common.CalendarsTypesPicker
import com.byagowi.persiancalendar.ui.common.DayPicker
import com.byagowi.persiancalendar.ui.theme.AppTheme
import com.byagowi.persiancalendar.ui.utils.MaterialCornerExtraLargeTop
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.ui.utils.setupLayoutTransition
import com.byagowi.persiancalendar.ui.utils.shareText
import com.byagowi.persiancalendar.utils.ONE_MINUTE_IN_MILLIS
import com.byagowi.persiancalendar.utils.calculateDaysDifference
import com.byagowi.persiancalendar.utils.dateStringOfOtherCalendars
import com.byagowi.persiancalendar.utils.dayTitleSummary
import io.github.persiancalendar.calculator.eval
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.GregorianCalendar
import java.util.TimeZone

class ConverterFragment : Fragment(R.layout.converter_screen) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModel by viewModels<ConverterViewModel>()

        val binding = ConverterScreenBinding.bind(view)

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
                    Card(
                        shape = MaterialTheme.shapes.extraLarge,
                        elevation = CardDefaults.cardElevation(8.dp),
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.padding(16.dp),
                    ) { Content() }
                }
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

        binding.content.setContent {
            AppTheme {
                ConverterScreen(viewModel) {
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

                            ConverterScreenMode.TimeZones -> timeZonePickerBindingFlowPairs.joinToString(
                                "\n"
                            ) { (pickerBinding) ->
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    zoneNames[pickerBinding.timeZone.value] + ": " +
                                            Clock(
                                                pickerBinding.clock.hour,
                                                pickerBinding.clock.minute
                                            )
                                                .toBasicFormatString()
                                } else ""
                            }

                            ConverterScreenMode.QrCode -> ""
                        }
                    ) else binding.qrView.share(activity)
                }
            }
        }

        binding.dayPickerView.setContent {
            AppTheme {
                Column {
                    val calendar by viewModel.calendar.collectAsState()
                    CalendarsTypesPicker(calendar, viewModel::changeCalendar)

                    val jdn by viewModel.selectedDate.collectAsState()
                    DayPicker(
                        calendarType = calendar,
                        jdn = jdn,
                        setJdn = viewModel::changeSelectedDate
                    )
                }
            }
        }
        binding.secondDayPickerView.setContent {
            AppTheme {
                val calendar by viewModel.calendar.collectAsState()
                val jdn by viewModel.secondSelectedDate.collectAsState()
                DayPicker(
                    calendarType = calendar,
                    jdn = jdn,
                    setJdn = viewModel::changeSecondSelectedDate
                )
            }
        }

        binding.inputText.setText(viewModel.inputText.value)
        binding.inputText.doOnTextChanged { text, _, _, _ ->
            viewModel.changeCalculatorInput(text?.toString() ?: "")
        }

        binding.contentRoot.setupLayoutTransition()
        binding.landscapeSecondPane?.setupLayoutTransition()

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
                    viewModel.screenModeChangeEvent.collectLatest {
                        when (viewModel.screenMode.value) {
                            ConverterScreenMode.Converter -> {
                                binding.firstTimeZoneClockPicker.root.isVisible = false
                                binding.secondTimeZoneClockPicker.root.isVisible = false
                                binding.inputText.isVisible = false
                                binding.secondDayPickerView.isVisible = false
                                binding.dayPickerView.isVisible = true
                                binding.resultText.isVisible = false
                                binding.qrView.isVisible = false
                                binding.calendarsView.isVisible = true
                            }

                            ConverterScreenMode.Distance -> {
                                binding.firstTimeZoneClockPicker.root.isVisible = false
                                binding.secondTimeZoneClockPicker.root.isVisible = false

                                binding.inputText.isVisible = false
                                binding.dayPickerView.isVisible = true
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(viewModel: ConverterViewModel, onShareClick: () -> Unit) {
    val context = LocalContext.current
    // TODO: Ideally this should be onPrimary
    val colorOnAppBar = Color(context.resolveColor(R.attr.colorOnAppBar))
    Column {
        TopAppBar(
            title = {
                var showMenu by remember { mutableStateOf(false) }
                Box(
                    Modifier
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(Color.Gray.copy(alpha = .5f))
                        .clickable { showMenu = !showMenu },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Spacer(Modifier.width(16.dp))
                        Text(stringResource(viewModel.screenMode.value.title))
                        Icon(Icons.Default.ExpandMore, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        ConverterScreenMode.entries.filter {
                            // Converter doesn't work in Android 5, let's hide it there
                            it != ConverterScreenMode.TimeZones || Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        }.forEach {
                            DropdownMenuItem(
                                text = { Text(stringResource(it.title)) },
                                onClick = {
                                    showMenu = false
                                    viewModel.changeScreenMode(it)
                                },
                            )
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                navigationIconContentColor = colorOnAppBar,
                actionIconContentColor = colorOnAppBar,
                titleContentColor = colorOnAppBar,
            ),
            navigationIcon = {
                IconButton(onClick = { (context as? MainActivity)?.openDrawer() }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = stringResource(R.string.open_drawer)
                    )
                }
            },
            actions = {
                val todayButtonVisibility by viewModel.todayButtonVisibilityEvent
                    .collectAsState(initial = false)
                AnimatedVisibility(todayButtonVisibility) {
                    IconButton(onClick = {
                        val todayJdn = Jdn.today()
                        viewModel.changeSelectedDate(todayJdn)
                        viewModel.changeSecondSelectedDate(todayJdn)
                        viewModel.resetTimeZoneClock()
                    }) {
                        Icon(
                            ImageVector.vectorResource(R.drawable.ic_restore_modified),
                            contentDescription = stringResource(R.string.return_to_today),
                        )
                    }
                }
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = { PlainTooltip { Text(text = stringResource(R.string.share)) } },
                    state = rememberTooltipState()
                ) {
                    IconButton(onClick = onShareClick) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.share),
                        )
                    }
                }
            },
        )

        Surface(shape = MaterialCornerExtraLargeTop()) {
            // Just as a placeholder
            Spacer(
                modifier = Modifier
                    .height(24.dp)
                    .fillMaxWidth(),
            )
        }
    }
}
