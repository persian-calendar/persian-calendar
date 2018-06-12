package com.byagowi.persiancalendar.view.fragment;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.adapter.CalendarAdapter;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.dialog.SelectDayDialog;
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

public class CalendarFragment extends Fragment
        implements View.OnClickListener, ViewPager.OnPageChangeListener {
    private ViewPager monthViewPager;
    private Utils utils;

    private Calendar calendar = Calendar.getInstance();

    private Coordinate coordinate;

    private PrayTimesCalculator prayTimesCalculator;
    private TextView imsakTextView;
    private TextView fajrTextView;
    private TextView dhuhrTextView;
    private TextView asrTextView;
    private TextView maghribTextView;
    private TextView ishaTextView;
    private TextView sunriseTextView;
    private TextView sunsetTextView;
    private TextView midnightTextView;

    private TextView weekDayName;
    private TextView gregorianDate;
    private TextView islamicDate;
    private TextView shamsiDate;
    private TextView eventTitle;
    private TextView holidayTitle;
    private TextView today;
    private AppCompatImageView todayIcon;

    private AppCompatImageView moreOwghat;

    private CardView owghat;
    private CardView event;

    private RelativeLayout imsakLayout;
    private RelativeLayout fajrLayout;
    private RelativeLayout sunriseLayout;
    private RelativeLayout dhuhrLayout;
    private RelativeLayout asrLayout;
    private RelativeLayout sunsetLayout;
    private RelativeLayout maghribLayout;
    private RelativeLayout ishaLayout;
    private RelativeLayout midnightLayout;

    private int viewPagerPosition;

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        utils = Utils.getInstance(getContext());
        utils.clearYearWarnFlag();
        viewPagerPosition = 0;

        imsakLayout = view.findViewById(R.id.imsakLayout);
        fajrLayout = view.findViewById(R.id.fajrLayout);
        sunriseLayout = view.findViewById(R.id.sunriseLayout);
        dhuhrLayout = view.findViewById(R.id.dhuhrLayout);
        asrLayout = view.findViewById(R.id.asrLayout);
        sunsetLayout = view.findViewById(R.id.sunsetLayout);
        maghribLayout = view.findViewById(R.id.maghribLayout);
        ishaLayout = view.findViewById(R.id.ishaLayout);
        midnightLayout = view.findViewById(R.id.midnightLayout);

        gregorianDate = view.findViewById(R.id.gregorian_date);
        islamicDate = view.findViewById(R.id.islamic_date);
        shamsiDate = view.findViewById(R.id.shamsi_date);
        weekDayName = view.findViewById(R.id.week_day_name);
        today = view.findViewById(R.id.today);
        todayIcon = view.findViewById(R.id.today_icon);

        imsakTextView = view.findViewById(R.id.imsak);
        fajrTextView = view.findViewById(R.id.fajr);
        dhuhrTextView = view.findViewById(R.id.dhuhr);
        asrTextView = view.findViewById(R.id.asr);
        maghribTextView = view.findViewById(R.id.maghrib);
        ishaTextView = view.findViewById(R.id.isgha);
        sunriseTextView = view.findViewById(R.id.sunrise);
        sunsetTextView = view.findViewById(R.id.sunset);
        midnightTextView = view.findViewById(R.id.midnight);


        moreOwghat = view.findViewById(R.id.more_owghat);

        eventTitle = view.findViewById(R.id.event_title);
        holidayTitle = view.findViewById(R.id.holiday_title);

        owghat = view.findViewById(R.id.owghat);
        event = view.findViewById(R.id.cardEvent);

        monthViewPager = view.findViewById(R.id.calendar_pager);

        coordinate = utils.getCoordinate();
        prayTimesCalculator = new PrayTimesCalculator(utils.getCalculationMethod());

        monthViewPager.setAdapter(new CalendarAdapter(getChildFragmentManager()));
        monthViewPager.setCurrentItem(Constants.MONTHS_LIMIT / 2);

        monthViewPager.addOnPageChangeListener(this);

        owghat.setOnClickListener(this);
        today.setOnClickListener(this);
        todayIcon.setOnClickListener(this);
        gregorianDate.setOnClickListener(this);
        islamicDate.setOnClickListener(this);
        shamsiDate.setOnClickListener(this);

        String cityName = utils.getCityName(false);
        if (!TextUtils.isEmpty(cityName)) {
            ((TextView) view.findViewById(R.id.owghat_text))
                    .append(" (" + cityName + ")");
        }

        // This will immediately be replaced by the same functionality on fragment but is here to
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
        weekDayName.setText(utils.getWeekDayName(persianDate));
        shamsiDate.setText(utils.dateToString(persianDate));
        CivilDate civilDate = DateConverter.persianToCivil(persianDate);
        gregorianDate.setText(utils.dateToString(civilDate));
        islamicDate.setText(utils.dateToString(
                DateConverter.civilToIslamic(civilDate, utils.getIslamicOffset())));

        if (utils.getToday().equals(persianDate)) {
            today.setVisibility(View.GONE);
            todayIcon.setVisibility(View.GONE);
            if (utils.iranTime) {
                weekDayName.setText(weekDayName.getText() +
                        " (" + getString(R.string.iran_time) + ")");
            }
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
            holidayTitle.setText(holidays);
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

        imsakTextView.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.IMSAK)));
        fajrTextView.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.FAJR)));
        sunriseTextView.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.SUNRISE)));
        dhuhrTextView.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.DHUHR)));
        asrTextView.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.ASR)));
        sunsetTextView.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.SUNSET)));
        maghribTextView.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.MAGHRIB)));
        ishaTextView.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.ISHA)));
        midnightTextView.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.MIDNIGHT)));

        owghat.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.owghat:

                if (sunriseLayout.getVisibility() == View.VISIBLE) {
                    sunriseLayout.setVisibility(View.GONE);
                    dhuhrLayout.setVisibility(View.GONE);
                    asrLayout.setVisibility(View.GONE);
                    sunsetLayout.setVisibility(View.GONE);
                    maghribLayout.setVisibility(View.GONE);
                    ishaLayout.setVisibility(View.GONE);
                    midnightLayout.setVisibility(View.GONE);
                    moreOwghat.setImageResource(R.drawable.ic_keyboard_arrow_down);
                } else {
                    imsakLayout.setVisibility(View.VISIBLE);
                    fajrLayout.setVisibility(View.VISIBLE);
                    sunriseLayout.setVisibility(View.VISIBLE);
                    dhuhrLayout.setVisibility(View.VISIBLE);
                    asrLayout.setVisibility(View.VISIBLE);
                    sunsetLayout.setVisibility(View.VISIBLE);
                    maghribLayout.setVisibility(View.VISIBLE);
                    ishaLayout.setVisibility(View.VISIBLE);
                    midnightLayout.setVisibility(View.VISIBLE);
                    moreOwghat.setImageResource(R.drawable.ic_keyboard_arrow_up);
                }

                break;

            case R.id.today:
            case R.id.today_icon:
                bringTodayYearMonth();
                break;

            case R.id.islamic_date:
            case R.id.shamsi_date:
            case R.id.gregorian_date:
                utils.copyToClipboard(v);
                break;
        }
    }

    private void bringTodayYearMonth() {
        Intent intent = new Intent(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT);
        intent.putExtra(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT,
                Constants.BROADCAST_TO_MONTH_FRAGMENT_RESET_DAY);
        intent.putExtra(Constants.BROADCAST_FIELD_SELECT_DAY, -1);

        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

        if (monthViewPager.getCurrentItem() != Constants.MONTHS_LIMIT / 2) {
            monthViewPager.setCurrentItem(Constants.MONTHS_LIMIT / 2);
        }

        selectDay(utils.getToday());
    }

    public void bringDate(PersianDate date) {
        PersianDate today = utils.getToday();
        viewPagerPosition =
                (today.getYear() - date.getYear()) * 12 + today.getMonth() - date.getMonth();

        monthViewPager.setCurrentItem(viewPagerPosition + Constants.MONTHS_LIMIT / 2);

        Intent intent = new Intent(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT);
        intent.putExtra(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT, viewPagerPosition);
        intent.putExtra(Constants.BROADCAST_FIELD_SELECT_DAY, date.getDayOfMonth());

        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

        selectDay(date);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        viewPagerPosition = position - Constants.MONTHS_LIMIT / 2;

        Intent intent = new Intent(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT);
        intent.putExtra(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT, viewPagerPosition);
        intent.putExtra(Constants.BROADCAST_FIELD_SELECT_DAY, -1);

        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

        today.setVisibility(View.VISIBLE);
        todayIcon.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.action_button, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.go_to:
                SelectDayDialog dialog = new SelectDayDialog();
                dialog.show(getChildFragmentManager(), SelectDayDialog.class.getName());
                break;
            default:
                break;
        }
        return true;
    }

    public int getViewPagerPosition() {
        return viewPagerPosition;
    }
}
