package com.byagowi.persiancalendar;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class PersianDateHolidays {
	static private List<Date> holidays = Arrays.asList(new Date[]{
		new Date(2012, 1, 14),
		new Date(2012, 1, 22)
	});
	
	static public boolean isHoliday(Date day) {
		for (Date holiday : holidays) {
			if (day.equals(holiday)) {
				return true;
			}
		}
		return false;
	}
}
