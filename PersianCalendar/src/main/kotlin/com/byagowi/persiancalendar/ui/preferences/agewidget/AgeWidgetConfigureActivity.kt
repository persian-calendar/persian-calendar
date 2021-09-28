package com.byagowi.persiancalendar.ui.preferences.agewidget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.commit
import com.byagowi.persiancalendar.PREF_SELECTED_DATE_AGE_WIDGET
import com.byagowi.persiancalendar.PREF_TITLE_AGE_WIDGET
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.ActivityAgeWidgetConfigureBinding
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Theme
import com.byagowi.persiancalendar.ui.utils.makeWallpaperTransparency
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.createAgeRemoteViews
import com.byagowi.persiancalendar.utils.getJdnOrNull
import com.byagowi.persiancalendar.utils.getWidgetSize
import com.byagowi.persiancalendar.utils.putJdn
import com.byagowi.persiancalendar.utils.update

class AgeWidgetConfigureActivity : AppCompatActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private fun confirm() {
        // Make sure we pass back the original appWidgetId
        setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
        update(this, false)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.apply(this)
        applyAppLanguage(this)
        super.onCreate(savedInstanceState)
        window?.makeWallpaperTransparency()

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        val binding = ActivityAgeWidgetConfigureBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val widgetManager = AppWidgetManager.getInstance(this)
        val (width, height) = widgetManager.getWidgetSize(this, appWidgetId)
        fun updateWidget() {
            binding.preview.addView(
                createAgeRemoteViews(this, width, height, appWidgetId)
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

        // Put today's jdn if it wasn't set by the dialog, maybe a day counter is meant
        if (appPrefs.getJdnOrNull(PREF_SELECTED_DATE_AGE_WIDGET + appWidgetId) == null)
            appPrefs.edit {
                putJdn(PREF_SELECTED_DATE_AGE_WIDGET + appWidgetId, Jdn.today)
            }

        supportFragmentManager.commit {
            add(
                R.id.preference_fragment_holder, AgeWidgetConfigureFragment::class.java,
                bundleOf(AppWidgetManager.EXTRA_APPWIDGET_ID to appWidgetId), "TAG"
            )
        }

        val title = appPrefs.getString(PREF_TITLE_AGE_WIDGET + appWidgetId, "")
        binding.editWidgetTitle.setText(title)
        binding.addWidgetButton.setOnClickListener {
            confirm()
        }
        binding.editWidgetTitle.doOnTextChanged { text, _, _, _ ->
            appPrefs.edit {
                putString(PREF_TITLE_AGE_WIDGET + appWidgetId, text.toString())
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applyAppLanguage(this)
    }
}
