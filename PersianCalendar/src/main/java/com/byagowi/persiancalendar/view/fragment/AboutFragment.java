package com.byagowi.persiancalendar.view.fragment;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.util.Utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * About Calendar Activity
 *
 * @author ebraminio
 */
public class AboutFragment extends Fragment {

    public String programVersion() {
        try {
            return getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(AboutFragment.class.getName(), "Name not found on PersianCalendarUtils.programVersion");
            return "";
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        Utils.setActivityTitleAndSubtitle(getActivity(), getString(R.string.about), "");

        String version = programVersion();

        TextView versionTextView = view.findViewById(R.id.version2);
        versionTextView.setText(getString(R.string.version) + " " +
                Utils.formatNumber(version.split("-")[0]));

        TextView licenseTextView = view.findViewById(R.id.license);
        licenseTextView.setText("Android Persian Calendar Version " + version + "\n" +
                Utils.readRawResource(getContext(), R.raw.credits));

        Linkify.addLinks(licenseTextView, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES);

        return view;
    }
}
