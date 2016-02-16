package com.byagowi.persiancalendar.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.byagowi.persiancalendar.Utils;
import com.byagowi.persiancalendar.enums.CalendarType;
import com.byagowi.persiancalendar.locale.CalendarStrings;

import java.util.HashMap;
import java.util.Map;

public class CalendarTypesSpinnerAdapter extends ArrayAdapter {
    private Utils utils;

    // so there's an order
    private CalendarType[] calendarTypeKeys = new CalendarType[]{
            CalendarType.SHAMSI,
            CalendarType.ISLAMIC,
            CalendarType.GEORGIAN
    };
    private Map<CalendarType, String> calendarTypes = new HashMap<>();
    private int spinnerResource;

    public CalendarTypesSpinnerAdapter(Context context, int resource) {
        super(context, resource);
        utils = Utils.getInstance();
        utils.loadLanguageFromSettings(context);

        spinnerResource = resource;
        calendarTypes.put(CalendarType.SHAMSI, utils.getString(CalendarStrings.HIJRI_SHAMSI));
        calendarTypes.put(CalendarType.ISLAMIC, utils.getString(CalendarStrings.HIJRI_QAMARI));
        calendarTypes.put(CalendarType.GEORGIAN, utils.getString(CalendarStrings.GEORGIAN));
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
        utils.prepareShapeTextView(getContext(), textView);
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
