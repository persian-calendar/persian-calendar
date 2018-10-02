package com.byagowi.persiancalendar.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.adapter.CalendarItemAdapter;
import com.byagowi.persiancalendar.calendar.AbstractDate;
import com.byagowi.persiancalendar.calendar.CivilDate;
import com.byagowi.persiancalendar.databinding.CalendarsViewBinding;
import com.byagowi.persiancalendar.util.AstronomicalUtils;
import com.byagowi.persiancalendar.util.CalendarType;
import com.byagowi.persiancalendar.util.CalendarUtils;
import com.byagowi.persiancalendar.util.Utils;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import java.util.List;

public class CalendarsView extends FrameLayout implements View.OnClickListener {

    CalendarsViewBinding mBinding;
    private OnCalendarsViewExpandListener mCalendarsViewExpandListener = () -> {
    };
    private OnTodayButtonClickListener mTodayButtonClickListener = () -> {
    };
    private CalendarItemAdapter mCalendarItemAdapter;

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

    public void init(Context context) {
        mBinding = CalendarsViewBinding.inflate(LayoutInflater.from(context), this,
                true);
        mBinding.todayButton.setVisibility(View.GONE);
        mBinding.todayButton.setOnClickListener(this);

        mBinding.getRoot().setOnClickListener(this);
        mBinding.extraInformationContainer.setVisibility(View.GONE);

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(context);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setJustifyContent(JustifyContent.CENTER);
        mBinding.calendarsRecyclerView.setLayoutManager(layoutManager);
        mCalendarItemAdapter = new CalendarItemAdapter(context);
        mBinding.calendarsRecyclerView.setAdapter(mCalendarItemAdapter);
    }

    public void showTodayIcon() {
        mBinding.todayButton.setVisibility(View.VISIBLE);
    }

    public void hideMoreIcon() {
        mBinding.moreCalendar.setVisibility(View.GONE);
    }

    public void setOnCalendarsViewExpandListener(OnCalendarsViewExpandListener listener) {
        mCalendarsViewExpandListener = listener;
    }

    public void setOnTodayButtonClickListener(OnTodayButtonClickListener listener) {
        mTodayButtonClickListener = listener;
    }

    public void expand(boolean expanded) {
        mCalendarItemAdapter.setExpanded(expanded);

        mBinding.moreCalendar.setImageResource(expanded
                ? R.drawable.ic_keyboard_arrow_up
                : R.drawable.ic_keyboard_arrow_down);
        mBinding.extraInformationContainer.setVisibility(expanded ? View.VISIBLE : View.GONE);

        mCalendarsViewExpandListener.onCalendarsViewExpand();
    }

    @Override
    public void onClick(View view) {
        Context context = getContext();
        if (context == null) return;

        switch (view.getId()) {
            case R.id.today_button:
                mTodayButtonClickListener.onTodayButtonClick();
                break;

            case R.id.calendars_tab_content:
                expand(!mCalendarItemAdapter.isExpanded());
                break;
        }
    }

    public void showCalendars(long jdn,
                              CalendarType chosenCalendarType,
                              List<CalendarType> calendarsToShow) {
        Context context = getContext();
        if (context == null) return;

        mCalendarItemAdapter.setDate(calendarsToShow, jdn);
        mBinding.weekDayName.setText(Utils.getWeekDayName(new CivilDate(jdn)));

        mBinding.zodiac.setText(AstronomicalUtils.getZodiacInfo(context, jdn, true));
        mBinding.zodiac.setVisibility(TextUtils.isEmpty(mBinding.zodiac.getText()) ? View.GONE : View.VISIBLE);

        long diffDays = Math.abs(CalendarUtils.getTodayJdn() - jdn);

        if (diffDays == 0) {
            mBinding.todayButton.setVisibility(View.GONE);
            if (Utils.isIranTime()) {
                mBinding.weekDayName.setText(String.format("%s (%s)",
                        mBinding.weekDayName.getText(),
                        context.getString(R.string.iran_time)));
            }
            mBinding.todayButton.setVisibility(View.GONE);
            mBinding.diffDate.setVisibility(View.GONE);
        } else {
            mBinding.todayButton.setVisibility(View.VISIBLE);
            mBinding.diffDate.setVisibility(View.VISIBLE);

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
            mBinding.diffDate.setText(text);
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
            mBinding.startAndEndOfYearDiff.setText(String.format("%s\n%s", startOfYearText, endOfYearText));
        }

        mBinding.getRoot().setContentDescription(CalendarUtils.getA11yDaySummary(context, jdn,
                diffDays == 0,
                null, true, true, true));
    }

    public interface OnCalendarsViewExpandListener {
        void onCalendarsViewExpand();
    }

    public interface OnTodayButtonClickListener {
        void onTodayButtonClick();
    }
}
