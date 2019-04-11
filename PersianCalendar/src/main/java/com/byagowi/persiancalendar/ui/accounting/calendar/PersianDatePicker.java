package com.byagowi.persiancalendar.ui.accounting.calendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.TextView;

import com.byagowi.persiancalendar.R;

import java.util.Date;

public class PersianDatePicker extends LinearLayout {
    OnValueChangeListener dateChangeListener;
    private NumberPicker dayNumberPicker;
    private TextView descriptionTextView;
    private boolean displayDescription;
    private OnDateChangedListener mListener;
    private int maxYear;
    private int minYear;
    private NumberPicker monthNumberPicker;
    private NumberPicker yearNumberPicker;
    private int yearRange;

    class Value implements OnValueChangeListener {

        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            boolean isLeapYear = PersianCalendarUtils.isPersianLeapYear(PersianDatePicker.this.yearNumberPicker.getValue());
            int month = PersianDatePicker.this.monthNumberPicker.getValue();
            int day = PersianDatePicker.this.dayNumberPicker.getValue();
            if (month < 7) {
                PersianDatePicker.this.dayNumberPicker.setMinValue(1);
                PersianDatePicker.this.dayNumberPicker.setMaxValue(31);
            } else if (month > 6 && month < 12) {
                if (day == 31) {
                    PersianDatePicker.this.dayNumberPicker.setValue(30);
                }
                PersianDatePicker.this.dayNumberPicker.setMinValue(1);
                PersianDatePicker.this.dayNumberPicker.setMaxValue(30);
            } else if (month == 12) {
                if (isLeapYear) {
                    if (day == 31) {
                        PersianDatePicker.this.dayNumberPicker.setValue(30);
                    }
                    PersianDatePicker.this.dayNumberPicker.setMinValue(1);
                    PersianDatePicker.this.dayNumberPicker.setMaxValue(30);
                } else {
                    if (day > 29) {
                        PersianDatePicker.this.dayNumberPicker.setValue(29);
                    }
                    PersianDatePicker.this.dayNumberPicker.setMinValue(1);
                    PersianDatePicker.this.dayNumberPicker.setMaxValue(29);
                }
            }
            if (PersianDatePicker.this.displayDescription) {
                PersianDatePicker.this.descriptionTextView.setText(PersianDatePicker.this.getDisplayPersianDate().getPersianLongDate());
            }
            if (PersianDatePicker.this.mListener != null) {
                PersianDatePicker.this.mListener.onDateChanged(PersianDatePicker.this.yearNumberPicker.getValue(), PersianDatePicker.this.monthNumberPicker.getValue(), PersianDatePicker.this.dayNumberPicker.getValue());
            }
        }
    }

    public interface OnDateChangedListener {
        void onDateChanged(int i, int i2, int i3);
    }

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new state();
        long datetime;

        static class state implements Creator<SavedState> {

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            datetime = in.readLong();
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeLong(datetime);
        }
    }

    public PersianDatePicker(Context context) {
        this(context, null, -1);
    }

    public PersianDatePicker(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public PersianDatePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        dateChangeListener = new Value();
        @SuppressLint("WrongConstant") View view = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.persian_date_picker, this);
        yearNumberPicker = view.findViewById(R.id.yearNumberPicker);
        monthNumberPicker = view.findViewById(R.id.monthNumberPicker);
        dayNumberPicker = view.findViewById(R.id.dayNumberPicker);
        descriptionTextView = view.findViewById(R.id.descriptionTextView);
        PersianCalendar pCalendar = new PersianCalendar();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PersianDatePicker, 0, 0);
        if (a.getBoolean(R.styleable.PersianDatePicker_displayMonthNames, false)) {
            yearNumberPicker.setDescendantFocusability(393216);
            monthNumberPicker.setDescendantFocusability(393216);
            dayNumberPicker.setDescendantFocusability(393216);
        }
        yearRange = a.getInteger(R.styleable.PersianDatePicker_yearRange, 10);
        minYear = a.getInt(R.styleable.PersianDatePicker_minYear, pCalendar.getPersianYear() - yearRange);
        maxYear = a.getInt(R.styleable.PersianDatePicker_maxYear, pCalendar.getPersianYear() + yearRange);
        yearNumberPicker.setMinValue(minYear);
        yearNumberPicker.setMaxValue(maxYear);
        int selectedYear = a.getInt(R.styleable.PersianDatePicker_selectedYear, pCalendar.getPersianYear());
        if (selectedYear > maxYear || selectedYear < minYear) {
            throw new IllegalArgumentException(String.format("Selected year (%d) must be between minYear(%d) and maxYear(%d)", new Object[]{Integer.valueOf(selectedYear), Integer.valueOf(minYear), Integer.valueOf(maxYear)}));
        }
        yearNumberPicker.setValue(selectedYear);
        yearNumberPicker.setOnValueChangedListener(dateChangeListener);
        boolean displayMonthNames = a.getBoolean(R.styleable.PersianDatePicker_displayMonthNames, false);
        monthNumberPicker.setMinValue(1);
        monthNumberPicker.setMaxValue(12);
        if (displayMonthNames) {
            monthNumberPicker.setDisplayedValues(PersianCalendarConstants.persianMonthNames);
        }
        int selectedMonth = a.getInteger(R.styleable.PersianDatePicker_selectedMonth, pCalendar.getPersianMonth());
        if (selectedMonth < 1 || selectedMonth > 12) {
            throw new IllegalArgumentException(String.format("Selected month (%d) must be between 1 and 12", new Object[]{Integer.valueOf(selectedMonth)}));
        }
        monthNumberPicker.setValue(selectedMonth);
        monthNumberPicker.setOnValueChangedListener(dateChangeListener);
        dayNumberPicker.setMinValue(1);
        dayNumberPicker.setMaxValue(31);
        int selectedDay = a.getInteger(R.styleable.PersianDatePicker_selectedDay, pCalendar.getPersianDay());
        if (selectedDay > 31 || selectedDay < 1) {
            throw new IllegalArgumentException(String.format("Selected day (%d) must be between 1 and 31", new Object[]{Integer.valueOf(selectedDay)}));
        }
        if (selectedMonth > 6 && selectedMonth < 12 && selectedDay == 31) {
            selectedDay = 30;
        } else if (PersianCalendarUtils.isPersianLeapYear(selectedYear) && selectedDay == 31) {
            selectedDay = 30;
        } else if (selectedDay > 29) {
            selectedDay = 29;
        }
        dayNumberPicker.setValue(selectedDay);
        dayNumberPicker.setOnValueChangedListener(dateChangeListener);
        displayDescription = a.getBoolean(R.styleable.PersianDatePicker_displayDescription, false);
        if (displayDescription) {
            descriptionTextView.setVisibility(VISIBLE);
        }
        a.recycle();
    }

    public void setOnDateChangedListener(OnDateChangedListener onDateChangedListener) {
        mListener = onDateChangedListener;
    }

    public Date getDisplayDate() {
        PersianCalendar displayPersianDate = new PersianCalendar();
        displayPersianDate.setPersianDate(yearNumberPicker.getValue(), monthNumberPicker.getValue(), dayNumberPicker.getValue());
        return displayPersianDate.getTime();
    }

    public void setDisplayDate(Date displayDate) {
        setDisplayPersianDate(new PersianCalendar(displayDate.getTime()));
    }

    public PersianCalendar getDisplayPersianDate() {
        PersianCalendar displayPersianDate = new PersianCalendar();
        displayPersianDate.setPersianDate(yearNumberPicker.getValue(), monthNumberPicker.getValue(), dayNumberPicker.getValue());
        return displayPersianDate;
    }

    public void setDisplayPersianDate(PersianCalendar displayPersianDate) {
        int year = displayPersianDate.getPersianYear();
        int month = displayPersianDate.getPersianMonth();
        int day = displayPersianDate.getPersianDay();
        if (month > 6 && month < 12 && day == 31) {
            day = 30;
        } else if (PersianCalendarUtils.isPersianLeapYear(year) && day == 31) {
            day = 30;
        } else if (day > 29) {
            day = 29;
        }
        dayNumberPicker.setValue(day);
        minYear = year - yearRange;
        maxYear = yearRange + year;
        yearNumberPicker.setMinValue(minYear);
        yearNumberPicker.setMaxValue(maxYear);
        yearNumberPicker.setValue(year);
        monthNumberPicker.setValue(month);
        dayNumberPicker.setValue(day);
    }

    protected Parcelable onSaveInstanceState() {
        SavedState ss = new SavedState(super.onSaveInstanceState());
        ss.datetime = getDisplayDate().getTime();
        return ss;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState ss = (SavedState) state;
            super.onRestoreInstanceState(ss.getSuperState());
            setDisplayDate(new Date(ss.datetime));
            return;
        }
        super.onRestoreInstanceState(state);
    }
}
