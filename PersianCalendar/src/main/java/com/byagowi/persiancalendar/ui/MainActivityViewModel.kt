package com.byagowi.persiancalendar.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.byagowi.persiancalendar.utils.getTodayJdn

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    var clickedItem = 0
    var creationDateJdn: Long = getTodayJdn()
    var settingHasChanged = false
}