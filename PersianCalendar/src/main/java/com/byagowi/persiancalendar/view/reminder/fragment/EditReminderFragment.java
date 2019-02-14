package com.byagowi.persiancalendar.view.reminder.fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.view.reminder.common.Dialog;
import com.byagowi.persiancalendar.view.reminder.constants.Constants;
import com.byagowi.persiancalendar.view.reminder.database.DatabaseManager;
import com.byagowi.persiancalendar.view.reminder.model.ReminderDetails;
import com.byagowi.persiancalendar.view.reminder.model.ReminderUnit;
import com.byagowi.persiancalendar.view.reminder.utils.Utils;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class EditReminderFragment extends Fragment {

	private Dialog alert;
	private SimpleDateFormat time_format;
	private SimpleDateFormat date_format;
	private Calendar calendar;
	private ReminderDetails event;

	private EditText et_name;
	private EditText et_info;
	private Spinner sp_period_q;
	private Spinner sp_period_u;
	private Button btn_time;
	private Button btn_date;
	private DatabaseManager databaseManager;

	private long id;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_edit_reminder, container, false);

		Bundle args = getArguments();
		if(args != null){
			id = args.getLong(Constants.EVENT_ID, -1);
		}

		databaseManager = new DatabaseManager(getActivity());
		event = databaseManager.getEvent(id);
		time_format = new SimpleDateFormat("HH:mm", Locale.getDefault());
		date_format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		alert = new Dialog(getActivity());

		et_name = view.findViewById(R.id.et_name);
		et_info = view.findViewById(R.id.et_info);
		sp_period_q = view.findViewById(R.id.spin_per_q);
		sp_period_u = view.findViewById(R.id.spin_per_u);
		btn_time = view.findViewById(R.id.btn_time);
		btn_date = view.findViewById(R.id.btn_date);
		Button btn_save = view.findViewById(R.id.btn_save);

		sp_period_q.setAdapter(new ArrayAdapter<>(Objects.requireNonNull(getActivity()),
				R.layout.reminder_item, Utils.getQuantity()));
		sp_period_u.setAdapter(ArrayAdapter.createFromResource(getActivity(),
				R.array.period_units, R.layout.reminder_item));

		calendar = Calendar.getInstance();
		if (event != null) {
			et_name.setText(event.getReminderName());
			et_info.setText(event.getReminderInfo());
			sp_period_q.setSelection(event.getReminderPeriod().getQuantity() - 1);
			sp_period_u.setSelection(Utils.getUnit(getActivity(), event
					.getReminderPeriod().getUnit()));
			calendar.setTimeInMillis(event.getStartTime().getTime());
		}
		if (savedInstanceState != null) {
			calendar.setTimeInMillis(savedInstanceState.getLong("calendar"));
		}

		btn_time.setText(time_format.format(calendar.getTime()));
		btn_date.setText(date_format.format(calendar.getTime()));
		btn_time.setOnClickListener(v -> {
			TimePickerDialog time_dialog = new TimePickerDialog(
					getActivity(), (view1, hourOfDay, minute) -> {
						calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
						calendar.set(Calendar.MINUTE, minute);
						btn_time.setText(time_format.format(calendar
								.getTime()));
					}, calendar.get(Calendar.HOUR_OF_DAY), calendar
							.get(Calendar.MINUTE), true);
			time_dialog.show();
		});
		btn_date.setOnClickListener(v -> {
			DatePickerDialog date_dialog = new DatePickerDialog(
					getActivity(), (view12, year, monthOfYear, dayOfMonth) -> {
						calendar.set(Calendar.YEAR, year);
						calendar.set(Calendar.MONTH, monthOfYear);
						calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
						btn_date.setText(date_format.format(calendar
								.getTime()));
					}, calendar.get(Calendar.YEAR), calendar
							.get(Calendar.MONTH), calendar
							.get(Calendar.DAY_OF_MONTH));
			date_dialog.show();
		});
		btn_save.setOnClickListener(v -> {
			String name = et_name.getText().toString();
			String info = et_info.getText().toString();
			if (name.isEmpty()) {
				alert.showMessage();
			} else {
				if (event == null)
					event = new ReminderDetails();
				event.setReminderName(name);
				event.setReminderInfo(info);
				event.setReminderPeriod(new ReminderUnit(Integer.parseInt(sp_period_q
						.getSelectedItem().toString()), sp_period_u
						.getSelectedItem().toString()));
				event.setStartTime(Utils.stringToDate(btn_date.getText()
						+ " " + btn_time.getText()));
				databaseManager.saveEvent(event);
				ReminderFragment toReminderFragment = new ReminderFragment();
				assert this.getFragmentManager() != null;
				this.getFragmentManager().beginTransaction()
						.replace(R.id.fragment_holder_reminder, toReminderFragment)
						.addToBackStack(null)
						.commit();
				}
		});

		return view;
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
