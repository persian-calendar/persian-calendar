package com.byagowi.persiancalendar.view.fragment;

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.databinding.DialogEmailBinding;
import com.byagowi.persiancalendar.databinding.FragmentAboutBinding;
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.activity.MainActivity;
import com.google.android.material.chip.Chip;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import dagger.android.support.DaggerFragment;

public class AboutFragment extends DaggerFragment {

    @Inject
    MainActivityDependency mainActivityDependency;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        FragmentAboutBinding binding = FragmentAboutBinding.inflate(inflater, container, false);

        MainActivity activity = mainActivityDependency.getMainActivity();
        activity.setTitleAndSubtitle(getString(R.string.about), "");

        // version
        String[] version = programVersion(activity).split("-");
        version[0] = Utils.formatNumber(version[0]);
        binding.version.setText(String.format(getString(R.string.version), TextUtils.join("\n", version)));

        // licenses
        binding.licenses.setOnClickListener(arg -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(getResources().getString(R.string.about_license_title));
            TextView licenseTextView = new TextView(activity);
            licenseTextView.setText(Utils.readRawResource(activity, R.raw.credits));
            licenseTextView.setPadding(20, 20, 20, 20);
            licenseTextView.setTypeface(Typeface.MONOSPACE);
            Linkify.addLinks(licenseTextView, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES);
            ScrollView scrollView = new ScrollView(activity);
            scrollView.addView(licenseTextView);
            builder.setView(scrollView);
            builder.setCancelable(true);
            builder.setNegativeButton(R.string.about_license_dialog_close, null);
            builder.show();
        });

        // help
        binding.aboutTitle.setText(String.format(getString(R.string.about_help_subtitle),
                Utils.formatNumber(Utils.getMaxSupportedYear() - 1),
                Utils.formatNumber(Utils.getMaxSupportedYear())));
        switch (Utils.getAppLanguage()) {
            case Constants.LANG_FA:
            case Constants.LANG_FA_AF:
            case Constants.LANG_EN_IR: // en. unlike en-US, is for Iranians as indicated also on UI
                break;
            default:
                binding.helpCard.setVisibility(View.GONE);
        }

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
            DialogEmailBinding emailBinding = DialogEmailBinding.inflate(inflater, container, false);
            new AlertDialog.Builder(mainActivityDependency.getMainActivity())
                    .setView(emailBinding.getRoot())
                    .setTitle(R.string.about_email_sum)
                    .setPositiveButton(R.string.continue_button, (dialog, id) -> {
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts
                                ("mailto", "ebrahim@gnu.org", null));
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                        try {
                            emailIntent.putExtra(Intent.EXTRA_TEXT,
                                    String.format(emailBinding.inputText.getText() + "\n\n\n\n\n\n\n===Device Information===\nManufacturer: %s\nModel: %s\nAndroid Version: %s\nApp Version Code: %s",
                                            Build.MANUFACTURER, Build.MODEL, Build.VERSION.RELEASE, version[0]));
                            startActivity(Intent.createChooser(emailIntent, getString(R.string.about_sendMail)));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(activity, getString(R.string.about_noClient), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null).show();
        });

        Drawable developerIcon = AppCompatResources.getDrawable(activity, R.drawable.ic_developer);
        Drawable designerIcon = AppCompatResources.getDrawable(activity, R.drawable.ic_designer);
        Resources.Theme theme = activity.getTheme();
        TypedValue color = new TypedValue();
        theme.resolveAttribute(R.attr.colorDrawerIcon, color, true);

        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(8, 8, 8, 8);

        View.OnClickListener chipClick = view -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/" +
                                ((Chip) view).getText().toString()
                                        .split("@")[1].split("\\)")[0])));
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        for (String line : getString(R.string.about_developers_list).trim().split("\n")) {
            Chip chip = new Chip(activity);
            chip.setLayoutParams(layoutParams);
            chip.setOnClickListener(chipClick);
            chip.setText(line);
            chip.setChipIcon(developerIcon);
            chip.setChipIconTintResource(color.resourceId);
            binding.developers.addView(chip);
        }

        for (String line : getString(R.string.about_designers_list).trim().split("\n")) {
            Chip chip = new Chip(activity);
            chip.setLayoutParams(layoutParams);
            chip.setText(line);
            chip.setChipIcon(designerIcon);
            chip.setChipIconTintResource(color.resourceId);
            binding.developers.addView(chip);
        }

        for (String line : getString(R.string.about_contributors_list).trim().split("\n")) {
            Chip chip = new Chip(activity);
            chip.setLayoutParams(layoutParams);
            chip.setOnClickListener(chipClick);
            chip.setText(line);
            chip.setChipIcon(developerIcon);
            chip.setChipIconTintResource(color.resourceId);
            binding.developers.addView(chip);
        }


        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.about_menu_buttons, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deviceInfo:
                mainActivityDependency.getMainActivity().navigateTo(R.id.deviceInfo);
                break;
            default:
                break;
        }
        return true;
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
