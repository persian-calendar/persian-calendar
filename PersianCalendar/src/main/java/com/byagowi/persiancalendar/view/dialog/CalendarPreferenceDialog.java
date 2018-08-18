package com.byagowi.persiancalendar.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.adapter.RecyclerListAdapter;
import com.byagowi.persiancalendar.adapter.SimpleItemTouchHelperCallback;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CalendarPreferenceDialog extends AppCompatDialogFragment {
    private ItemTouchHelper mItemTouchHelper;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        RecyclerListAdapter adapter = new RecyclerListAdapter(getActivity(), this);

        RecyclerView recyclerView = new RecyclerView(getContext());
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
        builder.setView(recyclerView);
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
        });
        builder.setPositiveButton(R.string.select, (dialogInterface, i) -> {
            Context context = getContext();
            if (context == null) return;

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(Constants.PREF_MAIN_CALENDAR_KEY, adapter.getOrdering().get(0));
            edit.apply();
        });

        return builder.create();
    }

    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }
}
