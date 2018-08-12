package com.byagowi.persiancalendar.view.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.databinding.SelectdayFragmentBinding;
import com.byagowi.persiancalendar.util.CalendarUtils;
import com.byagowi.persiancalendar.util.UIUtils;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.fragment.CalendarFragment;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.databinding.DataBindingUtil;
import calendar.DateConverter;

/**
 * Created by ebrahim on 3/20/16.
 */
public class SelectDayDialog extends AppCompatDialogFragment {
    private int startingYearOnYearSpinner = 0;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        SelectdayFragmentBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.selectday_fragment, null, false);

        binding.calendarTypeSpinner.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.calendar_type)));

        binding.calendarTypeSpinner.setSelection(CalendarUtils.positionFromCalendarType(Utils.getMainCalendar()));
        startingYearOnYearSpinner = UIUtils.fillSelectdaySpinners(getContext(), binding, -1);


        binding.calendarTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                startingYearOnYearSpinner = UIUtils.fillSelectdaySpinners(getContext(), binding, -1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(binding.getRoot());
        builder.setCustomTitle(null);
        builder.setPositiveButton(R.string.go, (dialogInterface, i) -> {

            int year = startingYearOnYearSpinner + binding.yearSpinner.getSelectedItemPosition();
            int month = binding.monthSpinner.getSelectedItemPosition() + 1;
            int day = binding.daySpinner.getSelectedItemPosition() + 1;

            CalendarFragment calendarFragment = (CalendarFragment) getActivity()
                    .getSupportFragmentManager()
                    .findFragmentByTag(CalendarFragment.class.getName());

            try {
                switch (CalendarUtils.calendarTypeFromPosition(binding.calendarTypeSpinner.getSelectedItemPosition())) {
                    case GREGORIAN:
                        calendarFragment.bringDate(DateConverter.civilToJdn(year, month, day));
                        break;

                    case ISLAMIC:
                        calendarFragment.bringDate(DateConverter.islamicToJdn(year, month, day));
                        break;

                    case SHAMSI:
                        calendarFragment.bringDate(DateConverter.persianToJdn(year, month, day));
                        break;
                }
            } catch (RuntimeException e) {
                Toast.makeText(getContext(), getString(R.string.date_exception), Toast.LENGTH_SHORT).show();
                Log.e("SelectDayDialog", "", e);
            }
        });

        return builder.create();
    }
}
