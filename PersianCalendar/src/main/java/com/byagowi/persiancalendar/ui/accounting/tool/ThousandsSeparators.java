package com.byagowi.persiancalendar.ui.accounting.tool;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class ThousandsSeparators implements TextWatcher {

    private DecimalFormat df;
    private DecimalFormat dFnd;
    private boolean hasFractionalPart;

    private EditText et;

    public ThousandsSeparators(EditText et) {
        df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ENGLISH);
        df.setDecimalSeparatorAlwaysShown(true);
        dFnd = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ENGLISH);
        this.et = et;
        hasFractionalPart = false;
    }

    @Override
    public void afterTextChanged(Editable s) {
        et.removeTextChangedListener(this);

        try {
            int iniLen, endLen;
            iniLen = et.getText().length();

            String v = s.toString().replace(String.valueOf(df.getDecimalFormatSymbols().getGroupingSeparator()), "");
            Number n = df.parse(v);
            int cp = et.getSelectionStart();
            if (hasFractionalPart) {
                et.setText(df.format(n));
            } else {
                et.setText(dFnd.format(n));
            }
            endLen = et.getText().length();
            int sel = (cp + (endLen - iniLen));
            if (sel > 0 && sel <= et.getText().length()) {
                et.setSelection(sel);
            } else {
                //Place cursor at the end?
                et.setSelection(et.getText().length() - 1);
            }
        } catch (NumberFormatException | ParseException nfe) {
            //Do nothing?
        }

        et.addTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        hasFractionalPart = s.toString().contains(String.valueOf(df.getDecimalFormatSymbols().getDecimalSeparator()));
    }

}