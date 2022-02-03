package com.byagowi.persiancalendar.ui.preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.LOG_TAG
import com.byagowi.persiancalendar.PREF_HAS_EVER_VISITED
import com.byagowi.persiancalendar.PREF_NEW_INTERFACE
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.Variants.debugLog
import com.byagowi.persiancalendar.databinding.FragmentSettingsBinding
import com.byagowi.persiancalendar.databinding.NumericBinding
import com.byagowi.persiancalendar.global.enableNewInterface
import com.byagowi.persiancalendar.service.AlarmWorker
import com.byagowi.persiancalendar.ui.preferences.interfacecalendar.InterfaceCalendarFragment
import com.byagowi.persiancalendar.ui.preferences.locationathan.LocationAthanFragment
import com.byagowi.persiancalendar.ui.preferences.widgetnotification.WidgetNotificationFragment
import com.byagowi.persiancalendar.ui.utils.canEnableNewInterface
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.hideToolbarBottomShadow
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.setupMenuNavigation
import com.byagowi.persiancalendar.ui.utils.shareTextFile
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.logException
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayoutMediator
import java.util.concurrent.TimeUnit

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */

class PreferencesFragment : Fragment(R.layout.fragment_settings) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSettingsBinding.bind(view)
        binding.appBar.root.hideToolbarBottomShadow()
        binding.appBar.toolbar.let { toolbar ->
            toolbar.setTitle(R.string.settings)
            toolbar.setupMenuNavigation()
            setupDevelopmentMenu(toolbar, binding, layoutInflater)
        }

        val args by navArgs<PreferencesFragmentArgs>()
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = tabs.size
            override fun createFragment(position: Int) = tabs[position].first().also {
                if (position == args.tab && args.preferenceKey.isNotEmpty()) {
                    it.arguments = bundleOf(PREF_DESTINATION to args.preferenceKey)
                }
            }
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, i ->
            tab.text = tabs[i].second.joinToString(getString(R.string.spaced_and)) { getString(it) }
        }.attach()
        binding.viewPager.currentItem = args.tab
        view.post { view.context.appPrefs.edit { putBoolean(PREF_HAS_EVER_VISITED, true) } }
    }

    private val tabs = listOf(
        ::InterfaceCalendarFragment to listOf(R.string.pref_interface, R.string.calendar),
        ::WidgetNotificationFragment to listOf(R.string.pref_notification, R.string.pref_widget),
        ::LocationAthanFragment to listOf(R.string.location, R.string.athan)
    )

    companion object {
        const val PREF_DESTINATION = "DESTINATION"
        const val INTERFACE_CALENDAR_TAB = 0
        const val WIDGET_NOTIFICATION_TAB = 1
        const val LOCATION_ATHAN_TAB = 2
    }

    // Development only functionalities
    private fun setupDevelopmentMenu(
        toolbar: MaterialToolbar, binding: FragmentSettingsBinding, inflater: LayoutInflater
    ) {
        if (!BuildConfig.DEVELOPMENT) return
        val activity = activity ?: return
        if (canEnableNewInterface) {
            toolbar.menu.add(R.string.enable_new_interface).also {
                it.isCheckable = true
                it.isChecked = enableNewInterface
            }.onClick {
                binding.root.context.appPrefs.edit {
                    putBoolean(PREF_NEW_INTERFACE, !enableNewInterface)
                }
            }
        }
        toolbar.menu.add("Static vs generated icons").onClick { showIconsDemoDialog(activity) }
        toolbar.menu.add("Clear preferences store and exit").onClick {
            activity.appPrefs.edit { clear() }
            activity.finish()
        }
        toolbar.menu.add("Schedule an alarm").onClick {
            val numericBinding = NumericBinding.inflate(inflater)
            numericBinding.edit.setText("5")
            AlertDialog.Builder(activity)
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
        fun viewCommandResult(command: String) = AlertDialog.Builder(
            activity, R.style.AppFullscreenAlertDialog
        ).also { dialog ->
            val result = Runtime.getRuntime().exec(command).inputStream.bufferedReader().readText()
            val button = ImageButton(activity).also { button ->
                button.setImageDrawable(activity.getCompatDrawable(R.drawable.ic_baseline_share))
                button.setOnClickListener {
                    activity.shareTextFile(result, "log.txt", "text/plain")
                }
            }
            dialog.setCustomTitle(
                LinearLayout(activity).also {
                    it.layoutDirection = View.LAYOUT_DIRECTION_LTR
                    it.addView(button)
                }
            )
            dialog.setView(
                ScrollView(context).also { scrollView ->
                    scrollView.addView(TextView(context).also {
                        it.text = result
                        it.textDirection = View.TEXT_DIRECTION_LTR
                    })
                    // Scroll to bottom, https://stackoverflow.com/a/3080483
                    scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
                }
            )
        }.show().let {}
        toolbar.menu.addSubMenu("Log Viewer").also {
            it.add("Filtered").onClick {
                viewCommandResult("logcat -v raw -t 500 *:S $LOG_TAG:V AndroidRuntime:E")
            }
            it.add("Unfiltered").onClick { viewCommandResult("logcat -v raw -t 500") }
        }
        toolbar.menu.addSubMenu("Log").also {
            it.add("Log 'Hello'").onClick { debugLog("Hello!") }
            it.add("Handled Crash").onClick { logException(Exception("Logged Crash!")) }
            it.add("Crash!").onClick { error("Unhandled Crash!") }
        }
    }
}
