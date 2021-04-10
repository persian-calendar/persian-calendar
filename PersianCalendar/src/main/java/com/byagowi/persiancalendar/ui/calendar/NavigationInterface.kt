package com.byagowi.persiancalendar.ui.calendar

import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LifecycleOwner

interface NavigationInterface {
    fun setupToolbarWithDrawer(viewLifecycleOwner: LifecycleOwner, toolbar: Toolbar)
    fun restartActivity()
}
