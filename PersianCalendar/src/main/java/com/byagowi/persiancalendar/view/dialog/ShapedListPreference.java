package com.byagowi.persiancalendar.view.dialog;

import android.content.Context;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.TextView;

import com.byagowi.persiancalendar.Utils;

/**
 * Created by ebraminio on 2/16/16.
 */
public class ShapedListPreference extends ListPreference {
    public ShapedListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ShapedListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ShapedListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShapedListPreference(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        Utils.getInstance(getContext()).prepareShapePreference(holder);
    }
}
