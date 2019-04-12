package com.byagowi.persiancalendar.ui.accounting.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.ui.accounting.calendar.PersianCalendar;
import com.byagowi.persiancalendar.ui.accounting.database.DatabaseHandler;
import java.util.ArrayList;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class RangeReportActivity extends Activity implements OnTouchListener {

    DatabaseHandler db;
    String calDateEnd, calDateStart, calWeekDayEnd, calWeekDayStart;
    Button dateEnd, dateStart;
    int dateEndRequestCode = 3031;
    int dateStartRequestCode = 3030;
    LinearLayout reportContent, reportDetailsContent, RangeReport, RangeReportDetails;
    TextView reportCost, reportDetailsSign, reportIncome, reportSign, reportSum, reportDetection;

    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounting_range_report);
        
        db = new DatabaseHandler(getApplicationContext());
        db.open();

        dateStart = findViewById(R.id.buttonDateStart);
        dateEnd = findViewById(R.id.buttonDateEnd);
        reportContent = findViewById(R.id.layoutRangeReportContent);
        reportSign = findViewById(R.id.textViewReportPlusSign);
        reportCost = findViewById(R.id.textViewReportCost);
        reportIncome = findViewById(R.id.textViewReportIncome);
        reportSum = findViewById(R.id.textViewReportSum);
        reportDetection = findViewById(R.id.textViewReportDetection);
        reportDetailsContent = findViewById(R.id.layoutRangeReportDetailsContent);
        reportDetailsSign = findViewById(R.id.textViewReportDetailsPlusSign);
        dateStart.setOnTouchListener(this);
        dateEnd.setOnTouchListener(this);

        RangeReport = findViewById(R.id.layoutRangeReport);
        RangeReport.setOnClickListener(arg0 -> {
            if (reportContent.getVisibility() == View.VISIBLE) {
                reportContent.setVisibility(View.GONE);
                reportSign.setText(getResources().getString(R.string.plus_sign));
            } else if (reportContent.getVisibility() == View.GONE) {
                reportContent.setVisibility(View.VISIBLE);
                reportSign.setText(getResources().getString(R.string.minus_sign));
            }
        });

        RangeReportDetails = findViewById(R.id.layoutRangeReportDetails);
        RangeReportDetails.setOnClickListener(arg0 -> {
            if (reportDetailsContent.getVisibility() == View.VISIBLE) {
                reportDetailsContent.setVisibility(View.GONE);
                reportDetailsSign.setText(getResources().getString(R.string.plus_sign));
            } else if (reportDetailsContent.getVisibility() == View.GONE) {
                reportDetailsContent.setVisibility(View.VISIBLE);
                reportDetailsSign.setText(getResources().getString(R.string.minus_sign));
            }
        });
        
        showTodayDate();
        showRangeReport();
    }


    @SuppressLint("SetTextI18n")
    private void showTodayDate() {
        PersianCalendar today = new PersianCalendar();
        String mToday = null;
        String mMonth = null;
        if (today.getPersianDay() < 10) {
            mToday = "0" + Integer.toString(today.getPersianDay());
        } else if (today.getPersianDay() >= 10) {
            mToday = Integer.toString(today.getPersianDay());
        }
        if (today.getPersianMonth() < 10) {
            mMonth = "0" + Integer.toString(today.getPersianMonth());
        } else if (today.getPersianMonth() >= 10) {
            mMonth = Integer.toString(today.getPersianMonth());
        }
        calDateStart = today.getPersianYear() + "/" + mMonth + "/" + mToday;
        calWeekDayStart = today.getPersianWeekDayName();
        calDateEnd = today.getPersianYear() + "/" + mMonth + "/" + mToday;
        calWeekDayEnd = today.getPersianWeekDayName();
        dateStart.setText(calWeekDayStart + "   " + calDateStart);
        dateEnd.setText(calWeekDayEnd + "   " + calDateEnd);
    }

    public void onResume() {
        super.onResume();
        this.db.open();
    }

    public void onPause() {
        super.onPause();
        this.db.close();
    }

    public void onStop() {
        super.onStop();
        this.db.close();
    }

    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case 0:
                if (v != this.dateStart) {
                    if (v == this.dateEnd) {
                        this.dateEnd.setBackgroundColor(getResources().getColor(R.color.accents));
                        this.dateEnd.setTextColor(getResources().getColor(R.color.white));
                        break;
                    }
                }
                this.dateStart.setBackgroundColor(getResources().getColor(R.color.accents));
                this.dateStart.setTextColor(getResources().getColor(R.color.white));
            break;
            case 1:
                if (v != this.dateStart) {
                    if (v == this.dateEnd) {
                        this.dateEnd.setBackgroundColor(getResources().getColor(R.color.date_button_background));
                        this.dateEnd.setTextColor(getResources().getColor(R.color.black));
                        startActivityForResult(new Intent(getApplicationContext(), PickDateActivity.class), this.dateEndRequestCode);
                        break;
                    }
                }
                this.dateStart.setBackgroundColor(getResources().getColor(R.color.date_button_background));
                this.dateStart.setTextColor(getResources().getColor(R.color.black));
                startActivityForResult(new Intent(getApplicationContext(), PickDateActivity.class), this.dateStartRequestCode);
            break;
        }
        return true;
    }

    @SuppressLint("SetTextI18n")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.db.open();
        if (requestCode == this.dateStartRequestCode) {
            if (resultCode == -1) {
                this.calDateStart = data.getStringExtra("chosenDate");
                this.calWeekDayStart = data.getStringExtra("chosenDay");
                this.dateStart.setText(this.calWeekDayStart + "   " + this.calDateStart);
                showRangeReport();
            }
        } else if (requestCode == this.dateEndRequestCode && resultCode == -1) {
            this.calDateEnd = data.getStringExtra("chosenDate");
            this.calWeekDayEnd = data.getStringExtra("chosenDay");
            this.dateEnd.setText(this.calWeekDayEnd + "   " + this.calDateEnd);
            if (this.calDateStart.compareTo(this.calDateEnd) > 0) {
                Toast.makeText(getApplicationContext(), getString(R.string.note_for_date), Toast.LENGTH_SHORT).show();
            }
            showRangeReport();
        }
    }

    @SuppressLint("SetTextI18n")
    private void showRangeReport() {
        this.reportCost.setText(getResources().getString(R.string.cost_sum) + this.db.calculateRangeCost(this.calDateStart, this.calDateEnd));
        this.reportIncome.setText(getResources().getString(R.string.income_sum) + this.db.calculateRangeIncome(this.calDateStart, this.calDateEnd));
        int sum = this.db.calculateRangeIncome(this.calDateStart, this.calDateEnd) - this.db.calculateRangeCost(this.calDateStart, this.calDateEnd);
        this.reportSum.setText(getResources().getString(R.string.report_sum) + Integer.toString(sum));
        String Detection;
        if (sum < 0) {
            Detection = getString(R.string.cost);
        } else if (sum > 0) {
            Detection = getString(R.string.income);
        } else {
            Detection = getString(R.string.underscore);
        }
        reportDetection.setText(getResources().getString(R.string.report_detection) + Detection);
        ArrayList<String> costsDetails = this.db.DetailsCost(this.calDateStart, this.calDateEnd);
        ArrayList<String> incomesDetails = this.db.DetailsIncome(this.calDateStart, this.calDateEnd);
        reportDetailsContent.removeAllViews();
        if (incomesDetails.size() == 0 && costsDetails.size() == 0) {
            TextView textView = new TextView(this);
            textView.setText(getResources().getString(R.string.no_info));
            textView.setTextSize(getResources().getDimension(R.dimen.report_text_size));
            textView.setGravity(17);
            this.reportDetailsContent.addView(textView);
            return;
        }
        int i;
        int padding = (int) getResources().getDimension(R.dimen.content_padding);
        LayoutParams ParamLayputParams = new LayoutParams(0, -2, 0.5f);
        LayoutParams PriceLayoutParams = new LayoutParams(0, -2, 0.5f);
        LayoutParams costsIncomesLayoutParams = new LayoutParams(-1, -2);
        costsIncomesLayoutParams.setMargins(0, 0, 0, 15);
        LinearLayout textView = new LinearLayout(this);
        textView.setOrientation(LinearLayout.VERTICAL);
        textView.setPadding(padding, padding, padding, padding);
        reportDetailsContent.addView(textView, costsIncomesLayoutParams);
        textView = new LinearLayout(this);
        textView.setOrientation(LinearLayout.VERTICAL);
        textView.setPadding(padding, padding, padding, padding);
        reportDetailsContent.addView(textView, costsIncomesLayoutParams);
        textView.removeAllViews();
        textView.removeAllViews();
        TextView costsCaption = new TextView(this);
        costsCaption.setText(getResources().getString(R.string.costs_colon));
        costsCaption.setTextSize(getResources().getDimension(R.dimen.report_caption_size));
        costsCaption.setTextColor(getResources().getColor(R.color.caption_color));
        textView.addView(costsCaption);
        for (i = 0; i < costsDetails.size(); i += 2) {
            LinearLayout costsLayout = new LinearLayout(this);
            costsLayout.setOrientation(LinearLayout.HORIZONTAL);
            costsLayout.setWeightSum(1.0f);
            TextView costsPrice = new TextView(this);
            costsPrice.setText(costsDetails.get(i + 1));
            costsPrice.setPadding(10, 10, 0, 10);
            costsPrice.setTextColor(getResources().getColor(R.color.black));
            costsPrice.setTextSize(getResources().getDimension(R.dimen.report_text_size));
            costsLayout.addView(costsPrice, PriceLayoutParams);
            TextView costsParam = new TextView(this);
            costsParam.setText(costsDetails.get(i));
            costsParam.setGravity(5);
            costsParam.setPadding(10, 10, 0, 10);
            costsParam.setTextSize(getResources().getDimension(R.dimen.report_text_size));
            costsLayout.addView(costsParam, ParamLayputParams);
            textView.addView(costsLayout);
        }
        TextView incomesCaption = new TextView(this);
        incomesCaption.setText(getResources().getString(R.string.income_colon));
        incomesCaption.setTextSize(getResources().getDimension(R.dimen.report_caption_size));
        incomesCaption.setTextColor(getResources().getColor(R.color.caption_color));
        textView.addView(incomesCaption);
        for (i = 0; i < incomesDetails.size(); i += 2) {
            LinearLayout incomeLayout = new LinearLayout(this);
            incomeLayout.setOrientation(LinearLayout.HORIZONTAL);
            incomeLayout.setWeightSum(1.0f);
            TextView incomePrice = new TextView(this);
            incomePrice.setText(incomesDetails.get(i + 1));
            incomePrice.setPadding(10, 10, 0, 10);
            incomePrice.setTextColor(getResources().getColor(R.color.black));
            incomePrice.setTextSize(getResources().getDimension(R.dimen.report_text_size));
            incomeLayout.addView(incomePrice, PriceLayoutParams);
            TextView incomeParam = new TextView(this);
            incomeParam.setText(incomesDetails.get(i));
            incomeParam.setGravity(5);
            incomeParam.setPadding(10, 10, 0, 10);
            incomeParam.setTextSize(getResources().getDimension(R.dimen.report_text_size));
            incomeLayout.addView(incomeParam, ParamLayputParams);
            textView.addView(incomeLayout);
        }
    }
}

