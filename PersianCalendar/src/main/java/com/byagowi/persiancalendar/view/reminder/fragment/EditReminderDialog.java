package com.byagowi.persiancalendar.view.reminder.fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.databinding.FragmentEditReminderBinding;
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency;
import com.byagowi.persiancalendar.view.activity.MainActivity;
import com.byagowi.persiancalendar.view.reminder.constants.Constants;
import com.byagowi.persiancalendar.view.reminder.database.DatabaseManager;
import com.byagowi.persiancalendar.view.reminder.model.ReminderDetails;
import com.byagowi.persiancalendar.view.reminder.model.ReminderUnit;
import com.byagowi.persiancalendar.view.reminder.utils.Utils;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import dagger.android.support.DaggerAppCompatDialogFragment;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class EditReminderDialog extends DaggerAppCompatDialogFragment {
    private static String BUNDLE_KEY = "id";

    @Inject
    MainActivityDependency mainActivityDependency;

    private SimpleDateFormat time_format;
    private SimpleDateFormat date_format;
    private Calendar calendar;
    private ReminderDetails event;

    private DatabaseManager databaseManager;

    private long id;

    public static EditReminderDialog newInstance(int id) {
        Bundle args = new Bundle();
        args.putLong(BUNDLE_KEY, id);

        EditReminderDialog fragment = new EditReminderDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity mainActivity = mainActivityDependency.getMainActivity();
        FragmentEditReminderBinding binding = FragmentEditReminderBinding.inflate(LayoutInflater.from(mainActivity), null, false);

        Bundle args = getArguments();
        if (args != null) {
            id = args.getLong(Constants.EVENT_ID, -1);
        }

        databaseManager = new DatabaseManager(getActivity());
        event = databaseManager.getEvent(id);
        time_format = new SimpleDateFormat("HH:mm", Locale.getDefault());
        date_format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        binding.spinPerQ.setAdapter(new ArrayAdapter<>(mainActivity,
                R.layout.reminder_item, Utils.getQuantity()));
        binding.spinPerU.setAdapter(ArrayAdapter.createFromResource(mainActivity,
                R.array.period_units, R.layout.reminder_item));

        calendar = Calendar.getInstance();
        if (event != null) {
            binding.etName.setText(event.getReminderName());
            binding.etInfo.setText(event.getReminderInfo());
            binding.spinPerQ.setSelection(event.getReminderPeriod().getQuantity() - 1);
            binding.spinPerU.setSelection(Utils.getUnit(mainActivity, event
                    .getReminderPeriod().getUnit()));
            calendar.setTimeInMillis(event.getStartTime().getTime());
        }
        if (savedInstanceState != null) {
            calendar.setTimeInMillis(savedInstanceState.getLong("calendar"));
        }

        binding.btnTime.setText(time_format.format(calendar.getTime()));
        binding.btnDate.setText(date_format.format(calendar.getTime()));
        binding.btnTime.setOnClickListener(v -> {
            TimePickerDialog time_dialog = new TimePickerDialog(
                    getActivity(), (view1, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                binding.btnTime.setText(time_format.format(calendar
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
                binding.btnDate.setText(date_format.format(calendar
                        .getTime()));
            }, calendar.get(Calendar.YEAR), calendar
                    .get(Calendar.MONTH), calendar
                    .get(Calendar.DAY_OF_MONTH));
            date_dialog.show();
        });

        return new AlertDialog.Builder(mainActivity)
                .setView(binding.getRoot())
                .setPositiveButton(R.string.accept, (dialog, which) -> {
                    String name = binding.etName.getText().toString();
                    String info = binding.etInfo.getText().toString();
                    if (name.isEmpty()) {
                        // Replace with inline editor's material validator
                        com.byagowi.persiancalendar.util.Utils.createAndShowSnackbar(getView(),
                                "خالی است!", Snackbar.LENGTH_SHORT);
                    } else {
                        if (event == null)
                            event = new ReminderDetails();
                        event.setReminderName(name);
                        event.setReminderInfo(info);
                        event.setReminderPeriod(new ReminderUnit(Integer.parseInt(binding.spinPerQ
                                .getSelectedItem().toString()), binding.spinPerU
                                .getSelectedItem().toString()));
                        event.setStartTime(Utils.stringToDate(binding.btnDate.getText()
                                + " " + binding.btnTime.getText()));
                        databaseManager.saveEvent(event);
                        // FIXME: Notify the parent about the change
                    }
                })
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putLong("calendar", calendar.getTimeInMillis());
    }

}
