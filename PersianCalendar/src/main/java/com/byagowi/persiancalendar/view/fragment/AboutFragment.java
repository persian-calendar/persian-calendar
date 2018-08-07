package com.byagowi.persiancalendar.view.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.util.Utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import calendar.AbstractDate;
import calendar.CalendarType;

/**
 * About Calendar Activity
 *
 * @author ebraminio
 */
public class AboutFragment extends Fragment {

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        Utils.setActivityTitleAndSubtitle(getActivity(), getString(R.string.about), "");

        //version
        String version = programVersion();
        TextView versionTextView = view.findViewById(R.id.version);
        versionTextView.setText(getString(R.string.version) + " " +
                Utils.formatNumber(version.split("-")[0]));

        //licenses
        RelativeLayout licenses = view.findViewById(R.id.licenses);
        licenses.setOnClickListener(arg -> {
            WebView wv = new WebView(getActivity());
            WebSettings settings = wv.getSettings();
            settings.setDefaultTextEncodingName("utf-8");
            wv.loadUrl("file:///android_res/raw/credits.txt");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getResources().getString(R.string.license));
            builder.setView(wv);
            builder.setCancelable(true);
            builder.setNegativeButton(R.string.license_dialog_close, (dialog, which) -> {
            });
            builder.show();
        });

        //help
        CalendarType mainCalendar = Utils.getMainCalendar();
        AbstractDate today = Utils.getTodayOfCalendar(mainCalendar);
        TextView help_title = view.findViewById(R.id.about_title);
        help_title.setText(getString(R.string.about_help_title_one) + " " + Utils.formatNumber(today.getYear()) + " " + getString(R.string.about_help_title_two));
        TextView help = view.findViewById(R.id.help_sum);
        help.setText(R.string.about_help_sum);

        //report bug
        RelativeLayout bug = view.findViewById(R.id.reportBug);
        bug.setOnClickListener(arg -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", getString(R.string.mailto), null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            try {
                emailIntent.putExtra(Intent.EXTRA_TEXT, "\n\n\n\n\n\n\n" +
                        "===Device Information===\nManufacturer: " + Build.MANUFACTURER + "\nModel: " + Build.MODEL + "\nAndroid Version: " + Build.VERSION.RELEASE + "\nApp Version Code: " + version.split("-")[0]);
                startActivity(Intent.createChooser(emailIntent, getString(R.string.sendMail)));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(getActivity(), getString(R.string.noClient), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private String programVersion() {
        try {
            return getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(AboutFragment.class.getName(), "Name not found on PersianCalendarUtils.programVersion");
            return "";
        }
    }
}
