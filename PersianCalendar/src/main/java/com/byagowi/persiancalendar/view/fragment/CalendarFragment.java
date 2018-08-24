package com.byagowi.persiancalendar.view.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.adapter.CalendarAdapter;
import com.byagowi.persiancalendar.adapter.CardTabsAdapter;
import com.byagowi.persiancalendar.databinding.CalendarsTabContentBinding;
import com.byagowi.persiancalendar.databinding.EventsTabContentBinding;
import com.byagowi.persiancalendar.databinding.FragmentCalendarBinding;
import com.byagowi.persiancalendar.databinding.OwghatTabContentBinding;
import com.byagowi.persiancalendar.entity.AbstractEvent;
import com.byagowi.persiancalendar.entity.DeviceCalendarEvent;
import com.byagowi.persiancalendar.entity.GregorianCalendarEvent;
import com.byagowi.persiancalendar.entity.IslamicCalendarEvent;
import com.byagowi.persiancalendar.entity.PersianCalendarEvent;
import com.byagowi.persiancalendar.util.CalendarUtils;
import com.byagowi.persiancalendar.util.UIUtils;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.activity.MainActivity;
import com.byagowi.persiancalendar.view.dialog.SelectDayDialog;
import com.cepmuvakkit.times.posAlgo.SunMoonPosition;
import com.github.praytimes.Clock;
import com.github.praytimes.Coordinate;
import com.github.praytimes.PrayTime;
import com.github.praytimes.PrayTimesCalculator;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;
import calendar.AbstractDate;
import calendar.CalendarType;
import calendar.CivilDate;
import calendar.DateConverter;
import calendar.IslamicDate;
import calendar.PersianDate;

import static com.byagowi.persiancalendar.Constants.CALENDAR_EVENT_ADD_MODIFY_REQUEST_CODE;
import static com.byagowi.persiancalendar.Constants.PREF_HOLIDAY_TYPES;

public class CalendarFragment extends Fragment implements View.OnClickListener {
    private Calendar calendar = Calendar.getInstance();
    private Coordinate coordinate;
    private PrayTimesCalculator prayTimesCalculator;
    private int viewPagerPosition;
    private FragmentCalendarBinding mainBinding;
    private CalendarsTabContentBinding calendarsBinding;
    private OwghatTabContentBinding owghatBinding;
    private EventsTabContentBinding eventsBinding;

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        Context context = getContext();
        if (context == null) return null;

        setHasOptionsMenu(true);

        mainBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_calendar, container,
                false);
        viewPagerPosition = 0;

        boolean isRTL = UIUtils.isRTL(context);
        List<String> titles = new ArrayList<>();
        List<View> tabs = new ArrayList<>();

        titles.add(getString(R.string.calendar));
        calendarsBinding = DataBindingUtil.inflate(inflater, R.layout.calendars_tab_content, container, false);
        tabs.add(calendarsBinding.getRoot());

        titles.add(getString(R.string.events));
        eventsBinding = DataBindingUtil.inflate(inflater, R.layout.events_tab_content, container, false);
        tabs.add(eventsBinding.getRoot());

        coordinate = Utils.getCoordinate(context);
        if (coordinate != null) {
            titles.add(getString(R.string.owghat));
            owghatBinding = DataBindingUtil.inflate(inflater, R.layout.owghat_tab_content, container, false);
            tabs.add(owghatBinding.getRoot());
            owghatBinding.getRoot().setOnClickListener(this);
        }

        mainBinding.cardsViewPager.setAdapter(new CardTabsAdapter(getChildFragmentManager(), tabs));
        mainBinding.tabLayout.setupWithViewPager(mainBinding.cardsViewPager);

        mainBinding.tabLayout.getTabAt(0).setIcon(R.drawable.ic_event);
        mainBinding.tabLayout.getTabAt(1).setIcon(R.drawable.ic_event_note);
        if (coordinate != null) {
            mainBinding.tabLayout.getTabAt(2).setIcon(R.drawable.ic_access_time);
        }

        Resources.Theme theme = context.getTheme();
        TypedValue value = new TypedValue();

        theme.resolveAttribute(R.attr.colorAccent, value, true);
        int selectedColor = ContextCompat.getColor(context, value.resourceId);

        theme.resolveAttribute(R.attr.colorTextSecond, value, true);
        int unselectedColor = ContextCompat.getColor(context, value.resourceId);

        // https://stackoverflow.com/a/35461201
        mainBinding.tabLayout.addOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(mainBinding.cardsViewPager) {

                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);
                        tab.getIcon().setColorFilter(selectedColor, PorterDuff.Mode.SRC_IN);
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        super.onTabUnselected(tab);
                        tab.getIcon().setColorFilter(unselectedColor, PorterDuff.Mode.SRC_IN);
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        super.onTabReselected(tab);
                    }
                }
        );

        // https://stackoverflow.com/a/49455239 but obviously a hack we will try to remove
        if (isRTL) {
            for (View tab : tabs) {
                tab.setRotationY(180);
            }
            mainBinding.cardsViewPager.setRotationY(180);
        }

        prayTimesCalculator = new PrayTimesCalculator(Utils.getCalculationMethod());
        mainBinding.calendarViewPager.setAdapter(new CalendarAdapter(getChildFragmentManager(), isRTL));
        CalendarAdapter.gotoOffset(mainBinding.calendarViewPager, 0);

        mainBinding.calendarViewPager.addOnPageChangeListener(changeListener);

        calendarsBinding.today.setVisibility(View.GONE);
        calendarsBinding.todayIcon.setVisibility(View.GONE);
        calendarsBinding.today.setOnClickListener(this);
        calendarsBinding.todayIcon.setOnClickListener(this);

        calendarsBinding.firstCalendarDateLinear.setOnClickListener(this);
        calendarsBinding.firstCalendarDateDay.setOnClickListener(this);
        calendarsBinding.firstCalendarDate.setOnClickListener(this);
        calendarsBinding.secondCalendarDateLinear.setOnClickListener(this);
        calendarsBinding.secondCalendarDateDay.setOnClickListener(this);
        calendarsBinding.secondCalendarDate.setOnClickListener(this);
        calendarsBinding.thirdCalendarDateLinear.setOnClickListener(this);
        calendarsBinding.thirdCalendarDateDay.setOnClickListener(this);
        calendarsBinding.thirdCalendarDate.setOnClickListener(this);

        calendarsBinding.getRoot().setOnClickListener(this);

        calendarsBinding.firstCalendarDateLinear.setVisibility(View.GONE);
        calendarsBinding.secondCalendarDateLinear.setVisibility(View.GONE);
        calendarsBinding.thirdCalendarDateLinear.setVisibility(View.GONE);
        calendarsBinding.diffDateContainer.setVisibility(View.GONE);

        String cityName = Utils.getCityName(context, false);
        if (!TextUtils.isEmpty(cityName)) {
            owghatBinding.owghatText.setText(cityName);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int lastTab = prefs.getInt(Constants.LAST_CHOSEN_TAB_KEY, Constants.CALENDARS_TAB);
        if (lastTab >= tabs.size()) {
            lastTab = Constants.CALENDARS_TAB;
        }

        mainBinding.cardsViewPager.setCurrentItem(lastTab, false);
        mainBinding.tabLayout.getTabAt(lastTab).getIcon().setColorFilter(selectedColor,
                PorterDuff.Mode.SRC_IN);

        AbstractDate today = CalendarUtils.getTodayOfCalendar(Utils.getMainCalendar());
        UIUtils.setActivityTitleAndSubtitle(getActivity(), CalendarUtils.getMonthName(today),
                Utils.formatNumber(today.getYear()));

        // Easter egg to test AthanActivity
        if (coordinate != null) {
            owghatBinding.owghatText.setOnClickListener(this);
            owghatBinding.owghatText.setOnLongClickListener(v -> {
                Utils.startAthan(context, "FAJR");
                return true;
            });
        }

        java.util.Date date= new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        if      ((month == 12 && day >= 22 && day <= 31) || (month ==  1 && day >= 1 && day <= 19))
            calendarsBinding.zodiac.setText(getString(R.string.zodiac) + " " + getString(R.string.capricorn));
        else if ((month ==  1 && day >= 20 && day <= 31) || (month ==  2 && day >= 1 && day <= 17))
            calendarsBinding.zodiac.setText(getString(R.string.zodiac) + " " + getString(R.string.aquarius));
        else if ((month ==  2 && day >= 18 && day <= 29) || (month ==  3 && day >= 1 && day <= 19))
            calendarsBinding.zodiac.setText(getString(R.string.zodiac) + " " + getString(R.string.pisces));
        else if ((month ==  3 && day >= 20 && day <= 31) || (month ==  4 && day >= 1 && day <= 19))
            calendarsBinding.zodiac.setText(getString(R.string.zodiac) + " " + getString(R.string.aries));
        else if ((month ==  4 && day >= 20 && day <= 30) || (month ==  5 && day >= 1 && day <= 20))
            calendarsBinding.zodiac.setText(getString(R.string.zodiac) + " " + getString(R.string.taurus));
        else if ((month ==  5 && day >= 21 && day <= 31) || (month ==  6 && day >= 1 && day <= 20))
            calendarsBinding.zodiac.setText(getString(R.string.zodiac) + " " + getString(R.string.gemini));
        else if ((month ==  6 && day >= 21 && day <= 30) || (month ==  7 && day >= 1 && day <= 22))
            calendarsBinding.zodiac.setText(getString(R.string.zodiac) + " " + getString(R.string.cancer));
        else if ((month ==  7 && day >= 23 && day <= 31) || (month ==  8 && day >= 1 && day <= 22))
            calendarsBinding.zodiac.setText(getString(R.string.zodiac) + " " + getString(R.string.leo));
        else if ((month ==  8 && day >= 23 && day <= 31) || (month ==  9 && day >= 1 && day <= 22))
            calendarsBinding.zodiac.setText(getString(R.string.zodiac) + " " + getString(R.string.virgo));
        else if ((month ==  9 && day >= 23 && day <= 30) || (month == 10 && day >= 1 && day <= 22))
            calendarsBinding.zodiac.setText(getString(R.string.zodiac) + " " + getString(R.string.libra));
        else if ((month == 10 && day >= 23 && day <= 31) || (month == 11 && day >= 1 && day <= 21))
            calendarsBinding.zodiac.setText(getString(R.string.zodiac) + " " + getString(R.string.scorpio));
        else if ((month == 11 && day >= 22 && day <= 30) || (month == 12 && day >= 1 && day <= 21))
            calendarsBinding.zodiac.setText(getString(R.string.zodiac) + " " + getString(R.string.sagittarius));
        else
            calendarsBinding.zodiac.setText("Illegal date");

        return mainBinding.getRoot();

    }

    public boolean firstTime = true;

    ViewPager.OnPageChangeListener changeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            Context context = getContext();
            if (context == null) return;

            LocalBroadcastManager.getInstance(context).sendBroadcast(
                    new Intent(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT)
                            .putExtra(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT,
                                    CalendarAdapter.positionToOffset(position))
                            .putExtra(Constants.BROADCAST_FIELD_SELECT_DAY_JDN, lastSelectedJdn));

            calendarsBinding.today.setVisibility(View.VISIBLE);
            calendarsBinding.todayIcon.setVisibility(View.VISIBLE);
        }

    };

    void changeMonth(int position) {
        mainBinding.calendarViewPager.setCurrentItem(
                mainBinding.calendarViewPager.getCurrentItem() + position, true);
    }

    private long lastSelectedJdn = -1;

    public void selectDay(long jdn) {
        Context context = getContext();
        if (context == null) return;

        lastSelectedJdn = jdn;
        UIUtils.fillCalendarsCard(context, jdn, calendarsBinding, Utils.getMainCalendar(),
                Utils.getEnabledCalendarTypes());
        boolean isToday = CalendarUtils.getTodayJdn() == jdn;
        setOwghat(jdn, isToday);
        showEvent(jdn);
    }

    public void addEventOnCalendar(long jdn) {
        Activity activity = getActivity();
        if (activity == null) return;

        CivilDate civil = DateConverter.jdnToCivil(jdn);
        Calendar time = Calendar.getInstance();
        time.set(civil.getYear(), civil.getMonth() - 1, civil.getDayOfMonth());

        try {
            startActivityForResult(
                    new Intent(Intent.ACTION_INSERT)
                            .setData(CalendarContract.Events.CONTENT_URI)
                            .putExtra(CalendarContract.Events.DESCRIPTION, CalendarUtils.dayTitleSummary(
                                    CalendarUtils.getDateFromJdnOfCalendar(Utils.getMainCalendar(), jdn)))
                            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                                    time.getTimeInMillis())
                            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                                    time.getTimeInMillis())
                            .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true),
                    CALENDAR_EVENT_ADD_MODIFY_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(activity, R.string.device_calendar_does_not_support, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;

        if (requestCode == CALENDAR_EVENT_ADD_MODIFY_REQUEST_CODE) {
            if (Utils.isShowDeviceCalendarEvents()) {
                LocalBroadcastManager.getInstance(activity).sendBroadcast(
                        new Intent(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT)
                                .putExtra(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT, viewPagerPosition)
                                .putExtra(Constants.BROADCAST_FIELD_EVENT_ADD_MODIFY, true)
                                .putExtra(Constants.BROADCAST_FIELD_SELECT_DAY_JDN, lastSelectedJdn));
            } else {
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_CALENDAR)
                        != PackageManager.PERMISSION_GRANTED) {
                    UIUtils.askForCalendarPermission(activity);
                } else {
                    UIUtils.toggleShowCalendarOnPreference(activity, true);
                    activity.restartActivity();
                }
            }
        }
    }

    private SpannableString formatClickableEventTitle(DeviceCalendarEvent event) {
        Context context = getContext();
        if (context == null) return null;

        String title = UIUtils.formatDeviceCalendarEventTitle(event);
        SpannableString ss = new SpannableString(title);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                try {
                    startActivityForResult(new Intent(Intent.ACTION_VIEW)
                                    .setData(ContentUris.withAppendedId(
                                            CalendarContract.Events.CONTENT_URI, event.getId())),
                            CALENDAR_EVENT_ADD_MODIFY_REQUEST_CODE);
                } catch (Exception e) { // Should be ActivityNotFoundException but we don't care really
                    Toast.makeText(context, R.string.device_calendar_does_not_support, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                String color = event.getColor();
                if (!TextUtils.isEmpty(color)) {
                    try {
                        ds.setColor(Integer.parseInt(color));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        ss.setSpan(clickableSpan, 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    private SpannableStringBuilder getDeviceEventsTitle(List<AbstractEvent> dayEvents) {
        SpannableStringBuilder titles = new SpannableStringBuilder();
        boolean first = true;

        for (AbstractEvent event : dayEvents)
            if (event instanceof DeviceCalendarEvent) {
                if (first)
                    first = false;
                else
                    titles.append("\n");

                titles.append(formatClickableEventTitle((DeviceCalendarEvent) event));
            }

        return titles;
    }

    private void showEvent(long jdn) {
        List<AbstractEvent> events = Utils.getEvents(jdn,
                CalendarUtils.readDayDeviceEvents(getContext(), jdn));
        String holidays = Utils.getEventsTitle(events, true, false, false, false);
        String nonHolidays = Utils.getEventsTitle(events, false, false, false, false);
        SpannableStringBuilder deviceEvents = getDeviceEventsTitle(events);

        eventsBinding.holidayTitle.setVisibility(View.GONE);
        eventsBinding.deviceEventTitle.setVisibility(View.GONE);
        eventsBinding.eventTitle.setVisibility(View.GONE);
        eventsBinding.eventMessage.setVisibility(View.GONE);
        eventsBinding.noEvent.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(holidays)) {
            eventsBinding.noEvent.setVisibility(View.GONE);
            eventsBinding.holidayTitle.setText(holidays);
            eventsBinding.holidayTitle.setVisibility(View.VISIBLE);
        }

        if (deviceEvents.length() != 0) {
            eventsBinding.noEvent.setVisibility(View.GONE);
            eventsBinding.deviceEventTitle.setText(deviceEvents);
            eventsBinding.deviceEventTitle.setMovementMethod(LinkMovementMethod.getInstance());

            eventsBinding.deviceEventTitle.setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(nonHolidays)) {
            eventsBinding.noEvent.setVisibility(View.GONE);
            eventsBinding.eventTitle.setText(nonHolidays);

            eventsBinding.eventTitle.setVisibility(View.VISIBLE);
        }

        SpannableStringBuilder messageToShow = new SpannableStringBuilder();
        if (CalendarUtils.getPersianToday().getYear() > Utils.getMaxSupportedYear()) {
            String title = getString(R.string.shouldBeUpdated);
            SpannableString ss = new SpannableString(title);
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.byagowi.persiancalendar")));
                    } catch (ActivityNotFoundException e) { // Should be ActivityNotFoundException but we don't care really
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.byagowi.p+ersiancalendar")));
                    }
                }
            };
            ss.setSpan(clickableSpan, 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            messageToShow.append(ss);
        }

        Context context = getContext();
        if (context == null) return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> enabledTypes = prefs.getStringSet(PREF_HOLIDAY_TYPES, new HashSet<>());
        if (enabledTypes.size() == 0) {
            eventsBinding.noEvent.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(messageToShow))
                messageToShow.append("\n");

            String title = getString(R.string.warn_if_events_not_set);
            SpannableString ss = new SpannableString(title);
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    ((MainActivity) getActivity()).bringPreferences();
                }
            };
            ss.setSpan(clickableSpan, 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            messageToShow.append(ss);
        }

        if (!TextUtils.isEmpty(messageToShow)) {
            eventsBinding.eventMessage.setText(messageToShow);
            eventsBinding.eventMessage.setMovementMethod(LinkMovementMethod.getInstance());

            eventsBinding.eventMessage.setVisibility(View.VISIBLE);
        }
    }

    private void setOwghat(long jdn, boolean isToday) {
        if (coordinate == null) {
            return;
        }

        CivilDate civilDate = DateConverter.jdnToCivil(jdn);
        calendar.set(civilDate.getYear(), civilDate.getMonth() - 1, civilDate.getDayOfMonth());
        Date date = calendar.getTime();

        Map<PrayTime, Clock> prayTimes = prayTimesCalculator.calculate(date, coordinate);

        owghatBinding.imsak.setText(UIUtils.getFormattedClock(prayTimes.get(PrayTime.IMSAK)));
        Clock sunriseClock = prayTimes.get(PrayTime.FAJR);
        owghatBinding.fajr.setText(UIUtils.getFormattedClock(sunriseClock));
        owghatBinding.sunrise.setText(UIUtils.getFormattedClock(prayTimes.get(PrayTime.SUNRISE)));
        Clock midddayClock = prayTimes.get(PrayTime.DHUHR);
        owghatBinding.dhuhr.setText(UIUtils.getFormattedClock(midddayClock));
        owghatBinding.asr.setText(UIUtils.getFormattedClock(prayTimes.get(PrayTime.ASR)));
        owghatBinding.sunset.setText(UIUtils.getFormattedClock(prayTimes.get(PrayTime.SUNSET)));
        Clock maghribClock = prayTimes.get(PrayTime.MAGHRIB);
        owghatBinding.maghrib.setText(UIUtils.getFormattedClock(maghribClock));
        owghatBinding.isgha.setText(UIUtils.getFormattedClock(prayTimes.get(PrayTime.ISHA)));
        owghatBinding.midnight.setText(UIUtils.getFormattedClock(prayTimes.get(PrayTime.MIDNIGHT)));

        double moonPhase = 1;
        try {
            moonPhase = new SunMoonPosition(CalendarUtils.getTodayJdn(), coordinate.getLatitude(),
                    coordinate.getLongitude(), 0, 0).getMoonPhase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        owghatBinding.svPlot.setSunriseSunsetMoonPhase(prayTimes, moonPhase);

        if (isToday) {
            owghatBinding.svPlot.setVisibility(View.VISIBLE);
            if (mainBinding.cardsViewPager.getCurrentItem() == Constants.OWGHAT_TAB) {
                owghatBinding.svPlot.startAnimate(true);
            }
        } else {
            owghatBinding.svPlot.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        Context context = getContext();
        if (context == null) return;

        switch (v.getId()) {

            case R.id.calendars_tab_content:
                boolean isOpenCalendarCommand = calendarsBinding.firstCalendarDateLinear.getVisibility() != View.VISIBLE;

                calendarsBinding.moreCalendar.setImageResource(isOpenCalendarCommand
                        ? R.drawable.ic_keyboard_arrow_up
                        : R.drawable.ic_keyboard_arrow_down);
                calendarsBinding.firstCalendarDateLinear.setVisibility(isOpenCalendarCommand ? View.VISIBLE : View.GONE);
                calendarsBinding.secondCalendarDateLinear.setVisibility(isOpenCalendarCommand ? View.VISIBLE : View.GONE);
                calendarsBinding.thirdCalendarDateLinear.setVisibility(isOpenCalendarCommand ? View.VISIBLE : View.GONE);
                calendarsBinding.diffDateContainer.setVisibility(isOpenCalendarCommand ? View.VISIBLE : View.GONE);

                break;

            case R.id.owghat_text:
            case R.id.owghat_content:

                boolean isOpenOwghatCommand = owghatBinding.sunriseLayout.getVisibility() == View.GONE;

                owghatBinding.moreOwghat.setImageResource(isOpenOwghatCommand
                        ? R.drawable.ic_keyboard_arrow_up
                        : R.drawable.ic_keyboard_arrow_down);
                owghatBinding.imsakLayout.setVisibility(isOpenOwghatCommand ? View.VISIBLE : View.GONE);
                owghatBinding.sunriseLayout.setVisibility(isOpenOwghatCommand ? View.VISIBLE : View.GONE);
                owghatBinding.asrLayout.setVisibility(isOpenOwghatCommand ? View.VISIBLE : View.GONE);
                owghatBinding.sunsetLayout.setVisibility(isOpenOwghatCommand ? View.VISIBLE : View.GONE);
                owghatBinding.ishaLayout.setVisibility(isOpenOwghatCommand ? View.VISIBLE : View.GONE);
                owghatBinding.midnightLayout.setVisibility(isOpenOwghatCommand ? View.VISIBLE : View.GONE);

                mainBinding.cardsViewPager.measureCurrentView(owghatBinding.getRoot());

                if (lastSelectedJdn == -1)
                    lastSelectedJdn = CalendarUtils.getTodayJdn();

                break;

            case R.id.today:
            case R.id.today_icon:
                bringTodayYearMonth();
                break;

            case R.id.first_calendar_date:
            case R.id.first_calendar_date_day:
                UIUtils.copyToClipboard(context, calendarsBinding.firstCalendarDateDay.getText() + " " +
                        calendarsBinding.firstCalendarDate.getText().toString().replace("\n", " "));
                break;

            case R.id.first_calendar_date_linear:
                UIUtils.copyToClipboard(context, calendarsBinding.firstCalendarDateLinear.getText());
                break;

            case R.id.second_calendar_date:
            case R.id.second_calendar_date_day:
                UIUtils.copyToClipboard(context, calendarsBinding.secondCalendarDateDay.getText() + " " +
                        calendarsBinding.secondCalendarDate.getText().toString().replace("\n", " "));
                break;

            case R.id.second_calendar_date_linear:
                UIUtils.copyToClipboard(context, calendarsBinding.secondCalendarDateLinear.getText());
                break;

            case R.id.third_calendar_date:
            case R.id.third_calendar_date_day:
                UIUtils.copyToClipboard(context, calendarsBinding.thirdCalendarDateDay.getText() + " " +
                        calendarsBinding.thirdCalendarDate.getText().toString().replace("\n", " "));
                break;

            case R.id.third_calendar_date_linear:
                UIUtils.copyToClipboard(context, calendarsBinding.thirdCalendarDateLinear.getText());
                break;
        }
    }

    private void bringTodayYearMonth() {
        Context context = getContext();
        if (context == null) return;

        lastSelectedJdn = -1;
        LocalBroadcastManager.getInstance(context).sendBroadcast(
                new Intent(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT)
                        .putExtra(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT,
                                Constants.BROADCAST_TO_MONTH_FRAGMENT_RESET_DAY)
                        .putExtra(Constants.BROADCAST_FIELD_SELECT_DAY_JDN, -1));

        CalendarAdapter.gotoOffset(mainBinding.calendarViewPager, 0);

        selectDay(CalendarUtils.getTodayJdn());
    }

    public void bringDate(long jdn) {
        Context context = getContext();
        if (context == null) return;

        CalendarType mainCalendar = Utils.getMainCalendar();
        AbstractDate today = CalendarUtils.getTodayOfCalendar(mainCalendar);
        AbstractDate date = CalendarUtils.getDateFromJdnOfCalendar(mainCalendar, jdn);
        viewPagerPosition =
                (today.getYear() - date.getYear()) * 12 + today.getMonth() - date.getMonth();
        CalendarAdapter.gotoOffset(mainBinding.calendarViewPager, viewPagerPosition);

        selectDay(jdn);

        LocalBroadcastManager.getInstance(context).sendBroadcast(
                new Intent(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT)
                        .putExtra(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT, viewPagerPosition)
                        .putExtra(Constants.BROADCAST_FIELD_SELECT_DAY_JDN, jdn));
    }

    private SearchView mSearchView;
    private SearchView.SearchAutoComplete mSearchAutoComplete;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.calendar_menu_button, menu);

        mSearchView = (SearchView) menu.findItem(R.id.search).getActionView();
        mSearchView.setOnSearchClickListener(v -> {
            if (mSearchAutoComplete != null) mSearchAutoComplete.setOnItemClickListener(null);

            Context context = getContext();
            if (context == null) return;

            mSearchAutoComplete = mSearchView.findViewById(androidx.appcompat.R.id.search_src_text);
            mSearchAutoComplete.setHint(R.string.search_in_events);

            ArrayAdapter<AbstractEvent> eventsAdapter = new ArrayAdapter<>(context,
                    R.layout.suggestion, android.R.id.text1);
            eventsAdapter.addAll(Utils.getAllEnabledEvents());
            eventsAdapter.addAll(CalendarUtils.getAllEnabledAppointments(context));
            mSearchAutoComplete.setAdapter(eventsAdapter);
            mSearchAutoComplete.setOnItemClickListener((parent, view, position, id) -> {
                AbstractEvent ev = (AbstractEvent) parent.getItemAtPosition(position);
                if (ev instanceof PersianCalendarEvent) {
                    PersianDate todayPersian = CalendarUtils.getPersianToday();
                    PersianDate date = ((PersianCalendarEvent) ev).getDate();
                    int year = date.getYear();
                    if (year == -1) {
                        year = todayPersian.getYear() +
                                (date.getMonth() < todayPersian.getMonth() ? 1 : 0);
                    }
                    bringDate(DateConverter.persianToJdn(year, date.getMonth(), date.getDayOfMonth()));
                } else if (ev instanceof IslamicCalendarEvent) {
                    IslamicDate todayIslamic = CalendarUtils.getIslamicToday();
                    IslamicDate date = ((IslamicCalendarEvent) ev).getDate();
                    int year = date.getYear();
                    if (year == -1) {
                        year = todayIslamic.getYear() +
                                (date.getMonth() < todayIslamic.getMonth() ? 1 : 0);
                    }
                    bringDate(DateConverter.islamicToJdn(year, date.getMonth(), date.getDayOfMonth()));
                } else if (ev instanceof GregorianCalendarEvent) {
                    CivilDate todayCivil = CalendarUtils.getGregorianToday();
                    CivilDate date = ((GregorianCalendarEvent) ev).getDate();
                    int year = date.getYear();
                    if (year == -1) {
                        year = todayCivil.getYear() +
                                (date.getMonth() < todayCivil.getMonth() ? 1 : 0);
                    }
                    bringDate(DateConverter.civilToJdn(year, date.getMonth(), date.getDayOfMonth()));
                } else if (ev instanceof DeviceCalendarEvent) {
                    CivilDate todayCivil = CalendarUtils.getGregorianToday();
                    CivilDate date = ((DeviceCalendarEvent) ev).getCivilDate();
                    int year = date.getYear();
                    if (year == -1) {
                        year = todayCivil.getYear() +
                                (date.getMonth() < todayCivil.getMonth() ? 1 : 0);
                    }
                    bringDate(DateConverter.civilToJdn(year, date.getMonth(), date.getDayOfMonth()));
                }
                mSearchView.onActionViewCollapsed();
            });
        });
    }

    private void destroySearchView() {
        if (mSearchView != null) {
            mSearchView.setOnSearchClickListener(null);
            mSearchView = null;
        }

        if (mSearchAutoComplete != null) {
            mSearchAutoComplete.setAdapter(null);
            mSearchAutoComplete.setOnItemClickListener(null);
            mSearchAutoComplete = null;
        }
    }

    @Override
    public void onDestroyOptionsMenu() {
        destroySearchView();
        super.onDestroyOptionsMenu();
    }

    @Override
    public void onDestroy() {
        destroySearchView();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.go_to:
                new SelectDayDialog(lastSelectedJdn).show(getChildFragmentManager(),
                        SelectDayDialog.class.getName());
                break;
            case R.id.today:
                bringTodayYearMonth();
                break;
            case R.id.add_event:
                if (lastSelectedJdn == -1)
                    lastSelectedJdn = CalendarUtils.getTodayJdn();

                addEventOnCalendar(lastSelectedJdn);
                break;
            default:
                break;
        }
        return true;
    }

    public int getViewPagerPosition() {
        return viewPagerPosition;
    }

    public boolean closeSearch() {
        if (mSearchView != null && !mSearchView.isIconified()) {
            mSearchView.onActionViewCollapsed();
            return true;
        }
        return false;
    }

}
