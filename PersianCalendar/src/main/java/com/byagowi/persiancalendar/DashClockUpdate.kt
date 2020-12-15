package com.byagowi.persiancalendar

import android.content.Intent
import com.byagowi.persiancalendar.ui.MainActivity
import com.byagowi.persiancalendar.utils.*
import com.google.android.apps.dashclock.api.DashClockExtension
import com.google.android.apps.dashclock.api.ExtensionData

class DashClockUpdate : DashClockExtension() {
    override fun onUpdateData(reason: Int) {
        setUpdateWhenScreenOn(true)
        val jdn = getTodayJdn()
        val date = getDateFromJdnOfCalendar(mainCalendar, jdn)
        publishUpdate(
                ExtensionData().visible(true)
                        .icon(getDayIconResource(date.dayOfMonth))
                        .status(getMonthName(date))
                        .expandedTitle(dayTitleSummary(date))
                        .expandedBody(dateStringOfOtherCalendars(jdn, spacedComma))
                        .clickIntent(
                                Intent(applicationContext, MainActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
        )
    }
}
