package com.byagowi.persiancalendar.view.preferences;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.di.dependencies.AppDependency;
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency;
import com.byagowi.persiancalendar.view.ColorPickerView;

import java.util.Locale;

import javax.inject.Inject;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import dagger.android.support.AndroidSupportInjection;

import static com.byagowi.persiancalendar.Constants.DEFAULT_SELECTED_WIDGET_TEXT_COLOR;
import static com.byagowi.persiancalendar.Constants.PREF_SELECTED_WIDGET_TEXT_COLOR;

public class FragmentWidgetNotification extends PreferenceFragmentCompat {
    @Inject
    AppDependency appDependency;

    @Inject
    MainActivityDependency mainActivityDependency;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        AndroidSupportInjection.inject(this);
        addPreferencesFromResource(R.xml.preferences_widget_notification);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference.getKey().equals(PREF_SELECTED_WIDGET_TEXT_COLOR)) {

            ColorPickerView colorPickerView = new ColorPickerView(mainActivityDependency.getMainActivity());
            colorPickerView.setColorsToPick(
                    new int[]{0xFFFFFFFF, 0xFFE65100, 0xFF00796b, 0xFFFEF200, 0xFF202020});
            colorPickerView.setPickedColor(Color.parseColor(
                    appDependency.getSharedPreferences().getString(
                            PREF_SELECTED_WIDGET_TEXT_COLOR,
                            DEFAULT_SELECTED_WIDGET_TEXT_COLOR)));

            int padding = (int) (mainActivityDependency.getMainActivity().getResources().getDisplayMetrics().density * 10);
            colorPickerView.setPadding(padding, padding, padding, padding);

            new AlertDialog.Builder(mainActivityDependency.getMainActivity())
                    .setTitle(R.string.widget_text_color)
                    .setView(colorPickerView)
                    .setPositiveButton(R.string.accept, (dialogInterface, i) -> {
                        try {
                            SharedPreferences.Editor edit =
                                    appDependency.getSharedPreferences().edit();
                            edit.putString(PREF_SELECTED_WIDGET_TEXT_COLOR,
                                    String.format(Locale.ENGLISH, "#%06X",
                                            0xFFFFFF & colorPickerView.getPickerColor()));
                            edit.apply();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).setNegativeButton(R.string.cancel, null).show();
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }
}
