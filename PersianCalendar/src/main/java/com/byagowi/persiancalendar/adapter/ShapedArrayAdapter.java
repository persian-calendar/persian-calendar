package com.byagowi.persiancalendar.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.byagowi.persiancalendar.R;

/**
 * Created by ebraminio on 2/17/16.
 */
public class ShapedArrayAdapter<String> extends ArrayAdapter<String> {

    // preferred drop down list item padding, used for spinners and such
    int padding;

    public ShapedArrayAdapter(Context context, int resource, String[] objects) {
        super(context, resource, objects);
        padding = (int) getContext().getResources().getDimension(R.dimen.listPreferredItemPadding);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        view.setPadding(padding, 0, padding, 0);
        return view;
    }
}
