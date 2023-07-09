package com.byagowi.persiancalendar.ui.settings.wallpaper

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.WallpaperSettingsBinding
import com.byagowi.persiancalendar.entities.Theme
import com.byagowi.persiancalendar.ui.utils.makeWallpaperTransparency
import com.byagowi.persiancalendar.ui.utils.transparentSystemBars
import com.byagowi.persiancalendar.utils.applyAppLanguage

class WallpaperSettingsActivity : AppCompatActivity() {

    private val onBackPressedCloseCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() = finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.apply(this)
        applyAppLanguage(this)
        super.onCreate(savedInstanceState)
        window?.makeWallpaperTransparency()
        transparentSystemBars()

        onBackPressedDispatcher.addCallback(this, onBackPressedCloseCallback)

        val binding = WallpaperSettingsBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }

        supportFragmentManager.commit {
            replace(
                R.id.preference_fragment_holder, WallpaperSettingsFragment::class.java, bundleOf()
            )
        }
        binding.addWidgetButton.setOnClickListener { finish() }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applyAppLanguage(this)
    }
}
