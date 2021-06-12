package com.byagowi.persiancalendar.ui.preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentSettingsBinding
import com.byagowi.persiancalendar.ui.preferences.interfacecalendar.InterfaceCalendarFragment
import com.byagowi.persiancalendar.ui.preferences.locationathan.LocationAthanFragment
import com.byagowi.persiancalendar.ui.preferences.widgetnotification.WidgetNotificationFragment
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.onClick
import com.byagowi.persiancalendar.utils.setupUpNavigation
import com.google.android.material.tabs.TabLayoutMediator

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
class PreferencesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = FragmentSettingsBinding.inflate(inflater, container, false).also { binding ->
        binding.appBar.toolbar.let {
            it.setTitle(R.string.settings)
            it.setupUpNavigation()
            if (BuildConfig.DEBUG) {
                it.menu.add("Generated vs static icons").onClick { showIconsDemoDialog() }
                it.menu.add("Clear preferences store")
                    .onClick { it.context.appPrefs.edit { clear() }; activity?.recreate() }
            }
        }

        val tabs = listOf(
            R.string.pref_header_interface_calendar to InterfaceCalendarFragment::class.java,
            R.string.pref_header_notification_widget to WidgetNotificationFragment::class.java,
            R.string.pref_header_location_athan to LocationAthanFragment::class.java
        )
        val args: PreferencesFragmentArgs by navArgs()
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = tabs.size
            override fun createFragment(position: Int) = tabs[position].second.newInstance().also {
                if (position == args.tab && args.preferenceKey.isNotEmpty()) {
                    it.arguments = bundleOf(PREF_DESTINATION to args.preferenceKey)
                }
            }
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, i ->
            tab.setText(tabs[i].first)
        }.attach()
        binding.viewPager.currentItem = args.tab
    }.root
}

val PREF_DESTINATION = "DESTINATION"

val INTERFACE_CALENDAR_TAB = 0
val WIDGET_NOTIFICATION_TAB = 1
val LOCATION_ATHAN_TAB = 2
