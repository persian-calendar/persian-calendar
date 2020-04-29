package com.byagowi.persiancalendar.ui.preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentSettingsBinding
import com.byagowi.persiancalendar.ui.MainActivity
import com.byagowi.persiancalendar.ui.MainActivityViewModel
import com.byagowi.persiancalendar.ui.preferences.interfacecalendar.FragmentInterfaceCalendar
import com.byagowi.persiancalendar.ui.preferences.locationathan.FragmentLocationAthan
import com.byagowi.persiancalendar.ui.preferences.widgetnotification.FragmentWidgetNotification
import com.google.android.material.tabs.TabLayoutMediator

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
class PreferencesFragment : Fragment() {

    private val model: MainActivityViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = FragmentSettingsBinding.inflate(inflater, container, false).apply {
        model.updateActionBar(getString(R.string.settings), "")
        val tabs = listOf(
            R.string.pref_header_interface_calendar to FragmentInterfaceCalendar::class.java,
            R.string.pref_header_widget_location to FragmentWidgetNotification::class.java,
            R.string.pref_header_location_athan to FragmentLocationAthan::class.java
        )
        viewPager.adapter = object : FragmentStateAdapter(this@PreferencesFragment) {
            override fun getItemCount() = tabs.size
            override fun createFragment(position: Int) = tabs[position].second.newInstance()
        }
        TabLayoutMediator(tabLayout, viewPager) { tab, i -> tab.setText(tabs[i].first) }.attach()
    }.root
}
