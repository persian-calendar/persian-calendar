package com.byagowi.persiancalendar.ui

import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LifecycleOwner

interface DrawerSetupContext {
    fun setupToolbarWithDrawer(viewLifecycleOwner: LifecycleOwner, toolbar: Toolbar)
}
