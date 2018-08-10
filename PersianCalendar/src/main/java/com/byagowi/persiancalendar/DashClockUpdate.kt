package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.util.UpdateUtils
import com.google.android.apps.dashclock.api.DashClockExtension

class DashClockUpdate : DashClockExtension() {

  override fun onUpdateData(reason: Int) {
    setUpdateWhenScreenOn(true)
    UpdateUtils.update(applicationContext, false)
    publishUpdate(UpdateUtils.getExtensionData())
  }

}
