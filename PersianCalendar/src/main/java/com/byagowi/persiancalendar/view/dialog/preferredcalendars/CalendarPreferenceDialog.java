package com.byagowi.persiancalendar.view.dialog.preferredcalendars;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.entity.CalendarTypeEntity;
import com.byagowi.persiancalendar.util.Utils;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import calendar.CalendarType;

public class CalendarPreferenceDialog extends AppCompatDialogFragment {
    private ItemTouchHelper mItemTouchHelper;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getContext();
        if (context == null) return null;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        List<String> values = new ArrayList<>();
        List<String> titles = new ArrayList<>();
        List<Boolean> enabled = new ArrayList<>();

        Utils.updateStoredPreference(context);
        List<CalendarType> enabledCalendarTypes = Utils.getEnabledCalendarTypes();
        List<CalendarTypeEntity> orderedCalendarTypes = Utils.getOrderedCalendarEntities(context);
        for (CalendarTypeEntity entity : orderedCalendarTypes) {
            values.add(entity.getType().toString());
            titles.add(entity.toString());
            enabled.add(enabledCalendarTypes.contains(entity.getType()));
        }

        RecyclerListAdapter adapter = new RecyclerListAdapter(this,
                titles, values, enabled);

        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
        builder.setView(recyclerView);
        builder.setTitle(R.string.calendars_priority);
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
        });
        builder.setPositiveButton(R.string.accept, (dialogInterface, i) -> {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor edit = prefs.edit();
            List<String> ordering = adapter.getResult();
            if (ordering.size() != 0) {
                edit.putString(Constants.PREF_MAIN_CALENDAR_KEY, ordering.get(0));
                edit.putString(Constants.PREF_OTHER_CALENDARS_KEY, TextUtils.join(",",
                        ordering.subList(1, ordering.size())));
            }
            edit.apply();
        });

        return builder.create();
    }

    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }
}
