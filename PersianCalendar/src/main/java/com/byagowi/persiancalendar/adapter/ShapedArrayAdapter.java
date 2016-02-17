package com.byagowi.persiancalendar.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.byagowi.persiancalendar.Utils;

import java.util.List;

/**
 * Created by ebraminio on 2/17/16.
 */
public class ShapedArrayAdapter extends ArrayAdapter {
    Context context;

    public ShapedArrayAdapter(Context context, int resource) {
        super(context, resource);
        this.context = context;
    }

    public ShapedArrayAdapter(Context context, int resource, List objects) {
        super(context, resource, objects);
        this.context = context;
    }

    public ShapedArrayAdapter(Context context, int resource, Object[] objects) {
        super(context, resource, objects);
        this.context = context;
    }

    public ShapedArrayAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
        this.context = context;
    }

    public ShapedArrayAdapter(Context context, int resource, int textViewResourceId, List objects) {
        super(context, resource, textViewResourceId, objects);
        this.context = context;
    }

    public ShapedArrayAdapter(Context context, int resource, int textViewResourceId, Object[] objects) {
        super(context, resource, textViewResourceId, objects);
        this.context = context;
    }

    Utils utils = Utils.getInstance();
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        if (view instanceof TextView) utils.prepareShapeTextView(context, (TextView) view);
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        if (view instanceof TextView) utils.prepareShapeTextView(context, (TextView) view);
        return view;
    }
}
