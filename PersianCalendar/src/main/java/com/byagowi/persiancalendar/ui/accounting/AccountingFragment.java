package com.byagowi.persiancalendar.ui.accounting;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.ui.accounting.activity.AddCostActivity;
import com.byagowi.persiancalendar.ui.accounting.activity.AddCostParamActivity;
import com.byagowi.persiancalendar.ui.accounting.activity.AddIncomeActivity;
import com.byagowi.persiancalendar.ui.accounting.activity.AddIncomeParamActivity;
import com.byagowi.persiancalendar.ui.accounting.activity.PickDateActivity;
import com.byagowi.persiancalendar.ui.accounting.activity.RangeReportActivity;
import com.byagowi.persiancalendar.ui.accounting.calendar.PersianCalendar;
import com.byagowi.persiancalendar.ui.accounting.database.DatabaseHandler;
import com.byagowi.persiancalendar.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class AccountingFragment extends Fragment implements OnTouchListener, OnClickListener {
    public static String currentCI = "0";
    private static int editButtonId = 0;
    private static int editTextId = 0;
    private ArrayList<Button> allEdButton = new ArrayList();
    private ArrayList<EditText> allEds = new ArrayList();
    private Button btnAddCost, btnAddIncome;
    private String calDate, calWeekDay;
    private LinearLayout costsContent, incomeContent, DailyReportContent, TotalReportContent;
    private TextView costsSign, incomeSign, reportCaption, reportTotalCaption, reportCost, reportTotalCost, reportIncome, reportTotalIncome, reportSign, reportTotalSign, reportSum, reportTotalSum, reportDetection, reportTotalDetection;
    private EditText dateText, newEditText;
    private DatabaseHandler db;

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_accounting, container, false);
        db = new DatabaseHandler(getActivity().getApplicationContext());
        db.open();
        try {
            ViewConfiguration config = ViewConfiguration.get(getActivity());
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            menuKeyField.setAccessible(true);
            menuKeyField.setBoolean(config, false);
        } catch (Exception ignored) {
        }

        costsContent = view.findViewById(R.id.layoutCostsContent);
        incomeContent = view.findViewById(R.id.layoutIncomeContent);
        DailyReportContent = view.findViewById(R.id.layoutRangeReportContent);
        TotalReportContent = view.findViewById(R.id.layoutTotalRangeReportContent);
        costsSign = view.findViewById(R.id.textViewCostsPlusSign);
        incomeSign = view.findViewById(R.id.textViewIncomePlusSign);
        reportSign = view.findViewById(R.id.textViewReportPlusSign);
        reportTotalSign = view.findViewById(R.id.textViewTotalReportPlusSign);
        dateText = view.findViewById(R.id.editTextPickDate);
        dateText.setInputType(0);
        btnAddCost = view.findViewById(R.id.buttonAddCostParam);
        btnAddIncome = view.findViewById(R.id.buttonAddIncomeParam);
        reportCaption = view.findViewById(R.id.textViewReportCaption);
        reportTotalCaption = view.findViewById(R.id.textViewTotalReportCaption);
        reportCost = view.findViewById(R.id.textViewReportCost);
        reportTotalCost = view.findViewById(R.id.textViewTotalReportCost);
        reportIncome = view.findViewById(R.id.textViewReportIncome);
        reportTotalIncome = view.findViewById(R.id.textViewTotalReportIncome);
        reportSum = view.findViewById(R.id.textViewReportSum);
        reportTotalSum = view.findViewById(R.id.textViewTotalReportSum);
        reportDetection = view.findViewById(R.id.textViewReportDetection);
        reportTotalDetection = view.findViewById(R.id.textViewTotalReportDetection);

        dateText.setOnTouchListener(this);
        btnAddCost.setOnTouchListener(this);
        btnAddIncome.setOnTouchListener(this);

        LinearLayout expense = view.findViewById(R.id.layoutCosts);
        expense.setOnClickListener(arg0 -> {
            if (costsContent.getVisibility() == View.GONE) {
                costsContent.setVisibility(View.VISIBLE);
                costsSign.setText(R.string.minus_sign);
            } else if (costsContent.getVisibility() == View.VISIBLE) {
                costsContent.setVisibility(View.GONE);
                costsSign.setText(R.string.plus_sign);
            }
        });

        LinearLayout income = view.findViewById(R.id.layoutIncome);
        income.setOnClickListener(arg0 -> {
            if (incomeContent.getVisibility() == View.GONE) {
                incomeContent.setVisibility(View.VISIBLE);
                incomeSign.setText(R.string.minus_sign);
            } else if (incomeContent.getVisibility() == View.VISIBLE) {
                incomeContent.setVisibility(View.GONE);
                incomeSign.setText(R.string.plus_sign);
            }
        });

        LinearLayout DailyReport = view.findViewById(R.id.layoutDailyReport);
        DailyReport.setOnClickListener(arg0 -> {
            if (DailyReportContent.getVisibility() == View.GONE) {
                DailyReportContent.setVisibility(View.VISIBLE);
                reportSign.setText("-");
            } else if (DailyReportContent.getVisibility() == View.VISIBLE) {
                DailyReportContent.setVisibility(View.GONE);
                reportSign.setText("+");
            }
        });

        LinearLayout TotalReport = view.findViewById(R.id.layoutTotalReport);
        TotalReport.setOnClickListener(arg0 -> {
            if (TotalReportContent.getVisibility() == View.GONE) {
                TotalReportContent.setVisibility(View.VISIBLE);
                reportTotalSign.setText("-");
            } else if (TotalReportContent.getVisibility() == View.VISIBLE) {
                TotalReportContent.setVisibility(View.GONE);
                reportTotalSign.setText("+");
            }
        });

        showCostParams();
        showIncomeParams();
        showTodayDate();
        showDailyReport(calDate);
        showTotalReport(calDate);

        return view;
    }

    @SuppressLint("SetTextI18n")
    private void showTodayDate() {
        PersianCalendar today = new PersianCalendar();
        String dayS = null;
        String monthS = null;
        if (today.getPersianDay() < 10) {
            dayS = "0" + Integer.toString(today.getPersianDay());
        } else if (today.getPersianDay() >= 10) {
            dayS = Integer.toString(today.getPersianDay());
        }
        if (today.getPersianMonth() < 10) {
            monthS = "0" + Integer.toString(today.getPersianMonth());
        } else if (today.getPersianMonth() >= 10) {
            monthS = Integer.toString(today.getPersianMonth());
        }
        calDate = today.getPersianYear() + "/" + monthS + "/" + dayS;
        calWeekDay = today.getPersianWeekDayName();
        dateText.setText(calWeekDay + "   " + calDate);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void onResume() {
        super.onResume();
        db.open();
        showPrices();
        for (int i = 0; i < allEds.size(); i++) {
            (allEds.get(i)).setOnTouchListener(this);
            (allEds.get(i)).setGravity(3);
            (allEds.get(i)).setTextColor(getResources().getColor(R.color.gray_dark));
            (allEdButton.get(i)).setOnTouchListener(this);
        }
    }

    @SuppressLint("SetTextI18n")
    private void showTotalReport(String date) {
        PersianCalendar today = new PersianCalendar();
        //String[] monthAndYear = db.getMonthAndYear(calDate);
        //reportTotalCaption.setText(getString(R.string.all_result) + monthAndYear[0] + getString(R.string.month) + monthAndYear[1]);
        reportTotalCaption.setText(getString(R.string.all_result));
        reportTotalCost.setText(getResources().getString(R.string.cost_sum) + db.calculateTotalCost(Integer.toString(today.getPersianMonth())));
        reportTotalIncome.setText(getResources().getString(R.string.income_sum) + db.calculateTotalIncome(Integer.toString(today.getPersianMonth())));
        int sum = db.calculateTotalIncome(Integer.toString(today.getPersianMonth())) - db.calculateTotalCost(Integer.toString(today.getPersianMonth()));
        reportTotalSum.setText(getResources().getString(R.string.report_sum) + sum);
        String Detection;
        if (sum < 0) {
            Detection = getString(R.string.cost);
        } else if (sum > 0) {
            Detection = getString(R.string.income);
        } else {
            Detection = getString(R.string.underscore);
        }
        reportTotalDetection.setText(getResources().getString(R.string.report_detection) + Detection);
    }

    @SuppressLint("SetTextI18n")
    private void showDailyReport(String date) {
        reportCaption.setText(getString(R.string.all_today_result) + date);
        reportCost.setText(getResources().getString(R.string.cost_sum) + db.calculateDailyCost(calDate));
        reportIncome.setText(getResources().getString(R.string.income_sum) + db.calculateDailyIncome(calDate));
        int sum = db.calculateDailyIncome(calDate) - db.calculateDailyCost(calDate);
        reportSum.setText(getResources().getString(R.string.report_sum) + sum);
        String Detection;
        if (sum < 0) {
            Detection = getString(R.string.cost);
        } else if (sum > 0) {
            Detection = getString(R.string.income);
        } else {
            Detection = getString(R.string.underscore);
        }
        reportDetection.setText(getResources().getString(R.string.report_detection) + Detection);
    }

    @SuppressLint("SetTextI18n")
    private void showPrices() {
        int i;
        for (i = 0; i < allEds.size(); i++) {
            (allEds.get(i)).setText("0");
        }
        if (!dateText.getText().toString().isEmpty() && db.getPrices(this.calDate).size() > 0 && db.getPricesParamID(calDate).size() > 0) {
            ArrayList<Integer> prices = db.getPrices(calDate);
            ArrayList<Integer> paramIDs = db.getPricesParamID(calDate);
            for (i = 0; i < allEds.size(); i++) {
                for (int j = 0; j < paramIDs.size(); j++) {
                    if ((allEds.get(i)).getId() == paramIDs.get(j)) {
                        (allEds.get(i)).setText(Integer.toString(prices.get(paramIDs.get(j) - 1)));
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void showIncomeParams() {
        LayoutParams labelParams = new LayoutParams(0, -2, 0.4f);
        LayoutParams textParams = new LayoutParams(0, -2, 0.6f);
        LayoutParams editPriceParams = new LayoutParams(0, -2, 0.2f);
        for (int i = 0; i < db.countIncomeParams(); i++) {
            LinearLayout newLayout = new LinearLayout(getActivity());
            newLayout.setOrientation(LinearLayout.HORIZONTAL);
            newLayout.setWeightSum(1.2f);
            TextView newTextView = new TextView(getActivity());
            newTextView.setText(db.displayIncomeParams(i));
            newTextView.setPadding(10, 0, 10, 0);
            newTextView.setGravity(17);
            EditText newEditText = new EditText(getActivity());
            newEditText.setId(db.getIncomeIds(i));
            allEds.add(newEditText);
            newEditText.setOnClickListener(this);
            Button newButton = new Button(getActivity());
            newButton.setId(db.getIncomeIds(i) + 1000);
            newButton.setOnTouchListener(this);
            newButton.setText(getResources().getString(R.string.underscore));
            newButton.setTextSize(getResources().getDimension(R.dimen.button_text_size));
            newButton.setBackgroundColor(getResources().getColor(R.color.transparent));
            newButton.setTextColor(getResources().getColor(R.color.button_color));
            allEdButton.add(newButton);
            newLayout.addView(newButton, editPriceParams);
            newLayout.addView(newEditText, textParams);
            newLayout.addView(newTextView, labelParams);
            incomeContent.addView(newLayout);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void showCostParams() {
        LayoutParams labelParams = new LayoutParams(0, -2, 0.4f);
        LayoutParams textParams = new LayoutParams(0, -2, 0.6f);
        LayoutParams editPriceParams = new LayoutParams(0, -2, 0.2f);
        for (int i = 0; i < db.countCostParams(); i++) {
            LinearLayout newLayout = new LinearLayout(getActivity());
            newLayout.setOrientation(LinearLayout.HORIZONTAL);
            newLayout.setWeightSum(1.2f);
            TextView newTextView = new TextView(getActivity());
            newTextView.setText(db.displayCostParams(i));
            newTextView.setPadding(10, 0, 10, 0);
            newTextView.setGravity(17);
            EditText newEditText = new EditText(getActivity());
            newEditText.setId(db.getCostIds(i));
            allEds.add(newEditText);
            Button newButton = new Button(getActivity());
            newButton.setId(db.getCostIds(i) + 1000);
            newButton.setOnTouchListener(this);
            newButton.setText(getResources().getString(R.string.underscore));
            newButton.setTextSize(getResources().getDimension(R.dimen.button_text_size));
            newButton.setBackgroundColor(getResources().getColor(R.color.transparent));
            newButton.setTextColor(getResources().getColor(R.color.button_color));
            allEdButton.add(newButton);
            newLayout.addView(newButton, editPriceParams);
            newLayout.addView(newEditText, textParams);
            newLayout.addView(newTextView, labelParams);
            costsContent.addView(newLayout);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case 0:
                Log.d("DOWN", "DOWN");
                if (v != btnAddCost) {
                    if (v == this.btnAddIncome) {
                        btnAddIncome.setBackgroundColor(getResources().getColor(R.color.accents));
                        btnAddIncome.setTextColor(getResources().getColor(R.color.white));
                        break;
                    }
                }
                btnAddCost.setBackgroundColor(getResources().getColor(R.color.accents));
                btnAddCost.setTextColor(getResources().getColor(R.color.white));
                break;
            case 1:
                Log.d("UP", "UP");
                if (v == this.btnAddCost) {
                    btnAddCost.setBackgroundColor(getResources().getColor(R.color.gray));
                    btnAddCost.setTextColor(getResources().getColor(R.color.accents));
                    startActivityForResult(new Intent(getActivity(), AddCostParamActivity.class), 2020);
                } else if (v == btnAddIncome) {
                    btnAddIncome.setBackgroundColor(getResources().getColor(R.color.gray));
                    btnAddIncome.setTextColor(getResources().getColor(R.color.accents));
                    startActivityForResult(new Intent(getActivity(), AddIncomeParamActivity.class), 2021);
                } else if (v == dateText) {
                    startActivityForResult(new Intent(getActivity().getApplicationContext(), PickDateActivity.class), 2022);
                }
                for (int i = 0; i < allEds.size(); i++) {
                    Intent costInt;
                    Intent incomeInt;
                    if (v.getId() == allEds.get(i).getId()) {
                        if (dateCorrectFormat(calDate)) {
                            editTextId = allEds.get(i).getId();
                            currentCI = allEds.get(i).getText().toString();
                            if (db.costOrIncome(editTextId) == "Cost") {
                                costInt = new Intent(getActivity(), AddCostActivity.class);
                                costInt.putExtra("Date", calDate);
                                costInt.putExtra("ParamId", editTextId);
                                startActivityForResult(costInt, this.allEds.get(i).getId());
                            } else if (db.costOrIncome(editTextId).equals("Income")) {
                                incomeInt = new Intent(getActivity(), AddIncomeActivity.class);
                                incomeInt.putExtra("Date", calDate);
                                incomeInt.putExtra("ParamId", editTextId);
                                startActivityForResult(incomeInt, (this.allEds.get(i)).getId());
                            }
                        } else {
                            Utils.createAndShowShortSnackbar(getView(), R.string.please_add_correct_date);
                        }
                    } else if (v.getId() == allEdButton.get(i).getId() && dateCorrectFormat(calDate)) {
                        editButtonId = allEdButton.get(i).getId();
                        editTextId = allEds.get(i).getId();
                        currentCI = allEds.get(i).getText().toString();
                        if (allEds.get(i).getText().toString().equals("0")) {
                            Utils.createAndShowShortSnackbar(getView(), R.string.please_add_first_value);
                        } else if (db.costOrIncome(editTextId).equals("Cost")) {
                            costInt = new Intent(getActivity(), AddCostActivity.class);
                            costInt.putExtra("Date", calDate);
                            costInt.putExtra("ParamId", editButtonId);
                            startActivityForResult(costInt, allEdButton.get(i).getId());
                        } else if (db.costOrIncome(editTextId).equals("Income")) {
                            incomeInt = new Intent(getActivity(), AddIncomeActivity.class);
                            incomeInt.putExtra("Date", calDate);
                            incomeInt.putExtra("ParamId", editButtonId);
                            startActivityForResult(incomeInt, allEdButton.get(i).getId());
                        }
                    }
                }
                break;
            case 2:
                Log.d("MOVE", "MOVE");
                break;
        }
        return true;
    }

    @SuppressLint("SetTextI18n")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.db.open();
        if (requestCode == 2020) {
            if (resultCode == -1) {
                addCostParam(data);
            }
        } else if (requestCode == 2021) {
            if (resultCode == -1) {
                addIncomeParam(data);
            }
        } else if (requestCode == 2022) {
            if (resultCode == -1) {
                dateText.setText(data.getStringExtra("chosenDay") + "   " + data.getStringExtra("chosenDate"));
                calDate = data.getStringExtra("chosenDate");
                calWeekDay = data.getStringExtra("chosenDay");
                showDailyReport(calDate);
                showTotalReport(calDate);
            }
        } else if (requestCode == editTextId) {
            if (resultCode == -1) {
                for (int i = 0; i < this.allEds.size(); i++) {
                    if (allEds.get(i).getId() == editTextId) {
                        //int pid = i;
                        break;
                    }
                }
                if (dateCorrectFormat(calDate)) {
                    showDailyReport(calDate);
                    showTotalReport(calDate);
                } else {
                    Utils.createAndShowShortSnackbar(getView(), R.string.please_add_correct_date);
                }
                db.close();
            }
        } else if (requestCode == editButtonId && resultCode == -1) {
            for (int i = 0; i < allEdButton.size(); i++) {
                if (allEdButton.get(i).getId() == editButtonId) {
                    //int pid = i;
                    break;
                }
            }
            if (dateCorrectFormat(calDate)) {
                showDailyReport(calDate);
                showTotalReport(calDate);
            } else {
                Utils.createAndShowShortSnackbar(getView(), R.string.please_add_correct_date);
            }
            db.close();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addIncomeParam(Intent iData) {
        LayoutParams labelParams = new LayoutParams(0, -2, 0.4f);
        LayoutParams textParams = new LayoutParams(0, -2, 0.6f);
        LayoutParams editPriceParams = new LayoutParams(0, -2, 0.2f);
        LinearLayout newLayout = new LinearLayout(getActivity());
        newLayout.setOrientation(LinearLayout.HORIZONTAL);
        newLayout.setWeightSum(1.2f);
        TextView newTextView = new TextView(getActivity());
        newTextView.setText(iData.getStringExtra("resultIncome"));
        newTextView.setPadding(10, 0, 10, 0);
        newTextView.setGravity(17);
        EditText newEditText = new EditText(getActivity());
        newEditText.setId(this.db.getIncomeIds(this.db.countIncomeParams() - 1));
        allEds.add(newEditText);
        Button newButton = new Button(getActivity());
        newButton.setId(this.db.getIncomeIds(this.db.countIncomeParams() - 1) + 1000);
        newButton.setOnTouchListener(this);
        newButton.setText(getResources().getString(R.string.underscore));
        newButton.setTextSize(getResources().getDimension(R.dimen.button_text_size));
        newButton.setBackgroundColor(getResources().getColor(R.color.transparent));
        newButton.setTextColor(getResources().getColor(R.color.button_color));
        allEdButton.add(newButton);
        newLayout.addView(newButton, editPriceParams);
        newLayout.addView(newEditText, textParams);
        newLayout.addView(newTextView, labelParams);
        incomeContent.addView(newLayout);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addCostParam(Intent iData) {
        LayoutParams labelParams = new LayoutParams(0, -2, 0.4f);
        LayoutParams textParams = new LayoutParams(0, -2, 0.6f);
        LayoutParams editPriceParams = new LayoutParams(0, -2, 0.2f);
        LinearLayout newLayout = new LinearLayout(getActivity());
        newLayout.setOrientation(LinearLayout.HORIZONTAL);
        newLayout.setWeightSum(1.2f);
        TextView newTextView = new TextView(getActivity());
        newTextView.setText(iData.getStringExtra("resultCost"));
        newTextView.setPadding(10, 0, 10, 0);
        newTextView.setGravity(17);
        EditText newEditText = new EditText(getActivity());
        newEditText.setId(this.db.getCostIds(this.db.countCostParams() - 1));
        allEds.add(newEditText);
        Button newButton = new Button(getActivity());
        newButton.setId(this.db.getCostIds(this.db.countCostParams() - 1) + 1000);
        newButton.setOnTouchListener(this);
        newButton.setText(getResources().getString(R.string.underscore));
        newButton.setTextSize(getResources().getDimension(R.dimen.button_text_size));
        newButton.setBackgroundColor(getResources().getColor(R.color.transparent));
        newButton.setTextColor(getResources().getColor(R.color.button_color));
        allEdButton.add(newButton);
        newLayout.addView(newButton, editPriceParams);
        newLayout.addView(newEditText, textParams);
        newLayout.addView(newTextView, labelParams);
        costsContent.addView(newLayout);
    }

    public void onPause() {
        super.onPause();
        db.close();
    }

    public void onStop() {
        super.onStop();
        db.close();
    }

    public void onClick(View v) {
        if (v.getId() == this.allEds.get(1).getId()) {
            Utils.createAndShowShortSnackbar(getView(), "text clicked");
        } else if (v.getId() == costsSign.getId()) {
            Utils.createAndShowShortSnackbar(getView(), "income clicked");
        } else if (v.getId() == dateText.getId()) {
            Utils.createAndShowShortSnackbar(getView(), "date click");
        }
    }

    private boolean dateCorrectFormat(String date) {
        char[] dateChar = date.toCharArray();
        if (dateChar.length != 10) {
            return false;
        }
        String day = Character.toString(dateChar[8]) + Character.toString(dateChar[9]);
        String month = Character.toString(dateChar[5]) + Character.toString(dateChar[6]);
        if (!tryParse(day) || !tryParse(month)) {
            return false;
        }
        return Integer.parseInt(day) >= 1 && Integer.parseInt(day) <= 31 && Integer.parseInt(month) >= 1 && Integer.parseInt(month) <= 12 && dateChar[4] == '/' && dateChar[7] == '/';
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.accounting_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.rangeReport:
                startActivityForResult(new Intent(getActivity(), RangeReportActivity.class), 2020);
                return false;
            default:
                break;
        }
        return false;
    }

    private boolean tryParse(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            Utils.createAndShowShortSnackbar(getView(), R.string.please_add_correct_date);
            return false;
        }
    }
}
