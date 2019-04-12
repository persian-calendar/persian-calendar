package com.byagowi.persiancalendar.ui.accounting.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.ui.accounting.calendar.PersianCalendar;
import com.byagowi.persiancalendar.ui.accounting.calendar.PersianDatePicker;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class PickDateActivity extends Activity implements OnTouchListener {
    Button btnDateSelect;
    public PersianDatePicker datePicker;
    int day, month, year;
    String dayS, monthS, weekDay;

    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounting_pick_date);
        btnDateSelect = findViewById(R.id.buttonSelectDate);
        btnDateSelect.setOnTouchListener(this);
        datePicker = findViewById(R.id.persianDatePick);
        datePicker.setDisplayPersianDate(new PersianCalendar());
    }

    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case 0:
                Log.d("DOWN", "DOWN");
                if (v == this.btnDateSelect) {
                    btnDateSelect.setBackgroundColor(getResources().getColor(R.color.date_button_background));
                    btnDateSelect.setTextColor(getResources().getColor(R.color.date_picker_select_dark));
                    break;
                }
                break;
            case 1:
                Log.d("UP", "UP");
                if (v == this.btnDateSelect) {
                    PersianCalendar pCal = this.datePicker.getDisplayPersianDate();
                    day = pCal.getPersianDay();
                    month = pCal.getPersianMonth();
                    year = pCal.getPersianYear();
                    if (day < 10) {
                        dayS = "0" + Integer.toString(this.day);
                    } else {
                        dayS = Integer.toString(this.day);
                    }
                    if (month < 10) {
                        monthS = "0" + Integer.toString(this.month);
                    } else {
                        monthS = Integer.toString(this.month);
                    }
                    weekDay = pCal.getPersianWeekDayName();
                    Intent dateResInt = new Intent();
                    dateResInt.putExtra("chosenDate", this.year + "/" + this.monthS + "/" + this.dayS);
                    dateResInt.putExtra("chosenDay", this.weekDay);
                    setResult(-1, dateResInt);
                    finish();
                    break;
                }
                break;
            case 2:
                Log.d("MOVE", "MOVE");
                break;
        }
        return true;
    }
}
