package com.byagowi.persiancalendar.view.reminder.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.databinding.FragmentReminderBinding;
import com.byagowi.persiancalendar.databinding.ReminderAdapterItemBinding;
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.reminder.model.ReminderDetails;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    private ReminderAdapter mReminderAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivityDependency.getMainActivity().setTitleAndSubtitle(getString(R.string.reminder), "");

        setHasOptionsMenu(true);

        FragmentReminderBinding binding = FragmentReminderBinding.inflate(inflater, container, false);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(mainActivityDependency.getMainActivity()));
        mReminderAdapter = new ReminderAdapter();
        binding.recyclerView.setAdapter(mReminderAdapter);
        binding.recyclerView.addItemDecoration(new DividerItemDecoration(binding.recyclerView.getContext(), DividerItemDecoration.VERTICAL));

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

    void updateList(boolean isNew) {
        mReminderAdapter.refresh();
        if (isNew)
            mReminderAdapter.notifyItemInserted(mReminderAdapter.getItemCount());
        else
            mReminderAdapter.notifyDataSetChanged();
    }

    class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

        ReminderAdapter() {
            refresh();
        }

        private List<ReminderDetails> remindersList = new ArrayList<>();

        private void refresh() {
            Utils.updateStoredPreference(mainActivityDependency.getMainActivity());
            remindersList = Utils.getReminderDetails();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //ReminderAdapterItemBinding binding = ReminderAdapterItemBinding.inflate(
            //        LayoutInflater.from(parent.getContext()), parent, false);
            //return new ViewHolder(binding);
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reminder_adapter_item,parent,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            //holder.bind(position);
            ReminderDetails reminderDetails = remindersList.get(position);
            holder.name.setText(reminderDetails.name);
            holder.period.setText(String.format("%s %s %s",
                                    mainActivityDependency.getMainActivity().getResources().getString(R.string.reminderPeriod),
                                    Utils.formatNumber(reminderDetails.quantity),
                                    reminderDetails.unit));

            holder.content.setOnClickListener(v -> EditReminderDialog.newInstance(remindersList.get(position).id).show(getChildFragmentManager(),
                    EditReminderDialog.class.getName()));

        }

        @Override
        public int getItemCount() {
            return remindersList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            //private ReminderAdapterItemBinding mBinding;
            private int mPosition;
            TextView name, period;
            ImageView delete;
            LinearLayout content;

            public ViewHolder(@NonNull View view) {
                super(view);
                //mBinding = binding;
                //mBinding.getRoot().setOnClickListener(v -> {
                //    EditReminderDialog.newInstance(remindersList.get(mPosition).id).show(getChildFragmentManager(),
                //            EditReminderDialog.class.getName());
                //});
                //mBinding.delete.setOnClickListener(v -> {
                //    List<ReminderDetails> reminders = new ArrayList<>(Utils.getReminderDetails());
                //    if (reminders.remove(remindersList.get(mPosition)))
                //        Utils.storeReminders(mainActivityDependency.getMainActivity(), reminders);
                //    refresh();
                //    notifyItemRemoved(mPosition);
                //});

                content = view.findViewById(R.id.content);
                name = view.findViewById(R.id.name);
                period = view.findViewById(R.id.period);
                delete = view.findViewById(R.id.delete);
                delete.setOnClickListener(v -> {
                    List<ReminderDetails> reminders = new ArrayList<>(Utils.getReminderDetails());
                    if (reminders.remove(remindersList.get(mPosition)))
                        Utils.storeReminders(mainActivityDependency.getMainActivity(), reminders);
                    refresh();
                    notifyItemRemoved(mPosition);
                });
            }

            //public void bind(int position) {
                //mPosition = position;
                //ReminderDetails reminderDetails = remindersList.get(position);
                //mBinding.name.setText(reminderDetails.name);
                //mBinding.period.setText(
                //        String.format("%s %s %s",
                //                mainActivityDependency.getMainActivity().getResources().getString(R.string.reminderPeriod),
                //                Utils.formatNumber(reminderDetails.quantity),
                //                reminderDetails.unit));
            //}
        }
    }
}
