package com.byagowi.persiancalendar.ui.accounting.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.ui.accounting.AccountingFragment;
import com.byagowi.persiancalendar.ui.accounting.database.DatabaseHandler;
import com.byagowi.persiancalendar.ui.accounting.tool.ThousandsSeparators;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class AddCostActivity extends Activity {
    EditText costText;
    String date;
    DatabaseHandler db;
    int paramId;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounting_add_cost);
        db = new DatabaseHandler(this);
        db.open();
        costText = findViewById(R.id.editTextAddCost);
        costText.addTextChangedListener(new ThousandsSeparators(costText));
        Intent mainInt = getIntent();
        date = mainInt.getStringExtra("Date");
        paramId = mainInt.getIntExtra("ParamId", 0);
        if (paramId >= 1000) {
            costText.setHint(AccountingFragment.currentCI + " - ");
        } else {
            costText.setHint(AccountingFragment.currentCI + " + ");
        }

        Button button = findViewById(R.id.buttonAddCostInsert);
        button.setOnClickListener(v -> {
            String mCostText = costText.getText().toString().replace(",","");
            if (this.costText.getText().toString().isEmpty()) {
                costText.setError(getString(R.string.dont_added_value));
                return;
            }
            if (paramId >= 1000) {
                db.editCost(this.paramId - 1000, Integer.parseInt(mCostText), date);
            } else {
                db.insertCost(this.paramId, Integer.parseInt(mCostText), date);
            }
            db.close();
            Intent costResInt = new Intent();
            costResInt.putExtra("priceAdded", mCostText);
            setResult(-1, costResInt);
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

    public void onStop() {
        super.onStop();
        db.close();
    }
}
