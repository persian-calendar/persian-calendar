package com.byagowi.persiancalendar.ui.preferences.locationathan.location;

import android.view.View;

import androidx.lifecycle.ViewModel;

public class LocationAdapterModel extends ViewModel {
    final public String city;
    final public String country;

    final public View.OnClickListener callback;

    LocationAdapterModel(String city, String country, View.OnClickListener callback) {
        this.city = city;
        this.country = country;

        this.callback = callback;
    }
}
