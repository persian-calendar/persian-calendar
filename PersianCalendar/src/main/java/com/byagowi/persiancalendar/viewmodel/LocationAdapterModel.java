package com.byagowi.persiancalendar.viewmodel;

import android.view.View;

import androidx.lifecycle.ViewModel;

public class LocationAdapterModel extends ViewModel {
    final public String city;
    final public String country;

    final public View.OnClickListener callback;

    public LocationAdapterModel(String city, String country, View.OnClickListener callback) {
        this.city = city;
        this.country = country;

        this.callback = callback;
    }
}
