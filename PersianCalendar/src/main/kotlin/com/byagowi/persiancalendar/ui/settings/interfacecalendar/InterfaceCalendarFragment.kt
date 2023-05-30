package com.byagowi.persiancalendar.ui.settings.interfacecalendar

import android.Manifest
import android.animation.ValueAnimator
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.DEFAULT_ISLAMIC_OFFSET
import com.byagowi.persiancalendar.DEFAULT_THEME_GRADIENT
import com.byagowi.persiancalendar.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.PREF_ASTRONOMICAL_FEATURES
import com.byagowi.persiancalendar.PREF_EASTERN_GREGORIAN_ARABIC_MONTHS
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
import com.byagowi.persiancalendar.databinding.ColorGradientToggleBinding
import com.byagowi.persiancalendar.entities.Theme
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.weekDays
import com.byagowi.persiancalendar.ui.settings.SettingsScreen
import com.byagowi.persiancalendar.ui.settings.build
import com.byagowi.persiancalendar.ui.settings.clickable
import com.byagowi.persiancalendar.ui.settings.interfacecalendar.calendarsorder.showCalendarPreferenceDialog
import com.byagowi.persiancalendar.ui.settings.multiSelect
import com.byagowi.persiancalendar.ui.settings.section
import com.byagowi.persiancalendar.ui.settings.singleSelect
import com.byagowi.persiancalendar.ui.settings.summary
import com.byagowi.persiancalendar.ui.settings.switch
import com.byagowi.persiancalendar.ui.settings.title
import com.byagowi.persiancalendar.ui.utils.askForCalendarPermission
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.isIslamicOffsetExpired
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class InterfaceCalendarFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val activity = activity ?: return
        val destination = arguments?.getString(SettingsScreen.PREF_DESTINATION)
        if (destination == PREF_HOLIDAY_TYPES) {
            showHolidaysTypesDialog(activity)
            arguments?.remove(SettingsScreen.PREF_DESTINATION)
        }

        preferenceScreen = preferenceManager.createPreferenceScreen(activity).build {
            section(R.string.pref_interface) {
                clickable(onClick = { showLanguagePreferenceDialog(activity) }) {
                    if (destination == PREF_APP_LANGUAGE) title = "Language"
                    else title(R.string.language)
                    summary = language.nativeName
                }
                switch(PREF_EASTERN_GREGORIAN_ARABIC_MONTHS, false) {
                    if (language.isArabic) {
                        title = "السنة الميلادية بالاسماء الشرقية"
                        summary = "كانون الثاني، شباط، آذار، …"
                    } else isVisible = false
                }
                themeSelect()
                // TODO: To be integrated into the language selection dialog one day
                switch(PREF_LOCAL_DIGITS, true) {
                    title(R.string.native_digits)
                    summary(R.string.enable_native_digits)
                    if (!language.canHaveLocalDigits) isVisible = false
                }
            }
            section(R.string.calendar) {
                // Mark the rest of options as advanced
                initialExpandedChildrenCount = 5
                clickable(onClick = { showHolidaysTypesDialog(activity) }) {
                    title(R.string.events)
                    summary(R.string.events_summary)
                }
                switch(PREF_SHOW_DEVICE_CALENDAR_EVENTS, false) {
                    title(R.string.show_device_calendar_events)
                    summary(R.string.show_device_calendar_events_summary)
                    this.setOnPreferenceChangeListener { _, _ ->
                        isChecked = if (ActivityCompat.checkSelfPermission(
                                activity, Manifest.permission.READ_CALENDAR
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            activity.askForCalendarPermission()
                            false
                        } else {
                            !isChecked
                        }
                        false
                    }
                }
                clickable(onClick = {
                    showCalendarPreferenceDialog(activity, onEmpty = {
                        // Easter egg when empty result is rejected
                        val view = view?.rootView ?: return@showCalendarPreferenceDialog
                        ValueAnimator.ofFloat(0f, 360f).also {
                            it.duration = 3000L
                            it.interpolator = AccelerateDecelerateInterpolator()
                            it.addUpdateListener { value ->
                                view.rotation = value.animatedValue as? Float ?: 0f
                            }
                        }.start()
                    })
                }) {
                    title(R.string.calendars_priority)
                    summary(R.string.calendars_priority_summary)
                }
                switch(PREF_ASTRONOMICAL_FEATURES, false) {
                    title(R.string.astronomy)
                    summary(R.string.astronomical_info_summary)
                }
                switch(PREF_SHOW_WEEK_OF_YEAR_NUMBER, false) {
                    title(R.string.week_of_year)
                    summary(R.string.week_of_year_summary)
                }
                run { // reset Islamic offset if is already expired
                    val appPrefs = context.appPrefs
                    if (PREF_ISLAMIC_OFFSET in appPrefs && appPrefs.isIslamicOffsetExpired)
                        appPrefs.edit { putString(PREF_ISLAMIC_OFFSET, DEFAULT_ISLAMIC_OFFSET) }
                }
                singleSelect(
                    PREF_ISLAMIC_OFFSET,
                    // One is formatted with locale's numerals and the other used for keys isn't
                    (-2..2).map { formatNumber(it.toString()) }, (-2..2).map { it.toString() },
                    DEFAULT_ISLAMIC_OFFSET, R.string.islamic_offset,
                    R.string.islamic_offset_summary
                ) { title(R.string.islamic_offset) }
                val weekDaysValues = (0..6).map { it.toString() }
                singleSelect(
                    PREF_WEEK_START, weekDays, weekDaysValues, language.defaultWeekStart,
                    R.string.week_start_summary
                ) { title(R.string.week_start) }
                multiSelect(
                    PREF_WEEK_ENDS, weekDays, weekDaysValues, language.defaultWeekEnds,
                    R.string.week_ends_summary
                ) {
                    title(R.string.week_ends)
                    summary(R.string.week_ends_summary)
                }
            }
        }
    }

    private fun PreferenceCategory.themeSelect() {
        val entries = enumValues<Theme>().map { getString(it.title) }
        val entryValues = enumValues<Theme>().map { it.key }
        var preference: Preference? = null
        clickable(
            onClick = {
                val currentValue = entryValues.indexOf(
                    context.appPrefs.getString(PREF_THEME, null) ?: Theme.SYSTEM_DEFAULT.key
                )
                val dialog = MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.select_skin)
                    .setNegativeButton(R.string.cancel, null)
                    .setSingleChoiceItems(entries.toTypedArray(), currentValue) { dialog, which ->
                        context.appPrefs.edit { putString(PREF_THEME, entryValues[which]) }
                        preference?.summary = entries[which]
                        dialog.dismiss()
                    }
                    .show()

                val activity = activity ?: return@clickable
                if (!Theme.supportsGradient(activity)) return@clickable
                val buttonBinding = ColorGradientToggleBinding.inflate(activity.layoutInflater)
                (dialog.getButton(DialogInterface.BUTTON_NEGATIVE)?.parent as? ViewGroup)
                    ?.addView(buttonBinding.root, 0)
                if (activity.appPrefs.getBoolean(PREF_THEME_GRADIENT, DEFAULT_THEME_GRADIENT))
                    buttonBinding.root.check(R.id.color_gradient)
                buttonBinding.root.addOnButtonCheckedListener { _, _, isChecked ->
                    activity.appPrefs.edit { putBoolean(PREF_THEME_GRADIENT, isChecked) }
                }
            }
        ) {
            preference = this
            summary = entries[entryValues.indexOf(
                context.appPrefs.getString(key, null) ?: Theme.SYSTEM_DEFAULT.key
            )]
            title(R.string.select_skin)
        }
    }

    override fun onCreateRecyclerView(
        inflater: LayoutInflater, parent: ViewGroup, savedInstanceState: Bundle?
    ): RecyclerView =
        SettingsScreen.insetsFix(super.onCreateRecyclerView(inflater, parent, savedInstanceState))
}
