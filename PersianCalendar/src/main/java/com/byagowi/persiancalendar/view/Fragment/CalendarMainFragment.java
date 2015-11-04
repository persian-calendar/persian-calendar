package com.byagowi.persiancalendar.view.Fragment;

import android.animation.ArgbEvaluator;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.byagowi.persiancalendar.Adapter.CalendarAdapter;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.Utils;

import calendar.CivilDate;
import calendar.DateConverter;
import calendar.PersianDate;

public class CalendarMainFragment extends Fragment implements ViewPager.OnPageChangeListener, View.OnClickListener {
    public static final int MONTHS_LIMIT = 1200;
    private ViewPager viewPager;
    private final Utils utils = Utils.getInstance();
    private RelativeLayout infoDay;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_calendar, container, false);

        utils.loadHolidays(getResources().openRawResource(R.raw.holidays));

        infoDay = (RelativeLayout) view.findViewById(R.id.info_day);
        infoDay.setOnClickListener(this);

        viewPager = (ViewPager) view.findViewById(R.id.calendar_pager);
        viewPager.setAdapter(new CalendarAdapter(getActivity().getSupportFragmentManager()));
        viewPager.setCurrentItem(MONTHS_LIMIT / 2);
        viewPager.addOnPageChangeListener(this);

        return view;
    }

    int[] colors = { 0xFF689F38, 0xFFFFEB3B, 0xFFFFB74D, 0xFF039BE5 };
    Toolbar toolbar;
    ArgbEvaluator argbEvaluator;
    Window window;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
            return;

        if (toolbar == null)
            toolbar = (Toolbar)
                    getActivity().findViewById(R.id.toolbar);

        if (argbEvaluator == null)
            argbEvaluator = new ArgbEvaluator();

        int color = (Integer) argbEvaluator.evaluate(positionOffset,
                colors[position % 4],
                colors[(position + 1) % 4]);

        toolbar.setBackgroundColor(color);

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return;

        if (window == null)
            window = getActivity().getWindow();

        window.setNavigationBarColor(color);
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
