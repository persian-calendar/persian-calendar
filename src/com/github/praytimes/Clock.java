package com.github.praytimes;

import java.util.Locale;

import static com.github.praytimes.StaticUtils.fixHour;

public class Clock {
	private final int hour;
	private final int minute;

	private Clock(int hour, int minute) {
		this.hour = hour;
		this.minute = minute;
	}

	static Clock fromDouble(double arg) {
		arg = fixHour(arg + 0.5 / 60); // add 0.5 minutes to round
		int hour = (int) arg;
		int minute = (int) ((arg - hour) * 60d);
		return new Clock(hour, minute);
	}

	@Override
	public String toString() {
		return String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
	}

	public int getHour() {
		return hour;
	}

	public int getMinute() {
		return minute;
	}
}