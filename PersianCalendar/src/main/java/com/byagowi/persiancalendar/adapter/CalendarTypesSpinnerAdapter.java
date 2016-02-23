package com.byagowi.persiancalendar.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.enums.CalendarTypeEnum;
import com.byagowi.persiancalendar.locale.CalendarStrings;

import java.util.HashMap;
import java.util.Map;

public class CalendarTypesSpinnerAdapter extends ArrayAdapter {
    private Utils utils;

    // so there's an order
    private CalendarTypeEnum[] calendarTypeKeys = new CalendarTypeEnum[]{
            CalendarTypeEnum.SHAMSI,
            CalendarTypeEnum.ISLAMIC,
            CalendarTypeEnum.GEORGIAN
    };
    private Map<CalendarTypeEnum, String> calendarTypes = new HashMap<>();
    private int spinnerResource;

    public CalendarTypesSpinnerAdapter(Context context, int resource) {
        super(context, resource);
        utils = Utils.getInstance(context);
        utils.loadLanguageFromSettings();

        spinnerResource = resource;
        calendarTypes.put(CalendarTypeEnum.SHAMSI, utils.getString(Constants.HIJRI_SHAMSI));
        calendarTypes.put(CalendarTypeEnum.ISLAMIC, utils.getString(Constants.HIJRI_QAMARI));
        calendarTypes.put(CalendarTypeEnum.GEORGIAN, utils.getString(Constants.GEORGIAN));
    }

    @Override
    public int getCount() {
        return calendarTypes.size();
    }

    @Override
    public Object getItem(int position) {
        return calendarTypeKeys[position];
    }

    public View getSpinnerItemView(int position, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TextView textView = (TextView) inflater.inflate(spinnerResource, parent, false);
        textView.setText(calendarTypes.get(calendarTypeKeys[position]));
        utils.prepareShapeTextView(textView);
        return textView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getSpinnerItemView(position, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getSpinnerItemView(position, parent);
    }
}
