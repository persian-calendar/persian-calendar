package com.byagowi.persiancalendar.view.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.databinding.FragmentAboutBinding;
import com.byagowi.persiancalendar.util.UIUtils;
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
        Context ctx = getContext();
        if (ctx == null) return null;

        FragmentAboutBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_about,
                container, false);

        UIUtils.setActivityTitleAndSubtitle(getActivity(), getString(R.string.about), "");

        // version
        String version = programVersion(ctx);
        binding.version.setText(getString(R.string.version) + " " + Utils.formatNumber(version.split("-")[0]));

        // licenses
        binding.licenses.setOnClickListener(arg -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setTitle(getResources().getString(R.string.about_license_title));
            TextView licenseTextView = new TextView(ctx);
            licenseTextView.setText(Utils.readRawResource(ctx, R.raw.credits));
            licenseTextView.setPadding(20, 20, 20, 20);
            licenseTextView.setTypeface(Typeface.MONOSPACE);
            Linkify.addLinks(licenseTextView, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES);
            ScrollView scrollView = new ScrollView(ctx);
            scrollView.addView(licenseTextView);
            builder.setView(scrollView);
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
                Toast.makeText(ctx, getString(R.string.about_noClient), Toast.LENGTH_SHORT).show();
            }
        });

        return binding.getRoot();
    }

    private String programVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(AboutFragment.class.getName(), "Name not found on PersianCalendarUtils.programVersion");
            return "";
        }
    }
}
