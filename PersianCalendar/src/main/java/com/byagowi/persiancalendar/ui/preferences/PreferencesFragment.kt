package com.byagowi.persiancalendar.ui.preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentSettingsBinding
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency
import com.byagowi.persiancalendar.ui.preferences.interfacecalendar.FragmentInterfaceCalendar
import com.byagowi.persiancalendar.ui.preferences.locationathan.FragmentLocationAthan
import com.byagowi.persiancalendar.ui.preferences.widgetnotification.FragmentWidgetNotification
import dagger.android.support.DaggerFragment
import javax.inject.Inject

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
class PreferencesFragment : DaggerFragment() {

    @Inject
    lateinit var mainActivityDependency: MainActivityDependency

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainActivityDependency.mainActivity.setTitleAndSubtitle(getString(R.string.settings), "")

        FragmentSettingsBinding.inflate(LayoutInflater.from(container?.context), container, false).apply {
            viewPager.adapter = ViewPagerAdapter(childFragmentManager, 3)
            tabLayout.setupWithViewPager(viewPager)

            return root
        }
    }

    internal inner class ViewPagerAdapter(manager: FragmentManager, private var pageCount: Int) : FragmentPagerAdapter(manager) {

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

        override fun getCount(): Int = pageCount

        override fun getPageTitle(position: Int): CharSequence? {
            return when (position) {
                0 -> resources.getString(R.string.pref_header_interface_calendar)
                1 -> resources.getString(R.string.pref_header_widget_location)
                2 -> resources.getString(R.string.pref_header_location_athan)
                else -> resources.getString(R.string.pref_header_location_athan)
            }
        }
    }
}
