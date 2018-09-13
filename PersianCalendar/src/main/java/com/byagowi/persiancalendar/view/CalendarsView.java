package com.byagowi.persiancalendar.view;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.databinding.CalendarsViewBinding;
import com.byagowi.persiancalendar.util.CalendarUtils;
import com.byagowi.persiancalendar.util.TypefaceUtils;
import com.byagowi.persiancalendar.util.Utils;

import java.util.List;

import androidx.databinding.DataBindingUtil;
import calendar.AbstractDate;
import calendar.CalendarType;
import calendar.CivilDate;

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
        binding.todayButton.setVisibility(View.GONE);
        binding.todayButton.setOnClickListener(this);

        binding.firstCalendarDateLinear.setOnClickListener(this);
        binding.firstCalendarDateContainer.setOnClickListener(this);
        binding.secondCalendarDateLinear.setOnClickListener(this);
        binding.secondCalendarDateContainer.setOnClickListener(this);
        binding.thirdCalendarDateLinear.setOnClickListener(this);
        binding.thirdCalendarDateContainer.setOnClickListener(this);

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
        binding.todayButton.setVisibility(View.VISIBLE);
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
            case R.id.today_button:
                todayButtonClickListener.onTodayButtonClick();
                break;

            case R.id.calendars_tab_content:
                expand(binding.firstCalendarDateLinear.getVisibility() != View.VISIBLE);
                break;

            case R.id.first_calendar_date_container:
            case R.id.first_calendar_date_linear:
            case R.id.second_calendar_date_container:
            case R.id.second_calendar_date_linear:
            case R.id.third_calendar_date_container:
            case R.id.third_calendar_date_linear:
                ClipboardManager clipboardService =
                        (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                CharSequence text = view.getContentDescription();

                if (clipboardService != null && text != null) {
                    clipboardService.setPrimaryClip(ClipData.newPlainText("converted date", text));
                    Toast.makeText(context, "«" + text + "»\n" + context.getString(R.string.date_copied_clipboard), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

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

        String firstCalendarLinear = CalendarUtils.toLinearDate(firstCalendar);
        binding.firstCalendarDateLinear.setText(firstCalendarLinear);
        binding.firstCalendarDateLinear.setContentDescription(firstCalendarLinear);

        String firstCalendarString = CalendarUtils.dateToString(firstCalendar);
        binding.firstCalendarDateContainer.setContentDescription(firstCalendarString);
        binding.firstCalendarDateDay.setContentDescription("");
        binding.firstCalendarDateDay.setText(Utils.formatNumber(firstCalendar.getDayOfMonth()));
        binding.firstCalendarDate.setContentDescription("");
        binding.firstCalendarDate.setText(String.format("%s\n%s",
                CalendarUtils.getMonthName(firstCalendar),
                Utils.formatNumber(firstCalendar.getYear())));

        if (secondCalendar == null) {
            binding.secondCalendarContainer.setVisibility(View.GONE);
        } else {
            String secondCalendarLinear = CalendarUtils.toLinearDate(secondCalendar);
            binding.secondCalendarDateLinear.setText(secondCalendarLinear);
            binding.secondCalendarDateLinear.setContentDescription(secondCalendarLinear);

            String secondCalendarString = CalendarUtils.dateToString(secondCalendar);
            binding.secondCalendarDateContainer.setContentDescription(secondCalendarString);
            binding.secondCalendarDateDay.setContentDescription("");
            binding.secondCalendarDateDay.setText(Utils.formatNumber(secondCalendar.getDayOfMonth()));
            binding.secondCalendarDate.setContentDescription("");
            binding.secondCalendarDate.setText(String.format("%s\n%s",
                    CalendarUtils.getMonthName(secondCalendar),
                    Utils.formatNumber(secondCalendar.getYear())));
        }

        if (thirdCalendar == null) {
            binding.thirdCalendarContainer.setVisibility(View.GONE);
        } else {
            String thirdCalendarLinear = CalendarUtils.toLinearDate(thirdCalendar);
            binding.thirdCalendarDateLinear.setText(thirdCalendarLinear);
            binding.thirdCalendarDateLinear.setContentDescription(thirdCalendarLinear);

            String thirdCalendarString = CalendarUtils.dateToString(thirdCalendar);
            binding.thirdCalendarDateContainer.setContentDescription(thirdCalendarString);
            binding.thirdCalendarDateDay.setContentDescription("");
            binding.thirdCalendarDateDay.setText(Utils.formatNumber(thirdCalendar.getDayOfMonth()));
            binding.thirdCalendarDate.setContentDescription("");
            binding.thirdCalendarDate.setText(String.format("%s\n%s",
                    CalendarUtils.getMonthName(thirdCalendar),
                    Utils.formatNumber(thirdCalendar.getYear())));
        }

        binding.zodiac.setText(CalendarUtils.getZodiacInfo(context, jdn, true));
        binding.zodiac.setVisibility(TextUtils.isEmpty(binding.zodiac.getText()) ? View.GONE : View.VISIBLE);

        long diffDays = Math.abs(CalendarUtils.getTodayJdn() - jdn);

        if (diffDays == 0) {
            binding.todayButton.setVisibility(View.GONE);
            if (Utils.isIranTime()) {
                binding.weekDayName.setText(String.format("%s (%s)",
                        binding.weekDayName.getText(),
                        context.getString(R.string.iran_time)));
            }
            binding.todayButton.setVisibility(View.GONE);
            binding.diffDate.setVisibility(View.GONE);
        } else {
            binding.todayButton.setVisibility(View.VISIBLE);
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

            String startOfYearText = String.format(context.getString(R.string.start_of_year_diff),
                    Utils.formatNumber((int) (jdn - startOfYearJdn)),
                    Utils.formatNumber(currentWeek),
                    Utils.formatNumber(mainDate.getMonth()));
            String endOfYearText = String.format(context.getString(R.string.end_of_year_diff),
                    Utils.formatNumber((int) (endOfYearJdn - jdn)),
                    Utils.formatNumber(weeksCount - currentWeek),
                    Utils.formatNumber(12 - mainDate.getMonth()));
            binding.startAndEndOfYearDiff.setText(String.format("%s\n%s", startOfYearText, endOfYearText));
        }

        binding.getRoot().setContentDescription(CalendarUtils.getA11yDaySummary(context, jdn,
                diffDays == 0,
                null, true, true, true));
    }
}
