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
import android.widget.Toast;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.databinding.FragmentAboutBinding;
import com.byagowi.persiancalendar.util.Utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

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
        FragmentAboutBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_about,
                container, false);

        Utils.setActivityTitleAndSubtitle(getActivity(), getString(R.string.about), "");

        // version
        String version = programVersion();
        binding.version.setText(getString(R.string.version) + " " + Utils.formatNumber(version.split("-")[0]));

        // licenses
        binding.licenses.setOnClickListener(arg -> {
            WebView wv = new WebView(getActivity());
            WebSettings settings = wv.getSettings();
            settings.setDefaultTextEncodingName("utf-8");
            wv.loadUrl("file:///android_res/raw/credits.txt");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getResources().getString(R.string.about_license_title));
            builder.setView(wv);
            builder.setCancelable(true);
            builder.setNegativeButton(R.string.about_license_dialog_close, (dialog, which) -> {
            });
            builder.show();
        });

        // help
        binding.aboutTitle.setText(String.format(getString(R.string.about_help_subtitle),
                Utils.formatNumber(Utils.getMaxSupportedYear())));
        binding.helpSum.setText(R.string.about_help_sum);

        // report bug
        binding.reportBug.setOnClickListener(arg -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/ebraminio/DroidPersianCalendar/issues/new")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        binding.email.setOnClickListener(arg -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", getString(R.string.about_mailto), null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            try {
                emailIntent.putExtra(Intent.EXTRA_TEXT, "\n\n\n\n\n\n\n" +
                        "===Device Information===\nManufacturer: " + Build.MANUFACTURER + "\nModel: " + Build.MODEL + "\nAndroid Version: " + Build.VERSION.RELEASE + "\nApp Version Code: " + version.split("-")[0]);
                startActivity(Intent.createChooser(emailIntent, getString(R.string.about_sendMail)));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(getActivity(), getString(R.string.about_noClient), Toast.LENGTH_SHORT).show();
            }
        });

        return binding.getRoot();
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
