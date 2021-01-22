package com.byagowi.persiancalendar.ui.preferences.agewidget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.byagowi.persiancalendar.PREF_TITLE_AGE_WIDGET
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.ActivityAgeWidgetConfigureBinding
import com.byagowi.persiancalendar.updateAgeWidget
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.getThemeFromName
import com.byagowi.persiancalendar.utils.getThemeFromPreference

class AgeWidgetConfigureActivity : AppCompatActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private fun confirm() {
        val context = this@AgeWidgetConfigureActivity

        // It is the responsibility of the configuration activity to update the app widget
        val appWidgetManager = AppWidgetManager.getInstance(context)
        updateAgeWidget(context, appWidgetManager, appWidgetId)

        // Make sure we pass back the original appWidgetId
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }

    override fun onCreate(icicle: Bundle?) {
        setTheme(getThemeFromName(getThemeFromPreference(this, appPrefs)))
        applyAppLanguage(this)

        super.onCreate(icicle)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        val binding = ActivityAgeWidgetConfigureBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        val intent = intent
        val extras = intent?.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val args = Bundle().apply {
            putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        supportFragmentManager.commit {
            add(
                R.id.preference_fragment_holder,
                WidgetAgeConfigureFragment::class.java,
                args,
                "TAG"
            )
        }

        binding.editWidgetTitle.visibility
        val title = appPrefs.getString(PREF_TITLE_AGE_WIDGET + appWidgetId, "")
        binding.editWidgetTitle.text = SpannableStringBuilder(title)
        binding.addWidgetButton.setOnClickListener {
            appPrefs.edit().putString(
                PREF_TITLE_AGE_WIDGET + appWidgetId,
                binding.editWidgetTitle.text.toString()
            ).apply()
            confirm()
        }
    }
}
