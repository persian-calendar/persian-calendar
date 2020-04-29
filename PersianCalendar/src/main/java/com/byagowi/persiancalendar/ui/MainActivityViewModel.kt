package com.byagowi.persiancalendar.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.byagowi.persiancalendar.utils.getTodayJdn

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    var clickedItem = 0
    var creationDateJdn: Long = getTodayJdn()
    var settingHasChanged = false
    private val _title by lazy {
        MutableLiveData<String>()
    }
    private val _subTitle by lazy {
        MutableLiveData<String>()
    }
    val actionBarSubTitle: LiveData<String>
        get() = _subTitle
    val actionBarTitle: LiveData<String>
        get() = _title

    private fun updateActionBarTitle(title: String) = _title.postValue(title)
    private fun updateActionBarSubtitle(subtitle: String) = _subTitle.postValue(subtitle)
    fun updateActionBar(title: String, subtitle: String) {
        updateActionBarTitle(title)
        updateActionBarSubtitle(subtitle)
    }

}