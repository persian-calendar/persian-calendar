package com.byagowi.persiancalendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class CalendarTypesSpinnerAdapter extends ArrayAdapter {
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

        spinnerResource = resource;
        calendarTypes.put(CalendarType.SHAMSI, context.getString(R.string.hijri_shamsi));
        calendarTypes.put(CalendarType.ISLAMIC, context.getString(R.string.hijri_qamari));
        calendarTypes.put(CalendarType.GEORGIAN, context.getString(R.string.georgian));
    }

    @Override
    public int getCount() {
        return calendarTypes.size();
    }

    @Override
    public Object getItem(int position) {
        return calendarTypeKeys[position];
    }

    public View getSpinnerItemView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TextView textView = (TextView) inflater.inflate(spinnerResource, parent, false);
        textView.setText(calendarTypes.get(calendarTypeKeys[position]));
        return textView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getSpinnerItemView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getSpinnerItemView(position, convertView, parent);
    }
}
