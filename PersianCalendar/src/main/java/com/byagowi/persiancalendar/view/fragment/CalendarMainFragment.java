package com.byagowi.persiancalendar.view.fragment;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.adapter.CalendarAdapter;
import com.github.praytimes.Clock;
import com.github.praytimes.Coordinate;
import com.github.praytimes.PrayTime;
import com.github.praytimes.PrayTimesCalculator;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import calendar.CivilDate;
import calendar.DateConverter;
import calendar.PersianDate;

public class CalendarMainFragment extends Fragment
        implements View.OnClickListener, ViewPager.OnPageChangeListener {
    public static int viewPagerPosition;
    private ViewPager monthViewPager;
    private Utils utils;

    private Calendar calendar = Calendar.getInstance();

    private Coordinate coordinate;

    private PrayTimesCalculator prayTimesCalculator;
    private TextView athan1;
    private TextView athan2;
    private TextView athan3;
    private TextView athan4;
    private TextView athan5;
    private TextView aftab1;
    private TextView aftab2;
    private TextView aftab3;

    private TextView weekDayName;
    private TextView georgianDate;
    private TextView islamicDate;
    private TextView shamsiDate;
    private TextView eventTitle;
    private TextView holidayTitle;
    private TextView today;
    private AppCompatImageView todayIcon;

    private AppCompatImageView moreOwghat;

    private CardView owghat;
    private CardView event;

    private RelativeLayout owghat1;
    private RelativeLayout owghat2;
    private RelativeLayout owghat3;
    private RelativeLayout owghat4;
    private RelativeLayout owghat5;
    private RelativeLayout owghat6;
    private RelativeLayout owghat7;
    private RelativeLayout owghat8;

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        utils = Utils.getInstance(getContext());
        viewPagerPosition = 0;

        owghat1 = (RelativeLayout) view.findViewById(R.id.owghat1);
        owghat2 = (RelativeLayout) view.findViewById(R.id.owghat2);
        owghat3 = (RelativeLayout) view.findViewById(R.id.owghat3);
        owghat4 = (RelativeLayout) view.findViewById(R.id.owghat4);
        owghat5 = (RelativeLayout) view.findViewById(R.id.owghat5);
        owghat6 = (RelativeLayout) view.findViewById(R.id.owghat6);
        owghat7 = (RelativeLayout) view.findViewById(R.id.owghat7);
        owghat8 = (RelativeLayout) view.findViewById(R.id.owghat8);

        georgianDate = (TextView) view.findViewById(R.id.georgian_date);
        utils.prepareTextView(georgianDate);
        islamicDate = (TextView) view.findViewById(R.id.islamic_date);
        utils.prepareTextView(islamicDate);
        shamsiDate = (TextView) view.findViewById(R.id.shamsi_date);
        utils.prepareTextView(shamsiDate);
        weekDayName = (TextView) view.findViewById(R.id.week_day_name);
        utils.prepareTextView(weekDayName);
        today = (TextView) view.findViewById(R.id.today);
        todayIcon = (AppCompatImageView) view.findViewById(R.id.today_icon);

        athan1 = (TextView) view.findViewById(R.id.azan1);
        utils.prepareTextView(athan1);
        utils.prepareShapeTextView((TextView) view.findViewById(R.id.azan1text));

        athan2 = (TextView) view.findViewById(R.id.azan2);
        utils.prepareTextView(athan2);
        utils.prepareShapeTextView((TextView) view.findViewById(R.id.azan2text));

        athan3 = (TextView) view.findViewById(R.id.azan3);
        utils.prepareTextView(athan3);
        utils.prepareShapeTextView((TextView) view.findViewById(R.id.azan3text));

        athan4 = (TextView) view.findViewById(R.id.azan4);
        utils.prepareTextView(athan4);
        utils.prepareShapeTextView((TextView) view.findViewById(R.id.azan4text));

        athan5 = (TextView) view.findViewById(R.id.azan5);
        utils.prepareTextView(athan5);
        utils.prepareShapeTextView((TextView) view.findViewById(R.id.azan5text));

        aftab1 = (TextView) view.findViewById(R.id.aftab1);
        utils.prepareTextView(aftab1);
        utils.prepareShapeTextView((TextView) view.findViewById(R.id.aftab1text));

        aftab2 = (TextView) view.findViewById(R.id.aftab2);
        utils.prepareTextView(aftab2);
        utils.prepareShapeTextView((TextView) view.findViewById(R.id.aftab2text));

        aftab3 = (TextView) view.findViewById(R.id.aftab3);
        utils.prepareTextView(aftab3);
        utils.prepareShapeTextView((TextView) view.findViewById(R.id.aftab3text));


        moreOwghat = (AppCompatImageView) view.findViewById(R.id.more_owghat);

        eventTitle = (TextView) view.findViewById(R.id.event_title);
        holidayTitle = (TextView) view.findViewById(R.id.holiday_title);
        utils.prepareTextView(holidayTitle);

        owghat = (CardView) view.findViewById(R.id.owghat);
        event = (CardView) view.findViewById(R.id.event);

        monthViewPager = (ViewPager) view.findViewById(R.id.calendar_pager);

        coordinate = utils.getCoordinate();
        prayTimesCalculator = new PrayTimesCalculator(utils.getCalculationMethod());

        monthViewPager.setAdapter(new CalendarAdapter(getActivity().getSupportFragmentManager()));
        monthViewPager.setCurrentItem(Constants.MONTHS_LIMIT / 2);

        monthViewPager.addOnPageChangeListener(this);

        owghat.setOnClickListener(this);
        today.setOnClickListener(this);
        todayIcon.setOnClickListener(this);
        georgianDate.setOnClickListener(this);
        islamicDate.setOnClickListener(this);
        shamsiDate.setOnClickListener(this);

        utils.prepareShapeTextView((TextView) view.findViewById(R.id.event_card_title));
        utils.prepareShapeTextView((TextView) view.findViewById(R.id.today));
        utils.prepareShapeTextView((TextView) view.findViewById(R.id.owghat_text));

        // This will immediately be replaced by the same functionallity on fragment but is here to
        // make sure enough space is dedicated to actionbar's title and subtitle, kinda hack anyway
        PersianDate today = utils.getToday();
        utils.setActivityTitleAndSubtitle(getActivity(), utils.getMonthName(today),
                utils.formatNumber(today.getYear()));

        return view;
    }

    public void changeMonth(int position) {
        monthViewPager.setCurrentItem(monthViewPager.getCurrentItem() + position, true);
    }

    public void selectDay(PersianDate persianDate) {
        CivilDate civilDate = DateConverter.persianToCivil(persianDate);
        weekDayName.setText(utils.shape(utils.getWeekDayName(persianDate)));
        shamsiDate.setText(utils.shape(utils.dateToString(persianDate)));
        georgianDate.setText(utils.shape(utils.dateToString(civilDate)));
        islamicDate.setText(utils.shape(utils.dateToString(
                DateConverter.civilToIslamic(civilDate, utils.getIslamicOffset()))));

        if (isToday(civilDate)) {
            today.setVisibility(View.GONE);
            todayIcon.setVisibility(View.GONE);
        } else {
            today.setVisibility(View.VISIBLE);
            todayIcon.setVisibility(View.VISIBLE);
        }

        setOwghat(civilDate);
        showEvent(persianDate);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void addEventOnCalendar(PersianDate persianDate) {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(CalendarContract.Events.CONTENT_URI);

        CivilDate civil = DateConverter.persianToCivil(persianDate);

        intent.putExtra(CalendarContract.Events.DESCRIPTION,
                utils.dayTitleSummary(persianDate));

        Calendar time = Calendar.getInstance();
        time.set(civil.getYear(), civil.getMonth() - 1, civil.getDayOfMonth());

        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                time.getTimeInMillis());
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                time.getTimeInMillis());
        intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);

        startActivity(intent);
    }

    private void showEvent(PersianDate persianDate) {
        String holidays = utils.getEventsTitle(persianDate, true);
        String events = utils.getEventsTitle(persianDate, false);

        event.setVisibility(View.GONE);
        holidayTitle.setVisibility(View.GONE);
        eventTitle.setVisibility(View.GONE);

        if (!TextUtils.isEmpty(holidays)) {
            holidayTitle.setText(utils.shape(holidays));
            holidayTitle.setVisibility(View.VISIBLE);
            event.setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(events)) {
            eventTitle.setText(utils.shape(events));
            eventTitle.setVisibility(View.VISIBLE);
            event.setVisibility(View.VISIBLE);
        }
    }

    private void setOwghat(CivilDate civilDate) {
        if (coordinate == null) {
            return;
        }

        calendar.set(civilDate.getYear(), civilDate.getMonth() - 1, civilDate.getDayOfMonth());
        Date date = calendar.getTime();

        Map<PrayTime, Clock> prayTimes = prayTimesCalculator.calculate(date, coordinate);

        athan1.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.IMSAK)));
        aftab1.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.SUNRISE)));
        athan2.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.DHUHR)));
        athan3.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.ASR)));
        aftab2.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.SUNSET)));
        athan4.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.MAGHRIB)));
        athan5.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.ISHA)));
        aftab3.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.MIDNIGHT)));

        owghat.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.owghat:
                owghat1.setVisibility(View.VISIBLE);
                owghat2.setVisibility(View.VISIBLE);
                owghat3.setVisibility(View.VISIBLE);
                owghat4.setVisibility(View.VISIBLE);
                owghat5.setVisibility(View.VISIBLE);
                owghat6.setVisibility(View.VISIBLE);
                owghat7.setVisibility(View.VISIBLE);
                owghat8.setVisibility(View.VISIBLE);

                moreOwghat.setVisibility(View.GONE);
                break;

            case R.id.today:
                bringTodayYearMonth();
                break;

            case R.id.today_icon:
                bringTodayYearMonth();
                break;

            case R.id.islamic_date:
                utils.copyToClipboard(v);
                break;

            case R.id.shamsi_date:
                utils.copyToClipboard(v);
                break;

            case R.id.georgian_date:
                utils.copyToClipboard(v);
                break;
        }
    }

    private void bringTodayYearMonth() {
        Intent intent = new Intent(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT); //todo use fragment tag
        intent.putExtra(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT,
                Constants.BROADCAST_TO_MONTH_FRAGMENT_RESET_DAY);

        getContext().sendBroadcast(intent);

        if (monthViewPager.getCurrentItem() != Constants.MONTHS_LIMIT / 2) {
            monthViewPager.setCurrentItem(Constants.MONTHS_LIMIT / 2);
        }

        selectDay(utils.getToday());
    }

    private boolean isToday(CivilDate civilDate) {
        CivilDate today = new CivilDate();
        return today.getYear() == civilDate.getYear()
                && today.getMonth() == civilDate.getMonth()
                && today.getDayOfMonth() == civilDate.getDayOfMonth();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        viewPagerPosition = position - Constants.MONTHS_LIMIT / 2;
        Intent intent = new Intent(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT);//todo use fragment tag
        intent.putExtra(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT, position - Constants.MONTHS_LIMIT / 2);
        getContext().sendBroadcast(intent);

        today.setVisibility(View.VISIBLE);
        todayIcon.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}
