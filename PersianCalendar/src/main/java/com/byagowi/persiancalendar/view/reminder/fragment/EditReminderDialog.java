package com.byagowi.persiancalendar.view.reminder.fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.databinding.FragmentEditReminderBinding;
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.activity.MainActivity;
import com.byagowi.persiancalendar.view.reminder.constants.Constants;
import com.byagowi.persiancalendar.view.reminder.model.ReminderDetails;
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

    static EditReminderDialog newInstance(int id) {
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
    private static ReminderDetails findReminderById(long id) {
        List<ReminderDetails> reminderDetails = Utils.getRemiderDetails();
        int length = reminderDetails.size();
        for (int i = 0; i < length; ++i) {
            if (id == reminderDetails.get(i).id) {
                return reminderDetails.get(i);
            }
        }
        return null;
    }

    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity mainActivity = mainActivityDependency.getMainActivity();
        FragmentEditReminderBinding binding = FragmentEditReminderBinding.inflate(LayoutInflater.from(mainActivity), null, false);

        long tmpId = new Random().nextInt();
        Bundle args = getArguments();
        if (args != null) tmpId = args.getLong(Constants.REMINDER_ID, tmpId);
        final long id = tmpId;

        String[] quantity = new String[Constants.MAX_QUANTITY - 1];
        for (int i = 1; i < Constants.MAX_QUANTITY; i++)
            quantity[i - 1] = String.valueOf(i);
        binding.quantity.setAdapter(new ArrayAdapter<>(mainActivity,
                R.layout.reminder_item, quantity));
        binding.unit.setAdapter(ArrayAdapter.createFromResource(mainActivity,
                R.array.period_units, R.layout.reminder_item));

        Calendar calendar = Calendar.getInstance();
        ReminderDetails reminder = findReminderById(id);
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

        return new AlertDialog.Builder(mainActivity)
                .setView(binding.getRoot())
                .setPositiveButton(R.string.accept, (dialog, which) -> {
                    String name = binding.name.getText().toString();
                    String info = binding.info.getText().toString();
                    if (name.isEmpty()) {
                        // Replace with inline editor's material validator
                        com.byagowi.persiancalendar.util.Utils.createAndShowSnackbar(getView(),
                                "خالی است!", Snackbar.LENGTH_SHORT);
                    } else {
                        ReminderDetails newReminder = new ReminderDetails(
                                id,
                                name,
                                info,
                                ordinationToUnit(binding.unit.getSelectedItemPosition()),
                                binding.quantity.getSelectedItemPosition(),
                                System.currentTimeMillis() // Somehow should be get from binding.btnDate.getText() and binding.time.getText()
                        );

                        ArrayList<ReminderDetails> reminders = new ArrayList<>(Utils.getRemiderDetails());
                        int length = reminders.size();
                        int index = -1;
                        for (int i = 0; i < length; ++i) {
                            if (id == reminders.get(i).id) {
                                index = i;
                                break;
                            }
                        }

                        if (index == -1)
                            reminders.add(newReminder);
                        else
                            reminders.set(index, newReminder);

                        Utils.storeReminders(mainActivity, reminders);
                    }
                })
                .create();
    }

}
