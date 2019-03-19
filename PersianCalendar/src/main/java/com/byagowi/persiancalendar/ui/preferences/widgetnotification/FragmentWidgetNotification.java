package com.byagowi.persiancalendar.ui.preferences.widgetnotification;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.byagowi.persiancalendar.R;

import java.util.Locale;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import static com.byagowi.persiancalendar.Constants.DEFAULT_SELECTED_WIDGET_TEXT_COLOR;
import static com.byagowi.persiancalendar.Constants.PREF_SELECTED_WIDGET_TEXT_COLOR;

// Don't use dagger in this class
public class FragmentWidgetNotification extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences_widget_notification);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        FragmentActivity activity = getActivity();
        if (activity == null) return false;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);

        if (preference.getKey().equals(PREF_SELECTED_WIDGET_TEXT_COLOR)) {

            ColorPickerView colorPickerView = new ColorPickerView(activity);
            colorPickerView.setColorsToPick(
                    new int[]{0xFFFFFFFF, 0xFFE65100, 0xFF00796b, 0xFFFEF200, 0xFF202020});
            colorPickerView.setPickedColor(Color.parseColor(
                    sharedPreferences.getString(
                            PREF_SELECTED_WIDGET_TEXT_COLOR,
                            DEFAULT_SELECTED_WIDGET_TEXT_COLOR)));

            int padding = (int) (activity.getResources().getDisplayMetrics().density * 10);
            colorPickerView.setPadding(padding, padding, padding, padding);

            new AlertDialog.Builder(activity)
                    .setTitle(R.string.widget_text_color)
                    .setView(colorPickerView)
                    .setPositiveButton(R.string.accept, (dialogInterface, i) -> {
                        try {
                            SharedPreferences.Editor edit = sharedPreferences.edit();
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
