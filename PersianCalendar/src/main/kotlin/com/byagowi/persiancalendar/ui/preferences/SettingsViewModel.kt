package com.byagowi.persiancalendar.ui.preferences

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel : ViewModel() {
    private val _selectedTab = MutableStateFlow(DEFAULT_SELECTED_TAB)
    val selectedTab: StateFlow<Int> get() = _selectedTab

    // Commands
    fun changeSelectedTab(index: Int) {
        _selectedTab.value = index
    }

    companion object {
        const val DEFAULT_SELECTED_TAB = -1
    }
}
