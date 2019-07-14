package com.byagowi.persiancalendar.ui.preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentSettingsBinding
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency
import com.byagowi.persiancalendar.ui.preferences.interfacecalendar.FragmentInterfaceCalendar
import com.byagowi.persiancalendar.ui.preferences.locationathan.FragmentLocationAthan
import com.byagowi.persiancalendar.ui.preferences.widgetnotification.FragmentWidgetNotification
import com.google.android.material.tabs.TabLayoutMediator
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
            viewPager.adapter = ViewPagerAdapter(3)
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.setText(when (position) {
                    0 -> R.string.pref_header_interface_calendar
                    1 -> R.string.pref_header_widget_location
                    2 -> R.string.pref_header_location_athan
                    else -> R.string.pref_header_location_athan
                })
            }.attach()
            return root
        }
    }

    internal inner class ViewPagerAdapter(private var pageCount: Int) : FragmentStateAdapter(this) {
        override fun createFragment(position: Int): Fragment {
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

        override fun getItemCount(): Int = pageCount

        fun getPageTitle(position: Int): CharSequence? {
            return when (position) {
                0 -> resources.getString(R.string.pref_header_interface_calendar)
                1 -> resources.getString(R.string.pref_header_widget_location)
                2 -> resources.getString(R.string.pref_header_location_athan)
                else -> resources.getString(R.string.pref_header_location_athan)
            }
        }
    }
}
