package com.byagowi.persiancalendar.view.reminder.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.databinding.ReminderAdapterItemBinding;
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.reminder.model.ReminderDetails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import dagger.android.support.DaggerFragment;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class ReminderFragment extends DaggerFragment {

    @Inject
    MainActivityDependency mainActivityDependency;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivityDependency.getMainActivity().setTitleAndSubtitle(
                getString(R.string.reminder), "");

        setHasOptionsMenu(true);

        RecyclerView recyclerView = new RecyclerView(mainActivityDependency.getMainActivity());
        recyclerView.setAdapter(new ReminderAdapter());

        return recyclerView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.reminder_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                EditReminderDialog.newInstance(-1).show(getChildFragmentManager(),
                        EditReminderDialog.class.getName());
                break;
            default:
                break;
        }
        return true;
    }

    class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

        ReminderAdapter() {
            refresh();
        }

        List<ReminderDetails> allEvents = Collections.emptyList();

        private void refresh() {
            Utils.updateStoredPreference(mainActivityDependency.getMainActivity());
            allEvents = Utils.getRemiderDetails();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ReminderAdapterItemBinding binding = ReminderAdapterItemBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);

            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(position);
        }

        @Override
        public int getItemCount() {
            return allEvents.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private ReminderAdapterItemBinding mBinding;

            public ViewHolder(@NonNull ReminderAdapterItemBinding binding) {
                super(binding.getRoot());
                mBinding = binding;
            }

            public void bind(int position) {
                ReminderDetails reminderDetails = allEvents.get(position);
                mBinding.name.setText(reminderDetails.name);
                mBinding.period.setText(
                        String.format("%s %s %s",
                                mainActivityDependency.getMainActivity().getResources().getString(R.string.reminderPeriod),
                                Utils.formatNumber(reminderDetails.quantity),
                                reminderDetails.unit));

                mBinding.delete.setOnClickListener(v -> {
                    List<ReminderDetails> reminders = new ArrayList<>(Utils.getRemiderDetails());
                    if (reminders.remove(reminderDetails))
                        Utils.storeReminders(mainActivityDependency.getMainActivity(), reminders);
                    notifyDataSetChanged();
                });
            }
        }
    }
}
