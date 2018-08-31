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
import com.byagowi.persiancalendar.entity.CalendarTypeEntity;
import com.byagowi.persiancalendar.entity.FormattedIntEntity;
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
        LayoutInflater inflater = getActivity().getLayoutInflater();

        Bundle args = getArguments();
        long jdn = args.getLong(BUNDLE_KEY, -1);

        SelectdayFragmentBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.selectday_fragment, null, false);

        binding.calendarTypeSpinner.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                Utils.getOrderedCalendarEntities(getContext())));

        binding.calendarTypeSpinner.setSelection(0);
        UIUtils.fillSelectDaySpinners(getContext(), binding, jdn);

        binding.calendarTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                UIUtils.fillSelectDaySpinners(getContext(), binding, jdn);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        return new AlertDialog.Builder(getActivity())
                .setView(binding.getRoot())
                .setCustomTitle(null)
                .setPositiveButton(R.string.go, (dialogInterface, i) -> {

                    int year = ((FormattedIntEntity)
                            binding.yearSpinner.getSelectedItem()).getValue();
                    int month = ((FormattedIntEntity)
                            binding.monthSpinner.getSelectedItem()).getValue();
                    int day = ((FormattedIntEntity)
                            binding.daySpinner.getSelectedItem()).getValue();

                    CalendarFragment calendarFragment = (CalendarFragment) getActivity()
                            .getSupportFragmentManager()
                            .findFragmentByTag(CalendarFragment.class.getName());

                    try {
                        switch (((CalendarTypeEntity)
                                binding.calendarTypeSpinner.getSelectedItem()).getType()) {
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
                }).create();
    }
}
