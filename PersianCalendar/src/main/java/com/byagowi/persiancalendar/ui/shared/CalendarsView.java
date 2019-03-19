package com.byagowi.persiancalendar.ui.shared;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.calendar.AbstractDate;
import com.byagowi.persiancalendar.calendar.CivilDate;
import com.byagowi.persiancalendar.databinding.CalendarsViewBinding;
import com.byagowi.persiancalendar.praytimes.Clock;
import com.byagowi.persiancalendar.utils.AstronomicalUtils;
import com.byagowi.persiancalendar.utils.CalendarType;
import com.byagowi.persiancalendar.utils.Utils;

import java.util.Calendar;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CalendarsView extends FrameLayout {

    CalendarsViewBinding mBinding;
    private OnCalendarsViewExpandListener mCalendarsViewExpandListener = () -> {
    };
    private OnShowHideTodayButton mOnShowHideTodayButton = show -> {
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

        mBinding.getRoot().setOnClickListener(v -> expand(!mCalendarItemAdapter.isExpanded()));
        mBinding.extraInformationContainer.setVisibility(View.GONE);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        mBinding.calendarsRecyclerView.setLayoutManager(linearLayoutManager);
        mCalendarItemAdapter = new CalendarItemAdapter(context);
        mBinding.calendarsRecyclerView.setAdapter(mCalendarItemAdapter);
    }

    public void hideMoreIcon() {
        mBinding.moreCalendar.setVisibility(View.GONE);
    }

    public void setOnCalendarsViewExpandListener(OnCalendarsViewExpandListener listener) {
        mCalendarsViewExpandListener = listener;
    }

    public void setOnShowHideTodayButton(OnShowHideTodayButton listener) {
        mOnShowHideTodayButton = listener;
    }

    public void expand(boolean expanded) {
        mCalendarItemAdapter.setExpanded(expanded);

        mBinding.moreCalendar.setImageResource(expanded
                ? R.drawable.ic_keyboard_arrow_up
                : R.drawable.ic_keyboard_arrow_down);
        mBinding.extraInformationContainer.setVisibility(expanded ? View.VISIBLE : View.GONE);

        mCalendarsViewExpandListener.onCalendarsViewExpand();
    }

    public void showCalendars(long jdn,
                              CalendarType chosenCalendarType,
                              List<CalendarType> calendarsToShow) {
        Context context = getContext();
        if (context == null) return;

        mCalendarItemAdapter.setDate(calendarsToShow, jdn);
        mBinding.weekDayName.setText(Utils.getWeekDayName(new CivilDate(jdn)));

        mBinding.zodiac.setText(AstronomicalUtils.INSTANCE.getZodiacInfo(context, jdn, true));
        mBinding.zodiac.setVisibility(TextUtils.isEmpty(mBinding.zodiac.getText()) ? View.GONE : View.VISIBLE);

        long diffDays = Math.abs(Utils.getTodayJdn() - jdn);

        if (diffDays == 0) {
            if (Utils.isIranTime()) {
                mBinding.weekDayName.setText(String.format("%s (%s)",
                        mBinding.weekDayName.getText(),
                        context.getString(R.string.iran_time)));
            }
            mOnShowHideTodayButton.onShowHideTodayButton(false);
            mBinding.diffDate.setVisibility(View.GONE);
        } else {
            mOnShowHideTodayButton.onShowHideTodayButton(true);
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
            AbstractDate mainDate = Utils.getDateFromJdnOfCalendar(chosenCalendarType, jdn);
            AbstractDate startOfYear = Utils.getDateOfCalendar(chosenCalendarType,
                    mainDate.getYear(), 1, 1);
            AbstractDate startOfNextYear = Utils.getDateOfCalendar(
                    chosenCalendarType, mainDate.getYear() + 1, 1, 1);
            long startOfYearJdn = startOfYear.toJdn();
            long endOfYearJdn = startOfNextYear.toJdn() - 1;
            int currentWeek = Utils.calculateWeekOfYear(jdn, startOfYearJdn);
            int weeksCount = Utils.calculateWeekOfYear(endOfYearJdn, startOfYearJdn);

            String startOfYearText = String.format(context.getString(R.string.start_of_year_diff),
                    Utils.formatNumber((int) (jdn - startOfYearJdn)),
                    Utils.formatNumber(currentWeek),
                    Utils.formatNumber(mainDate.getMonth()));
            String endOfYearText = String.format(context.getString(R.string.end_of_year_diff),
                    Utils.formatNumber((int) (endOfYearJdn - jdn)),
                    Utils.formatNumber(weeksCount - currentWeek),
                    Utils.formatNumber(12 - mainDate.getMonth()));
            mBinding.startAndEndOfYearDiff.setText(String.format("%s\n%s", startOfYearText, endOfYearText));

            String equinox = "";
            if (Utils.getMainCalendar() == chosenCalendarType &&
                    chosenCalendarType == CalendarType.SHAMSI) {
                if ((mainDate.getMonth() == 12 && mainDate.getDayOfMonth() >= 20) ||
                        (mainDate.getMonth() == 1 && mainDate.getDayOfMonth() == 1)) {
                    int addition = mainDate.getMonth() == 12 ? 1 : 0;
                    Calendar springEquinox = Utils.getSpringEquinox(mainDate.toJdn());
                    equinox = String.format(context.getString(R.string.spring_equinox),
                            Utils.formatNumber(mainDate.getYear() + addition),
                            Utils.getFormattedClock(
                                    new Clock(springEquinox.get(Calendar.HOUR_OF_DAY),
                                            springEquinox.get(Calendar.MINUTE)), true));
                }
            }
            mBinding.equinox.setText(equinox);
            mBinding.equinox.setVisibility(TextUtils.isEmpty(equinox) ? GONE : VISIBLE);
        }

        mBinding.getRoot().setContentDescription(Utils.getA11yDaySummary(context, jdn,
                diffDays == 0,
                null, true, true, true));
    }

    public interface OnShowHideTodayButton {
        void onShowHideTodayButton(boolean show);
    }

    public interface OnCalendarsViewExpandListener {
        void onCalendarsViewExpand();
    }
}
