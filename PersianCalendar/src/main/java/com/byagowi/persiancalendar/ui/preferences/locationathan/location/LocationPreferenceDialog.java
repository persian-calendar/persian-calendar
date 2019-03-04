package com.byagowi.persiancalendar.ui.preferences.locationathan.location;

import com.byagowi.persiancalendar.utils.Utils;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * persian_calendar
 * Author: hamidsafdari22@gmail.com
 * Date: 1/17/16
 */
public class LocationPreferenceDialog extends PreferenceDialogFragmentCompat {

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        RecyclerView recyclerView = new RecyclerView(builder.getContext());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new LocationAdapter(this,
                Utils.getAllCities(getContext(), true)));
        builder.setView(recyclerView);

        builder.setPositiveButton("", null);
        builder.setNegativeButton("", null);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
    }

    void selectItem(String city) {
        ((LocationPreference) getPreference()).setSelected(city);
        dismiss();
    }
}
