package com.byagowi.persiancalendar.ui.preferences.widgetnotification;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.databinding.WidgetPreferenceLayoutBinding;
import com.byagowi.persiancalendar.utils.UpdateUtils;
import com.byagowi.persiancalendar.utils.Utils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

public class WidgetConfigurationActivity extends AppCompatActivity {
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
        // Don't replace below with appDependency.getSharedPreferences() ever
        // as the injection won't happen at the right time
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(Utils.getThemeFromName(Utils.getThemeFromPreference(prefs)));

        Utils.applyAppLanguage(this);
        super.onCreate(savedInstanceState);
        WidgetPreferenceLayoutBinding binding =
                DataBindingUtil.setContentView(this, R.layout.widget_preference_layout);

        getSupportFragmentManager().beginTransaction().add(
                R.id.preference_fragment_holder,
                new FragmentWidgetNotification(), "TAG").commit();

        binding.addWidgetButton.setOnClickListener(v -> finishAndSuccess());
    }
}
