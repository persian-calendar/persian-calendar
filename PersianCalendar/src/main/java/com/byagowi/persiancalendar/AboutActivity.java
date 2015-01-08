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
public class AboutActivity extends Activity {
    private final Utils utils = Utils.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        utils.setTheme(this);
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.about);

        char[] digits = utils.preferredDigits(this);
        TextView versionTextView = (TextView) findViewById(R.id.version2);

        String version = utils.programVersion(this);

        String versionTitle = utils.version + " "
                + utils.formatNumber(version, digits);

        utils.prepareTextView(versionTextView);
        versionTextView.setText(versionTitle);

        TextView licenseTextView = (TextView) findViewById(R.id.license);

        String text = "Android Persian Calendar Version "
                + version
                + "\n"
                + "Copyright (C) 2012-2015  ebrahim@gnu.org "
                + utils.textShaper("ابراهیم بیاگوی")
                + "\n\n"
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
                + "For bug report and credits: http://github.com/ebraminio/DroidPersianCalendar";

        licenseTextView.setText(text);
    }
}