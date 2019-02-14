package com.byagowi.persiancalendar.view.reminder.common;

import android.app.AlertDialog;
import android.content.Context;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.view.reminder.constants.Constants;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class Dialog {

	private AlertDialog.Builder builder;

	public Dialog(Context context) {
		builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.alertTitle);
		builder.setPositiveButton(R.string.ok, (dialog, which) -> dialog.cancel());
	}

	public void showMessage() {
		switch(Constants.EMPTY_NAME) {
		case Constants.EMPTY_NAME:
			builder.setMessage(R.string.emptyName);
			break;
		}
		builder.create().show();
	}
}
