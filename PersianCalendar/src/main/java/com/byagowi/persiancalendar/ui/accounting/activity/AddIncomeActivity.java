package com.byagowi.persiancalendar.ui.accounting.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.ui.accounting.AccountingFragment;
import com.byagowi.persiancalendar.ui.accounting.database.DatabaseHandler;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class AddIncomeActivity extends Activity {
    String date;
    DatabaseHandler db;
    EditText incomeText;
    int paramId;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounting_add_income);
        db = new DatabaseHandler(this);
        db.open();
        incomeText = findViewById(R.id.editTextAddIncome);
        Intent mainInt = getIntent();
        date = mainInt.getStringExtra("Date");
        paramId = mainInt.getIntExtra("ParamId", 0);
        if (paramId >= 1000) {
            incomeText.setHint(AccountingFragment.currentCI + " - ");
        } else {
            incomeText.setHint(AccountingFragment.currentCI + " + ");
        }

        Button button = findViewById(R.id.buttonAddIncomeInsert);
        button.setOnClickListener(v -> {
            if (incomeText.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), R.string.dont_added_value, Toast.LENGTH_SHORT).show();
                return;
            }
            if (paramId >= 1000) {
                db.editIncome(paramId - 1000, Integer.parseInt(incomeText.getText().toString()), date);
            } else {
                db.insertIncome(paramId, Integer.parseInt(incomeText.getText().toString()), date);
            }
            db.close();
            Intent incomeResInt = new Intent();
            incomeResInt.putExtra("priceAdded", incomeText.getText().toString());
            setResult(-1, incomeResInt);
            finish();
        });
    }

    protected void onResume() {
        super.onResume();
        db.open();
    }

    protected void onPause() {
        super.onPause();
        db.close();
    }

    protected void onStop() {
        super.onStop();
        db.close();
    }
}
