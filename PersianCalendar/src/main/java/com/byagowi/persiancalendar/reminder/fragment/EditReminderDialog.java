package com.byagowi.persiancalendar.reminder.fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.databinding.EditReminderDialogBinding;
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency;
import com.byagowi.persiancalendar.di.dependencies.ReminderFragmentDependency;
import com.byagowi.persiancalendar.entity.FormattedIntEntity;
import com.byagowi.persiancalendar.reminder.constants.Constants;
import com.byagowi.persiancalendar.reminder.model.Reminder;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.activity.MainActivity;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import dagger.android.support.DaggerAppCompatDialogFragment;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class EditReminderDialog extends DaggerAppCompatDialogFragment {
    @Inject
    MainActivityDependency mainActivityDependency;
    @Inject
    ReminderFragmentDependency reminderFragmentDependency;

    static EditReminderDialog newInstance(long id) {
        Bundle args = new Bundle();
        args.putLong(Constants.REMINDER_ID, id);

        EditReminderDialog fragment = new EditReminderDialog();
        fragment.setArguments(args);
        return fragment;
    }

    private static int unitToOrdination(TimeUnit unit) {
        switch (unit) {
            case MINUTES:
                return 0;
            case HOURS:
                return 1;
            default:
            case DAYS:
                return 2;
        }
    }

    private static TimeUnit ordinationToUnit(int ordination) {
        switch (ordination) {
            case 0:
                return TimeUnit.MINUTES;
            case 1:
                return TimeUnit.HOURS;
            default:
            case 2:
                return TimeUnit.DAYS;
        }
    }

    @Nullable
    private static Reminder findReminderById(long id) {
        List<Reminder> reminderDetails = Utils.getReminderDetails();
        int length = reminderDetails.size();
        for (int i = 0; i < length; ++i) {
            if (id == reminderDetails.get(i).id) {
                return reminderDetails.get(i);
            }
        }
        return null;
    }

    private long getIdFromArguments() {
        long result = -1;
        Bundle args = getArguments();
        if (args != null) result = args.getLong(Constants.REMINDER_ID, -1);
        return result;
    }

    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity mainActivity = mainActivityDependency.getMainActivity();
        EditReminderDialogBinding binding = EditReminderDialogBinding.inflate(
                LayoutInflater.from(mainActivity), null, false);

        long tmpId = getIdFromArguments();
        if (tmpId == -1) tmpId = new Random().nextInt();
        final long id = tmpId;

        List<FormattedIntEntity> quantities = new ArrayList<>();
        for (int i = 1; i <= Constants.MAX_QUANTITY; ++i) {
            quantities.add(new FormattedIntEntity(i, Utils.formatNumber(i)));
        }
        binding.quantity.setAdapter(new ArrayAdapter<>(mainActivity,
                R.layout.reminder_item, quantities));
        binding.unit.setAdapter(ArrayAdapter.createFromResource(mainActivity,
                R.array.period_units, R.layout.reminder_item));

        Calendar calendar = Calendar.getInstance();
        Reminder reminder = findReminderById(id);
        if (reminder != null) {
            binding.name.setText(reminder.name);
            binding.info.setText(reminder.info);
            binding.quantity.setSelection(reminder.quantity);
            binding.unit.setSelection(unitToOrdination(reminder.unit));
            calendar.setTimeInMillis(reminder.startTime);
        }

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        binding.time.setText(timeFormat.format(calendar.getTime()));
        binding.btnDate.setText(dateFormat.format(calendar.getTime()));
        binding.time.setOnClickListener(v -> {
            TimePickerDialog time_dialog = new TimePickerDialog(
                    getActivity(), (view1, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                binding.time.setText(timeFormat.format(calendar
                        .getTime()));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar
                    .get(Calendar.MINUTE), true);
            time_dialog.show();
        });
        binding.btnDate.setOnClickListener(v -> {
            DatePickerDialog date_dialog = new DatePickerDialog(
                    mainActivity, (view12, year, monthOfYear, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                binding.btnDate.setText(dateFormat.format(calendar.getTime()));
            }, calendar.get(Calendar.YEAR), calendar
                    .get(Calendar.MONTH), calendar
                    .get(Calendar.DAY_OF_MONTH));
            date_dialog.show();
        });

        binding.name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.nameLayout.setError(TextUtils.isEmpty(s) ? "خالی است" : "");
                AlertDialog dialog = (AlertDialog) getDialog();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!TextUtils.isEmpty(s));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return new AlertDialog.Builder(mainActivity)
                .setView(binding.getRoot())
                .setPositiveButton(R.string.accept, (d, which) -> {
                    Editable nameEditable = binding.name.getText();
                    String name = nameEditable == null ? "" : nameEditable.toString();
                    Editable infoEditable = binding.info.getText();
                    String info = infoEditable == null ? "" : infoEditable.toString();
                    Reminder newReminder = new Reminder(
                            id,
                            name,
                            info,
                            ordinationToUnit(binding.unit.getSelectedItemPosition()),
                            binding.quantity.getSelectedItemPosition(),
                            System.currentTimeMillis() // Somehow should be get from binding.btnDate.getText() and binding.time.getText()
                    );

                    ArrayList<Reminder> reminders = new ArrayList<>(Utils.getReminderDetails());
                    int length = reminders.size();
                    int index = -1;
                    for (int i = 0; i < length; ++i) {
                        if (id == reminders.get(i).id) {
                            index = i;
                            break;
                        }
                    }

                    boolean isNew = index == -1;
                    if (isNew)
                        reminders.add(newReminder);
                    else
                        reminders.set(index, newReminder);

                    Utils.storeReminders(mainActivity, reminders);

                    reminderFragmentDependency.getReminderFragment().updateList(isNew);
                })
                .create();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getIdFromArguments() == -1) {
            AlertDialog dialog = (AlertDialog) getDialog();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        }
    }
}
