package com.byagowi.persiancalendar.view.drugalert.receiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.byagowi.persiancalendar.view.drugalert.service.RestoreDrugAlert;


/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class ResetService extends BroadcastReceiver {

	@SuppressLint("UnsafeProtectedBroadcastReceiver")
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent restoreIntent = new Intent(context, RestoreDrugAlert.class);
		context.startService(restoreIntent);
	}

}
