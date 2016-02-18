package com.byagowi.persiancalendar.view.fragment;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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
import com.byagowi.persiancalendar.Utils;
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
    private final Utils utils = Utils.getInstance();

    private Calendar calendar = Calendar.getInstance();
    private char[] digits;
    private boolean clockIn24;

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

        viewPagerPosition = 0;

        owghat1 = (RelativeLayout) view.findViewById(R.id.owghat1);
        owghat2 = (RelativeLayout) view.findViewById(R.id.owghat2);
        owghat3 = (RelativeLayout) view.findViewById(R.id.owghat3);
        owghat4 = (RelativeLayout) view.findViewById(R.id.owghat4);
        owghat5 = (RelativeLayout) view.findViewById(R.id.owghat5);
        owghat6 = (RelativeLayout) view.findViewById(R.id.owghat6);
        owghat7 = (RelativeLayout) view.findViewById(R.id.owghat7);
        owghat8 = (RelativeLayout) view.findViewById(R.id.owghat8);

        FragmentActivity activity = getActivity();

        georgianDate = (TextView) view.findViewById(R.id.georgian_date);
        utils.prepareTextView(activity, georgianDate);
        islamicDate = (TextView) view.findViewById(R.id.islamic_date);
        utils.prepareTextView(activity, islamicDate);
        shamsiDate = (TextView) view.findViewById(R.id.shamsi_date);
        utils.prepareTextView(activity, shamsiDate);
        weekDayName = (TextView) view.findViewById(R.id.week_day_name);
        utils.prepareTextView(activity, weekDayName);
        today = (TextView) view.findViewById(R.id.today);
        todayIcon = (AppCompatImageView) view.findViewById(R.id.today_icon);

        athan1 = (TextView) view.findViewById(R.id.azan1);
        utils.prepareTextView(activity, athan1);
        utils.prepareShapeTextView(activity, (TextView) view.findViewById(R.id.azan1text));

        athan2 = (TextView) view.findViewById(R.id.azan2);
        utils.prepareTextView(activity, athan2);
        utils.prepareShapeTextView(activity, (TextView) view.findViewById(R.id.azan2text));

        athan3 = (TextView) view.findViewById(R.id.azan3);
        utils.prepareTextView(activity, athan3);
        utils.prepareShapeTextView(activity, (TextView) view.findViewById(R.id.azan3text));

        athan4 = (TextView) view.findViewById(R.id.azan4);
        utils.prepareTextView(activity, athan4);
        utils.prepareShapeTextView(activity, (TextView) view.findViewById(R.id.azan4text));

        athan5 = (TextView) view.findViewById(R.id.azan5);
        utils.prepareTextView(activity, athan5);
        utils.prepareShapeTextView(activity, (TextView) view.findViewById(R.id.azan5text));

        aftab1 = (TextView) view.findViewById(R.id.aftab1);
        utils.prepareTextView(activity, aftab1);
        utils.prepareShapeTextView(activity, (TextView) view.findViewById(R.id.aftab1text));

        aftab2 = (TextView) view.findViewById(R.id.aftab2);
        utils.prepareTextView(activity, aftab2);
        utils.prepareShapeTextView(activity, (TextView) view.findViewById(R.id.aftab2text));

        aftab3 = (TextView) view.findViewById(R.id.aftab3);
        utils.prepareTextView(activity, aftab3);
        utils.prepareShapeTextView(activity, (TextView) view.findViewById(R.id.aftab3text));


        moreOwghat = (AppCompatImageView) view.findViewById(R.id.more_owghat);

        eventTitle = (TextView) view.findViewById(R.id.event_title);
        holidayTitle = (TextView) view.findViewById(R.id.holiday_title);
        utils.prepareTextView(activity, holidayTitle);

        owghat = (CardView) view.findViewById(R.id.owghat);
        event = (CardView) view.findViewById(R.id.event);

        monthViewPager = (ViewPager) view.findViewById(R.id.calendar_pager);

        utils.loadHolidays(getResources().openRawResource(R.raw.holidays));
        utils.loadEvents(getResources().openRawResource(R.raw.events));

        digits = utils.preferredDigits(getContext());
        clockIn24 = utils.clockIn24(getContext());
        coordinate = utils.getCoordinate(getContext());
        prayTimesCalculator = new PrayTimesCalculator(utils.getCalculationMethod(getContext()));

        monthViewPager.setAdapter(new CalendarAdapter(activity.getSupportFragmentManager()));
        monthViewPager.setCurrentItem(Constants.MONTHS_LIMIT / 2);

        monthViewPager.addOnPageChangeListener(this);

        owghat.setOnClickListener(this);
        today.setOnClickListener(this);
        todayIcon.setOnClickListener(this);
        georgianDate.setOnClickListener(this);
        islamicDate.setOnClickListener(this);
        shamsiDate.setOnClickListener(this);

        utils.prepareShapeTextView(activity, (TextView) view.findViewById(R.id.event_card_title));
        utils.prepareShapeTextView(activity, (TextView) view.findViewById(R.id.today));
        utils.prepareShapeTextView(activity, (TextView) view.findViewById(R.id.owghat_text));

        return view;
    }

    public void changeMonth(int position) {
        monthViewPager.setCurrentItem(monthViewPager.getCurrentItem() + position, true);
    }

    public void selectDay(PersianDate persianDate) {
        CivilDate civilDate = DateConverter.persianToCivil(persianDate);
        weekDayName.setText(Utils.shape(utils.getWeekDayName(persianDate)));
        shamsiDate.setText(Utils.shape(utils.dateToString(persianDate, digits)));
        georgianDate.setText(Utils.shape(utils.dateToString(civilDate, digits)));
        islamicDate.setText(Utils.shape(utils.dateToString(
                DateConverter.civilToIslamic(
                        civilDate, Utils.getIslamicOffset(getContext())),
                digits)));

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
                utils.dayTitleSummary(persianDate, digits));

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
        String holidays = utils.getHolidayTitle(persianDate);
        String events = utils.getEventTitle(persianDate);

        event.setVisibility(View.GONE);
        holidayTitle.setVisibility(View.GONE);
        eventTitle.setVisibility(View.GONE);

        if (holidays != null) {
            holidayTitle.setText(Utils.shape(holidays));
            holidayTitle.setVisibility(View.VISIBLE);
            event.setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(events)) {
            eventTitle.setText(events);
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

        athan1.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.IMSAK), digits, clockIn24));
        aftab1.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.SUNRISE), digits, clockIn24));
        athan2.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.DHUHR), digits, clockIn24));
        athan3.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.ASR), digits, clockIn24));
        aftab2.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.SUNSET), digits, clockIn24));
        athan4.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.MAGHRIB), digits, clockIn24));
        athan5.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.ISHA), digits, clockIn24));
        aftab3.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.MIDNIGHT), digits, clockIn24));

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
                Utils.copyToClipboard(getContext(), v);
                break;

            case R.id.shamsi_date:
                Utils.copyToClipboard(getContext(), v);
                break;

            case R.id.georgian_date:
                Utils.copyToClipboard(getContext(), v);
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

        selectDay(Utils.getToday());
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
