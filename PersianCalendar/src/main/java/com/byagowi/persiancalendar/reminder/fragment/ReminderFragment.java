package com.byagowi.persiancalendar.reminder.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.databinding.FragmentReminderBinding;
import com.byagowi.persiancalendar.databinding.ReminderAdapterItemBinding;
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency;
import com.byagowi.persiancalendar.reminder.ReminderUtils;
import com.byagowi.persiancalendar.reminder.model.Reminder;
import com.byagowi.persiancalendar.reminder.viewmodel.ReminderModel;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.activity.MainActivity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
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

        MainActivity mainActivity = mainActivityDependency.getMainActivity();
        mainActivity.setTitleAndSubtitle(getString(R.string.reminder), "");

        setHasOptionsMenu(true);

        FragmentReminderBinding binding = FragmentReminderBinding.inflate(inflater, container, false);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(mainActivity));
        ReminderAdapter reminderAdapter = new ReminderAdapter();
        binding.recyclerView.setAdapter(reminderAdapter);
        binding.recyclerView.addItemDecoration(new DividerItemDecoration(binding.recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        ReminderModel viewModel = ViewModelProviders.of(mainActivity).get(ReminderModel.class);
        viewModel.updateHandler.observe(this, isNew -> {
            reminderAdapter.refresh();
            if (isNew)
                reminderAdapter.notifyItemInserted(reminderAdapter.getItemCount());
            else
                reminderAdapter.notifyDataSetChanged();
        });

        return binding.getRoot();
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

        private List<Reminder> remindersList = new ArrayList<>();

        ReminderAdapter() {
            refresh();
        }

        private void refresh() {
            Utils.initUtils(mainActivityDependency.getMainActivity());
            remindersList = Utils.getReminderDetails();
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
            return remindersList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private ReminderAdapterItemBinding mBinding;
            private int id;

            public ViewHolder(@NonNull ReminderAdapterItemBinding binding) {
                super(binding.getRoot());
                mBinding = binding;
                mBinding.getRoot().setOnClickListener(
                        v -> EditReminderDialog.newInstance(id)
                                .show(getChildFragmentManager(), EditReminderDialog.class.getName()));
                mBinding.delete.setOnClickListener(v -> {
                    List<Reminder> reminders = new ArrayList<>(Utils.getReminderDetails());
                    Reminder reminder = Utils.getReminderById(id);
                    if (reminder != null && reminders.remove(reminder))
                        Utils.storeReminders(mainActivityDependency.getMainActivity(), reminders);
                    ReminderUtils.turnOff(mainActivityDependency.getMainActivity(), id);
                    refresh();
                    notifyDataSetChanged();
                });
            }

            public void bind(int position) {
                Reminder reminder = remindersList.get(position);
                id = reminder.id;
                mBinding.name.setText(reminder.name);
                mBinding.period.setText(
                        String.format(mainActivityDependency.getMainActivity().getResources().getString(R.string.reminder_summary),
                                Utils.formatNumber(reminder.quantity),
                                getString(ReminderUtils.unitToStringId(reminder.unit))));
            }
        }
    }
}
