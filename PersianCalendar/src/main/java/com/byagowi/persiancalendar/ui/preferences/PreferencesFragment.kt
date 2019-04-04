package com.byagowi.persiancalendar.ui.preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentSettingsBinding
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency
import com.byagowi.persiancalendar.ui.preferences.interfacecalendar.FragmentInterfaceCalendar
import com.byagowi.persiancalendar.ui.preferences.locationathan.FragmentLocationAthan
import com.byagowi.persiancalendar.ui.preferences.widgetnotification.FragmentWidgetNotification

import javax.inject.Inject
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import dagger.android.support.DaggerFragment

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
class PreferencesFragment : DaggerFragment() {
    @Inject
    internal var mainActivityDependency: MainActivityDependency? = null

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainActivityDependency!!.mainActivity.setTitleAndSubtitle(getString(R.string.settings), "")

        val binding = FragmentSettingsBinding.inflate(
                LayoutInflater.from(container!!.context), container, false)

        binding.viewPager.adapter = ViewPagerAdapter(childFragmentManager, 3)
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        return binding.root
    }


    internal inner class ViewPagerAdapter(manager: FragmentManager, var pageCount: Int) : FragmentPagerAdapter(manager) {

        override fun getItem(position: Int): Fragment {
            var fragment = Fragment()
            when (position) {
                0 -> fragment = FragmentInterfaceCalendar()
                1 -> fragment = FragmentWidgetNotification()
                2 -> fragment = FragmentLocationAthan()
                else -> {
                }
            }
            return fragment
        }

        override fun getCount(): Int {
            return pageCount
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return resources.getString(R.string.pref_header_interface_calendar)

                1 -> return resources.getString(R.string.pref_header_widget_location)
                2 -> return resources.getString(R.string.pref_header_location_athan)

                else -> return resources.getString(R.string.pref_header_location_athan)
            }
        }
    }
}
