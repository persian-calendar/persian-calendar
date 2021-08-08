package com.byagowi.persiancalendar.ui.preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
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
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.setupMenuNavigation
import com.byagowi.persiancalendar.utils.appPrefs
import com.google.android.material.tabs.TabLayoutMediator

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
class PreferencesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSettingsBinding.inflate(inflater, container, false)
        binding.appBar.toolbar.let { toolbar ->
            toolbar.setTitle(R.string.settings)
            toolbar.setupMenuNavigation(this)
            if (BuildConfig.DEBUG) {
                toolbar.menu.add("Static vs generated icons").onClick { showIconsDemoDialog() }
                toolbar.menu.add("Clear preferences store")
                    .onClick { toolbar.context.appPrefs.edit { clear() }; activity?.recreate() }
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
        return binding.root
    }
}

const val PREF_DESTINATION = "DESTINATION"
const val INTERFACE_CALENDAR_TAB = 0
const val WIDGET_NOTIFICATION_TAB = 1
const val LOCATION_ATHAN_TAB = 2
