/*
 * March 2012
 *
 * In place of a legal notice, here is a blessing:
 *
 *    May you do good and not evil.
 *    May you find forgiveness for yourself and forgive others.
 *    May you share freely, never taking more than you give.
 *
 */
package com.byagowi.persiancalendar;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

/**
 * About Calendar Activity
 * 
 * @author ebraminio
 * 
 */
public class PersianCalendarAboutActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.about);

		char[] digits = PersianCalendarUtils.getDigitsFromPreference(this);
		TextView versionTextView = ((TextView) findViewById(R.id.version2));
		String version = "";
		try {
			version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			
			String versionTitle = "نسخهٔ "
					+ PersianCalendarUtils.formatNumber(version, digits);

			versionTextView.setText(PersianCalendarUtils
					.textShaper(versionTitle));
		} catch (Exception e) {
			Log.e(getPackageName(), e.getMessage());
		}
		
		((TextView) findViewById(R.id.license))
				.setText("Android Persian Calendar Version " + version + "\n"
						+ "Copyright (C) 2012  ebrahim@byagowi.com\n"
						+ "\n"
						+ "This program is free software: you can redistribute it and/or modify "
						+ "it under the terms of the GNU General Public License as published by "
						+ "the Free Software Foundation, either version 3 of the License, or "
						+ "(at your option) any later version.\n"
						+ "\n"
						+ "This program is distributed in the hope that it will be useful, "
						+ "but WITHOUT ANY WARRANTY; without even the implied warranty of "
						+ "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the "
						+ "GNU General Public License for more details.\n"
						+ "\n"
						+ "You should have received a copy of the GNU General Public License "
						+ "along with this program.  If not, see http://www.gnu.org/licenses/.\n"
						+ "\n"
						+ "Background and about icon artworks designs are from Iman Soltanian.\n"
						+ "Calendar converter code used from http://code.google.com/p/mobile-persian-calendar/ "
						+ "that it was under GPLv2.\n"
						+ "ArabicShaper for shaping Persian Characters used from Azizhuss, it is "
						+ "under BSD License.\n"
						+ "Iranian Sans and Nazli font also used in this application that they are opensources fonts.\n"
						+ "\n"
						+ "For repoting any bug, please come to this address: http://github.com/ebraminio/DroidPersianCalendar :)\n");
	}
}