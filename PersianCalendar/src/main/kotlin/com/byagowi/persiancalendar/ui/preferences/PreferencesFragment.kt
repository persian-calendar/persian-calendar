package com.byagowi.persiancalendar.ui.preferences

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.Variants.enableDevelopmentFeatures
import com.byagowi.persiancalendar.databinding.FragmentSettingsBinding
import com.byagowi.persiancalendar.databinding.NumericBinding
import com.byagowi.persiancalendar.service.AlarmWorker
import com.byagowi.persiancalendar.ui.preferences.interfacecalendar.InterfaceCalendarFragment
import com.byagowi.persiancalendar.ui.preferences.locationathan.LocationAthanFragment
import com.byagowi.persiancalendar.ui.preferences.widgetnotification.WidgetNotificationFragment
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.setupMenuNavigation
import com.byagowi.persiancalendar.utils.appPrefs
import com.google.android.material.tabs.TabLayoutMediator
import java.util.concurrent.TimeUnit

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
            toolbar.setupMenuNavigation()
            if (enableDevelopmentFeatures) {
                toolbar.menu.add("Static vs generated icons")
                    .onClick { showIconsDemoDialog(binding.root.context) }
                toolbar.menu.add("Clear preferences store")
                    .onClick { toolbar.context.appPrefs.edit { clear() }; activity?.recreate() }
                toolbar.menu.add("Schedule an alarm").onClick {
                    val numericBinding = NumericBinding.inflate(inflater)
                    numericBinding.edit.setText("5")
                    AlertDialog.Builder(context)
                        .setTitle("Enter seconds to schedule alarm")
                        .setView(numericBinding.root)
                        .setPositiveButton(R.string.accept) { _, _ ->
                            val seconds = numericBinding.edit.text.toString().toLongOrNull() ?: 0L
                            val alarmWorker = OneTimeWorkRequest.Builder(AlarmWorker::class.java)
                                .setInitialDelay(
                                    TimeUnit.SECONDS.toMillis(seconds), TimeUnit.MILLISECONDS
                                )
                                .build()
                            WorkManager.getInstance(binding.root.context)
                                .beginUniqueWork(
                                    "TestAlarm", ExistingWorkPolicy.REPLACE, alarmWorker
                                )
                                .enqueue()
                            Toast.makeText(context, "Alarm in ${seconds}s", Toast.LENGTH_SHORT)
                                .show()
                        }
                        .show()
                }
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
