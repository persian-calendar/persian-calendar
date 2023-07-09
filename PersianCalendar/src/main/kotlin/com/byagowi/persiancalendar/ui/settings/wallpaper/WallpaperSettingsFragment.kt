package com.byagowi.persiancalendar.ui.settings.wallpaper

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.byagowi.persiancalendar.DEFAULT_WALLPAPER_DARK
import com.byagowi.persiancalendar.PREF_WALLPAPER_DARK
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.settings.build
import com.byagowi.persiancalendar.ui.settings.section
import com.byagowi.persiancalendar.ui.settings.switch
import com.byagowi.persiancalendar.ui.settings.title

class WallpaperSettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val activity = activity ?: return
        preferenceScreen = preferenceManager.createPreferenceScreen(activity).build {
            section(R.string.empty) {
                switch(PREF_WALLPAPER_DARK, DEFAULT_WALLPAPER_DARK) {
                    title(R.string.theme_dark)
                }
            }
        }
    }
}
