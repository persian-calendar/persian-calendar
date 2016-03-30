package com.byagowi.persiancalendar.view.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.adapter.ShapedArrayAdapter;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.fragment.CalendarFragment;

import calendar.CivilDate;
import calendar.DateConverter;
import calendar.IslamicDate;
import calendar.PersianDate;

/**
 * Created by ebrahim on 3/20/16.
 */
public class SelectDayDialog extends AppCompatDialogFragment {
    private int startingYearOnYearSpinner = 0;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.selectday_fragment, null);

        final Utils utils = Utils.getInstance(getContext());

        // fill members
        final Spinner calendarTypeSpinner = (Spinner) view.findViewById(R.id.calendarTypeSpinner);
        final Spinner yearSpinner = (Spinner) view.findViewById(R.id.yearSpinner);
        final Spinner monthSpinner = (Spinner) view.findViewById(R.id.monthSpinner);
        final Spinner daySpinner = (Spinner) view.findViewById(R.id.daySpinner);

        utils.setFontAndShape((TextView) view.findViewById(R.id.converterLabelDay));
        utils.setFontAndShape((TextView) view.findViewById(R.id.converterLabelMonth));
        utils.setFontAndShape((TextView) view.findViewById(R.id.converterLabelYear));
        utils.setFontAndShape((TextView) view.findViewById(R.id.calendarTypeTitle));

        startingYearOnYearSpinner = utils.fillYearMonthDaySpinners(getContext(),
                calendarTypeSpinner, yearSpinner, monthSpinner, daySpinner);

        calendarTypeSpinner.setAdapter(new ShapedArrayAdapter<>(getContext(),
                Utils.DROPDOWN_LAYOUT, getResources().getStringArray(R.array.calendar_type)));
        calendarTypeSpinner.setSelection(0);

        calendarTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                startingYearOnYearSpinner = utils.fillYearMonthDaySpinners(getContext(),
                        calendarTypeSpinner, yearSpinner, monthSpinner, daySpinner);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

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
                        .findFragmentByTag(CalendarFragment.class.getName());

                try {
                    switch (utils.calendarTypeFromPosition(calendarTypeSpinner.getSelectedItemPosition())) {
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
                    }
                } catch (RuntimeException e) {
                    utils.quickToast(getString(R.string.date_exception));
                    Log.e("SelectDayDialog", "", e);
                }
            }
        });

        return builder.create();
    }
}
