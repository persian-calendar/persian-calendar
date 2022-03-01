package com.byagowi.persiancalendar.ui.preferences

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class PreferencesViewModel : ViewModel() {

    // State
    private val _selectedTab = MutableStateFlow(DEFAULT_SELECTED_TAB)

    // Values
    val selectedTab: Int get() = _selectedTab.value

    // Events
    val selectedTabEvent: Flow<Int> get() = _selectedTab

    // Commands
    fun changeSelectedTab(index: Int) {
        _selectedTab.value = index
    }

    companion object {
        const val DEFAULT_SELECTED_TAB = -1
    }
}
