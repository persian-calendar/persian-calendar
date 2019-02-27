package com.byagowi.persiancalendar.ui.calendar.month;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.calendar.AbstractDate;
import com.byagowi.persiancalendar.databinding.FragmentMonthBinding;
import com.byagowi.persiancalendar.di.dependencies.AppDependency;
import com.byagowi.persiancalendar.di.dependencies.CalendarFragmentDependency;
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency;
import com.byagowi.persiancalendar.entities.DayItem;
import com.byagowi.persiancalendar.ui.calendar.CalendarFragment;
import com.byagowi.persiancalendar.ui.calendar.CalendarFragmentModel;
import com.byagowi.persiancalendar.utils.CalendarType;
import com.byagowi.persiancalendar.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import dagger.android.support.DaggerFragment;

public class MonthFragment extends DaggerFragment {
    @Inject
    AppDependency appDependency;
    @Inject
    MainActivityDependency mainActivityDependency;
    @Inject
    CalendarFragmentDependency calendarFragmentDependency;

    public static AbstractDate getDateFromOffset(CalendarType calendar, int offset) {
        AbstractDate date = Utils.getTodayOfCalendar(calendar);
        int month = date.getMonth() - offset;
        month -= 1;
        int year = date.getYear();

        year = year + (month / 12);
        month = month % 12;
        if (month < 0) {
            year -= 1;
            month += 12;
        }
        month += 1;
        return Utils.getDateOfCalendar(calendar, year, month, 1);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentMonthBinding fragmentMonthBinding = FragmentMonthBinding.inflate(inflater,
                container, false);
        CalendarFragment calendarFragment = calendarFragmentDependency.getCalendarFragment();
        boolean isRTL = Utils.isRTL(mainActivityDependency.getMainActivity());
        Bundle args = getArguments();
        int offset = args == null ? 0 : args.getInt(Constants.OFFSET_ARGUMENT);

        fragmentMonthBinding.next.setImageResource(isRTL
                ? R.drawable.ic_keyboard_arrow_left
                : R.drawable.ic_keyboard_arrow_right);
        fragmentMonthBinding.next.setOnClickListener(v -> calendarFragment.changeMonth(isRTL ? -1 : 1));

        fragmentMonthBinding.prev.setImageResource(isRTL
                ? R.drawable.ic_keyboard_arrow_right
                : R.drawable.ic_keyboard_arrow_left);
        fragmentMonthBinding.prev.setOnClickListener(v -> calendarFragment.changeMonth(isRTL ? 1 : -1));

        fragmentMonthBinding.monthDays.setHasFixedSize(true);


        fragmentMonthBinding.monthDays.setLayoutManager(new GridLayoutManager(mainActivityDependency.getMainActivity(),
                Utils.isWeekOfYearEnabled() ? 8 : 7));
        ///////
        ///////
        ///////
        CalendarType mainCalendar = Utils.getMainCalendar();
        List<DayItem> days = new ArrayList<>();

        AbstractDate date = getDateFromOffset(mainCalendar, offset);
        long baseJdn = date.toJdn();
        int monthLength = Utils.getMonthLength(mainCalendar, date.getYear(), date.getMonth());

        int dayOfWeek = Utils.getDayOfWeekFromJdn(baseJdn);

        long todayJdn = Utils.getTodayJdn();
        for (int i = 0; i < monthLength; i++) {
            long jdn = baseJdn + i;
            days.add(new DayItem(jdn == todayJdn, jdn, dayOfWeek));
            dayOfWeek++;
            if (dayOfWeek == 7) {
                dayOfWeek = 0;
            }
        }

        long startOfYearJdn = Utils.getDateOfCalendar(mainCalendar, date.getYear(), 1, 1).toJdn();
        int weekOfYearStart = Utils.calculateWeekOfYear(baseJdn, startOfYearJdn);
        int weeksCount = 1 + Utils.calculateWeekOfYear(baseJdn + monthLength - 1, startOfYearJdn) - weekOfYearStart;

        int startingDayOfWeek = Utils.getDayOfWeekFromJdn(baseJdn);
        ///////
        ///////
        ///////

        CalendarFragmentModel calendarFragmentModel = ViewModelProviders.of(calendarFragment).get(CalendarFragmentModel.class);

        MonthAdapter adapter = new MonthAdapter(calendarFragmentDependency, days,
                startingDayOfWeek, weekOfYearStart, weeksCount);
        fragmentMonthBinding.monthDays.setAdapter(adapter);
        fragmentMonthBinding.monthDays.setItemAnimator(null);

        if (calendarFragmentModel.isTheFirstTime &&
                offset == 0 && calendarFragment.getViewPagerPosition() == offset) {
            calendarFragmentModel.isTheFirstTime = false;
            calendarFragmentModel.selectDay(Utils.getTodayJdn());
            updateTitle(date);
        }

        calendarFragmentModel.monthFragmentsHandler.observe(this, command -> {
            if (command.target == offset) {
                long jdn = command.currentlySelectedJdn;

                if (command.isEventsModification) {
                    adapter.initializeMonthEvents(mainActivityDependency.getMainActivity());
                    calendarFragmentModel.selectDay(jdn);
                } else {
                    adapter.selectDay(-1);
                    updateTitle(date);
                }

                long selectedDay = 1 + jdn - baseJdn;
                if (jdn != -1 && jdn >= baseJdn && selectedDay <= monthLength) {
                    adapter.selectDay((int) (1 + jdn - baseJdn));
                }
            } else {
                adapter.selectDay(-1);
            }
        });

        return fragmentMonthBinding.getRoot();
    }

    private void updateTitle(AbstractDate date) {
        mainActivityDependency.getMainActivity().setTitleAndSubtitle(
                Utils.getMonthName(date),
                Utils.formatNumber(date.getYear()));
    }
}
