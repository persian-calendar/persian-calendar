package com.byagowi.persiancalendar.ui.settings.interfacecalendar

import android.Manifest
import android.animation.ValueAnimator
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import com.byagowi.persiancalendar.DEFAULT_EASTERN_GREGORIAN_ARABIC_MONTHS
import com.byagowi.persiancalendar.DEFAULT_ENGLISH_GREGORIAN_PERSIAN_MONTHS
import com.byagowi.persiancalendar.DEFAULT_ISLAMIC_OFFSET
import com.byagowi.persiancalendar.DEFAULT_THEME_GRADIENT
import com.byagowi.persiancalendar.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.PREF_ASTRONOMICAL_FEATURES
import com.byagowi.persiancalendar.PREF_EASTERN_GREGORIAN_ARABIC_MONTHS
import com.byagowi.persiancalendar.PREF_ENGLISH_GREGORIAN_PERSIAN_MONTHS
import com.byagowi.persiancalendar.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.PREF_ISLAMIC_OFFSET
import com.byagowi.persiancalendar.PREF_LOCAL_DIGITS
import com.byagowi.persiancalendar.PREF_SHOW_DEVICE_CALENDAR_EVENTS
import com.byagowi.persiancalendar.PREF_SHOW_WEEK_OF_YEAR_NUMBER
import com.byagowi.persiancalendar.PREF_THEME
import com.byagowi.persiancalendar.PREF_THEME_GRADIENT
import com.byagowi.persiancalendar.PREF_WEEK_ENDS
import com.byagowi.persiancalendar.PREF_WEEK_START
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.ColorGradientSwitchBinding
import com.byagowi.persiancalendar.entities.Theme
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.weekDays
import com.byagowi.persiancalendar.ui.settings.SettingsClickable
import com.byagowi.persiancalendar.ui.settings.SettingsMultiSelect
import com.byagowi.persiancalendar.ui.settings.SettingsScreen
import com.byagowi.persiancalendar.ui.settings.SettingsScreenDirections
import com.byagowi.persiancalendar.ui.settings.SettingsSection
import com.byagowi.persiancalendar.ui.settings.SettingsSingleSelect
import com.byagowi.persiancalendar.ui.settings.SettingsSwitch
import com.byagowi.persiancalendar.ui.settings.interfacecalendar.calendarsorder.showCalendarPreferenceDialog
import com.byagowi.persiancalendar.ui.utils.askForCalendarPermission
import com.byagowi.persiancalendar.ui.utils.navigateSafe
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.isIslamicOffsetExpired
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.random.Random

class InterfaceCalendarFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val destination = arguments?.getString(SettingsScreen.PREF_DESTINATION)
        val root = ComposeView(inflater.context)
        val activity = activity ?: return root
        if (destination == PREF_HOLIDAY_TYPES) {
            showHolidaysTypesDialog(activity)
            arguments?.remove(SettingsScreen.PREF_DESTINATION)
        }
        run { // reset Islamic offset if is already expired
            val appPrefs = activity.appPrefs
            if (PREF_ISLAMIC_OFFSET in appPrefs && appPrefs.isIslamicOffsetExpired)
                appPrefs.edit { putString(PREF_ISLAMIC_OFFSET, DEFAULT_ISLAMIC_OFFSET) }
        }
        root.setContent {
            Mdc3Theme {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    SettingsSection(stringResource(R.string.pref_interface))
                    ThemeSelect(activity)
                    SettingsClickable(
                        title = if (destination == PREF_APP_LANGUAGE) "Language"
                        else stringResource(R.string.language),
                        summary = language.nativeName,
                    ) { showLanguagePreferenceDialog(activity) }
                    if (language.isArabic) {
                        SettingsSwitch(
                            PREF_EASTERN_GREGORIAN_ARABIC_MONTHS,
                            DEFAULT_EASTERN_GREGORIAN_ARABIC_MONTHS,
                            "السنة الميلادية بالاسماء الشرقية",
                            "كانون الثاني، شباط، آذار، …"
                        )
                    }
                    if (language.isPersian) {
                        SettingsSwitch(
                            PREF_ENGLISH_GREGORIAN_PERSIAN_MONTHS,
                            DEFAULT_ENGLISH_GREGORIAN_PERSIAN_MONTHS,
                            "ماه‌های میلادی با نام انگلیسی",
                            "جون، جولای، آگوست، …"
                        )
                    }
                    // TODO: To be integrated into the language selection dialog one day
                    if (language.canHaveLocalDigits) {
                        SettingsSwitch(
                            PREF_LOCAL_DIGITS,
                            true,
                            stringResource(R.string.native_digits),
                            stringResource(R.string.enable_native_digits)
                        )
                    }

                    Divider()
                    SettingsSection(stringResource(R.string.calendar))
                    SettingsClickable(
                        stringResource(R.string.events), stringResource(R.string.events_summary)
                    ) { showHolidaysTypesDialog(activity) }
                    SettingsSwitch(
                        PREF_SHOW_DEVICE_CALENDAR_EVENTS, false,
                        stringResource(R.string.show_device_calendar_events),
                        stringResource(R.string.show_device_calendar_events_summary),
                        onBeforeToggle = {
                            if (it && ActivityCompat.checkSelfPermission(
                                    activity, Manifest.permission.READ_CALENDAR
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                activity.askForCalendarPermission()
                                false
                            } else it
                        },
                        watchChanges = true,
                    )
                    SettingsClickable(
                        stringResource(R.string.calendars_priority),
                        stringResource(R.string.calendars_priority_summary)
                    ) {
                        showCalendarPreferenceDialog(activity, onEmpty = {
                            // Easter egg when empty result is rejected
                            val view = view?.rootView ?: return@showCalendarPreferenceDialog
                            val animator = ValueAnimator.ofFloat(0f, 1f)
                            animator.duration = 3000L
                            animator.interpolator = AccelerateDecelerateInterpolator()
                            animator.addUpdateListener {
                                view.rotation = it.animatedFraction * 360f
                            }
                            if (Random.nextBoolean()) animator.start() else animator.reverse()
                        })
                    }
                    SettingsSwitch(
                        PREF_ASTRONOMICAL_FEATURES, false,
                        stringResource(R.string.astronomy),
                        stringResource(R.string.astronomical_info_summary)
                    )
                    SettingsSwitch(
                        PREF_SHOW_WEEK_OF_YEAR_NUMBER, false,
                        stringResource(R.string.week_number),
                        stringResource(R.string.week_number_summary)
                    )
                    SettingsSingleSelect(
                        PREF_ISLAMIC_OFFSET,
                        // One is formatted with locale's numerals and the other used for keys isn't
                        (-2..2).map { formatNumber(it.toString()) },
                        (-2..2).map { it.toString() },
                        DEFAULT_ISLAMIC_OFFSET,
                        R.string.islamic_offset,
                        stringResource(R.string.islamic_offset),
                        R.string.islamic_offset_summary,
                    )
                    val weekDaysValues = (0..6).map { it.toString() }
                    SettingsSingleSelect(
                        key = PREF_WEEK_START,
                        entries = weekDays,
                        entryValues = weekDaysValues,
                        defaultValue = language.defaultWeekStart,
                        dialogTitleResId = R.string.week_start_summary,
                        title = stringResource(R.string.week_start),
                        summaryResId = R.string.week_start_summary,
                    )
                    SettingsMultiSelect(
                        key = PREF_WEEK_ENDS,
                        entries = weekDays,
                        entryValues = weekDaysValues,
                        defaultValue = language.defaultWeekEnds,
                        dialogTitleResId = R.string.week_ends_summary,
                        title = stringResource(R.string.week_ends),
                        summary = stringResource(R.string.week_ends),
                    )

                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
                }
            }
        }

        activity.appPrefs.registerOnSharedPreferenceChangeListener(this)
        return root
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == PREF_APP_LANGUAGE) {
            val navController = activity?.findNavController(R.id.navHostFragment)
            if (navController?.currentDestination?.id == R.id.settings) navController.navigateSafe(
                SettingsScreenDirections.navigateToSelf()
            )
        }
    }
}

@Composable
private fun ThemeSelect(activity: FragmentActivity) {
    val entries = Theme.entries.map { activity.getString(it.title) }
    val entryValues = Theme.entries.map { it.key }
    val context = LocalContext.current
    var themeDisplayName by remember {
        mutableStateOf(
            entries[entryValues.indexOf(
                context.appPrefs.getString(PREF_THEME, null) ?: Theme.SYSTEM_DEFAULT.key
            )]
        )
    }
    SettingsClickable(
        title = stringResource(R.string.select_skin), summary = themeDisplayName
    ) clickable@{
        val currentValue = entryValues.indexOf(
            context.appPrefs.getString(PREF_THEME, null) ?: Theme.SYSTEM_DEFAULT.key
        )
        val dialog = MaterialAlertDialogBuilder(context).setTitle(R.string.select_skin)
            .setNegativeButton(R.string.cancel, null)
            .setSingleChoiceItems(entries.toTypedArray(), currentValue) { dialog, which ->
                context.appPrefs.edit { putString(PREF_THEME, entryValues[which]) }
                themeDisplayName = entries[which]
                dialog.dismiss()
            }.show()

        if (!Theme.supportsGradient(activity)) return@clickable
        val binding = ColorGradientSwitchBinding.inflate(activity.layoutInflater)
        (dialog.getButton(DialogInterface.BUTTON_NEGATIVE)?.parent as? ViewGroup)?.addView(
            binding.root,
            0
        )
        if (activity.appPrefs.getBoolean(
                PREF_THEME_GRADIENT,
                DEFAULT_THEME_GRADIENT
            )
        ) binding.button.isChecked = true
        binding.label.setOnClickListener {
            binding.button.isChecked = !binding.button.isChecked
        }
        binding.button.setOnCheckedChangeListener { _, isChecked ->
            activity.appPrefs.edit { putBoolean(PREF_THEME_GRADIENT, isChecked) }
        }
    }
}
