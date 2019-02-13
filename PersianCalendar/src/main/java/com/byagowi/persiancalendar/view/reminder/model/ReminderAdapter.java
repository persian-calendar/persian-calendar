package com.byagowi.persiancalendar.view.reminder.model;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.view.reminder.database.DatabaseManager;

import androidx.annotation.NonNull;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class ReminderAdapter extends ArrayAdapter<ReminderDetails> {
	private Context context;
	private int layoutResourceId;
	private ReminderDetails data[];
	private DatabaseManager databaseManager;

	public ReminderAdapter(Context context, int layoutResourceId, ReminderDetails[] data) {
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = data;
		databaseManager = new DatabaseManager(context);
	}

	public long getItemId(int position) {
		return data[position].getId();
	}

	@NonNull
	@SuppressLint("SetTextI18n")
	@Override
	public View getView(int position, View convertView, @NonNull ViewGroup parent) {
		View row = convertView;
		ViewHolder holder;

		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new ViewHolder();
			holder.tv_name = row.findViewById(R.id.tv_name);
			holder.tv_period = row.findViewById(R.id.tv_period);
			holder.btn_delete = row.findViewById(R.id.btn_delete);
			row.setTag(holder);
		} else {
			holder = (ViewHolder) row.getTag();
		}

		holder.tv_name.setText(data[position].getReminderName());
		holder.tv_period.setText(context.getResources().getString(
				R.string.reminderPeriod)
				+ " "
				+ data[position].getReminderPeriod().getQuantity()
				+ " "
				+ data[position].getReminderPeriod().getUnit());

		final int pos = position;
		holder.btn_delete.setOnClickListener(v -> {
			databaseManager.removeEvent(data[pos].getId());
			//FIXME
		});
		return row;
	}

	private class ViewHolder {
		TextView tv_name;
		TextView tv_period;
		Button btn_delete;
	}
}
