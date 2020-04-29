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
    private val _titleAndSubtitle by lazy {
        MutableLiveData<Pair<String, String>>()
    }
    val actionBarTitleAndSubtitle: LiveData<Pair<String, String>>
        get() = _titleAndSubtitle

    fun updateActionBar(title: String, subtitle: String) =
        _titleAndSubtitle.postValue(Pair(title, subtitle))

}