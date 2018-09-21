package com.byagowi.persiancalendar.view.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.view.daypickerview.DayPickerView;
import com.byagowi.persiancalendar.view.daypickerview.SimpleDayPickerView;
import com.byagowi.persiancalendar.view.fragment.CalendarFragment;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

/**
 * Created by ebrahim on 3/20/16.
 */
public class SelectDayDialog extends AppCompatDialogFragment {
    private static String BUNDLE_KEY = "jdn";

    public static SelectDayDialog newInstance(long jdn) {
        Bundle args = new Bundle();
        args.putLong(BUNDLE_KEY, jdn);

        SelectDayDialog fragment = new SelectDayDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        long jdn = args.getLong(BUNDLE_KEY, -1);

        DayPickerView dayPickerView = new SimpleDayPickerView(getContext());
        dayPickerView.setDayJdnOnView(jdn);

        return new AlertDialog.Builder(getActivity())
                .setView((View) dayPickerView)
                .setCustomTitle(null)
                .setPositiveButton(R.string.go, (dialogInterface, i) -> {
                    CalendarFragment calendarFragment = (CalendarFragment) getActivity()
                            .getSupportFragmentManager()
                            .findFragmentByTag(CalendarFragment.class.getName());

                    long resultJdn = dayPickerView.getDayJdnFromView();
                    if (resultJdn != -1) calendarFragment.bringDate(resultJdn);
                }).create();
    }
}
