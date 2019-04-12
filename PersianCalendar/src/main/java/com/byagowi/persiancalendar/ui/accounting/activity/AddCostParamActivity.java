package com.byagowi.persiancalendar.ui.accounting.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.ui.accounting.database.DatabaseHandler;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class AddCostParamActivity extends Activity {
    EditText captionText;
    DatabaseHandler db;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounting_add_cost_param);
        db = new DatabaseHandler(getApplicationContext());
        db.open();

        Button button = findViewById(R.id.buttonAddCostParamSave);
        button.setOnClickListener(v -> {
            captionText = findViewById(R.id.editTextAddCostParam);
            if (captionText.getText().toString().trim().length() > 0) {
                db.insertCostCaption(captionText.getText().toString());
                Intent returnCost = new Intent();
                returnCost.putExtra("resultCost", captionText.getText().toString());
                setResult(-1, returnCost);
                db.close();
                finish();
                return;
            }
            captionText.setError(getString(R.string.please_add_subtitle));
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
