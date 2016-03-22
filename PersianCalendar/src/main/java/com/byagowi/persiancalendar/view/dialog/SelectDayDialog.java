package com.byagowi.persiancalendar.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.adapter.ShapedArrayAdapter;
import com.byagowi.persiancalendar.enums.CalendarTypeEnum;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.fragment.CalendarFragment;

import java.util.ArrayList;
import java.util.List;

import calendar.AbstractDate;
import calendar.CivilDate;
import calendar.DateConverter;
import calendar.IslamicDate;
import calendar.PersianDate;

/**
 * Created by ebrahim on 3/20/16.
 */
public class SelectDayDialog extends AppCompatDialogFragment
        implements AdapterView.OnItemSelectedListener {
    private Utils utils;
    private Spinner calendarTypeSpinner;
    private Spinner yearSpinner;
    private Spinner monthSpinner;
    private Spinner daySpinner;
    private int startingYearOnYearSpinner = 0;

    @IdRes private final static int DROPDOWN_LAYOUT = R.layout.select_dialog_item;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.selectday_fragment, null);

        utils = Utils.getInstance(getContext());

        // fill members
        calendarTypeSpinner = (Spinner) view.findViewById(R.id.calendarTypeSpinner);
        yearSpinner = (Spinner) view.findViewById(R.id.yearSpinner);
        monthSpinner = (Spinner) view.findViewById(R.id.monthSpinner);
        daySpinner = (Spinner) view.findViewById(R.id.daySpinner);

        utils.setFontAndShape((TextView) view.findViewById(R.id.converterLabelDay));
        utils.setFontAndShape((TextView) view.findViewById(R.id.converterLabelMonth));
        utils.setFontAndShape((TextView) view.findViewById(R.id.converterLabelYear));
        utils.setFontAndShape((TextView) view.findViewById(R.id.calendarTypeTitle));

        fillYearMonthDaySpinners();

        calendarTypeSpinner.setAdapter(new ShapedArrayAdapter(getContext(),
                DROPDOWN_LAYOUT, getResources().getStringArray(R.array.calendar_type)));
        calendarTypeSpinner.setSelection(0);

        calendarTypeSpinner.setOnItemSelectedListener(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setCustomTitle(null);
        builder.setPositiveButton(R.string.select, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                int year = startingYearOnYearSpinner + yearSpinner.getSelectedItemPosition();
                int month = monthSpinner.getSelectedItemPosition() + 1;
                int day = daySpinner.getSelectedItemPosition() + 1;

                CalendarFragment calendarFragment = (CalendarFragment) getActivity()
                        .getSupportFragmentManager()
                        .findFragmentByTag(Constants.CALENDAR_MAIN_FRAGMENT_TAG);

                try {
                    switch (calendarTypeFromPosition(
                            calendarTypeSpinner.getSelectedItemPosition())) {
                        case GREGORIAN:
                            calendarFragment.bringDate(DateConverter.civilToPersian(
                                    new CivilDate(year, month, day)));
                            break;

                        case ISLAMIC:
                            calendarFragment.bringDate(DateConverter.islamicToPersian(
                                    new IslamicDate(year, month, day)));
                            break;

                        case SHAMSI:
                            calendarFragment.bringDate(new PersianDate(year, month, day));
                            break;

                        default:
                            return;
                    }
                } catch (RuntimeException e) {
                    // conversion exception, nvm
                    Log.e("SelectDayDialog", "", e);
                }
            }
        });

        return builder.create();
    }

    private CalendarTypeEnum calendarTypeFromPosition(int position) {
        if (position == 0)
            return CalendarTypeEnum.SHAMSI;
        else if (position == 1)
            return CalendarTypeEnum.ISLAMIC;
        else
            return CalendarTypeEnum.GREGORIAN;
    }

    private void fillYearMonthDaySpinners() {
        AbstractDate date = null;
        PersianDate newDatePersian = utils.getToday();
        CivilDate newDateCivil = DateConverter.persianToCivil(newDatePersian);
        IslamicDate newDateIslamic = DateConverter.persianToIslamic(newDatePersian);

        date = newDateCivil;
        CalendarTypeEnum selectedCalendarType = calendarTypeFromPosition(
                calendarTypeSpinner.getSelectedItemPosition());
        switch (selectedCalendarType) {
            case GREGORIAN:
                date = newDateCivil;
                break;

            case ISLAMIC:
                date = newDateIslamic;
                break;

            case SHAMSI:
                date = newDatePersian;
                break;
        }

        // years spinner init.
        List<String> yearsList = new ArrayList<>();
        int yearDiffRange = 200;
        startingYearOnYearSpinner = date.getYear() - yearDiffRange / 2;
        for (int i = startingYearOnYearSpinner; i < startingYearOnYearSpinner + yearDiffRange; ++i) {
            yearsList.add(utils.formatNumber(i));
        }
        yearSpinner.setAdapter(new ShapedArrayAdapter(getContext(), DROPDOWN_LAYOUT, yearsList));
        yearSpinner.setSelection(yearDiffRange / 2);
        //

        // month spinner init.
        List<String> monthsList = utils.getMonthsNamesListWithOrdinal(date);
        monthSpinner.setAdapter(new ShapedArrayAdapter(getContext(), DROPDOWN_LAYOUT, monthsList));
        monthSpinner.setSelection(date.getMonth() - 1);
        //

        // days spinner init.
        List<String> daysList = new ArrayList<>();
        for (int i = 1; i <= 31; ++i) {
            daysList.add(utils.formatNumber(i));
        }
        daySpinner.setAdapter(new ShapedArrayAdapter(getContext(), DROPDOWN_LAYOUT, daysList));
        daySpinner.setSelection(date.getDayOfMonth() - 1);
        //

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        fillYearMonthDaySpinners();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) { }
}
