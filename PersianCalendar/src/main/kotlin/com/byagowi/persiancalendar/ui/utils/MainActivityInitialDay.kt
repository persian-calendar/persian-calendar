package com.byagowi.persiancalendar.ui.utils;

import android.content.Intent
import android.provider.CalendarContract
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.utils.jdnActionKey
import com.byagowi.persiancalendar.utils.toCivilDate
import com.byagowi.persiancalendar.utils.toGregorianCalendar
import java.util.Date

fun initialDayFromIntent(intent: Intent?): Jdn? {
    val intent = intent ?: return null
    return initialDayFromAndroidCalendarIntent(intent) ?: run {
        Jdn(initialDayFromExtras(intent) ?: initialDayFromAction(intent) ?: return null)
    }
}

private fun initialDayFromExtras(intent: Intent): Long? =
    intent.getLongExtra(jdnActionKey, -1L).takeIf { it != -1L }

private fun initialDayFromAction(intent: Intent): Long? {
    val action = intent.action ?: return null
    if (!action.startsWith(jdnActionKey)) return null
    return action.replace(jdnActionKey, "").toLongOrNull()
}

private fun initialDayFromAndroidCalendarIntent(intent: Intent): Jdn? {
    // Follows https://github.com/FossifyOrg/Calendar/blob/fb56145d/app/src/main/kotlin/org/fossify/calendar/activities/MainActivity.kt#L531-L554
    // Receives content://com.android.calendar/time/1740774600000 or content://0@com.android.calendar/time/1740774600000
    val data = intent.data ?: return null

    when (CalendarContract.AUTHORITY) {
        data.authority, data.authority?.substringAfter("@") -> Unit
        else -> return null
    }

    when {
        data.path?.startsWith("/time") == true -> Unit
        intent.getBooleanExtra("DETAIL_VIEW", false) -> Unit
        else -> return null
    }

    val time = data.pathSegments?.last()?.toLongOrNull() ?: return null
    return Jdn(Date(time).toGregorianCalendar().toCivilDate())
}
