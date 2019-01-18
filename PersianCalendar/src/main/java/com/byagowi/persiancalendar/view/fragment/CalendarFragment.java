package com.byagowi.persiancalendar.view.fragment;

import android.Manifest;
import android.animation.LayoutTransition;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.adapter.CalendarAdapter;
import com.byagowi.persiancalendar.adapter.CardTabsAdapter;
import com.byagowi.persiancalendar.adapter.TimeItemAdapter;
import com.byagowi.persiancalendar.calendar.AbstractDate;
import com.byagowi.persiancalendar.calendar.CivilDate;
import com.byagowi.persiancalendar.databinding.EventsTabContentBinding;
import com.byagowi.persiancalendar.databinding.FragmentCalendarBinding;
import com.byagowi.persiancalendar.databinding.OwghatTabContentBinding;
import com.byagowi.persiancalendar.di.dependencies.AppDependency;
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency;
import com.byagowi.persiancalendar.entity.AbstractEvent;
import com.byagowi.persiancalendar.entity.DeviceCalendarEvent;
import com.byagowi.persiancalendar.praytimes.Coordinate;
import com.byagowi.persiancalendar.praytimes.PrayTimes;
import com.byagowi.persiancalendar.praytimes.PrayTimesCalculator;
import com.byagowi.persiancalendar.util.CalendarType;
import com.byagowi.persiancalendar.util.CalendarUtils;
import com.byagowi.persiancalendar.util.UIUtils;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.CalendarsView;
import com.byagowi.persiancalendar.view.activity.MainActivity;
import com.byagowi.persiancalendar.view.dialog.SelectDayDialog;
import com.byagowi.persiancalendar.view.dialog.ShiftWorkDialog;
import com.cepmuvakkit.times.posAlgo.SunMoonPosition;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import dagger.android.support.DaggerFragment;

import static com.byagowi.persiancalendar.Constants.CALENDAR_EVENT_ADD_MODIFY_REQUEST_CODE;
import static com.byagowi.persiancalendar.Constants.PREF_HOLIDAY_TYPES;

public class CalendarFragment extends DaggerFragment {
    @Inject
    AppDependency appDependency; // same object from App
    @Inject
    MainActivityDependency mainActivityDependency; // same object from MainActivity
    boolean mFirstTime = true;
    private Calendar mCalendar = Calendar.getInstance();
    private Coordinate mCoordinate;
    private int mViewPagerPosition;
    private FragmentCalendarBinding mMainBinding;
    private CalendarsView mCalendarsView;
    private OwghatTabContentBinding mOwghatBinding;
    private EventsTabContentBinding mEventsBinding;
    private long mLastSelectedJdn = -1;
    private SearchView mSearchView;
    private SearchView.SearchAutoComplete mSearchAutoComplete;
    private CalendarAdapter.CalendarAdapterHelper mCalendarAdapterHelper;
    private ViewPager.OnPageChangeListener mChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            sendBroadcastToMonthFragments(mCalendarAdapterHelper.positionToOffset(position), false);
            mMainBinding.todayButton.show();
        }

    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Context context = mainActivityDependency.getMainActivity();

        setHasOptionsMenu(true);

        mMainBinding = FragmentCalendarBinding.inflate(inflater, container, false);
        mViewPagerPosition = 0;

        List<String> titles = new ArrayList<>();
        List<View> tabs = new ArrayList<>();

        titles.add(getString(R.string.calendar));
        mCalendarsView = new CalendarsView(context);
        mCalendarsView.setOnCalendarsViewExpandListener(() -> mMainBinding.cardsViewPager.measureCurrentView(mCalendarsView));
        mCalendarsView.setOnShowHideTodayButton(show -> {
            if (show)
                mMainBinding.todayButton.show();
            else
                mMainBinding.todayButton.hide();
        });
        mMainBinding.todayButton.setOnClickListener(v -> bringTodayYearMonth());
        tabs.add(mCalendarsView);

        titles.add(getString(R.string.events));
        mEventsBinding = EventsTabContentBinding.inflate(inflater, container, false);
        tabs.add(mEventsBinding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            LayoutTransition layoutTransition = new LayoutTransition();
            layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
            mEventsBinding.eventsContent.setLayoutTransition(layoutTransition);
            // Don't do the same for others tabs, it is problematic
        }

        mCoordinate = Utils.getCoordinate(context);
        if (mCoordinate != null) {
            titles.add(getString(R.string.owghat));
            mOwghatBinding = OwghatTabContentBinding.inflate(inflater, container, false);
            tabs.add(mOwghatBinding.getRoot());
            mOwghatBinding.getRoot().setOnClickListener(this::onOwghatClick);
            mOwghatBinding.cityName.setOnClickListener(this::onOwghatClick);
            // Easter egg to test AthanActivity
            mOwghatBinding.cityName.setOnLongClickListener(v -> {
                Utils.startAthan(context, "FAJR");
                return true;
            });
            String cityName = Utils.getCityName(context, false);
            if (!TextUtils.isEmpty(cityName)) {
                mOwghatBinding.cityName.setText(cityName);
            }

            FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(context);
            layoutManager.setFlexWrap(FlexWrap.WRAP);
            layoutManager.setJustifyContent(JustifyContent.CENTER);
            mOwghatBinding.timesRecyclerView.setLayoutManager(layoutManager);
            mOwghatBinding.timesRecyclerView.setAdapter(new TimeItemAdapter());
        }

        mMainBinding.cardsViewPager.setAdapter(new CardTabsAdapter(getChildFragmentManager(),
                appDependency, tabs, titles));
        mMainBinding.tabLayout.setupWithViewPager(mMainBinding.cardsViewPager);

        mCalendarAdapterHelper = new CalendarAdapter.CalendarAdapterHelper(UIUtils.isRTL(context));
        mMainBinding.calendarViewPager.setAdapter(new CalendarAdapter(getChildFragmentManager(),
                mCalendarAdapterHelper));
        mCalendarAdapterHelper.gotoOffset(mMainBinding.calendarViewPager, 0);

        mMainBinding.calendarViewPager.addOnPageChangeListener(mChangeListener);

        int lastTab = appDependency.getSharedPreferences()
                .getInt(Constants.LAST_CHOSEN_TAB_KEY, Constants.CALENDARS_TAB);
        if (lastTab >= tabs.size()) {
            lastTab = Constants.CALENDARS_TAB;
        }

        mMainBinding.cardsViewPager.setCurrentItem(lastTab, false);

        AbstractDate today = CalendarUtils.getTodayOfCalendar(Utils.getMainCalendar());
        mainActivityDependency.getMainActivity().setTitleAndSubtitle(CalendarUtils.getMonthName(today),
                Utils.formatNumber(today.getYear()));

        return mMainBinding.getRoot();
    }

    void changeMonth(int position) {
        mMainBinding.calendarViewPager.setCurrentItem(
                mMainBinding.calendarViewPager.getCurrentItem() + position, true);
    }

    public void selectDay(long jdn) {
        mLastSelectedJdn = jdn;
        mCalendarsView.showCalendars(jdn, Utils.getMainCalendar(), Utils.getEnabledCalendarTypes());
        setOwghat(jdn, CalendarUtils.getTodayJdn() == jdn);
        showEvent(jdn);
    }

    public void addEventOnCalendar(long jdn) {
        MainActivity activity = mainActivityDependency.getMainActivity();

        CivilDate civil = new CivilDate(jdn);
        Calendar time = Calendar.getInstance();
        time.set(civil.getYear(), civil.getMonth() - 1, civil.getDayOfMonth());
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            UIUtils.askForCalendarPermission(activity);
        } else {
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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        MainActivity activity = mainActivityDependency.getMainActivity();

        if (requestCode == CALENDAR_EVENT_ADD_MODIFY_REQUEST_CODE) {
            if (Utils.isShowDeviceCalendarEvents()) {
                sendBroadcastToMonthFragments(calculateViewPagerPositionFromJdn(mLastSelectedJdn), true);
            } else {
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_CALENDAR)
                        != PackageManager.PERMISSION_GRANTED) {
                    UIUtils.askForCalendarPermission(activity);
                } else {
                    UIUtils.toggleShowDeviceCalendarOnPreference(activity, true);
                    activity.restartActivity();
                }
            }
        }
    }

    private void sendBroadcastToMonthFragments(int toWhich, boolean addOrModify) {
        appDependency.getLocalBroadcastManager().sendBroadcast(
                new Intent(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT)
                        .putExtra(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT, toWhich)
                        .putExtra(Constants.BROADCAST_FIELD_EVENT_ADD_MODIFY, addOrModify)
                        .putExtra(Constants.BROADCAST_FIELD_SELECT_DAY_JDN, mLastSelectedJdn));
    }

    private SpannableString formatClickableEventTitle(DeviceCalendarEvent event) {
        String title = UIUtils.formatDeviceCalendarEventTitle(event);
        SpannableString ss = new SpannableString(title);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View textView) {
                try {
                    startActivityForResult(new Intent(Intent.ACTION_VIEW)
                                    .setData(ContentUris.withAppendedId(
                                            CalendarContract.Events.CONTENT_URI, event.getId())),
                            CALENDAR_EVENT_ADD_MODIFY_REQUEST_CODE);
                } catch (Exception e) { // Should be ActivityNotFoundException but we don't care really
                    Toast.makeText(mainActivityDependency.getMainActivity(),
                            R.string.device_calendar_does_not_support, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
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
        mEventsBinding.shiftWorkTitle.setText(Utils.getShiftWorkTitle(jdn, false));

        List<AbstractEvent> events = Utils.getEvents(jdn,
                CalendarUtils.readDayDeviceEvents(mainActivityDependency.getMainActivity(), jdn));
        String holidays = Utils.getEventsTitle(events, true, false, false, false);
        String nonHolidays = Utils.getEventsTitle(events, false, false, false, false);
        SpannableStringBuilder deviceEvents = getDeviceEventsTitle(events);
        StringBuilder contentDescription = new StringBuilder();

        mEventsBinding.eventMessage.setVisibility(View.GONE);
        mEventsBinding.noEvent.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(holidays)) {
            mEventsBinding.noEvent.setVisibility(View.GONE);
            mEventsBinding.holidayTitle.setText(holidays);
            String holidayContent = getString(R.string.holiday_reason) + "\n" + holidays;
            mEventsBinding.holidayTitle.setContentDescription(holidayContent);
            contentDescription.append(holidayContent);
            mEventsBinding.holidayTitle.setVisibility(View.VISIBLE);
        } else {
            mEventsBinding.holidayTitle.setVisibility(View.GONE);
        }

        if (deviceEvents.length() != 0) {
            mEventsBinding.noEvent.setVisibility(View.GONE);
            mEventsBinding.deviceEventTitle.setText(deviceEvents);
            contentDescription.append("\n");
            contentDescription.append(getString(R.string.show_device_calendar_events));
            contentDescription.append("\n");
            contentDescription.append(deviceEvents);
            mEventsBinding.deviceEventTitle.setMovementMethod(LinkMovementMethod.getInstance());

            mEventsBinding.deviceEventTitle.setVisibility(View.VISIBLE);
        } else {
            mEventsBinding.deviceEventTitle.setVisibility(View.GONE);
        }


        if (!TextUtils.isEmpty(nonHolidays)) {
            mEventsBinding.noEvent.setVisibility(View.GONE);
            mEventsBinding.eventTitle.setText(nonHolidays);
            contentDescription.append("\n");
            contentDescription.append(getString(R.string.events));
            contentDescription.append("\n");
            contentDescription.append(nonHolidays);

            mEventsBinding.eventTitle.setVisibility(View.VISIBLE);
        } else {
            mEventsBinding.eventTitle.setVisibility(View.GONE);
        }

        SpannableStringBuilder messageToShow = new SpannableStringBuilder();

        Set<String> enabledTypes = appDependency.getSharedPreferences()
                .getStringSet(PREF_HOLIDAY_TYPES, new HashSet<>());
        if (enabledTypes == null || enabledTypes.size() == 0) {
            mEventsBinding.noEvent.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(messageToShow))
                messageToShow.append("\n");

            String title = getString(R.string.warn_if_events_not_set);
            SpannableString ss = new SpannableString(title);
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View textView) {
                    mainActivityDependency.getMainActivity().navigateTo(R.id.settings);
                }
            };
            ss.setSpan(clickableSpan, 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            messageToShow.append(ss);

            contentDescription.append("\n");
            contentDescription.append(title);
        }

        if (!TextUtils.isEmpty(messageToShow)) {
            mEventsBinding.eventMessage.setText(messageToShow);
            mEventsBinding.eventMessage.setMovementMethod(LinkMovementMethod.getInstance());

            mEventsBinding.eventMessage.setVisibility(View.VISIBLE);
        }

        mEventsBinding.getRoot().setContentDescription(contentDescription);
    }

    private void setOwghat(long jdn, boolean isToday) {
        if (mCoordinate == null) {
            return;
        }

        CivilDate civilDate = new CivilDate(jdn);
        mCalendar.set(civilDate.getYear(), civilDate.getMonth() - 1, civilDate.getDayOfMonth());
        Date date = mCalendar.getTime();

        PrayTimes prayTimes = PrayTimesCalculator.calculate(Utils.getCalculationMethod(),
                date, mCoordinate);
        RecyclerView.Adapter adapter = mOwghatBinding.timesRecyclerView.getAdapter();
        if (adapter instanceof TimeItemAdapter) {
            ((TimeItemAdapter) adapter).setTimes(prayTimes);
        }

        double moonPhase = 1;
        try {
            moonPhase = new SunMoonPosition(CalendarUtils.getTodayJdn(), mCoordinate.getLatitude(),
                    mCoordinate.getLongitude(), 0, 0).getMoonPhase();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mOwghatBinding.sunView.setSunriseSunsetMoonPhase(prayTimes, moonPhase);
        if (isToday) {
            mOwghatBinding.sunView.setVisibility(View.VISIBLE);
            if (mMainBinding.cardsViewPager.getCurrentItem() == Constants.OWGHAT_TAB) {
                mOwghatBinding.sunView.startAnimate(true);
            }
        } else {
            mOwghatBinding.sunView.setVisibility(View.GONE);
        }
    }

    private void onOwghatClick(View v) {
        RecyclerView.Adapter adapter = mOwghatBinding.timesRecyclerView.getAdapter();
        if (adapter instanceof TimeItemAdapter) {
            TimeItemAdapter timesAdapter = (TimeItemAdapter) adapter;
            boolean expanded = !timesAdapter.isExpanded();
            timesAdapter.setExpanded(expanded);
            mOwghatBinding.moreOwghat.setImageResource(expanded
                    ? R.drawable.ic_keyboard_arrow_up
                    : R.drawable.ic_keyboard_arrow_down);
        }
        mMainBinding.cardsViewPager.measureCurrentView(mOwghatBinding.getRoot());

        if (mLastSelectedJdn == -1)
            mLastSelectedJdn = CalendarUtils.getTodayJdn();
    }

    private void bringTodayYearMonth() {
        mLastSelectedJdn = -1;
        sendBroadcastToMonthFragments(Constants.BROADCAST_TO_MONTH_FRAGMENT_RESET_DAY, false);

        mCalendarAdapterHelper.gotoOffset(mMainBinding.calendarViewPager, 0);

        selectDay(CalendarUtils.getTodayJdn());
    }

    public void afterShiftWorkChange() {
        Utils.updateStoredPreference(getContext());
        sendBroadcastToMonthFragments(calculateViewPagerPositionFromJdn(mLastSelectedJdn), true);
    }

    public void bringDate(long jdn) {
        Context context = getContext();
        if (context == null) return;

        mViewPagerPosition = calculateViewPagerPositionFromJdn(jdn);
        mCalendarAdapterHelper.gotoOffset(mMainBinding.calendarViewPager, mViewPagerPosition);

        selectDay(jdn);
        sendBroadcastToMonthFragments(mViewPagerPosition, false);

        if (Utils.isTalkBackEnabled()) {
            long todayJdn = CalendarUtils.getTodayJdn();
            if (jdn != todayJdn) {
                Toast.makeText(context, CalendarUtils.getA11yDaySummary(context, jdn,
                        false, null, true,
                        true, true), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private int calculateViewPagerPositionFromJdn(long jdn) {
        CalendarType mainCalendar = Utils.getMainCalendar();
        AbstractDate today = CalendarUtils.getTodayOfCalendar(mainCalendar);
        AbstractDate date = CalendarUtils.getDateFromJdnOfCalendar(mainCalendar, jdn);
        return (today.getYear() - date.getYear()) * 12 + today.getMonth() - date.getMonth();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.calendar_menu_buttons, menu);

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
                AbstractDate date = ev.getDate();
                CalendarType type = CalendarUtils.getCalendarTypeFromDate(date);
                AbstractDate today = CalendarUtils.getTodayOfCalendar(type);
                int year = date.getYear();
                if (year == -1) {
                    year = today.getYear() + (date.getMonth() < today.getMonth() ? 1 : 0);
                }
                bringDate(CalendarUtils.getDateOfCalendar(type, year, date.getMonth(), date.getDayOfMonth()).toJdn());
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
                SelectDayDialog.newInstance(mLastSelectedJdn).show(getChildFragmentManager(),
                        SelectDayDialog.class.getName());
                break;
            case R.id.add_event:
                if (mLastSelectedJdn == -1)
                    mLastSelectedJdn = CalendarUtils.getTodayJdn();

                addEventOnCalendar(mLastSelectedJdn);
                break;
            case R.id.shift_work:
                ShiftWorkDialog.newInstance(mLastSelectedJdn).show(getChildFragmentManager(),
                        ShiftWorkDialog.class.getName());
                break;
            default:
                break;
        }
        return true;
    }

    int getViewPagerPosition() {
        return mViewPagerPosition;
    }

    public boolean closeSearch() {
        if (mSearchView != null && !mSearchView.isIconified()) {
            mSearchView.onActionViewCollapsed();
            return true;
        }
        return false;
    }

}
