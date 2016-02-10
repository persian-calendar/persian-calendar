package com.byagowi.persiancalendar.view.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * About Calendar Activity
 *
 * @author ebraminio
 */
public class AboutFragment extends Fragment {
    private final Utils utils = Utils.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        String version = utils.programVersion(getContext());

        TextView versionTextView = (TextView) view.findViewById(R.id.version2);
        utils.prepareTextView(versionTextView);
        versionTextView.setText(getString(R.string.version) + " " +
                Utils.formatNumber(version, utils.preferredDigits(getContext())));

        StringBuilder sb = new StringBuilder();

        BufferedReader input = new BufferedReader(
                new InputStreamReader(getResources().openRawResource(R.raw.credits)));
        try {
            String line;
            while ((line = input.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        TextView licenseTextView = (TextView) view.findViewById(R.id.license);
        licenseTextView.setText("Android Persian Calendar Version "
                + version
                + "\n"
                + "Copyright (C) 2012-2016  ebrahim@gnu.org "
                + Utils.textShaper("ابراهیم بیاگوی")
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
                + "For bug report and credits: http://github.com/ebraminio/DroidPersianCalendar"
                + "\n\n----\n\n" + sb.toString());

        return view;
    }
}
