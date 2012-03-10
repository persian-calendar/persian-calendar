package com.byagowi.persiancalendar;

import java.util.Date;

/**
 * Utilities needed for Persian
 * 
 * @author ebraminio
 * 
 */
public final class ArabicUtils {

	private static final char[] persianDigit = { '٠', '١', '٢', '٣', '٤', '٥',
			'٦', '٧', '٨', '٩' };

	public static String getArabicNumber(int number) {
		return getArabicNumber(number + "");
	}

	public static String getArabicNumber(String number) {
		StringBuilder sb = new StringBuilder();
		for (char i : number.toCharArray()) {
			if (Character.isDigit(i)) {
				sb.append(persianDigit[Integer.parseInt(i + "")]);
			} else {
				sb.append(i);
			}
		}
		return sb.toString();
	}
}
