package com.byagowi.persiancalendar.ui.converter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
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
import com.byagowi.persiancalendar.utils.EventsStore
import com.byagowi.persiancalendar.utils.dateStringOfOtherCalendars
import com.byagowi.persiancalendar.utils.dayTitleSummary
import com.byagowi.persiancalendar.utils.getA11yDaySummary

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

        val todayJdn = Jdn.today()

        binding.appBar.toolbar.menu.add(R.string.share).also { menu ->
            menu.icon =
                binding.appBar.toolbar.context.getCompatDrawable(R.drawable.ic_baseline_share)
            menu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }.onClick {
            val jdn = binding.dayPickerView.jdn
            activity?.shareText(
                listOf(
                    dayTitleSummary(jdn, jdn.toCalendar(mainCalendar)),
                    getString(R.string.equivalent_to),
                    dateStringOfOtherCalendars(jdn, spacedComma)
                ).joinToString(" ")
            )
        }
        val todayButton = binding.appBar.toolbar.menu.add(R.string.return_to_today).also {
            it.icon =
                binding.appBar.toolbar.context.getCompatDrawable(R.drawable.ic_restore_modified)
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            it.isVisible = false
            it.onClick { binding.dayPickerView.jdn = todayJdn }
        }

        binding.dayPickerView.also {
            it.selectedDayListener = { jdn ->
                todayButton.isVisible = jdn != todayJdn
                binding.resultCard.isVisible = true
                val selectedCalendarType = binding.dayPickerView.selectedCalendarType
                binding.calendarsView.showCalendars(
                    jdn, selectedCalendarType, enabledCalendars - selectedCalendarType
                )
            }
            it.jdn = Jdn.today()
        }
        return binding.root
    }
}
