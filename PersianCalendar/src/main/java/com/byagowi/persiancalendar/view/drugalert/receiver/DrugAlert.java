package com.byagowi.persiancalendar.view.drugalert.receiver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.byagowi.persiancalendar.view.drugalert.activity.DrugAlertActivity;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class DrugAlert extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent reminderIntent = new Intent(context, DrugAlertActivity.class);
		reminderIntent.putExtras(intent.getExtras());
		reminderIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(reminderIntent);
	}
}