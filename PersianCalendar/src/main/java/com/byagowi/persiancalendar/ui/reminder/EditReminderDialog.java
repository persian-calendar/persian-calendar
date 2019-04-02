//package com.byagowi.persiancalendar.ui.reminder;
//
//import android.app.DatePickerDialog;
//import android.app.Dialog;
//import android.app.TimePickerDialog;
//import android.os.Bundle;
//import android.text.Editable;
//import android.text.TextUtils;
//import android.text.TextWatcher;
//import android.view.LayoutInflater;
//import android.widget.ArrayAdapter;
//
//import com.byagowi.persiancalendar.Constants;
//import com.byagowi.persiancalendar.R;
//import com.byagowi.persiancalendar.databinding.EditReminderDialogBinding;
//import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency;
//import com.byagowi.persiancalendar.entities.Reminder;
//import com.byagowi.persiancalendar.entities.StringWithValueItem;
//import com.byagowi.persiancalendar.praytimes.Clock;
//import com.byagowi.persiancalendar.ui.MainActivity;
//import com.byagowi.persiancalendar.utils.ReminderUtils;
//import com.byagowi.persiancalendar.utils.Utils;
//
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.List;
//import java.util.Random;
//
//import javax.inject.Inject;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AlertDialog;
//import androidx.lifecycle.ViewModelProviders;
//import dagger.android.support.DaggerAppCompatDialogFragment;
//
///**
// * @author MEHDI DIMYADI
// * MEHDIMYADI
// */
//public class EditReminderDialog extends DaggerAppCompatDialogFragment {
//    @Inject
//    MainActivityDependency mainActivityDependency;
//
//    static EditReminderDialog newInstance(int id) {
//        Bundle args = new Bundle();
//        args.putInt(Constants.REMINDER_ID, id);
//
//        EditReminderDialog fragment = new EditReminderDialog();
//        fragment.setArguments(args);
//        return fragment;
//    }
//
////    @Nullable
////    private static Reminder findReminderById(long id) {
////        List<Reminder> reminderDetails = Utils.getReminderDetails();
////        int length = reminderDetails.size();
////        for (int i = 0; i < length; ++i) {
////            if (id == reminderDetails.get(i).id) {
////                return reminderDetails.get(i);
////            }
////        }
////        return null;
////    }
//
//    private int getIdFromArguments() {
//        int result = -1;
//        Bundle args = getArguments();
//        if (args != null) result = args.getInt(Constants.REMINDER_ID, -1);
//        return result;
//    }
//
//    @NonNull
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        MainActivity mainActivity = mainActivityDependency.getMainActivity();
//        EditReminderDialogBinding binding = EditReminderDialogBinding.inflate(
//                LayoutInflater.from(mainActivity), null, false);
//
//        int tmpId = getIdFromArguments();
//        if (tmpId == -1) tmpId = new Random().nextInt() % Constants.REMINDERS_MAX_ID;
//        final int id = tmpId;
//
//        List<StringWithValueItem> quantities = new ArrayList<>();
//        for (int i = 1; i <= 60; ++i) {
//            quantities.add(new StringWithValueItem(i, Utils.formatNumber(i)));
//        }
//        binding.quantity.setAdapter(new ArrayAdapter<>(mainActivity,
//                android.R.layout.simple_spinner_dropdown_item, quantities));
//        binding.unit.setAdapter(new ArrayAdapter<>(mainActivity,
//                android.R.layout.simple_spinner_dropdown_item,
//                ReminderUtils.timeUnitsStringArray(mainActivity)));
//
//        Calendar calendar = Calendar.getInstance();
//        Reminder reminder = findReminderById(id);
//        if (reminder != null) {
//            binding.name.setText(reminder.name);
//            binding.info.setText(reminder.info);
//            binding.quantity.setSelection(reminder.quantity - 1);
//            binding.unit.setSelection(ReminderUtils.unitToOrdination(reminder.unit));
//            calendar.setTimeInMillis(reminder.startTime);
//        }
//
//        binding.time.setText(Utils.getFormattedClock(new Clock(calendar.get(Calendar.HOUR_OF_DAY),
//                calendar.get(Calendar.MINUTE)), false));
//        binding.date.setText(Utils.formatDate(Utils.getDateFromJdnOfCalendar(Utils.getMainCalendar(),
//                Utils.calendarToCivilDate(calendar).toJdn())));
//        binding.time.setOnClickListener(v ->
//                new TimePickerDialog(mainActivity, (view1, hourOfDay, minute) -> {
//                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
//                    calendar.set(Calendar.MINUTE, minute);
//                    binding.time.setText(Utils.getFormattedClock(new Clock(calendar.get(Calendar.HOUR_OF_DAY),
//                            calendar.get(Calendar.MINUTE)), false));
//                },
//                        calendar.get(Calendar.HOUR_OF_DAY),
//                        calendar.get(Calendar.MINUTE),
//                        true).show());
//        binding.date.setOnClickListener(v ->
//                new DatePickerDialog(mainActivity, (view12, year, monthOfYear, dayOfMonth) -> {
//                    calendar.set(Calendar.YEAR, year);
//                    calendar.set(Calendar.MONTH, monthOfYear);
//                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
//                    binding.date.setText(Utils.formatDate(Utils.getDateFromJdnOfCalendar(Utils.getMainCalendar(),
//                            Utils.calendarToCivilDate(calendar).toJdn())));
//                },
//                        calendar.get(Calendar.YEAR),
//                        calendar.get(Calendar.MONTH),
//                        calendar.get(Calendar.DAY_OF_MONTH)).show());
//
//        binding.name.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                binding.nameLayout.setError(getString(TextUtils.isEmpty(s) ?
//                        R.string.please_enter_a_name : R.string.empty));
//                AlertDialog dialog = (AlertDialog) getDialog();
//                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!TextUtils.isEmpty(s));
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//            }
//        });
//
//        return new AlertDialog.Builder(mainActivity)
//                .setView(binding.getRoot())
//                .setPositiveButton(R.string.accept, (d, which) -> {
//                    Editable nameEditable = binding.name.getText();
//                    String name = nameEditable == null ? "" : nameEditable.toString();
//                    Editable infoEditable = binding.info.getText();
//                    String info = infoEditable == null ? "" : infoEditable.toString();
//                    Reminder newReminder = new Reminder(
//                            id,
//                            name,
//                            info,
//                            ReminderUtils.ordinationToUnit(binding.unit.getSelectedItemPosition()),
//                            ((StringWithValueItem) binding.quantity.getSelectedItem()).getValue(),
//                            calendar.getTimeInMillis()
//                    );
//
//                    ArrayList<Reminder> reminders = new ArrayList<>(Utils.getReminderDetails());
//                    int length = reminders.size();
//                    int index = -1;
//                    for (int i = 0; i < length; ++i) {
//                        if (id == reminders.get(i).id) {
//                            index = i;
//                            break;
//                        }
//                    }
//
//                    boolean isNew = index == -1;
//                    if (isNew)
//                        reminders.add(newReminder);
//                    else
//                        reminders.set(index, newReminder);
//
//                    Utils.storeReminders(mainActivity, reminders);
//
//                    ViewModelProviders.of(mainActivity).get(ReminderModel.class).update(isNew);
//                })
//                .create();
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        if (getIdFromArguments() == -1) {
//            AlertDialog dialog = (AlertDialog) getDialog();
//            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
//        }
//    }
//}
