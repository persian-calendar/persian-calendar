package com.byagowi.persiancalendar.view.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.util.TypedValue;
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
import com.google.android.material.chip.Chip;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

public class AboutFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Activity localActivity = getActivity();
        if (localActivity == null) return null;

        FragmentAboutBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_about,
                container, false);

        UIUtils.setActivityTitleAndSubtitle(localActivity, getString(R.string.about), "");

        // version
        String[] version = programVersion(localActivity).split("-");
        version[0] = Utils.formatNumber(version[0]);
        binding.version.setText(String.format(getString(R.string.version), TextUtils.join("\n", version)));

        // licenses
        binding.licenses.setOnClickListener(arg -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(localActivity);
            builder.setTitle(getResources().getString(R.string.about_license_title));
            TextView licenseTextView = new TextView(localActivity);
            licenseTextView.setText(Utils.readRawResource(localActivity, R.raw.credits));
            licenseTextView.setPadding(20, 20, 20, 20);
            licenseTextView.setTypeface(Typeface.MONOSPACE);
            Linkify.addLinks(licenseTextView, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES);
            ScrollView scrollView = new ScrollView(localActivity);
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
                emailIntent.putExtra(Intent.EXTRA_TEXT,
                        String.format("\n\n\n\n\n\n\n===Device Information===\nManufacturer: %s\nModel: %s\nAndroid Version: %s\nApp Version Code: %s",
                                Build.MANUFACTURER, Build.MODEL, Build.VERSION.RELEASE, version[0]));
                startActivity(Intent.createChooser(emailIntent, getString(R.string.about_sendMail)));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(localActivity, getString(R.string.about_noClient), Toast.LENGTH_SHORT).show();
            }
        });

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        Drawable developerIcon = ContextCompat.getDrawable(localActivity, R.drawable.ic_developer);
        Drawable designerIcon = ContextCompat.getDrawable(localActivity, R.drawable.ic_designer);
        Resources.Theme theme = localActivity.getTheme();
        TypedValue color = new TypedValue();
        theme.resolveAttribute(R.attr.colorDrawerIcon, color, true);

        for (String line : getString(R.string.about_developers_list).trim().split("\n")) {
            Chip chip = new Chip(localActivity);
            chip.setText(line);
            chip.setChipIcon(developerIcon);
            chip.setChipIconTintResource(color.resourceId);
            binding.developers.addView(chip);
        }

        for (String line : getString(R.string.about_designers_list).trim().split("\n")) {
            Chip chip = new Chip(localActivity);
            chip.setText(line);
            chip.setChipIcon(designerIcon);
            chip.setChipIconTintResource(color.resourceId);
            binding.developers.addView(chip);
        }

        for (String line : getString(R.string.about_contributors_list).trim().split("\n")) {
            Chip chip = new Chip(localActivity);
            chip.setText(line);
            chip.setChipIcon(developerIcon);
            chip.setChipIconTintResource(color.resourceId);
            binding.developers.addView(chip);
        }


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
