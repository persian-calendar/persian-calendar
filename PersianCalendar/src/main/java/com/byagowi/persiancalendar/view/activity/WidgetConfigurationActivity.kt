package com.byagowi.persiancalendar.view.activity

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.util.UIUtils
import com.byagowi.persiancalendar.util.UpdateUtils
import com.byagowi.persiancalendar.util.Utils

class WidgetConfigurationActivity : AppCompatActivity() {

  // don't remove public ever
  class PreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
      AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
      addPreferencesFromResource(R.xml.widget_preferences)
    }

    companion object {
      internal fun newInstance(): PreferenceFragment = PreferenceFragment()
    }
  }

  private fun finishAndSuccess() {
    val extras = intent.extras
    if (extras != null) {
      val appwidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID)
      setResult(Activity.RESULT_OK, Intent()
          .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appwidgetId))
    }
    Utils.updateStoredPreference(this)
    UpdateUtils.update(this, false)
    finish()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    UIUtils.setTheme(this)
    Utils.changeAppLanguage(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.widget_preference_layout)

    supportFragmentManager.beginTransaction().add(
        R.id.preference_fragment_holder,
        PreferenceFragment.newInstance(), "TAG").commit()

    findViewById<View>(R.id.add_widget_button).setOnClickListener { finishAndSuccess() }
  }
}
