package com.byagowi.persiancalendar.ui.calendar.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.di.dependencies.CalendarFragmentDependency;
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency;
import com.byagowi.persiancalendar.ui.MainActivity;
import com.byagowi.persiancalendar.ui.shared.DayPickerView;
import com.byagowi.persiancalendar.ui.shared.SimpleDayPickerView;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import dagger.android.support.DaggerAppCompatDialogFragment;

/**
 * Created by ebrahim on 3/20/16.
 */
public class SelectDayDialog extends DaggerAppCompatDialogFragment {
    private static String BUNDLE_KEY = "jdn";
    @Inject
    MainActivityDependency mainActivityDependency;
    @Inject
    CalendarFragmentDependency calendarFragmentDependency;

    public static SelectDayDialog newInstance(long jdn) {
        Bundle args = new Bundle();
        args.putLong(BUNDLE_KEY, jdn);

        SelectDayDialog fragment = new SelectDayDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        long jdn = args == null ? -1 : args.getLong(BUNDLE_KEY, -1);

        MainActivity mainActivity = mainActivityDependency.getMainActivity();
        DayPickerView dayPickerView = new SimpleDayPickerView(mainActivity);
        dayPickerView.setDayJdnOnView(jdn);

        return new AlertDialog.Builder(mainActivity)
                .setView((View) dayPickerView)
                .setCustomTitle(null)
                .setPositiveButton(R.string.go, (dialogInterface, i) -> {
                    long resultJdn = dayPickerView.getDayJdnFromView();
                    if (resultJdn != -1)
                        calendarFragmentDependency.getCalendarFragment().bringDate(resultJdn);
                }).create();
    }
}
