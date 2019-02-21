package com.byagowi.persiancalendar.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainActivityModel extends ViewModel {
    public final MutableLiveData<Void> preferenceUpdateHandler = new MutableLiveData<>();

    public void preferenceIsUpdate() {
        preferenceUpdateHandler.postValue(null);
    }
}
