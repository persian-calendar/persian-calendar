package com.byagowi.persiancalendar.ui

import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LifecycleOwner

interface NavigationInterface {
    fun setupToolbarWithDrawer(viewLifecycleOwner: LifecycleOwner, toolbar: Toolbar)
    fun restartActivity()
}
