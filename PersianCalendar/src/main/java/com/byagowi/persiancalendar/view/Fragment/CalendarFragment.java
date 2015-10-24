package com.byagowi.persiancalendar.view.Fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.Utils;
import com.byagowi.persiancalendar.locale.CalendarStrings;
import com.byagowi.persiancalendar.view.MonthFragment;

import calendar.CivilDate;
import calendar.DateConverter;
import calendar.PersianDate;

/**
 * Created by behdad on 10/15/15.
 */
public class CalendarFragment extends Fragment {

    public Utils utils = Utils.getInstance();

    private ViewPager viewPager;
    private TextView calendarInfo;
    private Button resetButton;
    //    private PrayTimeActivityHelper prayTimeActivityHelper;
    private static final int MONTHS_LIMIT = 1200;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);


        calendarInfo = (TextView) view.findViewById(R.id.calendar_info);

        // Pray Time
//        prayTimeActivityHelper = new PrayTimeActivityHelper(getActivity());
//        prayTimeActivityHelper.fillPrayTime();

        // Load Holidays
        utils.loadHolidays(getResources().openRawResource(R.raw.holidays));

        // Reset button
        resetButton = (Button) view.findViewById(R.id.reset_button);
        resetButton.setText(utils.getString(CalendarStrings.TODAY));
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                bringTodayYearMonth();
                setFocusedDay(Utils.getToday());
            }
        });
        resetButton.setTypeface(Typeface.createFromAsset(this.getContext().getAssets(),
                "fonts/NotoNaskhArabic-Regular.ttf"));

        // Initializing the viewPager
        viewPager = (ViewPager) view.findViewById(R.id.calendar_pager);
        viewPager.setAdapter(createCalendarAdaptor());
        viewPager.setCurrentItem(MONTHS_LIMIT / 2);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                updateResetButtonState();
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }
        });

        fillCalendarInfo(Utils.getToday());
        utils.setAthanRepeater(getContext());

        return view;
    }


    private void updateResetButtonState() {
        if (viewPager.getCurrentItem() == MONTHS_LIMIT / 2) {
            resetButton.setVisibility(View.GONE);
            fillCalendarInfo(Utils.getToday());
        } else {
            resetButton.setVisibility(View.VISIBLE);
            clearInfo();
        }
    }

    private void clearInfo() {
        calendarInfo.setText("");
//        prayTimeActivityHelper.clearInfo();
    }

    private void bringTodayYearMonth() {
        if (viewPager.getCurrentItem() != MONTHS_LIMIT / 2) {
            viewPager.setCurrentItem(MONTHS_LIMIT / 2);
            fillCalendarInfo(Utils.getToday());
        }
    }


    private void fillCalendarInfo(PersianDate persianDate) {
        CivilDate civilDate = DateConverter.persianToCivil(persianDate);
        char[] digits = utils.preferredDigits(getContext());
        utils.prepareTextView(calendarInfo);
        StringBuilder sb = new StringBuilder();

        if (isToday(civilDate)) {
            sb.append(utils.getString(CalendarStrings.TODAY)).append(":\n");
            resetButton.setVisibility(View.GONE);
        } else {
            resetButton.setVisibility(View.VISIBLE);
        }

        sb.append(utils.getWeekDayName(persianDate)).append(Utils.PERSIAN_COMMA)
                .append(" ")
                .append(utils.dateToString(persianDate, digits))
                .append("\n\n")
                .append(utils.getString(CalendarStrings.EQUALS_WITH))
                .append(":\n")
                .append(utils.dateToString(civilDate, digits))
                .append("\n")
                .append(utils.dateToString(DateConverter.civilToIslamic(civilDate), digits))
                .append("\n");
        calendarInfo.setText(Utils.textShaper(sb.toString()));

//        prayTimeActivityHelper.setDate(civilDate.getYear(),
//                civilDate.getMonth() - 1, civilDate.getDayOfMonth());
//        prayTimeActivityHelper.fillPrayTime();
    }


    private boolean isToday(CivilDate civilDate) {
        CivilDate today = new CivilDate();
        return today.getYear() == civilDate.getYear()
                && today.getMonth() == civilDate.getMonth()
                && today.getDayOfMonth() == civilDate.getDayOfMonth();
    }


    public void setFocusedDay(PersianDate persianDate) {
        fillCalendarInfo(persianDate);
    }

    private PagerAdapter createCalendarAdaptor() {
        return new FragmentPagerAdapter(
                getActivity().getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return MONTHS_LIMIT;
            }

            @Override
            public Fragment getItem(int position) {
                MonthFragment fragment = new MonthFragment();
                Bundle args = new Bundle();
                args.putInt("offset", position - MONTHS_LIMIT / 2);
                fragment.setArguments(args);
                return fragment;
            }
        };
    }
}
