package com.byagowi.persiancalendar.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityModel : ViewModel() {
    val preferenceUpdateHandler = MutableLiveData<Void>()

    internal fun preferenceIsUpdated() {
        preferenceUpdateHandler.postValue(null)
    }
}
