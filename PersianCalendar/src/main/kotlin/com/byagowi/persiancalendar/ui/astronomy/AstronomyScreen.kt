package com.byagowi.persiancalendar.ui.astronomy

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.MenuItem
import android.view.View
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentAstronomyBinding
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Season
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.ui.calendar.dialogs.showDayPickerDialog
import com.byagowi.persiancalendar.ui.common.ArrowView
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.setupMenuNavigation
import com.byagowi.persiancalendar.utils.formatDateAndTime
import com.byagowi.persiancalendar.utils.isRtl
import com.byagowi.persiancalendar.utils.toCivilDate
import com.byagowi.persiancalendar.utils.toJavaCalendar
import com.google.android.material.switchmaterial.SwitchMaterial
import io.github.cosinekitty.astronomy.SeasonsInfo
import io.github.cosinekitty.astronomy.seasons
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.PersianDate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.*
import kotlin.math.abs

class AstronomyScreen : Fragment(R.layout.fragment_astronomy) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAstronomyBinding.bind(view)

        binding.appBar.toolbar.let {
            it.setTitle(R.string.astronomy)
            it.setupMenuNavigation()
        }

        val resetButton = binding.appBar.toolbar.menu.add(R.string.return_to_today).also {
            it.icon =
                binding.appBar.toolbar.context.getCompatDrawable(R.drawable.ic_restore_modified)
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            it.isVisible = false
        }

        val viewModel by viewModels<AstronomyViewModel>()
        if (viewModel.time.value == AstronomyViewModel.DEFAULT_TIME)
            viewModel.changeToDayOffset(navArgs<AstronomyScreenArgs>().value.dayOffset)

        binding.solarView.setOnLongClickListener longClick@{
            val activity = activity ?: return@longClick true
            val time = GregorianCalendar().also { it.add(Calendar.MINUTE, viewModel.time.value) }
            showHoroscopesDialog(activity, time.time)
            true
        }

        val seasonsCache = mutableMapOf<Int, SeasonsInfo>()
        fun calculateSeasons(year: Int) = seasonsCache.getOrPut(year) { seasons(year) }

        val headerCache = mutableMapOf<Long, String>()

        fun screenUpdate(state: AstronomyState) {
            val tropical = viewModel.isTropical.value
            val sunZodiac =
                if (tropical) Zodiac.fromTropical(state.sun.elon)
                else Zodiac.fromIau(state.sun.elon)
            val moonZodiac =
                if (tropical) Zodiac.fromTropical(state.moon.elon)
                else Zodiac.fromIau(state.moon.elon)

            binding.sunText.text = sunZodiac.format(view.context, true) // ☉☀️
            binding.moonText.text =
                moonZodiac.format(binding.root.context, true) // ☽it.moonPhaseEmoji
            binding.time.text = state.date.formatDateAndTime()

            val civilDate = state.date.toCivilDate()
            val jdn = civilDate.toJdn()
            val persianDate = PersianDate(jdn)
            binding.headerInformation.text = headerCache.getOrPut(jdn) {
                state.generateHeader(context ?: return, persianDate)
            }

            (1..4).forEach { i ->
                when (i) {
                    1 -> binding.firstSeasonText
                    2 -> binding.secondSeasonText
                    3 -> binding.thirdSeasonText
                    else -> binding.fourthSeasonText
                }.text = Date(
                    calculateSeasons(
                        CivilDate(PersianDate(persianDate.year, i * 3, 29)).year
                    ).let {
                        when (i) {
                            1 -> it.juneSolstice
                            2 -> it.septemberEquinox
                            3 -> it.decemberSolstice
                            else -> it.marchEquinox
                        }
                    }.toMillisecondsSince1970()
                ).toJavaCalendar().formatDateAndTime()
            }
        }

        val tropicalMenuItem = binding.appBar.toolbar.menu.add(R.string.tropical).also { menuItem ->
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            menuItem.actionView = SwitchMaterial(binding.appBar.toolbar.context).also { switch ->
                @SuppressLint("SetTextI18n")
                switch.text = getString(R.string.tropical) + " "
                switch.isChecked = viewModel.isTropical.value
                switch.setOnClickListener { viewModel.changeTropical(switch.isChecked) }
            }
        }

        binding.railView.itemIconTintList = null // makes it to not apply tint on modes icons
        binding.railView.menu.also { menu ->
            val buttons = AstronomyMode.values().map { menu.add(it.title).setIcon(it.icon) to it }
            binding.railView.post {
                buttons.forEach { (item, mode) ->
                    if (viewModel.mode.value == mode) item.isChecked = true
                    item.onClick { viewModel.changeScreenMode(mode) }
                }
            }
        }

        listOf(
            binding.firstSeasonChip to 4, binding.secondSeasonChip to 7,
            binding.thirdSeasonChip to 10, binding.fourthSeasonChip to 1
        ).map { (chip, month) -> // 'month' is month number of first Persian month in the season
            val season = Season.fromPersianCalendar(PersianDate(1400, month, 1), coordinates)
            chip.setText(season.nameStringId)
            chip.chipBackgroundColor = ColorStateList.valueOf(season.color)
        }

        // Tells solar view whether it needs to apply changes immediately or in a value animator
        var immediate = false

        binding.appBar.toolbar.menu.add(R.string.goto_date).also {
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            it.onClick {
                val startJdn = Jdn(
                    GregorianCalendar().apply { add(Calendar.MINUTE, viewModel.time.value) }
                        .toCivilDate()
                )
                showDayPickerDialog(activity ?: return@onClick, startJdn, R.string.go) { jdn ->
                    immediate = false
                    viewModel.changeToDayOffset(jdn - Jdn.today())
                }
            }
        }

        resetButton.onClick {
            immediate = false
            viewModel.changeTime(0)
            resetButton.isVisible = false
        }

        val viewDirection = if (resources.isRtl) -1 else 1

        var lastButtonClickTimestamp = System.currentTimeMillis()
        binding.slider.smoothScrollBy(200 * viewDirection, 0)

        var latestVibration = 0L
        binding.slider.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dx == 0) return
                val current = System.currentTimeMillis()
                if (current - lastButtonClickTimestamp < 2000) return
                if (current >= latestVibration + 35_000 / abs(dx)) {
                    binding.slider.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    latestVibration = current
                }
                immediate = true
                viewModel.addTime(dx * viewDirection)
            }
        })

        fun buttonScrollSlider(days: Int): Boolean {
            lastButtonClickTimestamp = System.currentTimeMillis()
            binding.slider.smoothScrollBy(50 * days * viewDirection, 0)
            immediate = false
            viewModel.addDayOffset(days)
            return true
        }
        binding.startArrow.rotateTo(ArrowView.Direction.START)
        binding.startArrow.setOnClickListener {
            binding.startArrow.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            buttonScrollSlider(-1)
        }
        binding.startArrow.setOnLongClickListener { buttonScrollSlider(-365) }
        binding.startArrow.contentDescription =
            getString(R.string.previous_x, getString(R.string.day))
        binding.endArrow.rotateTo(ArrowView.Direction.END)
        binding.endArrow.setOnClickListener {
            binding.endArrow.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            buttonScrollSlider(1)
        }
        binding.endArrow.setOnLongClickListener { buttonScrollSlider(365) }
        binding.endArrow.contentDescription = getString(R.string.next_x, getString(R.string.day))

        val layoutTransition = LayoutTransition().apply {
            enableTransitionType(LayoutTransition.APPEARING or LayoutTransition.DISAPPEARING)
            setAnimateParentHierarchy(false)
        }
        binding.firstColumn.layoutTransition = layoutTransition
        binding.secondColumn.layoutTransition = layoutTransition

        // Setup view model change listeners
        viewModel.isTropical
            .onEach { isTropical ->
                val time = GregorianCalendar().apply { add(Calendar.MINUTE, viewModel.time.value) }
                screenUpdate(AstronomyState(time))
                binding.solarView.isTropicalDegree = isTropical
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
        viewModel.resetButtonVisibilityEvent
            .onEach { resetButton.isVisible = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)
        viewModel.mode
            .onEach {
                binding.solarView.mode = it
                val showTropicalRelatedElements = it == AstronomyMode.Earth
                tropicalMenuItem.isVisible = showTropicalRelatedElements
                binding.sunStatusWrapper.isInvisible = !showTropicalRelatedElements
                binding.moonStatusWrapper.isInvisible = !showTropicalRelatedElements
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
        viewModel.time
            .onEach {
                val time = GregorianCalendar().apply { add(Calendar.MINUTE, viewModel.time.value) }
                binding.solarView.setTime(time, immediate, ::screenUpdate)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }
}
