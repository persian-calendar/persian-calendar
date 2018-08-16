package com.byagowi.persiancalendar.viewmodel;

import android.view.View;

import androidx.annotation.IdRes;
import androidx.lifecycle.ViewModel;

public class DrawerItemViewModel extends ViewModel {
    @IdRes
    final public int icon;
    final public String title;
    final public String subtitle;
    final public int subtitleVisibility;

    final public View.OnClickListener callback;

    public DrawerItemViewModel(@IdRes int icon, String title, String subtitle,
                               View.OnClickListener callback) {
        this.icon = icon;
        this.title = title;
        this.subtitle = subtitle;
        this.subtitleVisibility = subtitle.length() == 0 ? View.GONE : View.VISIBLE;

        this.callback = callback;
    }
}
