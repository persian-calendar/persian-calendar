//package com.byagowi.persiancalendar.ui.reminder;
//
//import android.content.res.Resources;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//
//import com.byagowi.persiancalendar.R;
//import com.byagowi.persiancalendar.databinding.FragmentReminderBinding;
//import com.byagowi.persiancalendar.databinding.ReminderAdapterItemBinding;
//import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency;
//import com.byagowi.persiancalendar.entities.Reminder;
//import com.byagowi.persiancalendar.ui.MainActivity;
//import com.byagowi.persiancalendar.utils.ReminderUtils;
//import com.byagowi.persiancalendar.utils.Utils;
//import com.google.android.material.snackbar.Snackbar;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import javax.inject.Inject;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.lifecycle.ViewModelProviders;
//import androidx.recyclerview.widget.DividerItemDecoration;
//import androidx.recyclerview.widget.ItemTouchHelper;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//import dagger.android.support.DaggerFragment;
//
///**
// * @author MEHDI DIMYADI
// * MEHDIMYADI
// */
//public class ReminderFragment extends DaggerFragment {
//
//    @Inject
//    MainActivityDependency mainActivityDependency;
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
//                             @Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        MainActivity mainActivity = mainActivityDependency.getMainActivity();
//        mainActivity.setTitleAndSubtitle(getString(R.string.reminder), "");
//
//        setHasOptionsMenu(true);
//
//        FragmentReminderBinding binding = FragmentReminderBinding.inflate(inflater, container, false);
//        binding.recyclerView.setLayoutManager(new LinearLayoutManager(mainActivity));
//        ItemsAdapter reminderAdapter = new ItemsAdapter();
//        binding.recyclerView.setAdapter(reminderAdapter);
//        binding.recyclerView.addItemDecoration(new DividerItemDecoration(mainActivity,
//                DividerItemDecoration.VERTICAL));
//        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
//            @Override
//            public boolean onMove(@NonNull RecyclerView recyclerView,
//                                  @NonNull RecyclerView.ViewHolder viewHolder,
//                                  @NonNull RecyclerView.ViewHolder target) {
//                return false;
//            }
//
//            @Override
//            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
//                ((ItemsAdapter.ViewHolder) viewHolder).remove();
//            }
//        }).attachToRecyclerView(binding.recyclerView);
//
//        ReminderModel viewModel = ViewModelProviders.of(mainActivity).get(ReminderModel.class);
//        viewModel.updateHandler.observe(this, isNew -> {
//            reminderAdapter.refresh();
//            if (isNew)
//                reminderAdapter.notifyItemInserted(reminderAdapter.getItemCount());
//            else
//                reminderAdapter.notifyDataSetChanged();
//        });
//
//        return binding.getRoot();
//    }
//
//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//        menu.clear();
//        inflater.inflate(R.menu.reminder_menu, menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_add:
//                EditReminderDialog.newInstance(-1).show(getChildFragmentManager(),
//                        EditReminderDialog.class.getName());
//                break;
//            default:
//                break;
//        }
//        return true;
//    }
//
//    class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ViewHolder> {
//
//        private List<Reminder> remindersList = new ArrayList<>();
//
//        ItemsAdapter() {
//            refresh();
//        }
//
//        private void refresh() {
//            Utils.initUtils(mainActivityDependency.getMainActivity());
//            remindersList = Utils.getReminderDetails();
//        }
//
//        @NonNull
//        @Override
//        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            ReminderAdapterItemBinding binding = ReminderAdapterItemBinding.inflate(
//                    LayoutInflater.from(parent.getContext()), parent, false);
//
//            return new ViewHolder(binding);
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//            holder.bind(position);
//        }
//
//        @Override
//        public int getItemCount() {
//            return remindersList.size();
//        }
//
//        class ViewHolder extends RecyclerView.ViewHolder {
//            private ReminderAdapterItemBinding mBinding;
//            private int mId;
//
//            public ViewHolder(@NonNull ReminderAdapterItemBinding binding) {
//                super(binding.getRoot());
//                mBinding = binding;
//                mBinding.getRoot().setOnClickListener(
//                        v -> EditReminderDialog.newInstance(mId).show(getChildFragmentManager(),
//                                EditReminderDialog.class.getName()));
//                mBinding.delete.setOnClickListener(v -> remove());
//            }
//
//            public void bind(int position) {
//                Reminder reminder = remindersList.get(position);
//                mId = reminder.id;
//                mBinding.name.setText(reminder.name);
//                mBinding.info.setText(reminder.info.replaceAll("\n", " "));
//
//                MainActivity mainActivity = mainActivityDependency.getMainActivity();
//                Resources resources = mainActivity.getResources();
//                mBinding.period.setText(String.format("%s | %s",
//                        String.format(resources.getString(R.string.reminder_summary),
//                                Utils.formatNumber(reminder.quantity),
//                                getString(ReminderUtils.unitToStringId(reminder.unit))),
//                        String.format(resources.getString(R.string.reminded),
//                                Utils.formatNumber(ReminderUtils.getReminderCount(mainActivity, reminder.id))))
//                );
//            }
//
//            public void remove() {
//                List<Reminder> reminders = new ArrayList<>(Utils.getReminderDetails());
//                Reminder reminder = Utils.getReminderById(mId);
//                if (reminder != null && reminders.remove(reminder)) {
//                    Utils.storeReminders(mainActivityDependency.getMainActivity(), reminders);
//                    Utils.createAndShowSnackbar(itemView, String.format(getString(R.string.item_removed),
//                            reminder.name), Snackbar.LENGTH_SHORT);
//                }
//                ReminderUtils.turnOff(mainActivityDependency.getMainActivity(), mId);
//                refresh();
//                notifyDataSetChanged();
//            }
//        }
//    }
//}
