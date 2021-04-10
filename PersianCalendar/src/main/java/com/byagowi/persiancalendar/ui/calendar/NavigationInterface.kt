package com.byagowi.persiancalendar.ui.calendar

import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LifecycleOwner

interface NavigationInterface {
    fun onBurgerMenuClicked()
    fun setupToolbarIconWithDrawerToggleSync(viewLifecycleOwner: LifecycleOwner, toolbar: Toolbar)
    fun restartActivity()
}