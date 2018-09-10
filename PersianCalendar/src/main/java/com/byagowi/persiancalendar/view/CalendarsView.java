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
import calendar.IslamicDate;
import calendar.PersianDate;

public class CalendarsView extends FrameLayout implements View.OnClickListener {

    public CalendarsView(Context context) {
        super(context);
        init(context);
    }

    public CalendarsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CalendarsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    CalendarsViewBinding binding;

    public void init(Context context) {
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
        binding.extraInformationContainer.setVisibility(View.GONE);

        boolean applyLineMultiplier = !TypefaceUtils.isCustomFontEnabled();
        Typeface calendarFont = TypefaceUtils.getCalendarFragmentFont(context);

        binding.firstCalendarDate.setTypeface(calendarFont);
        binding.firstCalendarDateDay.setTypeface(calendarFont);
        if (applyLineMultiplier) binding.firstCalendarDate.setLineSpacing(0, .6f);

        binding.secondCalendarDate.setTypeface(calendarFont);
        binding.secondCalendarDateDay.setTypeface(calendarFont);
        if (applyLineMultiplier) binding.secondCalendarDate.setLineSpacing(0, .6f);

        binding.thirdCalendarDate.setTypeface(calendarFont);
        binding.thirdCalendarDateDay.setTypeface(calendarFont);
        if (applyLineMultiplier) binding.thirdCalendarDate.setLineSpacing(0, .6f);
    }

    public void showTodayIcon() {
        binding.today.setVisibility(View.VISIBLE);
        binding.todayIcon.setVisibility(View.VISIBLE);
    }

    public void hideMoreIcon() {
        binding.moreCalendar.setVisibility(View.GONE);
    }

    public interface OnCalendarsViewExpandListener {
        void onCalendarsViewExpand();
    }

    private OnCalendarsViewExpandListener calendarsViewExpandListener = () -> {
    };

    public void setOnCalendarsViewExpandListener(OnCalendarsViewExpandListener listener) {
        calendarsViewExpandListener = listener;
    }

    public interface OnTodayButtonClickListener {
        void onTodayButtonClick();
    }

    private OnTodayButtonClickListener todayButtonClickListener = () -> {
    };

    public void setOnTodayButtonClickListener(OnTodayButtonClickListener listener) {
        todayButtonClickListener = listener;
    }

    public void expand(boolean expand) {
        binding.moreCalendar.setImageResource(expand
                ? R.drawable.ic_keyboard_arrow_up
                : R.drawable.ic_keyboard_arrow_down);
        binding.firstCalendarDateLinear.setVisibility(expand ? View.VISIBLE : View.GONE);
        binding.secondCalendarDateLinear.setVisibility(expand ? View.VISIBLE : View.GONE);
        binding.thirdCalendarDateLinear.setVisibility(expand ? View.VISIBLE : View.GONE);
        binding.extraInformationContainer.setVisibility(expand ? View.VISIBLE : View.GONE);

        calendarsViewExpandListener.onCalendarsViewExpand();
    }

    @Override
    public void onClick(View view) {
        Context context = getContext();
        if (context == null) return;

        switch (view.getId()) {
            case R.id.today:
            case R.id.today_icon:
                todayButtonClickListener.onTodayButtonClick();
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
            R.string.year10, R.string.year11, R.string.year12,
            R.string.year1, R.string.year2, R.string.year3,
            R.string.year4, R.string.year5, R.string.year6,
            R.string.year7, R.string.year8, R.string.year9
    };

    @StringRes
    final private static int[] ZODIAC_MONTHS = {
            R.string.empty,
            R.string.aries, R.string.taurus, R.string.gemini,
            R.string.cancer, R.string.leo, R.string.virgo,
            R.string.libra, R.string.scorpio, R.string.sagittarius,
            R.string.capricorn, R.string.aquarius, R.string.pisces
    };

    @StringRes
    final private static int[] ZODIAC_MONTHS_EMOJI = {
            R.string.empty,
            R.string.aries_emoji, R.string.taurus_emoji, R.string.gemini_emoji,
            R.string.cancer_emoji, R.string.leo_emoji, R.string.virgo_emoji,
            R.string.libra_emoji, R.string.scorpio_emoji, R.string.sagittarius_emoji,
            R.string.capricorn_emoji, R.string.aquarius_emoji, R.string.pisces_emoji
    };

    public void showCalendars(long jdn,
                              CalendarType chosenCalendarType,
                              List<CalendarType> calendarsToShow) {
        Context context = getContext();
        if (context == null) return;
        // There should be one at least, if not, nvm
        if (calendarsToShow.size() == 0) return;

        AbstractDate firstCalendar,
                secondCalendar = null,
                thirdCalendar = null;
        firstCalendar = CalendarUtils.getDateFromJdnOfCalendar(calendarsToShow.get(0), jdn);
        if (calendarsToShow.size() > 1) {
            secondCalendar = CalendarUtils.getDateFromJdnOfCalendar(calendarsToShow.get(1), jdn);
        }
        if (calendarsToShow.size() > 2) {
            thirdCalendar = CalendarUtils.getDateFromJdnOfCalendar(calendarsToShow.get(2), jdn);
        }

        binding.weekDayName.setText(Utils.getWeekDayName(firstCalendar));

        binding.firstCalendarDateLinear.setText(CalendarUtils.toLinearDate(firstCalendar));
        binding.firstCalendarDateDay.setText(Utils.formatNumber(firstCalendar.getDayOfMonth()));
        binding.firstCalendarDate.setText(String.format("%s\n%s",
                CalendarUtils.getMonthName(firstCalendar),
                Utils.formatNumber(firstCalendar.getYear())));

        if (secondCalendar == null) {
            binding.secondCalendarContainer.setVisibility(View.GONE);
        } else {
            binding.secondCalendarDateLinear.setText(CalendarUtils.toLinearDate(secondCalendar));
            binding.secondCalendarDateDay.setText(Utils.formatNumber(secondCalendar.getDayOfMonth()));

            binding.secondCalendarDate.setText(String.format("%s\n%s",
                    CalendarUtils.getMonthName(secondCalendar),
                    Utils.formatNumber(secondCalendar.getYear())));
        }

        if (thirdCalendar == null) {
            binding.thirdCalendarContainer.setVisibility(View.GONE);
        } else {
            binding.thirdCalendarDateLinear.setText(CalendarUtils.toLinearDate(thirdCalendar));
            binding.thirdCalendarDateDay.setText(Utils.formatNumber(thirdCalendar.getDayOfMonth()));

            binding.thirdCalendarDate.setText(String.format("%s\n%s",
                    CalendarUtils.getMonthName(thirdCalendar),
                    Utils.formatNumber(thirdCalendar.getYear())));
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
            CivilDate civilOffset = new CivilDate(diffDays + civilBase.toJdn());
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
            AbstractDate mainDate = CalendarUtils.getDateFromJdnOfCalendar(chosenCalendarType, jdn);
            AbstractDate startOfYear = CalendarUtils.getDateOfCalendar(chosenCalendarType,
                    mainDate.getYear(), 1, 1);
            AbstractDate startOfNextYear = CalendarUtils.getDateOfCalendar(
                    chosenCalendarType, mainDate.getYear() + 1, 1, 1);
            long startOfYearJdn = startOfYear.toJdn();
            long endOfYearJdn = startOfNextYear.toJdn() - 1;
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
            PersianDate persianDate = new PersianDate(jdn);
            binding.zodiac.setText(String.format("%s: %s\n%s: %s %s",
                    context.getString(R.string.year_name),
                    context.getString(YEARS_NAME[persianDate.getYear() % 12]),
                    context.getString(R.string.zodiac),
                    context.getString(ZODIAC_MONTHS_EMOJI[persianDate.getMonth()]),
                    context.getString(ZODIAC_MONTHS[persianDate.getMonth()])));

            if (CalendarUtils.isMoonInScorpio(persianDate, new IslamicDate(jdn)))
                binding.moonInScorpio.setVisibility(View.VISIBLE);
            else
                binding.moonInScorpio.setVisibility(View.GONE);
        } else {
            binding.zodiac.setVisibility(View.GONE);
            binding.moonInScorpio.setVisibility(View.GONE);
        }
    }
}
