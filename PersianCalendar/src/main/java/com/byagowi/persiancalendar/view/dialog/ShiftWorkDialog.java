package com.byagowi.persiancalendar.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.di.dependencies.CalendarFragmentDependency;
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency;
import com.byagowi.persiancalendar.entity.FormattedIntEntity;
import com.byagowi.persiancalendar.util.CalendarUtils;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.activity.MainActivity;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import dagger.android.support.DaggerAppCompatDialogFragment;

import static com.byagowi.persiancalendar.Constants.PREF_SHIFT_WORK_SETTING;
import static com.byagowi.persiancalendar.Constants.PREF_SHIFT_WORK_STARTING_JDN;

// Needs a rewrite with RecyclerView or such
public class ShiftWorkDialog extends DaggerAppCompatDialogFragment {
    private static String BUNDLE_KEY = "jdn";
    @Inject
    MainActivityDependency mainActivityDependency;
    @Inject
    CalendarFragmentDependency calendarFragmentDependency;

    public static ShiftWorkDialog newInstance(long jdn) {
        Bundle args = new Bundle();
        args.putLong(BUNDLE_KEY, jdn);

        ShiftWorkDialog fragment = new ShiftWorkDialog();
        fragment.setArguments(args);
        return fragment;
    }

    static class Row {
        final AppCompatSpinner daysSpinner;
        final AppCompatSpinner typeSpinner;

        Row(AppCompatSpinner daysSpinner, AppCompatSpinner typeSpinner) {
            this.daysSpinner = daysSpinner;
            this.typeSpinner = typeSpinner;
        }
    }

    private ArrayList<Row> rowsList = new ArrayList<>();

    private void addRow(Context context, LinearLayout rowsContainer, String type, int length) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);

        AppCompatSpinner daysSpinner = new AppCompatSpinner(context);
        List<FormattedIntEntity> days = new ArrayList<>();
        for (int i = 0; i <= 7; ++i) {
            days.add(new FormattedIntEntity(i, Utils.formatNumber(i)));
        }
        daysSpinner.setAdapter(new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item, days));
        daysSpinner.setSelection(length);
        row.addView(daysSpinner);

        AppCompatSpinner typeSpinner = new AppCompatSpinner(context);
        typeSpinner.setAdapter(new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.shift_work)));
        typeSpinner.setSelection(shiftWorkKeys.indexOf(type));
        row.addView(typeSpinner);

        MaterialButton button = new MaterialButton(context);
        button.setText(R.string.remove);
        button.setOnClickListener(v -> {
            daysSpinner.setSelection(0);
            row.setVisibility(View.GONE);
        });
        row.addView(button);

        rowsContainer.addView(row);
        rowsList.add(new Row(daysSpinner, typeSpinner));
    }

    private List<String> shiftWorkKeys = Collections.emptyList();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        long tempJdn = args == null ? -1 : args.getLong(BUNDLE_KEY, -1);
        if (tempJdn == -1) tempJdn = CalendarUtils.getTodayJdn();
        long jdn = tempJdn;

        shiftWorkKeys = Arrays.asList(getResources().getStringArray(R.array.shift_work_keys));

        MainActivity mainActivity = mainActivityDependency.getMainActivity();

        LinearLayout container = new LinearLayout(mainActivity);
        container.setPadding(20, 20, 20, 20);
        container.setOrientation(LinearLayout.VERTICAL);

        ScrollView scrollView = new ScrollView(mainActivity);
        container.addView(scrollView);

        LinearLayout rowsView = new LinearLayout(mainActivity);
        rowsView.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(rowsView);

        List<Utils.ShiftWorkRecord> shiftWorks = Utils.getShiftWorks();
        if (shiftWorks.size() == 0)
            shiftWorks = Collections.singletonList(new Utils.ShiftWorkRecord("d", 0));
        for (Utils.ShiftWorkRecord shift : shiftWorks)
            addRow(mainActivity, rowsView, shift.type, shift.length);

        MaterialButton addButton = new MaterialButton(mainActivity);
        addButton.setText(R.string.add);
        addButton.setOnClickListener(v -> addRow(mainActivity, rowsView, "r", 0));
        container.addView(addButton);

        TextView description = new TextView(mainActivity);
        description.setText(String.format("شروع شیفت‌کاری از روز انتخاب شده در تقویم، %s است.",
                CalendarUtils.formatDate(
                        CalendarUtils.getDateFromJdnOfCalendar(Utils.getMainCalendar(), jdn))));
        description.append("\n");
        description.append("برای غیرفعال کردن شیفت‌کاری و یا برنامه‌ریزی مجدد، همهٔ سطرها را حذف کنید دوباره وارد تنظیمات شوید.");
        description.setPadding(0, 20, 0, 0);
        container.addView(description);

        return new AlertDialog.Builder(mainActivity)
                .setView(container)
                .setTitle(R.string.shift_work_settings)
                .setPositiveButton(R.string.accept, (dialogInterface, i) -> {
                    StringBuilder result = new StringBuilder();
                    boolean first = true;
                    for (Row row : rowsList) {
                        int length = row.daysSpinner.getSelectedItemPosition();
                        if (length == 0) continue;

                        if (first) first = false;
                        else result.append(",");
                        result.append(shiftWorkKeys.get(row.typeSpinner.getSelectedItemPosition()));
                        result.append("=");
                        result.append(length);
                    }

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mainActivity);
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putLong(PREF_SHIFT_WORK_STARTING_JDN, result.length() == 0 ? -1 : jdn);
                    edit.putString(PREF_SHIFT_WORK_SETTING, result.toString());
                    edit.apply();

                    calendarFragmentDependency.getCalendarFragment().afterShiftWorkChange();
                    mainActivity.restartActivity();
                })
                .setCancelable(true)
                .setNegativeButton(R.string.cancel, null)
                .create();
    }
}
