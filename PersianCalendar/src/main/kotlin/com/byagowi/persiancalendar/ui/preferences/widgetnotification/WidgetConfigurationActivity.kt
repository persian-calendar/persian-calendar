package com.byagowi.persiancalendar.ui.preferences.widgetnotification

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.WidgetPreferenceLayoutBinding
import com.byagowi.persiancalendar.entities.Theme
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.update
import com.byagowi.persiancalendar.utils.updateStoredPreference

class WidgetConfigurationActivity : AppCompatActivity() {

    private fun finishAndSuccess() {
        intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID).also { i ->
            setResult(Activity.RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, i))
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

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER,
            WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER
        )
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val binding = WidgetPreferenceLayoutBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }

        supportFragmentManager.commit {
            add(
                R.id.preference_fragment_holder, WidgetNotificationFragment::class.java,
                bundleOf(WidgetNotificationFragment.IS_WIDGETS_CONFIGURATION to true), "TAG"
            )
        }
        binding.addWidgetButton.setOnClickListener { finishAndSuccess() }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applyAppLanguage(this)
    }
}
