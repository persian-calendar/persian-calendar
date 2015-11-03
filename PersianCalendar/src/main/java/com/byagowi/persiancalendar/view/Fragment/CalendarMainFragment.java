package com.byagowi.persiancalendar.view.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.byagowi.persiancalendar.Adapter.CalendarAdapter;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.Utils;

import calendar.CivilDate;
import calendar.DateConverter;
import calendar.PersianDate;

public class CalendarMainFragment extends Fragment  implements ViewPager.OnPageChangeListener, View.OnClickListener {
    public static final int MONTHS_LIMIT = 1200;
    private ViewPager viewPager;
    private final Utils utils = Utils.getInstance();
    private RelativeLayout infoDay;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_calendar, container, false);

        infoDay = (RelativeLayout) view.findViewById(R.id.info_day);

        utils.loadHolidays(getResources().openRawResource(R.raw.holidays));

        infoDay.setOnClickListener(this);

        viewPager = (ViewPager) view.findViewById(R.id.calendar_pager);
        viewPager.setAdapter(new CalendarAdapter(getActivity().getSupportFragmentManager(), this));
        viewPager.setCurrentItem(MONTHS_LIMIT / 2);
        viewPager.addOnPageChangeListener(this);

        return view;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public void changeMonth(int position) {
        viewPager.setCurrentItem(viewPager.getCurrentItem() + position, true);
    }

    public void selectDay(PersianDate persianDate) {
        CivilDate civilDate = DateConverter.persianToCivil(persianDate);
        char[] digits = utils.preferredDigits(getContext());

        View view = getView();
        if (view == null)
            return;

        TextView weekDayName = (TextView) view.findViewById(R.id.week_day_name);
        TextView shamsiDate = (TextView) view.findViewById(R.id.shamsi_date);
        TextView miladiDate = (TextView) view.findViewById(R.id.miladi_date);
        TextView ghamariDate = (TextView) view.findViewById(R.id.ghamari_date);

        weekDayName.setText(utils.getWeekDayName(persianDate));
        shamsiDate.setText(utils.dateToString(persianDate, digits));
        miladiDate.setText(utils.dateToString(civilDate, digits));
        ghamariDate.setText(utils.dateToString(DateConverter.civilToIslamic(civilDate), digits));
    }

    @Override
    public void onClick(View v) {
        View view = getView();
        if (view == null)
            return;

        TextView miladiDate = (TextView) view.findViewById(R.id.miladi_date);
        TextView ghamariDate = (TextView) view.findViewById(R.id.ghamari_date);

        miladiDate.setVisibility(View.VISIBLE);
        ghamariDate.setVisibility(View.VISIBLE);
    }
}
