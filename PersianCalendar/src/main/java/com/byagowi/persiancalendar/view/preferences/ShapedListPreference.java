package com.byagowi.persiancalendar.view.preferences;

import android.content.Context;
import android.support.v7.preference.ListPreference;
import android.util.AttributeSet;

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

    public void setSelected(String selected) {
        final boolean wasBlocking = shouldDisableDependents();
        persistString(selected);
        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) notifyDependencyChange(isBlocking);
    }

    String defaultValue = "";

    // steal default value, well, not aware of a better way
    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        super.onSetInitialValue(restoreValue, defaultValue);
        this.defaultValue = (String) defaultValue;
    }

    public String getSelected() {
        return getPersistedString(defaultValue);
    }
}
