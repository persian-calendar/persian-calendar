package com.byagowi.persiancalendar.ui.preferences

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentSettingsBinding
import com.byagowi.persiancalendar.ui.MainActivity
import com.byagowi.persiancalendar.ui.preferences.interfacecalendar.FragmentInterfaceCalendar
import com.byagowi.persiancalendar.ui.preferences.locationathan.FragmentLocationAthan
import com.byagowi.persiancalendar.ui.preferences.widgetnotification.FragmentWidgetNotification
import com.byagowi.persiancalendar.utils.layoutInflater
import com.google.android.material.tabs.TabLayoutMediator

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
class PreferencesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = FragmentSettingsBinding.inflate(
        (context as Context).layoutInflater, container, false
    ).apply {
        val mainActivity = activity as MainActivity
        mainActivity.setTitleAndSubtitle(getString(R.string.settings), "")
        viewPager.adapter = object : FragmentStateAdapter(mainActivity) {
            override fun getItemCount() = 3
            override fun createFragment(position: Int) = when (position) {
                0 -> FragmentInterfaceCalendar()
                1 -> FragmentWidgetNotification()
                2 -> FragmentLocationAthan()
                else -> Fragment()
            }
        }
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.setText(
                when (position) {
                    0 -> R.string.pref_header_interface_calendar
                    1 -> R.string.pref_header_widget_location
                    2 -> R.string.pref_header_location_athan
                    else -> R.string.pref_header_location_athan
                }
            )
        }.attach()
    }.root
}
