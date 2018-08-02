package com.byagowi.persiancalendar.view.activity;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.util.UpdateUtils;
import com.byagowi.persiancalendar.util.Utils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

public class WidgetConfigurationActivity extends AppCompatActivity {

    // don't remove public ever
    public static class PreferenceFragment extends PreferenceFragmentCompat {
        static PreferenceFragment newInstance() {
            return new PreferenceFragment();
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.widget_preferences);
        }
    }

    protected void finishAndSuccess() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int appwidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
            setResult(RESULT_OK, new Intent()
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appwidgetId));
        }
        Utils.updateStoredPreference(this);
        UpdateUtils.update(this, false);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.setTheme(this);
        Utils.changeAppLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.widget_preference_layout);

        getSupportFragmentManager().beginTransaction().add(
                R.id.preference_fragment_holder,
                PreferenceFragment.newInstance(), "TAG").commit();

        findViewById(R.id.add_widget_button).setOnClickListener((v) -> {
            finishAndSuccess();
        });
    }
}
