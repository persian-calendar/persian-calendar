package com.byagowi.persiancalendar.view.activity;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;

import com.byagowi.persiancalendar.R;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.setTheme(this);
        Utils.changeAppLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.widget_preference);

        getSupportFragmentManager().beginTransaction().add(
                R.id.preference_fragment_holder,
                PreferenceFragment.newInstance(), "TAG").commit();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int appwidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
            findViewById(R.id.add_widget_button).setOnClickListener((v) -> {
                setResult(RESULT_OK, new Intent()
                        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appwidgetId));
                finish();
            });
        }
    }
}
