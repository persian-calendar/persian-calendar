package com.byagowi.persiancalendar.ui.reminder;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ReminderModel extends ViewModel {
    final MutableLiveData<Boolean> updateHandler = new MutableLiveData<>();

    public void update(boolean isNew) {
        updateHandler.postValue(isNew);
    }
}
