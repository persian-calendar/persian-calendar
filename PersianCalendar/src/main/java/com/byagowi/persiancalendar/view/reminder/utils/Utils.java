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
	
	public static int getUnit(Context context, String unit) {
		String[] units = context.getResources().getStringArray(R.array.period_units);
		for(int i=0;i<units.length;i++)
			if(units[i].equals(unit))
				return i;
		return 0;
	}
}
