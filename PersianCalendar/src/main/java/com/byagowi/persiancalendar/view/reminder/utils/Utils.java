package com.byagowi.persiancalendar.view.reminder.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.view.reminder.constants.Constants;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class Utils {

	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";

	public static String dateToString(Date date) {
		SimpleDateFormat DateFormat = new SimpleDateFormat(DATE_FORMAT,
				Locale.getDefault());
		return DateFormat.format(date);
	}

	public static Date stringToDate(String date) {
		SimpleDateFormat DateFormat = new SimpleDateFormat(DATE_FORMAT,
				Locale.getDefault());
		try {
			return DateFormat.parse(date);
		} catch (ParseException e) {
			return null;
		}
	}

	public static String[] getQuantity() {
		String[] quantity = new String[Constants.MAX_QUANTITY - 1];
		for (int i = 1; i < Constants.MAX_QUANTITY; i++)
			quantity[i - 1] = String.valueOf(i);
		return quantity;
	}
	
	public static int getUnit(Context context, String unit) {
		String[] units = context.getResources().getStringArray(R.array.period_units);
		for(int i=0;i<units.length;i++)
			if(units[i].equals(unit))
				return i;
		return 0;
	}
}
