package com.byagowi.persiancalendar.ui.settings.widgetnotification

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.WidgetPreferenceLayoutBinding
import com.byagowi.persiancalendar.entities.Theme
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.makeWallpaperTransparency
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.createSampleRemoteViews
import com.byagowi.persiancalendar.utils.update

class WidgetConfigurationActivity : AppCompatActivity() {

    private fun finishAndSuccess() {
        intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID).also { i ->
            setResult(
                AppCompatActivity.RESULT_OK,
                Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, i)
            )
        }
        updateStoredPreference(this)
        update(this, false)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAndSuccess()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.apply(this)
        applyAppLanguage(this)
        super.onCreate(savedInstanceState)
        window?.makeWallpaperTransparency()

        val binding = WidgetPreferenceLayoutBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }

        val width = 200.dp.toInt()
        val height = 60.dp.toInt()
        fun updateWidget() {
            binding.preview.addView(
                createSampleRemoteViews(this, width, height)
                    .apply(applicationContext, binding.preview)
            )
        }
        updateWidget()

        val appPrefs = appPrefs
        appPrefs.registerOnSharedPreferenceChangeListener { _, _ ->
            // TODO: Investigate why sometimes gets out of sync
            binding.preview.post {
                binding.preview.removeAllViews()
                updateWidget()
            }
        }

        supportFragmentManager.commit {
            replace(
                R.id.preference_fragment_holder, WidgetNotificationFragment::class.java,
                bundleOf(WidgetNotificationFragment.IS_WIDGETS_CONFIGURATION to true)
            )
        }
        binding.addWidgetButton.setOnClickListener { finishAndSuccess() }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applyAppLanguage(this)
    }
}
