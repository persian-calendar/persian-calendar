package com.byagowi.persiancalendar.view.fragment;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.byagowi.persiancalendar.databinding.FragmentCalendarBinding;
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
import com.github.praytimes.Clock;
import com.github.praytimes.Coordinate;
import com.github.praytimes.PrayTime;
import com.github.praytimes.PrayTimesCalculator;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
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
    private FragmentCalendarBinding binding;

    @SuppressLint("SimpleDateFormat")
    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_calendar, container,
                false);
        viewPagerPosition = 0;

        binding.calendarsCard.today.setVisibility(View.GONE);
        binding.calendarsCard.todayIcon.setVisibility(View.GONE);

        coordinate = Utils.getCoordinate(getContext());
        prayTimesCalculator = new PrayTimesCalculator(Utils.getCalculationMethod());
        binding.calendarPager.setAdapter(new CalendarAdapter(getChildFragmentManager(),
                UIUtils.isRTL(getContext())));
        CalendarAdapter.gotoOffset(binding.calendarPager, 0);

        binding.calendarPager.addOnPageChangeListener(changeListener);

        binding.owghat.setOnClickListener(this);
        binding.calendarsCard.today.setOnClickListener(this);
        binding.calendarsCard.todayIcon.setOnClickListener(this);
        binding.calendarsCard.gregorianDate.setOnClickListener(this);
        binding.calendarsCard.gregorianDateDay.setOnClickListener(this);
        binding.calendarsCard.gregorianDateLinear.setOnClickListener(this);
        binding.calendarsCard.islamicDate.setOnClickListener(this);
        binding.calendarsCard.islamicDateDay.setOnClickListener(this);
        binding.calendarsCard.islamicDateLinear.setOnClickListener(this);
        binding.calendarsCard.shamsiDate.setOnClickListener(this);
        binding.calendarsCard.shamsiDateDay.setOnClickListener(this);
        binding.calendarsCard.shamsiDateLinear.setOnClickListener(this);

        binding.calendarsCard.calendarsCard.setOnClickListener(this);

        binding.warnUserIcon.setVisibility(View.GONE);
        binding.calendarsCard.gregorianDateLinear.setVisibility(View.GONE);
        binding.calendarsCard.islamicDateLinear.setVisibility(View.GONE);
        binding.calendarsCard.shamsiDateLinear.setVisibility(View.GONE);
        binding.calendarsCard.diffDateContainer.setVisibility(View.GONE);

        String cityName = Utils.getCityName(getContext(), false);
        if (!TextUtils.isEmpty(cityName)) {
            binding.owghatText.append(" (" + cityName + ")");
        }

        // This will immediately be replaced by the same functionality on fragment but is here to
        // make sure enough space is dedicated to actionbar's title and subtitle, kinda hack anyway
        AbstractDate today = CalendarUtils.getTodayOfCalendar(Utils.getMainCalendar());
        UIUtils.setActivityTitleAndSubtitle(getActivity(), CalendarUtils.getMonthName(today),
                Utils.formatNumber(today.getYear()));

        // Easter egg to test AthanActivity
        binding.owghatIcon.setOnLongClickListener(v -> {
            Utils.startAthan(getContext(), "FAJR");
            return true;
        });

        return binding.getRoot();

    }

    public boolean firstTime = true;

    ViewPager.OnPageChangeListener changeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(
                    new Intent(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT)
                            .putExtra(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT,
                                    CalendarAdapter.positionToOffset(position))
                            .putExtra(Constants.BROADCAST_FIELD_SELECT_DAY_JDN, lastSelectedJdn));

            binding.calendarsCard.today.setVisibility(View.VISIBLE);
            binding.calendarsCard.todayIcon.setVisibility(View.VISIBLE);
        }

    };

    void changeMonth(int position) {
        binding.calendarPager.setCurrentItem(binding.calendarPager.getCurrentItem() + position, true);
    }

    private long lastSelectedJdn = -1;

    void selectDay(long jdn) {
        lastSelectedJdn = jdn;
        UIUtils.fillCalendarsCard(getContext(), jdn, binding.calendarsCard,
                Utils.getMainCalendar());
        boolean isToday = CalendarUtils.getTodayJdn() == jdn;
        setOwghat(jdn, isToday);
        showEvent(jdn);
    }

    public void addEventOnCalendar(long jdn) {
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
            Toast.makeText(getContext(), R.string.device_calendar_does_not_support, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CALENDAR_EVENT_ADD_MODIFY_REQUEST_CODE) {
            if (Utils.isShowDeviceCalendarEvents()) {
                Utils.initUtils(getContext());
            } else {
                Toast.makeText(getContext(), R.string.enable_device_calendar,
                        Toast.LENGTH_LONG).show();
            }

            if (lastSelectedJdn == -1)
                lastSelectedJdn = CalendarUtils.getTodayJdn();
            selectDay(lastSelectedJdn);
        }
    }

    private SpannableString formatClickableEventTitle(DeviceCalendarEvent event) {
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
                    Toast.makeText(getContext(), R.string.device_calendar_does_not_support, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                try {
                    ds.setColor(Integer.parseInt(event.getColor()));
                } catch (Exception e) {
                    e.printStackTrace();
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
        List<AbstractEvent> events = Utils.getEvents(jdn);
        String holidays = Utils.getEventsTitle(events, true, false, false, false);
        String nonHolidays = Utils.getEventsTitle(events, false, false, false, false);
        SpannableStringBuilder deviceEvents = getDeviceEventsTitle(events);

        binding.cardEvent.setVisibility(View.GONE);
        binding.holidayTitle.setVisibility(View.GONE);
        binding.deviceEventTitle.setVisibility(View.GONE);
        binding.eventTitle.setVisibility(View.GONE);
        binding.eventMessage.setVisibility(View.GONE);

        if (!TextUtils.isEmpty(holidays)) {
            binding.holidayTitle.setText(holidays);
            binding.holidayTitle.setVisibility(View.VISIBLE);
            binding.cardEvent.setVisibility(View.VISIBLE);
        }

        if (deviceEvents.length() != 0) {
            binding.deviceEventTitle.setText(deviceEvents);
            binding.deviceEventTitle.setMovementMethod(LinkMovementMethod.getInstance());

            binding.deviceEventTitle.setVisibility(View.VISIBLE);
            binding.cardEvent.setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(nonHolidays)) {
            binding.eventTitle.setText(nonHolidays);

            binding.eventTitle.setVisibility(View.VISIBLE);
            binding.cardEvent.setVisibility(View.VISIBLE);
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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        Set<String> enabledTypes = prefs.getStringSet(PREF_HOLIDAY_TYPES, new HashSet<>());
        if (enabledTypes.size() == 0) {
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
            binding.warnUserIcon.setVisibility(View.VISIBLE);
            binding.eventMessage.setText(messageToShow);
            binding.eventMessage.setMovementMethod(LinkMovementMethod.getInstance());

            binding.eventMessage.setVisibility(View.VISIBLE);
            binding.cardEvent.setVisibility(View.VISIBLE);
        }
    }

    private void setOwghat(long jdn, boolean isToday) {
        if (coordinate == null) {
            binding.owghat.setVisibility(View.GONE);
            return;
        }

        CivilDate civilDate = DateConverter.jdnToCivil(jdn);
        calendar.set(civilDate.getYear(), civilDate.getMonth() - 1, civilDate.getDayOfMonth());
        Date date = calendar.getTime();

        Map<PrayTime, Clock> prayTimes = prayTimesCalculator.calculate(date, coordinate);

        binding.imsak.setText(UIUtils.getFormattedClock(prayTimes.get(PrayTime.IMSAK)));
        Clock sunriseClock = prayTimes.get(PrayTime.FAJR);
        binding.fajr.setText(UIUtils.getFormattedClock(sunriseClock));
        binding.sunrise.setText(UIUtils.getFormattedClock(prayTimes.get(PrayTime.SUNRISE)));
        Clock midddayClock = prayTimes.get(PrayTime.DHUHR);
        binding.dhuhr.setText(UIUtils.getFormattedClock(midddayClock));
        binding.asr.setText(UIUtils.getFormattedClock(prayTimes.get(PrayTime.ASR)));
        binding.sunset.setText(UIUtils.getFormattedClock(prayTimes.get(PrayTime.SUNSET)));
        Clock maghribClock = prayTimes.get(PrayTime.MAGHRIB);
        binding.maghrib.setText(UIUtils.getFormattedClock(maghribClock));
        binding.isgha.setText(UIUtils.getFormattedClock(prayTimes.get(PrayTime.ISHA)));
        binding.midnight.setText(UIUtils.getFormattedClock(prayTimes.get(PrayTime.MIDNIGHT)));
        binding.svPlot.setSunriseSunsetCalculator(prayTimes);

        if (isToday && !isOwghatOpen) {
            binding.svPlot.setVisibility(View.VISIBLE);
            binding.svPlot.startAnimate();
        } else {
            binding.svPlot.setVisibility(View.GONE);
        }
    }

    private boolean isOwghatOpen = false;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.calendars_card:
                boolean isOpenCalendarCommand = binding.calendarsCard.gregorianDateLinear.getVisibility() != View.VISIBLE;

                binding.calendarsCard.moreCalendar.setImageResource(isOpenCalendarCommand
                        ? R.drawable.ic_keyboard_arrow_up
                        : R.drawable.ic_keyboard_arrow_down);
                binding.calendarsCard.gregorianDateLinear.setVisibility(isOpenCalendarCommand ? View.VISIBLE : View.GONE);
                binding.calendarsCard.islamicDateLinear.setVisibility(isOpenCalendarCommand ? View.VISIBLE : View.GONE);
                binding.calendarsCard.shamsiDateLinear.setVisibility(isOpenCalendarCommand ? View.VISIBLE : View.GONE);
                binding.calendarsCard.diffDateContainer.setVisibility(isOpenCalendarCommand ? View.VISIBLE : View.GONE);

                break;

            case R.id.owghat:

                boolean isOpenOwghatCommand = binding.sunriseLayout.getVisibility() == View.GONE;

                binding.moreOwghat.setImageResource(isOpenOwghatCommand
                        ? R.drawable.ic_keyboard_arrow_up
                        : R.drawable.ic_keyboard_arrow_down);
                binding.imsakLayout.setVisibility(isOpenOwghatCommand ? View.VISIBLE : View.GONE);
                binding.sunriseLayout.setVisibility(isOpenOwghatCommand ? View.VISIBLE : View.GONE);
                binding.asrLayout.setVisibility(isOpenOwghatCommand ? View.VISIBLE : View.GONE);
                binding.sunsetLayout.setVisibility(isOpenOwghatCommand ? View.VISIBLE : View.GONE);
                binding.ishaLayout.setVisibility(isOpenOwghatCommand ? View.VISIBLE : View.GONE);
                binding.midnightLayout.setVisibility(isOpenOwghatCommand ? View.VISIBLE : View.GONE);
                isOwghatOpen = isOpenOwghatCommand;

                if (lastSelectedJdn == -1)
                    lastSelectedJdn = CalendarUtils.getTodayJdn();

                if (lastSelectedJdn == CalendarUtils.getTodayJdn() && !isOpenOwghatCommand) {
                    binding.svPlot.setVisibility(View.VISIBLE);
                    binding.svPlot.startAnimate();
                } else {
                    binding.svPlot.setVisibility(View.GONE);
                }

                break;

            case R.id.today:
            case R.id.today_icon:
                bringTodayYearMonth();
                break;

            case R.id.shamsi_date:
            case R.id.shamsi_date_day:
                UIUtils.copyToClipboard(getContext(), binding.calendarsCard.shamsiDateDay.getText() + " " +
                        binding.calendarsCard.shamsiDate.getText().toString().replace("\n", " "));
                break;

            case R.id.shamsi_date_linear:
                UIUtils.copyToClipboard(getContext(), binding.calendarsCard.shamsiDateLinear.getText());
                break;

            case R.id.gregorian_date:
            case R.id.gregorian_date_day:
                UIUtils.copyToClipboard(getContext(), binding.calendarsCard.gregorianDateDay.getText() + " " +
                        binding.calendarsCard.gregorianDate.getText().toString().replace("\n", " "));
                break;

            case R.id.gregorian_date_linear:
                UIUtils.copyToClipboard(getContext(), binding.calendarsCard.gregorianDateLinear.getText());
                break;

            case R.id.islamic_date:
            case R.id.islamic_date_day:
                UIUtils.copyToClipboard(getContext(), binding.calendarsCard.islamicDateDay.getText() + " " +
                        binding.calendarsCard.islamicDate.getText().toString().replace("\n", " "));
                break;

            case R.id.islamic_date_linear:
                UIUtils.copyToClipboard(getContext(), binding.calendarsCard.islamicDateLinear.getText());
                break;
        }
    }

    private void bringTodayYearMonth() {
        lastSelectedJdn = -1;
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(
                new Intent(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT)
                        .putExtra(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT,
                                Constants.BROADCAST_TO_MONTH_FRAGMENT_RESET_DAY)
                        .putExtra(Constants.BROADCAST_FIELD_SELECT_DAY_JDN, -1));

        CalendarAdapter.gotoOffset(binding.calendarPager, 0);

        selectDay(CalendarUtils.getTodayJdn());
    }

    public void bringDate(long jdn) {
        CalendarType mainCalendar = Utils.getMainCalendar();
        AbstractDate today = CalendarUtils.getTodayOfCalendar(mainCalendar);
        AbstractDate date = CalendarUtils.getDateFromJdnOfCalendar(mainCalendar, jdn);
        viewPagerPosition =
                (today.getYear() - date.getYear()) * 12 + today.getMonth() - date.getMonth();
        CalendarAdapter.gotoOffset(binding.calendarPager, viewPagerPosition);

        selectDay(jdn);

        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(
                new Intent(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT)
                        .putExtra(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT, viewPagerPosition)
                        .putExtra(Constants.BROADCAST_FIELD_SELECT_DAY_JDN, jdn));
    }

    private SearchView mSearchView;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.calendar_menu_button, menu);

        mSearchView = (SearchView) menu.findItem(R.id.search).getActionView();
        mSearchView.setOnSearchClickListener(v -> {
            SearchView.SearchAutoComplete searchAutoComplete = mSearchView.findViewById(androidx.appcompat.R.id.search_src_text);
            searchAutoComplete.setHint(R.string.search_in_events);
            SearchManager searchManager = (SearchManager) getContext().getSystemService(Context.SEARCH_SERVICE);
            if (searchManager == null) {
                return;
            }

            mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
            searchAutoComplete.setAdapter(new ArrayAdapter<>(getContext(),
                    R.layout.suggestion, android.R.id.text1, Utils.allEnabledEventsTitles));
            searchAutoComplete.setOnItemClickListener((parent, view, position, id) -> {
                Object ev = Utils.allEnabledEvents.get(Utils.allEnabledEventsTitles.indexOf(
                        (String) parent.getItemAtPosition(position)));
                long todayJdn = CalendarUtils.getTodayJdn();

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.go_to:
                new SelectDayDialog().show(getChildFragmentManager(),
                        SelectDayDialog.class.getName());
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
