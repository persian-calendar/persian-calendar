package com.byagowi.persiancalendar.view.drugalert.model;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.view.drugalert.fragment.DrugFragment;
import com.byagowi.persiancalendar.view.drugalert.application.DrugAlertApplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class DrugAdapter extends ArrayAdapter<DrugDetails> {
	private Context context;
	private int layoutResourceId;
	private DrugDetails data[];
	private DrugAlertApplication app;

	public DrugAdapter(Context context, int layoutResourceId, DrugDetails[] data) {
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = data;
		this.app = (DrugAlertApplication) ((Activity) context).getApplication();
	}

	public long getItemId(int position) {
		return data[position].getId();
	}

	@SuppressLint("SetTextI18n")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
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

		holder.tv_name.setText(data[position].getDrugName());
		holder.tv_period.setText(context.getResources().getString(
				R.string.drugPeriod)
				+ " "
				+ data[position].getDrugPeriod().getQuantity()
				+ " "
				+ data[position].getDrugPeriod().getUnit());

		final int pos = position;
		holder.btn_delete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				app.getDatabaseManager().removeEvent(data[pos].getId());
				//((DrugFragment) context).refresh();
				//FIXME
			}
		});
		return row;
	}

	private class ViewHolder {
		TextView tv_name;
		TextView tv_period;
		Button btn_delete;
	}

}
