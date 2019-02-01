package com.byagowi.persiancalendar.view.drugalert.fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.view.drugalert.application.DrugAlertApplication;
import com.byagowi.persiancalendar.view.drugalert.common.Dialog;
import com.byagowi.persiancalendar.view.drugalert.constants.Constants;
import com.byagowi.persiancalendar.view.drugalert.model.DrugDetails;
import com.byagowi.persiancalendar.view.drugalert.model.DrugUnit;
import com.byagowi.persiancalendar.view.drugalert.utils.Utils;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class EditDrugFragment extends Fragment {

	private DrugAlertApplication app;
	private Dialog alert;
	private SimpleDateFormat time_format;
	private SimpleDateFormat date_format;
	private Calendar calendar;
	private DrugDetails event;

	private EditText et_name;
	private EditText et_info;
	private Spinner sp_period_q;
	private Spinner sp_period_u;
	private Button btn_time;
	private Button btn_date;
	long id;
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_edit_drug, container, false);

		app = (DrugAlertApplication) getActivity().getApplication();
		Bundle args = getArguments();
		if(args != null){
			id = args.getLong(Constants.EVENT_ID, -1);
		}
		event = app.getDatabaseManager().getEvent(id);
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

		sp_period_q.setAdapter(new ArrayAdapter<>(getActivity(),
				R.layout.drug_item, Utils.getQuantity()));
		sp_period_u.setAdapter(ArrayAdapter.createFromResource(getActivity(),
				R.array.period_units, R.layout.drug_item));

		calendar = Calendar.getInstance();
		if (event != null) {
			et_name.setText(event.getDrugName());
			et_info.setText(event.getDrugInfo());
			sp_period_q.setSelection(event.getDrugPeriod().getQuantity() - 1);
			sp_period_u.setSelection(Utils.getUnit(getActivity(), event
					.getDrugPeriod().getUnit()));
			calendar.setTimeInMillis(event.getStartTime().getTime());
		}
		if (savedInstanceState != null) {
			calendar.setTimeInMillis(savedInstanceState.getLong("calendar"));
		}

		btn_time.setText(time_format.format(calendar.getTime()));
		btn_date.setText(date_format.format(calendar.getTime()));
		btn_time.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TimePickerDialog time_dialog = new TimePickerDialog(
						getActivity(), new OnTimeSetListener() {
							@Override
							public void onTimeSet(TimePicker view,
									int hourOfDay, int minute) {
								calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
								calendar.set(Calendar.MINUTE, minute);
								btn_time.setText(time_format.format(calendar
										.getTime()));
							}
						}, calendar.get(Calendar.HOUR_OF_DAY), calendar
								.get(Calendar.MINUTE), true);
				time_dialog.show();
			}
		});
		btn_date.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DatePickerDialog date_dialog = new DatePickerDialog(
						getActivity(), new OnDateSetListener() {
							@Override
							public void onDateSet(DatePicker view, int year,
									int monthOfYear, int dayOfMonth) {
								calendar.set(Calendar.YEAR, year);
								calendar.set(Calendar.MONTH, monthOfYear);
								calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
								btn_date.setText(date_format.format(calendar
										.getTime()));
							}
						}, calendar.get(Calendar.YEAR), calendar
								.get(Calendar.MONTH), calendar
								.get(Calendar.DAY_OF_MONTH));
				date_dialog.show();
			}
		});
		btn_save.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String name = et_name.getText().toString();
				String info = et_info.getText().toString();
				if (name.isEmpty()) {
					alert.showMessage();
				} else {
					if (event == null)
						event = new DrugDetails();
					event.setDrugName(name);
					event.setDrugInfo(info);
					event.setDrugPeriod(new DrugUnit(Integer.parseInt(sp_period_q
							.getSelectedItem().toString()), sp_period_u
							.getSelectedItem().toString()));
					event.setStartTime(Utils.stringToDate(btn_date.getText()
							+ " " + btn_time.getText()));
					app.getDatabaseManager().saveEvent(event);
					getFragmentManager().popBackStack();
					//FIXME
				}
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
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putLong("calendar", calendar.getTimeInMillis());
	}

}
