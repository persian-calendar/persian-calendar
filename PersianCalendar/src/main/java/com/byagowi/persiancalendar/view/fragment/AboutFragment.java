package com.byagowi.persiancalendar.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.util.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * About Calendar Activity
 *
 * @author ebraminio
 */
public class AboutFragment extends Fragment {
    private Utils utils;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        utils = Utils.getInstance(getContext());
        utils.setActivityTitleAndSubtitle(getActivity(), getString(R.string.about), "");

        String version = utils.programVersion();

        TextView versionTextView = (TextView) view.findViewById(R.id.version2);
        utils.setFont(versionTextView);
        versionTextView.setText(utils.shape(getString(R.string.version)) + " " + utils.formatNumber(version));

        TextView licenseTextView = (TextView) view.findViewById(R.id.license);
        licenseTextView.setText("Android Persian Calendar Version " + version + "\n" +
                utils.convertStreamToString(getResources().openRawResource(R.raw.credits)));

        Linkify.addLinks(licenseTextView, Linkify.ALL);

        return view;
    }
}
