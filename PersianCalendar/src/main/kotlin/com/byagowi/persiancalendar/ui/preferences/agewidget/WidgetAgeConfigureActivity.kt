package com.byagowi.persiancalendar.ui.preferences.agewidget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import com.byagowi.persiancalendar.PREF_SELECTED_DATE_AGE_WIDGET
import com.byagowi.persiancalendar.PREF_TITLE_AGE_WIDGET
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.ActivityAgeWidgetConfigureBinding
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.utils.Theme
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.getJdnOrNull
import com.byagowi.persiancalendar.utils.putJdn
import com.byagowi.persiancalendar.utils.update

class AgeWidgetConfigureActivity : AppCompatActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private fun confirm(title: String) {
        appPrefs.edit {
            // Put today's jdn if it wasn't set by the dialog, maybe a day counter is meant
            if (appPrefs.getJdnOrNull(PREF_SELECTED_DATE_AGE_WIDGET + appWidgetId) == null)
                putJdn(PREF_SELECTED_DATE_AGE_WIDGET + appWidgetId, Jdn.today)
            putString(PREF_TITLE_AGE_WIDGET + appWidgetId, title)
        }
        // Make sure we pass back the original appWidgetId
        setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
        update(this, false)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.apply(this)
        applyAppLanguage(this)
        super.onCreate(savedInstanceState)

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

        supportFragmentManager.commit {
            add(
                R.id.preference_fragment_holder, WidgetAgeConfigureFragment::class.java,
                bundleOf(AppWidgetManager.EXTRA_APPWIDGET_ID to appWidgetId), "TAG"
            )
        }

        val title = appPrefs.getString(PREF_TITLE_AGE_WIDGET + appWidgetId, "")
        binding.editWidgetTitle.setText(title)
        binding.addWidgetButton.setOnClickListener {
            confirm(binding.editWidgetTitle.text.toString())
        }
    }
}
