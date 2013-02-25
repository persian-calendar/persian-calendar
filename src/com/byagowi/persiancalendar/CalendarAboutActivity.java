package com.byagowi.persiancalendar;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

/**
 * About Calendar Activity
 * 
 * @author ebraminio
 */
public class CalendarAboutActivity extends Activity {
	private final CalendarUtils utils = CalendarUtils.getInstance();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		utils.setTheme(this);
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.about);

		char[] digits = utils.preferredDigits(this);
		TextView versionTextView = (TextView) findViewById(R.id.version2);

		String version = utils.programVersion(this);

		String versionTitle = "نسخهٔ " + utils.formatNumber(version, digits);

		utils.prepareTextView(versionTextView);
		versionTextView.setText(utils.textShaper(versionTitle));

		TextView licenseTextView = (TextView) findViewById(R.id.license);

		licenseTextView
				.setText("Android Persian Calendar Version "
						+ version
						+ "\n"
						+ "Copyright (C) 2012-2013  ebrahim@byagowi.com\n"
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
						+ "Launcher icon is by Behdad Pournader.\n"
						+ "Splash screen logo is by Iman Soltanian.\n"
						+ "Calendar converter code from http://code.google.com/p/mobile-persian-calendar/ "
						+ "which is under GPLv2.\n"
						+ "ArabicShaper for shaping Arabic characters is from Azizhuss which is "
						+ "under BSD License.\n"
						+ "\n"
						+ "For bug report: http://github.com/ebraminio/DroidPersianCalendar");
	}
}