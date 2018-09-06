package com.byagowi.persiancalendar.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.databinding.CalendarsViewBinding;
import com.byagowi.persiancalendar.util.CalendarUtils;
import com.byagowi.persiancalendar.util.TypefaceUtils;
import com.byagowi.persiancalendar.util.UIUtils;
import com.byagowi.persiancalendar.util.Utils;

import java.util.List;

import androidx.annotation.StringRes;
import androidx.databinding.DataBindingUtil;
import calendar.AbstractDate;
import calendar.CalendarType;
import calendar.CivilDate;
import calendar.DateConverter;

public class CalendarsView extends FrameLayout implements View.OnClickListener {

    public CalendarsView(Context context) {
        super(context);
        init();
    }

    public CalendarsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CalendarsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    CalendarsViewBinding binding;

    public void init() {
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()),
                R.layout.calendars_view, this,
                true);
        binding.today.setVisibility(View.GONE);
        binding.todayIcon.setVisibility(View.GONE);
        binding.today.setOnClickListener(this);
        binding.todayIcon.setOnClickListener(this);

        binding.firstCalendarDateLinear.setOnClickListener(this);
        binding.firstCalendarDateDay.setOnClickListener(this);
        binding.firstCalendarDate.setOnClickListener(this);
        binding.secondCalendarDateLinear.setOnClickListener(this);
        binding.secondCalendarDateDay.setOnClickListener(this);
        binding.secondCalendarDate.setOnClickListener(this);
        binding.thirdCalendarDateLinear.setOnClickListener(this);
        binding.thirdCalendarDateDay.setOnClickListener(this);
        binding.thirdCalendarDate.setOnClickListener(this);

        binding.getRoot().setOnClickListener(this);

        binding.firstCalendarDateLinear.setVisibility(View.GONE);
        binding.secondCalendarDateLinear.setVisibility(View.GONE);
        binding.thirdCalendarDateLinear.setVisibility(View.GONE);
        binding.diffDateContainer.setVisibility(View.GONE);
    }

    public void showTodayIcon() {
        binding.today.setVisibility(View.VISIBLE);
        binding.todayIcon.setVisibility(View.VISIBLE);
    }

    public void hideMoreIcon() {
        binding.moreCalendar.setVisibility(View.GONE);
    }

    public interface Action {
        void fire();
    }

    private Action emptyAction = () -> {
    };

    private Action onExpanded = emptyAction;
    private Action onTodayClicked = emptyAction;

    public void setOnExpanded(Action onExpanded) {
        this.onExpanded = onExpanded;
    }

    public void setOnTodayClicked(Action onTodayClicked) {
        this.onTodayClicked = onTodayClicked;
    }

    public void expand(boolean expand) {
        binding.moreCalendar.setImageResource(expand
                ? R.drawable.ic_keyboard_arrow_up
                : R.drawable.ic_keyboard_arrow_down);
        binding.firstCalendarDateLinear.setVisibility(expand ? View.VISIBLE : View.GONE);
        binding.secondCalendarDateLinear.setVisibility(expand ? View.VISIBLE : View.GONE);
        binding.thirdCalendarDateLinear.setVisibility(expand ? View.VISIBLE : View.GONE);
        binding.diffDateContainer.setVisibility(expand ? View.VISIBLE : View.GONE);

        onExpanded.fire();
    }

    @Override
    public void onClick(View view) {
        Context context = getContext();
        if (context == null) return;

        switch (view.getId()) {
            case R.id.today:
            case R.id.today_icon:
                onTodayClicked.fire();
                break;

            case R.id.calendars_tab_content:
                expand(binding.firstCalendarDateLinear.getVisibility() != View.VISIBLE);
                break;

            case R.id.first_calendar_date:
            case R.id.first_calendar_date_day:
                UIUtils.copyToClipboard(context, binding.firstCalendarDateDay.getText() + " " +
                        binding.firstCalendarDate.getText().toString().replace("\n", " "));
                break;

            case R.id.first_calendar_date_linear:
                UIUtils.copyToClipboard(context, binding.firstCalendarDateLinear.getText());
                break;

            case R.id.second_calendar_date:
            case R.id.second_calendar_date_day:
                UIUtils.copyToClipboard(context, binding.secondCalendarDateDay.getText() + " " +
                        binding.secondCalendarDate.getText().toString().replace("\n", " "));
                break;

            case R.id.second_calendar_date_linear:
                UIUtils.copyToClipboard(context, binding.secondCalendarDateLinear.getText());
                break;

            case R.id.third_calendar_date:
            case R.id.third_calendar_date_day:
                UIUtils.copyToClipboard(context, binding.thirdCalendarDateDay.getText() + " " +
                        binding.thirdCalendarDate.getText().toString().replace("\n", " "));
                break;

            case R.id.third_calendar_date_linear:
                UIUtils.copyToClipboard(context, binding.thirdCalendarDateLinear.getText());
                break;
        }
    }

    @StringRes
    final private static int[] YEARS_NAME = {
            R.string.year1, R.string.year2, R.string.year3,
            R.string.year4, R.string.year5, R.string.year6,
            R.string.year7, R.string.year8, R.string.year9,
            R.string.year10, R.string.year11, R.string.year12
    };

    public void fillCalendarsCard(long jdn,
                                  CalendarType calendarType,
                                  List<CalendarType> calendars) {
        Context context = getContext();
        if (context == null) return;

        AbstractDate firstCalendar,
                secondCalendar = null,
                thirdCalendar = null;
        firstCalendar = CalendarUtils.getDateFromJdnOfCalendar(calendars.get(0), jdn);
        if (calendars.size() > 1) {
            secondCalendar = CalendarUtils.getDateFromJdnOfCalendar(calendars.get(1), jdn);
        }
        if (calendars.size() > 2) {
            thirdCalendar = CalendarUtils.getDateFromJdnOfCalendar(calendars.get(2), jdn);
        }

        boolean applyLineMultiplier = !TypefaceUtils.isCustomFontEnabled();
        Typeface calendarFont = TypefaceUtils.getCalendarFragmentFont(context);

        binding.weekDayName.setText(Utils.getWeekDayName(firstCalendar));

        binding.firstCalendarDateLinear.setText(CalendarUtils.toLinearDate(firstCalendar));
        binding.firstCalendarDateDay.setText(Utils.formatNumber(firstCalendar.getDayOfMonth()));
        binding.firstCalendarDateDay.setTypeface(calendarFont);
        binding.firstCalendarDate.setText(String.format("%s\n%s",
                CalendarUtils.getMonthName(firstCalendar),
                Utils.formatNumber(firstCalendar.getYear())));
        binding.firstCalendarDate.setTypeface(calendarFont);
        if (applyLineMultiplier) binding.firstCalendarDate.setLineSpacing(0, .6f);

        if (secondCalendar == null) {
            binding.secondCalendarContainer.setVisibility(View.GONE);
        } else {
            binding.secondCalendarDateLinear.setText(CalendarUtils.toLinearDate(secondCalendar));
            binding.secondCalendarDateDay.setText(Utils.formatNumber(secondCalendar.getDayOfMonth()));
            binding.secondCalendarDateDay.setTypeface(calendarFont);
            binding.secondCalendarDate.setText(String.format("%s\n%s",
                    CalendarUtils.getMonthName(secondCalendar),
                    Utils.formatNumber(secondCalendar.getYear())));
            binding.secondCalendarDate.setTypeface(calendarFont);
            if (applyLineMultiplier) binding.secondCalendarDate.setLineSpacing(0, .6f);
        }

        if (thirdCalendar == null) {
            binding.thirdCalendarContainer.setVisibility(View.GONE);
        } else {
            binding.thirdCalendarDateLinear.setText(CalendarUtils.toLinearDate(thirdCalendar));
            binding.thirdCalendarDateDay.setText(Utils.formatNumber(thirdCalendar.getDayOfMonth()));
            binding.thirdCalendarDateDay.setTypeface(calendarFont);
            binding.thirdCalendarDate.setText(String.format("%s\n%s",
                    CalendarUtils.getMonthName(thirdCalendar),
                    Utils.formatNumber(thirdCalendar.getYear())));
            binding.thirdCalendarDate.setTypeface(calendarFont);
            if (applyLineMultiplier) binding.thirdCalendarDate.setLineSpacing(0, .6f);
        }

        long diffDays = Math.abs(CalendarUtils.getTodayJdn() - jdn);

        if (diffDays == 0) {
            binding.today.setVisibility(View.GONE);
            binding.todayIcon.setVisibility(View.GONE);
            if (Utils.isIranTime()) {
                binding.weekDayName.setText(String.format("%s (%s)",
                        binding.weekDayName.getText(),
                        context.getString(R.string.iran_time)));
            }
            binding.today.setVisibility(View.GONE);
            binding.todayIcon.setVisibility(View.GONE);
            binding.diffDate.setVisibility(View.GONE);
        } else {
            binding.today.setVisibility(View.VISIBLE);
            binding.todayIcon.setVisibility(View.VISIBLE);
            binding.diffDate.setVisibility(View.VISIBLE);

            CivilDate civilBase = new CivilDate(2000, 1, 1);
            CivilDate civilOffset = DateConverter.jdnToCivil(diffDays + DateConverter.civilToJdn(civilBase));
            int yearDiff = civilOffset.getYear() - 2000;
            int monthDiff = civilOffset.getMonth() - 1;
            int dayOfMonthDiff = civilOffset.getDayOfMonth() - 1;
            String text = String.format(context.getString(R.string.date_diff_text),
                    Utils.formatNumber((int) diffDays),
                    Utils.formatNumber(yearDiff),
                    Utils.formatNumber(monthDiff),
                    Utils.formatNumber(dayOfMonthDiff));
            if (diffDays <= 30) {
                text = text.split("\\(")[0];
            }
            binding.diffDate.setText(text);
        }

        {
            AbstractDate mainDate = CalendarUtils.getDateFromJdnOfCalendar(calendarType, jdn);
            AbstractDate startOfYear = CalendarUtils.getDateOfCalendar(calendarType,
                    mainDate.getYear(), 1, 1);
            AbstractDate startOfNextYear = CalendarUtils.getDateOfCalendar(
                    calendarType, mainDate.getYear() + 1, 1, 1);
            long startOfYearJdn = CalendarUtils.getJdnDate(startOfYear);
            long endOfYearJdn = CalendarUtils.getJdnDate(startOfNextYear) - 1;
            int currentWeek = CalendarUtils.calculateWeekOfYear(jdn, startOfYearJdn);
            int weeksCount = CalendarUtils.calculateWeekOfYear(endOfYearJdn, startOfYearJdn);

            binding.startAndEndOfYearDiff.setText(
                    String.format(context.getString(R.string.start_of_year_diff) + "\n" +
                                    context.getString(R.string.end_of_year_diff),
                            Utils.formatNumber((int) (jdn - startOfYearJdn)),
                            Utils.formatNumber(currentWeek),
                            Utils.formatNumber(mainDate.getMonth()),
                            Utils.formatNumber((int) (endOfYearJdn - jdn)),
                            Utils.formatNumber(weeksCount - currentWeek),
                            Utils.formatNumber(12 - mainDate.getMonth())));
        }

        // Based on Mehdi's work
        if (Utils.isAstronomicalFeaturesEnabled()) {
            CivilDate civilDate = DateConverter.jdnToCivil(jdn);
            int year = civilDate.getYear();
            int month = civilDate.getMonth();
            int day = civilDate.getDayOfMonth();

            @StringRes
            int monthName, monthEmoji;
            if ((month == 12 && day >= 22 && day <= 31) || (month == 1 && day >= 1 && day <= 19)) {
                monthName = R.string.capricorn;
                monthEmoji = R.string.capricorn_emoji;
            } else if ((month == 1 && day >= 20 && day <= 31) || (month == 2 && day >= 1 && day <= 17)) {
                monthName = R.string.aquarius;
                monthEmoji = R.string.aquarius_emoji;
            } else if ((month == 2 && day >= 18 && day <= 29) || (month == 3 && day >= 1 && day <= 19)) {
                monthName = R.string.pisces;
                monthEmoji = R.string.pisces_emoji;
            } else if ((month == 3 && day >= 20 && day <= 31) || (month == 4 && day >= 1 && day <= 19)) {
                monthName = R.string.aries;
                monthEmoji = R.string.aries_emoji;
            } else if ((month == 4 && day >= 20 && day <= 30) || (month == 5 && day >= 1 && day <= 20)) {
                monthName = R.string.taurus;
                monthEmoji = R.string.taurus_emoji;
            } else if ((month == 5 && day >= 21 && day <= 31) || (month == 6 && day >= 1 && day <= 20)) {
                monthName = R.string.gemini;
                monthEmoji = R.string.gemini_emoji;
            } else if ((month == 6 && day >= 21 && day <= 30) || (month == 7 && day >= 1 && day <= 22)) {
                monthName = R.string.cancer;
                monthEmoji = R.string.cancer_emoji;
            } else if ((month == 7 && day >= 23 && day <= 31) || (month == 8 && day >= 1 && day <= 22)) {
                monthName = R.string.leo;
                monthEmoji = R.string.leo_emoji;
            } else if ((month == 8 && day >= 23 && day <= 31) || (month == 9 && day >= 1 && day <= 22)) {
                monthName = R.string.virgo;
                monthEmoji = R.string.virgo_emoji;
            } else if ((month == 9 && day >= 23 && day <= 30) || (month == 10 && day >= 1 && day <= 22)) {
                monthName = R.string.libra;
                monthEmoji = R.string.libra_emoji;
            } else if ((month == 10 && day >= 23 && day <= 31) || (month == 11 && day >= 1 && day <= 21)) {
                monthName = R.string.scorpio;
                monthEmoji = R.string.scorpio_emoji;
            } else if ((month == 11 && day >= 22 && day <= 30) || (month == 12 && day >= 1 && day <= 21)) {
                monthName = R.string.sagittarius;
                monthEmoji = R.string.sagittarius_emoji;
            } else {
                monthName = R.string.sagittarius; // this never should happen
                monthEmoji = R.string.sagittarius_emoji;
            }

            binding.zodiac.setText(String.format("%s: %s\n%s: %s %s",
                    context.getString(R.string.year_name),
                    context.getString(YEARS_NAME[year % 12]),
                    context.getString(R.string.zodiac),
                    context.getString(monthEmoji), context.getString(monthName)));

            if (CalendarUtils.monthInScorpio(jdn))
                binding.moonInScorpio.setVisibility(View.VISIBLE);
            else
                binding.moonInScorpio.setVisibility(View.GONE);
        } else {
            binding.zodiac.setVisibility(View.GONE);
            binding.moonInScorpio.setVisibility(View.GONE);
        }
    }
}
