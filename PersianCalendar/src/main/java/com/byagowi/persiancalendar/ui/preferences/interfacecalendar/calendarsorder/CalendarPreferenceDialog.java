package com.byagowi.persiancalendar.ui.preferences.interfacecalendar.calendarsorder;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.di.dependencies.AppDependency;
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency;
import com.byagowi.persiancalendar.entities.CalendarTypeItem;
import com.byagowi.persiancalendar.utils.CalendarType;
import com.byagowi.persiancalendar.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dagger.android.support.DaggerAppCompatDialogFragment;

public class CalendarPreferenceDialog extends DaggerAppCompatDialogFragment {
    @Inject
    AppDependency appDependency;
    @Inject
    MainActivityDependency mainActivityDependency;
    private ItemTouchHelper mItemTouchHelper;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(mainActivityDependency.getMainActivity());

        List<String> values = new ArrayList<>();
        List<String> titles = new ArrayList<>();
        List<Boolean> enabled = new ArrayList<>();

        Utils.updateStoredPreference(mainActivityDependency.getMainActivity());
        List<CalendarType> enabledCalendarTypes = Utils.getEnabledCalendarTypes();
        List<CalendarTypeItem> orderedCalendarTypes =
                Utils.getOrderedCalendarEntities(mainActivityDependency.getMainActivity());
        for (CalendarTypeItem entity : orderedCalendarTypes) {
            values.add(entity.getType().toString());
            titles.add(entity.toString());
            enabled.add(enabledCalendarTypes.contains(entity.getType()));
        }

        RecyclerView recyclerView = new RecyclerView(mainActivityDependency.getMainActivity());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(mainActivityDependency.getMainActivity()));
        RecyclerListAdapter adapter = new RecyclerListAdapter(this,
                mainActivityDependency, titles, values, enabled);
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
        builder.setView(recyclerView);
        builder.setTitle(R.string.calendars_priority);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.accept, (dialogInterface, i) -> {
            SharedPreferences.Editor edit = appDependency.getSharedPreferences().edit();
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

    void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }
}
