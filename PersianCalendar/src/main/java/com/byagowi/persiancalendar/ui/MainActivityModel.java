package com.byagowi.persiancalendar.ui;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainActivityModel extends ViewModel {
    public final MutableLiveData<Void> preferenceUpdateHandler = new MutableLiveData<>();

    void preferenceIsUpdated() {
        preferenceUpdateHandler.postValue(null);
    }
}
